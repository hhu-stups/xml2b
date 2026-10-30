package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.AStringSetExpression;
import de.be4.classicalb.core.parser.node.PExpression;

import static de.be4.classicalb.core.parser.util.ASTBuilder.createIdentifier;
import static de.be4.classicalb.core.parser.util.ASTBuilder.createStringExpression;

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
		if (enumSet.isAllowBuiltIn()) {
			return new AStringSetExpression();
		}
		return enumSet.getIdentifierExpression();
	}

	@Override
	public PExpression getRawExpression(String rawData) {
		if (enumSet.isAllowBuiltIn()) {
			return createStringExpression(rawData);
		}

		String data = rawData.replace(".","~"); // ensure no dots in B identifiers (see check in XSDReader:collectEnumValuesFromFacets)
		if (enumSet.isExtensible()) {
			enumSet.addValue(data);
		} else if (!enumSet.getEnumValues().contains(data)) {
			throw new IllegalArgumentException("enum set does not contain argument " + data);
		}
		return createIdentifier(enumSet.getValueIdentifier(data));
	}
}
