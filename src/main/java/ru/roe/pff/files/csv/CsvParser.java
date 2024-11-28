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
    public Integer parse(DataRowValidator validator, InputStream input) throws IOException {
        int dataIndex = 1; // Starting index for data rows
        //int skuIndex = -1;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            // Read header row
            String headerLine = reader.readLine();
            if (headerLine != null) {
                String[] headerData = headerLine.split(",");
                //skuIndex = Arrays.asList(Arrays.stream(headerData).map(String::toLowerCase).toArray()).indexOf("sku");
            } else {
                // No header row
                return 0;
            }
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                validator.validateRow(new DataRow(Arrays.asList(data), dataIndex));
                dataIndex++;
            }
        }
        return dataIndex - 1; // Return the number of data rows
    }

    public List<DataRow> parseFrom(int begin, int end, InputStream input) throws IOException {
        if (begin > end || begin < 0 || end < 0) {
            throw new IllegalArgumentException("Invalid range: begin should be less than or equal to end and non-negative.");
        }

        List<DataRow> dataRows = new ArrayList<>();
        int rowIndex = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            // Read header row
            String headerLine = reader.readLine();
            if (headerLine != null) {
                String[] headerData = headerLine.split(",");
                rowIndex = 1; // Next row is the first data row
            } else {
                // No header row
                return dataRows;
            }

            // Skip rows until reaching 'begin'
            while (rowIndex < begin && reader.ready()) {
                reader.readLine();
                rowIndex++;
            }

            // Collect rows from 'begin' to 'end'
            while (rowIndex <= end && reader.ready()) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                String[] data = line.split(",");
                dataRows.add(new DataRow(Arrays.asList(data), rowIndex));
                rowIndex++;
            }
        }
        return dataRows;
    }
}