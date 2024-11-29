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

    public void clearTrackingCollections() {
        seenDataRows.clear();
        seenArticles.clear();
    }

    @Transactional
    public List<FileError> validateRow(DataRow row, List<String> tagNames) {
        fileErrorsBatch.clear();
        validateDuplicateRow(row);
        for (int i = 1; i < row.getData().size(); i++) {
            String tagName = tagNames.get(i);
            String tagValue = row.getData().get(i);

            if (tagValue.isEmpty()) {
                addErrorToBatch(
                        "Empty value found in tag: " + tagName,
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
                    "Duplicate article found: " + tagValue,
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
                    "Duplicate offer entry",
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
                        "Negative value found in tag: " + tagName,
                        ErrorType.TECHNICAL,
                        row.getIndex(),
                        tagIndex
                );
            }
            if (numericValue == 0 && tagName.equals("price")) {
                addErrorToBatch(
                        "Invalid price value (0) in tag: " + tagName,
                        ErrorType.TECHNICAL,
                        row.getIndex(),
                        tagIndex
                );
            }
            if (numericValue >= 100.0 && tagName.toLowerCase().contains("скидка")) {
                addErrorToBatch(
                        "Invalid discount value (>=100) in tag: " + tagName,
                        ErrorType.TECHNICAL,
                        row.getIndex(),
                        tagIndex
                );
            }
        } catch (NumberFormatException e) {
            addErrorToBatch(
                    "Invalid numeric value in tag: " + tagName,
                    ErrorType.TECHNICAL,
                    row.getIndex(),
                    tagIndex
            );
        }
    }

    protected void validatePrimaryId(DataRow row, String tagValue, String tagName, int tagIndex) {
        try {
            long parsedId = Long.parseLong(tagValue);
            if (parsedId <= 0) {
                addErrorToBatch(
                        "Invalid ID (<0) found: " + tagValue + " in tag: " + tagName,
                        ErrorType.TECHNICAL,
                        row.getIndex(),
                        tagIndex
                );
            }
            if (lastParsedId >= parsedId) {
                addErrorToBatch(
                        "Duplicate ID found: " + tagValue + " in tag: " + tagName,
                        ErrorType.TECHNICAL,
                        row.getIndex(),
                        tagIndex
                );
            }
            lastParsedId = parsedId;
        } catch (NumberFormatException e) {
            addErrorToBatch(
                    "Invalid ID found: " + tagValue + " in tag: " + tagName,
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