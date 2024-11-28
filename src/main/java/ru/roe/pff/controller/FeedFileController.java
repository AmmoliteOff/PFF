package ru.roe.pff.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.roe.pff.dto.in.FeedFileDto;
import ru.roe.pff.dto.in.FileLinkDto;
import ru.roe.pff.dto.out.FeedFileResponseDto;
import ru.roe.pff.service.FileService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class FeedFileController {
    private final FileService fileService;


    public FeedFileResponseDto get(UUID uuid) {
        return fileService.get(uuid);
    }

    public List<FeedFileResponseDto> getAll() {
        return fileService.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void create(@RequestParam("file") MultipartFile file) {
        fileService.createFromFile(file);
    }

    @PostMapping("/link")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void create(@RequestBody FileLinkDto object) {
        fileService.createFromLink(object);
    }

    public FeedFileResponseDto update(UUID uuid, FeedFileDto object) {
        return fileService.update(uuid, object);
    }

    public void delete(UUID uuid) {
        fileService.delete(uuid);
    }
}
