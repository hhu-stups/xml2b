package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.PExpression;

public abstract class BAttribute {
	protected final String data;

	public BAttribute(String data) {
		this.data = data;
	}

	abstract public PExpression getSetExpression();
	abstract public PExpression getDataExpression();
}
