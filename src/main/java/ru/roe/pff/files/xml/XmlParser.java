package ru.roe.pff.files.xml;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ru.roe.pff.files.FileParser;
import ru.roe.pff.processing.DataRow;
import ru.roe.pff.processing.DataRowValidator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class XmlParser extends FileParser {

    @Override
    public Integer parse(DataRowValidator validator, InputStream input) throws IOException {
        // TODO: в factory можно setValidating(true) и тогда парсер будет "валидирующий" - может это поможет в будущем.
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(input);

            Element root = document.getDocumentElement();
            NodeList offers = root.getElementsByTagName("offer");

            int rowIndex = 0;
            List<String> headers = getHeaders(); // Заголовки из XML структуры

            for (int i = 0; i < offers.getLength(); i++) {
                Node node = offers.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element offer = (Element) node;

                    List<String> rowData = new ArrayList<>();
                    for (String header : headers) {
                        String value = offer.getElementsByTagName(header).getLength() > 0
                                ? offer.getElementsByTagName(header).item(0).getTextContent()
                                : "";
                        rowData.add(value);
                    }
                    validator.validateRow(new DataRow(rowData, rowIndex));
                    rowIndex++;
                }
            }
            return rowIndex;
        } catch (ParserConfigurationException | SAXException e) {
            // TODO: process XML parsing exception
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<DataRow> parseFrom(int begin, int end, InputStream input) {
        return List.of(); // TODO: impl
    }

    // TODO: может определять все хэдеры именно во время парсинга
    private List<String> getHeaders() {
        return List.of("price", "currencyId", "categoryId", "picture", "name", "vendor", "description", "barcode");
    }
}
