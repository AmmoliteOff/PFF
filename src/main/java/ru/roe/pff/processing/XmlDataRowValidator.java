package ru.roe.pff.processing;

import ru.roe.pff.entity.FeedFile;
import ru.roe.pff.enums.ErrorType;
import ru.roe.pff.repository.FileErrorRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class XmlDataRowValidator extends DataRowValidator {
    // TODO: мб создать абстракцию для валидаторов

    private List<String> titles;
    private final List<String> requiredFields = List.of("price", "currencyId", "name", "barcode");
    private final Set<String> uniqueValues = new HashSet<>(); // Для проверки уникальных значений

    public XmlDataRowValidator(FeedFile feedFile, FileErrorRepository fileErrorRepository) {
        super(feedFile, fileErrorRepository);
    }

    // TODO: доработать!

    @Override
    public void validateRow(DataRow row) {
        if (row.getIndex() == 0) {
            titles = row.getData().stream().map(String::toLowerCase).toList();
        } else {
            for (int i = 0; i < row.getData().size(); i++) {
                String nodeName = titles.get(i);
                String nodeValue = row.getData().get(i);

                if (nodeValue.isEmpty() && requiredFields.contains(nodeName)) {
                    addErrorToBatch(
                            "Missing required field: " + nodeName,
                            ErrorType.TECHNICAL,
                            row.getIndex(),
                            i
                    );
                }

                if (nodeName.equalsIgnoreCase("price")) {
                    validateNumericValue(row, nodeValue, nodeName, i);
                }

                if (nodeName.equalsIgnoreCase("barcode")) {
                    validateUniqueValue(row, nodeValue, nodeName, i);
                }

                if (isAdditionalIdColumn(nodeName)) {
                    validateAdditionalId(row, nodeValue, nodeName, i);
                }
            }
        }
        saveBatch();
    }

    private void validateUniqueValue(DataRow row, String cellValue, String columnName, int columnIndex) {
        if (!uniqueValues.add(cellValue)) {
            addErrorToBatch(
                    "Duplicate value found in column: " + columnName + " (" + cellValue + ")",
                    ErrorType.TECHNICAL,
                    row.getIndex(),
                    columnIndex
            );
        }
    }

    @Override
    protected void validateAdditionalId(DataRow row, String cellValue, String columnName, int columnIndex) {
        if (cellValue.length() < 5) {
            addErrorToBatch(
                    "Invalid additional ID (too short) in column: " + columnName,
                    ErrorType.TECHNICAL,
                    row.getIndex(),
                    columnIndex
            );
        }
    }

    @Override
    protected boolean isAdditionalIdColumn(String columnName) {
        return List.of("external_id", "sku", "uuid", "barcode").contains(columnName.toLowerCase());
    }
}
