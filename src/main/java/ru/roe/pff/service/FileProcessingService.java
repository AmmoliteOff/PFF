package ru.roe.pff.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
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
import ru.roe.pff.files.csv.CsvParser;
import ru.roe.pff.files.xlsx.XlsxParser;
import ru.roe.pff.processing.DataRow;
import ru.roe.pff.processing.DataRowValidator;
import ru.roe.pff.repository.FileErrorRepository;
import ru.roe.pff.repository.FileRepository;
import ru.roe.pff.repository.FileRequestRepository;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileProcessingService {

    private final FileErrorRepository fileErrorRepository;
    private final MinioService minioService;
    private final FileRequestRepository fileRequestRepository;
    private final FileRepository fileRepository;
    private final ExecutorService executorService;

    private final Queue<Object> queue = new LinkedList<>();

    @Scheduled(fixedRate = 1000)
    public void processFiles() {
        if (!queue.isEmpty()) {
            var obj = queue.poll();
            executorService.submit(() -> {
                try {
                    processQueueElement(obj);
                } catch (IOException | URISyntaxException e) {
                    log.error("Error processing file: ", e);
                }
            });
        }
    }

    public List<DataRow> getFrom(FeedFile feedFile, Integer begin, Integer end){
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
        if (obj instanceof MultipartFile mf) {
            processFile(mf);
        } else if (obj instanceof FileLinkDto linkDto) {
            processLink(linkDto.link());
        }
    }

    void processFile(MultipartFile mf) throws IOException {
        String fileName = mf.getOriginalFilename();
        if (fileName == null) {
            throw new ApiException("No filename was provided");
        }

        uploadFileToMinio(mf, fileName);

        try (InputStream is = mf.getInputStream()) {
            proceedProcessing(is, fileName);
        }
    }

    private void processLink(String link) throws IOException, URISyntaxException {
        URI uri = new URI(link);
        try (ReadableByteChannel rbc = Channels.newChannel(uri.toURL().openStream())) {
            String safeFileName = getSafeFileName(link);
            uploadFileToMinio(safeFileName, rbc);

            try (InputStream is = new FileInputStream(safeFileName)) {
                proceedProcessing(is, safeFileName);
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
        return "temp_file_" + sub.substring(0, sub.lastIndexOf('?'));
    }

    private void proceedProcessing(InputStream is, String fileName) throws IOException {
        log.debug("Processing file: {}", fileName);

        String fileType = getFileExtension(fileName);
        FileParser parser = getParser(fileType);

        FeedFile feedFile = new FeedFile(null, fileName, List.of(), 0);
        DataRowValidator validator = new DataRowValidator(feedFile, fileErrorRepository);
        int rowsCount = parser.parse(validator, is);
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
            case "csv" -> new CsvParser();
            case "xlsx" -> new XlsxParser();
            default -> throw new IllegalArgumentException("Unsupported file type: " + fileType);
        };
    }
}