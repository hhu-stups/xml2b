package de.hhu.stups.xml2b.bTypes;

import de.be4.classicalb.core.parser.node.ABoolSetExpression;
import de.be4.classicalb.core.parser.node.ABooleanFalseExpression;
import de.be4.classicalb.core.parser.node.ABooleanTrueExpression;
import de.be4.classicalb.core.parser.node.PExpression;

public class BBoolAttributeType extends BAttributeType {
	public BBoolAttributeType(final String attributeName) {
		super(attributeName, "BOOL");
	}

	@Override
	public String toString() {
		return this.getIdentifier() + "(" + this.getTypeString() + ")";
	}

	@Override
	public PExpression getSetExpression() {
		return new ABoolSetExpression();
	}

	@Override
	public PExpression getRawExpression(String data) {
		return switch (data) {
			case "true", "1" -> new ABooleanTrueExpression();
			case "false", "0" -> new ABooleanFalseExpression();
			default -> throw new IllegalArgumentException("is not a valid xs:boolean value: " + data); // should not happen after schema validation!
		};
	}
}
