package ru.roe.pff.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.roe.pff.dto.in.FeedFileDto;
import ru.roe.pff.dto.in.FileLinkDto;
import ru.roe.pff.dto.out.FeedFileResponseDto;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService { //TODO
    private final FileProcessingService fileProcessingService;

    public FeedFileResponseDto get(UUID uuid) {
        return null;
    }

    public List<FeedFileResponseDto> getAll() {
        return List.of();
    }

    public void createFromFile(MultipartFile file) {
        try {
            fileProcessingService.processFile(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        fileProcessingService.addToQueue(file);
    }

    public void createFromLink(FileLinkDto object) {
        fileProcessingService.addToQueue(object);
    }

    public FeedFileResponseDto update(UUID uuid, FeedFileDto object) {
        return null;
    }

    public void delete(UUID uuid) {

    }


}
