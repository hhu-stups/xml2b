package de.hhu.stups.xml2b.readXml;

import de.be4.classicalb.core.parser.exceptions.BException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import static de.hhu.stups.xml2b.readXsd.TypeUtils.buildQName;

public class XMLReader extends DefaultHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(XMLReader.class);

	private static class OpenXMLElement {
		private final int recId, lineNumber, columnNumber;
		private final QName name;
		private final Map<String, String> attributes;
		private final List<Integer> pIds;
		private final List<QName> pNames;
		private final List<Integer> childPIds;
		private final List<QName> childPNames;
		private final StringBuilder content = new StringBuilder();

		private OpenXMLElement(int recId, int lineNumber, int columnNumber, QName name, Map<String, String> attributes,
		                       OpenXMLElement parent) {
			this.recId = recId;
			this.lineNumber = lineNumber;
			this.columnNumber = columnNumber;
			this.name = name;
			this.attributes = attributes;
			if (parent == null) {
				this.pIds = Collections.singletonList(0);
				this.pNames = Collections.emptyList();
				this.childPIds = Collections.singletonList(recId);
				this.childPNames = Collections.singletonList(name);
			} else {
				this.pIds = parent.childPIds;
				this.pNames = parent.childPNames;
				this.childPIds = Stream.concat(pIds.stream(), Stream.of(recId)).toList();
				this.childPNames = Stream.concat(pNames.stream(), Stream.of(name)).toList();
			}
		}

		private XMLElement close(int nrOfDescendants, int endLine, int endColumn) {
			String rawContent = content.toString(); // exclude useless newlines
			return new XMLElement(name, pIds, pNames, nrOfDescendants, recId, attributes, rawContent.strip(),
					lineNumber, columnNumber, endLine, endColumn);
		}
	}

	private final Deque<OpenXMLElement> openXMLElements = new ArrayDeque<>();
	private final List<XMLElement> closedXMLElements = new ArrayList<>();
	private Locator locator;
	private int recId = 1;

	public List<XMLElement> readXML(File file, File xsdFile) {
		try {
			SAXParserFactory saxFactory = SAXParserFactory.newDefaultInstance();
			if (xsdFile != null) {
				Schema schema = SchemaFactory.newDefaultInstance().newSchema(xsdFile);
				saxFactory.setSchema(schema);
				saxFactory.setNamespaceAware(true);
			}
			saxFactory.newSAXParser().parse(file, this);
		} catch (SAXParseException e) {
			errors.add(collectError(e));
		} catch (SAXException | IOException | ParserConfigurationException e) {
			errors.add(new ValidationError(String.valueOf(e.getMessage()), e));
		}
		if (!errors.isEmpty()) {
			closedXMLElements.removeIf(Objects::isNull); // parsing was aborted, some elements are still open
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
		OpenXMLElement parent = openXMLElements.peek();
		openXMLElements.push(new OpenXMLElement(recId, locator.getLineNumber(), locator.getColumnNumber(),
				buildQName(uri, localName, qName), extractAttributes(attributes), parent));
		closedXMLElements.add(null);
		recId++;
	}

	private static Map<String, String> extractAttributes(Attributes attributes) {
		Map<String, String> extractedAttributes = new HashMap<>();
		for (int i = 0; i < attributes.getLength(); i++) {
			extractedAttributes.put(attributes.getQName(i), attributes.getValue(i));
			// TODO: use QName objects for attributes as well
		}
		return extractedAttributes;
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		OpenXMLElement node = openXMLElements.peek();
		if (node != null) { // collect text content
			node.content.append(ch, start, length);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		OpenXMLElement currentNode = openXMLElements.pop();
		int nrOfDescendants = (recId - 1) - currentNode.recId;
		closedXMLElements.set(currentNode.recId - 1,
				currentNode.close(nrOfDescendants, locator.getLineNumber(), locator.getColumnNumber()));
	}

	public static class ValidationError {
		private final String message;
		private final int startLine, startColumn, endLine, endColumn;
		private final Exception exception;

		public ValidationError(String message, Exception exception) {
			this(message, 1, 1, 1, 1, exception);
		}

		public ValidationError(String message, int startLine, int startColumn, int endLine, int endColumn, Exception exception) {
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

		public Exception getException() {
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
