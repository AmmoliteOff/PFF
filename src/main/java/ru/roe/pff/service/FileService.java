package ru.roe.pff.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.roe.pff.dto.in.FeedFileDto;
import ru.roe.pff.dto.in.FileLinkDto;
import ru.roe.pff.dto.out.FeedFileResponseDto;
import ru.roe.pff.entity.FeedFile;
import ru.roe.pff.entity.FileRequest;
import ru.roe.pff.repository.FileRepository;
import ru.roe.pff.repository.FileRequestRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService { //TODO
    private final FileProcessingService fileProcessingService;
    private final FileRepository fileRepository;
    
    public FeedFileResponseDto get(UUID uuid) {
        return null;
    }
    
    public List<FeedFileResponseDto> getAll() {
        return List.of();
    }

    public void createFromLink(FileLinkDto object) {
        fileProcessingService.addLinkToQueue(object);
    }

    public FeedFileResponseDto update(UUID uuid, FeedFileDto object) {
        return null;
    }

    public void delete(UUID uuid) {

    }
}
