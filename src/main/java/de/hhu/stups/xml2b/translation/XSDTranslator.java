package de.hhu.stups.xml2b.translation;

import de.be4.classicalb.core.parser.node.AEnumeratedSetSet;
import de.be4.classicalb.core.parser.node.PSet;
import de.hhu.stups.xml2b.bTypes.BAttribute;
import de.hhu.stups.xml2b.bTypes.BEnumSetAttribute;
import de.hhu.stups.xml2b.bTypes.BStringAttribute;
import de.hhu.stups.xml2b.readXml.XMLElement;
import org.apache.ws.commons.schema.XmlSchemaAttribute;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class XSDTranslator extends Translator {

    public XSDTranslator(final File xmlFile, final File xsdFile) {
        super(xmlFile, xsdFile);
        XSDValidator.validateXmlForXsd(xmlFile, xsdFile);
        // TODO: abort in case of errors
    }

    @Override
    protected void getAttributeTypes() {
        Map<String, Set<XmlSchemaAttribute>> attributesOfElementName = xsdReader.getAttributesOfElementName();
        for (XMLElement element : xmlElements) {
            Map<String, String> presentAttributes = new HashMap<>(element.attributes());
            Set<XmlSchemaAttribute> xsdAttributes = attributesOfElementName.getOrDefault(element.elementType(), new HashSet<>());
            Map<String, BAttribute> bAttributes = new HashMap<>();
            for (XmlSchemaAttribute attribute : xsdAttributes) {
                String attrValue = presentAttributes.get(attribute.getName());
                if (attrValue != null) {
                    BAttribute bAttribute = xsdReader.extractAttributeType(attribute, attrValue);
                    bAttributes.put(attribute.getName(), bAttribute);
                    attributeTypes.put(attribute.getName(), bAttribute); // TODO: a bit overhead; improve later
                    presentAttributes.remove(attribute.getName());
                }
            }
            // Add attributes with default STRING type that are not declared in the schema. This can happen for header attributes like xmlns.
            for (String attribute : presentAttributes.keySet()) {
                BAttribute bAttribute = new BStringAttribute(presentAttributes.get(attribute));
                bAttributes.put(attribute, bAttribute);
                attributeTypes.put(attribute, bAttribute); // TODO: a bit overhead; improve later
            }
            xmlAttributes.put(element.elementType(), bAttributes);
        }
    }

    @Override
    protected List<PSet> getEnumSets() {
        List<PSet> enumSets = new ArrayList<>();
        for (BAttribute attribute : new HashSet<>(attributeTypes.values())) {
            if (attribute instanceof BEnumSetAttribute) {
	            BEnumSetAttribute enumSet = (BEnumSetAttribute) attribute;
	            enumSets.add(new AEnumeratedSetSet(enumSet.getIdentifier().getIdentifier(),
                        enumSet.getEnumValues().stream().map(ASTUtils::createIdentifier).collect(Collectors.toList())));
            }
        }
        return enumSets;
    }
}
