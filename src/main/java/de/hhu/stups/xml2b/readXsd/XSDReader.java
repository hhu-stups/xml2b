package de.hhu.stups.xml2b.readXsd;

import de.hhu.stups.xml2b.bTypes.BAttributeType;
import de.hhu.stups.xml2b.bTypes.BEnumSet;
import de.hhu.stups.xml2b.bTypes.BEnumSetAttributeType;
import de.hhu.stups.xml2b.bTypes.BStringAttributeType;
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.*;

import static de.hhu.stups.xml2b.readXsd.TypeUtils.isConvertibleType;
import static de.hhu.stups.xml2b.readXsd.TypeUtils.qNameToString;
import static de.hhu.stups.xml2b.readXsd.XSDUtils.XSDElementCollector.collectElementsFromElements;
import static de.hhu.stups.xml2b.readXsd.XSDUtils.XSDElementCollector.collectElementsFromSchemaTypes;
import static de.hhu.stups.xml2b.readXsd.XSDUtils.XSDGroupCollector.collectElementsFromGroups;

public class XSDReader {
	private final Map<QName, XmlSchemaType> types = new HashMap<>();
	private final Map<QName, XmlSchemaElement> elements = new HashMap<>();
	private final Map<QName, XmlSchemaAttributeGroup> attributeGroups = new HashMap<>();
	private final Map<String, Set<XmlSchemaAttribute>> attributesOfElement = new HashMap<>(); // keys are qNameAsString
	private final Map<String, BAttributeType> contentOfElement = new HashMap<>();
	private final Map<String, Map<String, BAttributeType>> attributeTypesOfElement = new HashMap<>(); // keys are qNameAsString
	private final Map<QName, BEnumSet> enumSets = new HashMap<>();

	// TODO: Attribute Freetypes identifier müssen aus element:attribut bestehen. Aber deren Typen müssen global sein, z.B: applicationDirection
	// gehe einfach einmal über alle SimpleTypes und extrahiere die enumSets. Wenn Attribut mit so einem Typ kommt: nehmen.
	// Einschränkung: zwei gleich benannte Elemente mit gleich benanntem Attribut aber unterschiedlichen Typen (sehr selten?)
	public XSDReader(final File xsdSchema) {
		System.setProperty("javax.xml.accessExternalDTD", "all");
		XmlSchema schema = new XmlSchemaCollection().read(new InputSource(xsdSchema.toURI().toString()));
		this.collectSchemaTypesAndGroups(schema, new ArrayList<>());
		this.collectSchemaElements();
		this.collectEnumSets(types.keySet());
		this.collectAttributeTypes();
	}

	private void collectSchemaTypesAndGroups(XmlSchema schema, List<XmlSchema> visited) {
		types.putAll(schema.getSchemaTypes());

		Map<QName, XmlSchemaElement> needRecursiveSearchElements = new HashMap<>();
		needRecursiveSearchElements.putAll(schema.getElements());
		needRecursiveSearchElements.putAll(collectElementsFromGroups(schema.getGroups()));
		elements.putAll(needRecursiveSearchElements);
		elements.putAll(collectElementsFromElements(needRecursiveSearchElements));
		elements.putAll(collectElementsFromSchemaTypes(schema.getSchemaTypes()));

		collectAttributeGroups(schema.getAttributeGroups());
		for (XmlSchemaExternal external : schema.getExternals()) {
			XmlSchema externalSchema = external.getSchema();
			for (XmlSchemaExternal furtherExternal : externalSchema.getExternals()) {
				XmlSchema furtherExternalSchema = furtherExternal.getSchema();
				// prevent from looping in references
				if (!visited.contains(furtherExternalSchema)) {
					visited.add(furtherExternalSchema);
					collectSchemaTypesAndGroups(furtherExternal.getSchema(), visited);
				}
			}
			types.putAll(externalSchema.getSchemaTypes());
			collectAttributeGroups(externalSchema.getAttributeGroups());
		}
	}

	private void collectAttributeGroups(Map<QName, XmlSchemaAttributeGroup> groups) {
		attributeGroups.putAll(groups);
		Map<QName, XmlSchemaAttributeGroup> innerGroups = new HashMap<>();
		for (XmlSchemaAttributeGroup group : groups.values()) {
			for (XmlSchemaAttributeGroupMember member : group.getAttributes()) {
				if (member instanceof XmlSchemaAttributeGroup) {
					XmlSchemaAttributeGroup innerGroup = (XmlSchemaAttributeGroup) member;
					innerGroups.put(innerGroup.getQName(), innerGroup);
				}
			}
		}
		attributeGroups.putAll(innerGroups);
		if (!innerGroups.isEmpty()) {
			collectAttributeGroups(innerGroups);
		}
	}

