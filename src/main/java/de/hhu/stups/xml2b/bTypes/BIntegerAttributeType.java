package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.*;

import java.util.Collections;

import static de.be4.classicalb.core.parser.util.ASTBuilder.createIdentifier;
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
	public PExpression getFunctionExpression(String data) {
		return new AFunctionExpression(createIdentifier(this.getIdentifier()), Collections.singletonList(createIntegerExpression(data)));
	}
}
