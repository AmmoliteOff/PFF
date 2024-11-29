package ru.roe.pff.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.roe.pff.llm.service.LLMService;
import ru.roe.pff.processing.LlmWarnings;
import ru.roe.pff.service.FileService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/foo")
public class FooController {
    private final LLMService llmService;
    private final FileService fileService;

    @PostMapping("/{id}")
    public List<LlmWarnings> foo(@PathVariable UUID id) throws JsonProcessingException {
        return llmService.checkForTitleChange(fileService.getDataRowsByPage(id, 1));
    }
}
