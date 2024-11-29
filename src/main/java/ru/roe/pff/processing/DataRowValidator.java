package ru.roe.pff.processing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.roe.pff.entity.FileError;
import ru.roe.pff.enums.ErrorType;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class DataRowValidator {
    private final List<FileError> fileErrorsBatch = new ArrayList<>();
    private static final int ERROR_BATCH_SIZE = 10;
    private long lastParsedId = 0;

    @Transactional
    public List<FileError> validateRow(DataRow row, List<String> titles) {
        fileErrorsBatch.clear();
        for (int i = 1; i < row.getData().size(); i++) {
            String columnName = titles.get(i);
            String cellValue = row.getData().get(i);

            if (cellValue.isEmpty()) {
                addErrorToBatch(
                    "Empty cell found in column: " + columnName,
                    ErrorType.TECHNICAL,
                    row.getIndex(),
                    i
                );
                continue;
            }

            if (columnName.equalsIgnoreCase("id")) {
                validatePrimaryId(row, cellValue, columnName, i);
                continue;
            }

            if (isAdditionalIdColumn(columnName)) {
                validateAdditionalId(row, cellValue, columnName, i);
            }

            if (isNumericValue(cellValue)) {
                validateNumericValue(row, cellValue, columnName, i);
            }
        }
        return fileErrorsBatch;
    }

    protected boolean isNumericValue(String cellValue) {
        try {
            Double.parseDouble(cellValue);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    protected boolean isAdditionalIdColumn(String columnName) {
        return List.of("внешний id", "sku", "uuid", "артикул").contains(columnName);
    }

    protected void validateNumericValue(DataRow row, String cellValue, String columnName, int columnIndex) {
        try {
            double numericValue = Double.parseDouble(cellValue);
            if (numericValue < 0) {
                addErrorToBatch(
                    "Negative value found in column: " + columnName,
                    ErrorType.TECHNICAL,
                    row.getIndex(),
                    columnIndex
                );
            }
            if (numericValue == 0 && columnName.contains("цена")) {
                addErrorToBatch(
                    "Invalid price value (0) in column: " + columnName,
                    ErrorType.TECHNICAL,
                    row.getIndex(),
                    columnIndex
                );
            }
            if (numericValue >= 100.0 && columnName.contains("скидка")) {
                addErrorToBatch(
                    "Invalid discount value (>=100) in column: " + columnName,
                    ErrorType.TECHNICAL,
                    row.getIndex(),
                    columnIndex
                );
            }
        } catch (NumberFormatException e) {
            addErrorToBatch(
                "Invalid numeric value in column: " + columnName,
                ErrorType.TECHNICAL,
                row.getIndex(),
                columnIndex
            );
        }
    }

    protected void validatePrimaryId(DataRow row, String cellValue, String columnName, int columnIndex) {
        try {
            long parsedId = Long.parseLong(cellValue);
            if (parsedId <= 0) {
                addErrorToBatch(
                    "Invalid ID (<0) found: " + cellValue + " in column: " + columnName,
                    ErrorType.TECHNICAL,
                    row.getIndex(),
                    columnIndex
                );
            }
            if (lastParsedId >= parsedId) {
                addErrorToBatch(
                    "Duplicate ID found: " + cellValue + " in column: " + columnName,
                    ErrorType.TECHNICAL,
                    row.getIndex(),
                    columnIndex
                );
            }
            lastParsedId = parsedId;
        } catch (NumberFormatException e) {
            addErrorToBatch(
                "Invalid ID found: " + cellValue + " in column: " + columnName,
                ErrorType.TECHNICAL,
                row.getIndex(),
                columnIndex
            );
        }
    }

    protected void validateAdditionalId(DataRow row, String cellValue, String columnName, int columnIndex) {
        // Additional validation logic can be added here
    }

    protected void addErrorToBatch(String error, ErrorType errorType, int rowIndex, int columnIndex) {
        FileError fileError = new FileError();
        fileError.setError(error);
        fileError.setErrorType(errorType);
        fileError.setRowIndex(rowIndex);
        fileError.setColumnIndex(columnIndex);
        fileErrorsBatch.add(fileError);
    }
}