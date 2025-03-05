package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.AIdentifierExpression;

import java.util.Set;

import static de.be4.classicalb.core.parser.util.ASTBuilder.createIdentifier;

public class BEnumSet {
	private final String identifier, prefix;
	private final Set<String> enum_values;
	// enum set is extensible e.g. for combinations of fixed values and pattern restricted elements;
	// these are added when the data is passed to the BEnumSetAttributeType during AST creation
	private boolean extensible = false;
	private boolean allowBuiltIn = false;

	public BEnumSet(String identifier, Set<String> values) {
		this.prefix = identifier + "_";
		this.identifier = identifier;
		this.enum_values = values;
	}

	public void addValue(String value) {
		enum_values.add(value);
	}

	public void addValues(Set<String> values) {
		enum_values.addAll(values);
	}

	public String getPrefix() {
		return prefix;
	}

	public String getIdentifier() {
		return this.identifier;
	}

	public String getValueIdentifier(final String data) {
		return this.prefix + data;
	}

	public AIdentifierExpression getIdentifierExpression() {
		return createIdentifier(identifier);
	}

	public Set<String> getEnumValues() {
		return enum_values;
	}

	public void setExtensible() {
		this.extensible = true;
	}

	public void setAllowBuiltIn() {
		this.allowBuiltIn = true;
	}

	public boolean isExtensible() {
		return extensible;
	}

	public boolean isAllowBuiltIn() {
		return allowBuiltIn;
	}

	@Override
	public String toString() {
		return "ENUM_SET(" + identifier + "; extensible:" + extensible + "; allowBuiltIn:" + allowBuiltIn + "; " + enum_values + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BEnumSet) {
			BEnumSet enumSet = (BEnumSet) obj;
			return this.enum_values.containsAll(enumSet.enum_values)
					&& this.identifier.equals(enumSet.identifier);
		} else {
			return false;
		}
	}
}
