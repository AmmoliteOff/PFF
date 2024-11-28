package ru.roe.pff.files.xlsx;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import ru.roe.pff.files.FileParser;
import ru.roe.pff.processing.DataRow;
import ru.roe.pff.processing.DataRowValidator;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Component
public class XlsxParser extends FileParser {

    @Override
    public void parse(DataRowValidator validator, InputStream input) throws IOException {
        IOUtils.setByteArrayMaxOverride(1000000000);
        try (XSSFWorkbook workbook = new XSSFWorkbook(input)) {
            var sheet = workbook.getSheetAt(0); // Считываем первый лист
            var rowIterator = sheet.iterator();

            int index = 0;
            int skuIndex = -1;
            int maxColumnsCount = sheet.getRow(0).getLastCellNum();
            while (rowIterator.hasNext()) {
                var row = rowIterator.next();
                var rowData = new ArrayList<String>();

                IntStream.range(0, maxColumnsCount).forEach(i -> {
                    var cell = row.getCell(i);
                    rowData.add(getCellValue(cell));
                });

//                Iterator<Cell> cellIterator = row.iterator();
//                while (cellIterator.hasNext()) {
//                    Cell cell = cellIterator.next();
//                    rowData.add(getCellValue(cell));
//                }

                if (index == 0) {
                    // Определяем индекс колонки с SKU в первой строке
                    skuIndex = getSkuIndex(rowData);
                }

                validator.validateRow(new DataRow(rowData, index, skuIndex));
                index++;
            }
        }
    }

    // Метод для получения значения ячейки в виде строки
    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    // Метод для поиска индекса колонки с SKU в первой строке
    private int getSkuIndex(List<String> rowData) {
        for (int i = 0; i < rowData.size(); i++) {
            if (rowData.get(i).toLowerCase().contains("внешний id")) {
                return i;
            }
        }
        return -1; // Если не нашли SKU
    }
}
