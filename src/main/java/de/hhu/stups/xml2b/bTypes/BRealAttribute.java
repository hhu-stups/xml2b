package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.ARealExpression;
import de.be4.classicalb.core.parser.node.ARealSetExpression;
import de.be4.classicalb.core.parser.node.PExpression;
import de.be4.classicalb.core.parser.node.TRealLiteral;

public class BRealAttribute extends BAttribute {
	public BRealAttribute(String data) {
		super(data);
	}

	@Override
	public String toString() {
		return "REAL(" + data + ")";
	}

	@Override
	public PExpression getSetExpression() {
		return new ARealSetExpression();
	}

	@Override
	public PExpression getDataExpression() {
		return new ARealExpression(new TRealLiteral(data));
	}
}
