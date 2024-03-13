package de.hhu.stups.xml2b.readXml;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XMLReader {
    private int recId = 0;

    public List<XMLElement> readXML(File xmlFile) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);

            Element rootElement = document.getDocumentElement();
            return processElement(rootElement, recId);
        } catch (Exception e) {
            throw new RuntimeException("failed to read XML file", e);
        }
    }

    private List<XMLElement> processElement(Element element, int pId) {
        recId++;
        int id = recId;
        String elementType = element.getTagName(); // later add "_" to avoid clashes with attribute identifiers
        List<XMLElement> xmlElements = new ArrayList<>();
        Map<String, String> attributes = new HashMap<>();

        if (element.hasAttributes()) {
            NamedNodeMap attributeNodes = element.getAttributes();
            for (int i = 0; i < attributeNodes.getLength(); i++) {
                Node attributeNode = attributeNodes.item(i);
                attributes.put(attributeNode.getNodeName(), attributeNode.getNodeValue());
            }
        }

        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                Element childElement = (Element) childNode;
                List<XMLElement> childXMLElements = processElement(childElement, id);
                xmlElements.addAll(childXMLElements);
            }
        }

        xmlElements.add(new XMLElement(elementType, pId, id, attributes));
        return xmlElements;
    }
}