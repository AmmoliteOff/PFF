package ru.roe.pff.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.roe.pff.dto.in.FeedFileDto;
import ru.roe.pff.dto.in.FileLinkDto;
import ru.roe.pff.dto.out.FeedFileResponseDto;
import ru.roe.pff.entity.FeedFile;
import ru.roe.pff.processing.DataRow;
import ru.roe.pff.service.FileService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class FeedFileController {
    private final FileService fileService;

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

    @GetMapping("/{id}/{page}")
    public List<DataRow> getDataRowsByPage(@PathVariable UUID id, @PathVariable Integer page){
        return fileService.getDataRowsByPage(id, page);
    }

    @GetMapping("/{id}")
    public FeedFile get(@PathVariable UUID id) {
        return fileService.getById(id);
    }

    @GetMapping
    public List<FeedFile> getAll() {
        return fileService.getAll();
    }
}
