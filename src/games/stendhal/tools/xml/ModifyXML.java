package games.stendhal.tools.xml;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ModifyXML {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Użycie: java ModifyXML <nazwa_pliku_xml> <mnożnik> <typ_statystyki>");
            return;
        }

        String fileName = args[0];
        String filePath = "data/conf/items/" + fileName + ".xml";
        float multiplier = Float.parseFloat(args[1]);
        String attributeType = args[2];

        try {
            // Wczytanie pliku XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(filePath));

            // Pobranie wszystkich elementów <def>, <atk>, <ratk>, <rate>
            NodeList nodeList = doc.getElementsByTagName(attributeType);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                int value = Integer.parseInt(element.getAttribute("value"));
                value *= multiplier;
                element.setAttribute("value", String.valueOf(value));
            }

            // Zapisanie zmienionego dokumentu do pliku
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filePath));
            transformer.transform(source, result);

            System.out.println("Plik XML został zaktualizowany pomyślnie.");

        } catch (Exception e) {
            System.out.println("Wystąpił błąd: " + e.getMessage());
        }
    }
}
