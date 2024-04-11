package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.*;

import java.util.Collections;

import static de.hhu.stups.xml2b.translation.ASTUtils.createIdentifier;

public class BIntegerAttributeType extends BAttributeType {
	public BIntegerAttributeType(final String elementType, final String attributeName) {
		super(elementType, attributeName);
	}

	@Override
	public String toString() {
		return "INTEGER(" + identifier + ")";
	}

	@Override
	public PExpression getSetExpression() {
		return new AIntegerSetExpression();
	}

	@Override
	public PExpression getDataExpression(String data) {
		PExpression dataExpression = new AIntegerExpression(new TIntegerLiteral(data));
		return new AFunctionExpression(createIdentifier(identifier), Collections.singletonList(dataExpression));
	}
}
