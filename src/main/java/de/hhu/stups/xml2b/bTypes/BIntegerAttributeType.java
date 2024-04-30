package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.*;

import java.util.Collections;

import static de.hhu.stups.xml2b.translation.ASTUtils.createIdentifier;
import static de.hhu.stups.xml2b.translation.ASTUtils.createInteger;

public class BIntegerAttributeType extends BAttributeType {
	public BIntegerAttributeType(final String elementType, final String attributeName) {
		super(elementType, attributeName);
		this.typeString = "INTEGER";
	}

	@Override
	public String toString() {
		return this.typeString + "(" + identifier + ")";
	}

	@Override
	public PExpression getSetExpression() {
		return new AIntegerSetExpression();
	}

	@Override
	public PExpression getDataExpression(String data) {
		PExpression dataExpression = createInteger(data);
		return new AFunctionExpression(createIdentifier(identifier), Collections.singletonList(dataExpression));
	}
}
