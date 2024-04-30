package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.*;

import java.util.Collections;

import static de.hhu.stups.xml2b.translation.ASTUtils.createIdentifier;

public class BStringAttributeType extends BAttributeType {
	public BStringAttributeType(final String elementType, final String attributeName) {
		super(elementType, attributeName);
		this.typeString = "STRING";
	}

	@Override
	public String toString() {
		return this.typeString + "(" + identifier + ")";
	}

	@Override
	public PExpression getSetExpression() {
		return new AStringSetExpression();
	}

	@Override
	public PExpression getDataExpression(String data) {
		PExpression dataExpression = new AStringExpression(new TStringLiteral(data));
		return new AFunctionExpression(createIdentifier(identifier), Collections.singletonList(dataExpression));
	}
}
