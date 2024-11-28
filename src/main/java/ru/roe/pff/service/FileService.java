package ru.roe.pff.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.roe.pff.dto.in.FeedFileDto;
import ru.roe.pff.dto.in.FileLinkDto;
import ru.roe.pff.dto.out.FeedFileResponseDto;
import ru.roe.pff.processing.DataRow;
import ru.roe.pff.repository.FileRepository;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {
    private static final Integer ELEMENTS_PER_PAGE = 25;
    private final FileProcessingService fileProcessingService;
    private final FileRepository fileRepository;

    public void createFromFile(MultipartFile file) {
        try {
            // TODO: proceed processing ASYNC!
            fileProcessingService.processFile(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        fileProcessingService.addToQueue(file);
    }

    public void createFromLink(FileLinkDto object) {
        fileProcessingService.addToQueue(object);
    }

    public List<DataRow> getDataRowsByPage(UUID id, Integer page) {
        var feedFile = fileRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(String.format("Feed File with UUID %s not found", id)));
        var rowsCount = feedFile.getRowsCount();
        var step = rowsCount/ELEMENTS_PER_PAGE;
        var begin = (page-1) * step;
        var end = page*step;
        return fileProcessingService.getFrom(feedFile, begin, end);
    }
}
