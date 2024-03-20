package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.PExpression;
import de.hhu.stups.xml2b.translation.ASTUtils;

public class BEnumSetAttribute extends BAttribute {
	private final BEnumSet enumSet;

	public BEnumSetAttribute(BEnumSet enumSet, String data) {
		super(data);
		this.enumSet = enumSet;
	}

	@Override
	public String toString() {
		return "ENUM_SET(" + data + ")";
	}

	@Override
	public PExpression getSetExpression() {
		return enumSet.getIdentifier();
	}

	@Override
	public PExpression getDataExpression() {
		return ASTUtils.createIdentifier(enumSet.getPrefix() + data);
	}
}
