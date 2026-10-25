package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.AFloatSetExpression;
import de.be4.classicalb.core.parser.node.PExpression;
import de.hhu.stups.xml2b.readXsd.TypeUtils;

public class BFloatAttributeType extends BAttributeType {

	public BFloatAttributeType(final String attributeName) {
		super(attributeName, "FLOAT");
	}

	@Override
	public String toString() {
		return this.getIdentifier() + "(" + this.getTypeString() + ")";
	}

	@Override
	public PExpression getSetExpression() {
		return new AFloatSetExpression();
	}

	@Override
	public PExpression getRawExpression(String data) {
		return TypeUtils.parseFiniteDouble(data);
	}
}