	private void collectSchemaElements() {
		for (XmlSchemaType type : types.values()) {
			if (type instanceof XmlSchemaComplexType) {
				XmlSchemaComplexType complex = (XmlSchemaComplexType) type;
				collectElementsForParticle(complex.getParticle());
				XmlSchemaContentModel content = complex.getContentModel();
				if (content != null && content.getContent() instanceof XmlSchemaComplexContentExtension) {
					XmlSchemaComplexContentExtension extension = (XmlSchemaComplexContentExtension) content.getContent();
					collectElementsForParticle(extension.getParticle());
				}
			}
		}
	}

	private void collectElementsForParticle(XmlSchemaParticle particle) {
		if (particle instanceof XmlSchemaAll) {
			XmlSchemaAll schemaAll = (XmlSchemaAll) particle;
			for (XmlSchemaAllMember all : schemaAll.getItems()) {
				addElement(all);
			}
		} else if (particle instanceof XmlSchemaSequence) {
			XmlSchemaSequence schemaSequence = (XmlSchemaSequence) particle;
			for (XmlSchemaSequenceMember sequence : schemaSequence.getItems()) {
				addElement(sequence);
				if (sequence instanceof XmlSchemaChoice) {
					XmlSchemaChoice schemaChoice = (XmlSchemaChoice) sequence;
					for (XmlSchemaChoiceMember choice : schemaChoice.getItems()) {
						addElement(choice);
					}
				}
			}
		}
	}

	private void addElement(XmlSchemaObjectBase object) {
		if (object instanceof XmlSchemaElement) {
			XmlSchemaElement element = (XmlSchemaElement) object;
			Set<XmlSchemaAttribute> attributes = collectSchemaAttributesAndContents(element);
			attributesOfElement.put(qNameToString(element.getQName()), attributes);
		}
	}

	private Set<XmlSchemaAttribute> collectSchemaAttributesAndContents(XmlSchemaElement element) {
		Set<XmlSchemaAttribute> attributes = new HashSet<>();
		XmlSchemaType schemaType = element.getSchemaType();
		if (schemaType instanceof XmlSchemaSimpleType) {
			XmlSchemaSimpleType simpleType = (XmlSchemaSimpleType) schemaType;
			collectContentForSimpleType(simpleType, qNameToString(element.getQName()));
		} else if (schemaType instanceof XmlSchemaComplexType) {
			XmlSchemaComplexType complexType = (XmlSchemaComplexType) schemaType;
			attributes.addAll(collectSchemaAttributesForComplexType(complexType));
		}
		return attributes;
	}

	private void collectContentForSimpleType(XmlSchemaSimpleType simpleType, String qNameOfElement) {
		BAttributeType type;
		if (isConvertibleType(simpleType.getQName())) {
			type = extractAttributeType(simpleType.getQName(), qNameOfElement,null);
		} else {
			XmlSchemaSimpleTypeContent content = simpleType.getContent();
			if (content instanceof XmlSchemaSimpleTypeRestriction) {
				XmlSchemaSimpleTypeRestriction restriction = (XmlSchemaSimpleTypeRestriction) content;
				type = extractAttributeType(restriction.getBaseTypeName(), qNameOfElement,null);
			} else {
				type = new BStringAttributeType(qNameOfElement, null);
			}
		}
		contentOfElement.put(qNameOfElement, type);
	}

	private Set<XmlSchemaAttribute> collectSchemaAttributesForComplexType(XmlSchemaComplexType complexType) {
		Set<XmlSchemaAttribute> collectedAttributes = extractAttributesFromXmlSchemaAttributeOrGroupRef(complexType.getAttributes());
		XmlSchemaContentModel content = complexType.getContentModel();
		if (content != null && content.getContent() instanceof XmlSchemaComplexContentExtension) {
			XmlSchemaComplexContentExtension extension = (XmlSchemaComplexContentExtension) content.getContent();
			collectedAttributes.addAll(extractAttributesFromXmlSchemaAttributeOrGroupRef(extension.getAttributes()));
			if (types.getOrDefault(extension.getBaseTypeName(), null) instanceof XmlSchemaComplexType) {
				XmlSchemaComplexType baseType = (XmlSchemaComplexType) types.getOrDefault(extension.getBaseTypeName(), null);
				// collect attributes of all base types
				collectedAttributes.addAll(collectSchemaAttributesForComplexType(baseType));
			}
		}
		return collectedAttributes;
	}

	private Set<XmlSchemaAttribute> extractAttributesFromXmlSchemaAttributeOrGroupRef(List<XmlSchemaAttributeOrGroupRef> attributes) {
		Set<XmlSchemaAttribute> collectedAttributes = new HashSet<>();
		for (XmlSchemaAttributeOrGroupRef attribute : attributes) {
			if (attribute instanceof XmlSchemaAttribute) {
				XmlSchemaAttribute attr = (XmlSchemaAttribute) attribute;
				collectedAttributes.add(attr);
			} else if (attribute instanceof XmlSchemaAttributeGroupRef) {
				XmlSchemaAttributeGroup group = attributeGroups.get(((XmlSchemaAttributeGroupRef) attribute).getTargetQName());
				if (group != null) {
					for (XmlSchemaAttributeGroupMember member : group.getAttributes()) {
						if (member instanceof XmlSchemaAttribute) {
							XmlSchemaAttribute attr = (XmlSchemaAttribute) member;
							collectedAttributes.add(attr);
						}
					}
				}
			}
		}
		return collectedAttributes;
	}

