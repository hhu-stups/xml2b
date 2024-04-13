package de.hhu.stups.xml2b.translation;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.AEnumeratedSetSet;
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
    }

    @Override
    protected void getAttributeTypes() {
        // TODO: allow enum set extensions with other: (tOtherEnumerationValue)
        Set<String> presentAttributes = new HashSet<>();
        Map<String, Map<String, BAttributeType>> types = xsdReader.getAttributeTypesOfElementName();
        Set<String> notPresentElements = new HashSet<>(types.keySet());
        for (XMLElement element : xmlElements) {
            presentAttributes.addAll(element.attributes().keySet());
            Map<String, BAttributeType> attributeTypeMap = new HashMap<>();
            if (types.containsKey(element.elementType())) {
                Map<String, BAttributeType> attributeTypes = types.get(element.elementType());
                attributeTypeMap.putAll(attributeTypes);
                presentAttributes.removeAll(attributeTypes.keySet());
            }
            for (String attribute : presentAttributes) {
                BAttributeType bAttributeType = new BStringAttributeType(element.elementType(), attribute);
                attributeTypeMap.put(attribute, bAttributeType);
            }
            attributeTypes.put(element.elementType(), attributeTypeMap);
            notPresentElements.remove(element.elementType());
        }
        for (String notPresentElement : notPresentElements) {
            attributeTypes.put(notPresentElement, types.get(notPresentElement));
        }
    }

    @Override
    protected List<PSet> getEnumSets(List<String> usedIdentifiers) {
        List<PSet> enumSets = new ArrayList<>();
        for (QName enumSetId : xsdReader.getEnumSets().keySet()) {
            BEnumSet enumSet = xsdReader.getEnumSets().get(enumSetId);
            enumSets.add(new AEnumeratedSetSet(enumSet.getIdentifier().getIdentifier(),
                    enumSet.getEnumValues().stream().map(ASTUtils::createIdentifier).collect(Collectors.toList())));
            usedIdentifiers.add(enumSet.getIdentifier().toString());
        }
        return enumSets;
    }
}
