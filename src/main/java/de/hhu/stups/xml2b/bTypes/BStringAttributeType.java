package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.*;

import java.util.Collections;

import static de.be4.classicalb.core.parser.util.ASTBuilder.createIdentifier;
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
	public PExpression getFunctionExpression(String data) {
		return new AFunctionExpression(createIdentifier(this.getIdentifier()), Collections.singletonList(createStringExpression(data)));
	}
}
