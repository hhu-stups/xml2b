package de.hhu.stups.xml2b.readXsd;

import de.hhu.stups.xml2b.bTypes.BAttributeType;
import de.hhu.stups.xml2b.bTypes.BEnumSet;
import de.hhu.stups.xml2b.bTypes.BEnumSetAttributeType;
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.walker.XmlSchemaAttrInfo;
import org.apache.ws.commons.schema.walker.XmlSchemaTypeInfo;
import org.apache.ws.commons.schema.walker.XmlSchemaVisitor;
import org.apache.ws.commons.schema.walker.XmlSchemaWalker;
import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.*;

import static de.hhu.stups.xml2b.readXsd.TypeUtils.qNameToString;
import static org.apache.ws.commons.schema.walker.XmlSchemaBaseSimpleType.ANYTYPE;

public class CustomXSDVisitor implements XmlSchemaVisitor {

	public static class XSDType {

		private final QName qName;
		private final BAttributeType contentType;
		private final Map<String, BAttributeType> attributeTypes;

		public XSDType(QName qName, BAttributeType contentType, Map<String, BAttributeType> attributeTypes) {
			this.qName = qName;
			this.contentType = contentType;
			this.attributeTypes = attributeTypes;
		}

		public XSDElement createXSDElement(final String qName, final List<String> parents) {
			return new XSDElement(qName, parents, contentType, attributeTypes);
		}

		public void addAttributeType(final String name, final BAttributeType attributeType) {
			this.attributeTypes.put(name, attributeType);
		}

		public QName getQName() {
			return qName;
		}

		public BAttributeType getContentType() {
			return contentType;
		}

		public Map<String, BAttributeType> getAttributeTypes() {
			return attributeTypes;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof XSDType && this.qName.equals(((XSDType) obj).qName);
		}
	}

	private final Stack<String> openElements = new Stack<>();
	private final Map<QName, XSDType> visitedTypes = new HashMap<>();
	private final Map<XmlSchemaElement, XSDElement> elementMapping = new HashMap<>();
	private final Map<String, BAttributeType> currentAttributes = new HashMap<>();
	private final Map<QName, Map<String, BAttributeType>> attributeMapping = new HashMap<>();

	private final Map<QName, XmlSchemaType> types = new HashMap<>();
	private final Map<QName, BEnumSet> enumSets = new HashMap<>();

	public CustomXSDVisitor(final File xsdSchema) {
		System.setProperty("javax.xml.accessExternalDTD", "all");
		XmlSchemaCollection collection = new XmlSchemaCollection();
		XmlSchema schema = collection.read(new InputSource(xsdSchema.toURI().toString()));
		this.collectSchemaTypesAndGroups(schema, new ArrayList<>());
		this.collectEnumSets(types.keySet());

		XmlSchemaWalker walker = new XmlSchemaWalker(collection, this);
		walker.setUserRecognizedTypes(types.keySet());
		for (XmlSchemaElement element : schema.getElements().values()) {
			walker.walk(element);
		}
		//this.getElements().values().forEach(e -> System.out.println(e));
		//System.out.println(visitedTypes.keySet());
		//this.attributeMapping.values().forEach(e -> System.out.println(e));
	}

