package ru.roe.pff.llm.utils;

import lombok.Data;

@Data
public class CompletionOptions {
    private final boolean stream;
    private final Double temperature;
    private final Integer maxTokens;
}
