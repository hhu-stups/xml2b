package de.hhu.stups.xml2b;

import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.util.PrettyPrinter;
import de.hhu.stups.xml2b.translation.StandaloneTranslator;
import de.hhu.stups.xml2b.translation.Translator;
import de.hhu.stups.xml2b.translation.XSDTranslator;
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

    private final Translator translator;

    public XML2B(File xmlFile, File xsdFile) {
        if (xsdFile != null) {
            this.translator = new XSDTranslator(xmlFile, xsdFile);
        } else {
            this.translator = new StandaloneTranslator(xmlFile);
        }
    }

    public Start translate() {
        return translator.createBAst();
    }
}