package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.AStringSetExpression;
import de.be4.classicalb.core.parser.node.PExpression;
import de.hhu.stups.xml2b.readXsd.TypeUtils;

import static de.be4.classicalb.core.parser.util.ASTBuilder.createIdentifier;
import static de.be4.classicalb.core.parser.util.ASTBuilder.createStringExpression;

public class BEnumSetAttributeType extends BAttributeType {
	private final BEnumSet enumSet;
	private boolean requiresClassicalType = false;

	public BEnumSetAttributeType(final String attributeName, final BEnumSet enumSet) {
		super(attributeName, enumSet.getIdentifier());
		this.enumSet = enumSet;
	}

	/**
	 * Set to true, if raw expression is not used inside a free type and requires a classical B type.
	 * Then, all expression for this attribute will be generated as a STRING to ensure correct typing.
	 * Only effective if enumSet.isAllowBuiltIn is true, i.e. it also allows default types.
	 */
	public void setRequiresClassicalType(final boolean requiresClassicalType) {
		this.requiresClassicalType = requiresClassicalType;
	}

	@Override
	public String toString() {
		return this.getIdentifier() + "(" + enumSet.toString() + ")";
	}

	@Override
	public PExpression getSetExpression() {
		if (enumSet.isAllowBuiltIn() && this.requiresClassicalType) {
			return new AStringSetExpression();
		}
		return enumSet.getIdentifierExpression();
	}

	@Override
	public PExpression getRawExpression(String data) {
		if (enumSet.isAllowBuiltIn()) {
			if (this.requiresClassicalType)
				return createStringExpression(data);
			BAttributeType type = TypeUtils.inferAttributeType(this.getAttributeName(), data);
			return type.getRawExpression(data);
		} else if (enumSet.isExtensible()) {
			enumSet.addValue(data);
		} else if (!enumSet.getEnumValues().contains(data)) {
			throw new IllegalArgumentException("enum set does not contain argument");
		}
		return createIdentifier(enumSet.getValueIdentifier(data));
	}
}
