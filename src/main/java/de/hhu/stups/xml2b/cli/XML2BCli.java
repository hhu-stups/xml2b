package de.hhu.stups.xml2b.cli;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.util.BasePrettyPrinter;
import de.be4.classicalb.core.parser.util.PrettyPrinter;
import de.hhu.stups.xml2b.XML2B;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.util.List;

public class XML2BCli {
	private static final Logger LOGGER = LoggerFactory.getLogger(XML2BCli.class);

	public final static String OUTPUT = "o", VERSION = "version", XSD = "xsd";
	private File xmlFile, xsdFile, outputMch;

	public void handleParameter(String[] args) {
		DefaultParser parser = new DefaultParser();
		Options options = getCommandlineOptions();
		try {
			CommandLine line = parser.parse(options, args);
			String[] remainingArgs = line.getArgs();
			if (line.hasOption(OUTPUT)) {
				outputMch = new File(line.getOptionValue(OUTPUT));
			}
			if (line.hasOption(VERSION)) {
				LOGGER.info("XML2B: {} [{}]", XML2B.getVersion(), XML2B.getGitSha());
			}
			if (line.hasOption(XSD)) {
				xsdFile = new File(line.getOptionValue(XSD));
			}
			if (remainingArgs.length != 1) {
				LOGGER.error("Error: expected an XML file.");
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("java -jar XML2B.jar [file]", options);
				System.exit(-1);
			} else {
				xmlFile = new File(remainingArgs[0]);
			}
		} catch (ParseException e) {
			LOGGER.error(e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar XML2B.jar [file]", options);
			System.exit(-1);
		}

	}

	public static void main(String[] args) throws Exception {
		XML2BCli xml2BCli = new XML2BCli();
		xml2BCli.handleParameter(args);

		try {
			XML2B xml2B = new XML2B(xml2BCli.xmlFile, xml2BCli.xsdFile);
			Start start = xml2B.translate();
			if (xml2BCli.xsdFile != null) {
				LOGGER.info("{} is valid according to {}", xml2BCli.xmlFile.getName(), xml2BCli.xsdFile.getName());
			}
			LOGGER.info("translation of {} succeeded", xml2BCli.xmlFile.getName());
			xml2BCli.createMachine(start);
		} catch (BCompoundException e) {
			if (xml2BCli.xsdFile != null) {
				LOGGER.error("{} is NOT valid according to {}", xml2BCli.xmlFile.getName(), xml2BCli.xsdFile.getName());
			}
			List<BException> bExceptions = e.getBExceptions();
			for (BException bException : bExceptions) {
				LOGGER.error("{} at {}", bException.getMessage(), bException.getLocations().get(0).toString());
			}
		}
	}

	public void createMachine(Start start) {
		if (outputMch != null) {
			try (final Writer writer = Files.newBufferedWriter(outputMch.toPath())) {
				BasePrettyPrinter prettyPrinter = new BasePrettyPrinter(writer);
				prettyPrinter.setUseIndentation(true);
				start.apply(prettyPrinter);
				prettyPrinter.flush();
			} catch (IOException e) {
				LOGGER.error("error creating machine file", e);
			}
		} else {
			LOGGER.info("no output path provided, print machine");
			PrettyPrinter prettyPrinter = new PrettyPrinter();
			prettyPrinter.setUseIndentation(true);
			start.apply(prettyPrinter);
			System.out.println(prettyPrinter.getPrettyPrint());
		}
	}

	private static Options getCommandlineOptions() {
		Options options = new Options();
		options.addOption(VERSION, false, "prints the current version of XML2B");

		Option output = Option.builder(OUTPUT)
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
