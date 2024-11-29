package ru.roe.pff.llm.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PromtMessage {
    private String role;
    private String text;
}
