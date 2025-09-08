package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.ARealSetExpression;
import de.be4.classicalb.core.parser.node.PExpression;

import java.time.Duration;

import static de.be4.classicalb.core.parser.util.ASTBuilder.createRealExpression;

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
	public PExpression getRawExpression(String data) {
		if (isDuration)
			data = Double.toString((double) Duration.parse(data).withNanos(0).toMillis());
		return createRealExpression(String.valueOf(Double.parseDouble(data)));
	}
}
