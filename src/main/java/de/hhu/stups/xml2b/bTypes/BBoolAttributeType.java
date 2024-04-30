package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.*;

import java.util.Collections;

import static de.hhu.stups.xml2b.translation.ASTUtils.createIdentifier;

public class BBoolAttributeType extends BAttributeType {
	public BBoolAttributeType(final String elementType, final String attributeName) {
		super(elementType, attributeName);
		this.typeString = "BOOL";
	}

	@Override
	public String toString() {
		return typeString + "(" + identifier + ")";
	}

	@Override
	public PExpression getSetExpression() {
		return new ABoolSetExpression();
	}

	@Override
	public PExpression getDataExpression(String data) {
		PExpression dataExpression = data.equals("true") ? new ABooleanTrueExpression() : new ABooleanFalseExpression();
		return new AFunctionExpression(createIdentifier(identifier), Collections.singletonList(dataExpression));
	}
}
