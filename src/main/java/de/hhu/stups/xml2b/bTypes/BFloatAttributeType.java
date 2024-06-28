package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.*;

import java.util.Collections;

import static de.hhu.stups.xml2b.translation.ASTUtils.createIdentifier;

public class BFloatAttributeType extends BAttributeType {

	public BFloatAttributeType(final String attributeName) {
		super(attributeName, "FLOAT");
	}

	@Override
	public String toString() {
		return this.getIdentifier() + "(" + this.getTypeString() + ")";
	}

	@Override
	public PExpression getSetExpression() {
		return new AFloatSetExpression();
	}

	@Override
	public PExpression getFunctionExpression(String data) {
		PExpression dataExpression = new ARealExpression(new TRealLiteral(String.valueOf(Float.parseFloat(data))));
		return new AFunctionExpression(createIdentifier(this.getIdentifier()), Collections.singletonList(dataExpression));
	}
}
