package de.hhu.stups.xml2b.cli;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.hhu.stups.xml2b.XML2B;
import de.hhu.stups.xml2b.XML2BOptions;
import de.hhu.stups.xml2b.XML2BOptions.XML2BOption;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static de.hhu.stups.xml2b.XML2BOptions.XML2BOption.*;

public class XML2BCli {
	private static final Logger LOGGER = LoggerFactory.getLogger(XML2BCli.class);
	private static final int EXIT_OK = 0, EXIT_INVALID_INPUT = 1, EXIT_ERROR = 2;

	public static void main(String[] args) {
		int exitCode = XML2BCli.run(args);
		if (exitCode != EXIT_OK) {
			System.exit(exitCode);
		}
	}

	public static int run(String[] args) {
		Options options = getCommandlineOptions();
		CommandLine line;
		try {
			line = new DefaultParser().parse(options, args);
		} catch (ParseException e) {
			LOGGER.error(e.getMessage());
			printHelp(options);
			return EXIT_ERROR;
		}

		if (line.hasOption(VERSION.arg())) {
			LOGGER.info("XML2B: {} [{}]", XML2B.getVersion(), XML2B.getGitSha());
			if (line.getArgs().length == 0) {
				return EXIT_OK;
			}
		}

		String[] remainingArgs = line.getArgs();
		if (remainingArgs.length != 1) {
			LOGGER.error("Error: expected an XML file.");
			printHelp(options);
			return EXIT_ERROR;
		}

		File xmlFile = new File(remainingArgs[0]);
		File xsdFile = line.hasOption(XSD.arg()) ? new File(line.getOptionValue(XSD.arg())) : null;
		XML2BOptions xml2bOptions;
		try {
			xml2bOptions = XML2BOptions.defaultOptions(xmlFile);
			if (line.hasOption(ABSTRACT_CONSTANTS.arg())) {
				xml2bOptions = xml2bOptions.withGenerateAbstractConstants(true);
			}
			if (line.hasOption(FAST_RW.arg())) {
				xml2bOptions = xml2bOptions.withPrologSystem(line.getOptionValue(FAST_RW.arg()));
			}
			if (line.hasOption(OUTPUT.arg())) {
				xml2bOptions = xml2bOptions.withOutputFile(new File(line.getOptionValue(OUTPUT.arg())));
			}
			if (line.hasOption(INLINE_DATA.arg())) {
				xml2bOptions = xml2bOptions.withInlineData(true);
			}
			if (line.hasOption(UPDATE_DATA_ONLY.arg()) && !line.hasOption(INLINE_DATA.arg())) {
				xml2bOptions = xml2bOptions.withUpdateDataOnly(true);
			}
		} catch (IllegalArgumentException e) {
			LOGGER.error(e.getMessage());
			printHelp(options);
			return EXIT_ERROR;
		}

		try {
			XML2B xml2B = xsdFile != null ? new XML2B(xmlFile, xsdFile, xml2bOptions) : new XML2B(xmlFile, xml2bOptions);
			xml2B.translate();
			if (xsdFile != null) {
				LOGGER.info("{} is valid according to {}", xmlFile.getName(), xsdFile.getName());
			}
			if (!xml2bOptions.updateDataOnly()) {
				Path outputFile = xml2B.generateMachine();
				LOGGER.info("created machine at {}", outputFile.toAbsolutePath().toRealPath());
			} else {
				LOGGER.info("data update mode: machine file NOT generated");
				LOGGER.info("updated data file at {}", xml2bOptions.directory().resolve(xml2bOptions.machineName() + ".probdata").toRealPath());
			}
			LOGGER.info("translation of {} succeeded", xmlFile.getName());
			return EXIT_OK;
		} catch (BCompoundException e) {
			if (xsdFile != null) {
				LOGGER.error("{} is NOT valid according to {}", xmlFile.getName(), xsdFile.getName());
			}
			List<BException> bExceptions = e.getBExceptions();
			for (BException bException : bExceptions) {
				List<BException.Location> locations = bException.getLocations();
				LOGGER.error("{}{}", bException.getMessage(), locations.isEmpty() ? "" : " at " + locations.get(0));
			}
			return EXIT_INVALID_INPUT;
		} catch (IOException | RuntimeException e) {
			LOGGER.error("translation of {} failed: {}", xmlFile.getName(), e.getMessage(), e);
			return EXIT_ERROR;
		}
	}

	private static void printHelp(Options options) {
		try {
			HelpFormatter.builder().setShowSince(false).get()
					.printHelp("java -jar XML2B.jar [file]", "", options, "", true);
		} catch (IOException e) {
			LOGGER.error("could not print help", e);
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
