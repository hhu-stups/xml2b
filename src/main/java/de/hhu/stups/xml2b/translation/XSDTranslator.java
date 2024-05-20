package de.hhu.stups.xml2b.translation;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.AEnumeratedSetSet;
import de.be4.classicalb.core.parser.node.PSet;
import de.hhu.stups.xml2b.bTypes.*;
import de.hhu.stups.xml2b.readXml.XMLElement;
import de.hhu.stups.xml2b.readXsd.XSDElement;

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
        Map<List<String>, XSDElement> types = xsdReader.getElements();
        Set<List<String>> notPresentElements = new HashSet<>(types.keySet());
        for (XMLElement element : xmlElements) {
            presentAttributes.addAll(element.attributes().keySet());
            Map<String, String> attributeIdentifierMap = new HashMap<>();
            if (types.containsKey(element.pNamesWithThis())) {
                // TODO: same attribute for same element in same namespace: can this happen ? handle possible different types
                Map<String, BAttributeType> attributeTypes = types.get(element.pNamesWithThis()).getAttributeTypes();
                Map<String, String> attributeIdentifiers = new HashMap<>();

                attributeTypes.forEach((name, type) -> {
                    attributeIdentifiers.put(name, type.getIdentifier());
                    allAttributeTypes.put(type.getIdentifier(), type);
                });

                attributeIdentifierMap.putAll(attributeIdentifiers);
                presentAttributes.removeAll(attributeTypes.keySet());
            }
            for (String attribute : presentAttributes) {
                BAttributeType bAttributeType = new BStringAttributeType(element.elementType(), attribute);
                attributeIdentifierMap.put(attribute, bAttributeType.getIdentifier());
                allAttributeTypes.put(bAttributeType.getIdentifier(), bAttributeType);
            }
            individualAttributeTypes.put(element.recId(), attributeIdentifierMap);
            notPresentElements.remove(element.pNamesWithThis());
        }

        for (List<String> notPresentElement : notPresentElements) {
            types.get(notPresentElement).getAttributeTypes().forEach((name, type) -> allAttributeTypes.put(type.getIdentifier(), type));
        }
    }

    @Override
    protected void getContentTypes() {
       // xsdReader.getContentOfElement().forEach((element, type) -> allContentTypes.put(type.getIdentifier(), type));
       // System.out.println(xsdReader.getContentOfElement());
    }

    @Override
    protected List<PSet> getEnumSets(List<String> usedIdentifiers) {
        List<PSet> enumSets = new ArrayList<>();
        for (QName enumSetId : xsdReader.getEnumSets().keySet()) {
            BEnumSet enumSet = xsdReader.getEnumSets().get(enumSetId);
            enumSets.add(new AEnumeratedSetSet(enumSet.getIdentifierExpression().getIdentifier(),
                    enumSet.getEnumValues().stream().map(ASTUtils::createIdentifier).collect(Collectors.toList())));
            usedIdentifiers.add(enumSet.getIdentifierExpression().toString().trim());
        }
        return enumSets;
    }
}
