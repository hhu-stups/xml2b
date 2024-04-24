package de.hhu.stups.xml2b.readXsd.XSDUtils;

import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

public class XSDElementCollector {

	// elements from xs:redefine are ignored
	public static Map<QName, XmlSchemaElement> collectElementsFromSchemaTypes(Map<QName, XmlSchemaType> typeMap) {
		Map<QName, XmlSchemaElement> elements = new HashMap<>();
		for (XmlSchemaType type : typeMap.values()) {
			elements.putAll(collectElementsFromType(type));
		}
		return elements;
	}

	public static Map<QName, XmlSchemaElement> collectElementsFromElements(Map<QName, XmlSchemaElement> elementMap) {
		Map<QName, XmlSchemaElement> elements = new HashMap<>();
		for (XmlSchemaElement element : elementMap.values()) {
			elements.putAll(collectElementsFromType(element.getSchemaType()));
		}
		return elements;
	}

	private static Map<QName, XmlSchemaElement> collectElementsFromType(XmlSchemaType type) {
		Map<QName, XmlSchemaElement> elements = new HashMap<>();
		if (type instanceof XmlSchemaComplexType) {
			XmlSchemaComplexType complex = (XmlSchemaComplexType) type;
			elements.putAll(collectElementsFromParticle(complex.getParticle()));
			XmlSchemaContentModel content = complex.getContentModel();
			// xs:restriction complexContent is ignored -- it restricts on existing types and does not introduce new elements
			if (content != null && content.getContent() instanceof XmlSchemaComplexContentExtension) {
				XmlSchemaComplexContentExtension extension = (XmlSchemaComplexContentExtension) content.getContent();
				elements.putAll(collectElementsFromParticle(extension.getParticle()));
			}
		}
		elements.putAll(collectElementsFromElements(elements));
		return elements;
	}

	private static Map<QName, XmlSchemaElement> collectElementsFromParticle(XmlSchemaParticle particle) {
		Map<QName, XmlSchemaElement> elements = new HashMap<>();
		if (particle instanceof XmlSchemaAll) {
			XmlSchemaAll schemaAll = (XmlSchemaAll) particle;
			for (XmlSchemaAllMember all : schemaAll.getItems()) {
				addElement(elements, all);
			}
		} else if (particle instanceof XmlSchemaChoice || particle instanceof XmlSchemaSequence) {
			elements.putAll(collectElementsFromNestedChoicesAndSequences(particle));
		}
		return elements;
	}

	private static void addElement(Map<QName, XmlSchemaElement> elements, XmlSchemaObjectBase object) {
		if (object instanceof XmlSchemaElement) {
			XmlSchemaElement element = (XmlSchemaElement) object;
			elements.put(element.getQName(), element);
		}
	}

	private static Map<QName, XmlSchemaElement> collectElementsFromNestedChoicesAndSequences(XmlSchemaObjectBase object) {
		Map<QName, XmlSchemaElement> elements = new HashMap<>();
		if (object instanceof XmlSchemaChoice) {
			XmlSchemaChoice schemaChoice = (XmlSchemaChoice) object;
			for (XmlSchemaChoiceMember choice : schemaChoice.getItems()) {
				addElement(elements, choice);
				elements.putAll(collectElementsFromNestedChoicesAndSequences(choice));
			}
		} else if (object instanceof XmlSchemaSequence) {
			XmlSchemaSequence schemaSequence = (XmlSchemaSequence) object;
			for (XmlSchemaSequenceMember sequence : schemaSequence.getItems()) {
				addElement(elements, sequence);
				elements.putAll(collectElementsFromNestedChoicesAndSequences(sequence));
			}
		}
		return elements;
	}
}
