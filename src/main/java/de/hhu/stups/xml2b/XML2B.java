package de.hhu.stups.xml2b;

import ch.qos.logback.classic.ClassicConstants;
import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.util.PrettyPrinter;
import de.hhu.stups.xml2b.translation.StandaloneTranslator;
import de.hhu.stups.xml2b.translation.Translator;
import de.hhu.stups.xml2b.translation.XSDTranslator;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class XML2B {
    private static final Logger LOGGER = LoggerFactory.getLogger(XML2B.class);
    private static final Properties buildProperties;
    static {
        buildProperties = new Properties();
        final InputStream is = XML2B.class.getResourceAsStream("build.properties");
        if (is == null) {
            throw new IllegalStateException("Build properties not found, this should never happen!");
        } else {
            try (final Reader r = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                buildProperties.load(r);
            } catch (final IOException e) {
                throw new IllegalStateException("IOException while loading build properties, this should never happen!", e);
            }
        }
    }
    public static String getVersion() {
        return buildProperties.getProperty("version");
    }
    public static String getGitSha() {
        return buildProperties.getProperty("commit");
    }

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
                LOGGER.info("XML2B: " + getVersion() + " [" + getGitSha() + "]");
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
        XML2B xml2B = new XML2B();
        xml2B.handleParameter(args);

        Start start = xml2B.translate();

        /*for (XmlSchemaElement e : xsdReader.getElements().keySet()) {
            System.out.println(e.getName() + ": ");
            for (XmlSchemaAttribute a : xsdReader.getElements().get(e)) {
                System.out.println("\t" + a.getName() + ": " + xsdReader.extractAttributeType(a));
            }
            System.out.println();
        }*/
        /*for (XMLElement element : xmlElements) {
            System.out.print(element.elementType() + ": ");
            xsdReader.getAttributesOfElementName().getOrDefault(element.elementType(), new HashSet<>())
                    .forEach(s -> System.out.print(s.getName() + "(" + xsdReader.extractAttributeType(s).toString() + ") "));
            System.out.println();
        }*/
    }

    public Start translate() {
        Translator translator;
        if (xsdFile != null) {
            translator = new XSDTranslator(xmlFile, xsdFile);
        } else {
            translator = new StandaloneTranslator(xmlFile);
        }
        Start start = translator.createBAst(xmlFile.getName().split("\\.")[0]);
        PrettyPrinter prettyPrinter = new PrettyPrinter();
        start.apply(prettyPrinter);
        System.out.println(prettyPrinter.getPrettyPrint());
        return start;
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