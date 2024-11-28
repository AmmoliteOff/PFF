package ru.roe.pff.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.roe.pff.dto.in.FeedFileDto;
import ru.roe.pff.dto.out.FeedFileResponseDto;
import ru.roe.pff.interfaces.CrudInterface;
import ru.roe.pff.repository.FileRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService implements CrudInterface<FeedFileDto, FeedFileResponseDto, UUID> { //TODO
    private final FileProcessingService fileProcessingService;
    private final FileRepository fileRepository;

    @Override
    public FeedFileResponseDto get(UUID uuid) {
        return null;
    }

    @Override
    public List<FeedFileResponseDto> getAll() {
        return List.of();
    }

    @Override
    public FeedFileResponseDto create(FeedFileDto object) {
        fileProcessingService.addFileToQueue(null);
        return null;
    }

    @Override
    public FeedFileResponseDto update(UUID uuid, FeedFileDto object) {
        return null;
    }

    @Override
    public void delete(UUID uuid) {

    }
}
