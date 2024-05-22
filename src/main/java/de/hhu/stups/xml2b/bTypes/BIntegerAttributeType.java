package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.*;

import java.util.Collections;

import static de.hhu.stups.xml2b.translation.ASTUtils.createIdentifier;
import static de.hhu.stups.xml2b.translation.ASTUtils.createInteger;

public class BIntegerAttributeType extends BAttributeType {
	public BIntegerAttributeType(final String attributeName) {
		super(attributeName, "INTEGER");
	}

	@Override
	public String toString() {
		return this.getTypeString() + "(" + this.getIdentifier() + ")";
	}

	@Override
	public PExpression getSetExpression() {
		return new AIntegerSetExpression();
	}

	@Override
	public PExpression getFunctionExpression(String data) {
		return new AFunctionExpression(createIdentifier(this.getIdentifier()), Collections.singletonList(createInteger(data)));
	}
}
