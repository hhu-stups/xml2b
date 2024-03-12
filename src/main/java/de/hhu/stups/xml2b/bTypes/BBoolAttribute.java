package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.ABoolSetExpression;
import de.be4.classicalb.core.parser.node.ABooleanFalseExpression;
import de.be4.classicalb.core.parser.node.ABooleanTrueExpression;
import de.be4.classicalb.core.parser.node.PExpression;

public class BBoolAttribute extends BAttribute {
	public BBoolAttribute(String data) {
		super(data);
	}

	@Override
	public String toString() {
		return "BOOL(" + data + ")";
	}

	@Override
	public PExpression getSetExpression() {
		return new ABoolSetExpression();
	}

	@Override
	public PExpression getDataExpression() {
		return data.equals("true") ? new ABooleanTrueExpression() : new ABooleanFalseExpression();
	}
}