	private void collectEnumSets(Set<QName> typeNames) {
		for (QName typeName : typeNames) {
			XmlSchemaType type = types.getOrDefault(typeName, null);
			if (type instanceof XmlSchemaSimpleType
					&& ((XmlSchemaSimpleType) type).getContent() instanceof XmlSchemaSimpleTypeRestriction) {
				XmlSchemaSimpleTypeRestriction restriction = (XmlSchemaSimpleTypeRestriction) ((XmlSchemaSimpleType) type).getContent();
				Set<String> enumValues = getEnumValuesFromFacets(restriction.getFacets(), typeName);
				if (!enumValues.isEmpty() && TypeUtils.getJavaType(restriction.getBaseTypeName()).equals("String")) {
					if (!enumSets.containsKey(typeName)) {
						enumSets.put(typeName, new BEnumSet(typeName.getLocalPart(), enumValues));
					} else {
						enumSets.get(typeName).addValues(enumValues);
					}
				}
			} else if (type instanceof XmlSchemaSimpleType
					&& ((XmlSchemaSimpleType) type).getContent() instanceof XmlSchemaSimpleTypeUnion) {
				// <xs:union memberTypes="rail3:tBaliseGroupType rail3:tOtherEnumerationValue"/>
				// TODO: this solution does not work for multiple memberTypes that are already enum sets!
				XmlSchemaSimpleTypeUnion union = (XmlSchemaSimpleTypeUnion) ((XmlSchemaSimpleType) type).getContent();
				collectEnumSets(new HashSet<>(Arrays.asList(union.getMemberTypesQNames())));
			}
		}
	}

	private static Set<String> getEnumValuesFromFacets(List<XmlSchemaFacet> facets, QName identifier) {
		Set<String> enum_values = new HashSet<>();
		for (XmlSchemaFacet facet : facets) {
			if (facet instanceof XmlSchemaEnumerationFacet) {
				XmlSchemaEnumerationFacet enumerationFacet = (XmlSchemaEnumerationFacet) facet;
				enum_values.add(identifier.getLocalPart() + "_" + enumerationFacet.getValue().toString());
			}
		}
		return enum_values;
	}

	public void collectAttributeTypes() {
		for (String elementType : attributesOfElement.keySet()) {
			Map<String, BAttributeType> attributeTypes = new HashMap<>();
			for (XmlSchemaAttribute attribute : attributesOfElement.get(elementType)) {
				String attributeName = qNameToString(attribute.getQName());
				attributeTypes.put(attributeName, extractAttributeType(attribute.getSchemaTypeName(), elementType, attributeName));
			}
			attributeTypesOfElement.put(elementType, attributeTypes);
		}
	}

	private BAttributeType extractAttributeType(QName typeName, String elementType, String attributeName) {
		XmlSchemaType type = types.getOrDefault(typeName, null);
		if (enumSets.containsKey(typeName)) {
			return new BEnumSetAttributeType(elementType, attributeName, enumSets.get(typeName));
		} else if (type instanceof XmlSchemaSimpleType
				&& ((XmlSchemaSimpleType) type).getContent() instanceof XmlSchemaSimpleTypeRestriction) {
			XmlSchemaSimpleTypeRestriction restriction = (XmlSchemaSimpleTypeRestriction) ((XmlSchemaSimpleType) type).getContent();
			QName baseName = restriction.getBaseTypeName();
			if (types.containsKey(baseName)) {
				return extractAttributeType(baseName, elementType, attributeName);
			} else {
				return TypeUtils.getBAttributeType(baseName, elementType, attributeName);
			}
		} else {
			return TypeUtils.getBAttributeType(typeName, elementType, attributeName);
		}
	}

	private static void getDocumentationOfAttribute(XmlSchemaAttribute attr) {
		if (attr.getAnnotation() != null) {
			List<XmlSchemaAnnotationItem> items = attr.getAnnotation().getItems();
			for(XmlSchemaAnnotationItem item : items) {
				if (item instanceof XmlSchemaDocumentation) {
					XmlSchemaDocumentation docu = (XmlSchemaDocumentation) item;
					NodeList markup = docu.getMarkup();
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < markup.getLength(); i++) {
						sb.append(markup.item(i).getTextContent());
					}
					//System.out.println(sb);
				}
			}
		}
	}

	public Map<QName, XmlSchemaType> getTypes() {
		return types;
	}

	public Map<String, Map<String, BAttributeType>> getAttributeTypesOfElement() {
		return attributeTypesOfElement;
	}

	public Map<String, BAttributeType> getContentOfElement() {
		return contentOfElement;
	}

	public Map<QName, BEnumSet> getEnumSets() {
		return enumSets;
	}
}
