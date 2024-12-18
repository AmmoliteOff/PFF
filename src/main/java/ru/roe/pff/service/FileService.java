package ru.roe.pff.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.roe.pff.dto.in.FileLinkDto;
import ru.roe.pff.dto.out.FileErrorDto;
import ru.roe.pff.dto.out.PagesCountDto;
import ru.roe.pff.entity.FeedFile;
import ru.roe.pff.entity.FeedFileLink;
import ru.roe.pff.entity.FixedFeedFileLog;
import ru.roe.pff.enums.FileStatus;
import ru.roe.pff.processing.DataRow;
import ru.roe.pff.repository.FeedFileLinkLogRepository;
import ru.roe.pff.repository.FeedFileLinkRepository;
import ru.roe.pff.repository.FileRepository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {
    private static final Integer ELEMENTS_PER_PAGE = 25;
    private final FileProcessingService fileProcessingService;
    private final FeedFileLinkLogRepository linkLogRepository;
    private final FeedFileLinkRepository linkRepository;
    private final FileRepository fileRepository;
    private final MinioService minioService;
    private final ErrorService errorService;

    @Transactional
    public ResponseEntity<String> getFixedFile() {

        // TODO: Get 'FIXED' feed file
        var latestFeedFile = fileRepository.getLatestFixedLink().orElseThrow(EntityNotFoundException::new);

        var fileStream = minioService.getFile(latestFeedFile.getFixedFileName());
        var xmlContent = new Scanner(fileStream, StandardCharsets.UTF_8)
            .useDelimiter("\\A")
            .next();

        log.debug("Feed file ({}) was accessed through the static link => saved log", latestFeedFile.getFileName());
        linkLogRepository.save(new FixedFeedFileLog(latestFeedFile));

        var headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/xml");
        // TODO: uncomment to download right away upon GET request
        // headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=%s.xml".formatted(latestFeedFile
        // .getFileName()));
        return new ResponseEntity<>(xmlContent, headers, HttpStatus.OK);
    }

    @Transactional(readOnly = true)
    public Page<FixedFeedFileLog> getStaticLinkLogs(Pageable pageable) {
        return linkLogRepository.findAll(pageable);
    }

    public void createFromFile(MultipartFile file) {
        var fileName = file.getOriginalFilename();
        fileName = getSafeFileName(LocalDateTime.now() + "_" + fileName);
        var feedFile = new FeedFile(fileName, 0, null, FileStatus.CREATED);
        feedFile = fileRepository.save(feedFile);
        fileProcessingService.processFile(file, fileName, feedFile.getId());
        //        fileProcessingService.addToQueue(file);
    }

    @Transactional
    public void createFromLink(FileLinkDto dto) {
        // ONLY 1 link can exist for now => must override
        linkRepository.deleteAll();
        linkRepository.saveAndFlush(new FeedFileLink(null, dto.link()));
        fileProcessingService.submitLinkToProcess(dto.link());
    }

    @Transactional(readOnly = true)
    public List<DataRow> getDataRowsByPage(UUID id, Integer page) {
        var feedFile = fileRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(String.format("Feed File with UUID %s not found", id)));
        var begin = (page - 1) * ELEMENTS_PER_PAGE;
        var end = page * ELEMENTS_PER_PAGE;

        var errors = errorService.getErrorsByFileId(feedFile.getId());
        var errorsMap = new HashMap<Integer, List<FileErrorDto>>();
        for (var error : errors) {
            if (!errorsMap.containsKey(error.getRowIndex())) {
                errorsMap.put(error.getRowIndex(), new ArrayList<>() {{
                    add(error);
                }});
            } else {
                errorsMap.get(error.getRowIndex()).add(error);
            }
        }

        var semiResult = fileProcessingService.getFrom(feedFile, begin, end);

        for (var row : semiResult) {
            if (errorsMap.containsKey(row.getIndex())) {
                for (var error : errorsMap.get(row.getIndex())) {
                    if (error.getUseSolve()) {
                        row.getData().set(error.getColumnIndex(), error.getErrorSolve().getValue());
                    }
                }
            }
        }

        return semiResult;
    }

    @Transactional(readOnly = true)
    public FeedFile getById(UUID id) {
        return fileRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(String.format("Feed File with UUID %s not found", id)));
    }

    public void completeFile(UUID fileId) {
        fileProcessingService.generateFixedFile(fileId);
    }

    @Transactional(readOnly = true)
    public List<FeedFile> getAll() {
        return fileRepository.findAll();
    }

    @Transactional(readOnly = true)
    public PagesCountDto getPages(UUID id) {
        var count = fileRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(String.format("Feed File with UUID %s not found", id)))
            .getRowsCount();

        return new PagesCountDto((int) Math.ceil((double) count / ELEMENTS_PER_PAGE));
    }

    private String getSafeFileName(String link) {
        return link.substring(link.indexOf("://") + 3)
            .replaceAll("[<>:\"/|*]", "_");
    }
}
