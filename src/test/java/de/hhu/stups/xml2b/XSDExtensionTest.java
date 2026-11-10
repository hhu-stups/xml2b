package de.hhu.stups.xml2b;

import de.hhu.stups.xml2b.readXml.XMLReader;
import de.hhu.stups.xml2b.readXsd.XSDElement;
import de.hhu.stups.xml2b.readXsd.XSDReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.xml.namespace.QName;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Schemas extending another schema in a different namespace: ext.xsd imports base.xsd from
 * an online location that does not exist and uses xsi:type extensions and a substitution group.
 */
public class XSDExtensionTest {

	private static final Path PATH = Path.of("src/test/resources/xsd/extension");
	private static final File BASE = PATH.resolve("base.xsd").toFile();
	private static final File EXT = PATH.resolve("ext.xsd").toFile();
	private static final File XML = PATH.resolve("items.xml").toFile();
	private static final List<File> SCHEMAS = List.of(BASE, EXT);
	private static final Map<String, File> LOCAL_SCHEMAS = Map.of("urn:base", BASE);

	private static final QName ITEMS = new QName("urn:base", "items");
	private static final QName ITEM = new QName("urn:base", "item");
	private static final QName PAYLOAD = new QName("urn:ext", "payload");
	private static final QName SLOT = new QName("urn:base", "extension");

	@Test
	void testImportResolvedToLocalSchema() {
		XSDReader reader = new XSDReader(SCHEMAS, LOCAL_SCHEMAS);
		Assertions.assertNotNull(reader.getElements().get(List.of(ITEMS, ITEM)));

		XMLReader xmlReader = new XMLReader();
		Assertions.assertEquals(8, xmlReader.readXML(XML, SCHEMAS, LOCAL_SCHEMAS).size()); // all elements read and validated
		Assertions.assertEquals(List.of(), xmlReader.getErrors());
	}

	@Test
	void testSubstitutionGroup() {
		Map<List<QName>, XSDElement> elements = new XSDReader(SCHEMAS, LOCAL_SCHEMAS).getElements();
		// payload replaces the abstract slot base:extension, which itself cannot occur
		XSDElement payload = elements.get(List.of(ITEMS, ITEM, PAYLOAD));
		Assertions.assertNotNull(payload);
		Assertions.assertTrue(payload.getAttributeTypes().get("value").isRequired());
		Assertions.assertNull(elements.get(List.of(ITEMS, ITEM, SLOT)));
	}
}
