package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.*;

import java.time.Duration;

public class BAttributeType {

	public enum BType {
		BOOL, ENUM_SET, INTEGER, REAL, STRING
	}

	private final String elementType, attributeName, identifier;
	private final BType bType;
	private final BEnumSet bEnumSet;
	private final boolean isDuration;

	public BAttributeType(final String elementType, final String attributeName) {
		this(elementType, attributeName, BType.STRING, null, false);
	}

	public BAttributeType(final String elementType, final String attributeName, final BType bType) {
		this(elementType, attributeName, bType, null, false);
		if (bType == BType.ENUM_SET) {
			throw new IllegalArgumentException("BType ENUM_SET without BEnumSet");
		}
	}

	public BAttributeType(final String elementType, final String attributeName, final BEnumSet bEnumSet) {
		this(elementType, attributeName, BType.ENUM_SET, bEnumSet, false);
	}

	public BAttributeType(final String elementType, final String attributeName, final boolean isDuration) {
		this(elementType, attributeName, BType.REAL, null, isDuration);
	}

	private BAttributeType(final String elementType, final String attributeName, final BType bType, final BEnumSet bEnumSet, final boolean isDuration) {
		this.elementType = elementType;
		this.attributeName = attributeName;
		this.identifier = elementType + ":" + attributeName;
		this.bType = bType;
		this.bEnumSet = bEnumSet;
		this.isDuration = isDuration;
	}

	public BAttribute getBAttribute(String value) {
		if (bType == BType.BOOL) {
			return new BBoolAttribute(value);
		} else if (bType == BType.ENUM_SET) {
			return new BEnumSetAttribute(bEnumSet, value);
		} else if (bType == BType.INTEGER) {
			return new BIntegerAttribute(value);
		} else if (bType == BType.REAL) {
			if (isDuration)
				value = Double.toString((double) Duration.parse(value).withNanos(0).toMillis());
			return new BRealAttribute(ensureRealHasDot(value));
		} else {
			return new BStringAttribute(value);
		}
	}

	public String getElementType() {
		return this.elementType;
	}

	public String getAttributeName() {
		return this.attributeName;
	}

	public String getIdentifier() {
		return this.identifier;
	}

	// TODO: split again in single classes
	public PExpression getSetExpression() {
		if (bType == BType.BOOL) {
			return new ABoolSetExpression();
		} else if (bType == BType.ENUM_SET) {
			return bEnumSet.getIdentifier();
		} else if (bType == BType.INTEGER) {
			return new AIntegerSetExpression();
		} else if (bType == BType.REAL) {
			return new ARealSetExpression();
		} else {
			return new AStringSetExpression();
		}
	}

	private static String ensureRealHasDot(String value) {
		// stricter checks should be done by validation in previous steps (string is assumed to be a number)
		if (value.contains(".")) {
			return value;
		} else {
			return value + ".0";
		}
	}

	@Override
	public String toString() {
		return "[" + identifier + "," + bType + "," + bEnumSet + "," + isDuration + "]";
	}
}
