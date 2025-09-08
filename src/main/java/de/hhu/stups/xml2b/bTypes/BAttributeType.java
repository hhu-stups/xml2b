package de.hhu.stups.xml2b.bTypes;

import com.sun.xml.xsom.XmlString;
import de.be4.classicalb.core.parser.node.ACoupleExpression;
import de.be4.classicalb.core.parser.node.PExpression;

import java.util.Arrays;

import static de.be4.classicalb.core.parser.util.ASTBuilder.createStringExpression;

public abstract class BAttributeType {
	private static final String PREFIX = "Xml"; // match LibraryJSON style

	private final String attributeName, identifier, typeString;
	private String defaultValue, fixedValue;
	private final boolean isContent;
	private boolean isRequired = false;

	public BAttributeType(final String attributeName, final String typeString) {
		this.attributeName = attributeName;
		this.isContent = attributeName == null;
		this.typeString = typeString;
		if (this instanceof BEnumSetAttributeType) {
			this.identifier = PREFIX + this.typeString; // don't change name of enum
		} else { // B types like Real
			this.identifier = PREFIX + this.typeString.charAt(0) + this.typeString.substring(1).toLowerCase();
		}
	}

	public void withDefaultValue(final XmlString defaultValue) {
		if (defaultValue != null) {
			if (fixedValue != null) {
				throw new IllegalStateException("default value cannot be set when fixed value is set");
			}
			this.defaultValue = defaultValue.toString();
		}
	}

	public void withFixedValue(final XmlString fixedValue) {
		if (fixedValue != null) {
			if (defaultValue != null) {
				throw new IllegalStateException("fixed value cannot be set when default value is set");
			}
			this.fixedValue = fixedValue.toString();
		}
	}

	public void withIsRequired(final boolean isRequired) {
		this.isRequired = isRequired;
	}

	public String getDefaultOrFixedValue() {
		if (this.defaultValue != null) { // only one of them can be set
			return this.defaultValue;
		} else {
			return this.fixedValue;
		}
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

	public boolean isRequired() {
		return this.isRequired;
	}

	public BStringAttributeType getStringAttributeType() {
		return new BStringAttributeType(this.attributeName);
	}
}
