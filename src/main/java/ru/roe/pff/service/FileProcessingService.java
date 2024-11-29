package ru.roe.pff.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.roe.pff.entity.FeedFile;
import ru.roe.pff.exception.ApiException;
import ru.roe.pff.files.FileParser;
import ru.roe.pff.files.xml.XmlGenerator;
import ru.roe.pff.files.xml.XmlParser;
import ru.roe.pff.processing.DataRow;
import ru.roe.pff.repository.FileErrorRepository;
import ru.roe.pff.repository.FileRepository;

import java.io.*;
import java.net.SocketException;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileProcessingService {

    private final FileErrorRepository fileErrorRepository;
    private final MinioService minioService;
    private final FileRepository fileRepository;
    private final ExecutorService executorService;

    private final XmlParser xmlParser;
    private final XmlGenerator xmlGenerator;

    public void submitLinkToProcess(String link) {
        try {
            executorService.submit(() -> processLink(link)).get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof SocketException) {
                log.error("Could not connect to the provided link: {}", link, e);
            }
        } catch (Exception e) {
            log.error("Error processing executor task: ", e);
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

    public void generateFixedFile(UUID fileId) {
        var feedFile = fileRepository.findById(fileId).orElseThrow();//todo
        var is = minioService.getFile(feedFile.getFileName());
        var parser = getParser(getFileExtension(feedFile.getFileName()));
        var dataRows = parser.parseFrom(0, feedFile.getRowsCount(), is);
        var errors = fileErrorRepository.findAllByFeedFile(feedFile);
        for (var error : errors) {
            // todo: check `error solve` for null vals
            var row = dataRows.get(error.getRowIndex()).getData();
            row.set(error.getColumnIndex(), error.getErrorSolve().getValue());
        }
        saveNewXml(dataRows, "fixed_" + feedFile.getFileName());
    }

    void processFile(MultipartFile mf, String fileName, UUID fileId) {
        if (fileName == null) {
            throw new ApiException("No filename was provided");
        }

        uploadFileToMinio(mf, fileName);

        try {
            executorService.submit(() -> {
                try (InputStream is = mf.getInputStream()) {
                    proceedProcessing(is, fileName, fileId);
                } catch (IOException e) {
                    log.error("Error processing file: ", e);
                }
            }).get();
        } catch (Exception e) {
            log.error("Error processing file: ", e);
        }
    }

    private void saveNewXml(List<DataRow> dataRows, String fileName) {
        xmlGenerator.saveNewXml(dataRows, fileName);
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

            try (InputStream is = new FileInputStream(safeFileName)) {
                proceedProcessing(is, safeFileName, feedFile.getId());
            } catch (IOException e) {
                log.error(e.getLocalizedMessage());
            } finally {
                new File(safeFileName).delete();
            }
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
        return lastIndex != -1 ? sub.substring(0, lastIndex) : sub;
    }

    public void proceedProcessing(InputStream is, String fileName, UUID fileId) {
        log.debug("Processing file: {}", fileName);

        String fileType = getFileExtension(fileName);
        FileParser parser = getParser(fileType);

        FeedFile feedFile = fileRepository.findById(fileId).orElseThrow();
        int rowsCount = parser.parse(feedFile.getId(), is);
        feedFile = fileRepository.findById(fileId).orElseThrow();
        feedFile.setRowsCount(rowsCount);
        fileRepository.save(feedFile);

        log.debug("Processed and parsed file: {}", fileName);
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    private FileParser getParser(String fileType) {
        return switch (fileType) {
            case "xml" -> xmlParser;
            default -> throw new IllegalArgumentException("Unsupported file type: " + fileType);
        };
    }
}