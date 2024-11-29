package ru.roe.pff.files.xml;

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

public class XmlParser extends FileParser {

    private static final String[] fieldOrder = {
        "id",
        "available",
        "price",
        "currencyId",
        "categoryId",
        "picture",
        "name",
        "vendor",
        "description",
        "barcode",
        "param_Артикул",
        "param_Рейтинг",
        "param_Количество отзывов",
        "param_Скидка",
        "param_Новинка"
    };

    private static final Map<String, Integer> fieldMap = new HashMap<>();

    static {
        for (int i = 0; i < fieldOrder.length; i++) {
            fieldMap.put(fieldOrder[i], i);
        }
    }
    @Override
    public Integer parse(DataRowValidator dataRowValidator, InputStream input) throws IOException {
        List<DataRow> dataRows = parseFrom(0, Integer.MAX_VALUE, input);
        int validCount = 0;
        for (DataRow row : dataRows) {
            dataRowValidator.validateRow(row, getColumnNames());
            validCount++;
        }
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
            throw new IOException("SAX parsing error", e);
        }
        return dataRows;
    }

    public static List<String> getColumnNames() {
        return Collections.unmodifiableList(Arrays.asList(fieldOrder));
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
                currentFields = new ArrayList<>(Collections.nCopies(fieldOrder.length, ""));
                insideOffer = true;
                String id = attributes.getValue("id");
                String available = attributes.getValue("available");
                setField("id", id);
                setField("available", available);
            } else if (insideOffer) {
                if (qName.equals("price")) {
                    currentElementName = "price";
                    characters = new StringBuilder();
                } else if (qName.equals("currencyId")) {
                    currentElementName = "currencyId";
                    characters = new StringBuilder();
                } else if (qName.equals("categoryId")) {
                    currentElementName = "categoryId";
                    characters = new StringBuilder();
                } else if (qName.equals("picture")) {
                    currentElementName = "picture";
                    characters = new StringBuilder();
                } else if (qName.equals("name")) {
                    currentElementName = "name";
                    characters = new StringBuilder();
                } else if (qName.equals("vendor")) {
                    currentElementName = "vendor";
                    characters = new StringBuilder();
                } else if (qName.equals("description")) {
                    currentElementName = "description";
                    characters = new StringBuilder();
                } else if (qName.equals("barcode")) {
                    currentElementName = "barcode";
                    characters = new StringBuilder();
                } else if (qName.equals("param")) {
                    String name = attributes.getValue("name");
                    if (name != null) {
                        currentParamName = "param_" + name;
                        characters = new StringBuilder();
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
                if (qName.equals("price")) {
                    setField("price", characters.toString());
                } else if (qName.equals("currencyId")) {
                    setField("currencyId", characters.toString());
                } else if (qName.equals("categoryId")) {
                    setField("categoryId", characters.toString());
                } else if (qName.equals("picture")) {
                    setField("picture", characters.toString());
                } else if (qName.equals("name")) {
                    setField("name", characters.toString());
                } else if (qName.equals("vendor")) {
                    setField("vendor", characters.toString());
                } else if (qName.equals("description")) {
                    setField("description", characters.toString());
                } else if (qName.equals("barcode")) {
                    setField("barcode", characters.toString());
                } else if (qName.equals("param")) {
                    if (currentParamName != null && fieldMap.containsKey(currentParamName)) {
                        setField(currentParamName, characters.toString());
                    }
                    currentParamName = null;
                } else if (qName.equals("offer")) {
                    if (currentRow >= begin && currentRow <= end) {
                        DataRow dataRow = new DataRow(currentFields, currentRow);
                        dataRows.add(dataRow);
                    }
                    insideOffer = false;
                    currentFields = null;
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