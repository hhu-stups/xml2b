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

public class XSDBasicTest {

	private static final Path path = Paths.get("src/test/resources/xsd/basic");
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
	void testBooleans() throws Exception {
		String name = "booleans";
		final Path pathInput = path.resolve(name + ".xml");
		final Path pathXsd = path.resolve(name + ".xsd");
		final Path pathMachine = path.resolve(name + ".mch");

		XML2BCli.main(new String[]{pathInput.toFile().toString(),"-xsd",pathXsd.toString(),"-inline-data","-o",pathMachine.toString()});

		StateSpace stateSpace = this.b_load(pathMachine.toString());
		State constants = stateSpace.getRoot().perform(Transition.SETUP_CONSTANTS_NAME);
		Assertions.assertEquals("[rec(Element:\"booleans\",attributes:[],content:[],maxCId:5,ns:\"\",pIds:[0],recId:1,xmlLocation:(2↦11↦(7↦12))),rec(Element:\"boolean\",attributes:{(\"value\"↦XmlBool(TRUE))},content:[],maxCId:2,ns:\"\",pIds:[1],recId:2,xmlLocation:(3↦28↦(3↦28))),rec(Element:\"boolean\",attributes:{(\"value\"↦XmlBool(FALSE))},content:[],maxCId:3,ns:\"\",pIds:[1],recId:3,xmlLocation:(4↦29↦(4↦29))),rec(Element:\"boolean\",attributes:{(\"value\"↦XmlBool(TRUE))},content:[],maxCId:4,ns:\"\",pIds:[1],recId:4,xmlLocation:(5↦25↦(5↦25))),rec(Element:\"boolean\",attributes:{(\"value\"↦XmlBool(FALSE))},content:[],maxCId:5,ns:\"\",pIds:[1],recId:5,xmlLocation:(6↦25↦(6↦25)))]",
				constants.eval("XML_DATA", FormulaExpand.EXPAND).toString());
		Assertions.assertTrue(pathMachine.toFile().delete());

		stateSpace.kill();
	}

}
