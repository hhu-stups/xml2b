package de.hhu.stups.xml2b.readXsd;

import de.hhu.stups.xml2b.bTypes.*;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

public class TypeUtils {

	private static final Map<String, String> xsdTypesToJava = new HashMap<>();
	// source of mapping:
	//   https://github.com/xmlet/XsdParser
	//   https://www.w3schools.com/XML/schema_elements_ref.asp
	static {
		String string = "String";
		String localDateTime = "LocalDateTime";
		String duration = "Duration";
		String bigInteger = "BigInteger";
		String integer = "Integer";
		String shortString = "Short";
		String qName = "QName";
		String longString = "Long";
		String byteString = "Byte";

		// String types
		xsdTypesToJava.put("xsd:ENTITIES", string);
		xsdTypesToJava.put("xs:ENTITIES", string);
		xsdTypesToJava.put("ENTITIES", string);
		xsdTypesToJava.put("xsd:ENTITY", string);
		xsdTypesToJava.put("xs:ENTITY", string);
		xsdTypesToJava.put("ENTITY", string);
		xsdTypesToJava.put("xsd:ID", string);
		xsdTypesToJava.put("xs:ID", string);
		xsdTypesToJava.put("ID", string);
		xsdTypesToJava.put("xsd:IDREF", string);
		xsdTypesToJava.put("xs:IDREF", string);
		xsdTypesToJava.put("IDREF", string);
		xsdTypesToJava.put("xsd:IDREFS", string);
		xsdTypesToJava.put("xs:IDREFS", string);
		xsdTypesToJava.put("IDREFS", string);
		xsdTypesToJava.put("xsd:language", string);
		xsdTypesToJava.put("xs:language", string);
		xsdTypesToJava.put("language", string);
		xsdTypesToJava.put("xsd:Name", string);
		xsdTypesToJava.put("xs:Name", string);
		xsdTypesToJava.put("Name", string);
		xsdTypesToJava.put("xsd:NCName", string);
		xsdTypesToJava.put("xs:NCName", string);
		xsdTypesToJava.put("NCName", string);
		xsdTypesToJava.put("xsd:NMTOKEN", string);
		xsdTypesToJava.put("xs:NMTOKEN", string);
		xsdTypesToJava.put("NMTOKEN", string);
		xsdTypesToJava.put("xsd:NMTOKENS", string);
		xsdTypesToJava.put("xs:NMTOKENS", string);
		xsdTypesToJava.put("NMTOKENS", string);
		xsdTypesToJava.put("xsd:normalizedString", string);
		xsdTypesToJava.put("xs:normalizedString", string);
		xsdTypesToJava.put("normalizedString", string);
		xsdTypesToJava.put("xsd:string", string);
		xsdTypesToJava.put("xs:string", string);
		xsdTypesToJava.put("string", string);
		xsdTypesToJava.put("xsd:token", string);
		xsdTypesToJava.put("xs:token", string);
		xsdTypesToJava.put("token", string);

		// Date and time types
		xsdTypesToJava.put("xsd:date", localDateTime);
		xsdTypesToJava.put("xs:date", localDateTime);
		xsdTypesToJava.put("date", localDateTime);
		xsdTypesToJava.put("xsd:dateTime", localDateTime);
		xsdTypesToJava.put("xs:dateTime", localDateTime);
		xsdTypesToJava.put("dateTime", localDateTime);
		xsdTypesToJava.put("xsd:localDateTime", localDateTime);
		xsdTypesToJava.put("xs:localDateTime", localDateTime);
		xsdTypesToJava.put("localDateTime", localDateTime);
		xsdTypesToJava.put("xsd:duration", duration);
		xsdTypesToJava.put("xs:duration", duration);
		xsdTypesToJava.put("duration", duration);
		xsdTypesToJava.put("xsd:dayTimeDuration", duration);
		xsdTypesToJava.put("xs:dayTimeDuration", duration);
		xsdTypesToJava.put("dayTimeDuration", duration);
		xsdTypesToJava.put("xsd:yearMonthDuration", duration);
		xsdTypesToJava.put("xs:yearMonthDuration", duration);
		xsdTypesToJava.put("yearMonthDuration", duration);
		xsdTypesToJava.put("xsd:gDay", localDateTime);
		xsdTypesToJava.put("xs:gDay", localDateTime);
		xsdTypesToJava.put("gDay", localDateTime);
		xsdTypesToJava.put("xsd:gMonth", localDateTime);
		xsdTypesToJava.put("xs:gMonth", localDateTime);
		xsdTypesToJava.put("gMonth", localDateTime);
		xsdTypesToJava.put("xsd:gMonthDay", localDateTime);
		xsdTypesToJava.put("xs:gMonthDay", localDateTime);
		xsdTypesToJava.put("gMonthDay", localDateTime);
		xsdTypesToJava.put("xsd:gYear", localDateTime);
		xsdTypesToJava.put("xs:gYear", localDateTime);
		xsdTypesToJava.put("gYear", localDateTime);
		xsdTypesToJava.put("xsd:gYearMonth", localDateTime);
		xsdTypesToJava.put("xs:gYearMonth", localDateTime);
		xsdTypesToJava.put("gYearMonth", localDateTime);
		xsdTypesToJava.put("xsd:time", localDateTime);
		xsdTypesToJava.put("xs:time", localDateTime);
		xsdTypesToJava.put("time", localDateTime);

		// Numeric types
		xsdTypesToJava.put("xsd:byte", byteString);
		xsdTypesToJava.put("xs:byte", byteString);
		xsdTypesToJava.put("byte", byteString);
		xsdTypesToJava.put("xsd:decimal", "BigDecimal");
		xsdTypesToJava.put("xs:decimal", "BigDecimal");
		xsdTypesToJava.put("decimal", "BigDecimal");
		xsdTypesToJava.put("xsd:int", integer);
		xsdTypesToJava.put("xs:int", integer);
		xsdTypesToJava.put("int", integer);
		xsdTypesToJava.put("xsd:integer", bigInteger);
		xsdTypesToJava.put("xs:integer", bigInteger);
		xsdTypesToJava.put("integer", bigInteger);
		xsdTypesToJava.put("xsd:long", longString);
		xsdTypesToJava.put("xs:long", longString);
		xsdTypesToJava.put("long", longString);
		xsdTypesToJava.put("xsd:negativeInteger", bigInteger);
		xsdTypesToJava.put("xs:negativeInteger", bigInteger);
		xsdTypesToJava.put("negativeInteger", bigInteger);
		xsdTypesToJava.put("xsd:nonNegativeInteger", bigInteger);
		xsdTypesToJava.put("xs:nonNegativeInteger", bigInteger);
		xsdTypesToJava.put("nonNegativeInteger", bigInteger);
		xsdTypesToJava.put("xsd:nonPositiveInteger", bigInteger);
		xsdTypesToJava.put("xs:nonPositiveInteger", bigInteger);
		xsdTypesToJava.put("nonPositiveInteger", bigInteger);
		xsdTypesToJava.put("xsd:positiveInteger", bigInteger);
		xsdTypesToJava.put("xs:positiveInteger", bigInteger);
		xsdTypesToJava.put("positiveInteger", bigInteger);
		xsdTypesToJava.put("xsd:short", shortString);
		xsdTypesToJava.put("xs:short", shortString);
		xsdTypesToJava.put("short", shortString);
		xsdTypesToJava.put("xsd:unsignedLong", bigInteger);
		xsdTypesToJava.put("xs:unsignedLong", bigInteger);
		xsdTypesToJava.put("unsignedLong", bigInteger);
		xsdTypesToJava.put("xsd:unsignedInt", longString);
		xsdTypesToJava.put("xs:unsignedInt", longString);
		xsdTypesToJava.put("unsignedInt", longString);
		xsdTypesToJava.put("xsd:unsignedShort", integer);
		xsdTypesToJava.put("xs:unsignedShort", integer);
		xsdTypesToJava.put("unsignedShort", integer);
		xsdTypesToJava.put("xsd:unsignedByte", shortString);
		xsdTypesToJava.put("xs:unsignedByte", shortString);
		xsdTypesToJava.put("unsignedByte", shortString);

		// Misc. types
		xsdTypesToJava.put("xsd:anyURI", string);
		xsdTypesToJava.put("xs:anyURI", string);
		xsdTypesToJava.put("anyURI", string);
		xsdTypesToJava.put("xsd:base64Binary", string);
		xsdTypesToJava.put("xs:base64Binary", string);
		xsdTypesToJava.put("base64Binary", string);
		xsdTypesToJava.put("xsd:boolean", "Boolean");
		xsdTypesToJava.put("xs:boolean", "Boolean");
		xsdTypesToJava.put("boolean", "Boolean");
		xsdTypesToJava.put("xsd:double", "Double");
		xsdTypesToJava.put("xs:double", "Double");
		xsdTypesToJava.put("double", "Double");
		xsdTypesToJava.put("xsd:float", "Float");
		xsdTypesToJava.put("xs:float", "Float");
		xsdTypesToJava.put("float", "Float");
		xsdTypesToJava.put("xsd:hexBinary", string);
		xsdTypesToJava.put("xs:hexBinary", string);
		xsdTypesToJava.put("hexBinary", string);
		xsdTypesToJava.put("xsd:NOTATION", qName);
		xsdTypesToJava.put("xs:NOTATION", qName);
		xsdTypesToJava.put("NOTATION", qName);
		xsdTypesToJava.put("xsd:QName", qName);
		xsdTypesToJava.put("xs:QName", qName);
		xsdTypesToJava.put("QName", qName);
		xsdTypesToJava.put("xsd:untypedAtomic", string);
		xsdTypesToJava.put("xs:untypedAtomic", string);
		xsdTypesToJava.put("untypedAtomic", string);
	}

	public static boolean isConvertibleType(final QName qName) {
		return xsdTypesToJava.containsKey(qNameToString(qName));
	}

	public static BAttributeType getBAttributeType(QName xsdTypeQ, String attributeName) {
		switch (getJavaType(xsdTypeQ)) {
			case "BigDecimal":
			case "Double":
			case "Float":
				return new BRealAttributeType(attributeName);
			case "BigInteger":
			case "Integer":
			case "Short":
			case "Long":
				return new BIntegerAttributeType(attributeName);
			case "Duration":
				return new BRealAttributeType(attributeName, true);
			case "Boolean":
				return new BBoolAttributeType(attributeName);
			default:
				return new BStringAttributeType(attributeName);
		}
	}

	public static String getJavaType(QName xsdType) {
		return xsdTypesToJava.getOrDefault(qNameToString(xsdType), "String");
	}

	public static String qNameToString(QName qName) {
		String xsdType = "";
		if (qName != null) {
			if (!qName.getPrefix().isEmpty()) {
				xsdType += qName.getPrefix() + ":";
			}
			xsdType += qName.getLocalPart();
		}
		return xsdType;
	}
}
