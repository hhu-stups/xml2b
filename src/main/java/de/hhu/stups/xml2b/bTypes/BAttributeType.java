package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.PExpression;

import static de.hhu.stups.xml2b.translation.Translator.ID_NAME;

public abstract class BAttributeType {
	protected final String elementType, attributeName, identifier;

	public BAttributeType(final String elementType, final String attributeName) {
		this.elementType = elementType;
		this.attributeName = attributeName;
		// id attribute is XML standard and should not be considered individually for each element type
		this.identifier = attributeName.equals(ID_NAME) ? attributeName : elementType + ":" + attributeName;
	}

	abstract public PExpression getSetExpression();
	abstract public PExpression getDataExpression(String data);

	public String getElementType() {
		return this.elementType;
	}

	public String getAttributeName() {
		return this.attributeName;
	}

	public String getIdentifier() {
		return this.identifier;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof BAttributeType && this.identifier.equals(((BAttributeType) obj).identifier);
	}
}
