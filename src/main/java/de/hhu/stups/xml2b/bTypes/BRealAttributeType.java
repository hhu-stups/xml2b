package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.*;

import java.time.Duration;
import java.util.Collections;

import static de.hhu.stups.xml2b.translation.ASTUtils.createIdentifier;

public class BRealAttributeType extends BAttributeType {
	private final boolean isDuration;

	public BRealAttributeType(final String attributeName) {
		super(attributeName, "REAL");
		this.isDuration = false;
	}

	public BRealAttributeType(final String attributeName, final boolean isDuration) {
		super(attributeName, "REAL");
		this.isDuration = isDuration;
	}

	@Override
	public String toString() {
		return this.getIdentifier() + "(" + this.getTypeString() + ")";
	}

	@Override
	public PExpression getSetExpression() {
		return new ARealSetExpression();
	}

	@Override
	public PExpression getFunctionExpression(String data) {
		if (isDuration)
			data = Double.toString((double) Duration.parse(data).withNanos(0).toMillis());
		PExpression dataExpression = new ARealExpression(new TRealLiteral(String.valueOf(Double.parseDouble(data))));

		return new AFunctionExpression(createIdentifier(this.getIdentifier()), Collections.singletonList(dataExpression));
	}
}
