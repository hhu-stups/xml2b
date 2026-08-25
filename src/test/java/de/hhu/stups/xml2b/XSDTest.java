package de.hhu.stups.xml2b;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.hhu.stups.xml2b.bTypes.BIntegerAttributeType;
import de.hhu.stups.xml2b.bTypes.BStringAttributeType;
import de.hhu.stups.xml2b.cli.XML2BCli;
import de.hhu.stups.xml2b.readXsd.XSDElement;
import de.hhu.stups.xml2b.readXsd.XSDReader;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.scripting.Api;
import de.prob.statespace.State;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Transition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class XSDTest {

	private static final Path path = Paths.get("src/test/resources/xsd");
	private static Api api;

	@BeforeAll
	static void beforeAll() {
		api = TestModule.getInjector().getInstance(Api.class);
	}

	StateSpace b_load(String path) throws IOException {
		StateSpace stateSpace = api.b_load(path);
		stateSpace.changePreferences(Map.of("PP_SEQUENCES", "TRUE"));
		return stateSpace;
	}

	@Test
	void testSimpleXSD() throws Exception {
		String name = "person";
		final Path pathInput = path.resolve(name + ".xml");
		final Path pathXsd = path.resolve(name + ".xsd");
		final Path pathMachine = path.resolve(name + ".mch");
		final Path pathData = path.resolve(name + ".probdata");

		XSDReader reader = new XSDReader(pathXsd.toFile());
		Map<List<QName>, XSDElement> xsdElements = reader.getElements();
		QName course = new QName("course");
		QName student = new QName("student");
		QName school = new QName("school");

		XSDElement courseElement = xsdElements.get(List.of(school,student,course));
		Assertions.assertInstanceOf(BStringAttributeType.class, courseElement.getAttributeTypes().get("title"));
		Assertions.assertTrue(courseElement.getAttributeTypes().get("title").isRequired());
		Assertions.assertInstanceOf(BIntegerAttributeType.class, courseElement.getAttributeTypes().get("credits"));
		Assertions.assertFalse(courseElement.getAttributeTypes().get("credits").isRequired());

		XSDElement studentElement = xsdElements.get(List.of(school,student));
		Assertions.assertInstanceOf(BStringAttributeType.class, studentElement.getAttributeTypes().get("name"));
		Assertions.assertTrue(studentElement.getAttributeTypes().get("name").isRequired());
		Assertions.assertInstanceOf(BIntegerAttributeType.class, studentElement.getAttributeTypes().get("age"));
		Assertions.assertFalse(studentElement.getAttributeTypes().get("age").isRequired());
		Assertions.assertEquals(1, studentElement.getMinOccurs().intValue());
		Assertions.assertEquals(-1, studentElement.getMaxOccurs().intValue());

		XSDElement schoolElement = xsdElements.get(List.of(school));
		Assertions.assertInstanceOf(BStringAttributeType.class, schoolElement.getAttributeTypes().get("id"));
		Assertions.assertTrue(schoolElement.getAttributeTypes().get("id").isRequired());

		XML2BCli.main(new String[]{pathInput.toFile().toString(),"-xsd",pathXsd.toString(),"-o",pathMachine.toString()});

		StateSpace stateSpace = this.b_load(pathMachine.toString());
		State constants = stateSpace.getRoot().perform(Transition.SETUP_CONSTANTS_NAME);
		Assertions.assertEquals("[rec(Element:\"school\",attributes:{(\"id\"↦XmlString(\"school01\"))},content:[],maxCId:5,ns:\"\",pIds:[0],recId:1,xmlLocation:(2↦23↦(10↦10))),rec(Element:\"student\",attributes:{(\"age\"↦XmlInteger(20)),(\"name\"↦XmlString(\"Anna\"))},content:[],maxCId:3,ns:\"\",pIds:[1],recId:2,xmlLocation:(3↦35↦(5↦15))),rec(Element:\"course\",attributes:{(\"credits\"↦XmlInteger(5)),(\"title\"↦XmlString(\"Mathematik\"))},content:[],maxCId:3,ns:\"\",pIds:[1,2],recId:3,xmlLocation:(4↦49↦(4↦49))),rec(Element:\"student\",attributes:{(\"name\"↦XmlString(\"Ben\"))},content:[],maxCId:5,ns:\"\",pIds:[1],recId:4,xmlLocation:(7↦25↦(9↦15))),rec(Element:\"course\",attributes:{(\"title\"↦XmlString(\"Informatik\"))},content:[],maxCId:5,ns:\"\",pIds:[1,4],recId:5,xmlLocation:(8↦37↦(8↦37)))]",
				constants.eval("XML_DATA", FormulaExpand.EXPAND).toString());
		Assertions.assertTrue(pathMachine.toFile().delete());
		Assertions.assertTrue(pathData.toFile().delete());

		stateSpace.kill();
	}

	@Test
	void testSimpleEnumeration() throws Exception {
		String name = "ticket";
		final Path pathInput = path.resolve(name + ".xml");
		final Path pathXsd = path.resolve(name + ".xsd");
		final Path pathMachine = path.resolve(name + ".mch");
		final Path pathData = path.resolve(name + ".probdata");

		XML2BCli.main(new String[]{pathInput.toFile().toString(),"-xsd",pathXsd.toString(),"-o",pathMachine.toString()});

		StateSpace stateSpace = this.b_load(pathMachine.toString());
		State constants = stateSpace.getRoot().perform(Transition.SETUP_CONSTANTS_NAME);
		Assertions.assertEquals("[rec(Element:\"ticket\",attributes:{(\"id\"↦XmlString(\"T1\")),(\"status\"↦XmlStatusType(StatusType_open))},content:[],maxCId:1,ns:\"\",pIds:[0],recId:1,xmlLocation:(2↦32↦(2↦32)))]",
				constants.eval("XML_DATA", FormulaExpand.EXPAND).toString());
		Assertions.assertEquals("{StatusType_pending,StatusType_closed,StatusType_open}",
				constants.eval("StatusType", FormulaExpand.EXPAND).toString());
		Assertions.assertTrue(pathMachine.toFile().delete());
		Assertions.assertTrue(pathData.toFile().delete());

		stateSpace.kill();
	}

	@Test
	void testExtendableEnumeration() throws Exception {
		String name = "status";
		final Path pathInput = path.resolve(name + ".xml");
		final Path pathXsd = path.resolve(name + ".xsd");
		final Path pathMachine = path.resolve(name + ".mch");
		final Path pathData = path.resolve(name + ".probdata");

		XML2BCli.main(new String[]{pathInput.toFile().toString(),"-xsd",pathXsd.toString(),"-o",pathMachine.toString()});

		StateSpace stateSpace = this.b_load(pathMachine.toString());
		State constants = stateSpace.getRoot().perform(Transition.SETUP_CONSTANTS_NAME);
		Assertions.assertEquals("[rec(Element:\"accounts\",attributes:[],content:[],maxCId:6,ns:\"\",pIds:[0],recId:1,xmlLocation:(2↦11↦(8↦12))),rec(Element:\"account\",attributes:{(\"id\"↦XmlString(\"A1\")),(\"status\"↦XmlStatusType(StatusType_new))},content:[],maxCId:2,ns:\"\",pIds:[1],recId:2,xmlLocation:(3↦36↦(3↦36))),rec(Element:\"account\",attributes:{(\"id\"↦XmlString(\"A2\")),(\"status\"↦XmlStatusType(StatusType_active))},content:[],maxCId:3,ns:\"\",pIds:[1],recId:3,xmlLocation:(4↦39↦(4↦39))),rec(Element:\"account\",attributes:{(\"id\"↦XmlString(\"A3\")),(\"status\"↦XmlStatusType(`StatusType_custom-premium`))},content:[],maxCId:4,ns:\"\",pIds:[1],recId:4,xmlLocation:(5↦47↦(5↦47))),rec(Element:\"account\",attributes:{(\"id\"↦XmlString(\"A4\")),(\"status\"↦XmlStatusType(`StatusType_custom-test123`))},content:[],maxCId:5,ns:\"\",pIds:[1],recId:5,xmlLocation:(6↦47↦(6↦47))),rec(Element:\"account\",attributes:{(\"id\"↦XmlString(\"A5\")),(\"status\"↦XmlStatusType(StatusType_inactive))},content:[],maxCId:6,ns:\"\",pIds:[1],recId:6,xmlLocation:(7↦41↦(7↦41)))]",
				constants.eval("XML_DATA", FormulaExpand.EXPAND).toString());
		Assertions.assertTrue(pathMachine.toFile().delete());
		Assertions.assertTrue(pathData.toFile().delete());

		stateSpace.kill();
	}

	@Test
	void testInvalidEnumeration() throws Exception {
		final Path pathInput = path.resolve("status_invalid.xml");
		final Path pathXsd = path.resolve("status.xsd");

		Assertions.assertThrows(BCompoundException.class, () -> new XML2B(pathInput.toFile(), pathXsd.toFile(), XML2BOptions.defaultOptions(pathXsd.toFile())));
	}

	@Test
	void testXSDInclusionWithComplexType() throws Exception {
		String name = "library";
		final Path pathInput = path.resolve(name + ".xml");
		final Path pathXsd = path.resolve(name + ".xsd");
		final Path pathMachine = path.resolve(name + ".mch");
		final Path pathData = path.resolve(name + ".probdata");

		XML2BCli.main(new String[]{pathInput.toFile().toString(),"-xsd",pathXsd.toString(),"-o",pathMachine.toString()});

		StateSpace stateSpace = this.b_load(pathMachine.toString());
		State constants = stateSpace.getRoot().perform(Transition.SETUP_CONSTANTS_NAME);
		Assertions.assertEquals("[rec(Element:\"library\",attributes:{(\"name\"↦XmlString(\"Stadtbibliothek\"))},content:[],maxCId:3,ns:\"\",pIds:[0],recId:1,xmlLocation:(2↦33↦(5↦11))),rec(Element:\"book\",attributes:{(\"author\"↦XmlString(\"Max Mustermann\")),(\"title\"↦XmlString(\"XML Grundlagen\"))},content:[],maxCId:2,ns:\"\",pIds:[1],recId:2,xmlLocation:(3↦59↦(3↦59))),rec(Element:\"book\",attributes:{(\"title\"↦XmlString(\"Java Basics\"))},content:[],maxCId:3,ns:\"\",pIds:[1],recId:3,xmlLocation:(4↦32↦(4↦32)))]",
				constants.eval("XML_DATA", FormulaExpand.EXPAND).toString());
		Assertions.assertTrue(pathMachine.toFile().delete());
		Assertions.assertTrue(pathData.toFile().delete());

		stateSpace.kill();
	}

	@Test
	void testNameSpaces() throws Exception {
		String name = "ns_data";
		final Path pathInput = path.resolve(name + ".xml");
		final Path pathXsd = path.resolve("ns_person.xsd");
		final Path pathMachine = path.resolve(name + ".mch");
		final Path pathData = path.resolve(name + ".probdata");

		XML2BCli.main(new String[]{pathInput.toFile().toString(),"-xsd",pathXsd.toString(),"-o",pathMachine.toString()});

		StateSpace stateSpace = this.b_load(pathMachine.toString());
		State constants = stateSpace.getRoot().perform(Transition.SETUP_CONSTANTS_NAME);
		Assertions.assertEquals("[rec(Element:\"person\",attributes:[],content:[],maxCId:3,ns:\"http://example.com/person\",pIds:[0],recId:1,xmlLocation:(4↦46↦(9↦12))),rec(Element:\"name\",attributes:[],content:{XmlString(\"Anna\")},maxCId:2,ns:\"http://example.com/person\",pIds:[1],recId:2,xmlLocation:(6↦13↦(6↦26))),rec(Element:\"city\",attributes:[],content:{XmlString(\"Berlin\")},maxCId:3,ns:\"http://example.com/address\",pIds:[1],recId:3,xmlLocation:(7↦13↦(7↦28)))]",
				constants.eval("XML_DATA", FormulaExpand.EXPAND).toString());
		Assertions.assertTrue(pathMachine.toFile().delete());
		Assertions.assertTrue(pathData.toFile().delete());

		stateSpace.kill();
	}

	@Test
	void testDefaultAndFixedValues() throws Exception {
		String name = "settings";
		final Path pathInput = path.resolve(name + ".xml");
		final Path pathXsd = path.resolve(name + ".xsd");
		final Path pathMachine = path.resolve(name + ".mch");
		final Path pathData = path.resolve(name + ".probdata");

		XML2BCli.main(new String[]{pathInput.toFile().toString(),"-xsd",pathXsd.toString(),"-o",pathMachine.toString()});

		StateSpace stateSpace = this.b_load(pathMachine.toString());
		State constants = stateSpace.getRoot().perform(Transition.SETUP_CONSTANTS_NAME);
		Assertions.assertEquals("[rec(Element:\"settings\",attributes:[],content:[],maxCId:3,ns:\"\",pIds:[0],recId:1,xmlLocation:(2↦11↦(5↦12))),rec(Element:\"user\",attributes:{(\"role\"↦XmlString(\"guest\")),(\"version\"↦XmlString(\"1.0\"))},content:[],maxCId:2,ns:\"\",pIds:[1],recId:2,xmlLocation:(3↦12↦(3↦12))),rec(Element:\"user\",attributes:{(\"role\"↦XmlString(\"admin\")),(\"version\"↦XmlString(\"1.0\"))},content:[],maxCId:3,ns:\"\",pIds:[1],recId:3,xmlLocation:(4↦39↦(4↦39)))]",
				constants.eval("XML_DATA", FormulaExpand.EXPAND).toString());
		Assertions.assertTrue(pathMachine.toFile().delete());
		Assertions.assertTrue(pathData.toFile().delete());

		stateSpace.kill();
	}

	@Test
	void testAnyAttribute() throws Exception {
		String name = "device";
		final Path pathInput = path.resolve(name + ".xml");
		final Path pathXsd = path.resolve(name + ".xsd");
		final Path pathMachine = path.resolve(name + ".mch");
		final Path pathData = path.resolve(name + ".probdata");

		XML2BCli.main(new String[]{pathInput.toFile().toString(),"-xsd",pathXsd.toString(),"-o",pathMachine.toString()});

		StateSpace stateSpace = this.b_load(pathMachine.toString());
		State constants = stateSpace.getRoot().perform(Transition.SETUP_CONSTANTS_NAME);
		Assertions.assertEquals("[rec(Element:\"devices\",attributes:[],content:[],maxCId:3,ns:\"\",pIds:[0],recId:1,xmlLocation:(2↦10↦(5↦11))),rec(Element:\"device\",attributes:{(\"enabled\"↦XmlString(\"true\")),(\"nr\"↦XmlInteger(1))},content:[],maxCId:2,ns:\"\",pIds:[1],recId:2,xmlLocation:(3↦36↦(3↦36))),rec(Element:\"device\",attributes:{(\"nr\"↦XmlInteger(2)),(\"temperature\"↦XmlString(\"21.5\"))},content:[],maxCId:3,ns:\"\",pIds:[1],recId:3,xmlLocation:(4↦40↦(4↦40)))]",
				constants.eval("XML_DATA", FormulaExpand.EXPAND).toString());
		Assertions.assertTrue(pathMachine.toFile().delete());
		Assertions.assertTrue(pathData.toFile().delete());

		stateSpace.kill();
	}

}
