package de.hhu.stups.xml2b.translation;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.AEnumeratedSetSet;
import de.be4.classicalb.core.parser.node.PExpression;
import de.be4.classicalb.core.parser.node.PSet;
import de.hhu.stups.xml2b.bTypes.*;
import de.hhu.stups.xml2b.readXml.XMLElement;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class XSDTranslator extends Translator {

    public XSDTranslator(final File xmlFile, final File xsdFile) throws BCompoundException {
        super(xmlFile, xsdFile);
        // TODO: abort in case of errors
    }

    @Override
    protected void getAttributeTypes() {
        // TODO: name enum sets after their schema name - otherwise they conflict (e.g. type in railML)
        // TODO: allow enum set extensions with other: (tOtherEnumerationValue)
        //Map<String, Set<XmlSchemaAttribute>> attributesOfElementName = xsdReader.getAttributesOfElementName();
        Set<String> presentAttributes = new HashSet<>();
        Map<String, Map<String, BAttributeType>> types = xsdReader.getAttributeTypesOfElementName();
        for (XMLElement element : xmlElements) {
            presentAttributes.addAll(element.attributes().keySet());
            Map<String, BAttributeType> attributeTypeMap = new HashMap<>();
            if (types.containsKey(element.elementType())) {
                Map<String, BAttributeType> attributeTypes = types.get(element.elementType());
                attributeTypeMap.putAll(attributeTypes);
                presentAttributes.removeAll(attributeTypes.keySet());
            } else {
                for (String attribute : presentAttributes) {
                    BAttributeType bAttributeType = new BStringAttributeType(element.elementType(), attribute);
                    attributeTypeMap.put(attribute, bAttributeType);
                }
            }

            attributeTypes.put(element.elementType(), attributeTypeMap);
        }
        // add actual values:
        /*for (XMLElement element : xmlElements) {
            Map<String, String> presentAttributes = new HashMap<>(element.attributes());
            Set<BAttributeType> xsdAttributes = types.getOrDefault(element.elementType(), new HashSet<>());
            Map<String, PExpression> bAttributes = new HashMap<>();
            for (BAttributeType attribute : xsdAttributes) {
                String attrName = attribute.getAttributeName();
                String attrValue = presentAttributes.get(attrName);
                if (attrValue != null) {
                    PExpression bAttributeType = attribute.getDataExpression(attrValue);
                    bAttributes.put(attrName, bAttributeType);
                    //attributeTypes.put(attribute.getName(), bAttribute); // TODO: a bit overhead; improve later
                    presentAttributes.remove(attrName);
                }
            }
            // Add attributes with default STRING type that are not declared in the schema. This can happen for header attributes like xmlns.
            for (String attribute : presentAttributes.keySet()) {
                BAttributeType bAttributeType = new BStringAttributeType(element.elementType(), attribute);
                bAttributes.put(attribute, bAttributeType.getDataExpression(presentAttributes.get(attribute)));
                //attributeTypes.put(attribute, bAttribute); // TODO: a bit overhead; improve later
            }
            xmlAttributes.put(element, bAttributes);
            //attributesOfElementName.remove(element.elementType());
        }*/
        // add
        //attributeTypes.putAll(types);
        /*for (Set<XmlSchemaAttribute> attributes : attributesOfElementName.values()) {
            for (XmlSchemaAttribute attribute : attributes) {
                BAttribute bAttribute = xsdReader.extractAttributeType(attribute, null);
                attributeTypes.put(attribute.getName(), bAttribute); // TODO: a bit overhead; improve later
            }
        }*/
    }

    @Override
    protected List<PSet> getEnumSets() {
        List<PSet> enumSets = new ArrayList<>();
        for (QName enumSetId : xsdReader.getEnumSets().keySet()) {
            BEnumSet enumSet = xsdReader.getEnumSets().get(enumSetId);
            enumSets.add(new AEnumeratedSetSet(enumSet.getIdentifier().getIdentifier(),
                    enumSet.getEnumValues().stream().map(ASTUtils::createIdentifier).collect(Collectors.toList())));
        }
        return enumSets;
    }
}
