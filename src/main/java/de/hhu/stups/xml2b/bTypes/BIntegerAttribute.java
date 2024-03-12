package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.AIntegerExpression;
import de.be4.classicalb.core.parser.node.AIntegerSetExpression;
import de.be4.classicalb.core.parser.node.PExpression;
import de.be4.classicalb.core.parser.node.TIntegerLiteral;

public class BIntegerAttribute extends BAttribute {
	public BIntegerAttribute(String data) {
		super(data);
	}

	@Override
	public String toString() {
		return "INTEGER(" + data + ")";
	}

	@Override
	public PExpression getSetExpression() {
		return new AIntegerSetExpression();
	}

	@Override
	public PExpression getDataExpression() {
		return new AIntegerExpression(new TIntegerLiteral(data));
	}
}
