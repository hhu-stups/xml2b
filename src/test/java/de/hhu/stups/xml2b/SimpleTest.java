package de.hhu.stups.xml2b;

import de.hhu.stups.xml2b.cli.XML2BCli;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.scripting.Api;
import de.prob.statespace.State;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Transition;
import org.junit.jupiter.api.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class SimpleTest {

	private static final Path path = Paths.get("src/test/resources/simple");
	private static Api api;

	@BeforeAll
	static void beforeAll() {
		api = TestModule.getInjector().getInstance(Api.class);
	}

	@Test
	void testSimpleSicstusFastRw() throws Exception {
		String name = "simple";
		final Path pathInput = path.resolve(name + ".xml");
		final Path pathMachine = path.resolve(name + ".mch");
		final Path pathData = path.resolve(name + ".probdata");

		XML2BCli.main(new String[]{pathInput.toFile().toString(),"-o",pathMachine.toString()});

		StateSpace stateSpace = api.b_load(pathMachine.toString());
		State constants = stateSpace.getRoot().perform(Transition.SETUP_CONSTANTS_NAME);
		Assertions.assertEquals("[rec(Element:\"order\",attributes:{(\"id\"↦XmlString(\"id_abc\"))},content:∅,maxCId:5,pIds:[0],recId:1,xmlLocation:(2↦20↦(9↦9))),rec(Element:\"customer\",attributes:{(\"name\"↦XmlString(\"Müller\"))},content:∅,maxCId:3,pIds:[1],recId:2,xmlLocation:(3↦29↦(5↦16))),rec(Element:\"item\",attributes:{(\"product\"↦XmlString(\"XMLFile1\")),(\"version\"↦XmlReal(1.0))},content:∅,maxCId:3,pIds:[1,2],recId:3,xmlLocation:(4↦49↦(4↦49))),rec(Element:\"customer\",attributes:{(\"name\"↦XmlString(\"Paul\"))},content:∅,maxCId:5,pIds:[1],recId:4,xmlLocation:(6↦27↦(8↦16))),rec(Element:\"item\",attributes:{(\"product\"↦XmlString(\"XMLFile2\"))},content:∅,maxCId:5,pIds:[1,4],recId:5,xmlLocation:(7↦35↦(7↦35)))]",
				constants.eval("XML_DATA", FormulaExpand.EXPAND).toString());
		Assertions.assertTrue(pathMachine.toFile().delete());
		Assertions.assertTrue(pathData.toFile().delete());
	}

	@Test
	void testSimpleTextData() throws Exception {
		String name = "simple";
		final Path pathInput = path.resolve(name + ".xml");
		final Path pathMachine = path.resolve(name + ".mch");
		final Path pathData = path.resolve(name + ".probdata");

		XML2BCli.main(new String[]{pathInput.toFile().toString(),"-frw","NONE","-o",pathMachine.toString()});

		List<String> lines = Files.readAllLines(pathData, StandardCharsets.UTF_8);
		Assertions.assertFalse(lines.isEmpty(), "File is empty");
		Assertions.assertEquals("[','(int(1),rec([field('Element',string(order)),field(attributes,[','(string(id),freeval('XML_ATTRIBUTE_TYPES','XmlString',string(id_abc))),','(string(id),freeval('XML_ATTRIBUTE_TYPES','XmlString',string(id_abc)))]),field(content,[]),field(maxCId,int(5)),field(pIds,[','(int(1),int(0))]),field(recId,int(1)),field(xmlLocation,','(','(int(2),int(20)),','(int(9),int(9))))])),','(int(2),rec([field('Element',string(customer)),field(attributes,[','(string(name),freeval('XML_ATTRIBUTE_TYPES','XmlString',string('M\\374\\ller'))),','(string(name),freeval('XML_ATTRIBUTE_TYPES','XmlString',string('M\\374\\ller')))]),field(content,[]),field(maxCId,int(3)),field(pIds,[','(int(1),int(1))]),field(recId,int(2)),field(xmlLocation,','(','(int(3),int(29)),','(int(5),int(16))))])),','(int(3),rec([field('Element',string(item)),field(attributes,[','(string(product),freeval('XML_ATTRIBUTE_TYPES','XmlString',string('XMLFile1'))),','(string(version),freeval('XML_ATTRIBUTE_TYPES','XmlReal',term(floating(1.0)))),','(string(product),freeval('XML_ATTRIBUTE_TYPES','XmlString',string('XMLFile1'))),','(string(version),freeval('XML_ATTRIBUTE_TYPES','XmlReal',term(floating(1.0))))]),field(content,[]),field(maxCId,int(3)),field(pIds,[','(int(1),int(1)),','(int(2),int(2))]),field(recId,int(3)),field(xmlLocation,','(','(int(4),int(49)),','(int(4),int(49))))])),','(int(4),rec([field('Element',string(customer)),field(attributes,[','(string(name),freeval('XML_ATTRIBUTE_TYPES','XmlString',string('Paul'))),','(string(name),freeval('XML_ATTRIBUTE_TYPES','XmlString',string('Paul')))]),field(content,[]),field(maxCId,int(5)),field(pIds,[','(int(1),int(1))]),field(recId,int(4)),field(xmlLocation,','(','(int(6),int(27)),','(int(8),int(16))))])),','(int(5),rec([field('Element',string(item)),field(attributes,[','(string(product),freeval('XML_ATTRIBUTE_TYPES','XmlString',string('XMLFile2'))),','(string(product),freeval('XML_ATTRIBUTE_TYPES','XmlString',string('XMLFile2')))]),field(content,[]),field(maxCId,int(5)),field(pIds,[','(int(1),int(1)),','(int(2),int(4))]),field(recId,int(5)),field(xmlLocation,','(','(int(7),int(35)),','(int(7),int(35))))]))].", lines.get(0));
		Assertions.assertTrue(pathMachine.toFile().delete());
		Assertions.assertTrue(pathData.toFile().delete());
	}

	@Test
	void testSimpleCustomName() throws Exception {
		final Path pathInput = path.resolve("simple.xml");
		final Path pathCorrect = path.resolve("Simple.mch");
		final Path pathCorrect2 = path.resolve("Simple.probdata");

		XML2BCli.main(new String[]{pathInput.toString(), "-o", pathCorrect.toString()});

		Assertions.assertTrue(pathCorrect.toFile().delete());
		Assertions.assertTrue(pathCorrect2.toFile().delete());
	}
}
