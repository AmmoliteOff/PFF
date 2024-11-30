package ru.roe.pff.processing;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LlmWarnings {
    private String title;
    private String message;
    private String value;
    private int rowIndex;
    private String column;
}
