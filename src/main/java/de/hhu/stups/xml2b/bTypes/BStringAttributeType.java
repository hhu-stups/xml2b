package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.AStringSetExpression;
import de.be4.classicalb.core.parser.node.PExpression;

import static de.be4.classicalb.core.parser.util.ASTBuilder.createStringExpression;

public class BStringAttributeType extends BAttributeType {
	public BStringAttributeType(final String attributeName) {
		super(attributeName, "STRING");
	}

	@Override
	public String toString() {
		return this.getIdentifier() + "(" + this.getTypeString() + ")";
	}

	@Override
	public PExpression getSetExpression() {
		return new AStringSetExpression();
	}

	@Override
	public PExpression getRawExpression(String data) {
		return createStringExpression(data);
	}
}
