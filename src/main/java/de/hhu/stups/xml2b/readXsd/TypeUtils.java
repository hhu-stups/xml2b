package de.hhu.stups.xml2b.readXsd;

import de.hhu.stups.xml2b.bTypes.*;

import javax.xml.namespace.QName;
import java.util.*;

public class TypeUtils {

	private static final Map<String, String> xsdTypesToJava;
	//source of mapping: https://github.com/xmlet/XsdParser
	static {
		xsdTypesToJava = new HashMap<>();
		String string = "String";
		String xmlGregorianCalendar = "XMLGregorianCalendar";
		String duration = "Duration";
		String bigInteger = "BigInteger";
		String integer = "Integer";
		String shortString = "Short";
		String qName = "QName";
		String longString = "Long";
		String byteString = "Byte";
		xsdTypesToJava.put("xsd:anyURI", string);
		xsdTypesToJava.put("xs:anyURI", string);
		xsdTypesToJava.put("anyURI", string);
		xsdTypesToJava.put("xsd:boolean", "Boolean");
		xsdTypesToJava.put("xs:boolean", "Boolean");
		xsdTypesToJava.put("boolean", "Boolean");
		xsdTypesToJava.put("xsd:date", xmlGregorianCalendar);
		xsdTypesToJava.put("xs:date", xmlGregorianCalendar);
		xsdTypesToJava.put("date", xmlGregorianCalendar);
		xsdTypesToJava.put("xsd:dateTime", xmlGregorianCalendar);
		xsdTypesToJava.put("xs:dateTime", xmlGregorianCalendar);
		xsdTypesToJava.put("dateTime", xmlGregorianCalendar);
		xsdTypesToJava.put("xsd:time", xmlGregorianCalendar);
		xsdTypesToJava.put("xs:time", xmlGregorianCalendar);
		xsdTypesToJava.put("time", xmlGregorianCalendar);
		xsdTypesToJava.put("xsd:duration", duration);
		xsdTypesToJava.put("xs:duration", duration);
		xsdTypesToJava.put("duration", duration);
		xsdTypesToJava.put("xsd:dayTimeDuration", duration);
		xsdTypesToJava.put("xs:dayTimeDuration", duration);
		xsdTypesToJava.put("dayTimeDuration", duration);
		xsdTypesToJava.put("xsd:yearMonthDuration", duration);
		xsdTypesToJava.put("xs:yearMonthDuration", duration);
		xsdTypesToJava.put("yearMonthDuration", duration);
		xsdTypesToJava.put("xsd:gDay", xmlGregorianCalendar);
		xsdTypesToJava.put("xs:gDay", xmlGregorianCalendar);
		xsdTypesToJava.put("gDay", xmlGregorianCalendar);
		xsdTypesToJava.put("xsd:gMonth", xmlGregorianCalendar);
		xsdTypesToJava.put("xs:gMonth", xmlGregorianCalendar);
		xsdTypesToJava.put("gMonth", xmlGregorianCalendar);
		xsdTypesToJava.put("xsd:gMonthDay", xmlGregorianCalendar);
		xsdTypesToJava.put("xs:gMonthDay", xmlGregorianCalendar);
		xsdTypesToJava.put("gMonthDay", xmlGregorianCalendar);
		xsdTypesToJava.put("xsd:gYear", xmlGregorianCalendar);
		xsdTypesToJava.put("xs:gYear", xmlGregorianCalendar);
		xsdTypesToJava.put("gYear", xmlGregorianCalendar);
		xsdTypesToJava.put("xsd:gYearMonth", xmlGregorianCalendar);
		xsdTypesToJava.put("xs:gYearMonth", xmlGregorianCalendar);
		xsdTypesToJava.put("gYearMonth", xmlGregorianCalendar);
		xsdTypesToJava.put("xsd:decimal", "BigDecimal");
		xsdTypesToJava.put("xs:decimal", "BigDecimal");
		xsdTypesToJava.put("decimal", "BigDecimal");
		xsdTypesToJava.put("xsd:integer", bigInteger);
		xsdTypesToJava.put("xs:integer", bigInteger);
		xsdTypesToJava.put("integer", bigInteger);
		xsdTypesToJava.put("xsd:nonPositiveInteger", bigInteger);
		xsdTypesToJava.put("xs:nonPositiveInteger", bigInteger);
		xsdTypesToJava.put("nonPositiveInteger", bigInteger);
		xsdTypesToJava.put("xsd:negativeInteger", bigInteger);
		xsdTypesToJava.put("xs:negativeInteger", bigInteger);
		xsdTypesToJava.put("negativeInteger", bigInteger);
		xsdTypesToJava.put("xsd:long", longString);
		xsdTypesToJava.put("xs:long", longString);
		xsdTypesToJava.put("long", longString);
		xsdTypesToJava.put("xsd:int", integer);
		xsdTypesToJava.put("xs:int", integer);
		xsdTypesToJava.put("int", integer);
		xsdTypesToJava.put("xsd:short", shortString);
		xsdTypesToJava.put("xs:short", shortString);
		xsdTypesToJava.put("short", shortString);
		xsdTypesToJava.put("xsd:byte", byteString);
		xsdTypesToJava.put("xs:byte", byteString);
		xsdTypesToJava.put("byte", byteString);
		xsdTypesToJava.put("xsd:nonNegativeInteger", bigInteger);
		xsdTypesToJava.put("xs:nonNegativeInteger", bigInteger);
		xsdTypesToJava.put("nonNegativeInteger", bigInteger);
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
		xsdTypesToJava.put("xsd:positiveInteger", bigInteger);
		xsdTypesToJava.put("xs:positiveInteger", bigInteger);
		xsdTypesToJava.put("positiveInteger", bigInteger);
		xsdTypesToJava.put("xsd:double", "Double");
		xsdTypesToJava.put("xs:double", "Double");
		xsdTypesToJava.put("double", "Double");
		xsdTypesToJava.put("xsd:float", "Float");
		xsdTypesToJava.put("xs:float", "Float");
		xsdTypesToJava.put("float", "Float");
		xsdTypesToJava.put("xsd:QName", qName);
		xsdTypesToJava.put("xs:QName", qName);
		xsdTypesToJava.put("QName", qName);
		xsdTypesToJava.put("xsd:NOTATION", qName);
		xsdTypesToJava.put("xs:NOTATION", qName);
		xsdTypesToJava.put("NOTATION", qName);
		xsdTypesToJava.put("xsd:string", string);
		xsdTypesToJava.put("xs:string", string);
		xsdTypesToJava.put("string", string);
		xsdTypesToJava.put("xsd:normalizedString", string);
		xsdTypesToJava.put("xs:normalizedString", string);
		xsdTypesToJava.put("normalizedString", string);
		xsdTypesToJava.put("xsd:token", string);
		xsdTypesToJava.put("xs:token", string);
		xsdTypesToJava.put("token", string);
		xsdTypesToJava.put("xsd:language", string);
		xsdTypesToJava.put("xs:language", string);
		xsdTypesToJava.put("language", string);
		xsdTypesToJava.put("xsd:NMTOKEN", string);
		xsdTypesToJava.put("xs:NMTOKEN", string);
		xsdTypesToJava.put("NMTOKEN", string);
		xsdTypesToJava.put("xsd:Name", string);
		xsdTypesToJava.put("xs:Name", string);
		xsdTypesToJava.put("Name", string);
		xsdTypesToJava.put("xsd:NCName", string);
		xsdTypesToJava.put("xs:NCName", string);
		xsdTypesToJava.put("NCName", string);
		xsdTypesToJava.put("xsd:ID", string);
		xsdTypesToJava.put("xs:ID", string);
		xsdTypesToJava.put("ID", string);
		xsdTypesToJava.put("xsd:IDREF", string);
		xsdTypesToJava.put("xs:IDREF", string);
		xsdTypesToJava.put("IDREF", string);
		xsdTypesToJava.put("xsd:ENTITY", string);
		xsdTypesToJava.put("xs:ENTITY", string);
		xsdTypesToJava.put("ENTITY", string);
		xsdTypesToJava.put("xsd:untypedAtomic", string);
		xsdTypesToJava.put("xs:untypedAtomic", string);
		xsdTypesToJava.put("untypedAtomic", string);
	}

	public static BAttributeType getBAttributeType(QName xsdTypeQ, String elementType, String attributeName) {
		switch (getJavaType(xsdTypeQ)) {
			case "BigDecimal":
			case "Double":
			case "Float":
				return new BAttributeType(elementType, attributeName, BAttributeType.BType.REAL);
			case "BigInteger":
			case "Integer":
			case "Short":
			case "Long":
				return new BAttributeType(elementType, attributeName, BAttributeType.BType.INTEGER);
			case "Duration":
				return new BAttributeType(elementType, attributeName, true);
			case "Boolean":
				return new BAttributeType(elementType, attributeName, BAttributeType.BType.BOOL);
			default:
				return new BAttributeType(elementType, attributeName);
		}
	}

	public static String getJavaType(QName xsdType) {
		return xsdTypesToJava.getOrDefault(qNameToString(xsdType), "unknown");
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
