package de.hhu.stups.xml2b.translation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XSDValidator {
	private static final Logger LOGGER = LoggerFactory.getLogger(XSDValidator.class);

	public static void validateXmlForXsd(File xmlFile, File xsdFile) {
		try {
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory.newSchema(xsdFile);
			Validator validator = schema.newValidator();
			CustomErrorHandler errorHandler = new CustomErrorHandler();
			validator.setErrorHandler(errorHandler);
			validator.validate(new StreamSource(xmlFile));
			List<String> errors = errorHandler.getErrors();
			if (errors.isEmpty()) {
				LOGGER.info("The XML file is valid according to the provided XML schema.");
			} else {
				LOGGER.error("The XML file is NOT valid according to the provided XML schema.");
				for (String error : errors) {
					LOGGER.error(error);
				}
			}
		} catch (SAXException e) {
			LOGGER.error("Error while parsing XML schema: " + e.getMessage());
		} catch (IOException e) {
			LOGGER.error("Error while reading XML file: " + e.getMessage());
		}
	}

	static class CustomErrorHandler implements ErrorHandler {
		private final List<String> errors = new ArrayList<>();

		@Override
		public void warning(SAXParseException e) {
			LOGGER.warn(collectErrorInformation(e));
		}

		@Override
		public void error(SAXParseException e) {
			errors.add(collectErrorInformation(e));
		}

		@Override
		public void fatalError(SAXParseException e) {
			errors.add("Fatal error " + collectErrorInformation(e));
		}

		public List<String> getErrors() {
			return errors;
		}

		private static String collectErrorInformation(SAXParseException e) {
			return "at line " + e.getLineNumber() + ", column " + e.getColumnNumber() + ": " + e.getMessage();
		}
	}
}
