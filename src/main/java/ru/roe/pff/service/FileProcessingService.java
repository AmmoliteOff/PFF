package ru.roe.pff.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.roe.pff.entity.ErrorSolve;
import ru.roe.pff.entity.FeedFile;
import ru.roe.pff.entity.FileError;
import ru.roe.pff.enums.ErrorType;
import ru.roe.pff.exception.ApiException;
import ru.roe.pff.files.xml.XmlGenerator;
import ru.roe.pff.files.xml.XmlParser;
import ru.roe.pff.llm.service.LLMService;
import ru.roe.pff.processing.DataRow;
import ru.roe.pff.processing.LlmWarnings;
import ru.roe.pff.repository.FileErrorRepository;
import ru.roe.pff.repository.FileRepository;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileProcessingService {

    private final static int VERIFY_LINK_TIMEOUT_MILLIS = 5000;

    private final FileErrorRepository fileErrorRepository;
    private final MinioService minioService;
    private final FileRepository fileRepository;
    private final ExecutorService executorService;
    private final LLMService llmService;

    private final XmlParser xmlParser;
    private final XmlGenerator xmlGenerator;

    private final Queue<Runnable> taskQueue = new LinkedList<>();

    @Scheduled(fixedRate = 1000)
    private void processTaskQueue() {
        if (!taskQueue.isEmpty()) {
            var task = taskQueue.poll();
            try {
                log.debug("Submitting task...");
                executorService.submit(task).get();
            } catch (Exception e) {
                switch (e.getCause()) {
                    case SocketException se -> log.error("Could not connect to the provided link", se);
                    case null, default -> log.error("Error processing executor task: ", e);
                }
            }
        }
    }

    public void submitLinkToProcess(String link) {
        verifyLinkIsAccessible(link);
        taskQueue.add(() -> processLink(link));
    }

    public List<DataRow> getFrom(FeedFile feedFile, Integer begin, Integer end) {
        try (InputStream is = minioService.getFile(feedFile.getFileName())) {
            return xmlParser.parseFrom(begin, end, is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            new File(feedFile.getFileName()).delete();
        }
    }

    public void generateFixedFile(UUID fileId) {
        var feedFile = fileRepository.findById(fileId).orElseThrow();//todo
        var is = minioService.getFile(feedFile.getFileName());

        var dataRows = xmlParser.parseFrom(0, feedFile.getRowsCount(), is);
        var errors = fileErrorRepository.findAllByFeedFile(feedFile);

        for (var error : errors) {
            // todo: check `error solve` for null vals
            var row = dataRows.get(error.getRowIndex()).getData();
            row.set(error.getColumnIndex(), error.getErrorSolve().getValue());
        }
        xmlGenerator.saveNewXml(dataRows, "fixed_" + feedFile.getFileName());
    }

    void processFile(MultipartFile mf, String fileName, UUID fileId) {
        if (fileName == null) {
            throw new ApiException("No filename was provided");
        }

        uploadFileToMinio(mf, fileName);
        taskQueue.add(() -> proceedProcessing(fileName, fileId));
    }

    @SneakyThrows
    private void processLink(String link) {
        URI uri = new URI(link);
        try (ReadableByteChannel rbc = Channels.newChannel(uri.toURL().openStream())) {
            String safeFileName = getSafeFileName(link);
            safeFileName = getSafeFileName(LocalDateTime.now() + "_" + safeFileName);

            var feedFile = new FeedFile(safeFileName, 0, link);
            feedFile = fileRepository.save(feedFile);

            uploadFileToMinio(safeFileName, rbc);
            proceedProcessing(safeFileName, feedFile.getId());
        }
    }

    private void uploadFileToMinio(MultipartFile mf, String fileName) {
        log.debug("Uploading file to MinIO: {}", fileName);
        minioService.uploadFile(fileName, mf);
    }

    private void uploadFileToMinio(String fileName, ReadableByteChannel rbc) throws IOException {
        log.debug("Uploading file to MinIO: {}", fileName);
        try (var fos = new FileOutputStream(fileName); var fis = new FileInputStream(fileName)) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            minioService.uploadFile(fileName, fis);
        } finally {
            new File(fileName).delete();
        }
    }

    private String getSafeFileName(String link) {
        String sub = link.substring(link.indexOf("://") + 3)
                .replaceAll("[<>:\"/|*]", "_");
        var lastIndex = sub.lastIndexOf('?');
        return lastIndex != -1 ? sub.substring(0, lastIndex) : sub;
    }

    public void proceedProcessing(String fileName, UUID fileId) {
        log.debug("Processing file: {}", fileName);
        var fileStream = minioService.getFile(fileName);

        FeedFile feedFile = fileRepository.findById(fileId).orElseThrow();
        var rows = xmlParser.parse(feedFile.getId(), fileStream);
        var aiSuggestions = llmService.checkForTitleChange(rows);
        addAiSuggestions(aiSuggestions, feedFile);
        feedFile.setRowsCount(rows.size());
        fileRepository.save(feedFile);

        log.debug("Processed and parsed file: {}", fileName);
    }

    private void addAiSuggestions(List<LlmWarnings> aiSuggestions, FeedFile feedFile) {
        for (var suggestion : aiSuggestions) {
            var error = new FileError(
                    null,
                    feedFile,
                    suggestion.getMessage(),
                    new ErrorSolve(null, suggestion.getValue()),
                    ErrorType.AI,
                    suggestion.getRowIndex(),
                    suggestion.getColumnIndex() - 1,
                    false
            );
            fileErrorRepository.save(error);
        }
    }

    private void verifyLinkIsAccessible(String link) {
        log.debug("Verifying provided link... ({})", link);
        try {
            var url = new URI(link).toURL();
            var connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(VERIFY_LINK_TIMEOUT_MILLIS);
            connection.setReadTimeout(VERIFY_LINK_TIMEOUT_MILLIS);
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new ApiException("Provided link is unreachable: " + link);
            }
            log.debug("Link verified: {}", link);
        } catch (URISyntaxException | IOException e) {
            throw new ApiException("Provided link is unreachable: " + link);
        }
    }

}