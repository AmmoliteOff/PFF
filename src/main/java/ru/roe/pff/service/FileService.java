package ru.roe.pff.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.roe.pff.dto.in.FileLinkDto;
import ru.roe.pff.entity.FeedFile;
import ru.roe.pff.entity.FeedFileLink;
import ru.roe.pff.processing.DataRow;
import ru.roe.pff.repository.FeedFileLinkRepository;
import ru.roe.pff.repository.FileRepository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {
    private static final Integer ELEMENTS_PER_PAGE = 25;
    private final FileProcessingService fileProcessingService;
    private final FeedFileLinkRepository linkRepository;
    private final FileRepository fileRepository;
    private final MinioService minioService;

    public ResponseEntity<String> getFixedFile(String fileName) {
        var fileStream = minioService.getFile(fileName);
        var xmlContent = new Scanner(fileStream, StandardCharsets.UTF_8)
                .useDelimiter("\\A")
                .next();

        var headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/xml");
        return new ResponseEntity<>(xmlContent, headers, HttpStatus.OK);
    }

    public void createFromFile(MultipartFile file) {
        var fileName = file.getOriginalFilename();
        fileName = getSafeFileName(LocalDateTime.now() + "_" + fileName);
        var feedFile = new FeedFile(null, fileName, 0);
        feedFile = fileRepository.save(feedFile);
        fileProcessingService.processFile(file, fileName, feedFile.getId());
        //        fileProcessingService.addToQueue(file);
    }

    public void createFromLink(FileLinkDto dto) {
        // ONLY 1 link can exist for now => must override
        linkRepository.deleteAll();
        linkRepository.saveAndFlush(new FeedFileLink(null, dto.link()));
        fileProcessingService.submitLinkToProcess(dto.link());
    }

    public List<DataRow> getDataRowsByPage(UUID id, Integer page) {
        var feedFile = fileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Feed File with UUID %s not found", id)));
        var begin = (page - 1) * ELEMENTS_PER_PAGE;
        var end = page * ELEMENTS_PER_PAGE;
        return fileProcessingService.getFrom(feedFile, begin, end);
    }

    public FeedFile getById(UUID id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Feed File with UUID %s not found", id)));
    }

    public void completeFile(UUID fileId) {
        fileProcessingService.generateFixedFile(fileId);
    }

    public List<FeedFile> getAll() {
        return fileRepository.findAll();
    }

    private String getSafeFileName(String link) {
        return link.substring(link.indexOf("://") + 3)
                .replaceAll("[<>:\"/|*]", "_");
    }
}
