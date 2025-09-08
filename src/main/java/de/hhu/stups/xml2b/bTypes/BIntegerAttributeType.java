package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.AIntegerSetExpression;
import de.be4.classicalb.core.parser.node.PExpression;

import static de.be4.classicalb.core.parser.util.ASTBuilder.createIntegerExpression;

public class BIntegerAttributeType extends BAttributeType {
	public BIntegerAttributeType(final String attributeName) {
		super(attributeName, "INTEGER");
	}

	@Override
	public String toString() {
		return this.getIdentifier() + "(" + this.getTypeString() + ")";
	}

	@Override
	public PExpression getSetExpression() {
		return new AIntegerSetExpression();
	}

	@Override
	public PExpression getRawExpression(String data) {
		return createIntegerExpression(data);
	}
}
