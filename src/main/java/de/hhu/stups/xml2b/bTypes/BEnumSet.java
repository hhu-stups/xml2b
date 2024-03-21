package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.AIdentifierExpression;
import de.hhu.stups.xml2b.translation.ASTUtils;

import java.util.Set;

public class BEnumSet {
	private final String prefix;
	private final AIdentifierExpression identifier;
	private final Set<String> enum_values;

	public BEnumSet(String identifier, Set<String> values) {
		this.prefix = identifier + "_";
		this.identifier = ASTUtils.createIdentifier(identifier);
		this.enum_values = values;
	}

	public void addValues(Set<String> values) {
		enum_values.addAll(values);
	}

	public String getPrefix() {
		return prefix;
	}
	public AIdentifierExpression getIdentifier() {
		return identifier.clone();
	}
	public Set<String> getEnumValues() {
		return enum_values;
	}

	@Override
	public String toString() {
		return "ENUM_SET(" + enum_values + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BEnumSet) {
			BEnumSet enumSet = (BEnumSet) obj;
			return this.enum_values.containsAll(enumSet.enum_values)
					&& this.identifier.toString().equals(enumSet.identifier.toString());
		} else {
			return false;
		}
	}
}
