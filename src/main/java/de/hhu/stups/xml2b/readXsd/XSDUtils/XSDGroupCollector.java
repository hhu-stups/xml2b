package de.hhu.stups.xml2b.readXsd.XSDUtils;

import org.apache.ws.commons.schema.*;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

public class XSDGroupCollector {

	public static Map<QName, XmlSchemaElement> collectElementsFromGroups(Map<QName, XmlSchemaGroup> groupMap) {
		Map<QName, XmlSchemaElement> elements = new HashMap<>();
		for (XmlSchemaGroup group : groupMap.values()) {
			elements.putAll(collectElementsForGroupParticle(group.getParticle()));
		}
		return elements;
	}

	private static Map<QName, XmlSchemaElement> collectElementsForGroupParticle(XmlSchemaGroupParticle particle) {
		Map<QName, XmlSchemaElement> groupElements = new HashMap<>();
		if (particle instanceof XmlSchemaAll) {
			XmlSchemaAll schemaAll = (XmlSchemaAll) particle;
			for (XmlSchemaAllMember all : schemaAll.getItems()) {
				if (all instanceof XmlSchemaGroup) {
					XmlSchemaGroup schemaGroup = (XmlSchemaGroup) all;
					groupElements.putAll(collectElementsForGroupParticle(schemaGroup.getParticle()));
				} else if (all instanceof XmlSchemaElement) {
					XmlSchemaElement schemaElement = (XmlSchemaElement) all;
					groupElements.put(schemaElement.getQName(), schemaElement);
				}
				// ignore if: all instanceof XmlSchemaGroupRef (these do not contain new XmlSchemaElements)
			}
		} else if (particle instanceof XmlSchemaSequence) {
			XmlSchemaSequence schemaSequence = (XmlSchemaSequence) particle;
			for (XmlSchemaSequenceMember sequence : schemaSequence.getItems()) {
				if (sequence instanceof XmlSchemaGroup) {
					XmlSchemaGroup schemaGroup = (XmlSchemaGroup) sequence;
					groupElements.putAll(collectElementsForGroupParticle(schemaGroup.getParticle()));
				} else if (sequence instanceof XmlSchemaElement) {
					XmlSchemaElement schemaElement = (XmlSchemaElement) sequence;
					groupElements.put(schemaElement.getQName(), schemaElement);
				}
				// ignore if: sequence instanceof XmlSchemaAny | XmlSchemaGroupRef (these do not contain new XmlSchemaElements)
			}
		} else if (particle instanceof XmlSchemaChoice) {
			XmlSchemaChoice schemaChoice = (XmlSchemaChoice) particle;
			for (XmlSchemaChoiceMember choice : schemaChoice.getItems()) {
				if (choice instanceof XmlSchemaGroup) {
					XmlSchemaGroup schemaGroup = (XmlSchemaGroup) choice;
					groupElements.putAll(collectElementsForGroupParticle(schemaGroup.getParticle()));
				} else if (choice instanceof XmlSchemaElement) {
					XmlSchemaElement schemaElement = (XmlSchemaElement) choice;
					groupElements.put(schemaElement.getQName(), schemaElement);
				}
				// ignore if: choice instanceof XmlSchemaAny | XmlSchemaGroupRef (these do not contain new XmlSchemaElements)
			}
		}
		return groupElements;
	}

}
