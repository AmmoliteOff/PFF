package ru.roe.pff.files.xml;

import lombok.extern.slf4j.Slf4j;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import ru.roe.pff.files.FileParser;
import ru.roe.pff.processing.DataRow;
import ru.roe.pff.processing.DataRowValidator;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static ru.roe.pff.files.xml.XmlUtil.FIELD_ORDER;
import static ru.roe.pff.files.xml.XmlUtil.getColumnNames;

@Slf4j
public class XmlParser extends FileParser {

    private static final Map<String, Integer> fieldMap = new HashMap<>();

    static {
        for (int i = 0; i < FIELD_ORDER.length; i++) {
            fieldMap.put(FIELD_ORDER[i], i);
        }
    }

    @Override
    public Integer parse(DataRowValidator dataRowValidator, InputStream input) throws IOException {
        List<DataRow> dataRows = parseFrom(0, Integer.MAX_VALUE, input);
        int validCount = 0;
        log.debug("Parsing XML...");
        for (DataRow row : dataRows) {
            dataRowValidator.validateRow(row, getColumnNames());
            log.debug("Validated row: {}", row.getIndex());
            validCount++;
        }
        log.debug("Finished parsing XML. Total count: {}", validCount);
        return validCount;
    }

    @Override
    public List<DataRow> parseFrom(int begin, int end, InputStream input) throws IOException {
        List<DataRow> dataRows = new ArrayList<>();
        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            SaxHandler handler = new SaxHandler(begin, end, dataRows);
            parser.parse(input, handler);
        } catch (Exception e) {
            log.error("Error while parsing XML (SAX)", e);
        }
        return dataRows;
    }

    private static class SaxHandler extends DefaultHandler {
        private int begin;
        private int end;
        private List<DataRow> dataRows;
        private int currentRow;
        private List<String> currentFields;
        private StringBuilder characters;
        private String currentElementName;
        private String currentParamName;
        private boolean insideOffer;

        public SaxHandler(int begin, int end, List<DataRow> dataRows) {
            this.begin = begin;
            this.end = end;
            this.dataRows = dataRows;
            this.currentRow = 0;
            this.characters = new StringBuilder();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            if (qName.equals("offer")) {
                currentRow++;
                currentFields = new ArrayList<>(Collections.nCopies(FIELD_ORDER.length, ""));
                insideOffer = true;
                String id = attributes.getValue("id");
                String available = attributes.getValue("available");
                setField("id", id);
                setField("available", available);
            } else if (insideOffer) {
                switch (qName) {
                    case "price" -> {
                        currentElementName = "price";
                        characters = new StringBuilder();
                    }
                    case "currencyId" -> {
                        currentElementName = "currencyId";
                        characters = new StringBuilder();
                    }
                    case "categoryId" -> {
                        currentElementName = "categoryId";
                        characters = new StringBuilder();
                    }
                    case "picture" -> {
                        currentElementName = "picture";
                        characters = new StringBuilder();
                    }
                    case "name" -> {
                        currentElementName = "name";
                        characters = new StringBuilder();
                    }
                    case "vendor" -> {
                        currentElementName = "vendor";
                        characters = new StringBuilder();
                    }
                    case "description" -> {
                        currentElementName = "description";
                        characters = new StringBuilder();
                    }
                    case "barcode" -> {
                        currentElementName = "barcode";
                        characters = new StringBuilder();
                    }
                    case "param" -> {
                        String name = attributes.getValue("name");
                        if (name != null) {
                            currentParamName = "param_" + name;
                            characters = new StringBuilder();
                        }
                    }
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            if (characters != null) {
                characters.append(ch, start, length);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (insideOffer) {
                switch (qName) {
                    case "price" -> setField("price", characters.toString());
                    case "currencyId" -> setField("currencyId", characters.toString());
                    case "categoryId" -> setField("categoryId", characters.toString());
                    case "picture" -> setField("picture", characters.toString());
                    case "name" -> setField("name", characters.toString());
                    case "vendor" -> setField("vendor", characters.toString());
                    case "description" -> setField("description", characters.toString());
                    case "barcode" -> setField("barcode", characters.toString());
                    case "param" -> {
                        if (currentParamName != null && fieldMap.containsKey(currentParamName)) {
                            setField(currentParamName, characters.toString());
                        }
                        currentParamName = null;
                    }
                    case "offer" -> {
                        if (currentRow >= begin && currentRow <= end) {
                            DataRow dataRow = new DataRow(currentFields, currentRow);
                            dataRows.add(dataRow);
                        }
                        insideOffer = false;
                        currentFields = null;
                    }
                }
                characters = null;
                currentElementName = null;
            }
        }

        private void setField(String fieldName, String value) {
            if (insideOffer && currentFields != null && fieldMap.containsKey(fieldName)) {
                currentFields.set(fieldMap.get(fieldName), value);
            }
        }
    }
}