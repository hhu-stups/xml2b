package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.ACoupleExpression;
import de.be4.classicalb.core.parser.node.AStringExpression;
import de.be4.classicalb.core.parser.node.PExpression;
import de.be4.classicalb.core.parser.node.TStringLiteral;

import java.util.ArrayList;
import java.util.Arrays;

import static de.hhu.stups.xml2b.translation.ASTUtils.createString;

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
		return new ACoupleExpression(Arrays.asList(createString(this.getAttributeName()), functionExpression));
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
