package ru.roe.pff.processing;

import lombok.RequiredArgsConstructor;
import ru.roe.pff.entity.FileError;
import ru.roe.pff.entity.FileRequest;
import ru.roe.pff.enums.ErrorType;
import ru.roe.pff.repository.FileErrorRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class DataRowValidator {
    private final FileRequest fileRequest;
    private final FileErrorRepository fileErrorRepository;
    private final Set<String> seenSkus = new HashSet<>();
    private final List<String> requiredTitles = new ArrayList<>() {{
        add("sku");
        add("title");
        add("price");
    }};
    private List<String> titles;


    public void validateRow(DataRow row) {
        if (row.getIndex() != 0) {
            String sku = row.getData().get(row.getSkuColumnIndex());

            if (sku != null) {
                if (!seenSkus.add(sku)) {
                    saveError("Duplicate SKU", ErrorType.LOGICAL, row.getIndex(), row.getSkuColumnIndex());
                }
            }

            for (var requiredTitle : requiredTitles) {
                if (row.getData().get(titles.indexOf(requiredTitle)).equalsIgnoreCase("")) {
                    saveError(
                        "Required field is missing",
                        ErrorType.TECHNICAL,
                        row.getIndex(),
                        titles.indexOf(requiredTitle)
                    );
                }
            }

            //TODO Другие проверки
        } else {
            titles = row.getData().stream().map(String::toLowerCase).toList();
        }
    }

    private void saveError(String error, ErrorType errorType, int rowIndex, int columnIndex) {
        FileError fileError = new FileError();
        fileError.setFileRequest(fileRequest);
        fileError.setError(error);
        fileError.setErrorType(errorType);
        fileError.setRowIndex(rowIndex);
        fileError.setColumnIndex(columnIndex);
        fileErrorRepository.save(fileError);
    }
}