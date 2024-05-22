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
    protected void getTypes() {
        // TODO: allow enum set extensions with other: (tOtherEnumerationValue)
        Set<String> presentAttributes = new HashSet<>();
        Map<List<String>, XSDElement> types = xsdReader.getElements();
        Set<List<String>> notPresentElements = new HashSet<>(types.keySet());
        for (XMLElement xmlElement : xmlElements) {
            presentAttributes.addAll(xmlElement.attributes().keySet());
	        XSDElement xsdElement;
            if (types.containsKey(xmlElement.pNamesWithThis())) {
                // TODO: same attribute for same xmlElement in same namespace: can this happen ? handle possible different types
                xsdElement = types.get(xmlElement.pNamesWithThis());
                Map<String, BAttributeType> attributeTypes = xsdElement.getAttributeTypes();
                attributeTypes.forEach((name, type) -> allAttributeTypes.put(type.getIdentifier(), type));
                presentAttributes.removeAll(attributeTypes.keySet());
            } else {
                // TODO: if there is content, add type!
                xsdElement = new XSDElement(xmlElement.elementType(), xmlElement.pNames(), null, new HashMap<>());
            }
            for (String attribute : presentAttributes) {
                BAttributeType bAttributeType = new BStringAttributeType(attribute);
                xsdElement.addAttributeType(attribute, bAttributeType);
                allAttributeTypes.put(bAttributeType.getIdentifier(), bAttributeType);
            }
            BAttributeType contentType = xsdElement.getContentType();
            if (contentType != null) {
                allAttributeTypes.put(contentType.getIdentifier(), contentType);
            }
            // IMPORTANT: ensure that for each xmlElement type info is added!
            xmlElement.addTypeInformation(xsdElement);
            notPresentElements.remove(xmlElement.pNamesWithThis());
        }

        for (List<String> notPresentElement : notPresentElements) {
            XSDElement xsdElement = types.get(notPresentElement);
            xsdElement.getAttributeTypes().forEach((name, type) -> allAttributeTypes.put(type.getIdentifier(), type));
            BAttributeType contentType = xsdElement.getContentType();
            if (contentType != null) {
                allAttributeTypes.put(contentType.getIdentifier(), contentType);
            }
        }
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
