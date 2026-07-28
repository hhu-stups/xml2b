package de.hhu.stups.xml2b;

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

		XML2BCli.main(new String[]{pathInput.toFile().toString(),"-xsd",pathXsd.toString(),"-o",pathMachine.toString(),"-frw","NONE"});

		StateSpace stateSpace = api.b_load(pathMachine.toString());
		State constants = stateSpace.getRoot().perform(Transition.SETUP_CONSTANTS_NAME);
		Assertions.assertEquals("[rec(Element:\"school\",attributes:{(\"id\"↦XmlString(\"school01\"))},content:∅,maxCId:5,ns:\"\",pIds:[0],recId:1,xmlLocation:(2↦23↦(10↦10))),rec(Element:\"student\",attributes:{(\"age\"↦XmlInteger(20)),(\"name\"↦XmlString(\"Anna\"))},content:∅,maxCId:3,ns:\"\",pIds:[1],recId:2,xmlLocation:(3↦35↦(5↦15))),rec(Element:\"course\",attributes:{(\"credits\"↦XmlInteger(5)),(\"title\"↦XmlString(\"Mathematik\"))},content:∅,maxCId:3,ns:\"\",pIds:[1,2],recId:3,xmlLocation:(4↦49↦(4↦49))),rec(Element:\"student\",attributes:{(\"name\"↦XmlString(\"Ben\"))},content:∅,maxCId:5,ns:\"\",pIds:[1],recId:4,xmlLocation:(7↦25↦(9↦15))),rec(Element:\"course\",attributes:{(\"title\"↦XmlString(\"Informatik\"))},content:∅,maxCId:5,ns:\"\",pIds:[1,4],recId:5,xmlLocation:(8↦37↦(8↦37)))]",
				constants.eval("XML_DATA", FormulaExpand.EXPAND).toString());
		Assertions.assertTrue(pathMachine.toFile().delete());
		Assertions.assertTrue(pathData.toFile().delete());
	}

	// TODO: enum sets, namespaces, default, fixed value, anyAttribute
}
