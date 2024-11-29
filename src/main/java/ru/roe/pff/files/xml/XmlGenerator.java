package ru.roe.pff.files.xml;

import org.w3c.dom.*;
import ru.roe.pff.processing.DataRow;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.List;

public class XmlGenerator {

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

    public void saveNewXml(List<DataRow> dataRows, String fileName) {
        try {
            // Create a DocumentBuilderFactory and DocumentBuilder
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();

            // Create root element
            Element rootElement = doc.createElement("yml_catalog");
            rootElement.setAttribute("date", "2024-11-25T00:00:00");
            doc.appendChild(rootElement);

            // Create shop element
            Element shop = doc.createElement("shop");
            rootElement.appendChild(shop);

            // Add static elements under shop
            addElement(doc, shop, "name", "Your Shop Name");
            addElement(doc, shop, "company", "Your Company Name");
            addElement(doc, shop, "url", "https://example.com");

            // Create offers element
            Element offers = doc.createElement("offers");
            shop.appendChild(offers);

            // Iterate over dataRows and add offer elements
            for (DataRow dataRow : dataRows) {
                if (dataRow.getData().size() < fieldOrder.length) {
                    // Skip rows that don't have enough data
                    continue;
                }
                Element offer = doc.createElement("offer");
                offer.setAttribute("id", dataRow.getData().get(0));
                offer.setAttribute("available", dataRow.getData().get(1));

                addElement(doc, offer, "price", dataRow.getData().get(2));
                addElement(doc, offer, "currencyId", dataRow.getData().get(3));
                addElement(doc, offer, "categoryId", dataRow.getData().get(4));
                addElement(doc, offer, "picture", dataRow.getData().get(5));
                addElement(doc, offer, "name", dataRow.getData().get(6));
                addElement(doc, offer, "vendor", dataRow.getData().get(7));
                addElement(doc, offer, "description", dataRow.getData().get(8));
                addElement(doc, offer, "barcode", dataRow.getData().get(9));

                // Add param elements
                addParamElement(doc, offer, "Артикул", dataRow.getData().get(10));
                addParamElement(doc, offer, "Рейтинг", dataRow.getData().get(11));
                addParamElement(doc, offer, "Количество отзывов", dataRow.getData().get(12));
                addParamElement(doc, offer, "Скидка", dataRow.getData().get(13));
                addParamElement(doc, offer, "Новинка", dataRow.getData().get(14));

                offers.appendChild(offer);
            }

            // Write the Document to a file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            // Pretty-print the output
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(fileName));
            transformer.transform(source, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addElement(Document doc, Element parent, String name, String value) {
        Element element = doc.createElement(name);
        element.appendChild(doc.createTextNode(value));
        parent.appendChild(element);
    }

    private void addParamElement(Document doc, Element parent, String paramName, String value) {
        Element param = doc.createElement("param");
        param.setAttribute("name", paramName);
        param.appendChild(doc.createTextNode(value));
        parent.appendChild(param);
    }
}