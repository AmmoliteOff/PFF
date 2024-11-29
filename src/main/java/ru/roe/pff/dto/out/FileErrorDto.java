package ru.roe.pff.dto.out;

import jakarta.persistence.*;
import ru.roe.pff.entity.ErrorSolve;
import ru.roe.pff.entity.FeedFile;
import ru.roe.pff.enums.ErrorType;

import java.util.UUID;

public record FileErrorDto(UUID id,
                           String error, ErrorSolve errorSolve,
                           ErrorType errorType, Integer rowIndex,
                           Integer columnIndex,
                           Boolean suppressed) {
}
