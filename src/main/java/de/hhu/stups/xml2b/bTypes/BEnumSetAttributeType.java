package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.AFunctionExpression;
import de.be4.classicalb.core.parser.node.PExpression;

import java.util.Collections;

import static de.hhu.stups.xml2b.translation.ASTUtils.createIdentifier;

public class BEnumSetAttributeType extends BAttributeType {
	private final BEnumSet enumSet;

	public BEnumSetAttributeType(final String attributeName, final BEnumSet enumSet) {
		super(attributeName, enumSet.getIdentifier());
		this.enumSet = enumSet;
	}

	@Override
	public String toString() {
		return this.getIdentifier() + "(" + enumSet.toString() + ")";
	}

	@Override
	public PExpression getSetExpression() {
		return enumSet.getIdentifierExpression();
	}

	@Override
	public PExpression getFunctionExpression(String data) {
		if (enumSet.isExtensible()) {
			enumSet.addValue(data);
		} else if (!enumSet.getEnumValues().contains(data)) {
			throw new IllegalArgumentException("enum set does not contain argument");
		}
		PExpression dataExpression = createIdentifier(enumSet.getValueIdentifier(data));
		return new AFunctionExpression(createIdentifier(this.getIdentifier()), Collections.singletonList(dataExpression));
	}
}
