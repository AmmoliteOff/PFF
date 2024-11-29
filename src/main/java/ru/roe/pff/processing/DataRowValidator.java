package ru.roe.pff.processing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.roe.pff.entity.FileError;
import ru.roe.pff.enums.ErrorType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Component
public class DataRowValidator {

    private final List<FileError> fileErrorsBatch = new ArrayList<>();

    private final Set<DataRow> seenDataRows = new HashSet<>();
    private final Set<String> seenArticles = new HashSet<>();
    private long lastParsedId = 0;

    public void resetDuplicateTracking() {
        seenDataRows.clear();
        seenArticles.clear();
        lastParsedId = 0;
    }

    @Transactional
    public List<FileError> validateRow(DataRow row, List<String> tagNames) {
        fileErrorsBatch.clear();
        validateDuplicateRow(row);
        for (int i = 0; i < row.getData().size(); i++) {
            String tagName = tagNames.get(i);
            String tagValue = row.getData().get(i);

            if (tagValue.isEmpty()) {
                addErrorToBatch(
                        "Пустое значение на позиции: " + tagName,
                        ErrorType.TECHNICAL,
                        row.getIndex(),
                        i
                );
                continue;
            }

            if (tagName.equalsIgnoreCase("id")) {
                validatePrimaryId(row, tagValue, tagName, i);
                continue;
            }

            if (isNumericValue(tagValue)) {
                validateNumericValue(row, tagValue, tagName, i);
            }

            if (tagName.toLowerCase().contains("артикул") || tagName.toLowerCase().contains("article")) {
                validateDuplicateArticle(row, tagValue, i);
            }
        }
        return fileErrorsBatch;
    }

    private void validateDuplicateArticle(DataRow row, String tagValue, int tagIndex) {
        if (seenArticles.contains(tagValue)) {
            addErrorToBatch(
                    "Дубликат артикула: " + tagValue,
                    ErrorType.TECHNICAL,
                    row.getIndex(),
                    tagIndex
            );
        } else {
            seenArticles.add(tagValue);
        }
    }

    private void validateDuplicateRow(DataRow row) {
        if (seenDataRows.contains(row)) {
            addErrorToBatch(
                    "Дубликат целой записи",
                    ErrorType.TECHNICAL,
                    row.getIndex(),
                    -1
            );
        } else {
            seenDataRows.add(row);
        }
    }

    protected boolean isNumericValue(String cellValue) {
        try {
            Double.parseDouble(cellValue);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    protected void validateNumericValue(DataRow row, String tagValue, String tagName, int tagIndex) {
        try {
            double numericValue = Double.parseDouble(tagValue);
            if (numericValue < 0) {
                addErrorToBatch(
                        "Отрицательное числовое значение на позиции: " + tagName,
                        ErrorType.TECHNICAL,
                        row.getIndex(),
                        tagIndex
                );
            }
            if (numericValue == 0 && tagName.equals("price")) {
                addErrorToBatch(
                        "Неправильное значение цены (ноль) на позиции: " + tagName,
                        ErrorType.TECHNICAL,
                        row.getIndex(),
                        tagIndex
                );
            }
            if (numericValue >= 100.0 && tagName.toLowerCase().contains("скидка")) {
                addErrorToBatch(
                        "Неправильное значение скидки (>=100) на позиции: " + tagName,
                        ErrorType.TECHNICAL,
                        row.getIndex(),
                        tagIndex
                );
            }
        } catch (NumberFormatException e) {
            addErrorToBatch(
                    "Неправильное числовое значение на позиции: " + tagName,
                    ErrorType.TECHNICAL,
                    row.getIndex(),
                    tagIndex
            );
        }
    }

    protected void validatePrimaryId(DataRow row, String tagValue, String tagName, int tagIndex) {
        if (row.getIndex() == -1) {
            return;
        }
        try {
            long parsedId = Long.parseLong(tagValue);
            if (parsedId <= 0) {
                addErrorToBatch(
                        "Неправильный ID (<0): " + tagValue,
                        ErrorType.TECHNICAL,
                        row.getIndex(),
                        tagIndex
                );
            }
            if (lastParsedId >= parsedId) {
                addErrorToBatch(
                        "Дубликат ID: " + tagValue,
                        ErrorType.TECHNICAL,
                        row.getIndex(),
                        tagIndex
                );
            }
            lastParsedId = parsedId;
        } catch (NumberFormatException e) {
            addErrorToBatch(
                    "Неправильный ID: " + tagValue,
                    ErrorType.TECHNICAL,
                    row.getIndex(),
                    tagIndex
            );
        }
    }

    protected void addErrorToBatch(String error, ErrorType errorType, int rowIndex, int tagIndex) {
        FileError fileError = new FileError();
        fileError.setError(error);
        fileError.setErrorType(errorType);
        fileError.setSuppressed(false);
        fileError.setRowIndex(rowIndex);
        fileError.setColumnIndex(tagIndex);
        fileErrorsBatch.add(fileError);
    }

}