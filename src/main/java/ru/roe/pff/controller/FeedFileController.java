package ru.roe.pff.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.roe.pff.dto.in.FeedFileDto;
import ru.roe.pff.dto.out.FeedFileResponseDto;
import ru.roe.pff.interfaces.CrudInterface;
import ru.roe.pff.service.FileService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class FeedFileController implements CrudInterface<FeedFileDto, FeedFileResponseDto, UUID> {
    private final FileService fileService;

    @Override
    public FeedFileResponseDto get(UUID uuid) {
        return fileService.get(uuid);
    }

    @Override
    public List<FeedFileResponseDto> getAll() {
        return fileService.getAll();
    }

    @Override
    public FeedFileResponseDto create(FeedFileDto object) {
        return fileService.create(object);
    }

    @Override
    public FeedFileResponseDto update(UUID uuid, FeedFileDto object) {
        return fileService.update(uuid, object);
    }

    @Override
    public void delete(UUID uuid) {
        fileService.delete(uuid);
    }
}
