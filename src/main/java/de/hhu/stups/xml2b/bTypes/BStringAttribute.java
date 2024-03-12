package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.AStringExpression;
import de.be4.classicalb.core.parser.node.AStringSetExpression;
import de.be4.classicalb.core.parser.node.PExpression;
import de.be4.classicalb.core.parser.node.TStringLiteral;

public class BStringAttribute extends BAttribute {
	public BStringAttribute(String data) {
		super(data);
	}

	@Override
	public String toString() {
		return "STRING(" + data + ")";
	}

	@Override
	public PExpression getSetExpression() {
		return new AStringSetExpression();
	}

	@Override
	public PExpression getDataExpression() {
		return new AStringExpression(new TStringLiteral(data));
	}
}
