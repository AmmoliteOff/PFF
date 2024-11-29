package ru.roe.pff.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.roe.pff.dto.in.FileLinkDto;
import ru.roe.pff.entity.FeedFile;
import ru.roe.pff.entity.FileRequest;
import ru.roe.pff.enums.FileRequestType;
import ru.roe.pff.exception.ApiException;
import ru.roe.pff.files.FileParser;
import ru.roe.pff.files.xml.XmlGenerator;
import ru.roe.pff.files.xml.XmlParser;
import ru.roe.pff.processing.DataRow;
import ru.roe.pff.processing.DataRowValidator;
import ru.roe.pff.repository.FileErrorRepository;
import ru.roe.pff.repository.FileRepository;
import ru.roe.pff.repository.FileRequestRepository;

import java.io.*;
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

    private final FileErrorRepository fileErrorRepository;
    private final MinioService minioService;
    private final FileRequestRepository fileRequestRepository;
    private final XmlGenerator xmlGenerator;
    private final FileRepository fileRepository;
    private final ExecutorService executorService;

    private final Queue<Object> queue = new LinkedList<>();

    @Scheduled(fixedRate = 1000)
    public void processFiles() {
        if (!queue.isEmpty()) {
            var obj = queue.poll();
            try {
                executorService.submit(() -> {
                    try {
                        processQueueElement(obj);
                    } catch (IOException | URISyntaxException e) {
                        log.error("Error processing file: ", e);
                    }
                }).get();
            } catch (Exception e) {
                log.error("Error processing executor task: ", e);
            }
        }
    }

    public List<DataRow> getFrom(FeedFile feedFile, Integer begin, Integer end) {
        String fileType = getFileExtension(feedFile.getFileName());
        FileParser parser = getParser(fileType);
        try (InputStream is = minioService.getFile(feedFile.getFileName())) {
            return parser.parseFrom(begin, end, is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            new File(feedFile.getFileName()).delete();
        }
    }

    public void addToQueue(Object file) {
        queue.add(file);
    }

    private void processQueueElement(Object obj) throws IOException, URISyntaxException {
        if (obj instanceof FileLinkDto linkDto) {
            processLink(linkDto.link());
        }
//        else if (obj instanceof MultipartFile mf) {
//            processFile(mf);
//        }
    }

    public void generateFixedFile(UUID fileId) {
        var feedFile = fileRepository.findById(fileId).orElseThrow();//todo
        var is = minioService.getFile(feedFile.getFileName());
        var parser = getParser(getFileExtension(feedFile.getFileName()));
        try {
            var dataRows = parser.parseFrom(0, feedFile.getRowsCount(), is);
            var errors = fileErrorRepository.findAllByFeedFile(feedFile);
            for (var error : errors) {
                var row = dataRows.get(error.getRowIndex()).getData();
                row.set(error.getErrorSolve().getColumnIndex(), error.getErrorSolve().getValue());
            }
            xmlGenerator.saveNewXml(dataRows, "fixed_" + feedFile.getFileName());
        } catch (IOException e) {
            log.error("Error while generating fixed file: ", e);
        }
    }

    void processFile(MultipartFile mf, String fileName, UUID fileId) throws IOException {
        if (fileName == null) {
            throw new ApiException("No filename was provided");
        }

        uploadFileToMinio(mf, fileName);
        try {
            executorService.submit(() -> proceedProcessing(fileName, fileId)).get();
        } catch (Exception e) {
            log.error("Error processing executor task: ", e);
        }
    }

    private void processLink(String link) throws IOException, URISyntaxException {
        URI uri = new URI(link);
        try (ReadableByteChannel rbc = Channels.newChannel(uri.toURL().openStream())) {
            String safeFileName = getSafeFileName(link);
            safeFileName = getSafeFileName(LocalDateTime.now() + "_" + safeFileName);
            var feedFile = new FeedFile(null, safeFileName, 0);
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
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            minioService.uploadFile(fileName, new FileInputStream(fileName));
        }
    }

    private String getSafeFileName(String link) {
        String sub = link.substring(link.indexOf("://") + 3)
                .replaceAll("[<>:\"/|*]", "_");
        var lastIndex = sub.lastIndexOf('?');
        return lastIndex == -1 ? sub : sub.substring(0, lastIndex);
    }

    @SneakyThrows
    private void proceedProcessing(String fileName, UUID fileId) {
        log.debug("Processing file: {}", fileName);
        var fileStream = minioService.getFile(fileName);

        String fileType = getFileExtension(fileName);
        FileParser parser = getParser(fileType);

        FeedFile feedFile = fileRepository.findById(fileId).orElseThrow();
        DataRowValidator validator = new DataRowValidator(feedFile, fileErrorRepository);
        int rowsCount = parser.parse(validator, fileStream);
        feedFile.setRowsCount(rowsCount);
        fileRepository.save(feedFile);
        FileRequest fileRequest = new FileRequest(null, feedFile, FileRequestType.UPLOADED);
        fileRequestRepository.save(fileRequest);

        log.debug("Processed and parsed file: {}", fileName);
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    private FileParser getParser(String fileType) {
        return switch (fileType) {
            case "xml" -> new XmlParser();
            default -> throw new IllegalArgumentException("Unsupported file type: " + fileType);
        };
    }
}