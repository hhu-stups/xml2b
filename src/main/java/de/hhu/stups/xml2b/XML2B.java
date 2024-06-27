package de.hhu.stups.xml2b;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;
import de.hhu.stups.xml2b.translation.StandaloneTranslator;
import de.hhu.stups.xml2b.translation.Translator;
import de.hhu.stups.xml2b.translation.XSDTranslator;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
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
    private final Path directory;
    private final String modelName;

    public XML2B(File... files) throws BCompoundException {
        if (files.length == 1) {
            this.translator = new StandaloneTranslator(files[0]);
        } else if (files.length == 2) {
            this.translator = new XSDTranslator(files[0], files[1]);
        } else {
            throw new IllegalArgumentException("expected 1 or 2 files, got " + files.length);
        }
        this.directory = files[0].getAbsoluteFile().getParentFile().toPath();
        this.modelName = files[0].getName().split("\\.")[0];
    }

    public Start translate() {
        // TODO: option for fastrw
        return translator.createBAst(new File(String.valueOf(directory.resolve(modelName + ".probdata"))));
    }
}