package ru.roe.pff.llm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public record PromtResultDto(List<AlternativesDto> alternatives) {
}
