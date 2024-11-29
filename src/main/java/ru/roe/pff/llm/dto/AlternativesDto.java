package ru.roe.pff.llm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AlternativesDto(MessageDto message, String status) {
}
