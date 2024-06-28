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
import java.util.stream.Collectors;

import static de.hhu.stups.xml2b.readXsd.TypeUtils.qNameToString;
import static org.apache.ws.commons.schema.walker.XmlSchemaBaseSimpleType.ANYTYPE;

public class XSDReader implements XmlSchemaVisitor {

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

		@Override
		public boolean equals(Object obj) {
			return obj instanceof XSDType && this.qName.equals(((XSDType) obj).qName);
		}
	}

	private final Stack<String> openElements = new Stack<>();
	private final Map<QName, XSDType> visitedTypes = new HashMap<>();
	private final Map<List<String>, XmlSchemaElement> visitLater = new HashMap<>();
	private final Map<List<String>, XSDElement> elements = new HashMap<>();
	private final Map<String, BAttributeType> currentAttributes = new HashMap<>();
	private final Map<QName, Map<String, BAttributeType>> attributeMapping = new HashMap<>();

	private final Map<QName, XmlSchemaType> types = new HashMap<>();
	private final Map<QName, BEnumSet> enumSets = new HashMap<>();

	public XSDReader(final File xsdSchema) {
		System.setProperty("javax.xml.accessExternalDTD", "all");
		XmlSchemaCollection collection = new XmlSchemaCollection();
		XmlSchema schema = collection.read(new InputSource(xsdSchema.toURI().toString()));
		this.collectSchemaTypes(schema, new ArrayList<>());
		this.collectEnumSets(types.keySet());

		XmlSchemaWalker walker = new XmlSchemaWalker(collection, this);
		walker.setUserRecognizedTypes(types.keySet());
		for (XmlSchemaElement element : schema.getElements().values()) {
			walker.walk(element);
			walker.clear();
		}
		while (!visitLater.isEmpty()) {
			Map<List<String>, XmlSchemaElement> actualVisitLater = new HashMap<>(visitLater);
			visitLater.clear();
			for (List<String> actualOpenElements : actualVisitLater.keySet()) {
				this.openElements.clear();
				this.openElements.addAll(actualOpenElements);
				// remove the currently walked element:
				this.openElements.pop();
				walker.walk(actualVisitLater.get(actualOpenElements));
				walker.clear();
			}
		}
	}

	public Map<List<String>, XSDElement> getElements() {
		return elements;
	}

	public Map<QName, BEnumSet> getEnumSets() {
		return enumSets;
	}

	private BAttributeType extractAttributeType(QName typeName, String attributeName) {
		XmlSchemaType type = types.getOrDefault(typeName, null);
		if (enumSets.containsKey(typeName)) {
			return new BEnumSetAttributeType(attributeName, enumSets.get(typeName));
		} else if (type instanceof XmlSchemaSimpleType
				&& ((XmlSchemaSimpleType) type).getContent() instanceof XmlSchemaSimpleTypeRestriction) {
			XmlSchemaSimpleTypeRestriction restriction = (XmlSchemaSimpleTypeRestriction) ((XmlSchemaSimpleType) type).getContent();
			QName baseName = restriction.getBaseTypeName();
			if (types.containsKey(baseName)) {
				return extractAttributeType(baseName, attributeName);
			} else {
				return TypeUtils.getBAttributeType(baseName, attributeName);
			}
		} else {
			return TypeUtils.getBAttributeType(typeName, attributeName);
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
					visited.add(furtherExternalSchema);
					collectSchemaTypes(furtherExternal.getSchema(), visited);
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
				BEnumSet enumSet = new BEnumSet(qNameToString(typeName), new HashSet<>());
				getEnumValuesFromFacets(restriction.getFacets(), enumSet);
				if (!enumSet.getEnumValues().isEmpty() && TypeUtils.getJavaType(restriction.getBaseTypeName()).equals("String")) {
					if (!enumSets.containsKey(typeName)) {
						enumSets.put(typeName, enumSet);
					} else {
						enumSets.get(typeName).addValues(enumSet.getEnumValues());
					}
				}
			} else if (type instanceof XmlSchemaSimpleType
					&& ((XmlSchemaSimpleType) type).getContent() instanceof XmlSchemaSimpleTypeUnion) {
				// e.g. <xs:union memberTypes="rail3:tBaliseGroupType rail3:tOtherEnumerationValue"/>
				// create new type containing values of all union types
				XmlSchemaSimpleTypeUnion union = (XmlSchemaSimpleTypeUnion) ((XmlSchemaSimpleType) type).getContent();
				BEnumSet enumSet = new BEnumSet(qNameToString(typeName), new HashSet<>());
				Arrays.stream(union.getMemberTypesQNames())
						.forEach(qName -> {
							collectUnionEnumSets(qName, enumSet);
							if (!enumSet.getEnumValues().isEmpty()) { // at least one XmlSchemaEnumerationFacet should have been found
								if (!enumSets.containsKey(typeName)) {
									enumSets.put(typeName, enumSet);
								} else {
									enumSets.get(typeName).addValues(enumSet.getEnumValues());
								}
							}
						});
			}
		}
	}

	private void collectUnionEnumSets(QName typeName, BEnumSet enumSet) {
		XmlSchemaType type = types.getOrDefault(typeName, null);
		if (type instanceof XmlSchemaSimpleType
				&& ((XmlSchemaSimpleType) type).getContent() instanceof XmlSchemaSimpleTypeRestriction) {
			XmlSchemaSimpleTypeRestriction restriction = (XmlSchemaSimpleTypeRestriction) ((XmlSchemaSimpleType) type).getContent();
			if (TypeUtils.getJavaType(restriction.getBaseTypeName()).equals("String")) {
				// Caution: this implicitly collects the enum values!
				if (getEnumValuesFromFacets(restriction.getFacets(), enumSet))
					enumSet.setExtensible();
			}
		} else if (type instanceof XmlSchemaSimpleType
				&& ((XmlSchemaSimpleType) type).getContent() instanceof XmlSchemaSimpleTypeUnion) {
			XmlSchemaSimpleTypeUnion union = (XmlSchemaSimpleTypeUnion) ((XmlSchemaSimpleType) type).getContent();
			for (QName qName : union.getMemberTypesQNames()) {
				collectUnionEnumSets(qName, enumSet);
			}
		}
	}

	private static boolean getEnumValuesFromFacets(List<XmlSchemaFacet> facets, BEnumSet enumSet) {
		boolean extensible = false;
		for (XmlSchemaFacet facet : facets) {
			if (facet instanceof XmlSchemaEnumerationFacet) {
				// defines a list of acceptable values
				XmlSchemaEnumerationFacet enumerationFacet = (XmlSchemaEnumerationFacet) facet;
				enumSet.addValue(enumerationFacet.getValue().toString());
			} else if (facet instanceof XmlSchemaPatternFacet) {
				// defines the exact sequence of characters that are acceptable
				extensible = true;
			}
			// all other facet types do not introduce new acceptable values, but provide bounds for length, minLength, ...
		}
		return extensible;
	}

	@Override
	public void onEnterElement(XmlSchemaElement xmlSchemaElement, XmlSchemaTypeInfo xmlSchemaTypeInfo, boolean b) {
		this.openElements.push(qNameToString(xmlSchemaElement.getQName()));
	}

	@Override
	public void onExitElement(XmlSchemaElement xmlSchemaElement, XmlSchemaTypeInfo xmlSchemaTypeInfo, boolean b) {
		String elementQName = qNameToString(xmlSchemaElement.getQName());
		BAttributeType contentType = null;
		if (xmlSchemaTypeInfo.getBaseType() != null && xmlSchemaTypeInfo.getBaseType() != ANYTYPE) {
			QName baseTypeName = xmlSchemaTypeInfo.getBaseType().getQName();
			contentType = extractAttributeType(baseTypeName, null);
		}
		QName typeName = xmlSchemaElement.getSchemaTypeName();
		XSDType xsdType;
		if (!visitedTypes.containsKey(typeName)) {
			Map<String, BAttributeType> attributeTypes = attributeMapping.getOrDefault(typeName, new HashMap<>());
			xsdType = new XSDType(typeName, contentType, attributeTypes);
			visitedTypes.put(typeName, xsdType);
		} else {
			xsdType = visitedTypes.get(typeName);
		}
		if (b)
			visitLater.put(new ArrayList<>(openElements), xmlSchemaElement);
		this.openElements.pop();
		XSDElement xsdElement = xsdType.createXSDElement(elementQName, new ArrayList<>(openElements));
		this.elements.put(xsdElement.getParentsWithThis(), xsdElement);
	}

	@Override
	public void onVisitAttribute(XmlSchemaElement xmlSchemaElement, XmlSchemaAttrInfo xmlSchemaAttrInfo) {
		String attributeName = qNameToString(xmlSchemaAttrInfo.getAttribute().getQName());
		BAttributeType attributeType = extractAttributeType(xmlSchemaAttrInfo.getAttribute().getSchemaTypeName(), attributeName);
		// put local name as key for later combination with read XMLElements
		currentAttributes.put(xmlSchemaAttrInfo.getAttribute().getName(),attributeType);
	}

	@Override
	public void onEndAttributes(XmlSchemaElement xmlSchemaElement, XmlSchemaTypeInfo xmlSchemaTypeInfo) {
		QName typeName = xmlSchemaElement.getSchemaTypeName();
		if (attributeMapping.containsKey(typeName)) {
			attributeMapping.get(typeName).putAll(currentAttributes);
		} else {
			attributeMapping.put(typeName, new HashMap<>(currentAttributes));
		}
		currentAttributes.clear();
	}

	@Override
	public void onEnterSubstitutionGroup(XmlSchemaElement xmlSchemaElement) {

	}

	@Override
	public void onExitSubstitutionGroup(XmlSchemaElement xmlSchemaElement) {

	}

	@Override
	public void onEnterAllGroup(XmlSchemaAll xmlSchemaAll) {

	}

	@Override
	public void onExitAllGroup(XmlSchemaAll xmlSchemaAll) {

	}

	@Override
	public void onEnterChoiceGroup(XmlSchemaChoice xmlSchemaChoice) {

	}

	@Override
	public void onExitChoiceGroup(XmlSchemaChoice xmlSchemaChoice) {

	}

	@Override
	public void onEnterSequenceGroup(XmlSchemaSequence xmlSchemaSequence) {

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
