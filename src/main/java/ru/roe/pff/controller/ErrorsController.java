package ru.roe.pff.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import ru.roe.pff.dto.in.ErrorSolveDto;
import ru.roe.pff.dto.out.FileErrorDto;
import ru.roe.pff.dto.out.PagesCountDto;
import ru.roe.pff.entity.FileError;
import ru.roe.pff.enums.ErrorType;
import ru.roe.pff.service.ErrorService;

import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/error")
public class ErrorsController {
    private final ErrorService errorService;

    @PostMapping
    public void fix(@RequestBody ErrorSolveDto fixDto) {
        errorService.fix(fixDto);
    }

    @PostMapping("/suppress/{id}")
    public void suppressError(@PathVariable UUID id) {
        errorService.suppressError(id);
    }

    @PostMapping("/suppress/all/{fileId}")
    public void suppressAll(@PathVariable UUID fileId) {
        errorService.suppressAll(fileId);
    }

    @PostMapping("/ai/{fileId}")
    public void fixWithAi(@PathVariable UUID fileId) {
        errorService.useAiForAll(fileId);
    }

    @GetMapping("/{id}/{page}")
    public Page<FileError> getErrors(@PathVariable UUID id, @PathVariable int page) {
        return errorService.getEntitiesPaginatedAndSortedByCreatedAtDesc(page, 10, id);
    }

    @GetMapping("/{id}/pages")
    public PagesCountDto getErrors(@PathVariable UUID id) {
        return errorService.getPagesCount(id);
    }

    @GetMapping("/filter/{id}/{page}")
    public Page<FileError> getErrors(@PathVariable UUID id, @PathVariable int page, @RequestParam("errorType") ErrorType errorType) {
        return errorService.getErrorsFiltered(id, page, errorType);
    }


    @PostMapping("/dropall/{id}")
    public void dropSolves(@PathVariable UUID id) {
        errorService.dropAll(id);
    }


    @GetMapping("/{id}")
    public List<FileErrorDto> getErrorsByFileId(@PathVariable UUID id) {
        return errorService.getErrorsByFileId(id);
    }
}
