package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.ARealSetExpression;
import de.be4.classicalb.core.parser.node.PExpression;
import de.hhu.stups.xml2b.readXsd.TypeUtils;

import java.math.BigDecimal;
import java.time.Duration;

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
		if (isDuration) {
			Duration duration = Duration.parse(data);
			data = BigDecimal.valueOf(duration.toNanos())
					.movePointLeft(6)
					.stripTrailingZeros()
					.toPlainString();
		}
		return TypeUtils.parseFiniteDouble(data);
	}
}
