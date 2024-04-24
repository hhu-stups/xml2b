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
			elements.putAll(collectElementsForParticle(complex.getParticle()));
			XmlSchemaContentModel content = complex.getContentModel();
			if (content != null && content.getContent() instanceof XmlSchemaComplexContentExtension) {
				XmlSchemaComplexContentExtension extension = (XmlSchemaComplexContentExtension) content.getContent();
				elements.putAll(collectElementsForParticle(extension.getParticle()));
			}
		}
		elements.putAll(collectElementsFromElements(elements));
		return elements;
	}

	private static Map<QName, XmlSchemaElement> collectElementsForParticle(XmlSchemaParticle particle) {
		Map<QName, XmlSchemaElement> elements = new HashMap<>();
		if (particle instanceof XmlSchemaAll) {
			XmlSchemaAll schemaAll = (XmlSchemaAll) particle;
			for (XmlSchemaAllMember all : schemaAll.getItems()) {
				elements.putAll(addElement(all));
			}
		} else if (particle instanceof XmlSchemaSequence) {
			XmlSchemaSequence schemaSequence = (XmlSchemaSequence) particle;
			for (XmlSchemaSequenceMember sequence : schemaSequence.getItems()) {
				elements.putAll(addElement(sequence));
				if (sequence instanceof XmlSchemaChoice) {
					XmlSchemaChoice schemaChoice = (XmlSchemaChoice) sequence;
					for (XmlSchemaChoiceMember choice : schemaChoice.getItems()) {
						elements.putAll(addElement(choice));
					}
				}
			}
		}
		return elements;
	}

	private static Map<QName, XmlSchemaElement> addElement(XmlSchemaObjectBase object) {
		Map<QName, XmlSchemaElement> elements = new HashMap<>();
		if (object instanceof XmlSchemaElement) {
			XmlSchemaElement element = (XmlSchemaElement) object;
			elements.put(element.getQName(), element);
		}
		return elements;
	}

	/*public static Map<QName, XmlSchemaType> collectLocalTypesFromElements(Map<QName, XmlSchemaElement> elementMap) {
		Map<QName, XmlSchemaType> types = new HashMap<>();
		for (XmlSchemaElement element : elementMap.values()) {
			types.put(element.getSchemaTypeName(), element.getSchemaType());
		}
		return elements;
	}*/

}
