package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.PExpression;

public class BAttribute {
	private final BAttributeType type;
	private final PExpression value;

	public BAttribute(final BAttributeType type, final PExpression value) {
		this.type = type;
		this.value = value;
	}

	public BAttributeType getType() {
		return this.type;
	}

	public PExpression getValue() {
		return this.value;
	}
}
