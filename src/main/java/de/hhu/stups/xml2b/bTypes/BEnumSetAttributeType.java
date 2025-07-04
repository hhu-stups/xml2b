package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.AFunctionExpression;
import de.be4.classicalb.core.parser.node.PExpression;
import de.hhu.stups.xml2b.readXsd.TypeUtils;

import java.util.Collections;

import static de.be4.classicalb.core.parser.util.ASTBuilder.createIdentifier;

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
		if (enumSet.isAllowBuiltIn()) {
			BAttributeType type = TypeUtils.inferAttributeType(this.getAttributeName(), data);
			return type.getFunctionExpression(data);
		} else if (enumSet.isExtensible()) {
			enumSet.addValue(data);
		} else if (!enumSet.getEnumValues().contains(data)) {
			throw new IllegalArgumentException("enum set does not contain argument");
		}
		PExpression dataExpression = createIdentifier(enumSet.getValueIdentifier(data));
		return new AFunctionExpression(createIdentifier(this.getIdentifier()), Collections.singletonList(dataExpression));
	}
}
