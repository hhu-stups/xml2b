package de.hhu.stups.xml2b;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;
import de.hhu.stups.xml2b.translation.StandaloneTranslator;
import de.hhu.stups.xml2b.translation.Translator;
import de.hhu.stups.xml2b.translation.XSDTranslator;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class XML2B {
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

    public XML2B(File xmlFile, XML2BOptions options) throws BCompoundException {
        this(xmlFile, null, options);
    }

    public XML2B(File xmlFile, File xsdFile, XML2BOptions options) throws BCompoundException {
        this.translator = xsdFile == null ? new StandaloneTranslator(xmlFile) : new XSDTranslator(xmlFile, xsdFile);
        this.translator.setCustomOptions(options);
    }

    public Start translate() {
        return translator.createBAst();
    }
}