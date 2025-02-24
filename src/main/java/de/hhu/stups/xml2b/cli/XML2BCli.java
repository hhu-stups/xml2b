package de.hhu.stups.xml2b.cli;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.util.BasePrettyPrinter;
import de.be4.classicalb.core.parser.util.PrettyPrinter;
import de.hhu.stups.xml2b.XML2B;
import de.hhu.stups.xml2b.XML2BOptions;
import de.hhu.stups.xml2b.XML2BOptions.XML2BOption;
import de.prob.prolog.output.FastReadWriter;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static de.hhu.stups.xml2b.XML2BOptions.XML2BOption.*;

public class XML2BCli {
	private static final Logger LOGGER = LoggerFactory.getLogger(XML2BCli.class);

	private File xmlFile, xsdFile;
	private XML2BOptions xml2bOptions;
	private boolean createOutput = false;

	private void handleParameter(String[] args) {
		DefaultParser parser = new DefaultParser();
		Options options = getCommandlineOptions();
		try {
			CommandLine line = parser.parse(options, args);
			String[] remainingArgs = line.getArgs();
			if (remainingArgs.length != 1) {
				LOGGER.error("Error: expected an XML file.");
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("java -jar XML2B.jar [file]", options);
				System.exit(-1);
			}
			xmlFile = new File(remainingArgs[0]);
			xml2bOptions = XML2BOptions.defaultOptions(xmlFile);

			if (line.hasOption(ABSTRACT_CONSTANTS.arg())) {
				xml2bOptions = xml2bOptions.withGenerateAbstractConstants(true);
			}
			if (line.hasOption(FAST_RW.arg())) {
				FastReadWriter.PrologSystem frwPrologSystem;
				String frw = line.getOptionValue(FAST_RW.arg()).toLowerCase();
				switch (frw) {
					case "sicstus":
						frwPrologSystem = FastReadWriter.PrologSystem.SICSTUS;
						break;
					case "swi":
						frwPrologSystem = FastReadWriter.PrologSystem.SWI;
						break;
					case "none":
						frwPrologSystem = null;
						break;
					default:
						throw new IllegalArgumentException("Unsupported Prolog system: " + frw);
				}
				xml2bOptions = xml2bOptions.withPrologSystem(frwPrologSystem);
			}
			if (line.hasOption(OUTPUT.arg())) {
				File outputFile = new File(line.getOptionValue(OUTPUT.arg()));
				String[] splitName = outputFile.getName().split("\\.");
				xml2bOptions = xml2bOptions.withMachineName(splitName[splitName.length > 1 ? splitName.length-2 : 0])
						.withDirectory(outputFile.getParentFile().toPath());
				createOutput = true;
			}
			if (line.hasOption(VERSION.arg())) {
				LOGGER.info("XML2B: {} [{}]", XML2B.getVersion(), XML2B.getGitSha());
			}
			if (line.hasOption(XSD.arg())) {
				xsdFile = new File(line.getOptionValue(XSD.arg()));
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
			XML2B xml2B = xml2BCli.xsdFile != null ? new XML2B(xml2BCli.xmlFile, xml2BCli.xsdFile, xml2BCli.xml2bOptions)
					: new XML2B(xml2BCli.xmlFile, xml2BCli.xml2bOptions);

			Start start = xml2B.translate();
			if (xml2BCli.xsdFile != null) {
				LOGGER.info("{} is valid according to {}", xml2BCli.xmlFile.getName(), xml2BCli.xsdFile.getName());
			}
			xml2BCli.createMachine(start);
			LOGGER.info("translation of {} succeeded", xml2BCli.xmlFile.getName());
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

	private void createMachine(Start start) {
		if (createOutput) {
			Path outputFile = xml2bOptions.directory().resolve(xml2bOptions.machineName() + ".mch");
			try (final Writer writer = Files.newBufferedWriter(outputFile)) {
				BasePrettyPrinter prettyPrinter = new BasePrettyPrinter(writer);
				prettyPrinter.setUseIndentation(true);
				start.apply(prettyPrinter);
				prettyPrinter.flush();
			} catch (IOException e) {
				LOGGER.error("error creating machine file", e);
			}
			LOGGER.info("created machine at {}", outputFile.toAbsolutePath());
		} else {
			LOGGER.info("no output path provided, print machine");
			System.out.println(PrettyPrinter.getPrettyPrint(start));
		}
	}

	private static Options getCommandlineOptions() {
		Options options = new Options();
		for (XML2BOption option : XML2BOption.values()) {
			options.addOption(option.arg(), option.hasArg(), option.desc());
		}
		return options;
	}
}
