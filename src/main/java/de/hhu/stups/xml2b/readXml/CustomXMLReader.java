package de.hhu.stups.xml2b.readXml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.*;

public class CustomXMLReader extends DefaultHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomXMLReader.class);
	private static class OpenXMLElement {
		private final int recId, lineNumber, columnNumber;
		private final Map<String, String> attributes;

		private OpenXMLElement(int recId, int lineNumber, int columnNumber, Map<String, String> attributes) {
			this.recId = recId;
			this.lineNumber = lineNumber;
			this.columnNumber = columnNumber;
			this.attributes = attributes;
		}

		private XMLElement getClosedXMLElement(String elementType, int pId, int endLine, int endColumn) {
			return new XMLElement(elementType, pId, recId, attributes, lineNumber, columnNumber, endLine, endColumn);
		}
	}

	private final Stack<OpenXMLElement> openXMLElements = new Stack<>();
	private final List<XMLElement> closedXMLElements = new ArrayList<>();
	private Locator locator;
	private int recId = 1;

	public List<XMLElement> readXML(File file) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(file, this);
		} catch (Exception e) {
			LOGGER.error("failed to read XML file", e);
		}
		return closedXMLElements;
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		super.setDocumentLocator(locator);
		this.locator = locator;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		OpenXMLElement newNode = new OpenXMLElement(recId, locator.getLineNumber(), locator.getColumnNumber(), extractAttributes(attributes));
		openXMLElements.push(newNode);
		recId++;
	}

	private static Map<String, String> extractAttributes(Attributes attributes) {
		Map<String, String> extractedAttributes = new HashMap<>();
		for (int i = 0; i < attributes.getLength(); i++) {
			extractedAttributes.put(attributes.getLocalName(i), attributes.getValue(i));
		}
		return extractedAttributes;
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		OpenXMLElement currentNode = openXMLElements.pop();
		if (!openXMLElements.isEmpty()) {
			OpenXMLElement parentNode = openXMLElements.peek();
			if (parentNode != null) {
				closedXMLElements.add(currentNode.getClosedXMLElement(qName, parentNode.recId, locator.getLineNumber(), locator.getColumnNumber()));
			}
		} else {
			closedXMLElements.add(currentNode.getClosedXMLElement(qName, 0, locator.getLineNumber(), locator.getColumnNumber()));
		}
	}
}