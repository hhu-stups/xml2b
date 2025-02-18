package de.hhu.stups.xml2b.readXml;

import de.be4.classicalb.core.parser.exceptions.BException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class XMLReader extends DefaultHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(XMLReader.class);
	private static class OpenXMLElement {
		private final int recId, lineNumber, columnNumber;
		private final Map<String, String> attributes;
		private String content = "";
		private int nrOfChildren = 0;

		private OpenXMLElement(int recId, int lineNumber, int columnNumber, Map<String, String> attributes) {
			this.recId = recId;
			this.lineNumber = lineNumber;
			this.columnNumber = columnNumber;
			this.attributes = attributes;
		}

		private void incNrOfChildren() {
			this.nrOfChildren++;
		}

		private void addContent(final String content) {
			this.content += content;
		}

		private XMLElement getClosedXMLElement(String elementType, List<Integer> pIds, List<String> pNames, int endLine, int endColumn) {
			return new XMLElement(elementType, pIds, pNames, nrOfChildren, recId, attributes, content, lineNumber, columnNumber, endLine, endColumn);
		}
	}

	private final Stack<OpenXMLElement> openXMLElements = new Stack<>();
	private final Stack<String> openXMLElementNames = new Stack<>();
	private final List<XMLElement> closedXMLElements = new ArrayList<>();
	private Locator locator;
	private int recId = 1;

	public List<XMLElement> readXML(File file, File xsdFile) {
		try {
			SAXParserFactory saxFactory = SAXParserFactory.newInstance();
			if (xsdFile != null) {
				SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
				Schema schema = schemaFactory.newSchema(xsdFile);
				saxFactory.setSchema(schema);
				saxFactory.setNamespaceAware(true);
			}
			SAXParser saxParser = saxFactory.newSAXParser();
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
		openXMLElements.forEach(OpenXMLElement::incNrOfChildren);
		OpenXMLElement newNode = new OpenXMLElement(recId, locator.getLineNumber(), locator.getColumnNumber(), extractAttributes(attributes));
		openXMLElements.push(newNode);
		// namespaces are just copied, e.g. exampleNS:exampleTag is used as String for the element field of the B record
		openXMLElementNames.push(qName);
		recId++;
	}

	private static Map<String, String> extractAttributes(Attributes attributes) {
		Map<String, String> extractedAttributes = new HashMap<>();
		for (int i = 0; i < attributes.getLength(); i++) {
			extractedAttributes.put(attributes.getQName(i), attributes.getValue(i));
		}
		return extractedAttributes;
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		OpenXMLElement node = openXMLElements.peek();
		if (node != null) {
			String content = new String(ch, start, length).trim(); // this could break content with surrounding white spaces!
			if (!content.isEmpty()) { // ignore indents and line breaks
				node.addContent(content);
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		OpenXMLElement currentNode = openXMLElements.pop();
		openXMLElementNames.pop();
		List<Integer> pIds = openXMLElements.stream().map(o -> o.recId).collect(Collectors.toList());
		List<String> pNames = new ArrayList<>(openXMLElementNames);
		if (pIds.isEmpty())
			pIds = Collections.singletonList(0);
		closedXMLElements.add(currentNode.getClosedXMLElement(qName, pIds, pNames,
					locator.getLineNumber(), locator.getColumnNumber()));
	}

	public static class ValidationError {
		private final String message;
		private final int startLine, startColumn, endLine, endColumn;
		private final SAXParseException exception;

		public ValidationError(String message, int startLine, int startColumn, int endLine, int endColumn, SAXParseException exception) {
			this.message = message;
			this.startLine = startLine;
			this.startColumn = startColumn;
			this.endLine = endLine;
			this.endColumn = endColumn;
			this.exception = exception;
		}

		public String getMessage() {
			return message;
		}

		public BException getBException(String fileName) {
			BException.Location location = new BException.Location(fileName, startLine, startColumn, endLine, endColumn);
			return new BException(fileName, Collections.singletonList(location), message, exception);
		}

		public SAXParseException getException() {
			return exception;
		}
	}

	private final List<ValidationError> errors = new ArrayList<>();

	@Override
	public void warning(SAXParseException e) {
		LOGGER.warn(collectErrorInformationAsString(e));
	}

	@Override
	public void error(SAXParseException e) {
		errors.add(collectError(e));
	}

	@Override
	public void fatalError(SAXParseException e) {
		errors.add(collectError(e));
	}

	public List<ValidationError> getErrors() {
		return errors;
	}

	private String collectErrorInformationAsString(SAXParseException e) {
		return "at line " + e.getLineNumber() + ", column " + e.getColumnNumber() + ": " + e.getMessage();
	}

	private ValidationError collectError(SAXParseException e) {
		return new ValidationError(e.getMessage(), e.getLineNumber(), 1, e.getLineNumber(), e.getColumnNumber(), e);
	}
}