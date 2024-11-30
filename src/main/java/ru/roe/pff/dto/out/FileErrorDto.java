package ru.roe.pff.dto.out;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.roe.pff.entity.ErrorSolve;
import ru.roe.pff.enums.ErrorType;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileErrorDto {
    private UUID id;
    private String title;
    private String description;
    private ErrorSolve errorSolve;
    private ErrorType errorType;
    private Integer rowIndex;
    private Integer columnIndex;
    private Boolean suppressed;
    private Boolean useSolve;
}
