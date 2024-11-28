package ru.roe.pff.dto.in;

import java.util.Objects;

public record FileLinkDto(String link) {

    public FileLinkDto {
        Objects.requireNonNull(link);
    }
}
