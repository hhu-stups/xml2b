package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.AIdentifierExpression;
import de.be4.classicalb.core.parser.node.PExpression;
import de.hhu.stups.xml2b.translation.ASTUtils;

import java.util.Set;

public class BEnumSetAttribute extends BAttribute {
	private static final String SUFFIX_FOR_SET = "_values";
	private final String prefix;
	private final AIdentifierExpression identifier;
	private final Set<String> enum_values;

	public BEnumSetAttribute(String identifier, Set<String> values, String data) {
		super(data);
		this.prefix = identifier + "_";
		this.identifier = ASTUtils.createIdentifier(identifier + SUFFIX_FOR_SET);
		this.enum_values = values;
	}
	public AIdentifierExpression getIdentifier() {
		return identifier.clone();
	}
	public Set<String> getEnumValues() {
		return enum_values;
	}

	@Override
	public String toString() {
		return "ENUM_SET(" + data + ")";
	}

	@Override
	public PExpression getSetExpression() {
		return this.getIdentifier();
	}

	@Override
	public PExpression getDataExpression() {
		return ASTUtils.createIdentifier(prefix + data);
	}
}
