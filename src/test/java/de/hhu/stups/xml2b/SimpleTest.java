package de.hhu.stups.xml2b;

import de.hhu.stups.xml2b.cli.XML2BCli;
import de.prob.animator.domainobjects.FormulaExpand;
import de.prob.scripting.Api;
import de.prob.statespace.State;
import de.prob.statespace.StateSpace;
import de.prob.statespace.Transition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class SimpleTest {

	private static final Path path = Paths.get("src/test/resources/simple");
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
	void testSimpleSicstusFastRw() throws Exception {
		String name = "simple";
		final Path pathInput = path.resolve(name + ".xml");
		final Path pathMachine = path.resolve(name + ".mch");
		final Path pathData = path.resolve(name + ".probdata");

		XML2BCli.main(new String[]{pathInput.toFile().toString(),"-frw","SICSTUS","-o",pathMachine.toString()});

		StateSpace stateSpace = this.b_load(pathMachine.toString());
		State constants = stateSpace.getRoot().perform(Transition.SETUP_CONSTANTS_NAME);
		Assertions.assertEquals("[rec(Element:\"order\",attributes:{(\"id\"↦XmlString(\"id_abc\"))},content:[],maxCId:5,ns:\"\",pIds:[0],recId:1,xmlLocation:(2↦20↦(9↦9))),rec(Element:\"customer\",attributes:{(\"name\"↦XmlString(\"Müller\"))},content:[],maxCId:3,ns:\"\",pIds:[1],recId:2,xmlLocation:(3↦29↦(5↦16))),rec(Element:\"item\",attributes:{(\"product\"↦XmlString(\"XMLFile1\")),(\"version\"↦XmlReal(1.0))},content:[],maxCId:3,ns:\"\",pIds:[1,2],recId:3,xmlLocation:(4↦49↦(4↦49))),rec(Element:\"customer\",attributes:{(\"name\"↦XmlString(\"Paul\"))},content:[],maxCId:5,ns:\"\",pIds:[1],recId:4,xmlLocation:(6↦27↦(8↦16))),rec(Element:\"item\",attributes:{(\"product\"↦XmlString(\"XMLFile2\"))},content:[],maxCId:5,ns:\"\",pIds:[1,4],recId:5,xmlLocation:(7↦35↦(7↦35)))]",
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

		StateSpace stateSpace = this.b_load(pathMachine.toString());
		State constants = stateSpace.getRoot().perform(Transition.SETUP_CONSTANTS_NAME);
		Assertions.assertEquals("[rec(Element:\"order\",attributes:{(\"id\"↦XmlString(\"id_abc\"))},content:[],maxCId:5,ns:\"\",pIds:[0],recId:1,xmlLocation:(2↦20↦(9↦9))),rec(Element:\"customer\",attributes:{(\"name\"↦XmlString(\"Müller\"))},content:[],maxCId:3,ns:\"\",pIds:[1],recId:2,xmlLocation:(3↦29↦(5↦16))),rec(Element:\"item\",attributes:{(\"product\"↦XmlString(\"XMLFile1\")),(\"version\"↦XmlReal(1.0))},content:[],maxCId:3,ns:\"\",pIds:[1,2],recId:3,xmlLocation:(4↦49↦(4↦49))),rec(Element:\"customer\",attributes:{(\"name\"↦XmlString(\"Paul\"))},content:[],maxCId:5,ns:\"\",pIds:[1],recId:4,xmlLocation:(6↦27↦(8↦16))),rec(Element:\"item\",attributes:{(\"product\"↦XmlString(\"XMLFile2\"))},content:[],maxCId:5,ns:\"\",pIds:[1,4],recId:5,xmlLocation:(7↦35↦(7↦35)))]",
				constants.eval("XML_DATA", FormulaExpand.EXPAND).toString());
		Assertions.assertTrue(pathMachine.toFile().delete());
		Assertions.assertTrue(pathData.toFile().delete());

		stateSpace.kill();
	}

	@Test
	void testSimpleTextInline() throws Exception {
		String name = "simple";
		final Path pathInput = path.resolve(name + ".xml");
		final Path pathMachine = path.resolve(name + ".mch");

		XML2BCli.main(new String[]{pathInput.toFile().toString(),"-inline-data","-o",pathMachine.toString()});

		StateSpace stateSpace = this.b_load(pathMachine.toString());
		State constants = stateSpace.getRoot().perform(Transition.SETUP_CONSTANTS_NAME);
		Assertions.assertEquals("[rec(Element:\"order\",attributes:{(\"id\"↦XmlString(\"id_abc\"))},content:[],maxCId:5,ns:\"\",pIds:[0],recId:1,xmlLocation:(2↦20↦(9↦9))),rec(Element:\"customer\",attributes:{(\"name\"↦XmlString(\"Müller\"))},content:[],maxCId:3,ns:\"\",pIds:[1],recId:2,xmlLocation:(3↦29↦(5↦16))),rec(Element:\"item\",attributes:{(\"product\"↦XmlString(\"XMLFile1\")),(\"version\"↦XmlReal(1.0))},content:[],maxCId:3,ns:\"\",pIds:[1,2],recId:3,xmlLocation:(4↦49↦(4↦49))),rec(Element:\"customer\",attributes:{(\"name\"↦XmlString(\"Paul\"))},content:[],maxCId:5,ns:\"\",pIds:[1],recId:4,xmlLocation:(6↦27↦(8↦16))),rec(Element:\"item\",attributes:{(\"product\"↦XmlString(\"XMLFile2\"))},content:[],maxCId:5,ns:\"\",pIds:[1,4],recId:5,xmlLocation:(7↦35↦(7↦35)))]",
				constants.eval("XML_DATA", FormulaExpand.EXPAND).toString());
		Assertions.assertTrue(pathMachine.toFile().delete());
		Assertions.assertFalse(path.resolve(name + ".probdata").toFile().delete());

		stateSpace.kill();
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

	@Test
	void testSimpleDotName() throws Exception {
		final Path pathInput = path.resolve("empty.dot.xml");
		final Path pathCorrect = path.resolve("empty_dot.mch");
		final Path pathCorrect2 = path.resolve("empty_dot.probdata");

		XML2BCli.main(new String[]{pathInput.toString(), "-o", pathCorrect.toString()});

		Assertions.assertTrue(pathCorrect.toFile().delete());
		Assertions.assertTrue(pathCorrect2.toFile().delete());
	}

	@Test
	void testLargeNumbers() throws Exception {
		// check that large numbers are handled correctly and that too large number is treated as string
		final Path pathInput = path.resolve("large_number.xml");
		final Path pathCorrect = path.resolve("large_number.mch");

		XML2BCli.main(new String[]{pathInput.toString(), "-inline-data", "-o", pathCorrect.toString()});
		StateSpace stateSpace = this.b_load(pathCorrect.toString());
		State constants = stateSpace.getRoot().perform(Transition.SETUP_CONSTANTS_NAME);
		Assertions.assertEquals("[rec(Element:\"numbers\",attributes:[],content:[],maxCId:3,ns:\"\",pIds:[0],recId:1,xmlLocation:(2↦10↦(5↦11))),rec(Element:\"number\",attributes:{(\"bitmask\"↦XmlReal(1.101010101010101E+165))},content:[],maxCId:2,ns:\"\",pIds:[1],recId:2,xmlLocation:(3↦191↦(3↦191))),rec(Element:\"number\",attributes:{(\"bitmask\"↦XmlString(\"11010101010101010101010101010101010010101010011001111100000101010010101001010101001010101000010100101010101001010000100101010100101010010100100000100111111101010010101101010101010101010101010101010101001010101001100111110000010101001010100101010100101010100001010010101010100101000010010101010010101001010010000010011111110101001010\"))},content:[],maxCId:3,ns:\"\",pIds:[1],recId:3,xmlLocation:(4↦357↦(4↦357)))]",
				constants.eval("XML_DATA", FormulaExpand.EXPAND).toString());

		Assertions.assertTrue(pathCorrect.toFile().delete());

		stateSpace.kill();
	}

}
