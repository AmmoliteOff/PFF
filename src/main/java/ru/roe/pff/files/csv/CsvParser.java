package ru.roe.pff.files.csv;

import org.springframework.stereotype.Component;
import ru.roe.pff.files.FileParser;
import ru.roe.pff.processing.DataRow;
import ru.roe.pff.processing.DataRowValidator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class CsvParser extends FileParser {

    @Override
    public List<DataRow> parse(DataRowValidator validator, InputStream input) throws IOException {
        List<DataRow> dataRows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            String line;
            int index = 0;
            int skuIndex = 0;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");

                if (index == 0) {
                    skuIndex = Arrays.stream(data).map(String::toLowerCase).toList().indexOf("sku");
                }
                dataRows.add(new DataRow(Arrays.asList(data), index, skuIndex));
                validator.validateRow(dataRows.get(index));
                index++;
            }
        }
        return dataRows;
    }
}