package de.hhu.stups.xml2b.readXsd;

import de.hhu.stups.xml2b.bTypes.BAttribute;
import de.hhu.stups.xml2b.bTypes.BEnumSet;
import de.hhu.stups.xml2b.bTypes.BEnumSetAttribute;
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.*;

public class XSDReader {
	private final Map<QName, XmlSchemaType> types = new HashMap<>();
	private final Map<XmlSchemaElement, Set<XmlSchemaAttribute>> elements = new HashMap<>();
	private final Map<String, Set<XmlSchemaAttribute>> attributesOfElementName = new HashMap<>();
	private final Map<QName, BEnumSet> enumSets = new HashMap<>();

	// TODO: Attribute Freetypes identifier müssen aus element:attribut bestehen. Aber deren Typen müssen global sein, z.B: applicationDirection
	// gehe einfach einmal über alle SimpleTypes und extrahiere die enumSets. Wenn Attribut mit so einem Typ kommt: nehmen.
	// Einschränkung: zwei gleich benannte Elemente mit gleich benanntem Attribut aber unterschiedlichen Typen (sehr selten?)
	public XSDReader(final File xsdSchema) {
		System.setProperty("javax.xml.accessExternalDTD", "all");
		XmlSchema schema = new XmlSchemaCollection().read(new InputSource(xsdSchema.toURI().toString()));
		this.collectSchemaTypes(schema);
		this.collectSchemaElements();
		this.collectEnumSets();
	}

	private void collectSchemaTypes(XmlSchema schema) {
		List<XmlSchema> visited = new ArrayList<>();
		visited.add(schema);
		types.putAll(schema.getSchemaTypes());
		for (XmlSchemaExternal external : schema.getExternals()) {
			XmlSchema externalSchema = external.getSchema();
			visited.add(externalSchema);
			for (XmlSchemaExternal furtherExternal : externalSchema.getExternals()) {
				collectSchemaTypes(furtherExternal.getSchema(), visited);
			}
			types.putAll(externalSchema.getSchemaTypes());
		}
	}

	private void collectSchemaTypes(XmlSchema schema, List<XmlSchema> visited) {
		types.putAll(schema.getSchemaTypes());
		for (XmlSchemaExternal external : schema.getExternals()) {
			XmlSchema externalSchema = external.getSchema();
			for (XmlSchemaExternal furtherExternal : externalSchema.getExternals()) {
				XmlSchema furtherExternalSchema = furtherExternal.getSchema();
				// prevent from looping in references
				if (!visited.contains(furtherExternalSchema)) {
					collectSchemaTypes(furtherExternal.getSchema());
				}
			}
			types.putAll(externalSchema.getSchemaTypes());
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
			Set<XmlSchemaAttribute> attributes = collectSchemaAttributes(element);
			elements.put(element, attributes);
			attributesOfElementName.put(element.getName(), attributes);
		}
	}

	private Set<XmlSchemaAttribute> collectSchemaAttributes(XmlSchemaElement element) {
		Set<XmlSchemaAttribute> attributes = new HashSet<>();
		if (types.getOrDefault(element.getSchemaTypeName(), null) instanceof XmlSchemaComplexType) {
			XmlSchemaComplexType complexType = (XmlSchemaComplexType) types.getOrDefault(element.getSchemaTypeName(), null);
			attributes.addAll(collectSchemaAttributesForType(complexType));
		}
		// TODO: attributeGroups
		return attributes;
	}

	private Set<XmlSchemaAttribute> collectSchemaAttributesForType(XmlSchemaComplexType complexType) {
		Set<XmlSchemaAttribute> collectedAttributes = extractAttributesFromXmlSchemaAttributeOrGroupRef(complexType.getAttributes());
		XmlSchemaContentModel content = complexType.getContentModel();
		if (content != null && content.getContent() instanceof XmlSchemaComplexContentExtension) {
			XmlSchemaComplexContentExtension extension = (XmlSchemaComplexContentExtension) content.getContent();
			collectedAttributes.addAll(extractAttributesFromXmlSchemaAttributeOrGroupRef(extension.getAttributes()));
			if (types.getOrDefault(extension.getBaseTypeName(), null) instanceof XmlSchemaComplexType) {
				XmlSchemaComplexType baseType = (XmlSchemaComplexType) types.getOrDefault(extension.getBaseTypeName(), null);
				// collect attributes of all base types
				collectedAttributes.addAll(collectSchemaAttributesForType(baseType));
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
			}
		}
		return collectedAttributes;
	}

	private void collectEnumSets() {
		for (QName typeName : types.keySet()) {
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

	public BAttribute extractAttributeType(XmlSchemaAttribute attribute, String value) {
		return extractAttributeType(attribute.getSchemaTypeName(), value);
	}

	private BAttribute extractAttributeType(QName typeName, String value) {
		XmlSchemaType type = types.getOrDefault(typeName, null);
		if (enumSets.containsKey(typeName)) {
			return new BEnumSetAttribute(enumSets.get(typeName), value);
		} else if (type instanceof XmlSchemaSimpleType
				&& ((XmlSchemaSimpleType) type).getContent() instanceof XmlSchemaSimpleTypeRestriction) {
			XmlSchemaSimpleTypeRestriction restriction = (XmlSchemaSimpleTypeRestriction) ((XmlSchemaSimpleType) type).getContent();
			QName baseName = restriction.getBaseTypeName();
			if (types.containsKey(baseName)) {
				return extractAttributeType(baseName, value);
			} else {
				return TypeUtils.getBType(baseName, value);
			}
		} else {
			return TypeUtils.getBType(typeName, value);
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

	public Map<XmlSchemaElement, Set<XmlSchemaAttribute>> getElements() {
		return elements;
	}

	public Map<String, Set<XmlSchemaAttribute>> getAttributesOfElementName() {
		return attributesOfElementName;
	}

	public Map<QName, BEnumSet> getEnumSets() {
		return enumSets;
	}
}
