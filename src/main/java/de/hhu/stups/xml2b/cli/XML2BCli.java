package de.hhu.stups.xml2b.cli;

import ch.qos.logback.classic.ClassicConstants;
import de.hhu.stups.xml2b.XML2B;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class XML2BCli {
	private static final Logger LOGGER = LoggerFactory.getLogger(XML2BCli.class);

	public final static String VERBOSE = "verbose", VERSION = "version", XSD = "xsd";
	private File xmlFile, xsdFile;

	public void handleParameter(String[] args) {
		DefaultParser parser = new DefaultParser();
		Options options = getCommandlineOptions();
		try {
			CommandLine line = parser.parse(options, args);
			String[] remainingArgs = line.getArgs();
			if (line.hasOption(VERBOSE)) {
				System.setProperty(ClassicConstants.CONFIG_FILE_PROPERTY, "logback_verbose.xml");
			}
			if (line.hasOption(VERSION)) {
				LOGGER.info("XML2B: " + XML2B.getVersion() + " [" + XML2B.getGitSha() + "]");
			}
			if (line.hasOption(XSD)) {
				xsdFile = new File(line.getOptionValue(XSD));
			}
			if (remainingArgs.length != 1) {
				LOGGER.error("Error: expected a XML file.");
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("java -jar XML2B.jar [file]", options);
				System.exit(-1);
			} else {
				xmlFile = new File(remainingArgs[0]);
			}
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar XML2B.jar [file]", options);
			System.exit(-1);
		}

	}

	public static void main(String[] args) throws Exception {
		XML2BCli xml2BCli = new XML2BCli();
		xml2BCli.handleParameter(args);
		XML2B xml2B = new XML2B(xml2BCli.xmlFile, xml2BCli.xsdFile);
		xml2B.translate();
	}

	private static Options getCommandlineOptions() {
		Options options = new Options();
		options.addOption(VERSION, false, "prints the current version of XML2B");
		options.addOption(VERBOSE, false, "makes output more verbose");

		Option output = Option.builder("o")
				.argName("path")
				.hasArg()
				.desc("output path for generated machine")
				.build();
		Option xsd = Option.builder("xsd")
				.argName("xsdFile")
				.hasArg()
				.desc("use XSD file for schema validation and type extraction")
				.build();
		options.addOption(output);
		options.addOption(xsd);
		return options;
	}
}
