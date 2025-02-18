package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.ACoupleExpression;
import de.be4.classicalb.core.parser.node.PExpression;

import java.util.Arrays;

import static de.be4.classicalb.core.parser.util.ASTBuilder.createStringExpression;

public abstract class BAttributeType {
	protected static final String SUFFIX = "__VALUE";

	private final String attributeName, identifier, typeString;
	private final boolean isContent;

	public BAttributeType(final String attributeName, final String typeString) {
		this.attributeName = attributeName;
		this.isContent = attributeName == null;
		this.typeString = typeString;
		this.identifier = typeString + SUFFIX;
	}

	abstract public PExpression getSetExpression();
	abstract protected PExpression getFunctionExpression(final String data);

	public PExpression getDataExpression(final String data) {
		PExpression functionExpression = this.getFunctionExpression(data);
		if (this.isContent)
			return functionExpression;
		return new ACoupleExpression(Arrays.asList(createStringExpression(this.getAttributeName()), functionExpression));
	}

	public String getAttributeName() {
		return this.attributeName;
	}

	public String getTypeString() {
		return this.typeString;
	}

	public String getIdentifier() {
		return this.identifier;
	}

	public BStringAttributeType getStringAttributeType() {
		return new BStringAttributeType(this.attributeName);
	}
}