	public Map<List<String>, XSDElement> getElements() {
		HashMap<List<String>, XSDElement> elements = new HashMap<>();
		elementMapping.forEach((element, xsdElement) -> {
			elements.put(xsdElement.getParentsWithThis(), xsdElement);
			/*Set<BAttributeType> attributeTypes = attributeMapping.get(element);
			if (attributeTypes != null)
				attributeTypes.forEach(a -> {
					xsdElement.addAttributeType(a.getAttributeName(), a);
				});*/
		});
		return elements;
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

	private void collectSchemaTypesAndGroups(XmlSchema schema, List<XmlSchema> visited) {
		types.putAll(schema.getSchemaTypes());
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
		}
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

	@Override
	public void onEnterElement(XmlSchemaElement xmlSchemaElement, XmlSchemaTypeInfo xmlSchemaTypeInfo, boolean b) {
		this.openElements.push(qNameToString(xmlSchemaElement.getQName()));
		//walkExtensions(xmlSchemaElement.getSchemaType());
		//System.out.println(xmlSchemaElement.getSchemaType());
		//System.out.println(xmlSchemaTypeInfo.getFacets());
		//if (xmlSchemaElement.getName().equals("elementA"))
		//	throw new RuntimeException();
	}

	@Override
	public void onExitElement(XmlSchemaElement xmlSchemaElement, XmlSchemaTypeInfo xmlSchemaTypeInfo, boolean b) {
		/*String elementName = qNameToString(xmlSchemaElement.getQName());
		BAttributeType contentType = null;
		if (xmlSchemaTypeInfo.getBaseType() != null && xmlSchemaTypeInfo.getBaseType() != ANYTYPE) {
			contentType = extractAttributeType(xmlSchemaTypeInfo.getBaseType().getQName(), elementName, null);
		}
		QName typeName = xmlSchemaElement.getSchemaTypeName();
		XSDType xsdType;
		if (!b) {
			//System.out.println(typeName +  " -> " + attributeMapping.get(typeName));
			Map<String, BAttributeType> attributeTypes = attributeMapping.get(typeName);
			if (attributeTypes != null) {
				xsdType = new XSDType(typeName, contentType, attributeTypes);
			} else {
				xsdType = new XSDType(typeName, contentType, new HashMap<>());
			}
			visitedTypes.put(typeName, xsdType);
		} else {
			xsdType = visitedTypes.get(typeName);
		}*/
		//System.out.println(elementName + ": " + xsdType.attributeTypes);
		// TODO: onEndAttributes does not work -> skips visited types
		this.openElements.pop();
		//XSDElement xsdElement = xsdType.createXSDElement(elementName, new ArrayList<>(openElements));
		//this.elements.put(elementName, xsdElement);
		//this.elementMapping.put(xmlSchemaElement, xsdElement);
	}

	@Override
	public void onVisitAttribute(XmlSchemaElement xmlSchemaElement, XmlSchemaAttrInfo xmlSchemaAttrInfo) {
		String attributeName = qNameToString(xmlSchemaAttrInfo.getAttribute().getQName());
		BAttributeType attributeType = extractAttributeType(xmlSchemaAttrInfo.getAttribute().getSchemaTypeName(),
				qNameToString(xmlSchemaElement.getQName()),
				attributeName);
		//QName typeName = xmlSchemaElement.getSchemaTypeName();
		currentAttributes.put(attributeName,attributeType);
		//System.out.println(attributeType + " " + xmlSchemaAttrInfo.getAttribute().g);
		//System.out.println(xmlSchemaElement.getQName() + " -> " + attributeType);
		/*if (attributeMapping.containsKey(typeName)) {
			attributeMapping.get(typeName).put(attributeType.getIdentifier(), attributeType);
		} else {
			Map<String, BAttributeType> attributes = new HashMap<>();
			attributes.put(attributeType.getIdentifier(), attributeType);
			attributeMapping.put(typeName, attributes);
		}*/
	}

	@Override
	public void onEndAttributes(XmlSchemaElement xmlSchemaElement, XmlSchemaTypeInfo xmlSchemaTypeInfo) {
		QName typeName = xmlSchemaElement.getSchemaTypeName();
		if (attributeMapping.containsKey(typeName)) {
			attributeMapping.get(typeName).putAll(currentAttributes);
		} else {
			attributeMapping.put(typeName, new HashMap<>(currentAttributes));
		}
		System.out.println(typeName + " -> " + currentAttributes);

		String elementName = qNameToString(xmlSchemaElement.getQName());
		BAttributeType contentType = null;
		if (xmlSchemaTypeInfo.getBaseType() != null && xmlSchemaTypeInfo.getBaseType() != ANYTYPE) {
			contentType = extractAttributeType(xmlSchemaTypeInfo.getBaseType().getQName(), elementName, null);
		}
		XSDType xsdType;
		if (!visitedTypes.containsKey(typeName)) {
			//System.out.println(typeName +  " -> " + attributeMapping.get(typeName));
			Map<String, BAttributeType> attributeTypes = new HashMap<>(currentAttributes); //attributeMapping.get(typeName);
			if (attributeTypes != null) {
				xsdType = new XSDType(typeName, contentType, attributeTypes);
			} else {
				xsdType = new XSDType(typeName, contentType, new HashMap<>());
			}
			visitedTypes.put(typeName, xsdType);
		} else {
			xsdType = visitedTypes.get(typeName);
		}
		//System.out.println(elementName + ": " + xsdType.attributeTypes);

		XSDElement xsdElement = xsdType.createXSDElement(elementName, new ArrayList<>(openElements));
		//this.elements.put(elementName, xsdElement);
		this.elementMapping.put(xmlSchemaElement, xsdElement);

		currentAttributes.clear();
	}

	@Override
	public void onEnterSubstitutionGroup(XmlSchemaElement xmlSchemaElement) {
		//XmlSchemaWalker walker = new XmlSchemaWalker(new XmlSchemaCollection(), this);
		//walker.walk(xmlSchemaElement);
	}

	@Override
	public void onExitSubstitutionGroup(XmlSchemaElement xmlSchemaElement) {

	}

	@Override
	public void onEnterAllGroup(XmlSchemaAll xmlSchemaAll) {
		//XmlSchemaWalker walker = new XmlSchemaWalker(new XmlSchemaCollection(), this);
		for (XmlSchemaAllMember member : xmlSchemaAll.getItems()) {
			if (member instanceof XmlSchemaElement) {
				//walker.walk((XmlSchemaElement) member);
			}
		}
	}

	@Override
	public void onExitAllGroup(XmlSchemaAll xmlSchemaAll) {

	}

	@Override
	public void onEnterChoiceGroup(XmlSchemaChoice xmlSchemaChoice) {
		//XmlSchemaWalker walker = new XmlSchemaWalker(new XmlSchemaCollection(), this);
		for (XmlSchemaChoiceMember member : xmlSchemaChoice.getItems()) {
			if (member instanceof XmlSchemaElement) {
				//walker.walk((XmlSchemaElement) member);
			}
		}
	}

	@Override
	public void onExitChoiceGroup(XmlSchemaChoice xmlSchemaChoice) {

	}

	@Override
	public void onEnterSequenceGroup(XmlSchemaSequence xmlSchemaSequence) {
		//XmlSchemaWalker walker = new XmlSchemaWalker(new XmlSchemaCollection(), this);
		for (XmlSchemaSequenceMember member : xmlSchemaSequence.getItems()) {
			if (member instanceof XmlSchemaElement) {
				//walker.walk((XmlSchemaElement) member);
			}
		}
	}

	@Override
	public void onExitSequenceGroup(XmlSchemaSequence xmlSchemaSequence) {

	}

	@Override
	public void onVisitAny(XmlSchemaAny xmlSchemaAny) {

	}

	@Override
	public void onVisitAnyAttribute(XmlSchemaElement xmlSchemaElement, XmlSchemaAnyAttribute xmlSchemaAnyAttribute) {

	}
}
