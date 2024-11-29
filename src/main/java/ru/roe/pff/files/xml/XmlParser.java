package ru.roe.pff.files.xml;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import ru.roe.pff.entity.FileError;
import ru.roe.pff.files.FileParser;
import ru.roe.pff.processing.DataRow;
import ru.roe.pff.processing.DataRowValidator;
import ru.roe.pff.repository.FileErrorRepository;
import ru.roe.pff.repository.FileRepository;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static ru.roe.pff.files.xml.XmlUtil.FIELD_ORDER;

@Slf4j
@Component
@RequiredArgsConstructor
public class XmlParser extends FileParser {
    private final DataRowValidator dataRowValidator;
    private final FileErrorRepository fileErrorRepository;

    private static final Map<String, Integer> fieldMap = new HashMap<>();

    static {
        for (int i = 0; i < FIELD_ORDER.length; i++) {
            fieldMap.put(FIELD_ORDER[i], i);
        }
    }

    private final FileRepository fileRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<DataRow> parse(UUID fileId, InputStream input) {
        var feedFile = fileRepository.findById(fileId).orElseThrow();
        List<FileError> errors = new ArrayList<>();
        List<DataRow> dataRows = parseFrom(0, Integer.MAX_VALUE, input);

        log.debug("Parsed file: {}", feedFile.getFileName());
        log.debug("Validating file... ({})", feedFile.getFileName());

        for (DataRow row : dataRows) {
            var rowErrors = dataRowValidator.validateRow(row, getTagNames());
            errors.addAll(rowErrors);
        }
        dataRowValidator.clearTrackingCollections();

        log.debug("Validated file: {}", feedFile.getFileName());
        log.debug("Saving found errors... ({})", feedFile.getFileName());

        errors.forEach(value -> value.setFeedFile(feedFile));
        fileErrorRepository.saveAll(errors);

        log.debug("Saved found errors for file: {}", feedFile.getFileName());
        return dataRows;
    }

    @Override
    public List<DataRow> parseFrom(int begin, int end, InputStream input) {
        List<DataRow> dataRows = new ArrayList<>();
        dataRows.add(new DataRow(getTagNames(), -1));
        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            SaxHandler handler = new SaxHandler(begin, end, dataRows);
            parser.parse(input, handler);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            System.out.println(e.getMessage());
        }
        return dataRows;
    }

    public static List<String> getTagNames() {
        return List.of(FIELD_ORDER);
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
                if (value != null) {
                    currentFields.set(fieldMap.get(fieldName), value);
                }
            }
        }
    }
}