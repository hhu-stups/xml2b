package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.*;

import java.time.Duration;
import java.util.Collections;

import static de.hhu.stups.xml2b.translation.ASTUtils.createIdentifier;

public class BRealAttributeType extends BAttributeType {
	private final boolean isDuration;

	public BRealAttributeType(final String elementType, final String attributeName) {
		super(elementType, attributeName);
		this.isDuration = false;
		this.typeString = "REAL";
	}

	public BRealAttributeType(final String elementType, final String attributeName, final boolean isDuration) {
		super(elementType, attributeName);
		this.isDuration = isDuration;
		this.typeString = "REAL";
	}

	@Override
	public String toString() {
		return this.typeString + "(" + identifier + ")";
	}

	@Override
	public PExpression getSetExpression() {
		return new ARealSetExpression();
	}

	@Override
	public PExpression getDataExpression(String data) {
		if (isDuration)
			data = Double.toString((double) Duration.parse(data).withNanos(0).toMillis());
		PExpression dataExpression = new ARealExpression(new TRealLiteral(String.valueOf(Double.parseDouble(data))));
		return new AFunctionExpression(createIdentifier(identifier), Collections.singletonList(dataExpression));
	}
}
