package de.hhu.stups.xml2b.translation;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class XSDValidator {
	private static final Logger LOGGER = LoggerFactory.getLogger(XSDValidator.class);

	public static void validateXmlForXsd(File xmlFile, File xsdFile) throws BCompoundException {
		try {
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory.newSchema(xsdFile);
			Validator validator = schema.newValidator();
			CustomErrorHandler errorHandler = new CustomErrorHandler();
			validator.setErrorHandler(errorHandler);
			validator.validate(new StreamSource(xmlFile));
			List<ValidationError> errors = errorHandler.getErrors();
			if (errors.isEmpty()) {
				LOGGER.info("The XML file is valid according to the provided XML schema.");
			} else {
				LOGGER.error("The XML file is NOT valid according to the provided XML schema.");
				throw new BCompoundException(errors.stream().map(e ->
						new BException(xmlFile.getAbsolutePath(), Collections.singletonList(e.getLocation(xmlFile.getAbsolutePath())),
								e.getMessage(), e.getException())).collect(Collectors.toList()));
			}
		} catch (SAXException e) {
			LOGGER.error("Error while parsing XML schema: " + e.getMessage());
		} catch (IOException e) {
			LOGGER.error("Error while reading XML file: " + e.getMessage());
		}
	}

	private static class CustomErrorHandler implements ErrorHandler {
		private final List<ValidationError> errors = new ArrayList<>();

		@Override
		public void warning(SAXParseException e) {
			LOGGER.warn(collectErrorInformationAsString(e));
		}

		@Override
		public void error(SAXParseException e) {
			errors.add(collectErrors(e));
		}

		@Override
		public void fatalError(SAXParseException e) {
			errors.add(collectErrors(e));
		}
		public List<ValidationError> getErrors() {
			return errors;
		}

		private static String collectErrorInformationAsString(SAXParseException e) {
			return "at line " + e.getLineNumber() + ", column " + e.getColumnNumber() + ": " + e.getMessage();
		}

		private static ValidationError collectErrors(SAXParseException e) {
			return new ValidationError(e.getMessage(), e.getLineNumber(), e.getColumnNumber(), e);
		}
	}

	private static class ValidationError {
		private final String message;
		private final int lineNumber, columnNumber;
		private final SAXParseException exception;

		public ValidationError(String message, int lineNumber, int columnNumber, SAXParseException exception) {
			this.message = message;
			this.lineNumber = lineNumber;
			this.columnNumber = columnNumber;
			this.exception = exception;
		}

		public String getMessage() {
			return message;
		}

		public BException.Location getLocation(String fileName) {
			return new BException.Location(fileName, lineNumber, columnNumber, lineNumber, columnNumber + 1);
		}

		public SAXParseException getException() {
			return exception;
		}
	}
}
