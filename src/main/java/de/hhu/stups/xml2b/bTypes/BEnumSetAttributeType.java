package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.AFunctionExpression;
import de.be4.classicalb.core.parser.node.PExpression;
import de.hhu.stups.xml2b.translation.ASTUtils;

import java.util.Collections;

import static de.hhu.stups.xml2b.translation.ASTUtils.createIdentifier;

public class BEnumSetAttributeType extends BAttributeType {
	private final BEnumSet enumSet;

	public BEnumSetAttributeType(final String elementType, final String attributeName, final BEnumSet enumSet) {
		super(elementType, attributeName);
		this.enumSet = enumSet;
	}

	@Override
	public String toString() {
		return "ENUM_SET(" + identifier + "[" + enumSet.toString() + "])";
	}

	@Override
	public PExpression getSetExpression() {
		return enumSet.getIdentifier();
	}

	@Override
	public PExpression getDataExpression(String data) {
		String enumData = enumSet.getPrefix() + data;
		if (enumSet.getEnumValues().contains(enumData)) {
			PExpression dataExpression = ASTUtils.createIdentifier(enumData);
			return new AFunctionExpression(createIdentifier(identifier), Collections.singletonList(dataExpression));
		} else {
			throw new IllegalArgumentException("enum set does not contain argument");
		}
	}
}
