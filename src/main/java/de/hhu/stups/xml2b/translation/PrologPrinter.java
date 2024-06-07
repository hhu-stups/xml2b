package de.hhu.stups.xml2b.translation;

import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.node.*;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.output.PrologTermOutput;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static de.hhu.stups.xml2b.translation.Translator.XML_FREETYPE_ATTRIBUTES_NAME;

public class PrologPrinter extends DepthFirstAdapter {

	private final IPrologTermOutput pout;

	private final HashMap<String, PExpression> currRecFields = new HashMap<>();

	public PrologPrinter(final FileOutputStream out) {
		this.pout = new PrologTermOutput(out);
	}

	public void print(PExpression start) {
		start.apply(this);
		pout.fullstop();
		pout.flush();
	}

	@Override
	public void caseAStringExpression(AStringExpression node) {
		pout.openTerm("string");
		pout.printAtom(node.getContent().getText());
		pout.closeTerm();
	}

	@Override
	public void caseABooleanTrueExpression(ABooleanTrueExpression node) {
		pout.printAtom("pred_true");
	}

	@Override
	public void caseABooleanFalseExpression(ABooleanFalseExpression node) {
		pout.printAtom("pred_false");
	}

	@Override
	public void caseAIntegerExpression(AIntegerExpression node) {
		pout.openTerm("int");
		pout.printAtomOrNumber(node.getLiteral().getText());
		pout.closeTerm();
	}

	@Override
	public void caseARealExpression(ARealExpression node) {
		pout.openTerm("term");
		pout.openTerm("floating");
		pout.printNumber(Double.parseDouble(node.getLiteral().getText()));
		pout.closeTerm();
		pout.closeTerm();
	}

	@Override
	public void caseACoupleExpression(ACoupleExpression node) {
		pout.openTerm(",");
		for (PExpression elem : node.getList()) {
			elem.apply(this);
		}
		pout.closeTerm();
	}

	@Override
	public void caseAEmptySetExpression(AEmptySetExpression node) {
		pout.openList();
		pout.closeList();
	}

	@Override
	public void caseASetExtensionExpression(ASetExtensionExpression node) {
		pout.openList();
		for (PExpression elem : node.getExpressions()) {
			elem.apply(this);
		}
		pout.closeList();
	}

	@Override
	public void caseASequenceExtensionExpression(ASequenceExtensionExpression node) {
		pout.openList();
		List<PExpression> elements = node.getExpression();
		for (int i = 0; i < elements.size(); i++) {
			pout.openTerm(",");
			pout.openTerm("int");
			pout.printNumber(i+1);
			pout.closeTerm();
			elements.get(i).apply(this);
			pout.closeTerm();
		}
		pout.closeList();
	}

	@Override
	public void caseAFunctionExpression(AFunctionExpression node) {
		pout.openTerm("freeval");
		pout.printAtom(XML_FREETYPE_ATTRIBUTES_NAME);
		pout.printAtom(node.getIdentifier().toString().trim());
		node.getParameters().getFirst().apply(this);
		pout.closeTerm();
	}

	@Override
	public void caseARecExpression(ARecExpression node) {
		this.currRecFields.clear();
		pout.openTerm("rec");
		pout.openList();
		for (PRecEntry recEntry : node.getEntries()) {
			recEntry.apply(this);
		}
		// record fields must be sorted!
		List<String> sortedFields = new ArrayList<>(currRecFields.keySet());
		Collections.sort(sortedFields);
		for (String field : sortedFields) {
			pout.openTerm("field");
			pout.printAtom(field);
			currRecFields.get(field).apply(this);
			pout.closeTerm();
		}
		pout.closeList();
		pout.closeTerm();
	}

	@Override
	public void caseARecEntry(ARecEntry node) {
		currRecFields.put(node.getIdentifier().toString().trim(), node.getValue());
	}
}
