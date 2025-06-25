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

		StateSpace stateSpace = api.b_load(pathMachine.toString());
		State constants = stateSpace.getRoot().perform(Transition.SETUP_CONSTANTS_NAME);
		Assertions.assertEquals("[rec(Element:\"order\",attributes:{(\"id\"↦XmlString(\"id_abc\"))},content:∅,maxCId:5,pIds:[0],recId:1,xmlLocation:(2↦20↦(9↦9))),rec(Element:\"customer\",attributes:{(\"name\"↦XmlString(\"Müller\"))},content:∅,maxCId:3,pIds:[1],recId:2,xmlLocation:(3↦29↦(5↦16))),rec(Element:\"item\",attributes:{(\"product\"↦XmlString(\"XMLFile1\")),(\"version\"↦XmlReal(1.0))},content:∅,maxCId:3,pIds:[1,2],recId:3,xmlLocation:(4↦49↦(4↦49))),rec(Element:\"customer\",attributes:{(\"name\"↦XmlString(\"Paul\"))},content:∅,maxCId:5,pIds:[1],recId:4,xmlLocation:(6↦27↦(8↦16))),rec(Element:\"item\",attributes:{(\"product\"↦XmlString(\"XMLFile2\"))},content:∅,maxCId:5,pIds:[1,4],recId:5,xmlLocation:(7↦35↦(7↦35)))]",
				constants.eval("XML_DATA", FormulaExpand.EXPAND).toString());
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
