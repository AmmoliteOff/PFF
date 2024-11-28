package ru.roe.pff.files.csv;

import org.springframework.stereotype.Component;
import ru.roe.pff.files.FileParser;
import ru.roe.pff.processing.DataRow;
import ru.roe.pff.processing.DataRowValidator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

@Component
public class CsvParser extends FileParser {

    @Override
    public void parse(DataRowValidator validator, InputStream input) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            String line;
            int index = 0;
            int skuIndex = 0;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");

                if (index == 0) {
                    skuIndex = Arrays.stream(data).map(String::toLowerCase).toList().indexOf("sku");
                }
                validator.validateRow(new DataRow(Arrays.asList(data), index, skuIndex));
                index++;
            }
        }
    }
}