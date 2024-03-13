package de.hhu.stups.xml2b.readXsd;

import de.hhu.stups.xml2b.bTypes.BAttribute;
import de.hhu.stups.xml2b.bTypes.BStringAttribute;
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

	public XSDReader(final File xsdSchema) {
		System.setProperty("javax.xml.accessExternalDTD", "all");
		XmlSchema schema = new XmlSchemaCollection().read(new InputSource(xsdSchema.toURI().toString()));
		this.collectSchemaTypes(schema);
		this.collectSchemaElements();
	}

	private void collectSchemaTypes(XmlSchema schema) {
		types.putAll(schema.getSchemaTypes());
		for (XmlSchemaExternal external : schema.getExternals()) {
			XmlSchema externalSchema = external.getSchema();
			for (XmlSchemaExternal furtherExternal : externalSchema.getExternals()) {
				collectSchemaTypes(furtherExternal.getSchema());
			}
			types.putAll(externalSchema.getSchemaTypes());
		}
	}

	private void collectSchemaElements() {
		for (XmlSchemaType type : types.values()) {
			if (type instanceof XmlSchemaComplexType complex) {
				collectElementsForParticle(complex.getParticle());
				XmlSchemaContentModel content = complex.getContentModel();
				if (content != null && content.getContent() instanceof XmlSchemaComplexContentExtension extension) {
					collectElementsForParticle(extension.getParticle());
				}
			}
		}
	}

	private void collectElementsForParticle(XmlSchemaParticle particle) {
		if (particle instanceof XmlSchemaAll schemaAll) {
			for (XmlSchemaAllMember all : schemaAll.getItems()) {
				addElement(all);
			}
		} else if (particle instanceof XmlSchemaSequence schemaSequence) {
			for (XmlSchemaSequenceMember sequence : schemaSequence.getItems()) {
				addElement(sequence);
				if (sequence instanceof XmlSchemaChoice schemaChoice) {
					for (XmlSchemaChoiceMember choice : schemaChoice.getItems()) {
						addElement(choice);
					}
				}
			}
		}
	}

	private void addElement(XmlSchemaObjectBase object) {
		if (object instanceof XmlSchemaElement element) {
			Set<XmlSchemaAttribute> attributes = collectSchemaAttributes(element);
			elements.put(element, attributes);
			attributesOfElementName.put(element.getName(), attributes);
		}
	}

	private Set<XmlSchemaAttribute> collectSchemaAttributes(XmlSchemaElement element) {
		Set<XmlSchemaAttribute> attributes = new HashSet<>();
		if (types.getOrDefault(element.getSchemaTypeName(), null) instanceof XmlSchemaComplexType complexType) {
			attributes.addAll(collectSchemaAttributesForType(complexType));
		}
		// TODO: attributeGroups
		return attributes;
	}

	private Set<XmlSchemaAttribute> collectSchemaAttributesForType(XmlSchemaComplexType complexType) {
		Set<XmlSchemaAttribute> collectedAttributes = extractAttributesFromXmlSchemaAttributeOrGroupRef(complexType.getAttributes());
		XmlSchemaContentModel content = complexType.getContentModel();
		if (content != null && content.getContent() instanceof XmlSchemaComplexContentExtension extension) {
			collectedAttributes.addAll(extractAttributesFromXmlSchemaAttributeOrGroupRef(extension.getAttributes()));
			if (types.getOrDefault(extension.getBaseTypeName(), null) instanceof XmlSchemaComplexType baseType) {
				// collect attributes of all base types
				collectedAttributes.addAll(collectSchemaAttributesForType(baseType));
			}
		}
		return collectedAttributes;
	}

	private static Set<XmlSchemaAttribute> extractAttributesFromXmlSchemaAttributeOrGroupRef(List<XmlSchemaAttributeOrGroupRef> attributes) {
		Set<XmlSchemaAttribute> collectedAttributes = new HashSet<>();
		for (XmlSchemaAttributeOrGroupRef attribute : attributes) {
			if (attribute instanceof XmlSchemaAttribute attr) {
				collectedAttributes.add(attr);
			}
		}
		return collectedAttributes;
	}

	public BAttribute extractAttributeType(XmlSchemaAttribute attribute, String value) {
		return extractAttributeType(attribute.getQName(), attribute.getSchemaTypeName(), value);
	}

	private BAttribute extractAttributeType(QName attrName, QName typeName, String value) {
		XmlSchemaType type = types.getOrDefault(typeName, null);
		if (type instanceof XmlSchemaSimpleType simple
				&& simple.getContent() instanceof XmlSchemaSimpleTypeRestriction restriction) {
			QName baseName = restriction.getBaseTypeName();
			if (types.containsKey(baseName)) {
				return extractAttributeType(attrName, baseName, value);
			} else if (TypeUtils.getBType(attrName, baseName, value) instanceof BStringAttribute) {
				return TypeUtils.getBType(attrName, baseName, restriction.getFacets(), value);
			} else {
				return TypeUtils.getBType(attrName, baseName, value);
			}
		} else {
			return TypeUtils.getBType(attrName, typeName, value);
		}
	}

	private static void getDocumentationOfAttribute(XmlSchemaAttribute attr) {
		if (attr.getAnnotation() != null) {
			List<XmlSchemaAnnotationItem> items = attr.getAnnotation().getItems();
			for(XmlSchemaAnnotationItem item : items) {
				if (item instanceof XmlSchemaDocumentation docu) {
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
}
