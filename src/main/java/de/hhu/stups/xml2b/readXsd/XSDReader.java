package de.hhu.stups.xml2b.readXsd;

import com.sun.xml.xsom.*;
import com.sun.xml.xsom.parser.XSOMParser;
import de.hhu.stups.xml2b.bTypes.BAttributeType;
import de.hhu.stups.xml2b.bTypes.BEnumSet;
import de.hhu.stups.xml2b.bTypes.BEnumSetAttributeType;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.sun.xml.xsom.XSFacet.FACET_ENUMERATION;
import static com.sun.xml.xsom.XSFacet.FACET_PATTERN;
import static de.hhu.stups.xml2b.readXsd.TypeUtils.*;

public class XSDReader {

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
	private final Map<List<String>, XSDElement> elements = new HashMap<>();
	private final Map<QName, BEnumSet> enumSets = new HashMap<>();
	private final XSSchemaSet schemaSet;

	public XSDReader(final File xsdSchema) {
		XSOMParser parser = new XSOMParser(SAXParserFactory.newInstance());
		try {
			parser.parse(xsdSchema);
			this.schemaSet = parser.getResult();
		} catch (SAXException | IOException e) {
			throw new RuntimeException(e);
		}
		this.collectEnumSets();

		for (XSSchema schema : schemaSet.getSchemas()) {
			for (XSElementDecl element : schema.getElementDecls().values()) {
				collectElementsFromElement(element);
			}
		}
	}

	public Map<List<String>, XSDElement> getElements() {
		return elements;
	}

	public Map<QName, BEnumSet> getEnumSets() {
		return enumSets;
	}

	private BAttributeType extractAttributeType(XSType type, String attributeName) {
		QName typeName = getQNameFromDeclaration(type);
		if (enumSets.containsKey(typeName)) {
			return new BEnumSetAttributeType(attributeName, enumSets.get(typeName));
		} else {
			return TypeUtils.getBAttributeType(type, attributeName);
		}
	}

	private void collectEnumSets() {
		// go over all simple types
		for (XSSchema schema : schemaSet.getSchemas()) {
			for (XSSimpleType simpleType : schema.getSimpleTypes().values()) {
				QName typeName = getQNameFromDeclaration(simpleType);
				if (simpleType.isRestriction()) {
					XSRestrictionSimpleType restriction = simpleType.asRestriction();
					BEnumSet enumSet = new BEnumSet(qNameToString(typeName), new HashSet<>());
					getEnumValuesFromFacets(restriction.iterateDeclaredFacets(), enumSet);
					if (!enumSet.getEnumValues().isEmpty() && TypeUtils.getJavaType(restriction).equals("String")) {
						if (!enumSets.containsKey(typeName)) {
							enumSets.put(typeName, enumSet);
						} else {
							enumSets.get(typeName).addValues(enumSet.getEnumValues());
						}
					}
				} else if (simpleType.isUnion()) {
					// e.g. <xs:union memberTypes="rail3:tBaliseGroupType rail3:tOtherEnumerationValue"/>
					// create new type containing values of all union types
					XSUnionSimpleType union = simpleType.asUnion();
					BEnumSet enumSet = new BEnumSet(qNameToString(typeName), new HashSet<>());
					for (int i = 0; i < union.getMemberSize(); i++) {
						collectUnionEnumSets(union.getMember(i), enumSet);
						if (!enumSet.getEnumValues().isEmpty()) { // at least one XmlSchemaEnumerationFacet should have been found
							if (!enumSets.containsKey(typeName)) {
								enumSets.put(typeName, enumSet);
							} else {
								enumSets.get(typeName).addValues(enumSet.getEnumValues());
							}
						}
					}
				}
			}
		}
	}

	private void collectUnionEnumSets(XSSimpleType type, BEnumSet enumSet) {
		if (type instanceof XSRestrictionSimpleType) {
			XSRestrictionSimpleType restriction = (XSRestrictionSimpleType) type;
			if (TypeUtils.getJavaType(restriction).equals("String")) {
				// Caution: this implicitly collects the enum values!
				if (getEnumValuesFromFacets(restriction.iterateDeclaredFacets(), enumSet))
					enumSet.setExtensible();
			}
		} else if (type instanceof XSUnionSimpleType) {
			XSUnionSimpleType union = (XSUnionSimpleType) type;
			for (int i = 0; i < union.getMemberSize(); i++) {
				collectUnionEnumSets(union.getMember(i), enumSet);
			}
		}
	}

	private static boolean getEnumValuesFromFacets(Iterator<XSFacet> facets, BEnumSet enumSet) {
		boolean extensible = false;
		while (facets.hasNext()) {
			XSFacet facet = facets.next();
			if (facet.getName().equals(FACET_ENUMERATION)) {
				// defines a list of acceptable values
				enumSet.addValue(facet.getValue().value);
			} else if (facet.getName().equals(FACET_PATTERN)) {
				// defines the exact sequence of characters that are acceptable
				extensible = true;
			}
			// all other facet types do not introduce new acceptable values, but provide bounds for length, minLength, ...
		}
		return extensible;
	}

	private void collectElementsFromElement(XSElementDecl elementDecl) {
		this.openElements.push(getQNameAsStringFromDeclaration(elementDecl));
		Map<String, BAttributeType> attributes = new HashMap<>();
		BAttributeType contentType = null;
		XSType type = elementDecl.getType();
		if (type.isComplexType()) {
			XSComplexType complexType = type.asComplexType();
			for (XSComplexType c : complexType.getSubtypes()) {
				collectElementsFromParticle(c.getContentType().asParticle());
				if (c.getExplicitContent() != null)
					collectElementsFromParticle(c.getExplicitContent().asParticle());
			}
			collectElementsFromParticle(complexType.getContentType().asParticle());
			if (complexType.getExplicitContent() != null)
				collectElementsFromParticle(complexType.getExplicitContent().asParticle());

			for (XSAttributeUse use : complexType.getAttributeUses()) {
				// TODO: default/fixed values
				XSAttributeDecl decl = use.getDecl();
				String attributeName = getQNameAsStringFromDeclaration(decl);
				BAttributeType attributeType = extractAttributeType(decl.getType(), attributeName);
				// put local name as key for later combination with read XMLElements
				attributes.put(decl.getName(), attributeType);
			}
		} else if (type.isSimpleType()) {
			//System.out.println(elementDecl.getName() + " " + type.asSimpleType().getSimpleBaseType().getName());
			contentType = extractAttributeType(type.asSimpleType(), null);
		}
		// TODO: mixed element types (content + complex)

		this.openElements.pop();
		XSDType xsdType = new XSDType(getQNameFromDeclaration(type), contentType, attributes);
		XSDElement xsdElement = xsdType.createXSDElement(getQNameAsStringFromDeclaration(elementDecl), new ArrayList<>(openElements));
		this.elements.put(xsdElement.getParentsWithThis(), xsdElement);
	}

	private void collectElementsFromParticle(XSParticle particle) {
		if (particle != null) {
			XSTerm term = particle.getTerm();
			if (term instanceof XSElementDecl) {
				collectElementsFromElement(term.asElementDecl());
			} else if (term instanceof XSModelGroup) {
				for (XSParticle childParticle : term.asModelGroup().getChildren()) {
					collectElementsFromParticle(childParticle);
				}
			}
		}
	}
}
