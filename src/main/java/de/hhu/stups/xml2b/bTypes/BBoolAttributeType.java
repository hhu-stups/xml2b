package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.*;

import java.util.Collections;

import static de.be4.classicalb.core.parser.util.ASTBuilder.createIdentifier;

public class BBoolAttributeType extends BAttributeType {
	public BBoolAttributeType(final String attributeName) {
		super(attributeName, "BOOL");
	}

	@Override
	public String toString() {
		return this.getIdentifier() + "(" + this.getTypeString() + ")";
	}

	@Override
	public PExpression getSetExpression() {
		return new ABoolSetExpression();
	}

	@Override
	public PExpression getFunctionExpression(String data) {
		PExpression dataExpression = data.equals("true") ? new ABooleanTrueExpression() : new ABooleanFalseExpression();
		return new AFunctionExpression(createIdentifier(this.getIdentifier()), Collections.singletonList(dataExpression));
	}
}
