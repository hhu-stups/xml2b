package de.hhu.stups.xml2b.translation;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.PSet;
import de.hhu.stups.xml2b.bTypes.*;
import de.hhu.stups.xml2b.readXml.XMLElement;

import java.io.File;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.*;

public class StandaloneTranslator extends Translator {

    public StandaloneTranslator(final File xmlFile) throws BCompoundException {
        super(xmlFile, null);
    }

    @Override
    protected void getAttributeTypes() {
        for (XMLElement element : xmlElements) {
            Map<String, String> bAttributeTypesSet = individualAttributeTypes.getOrDefault(element.recId(), new HashMap<>());
            for (String attribute : element.attributes().keySet()) {
                if (!attribute.equals(ID_NAME)) {
                    BAttributeType bAttributeType = getAttribute(element.elementType(), attribute, element.attributes().get(attribute));
                    determineIdentifiers(bAttributeType, allAttributeTypes);
                    bAttributeTypesSet.put(attribute, bAttributeType.getIdentifier());
                } else {
                    BAttributeType bAttributeType = new BStringAttributeType(element.elementType(), attribute);
                    allAttributeTypes.put(ID_NAME, bAttributeType);
                    bAttributeTypesSet.put(attribute, bAttributeType.getIdentifier());
                }
            }
            individualAttributeTypes.put(element.recId(), bAttributeTypesSet);
        }
    }

    @Override
    protected void getContentTypes() {
        for (XMLElement element : xmlElements) {
            String content = element.content();
            if (!content.isEmpty()) {
                BAttributeType bContentType = getContent(element.elementType(), content);
                determineIdentifiers(bContentType, allContentTypes);
                individualContentTypes.put(element.recId(), bContentType.getIdentifier());
            }
        }
    }

    private static void determineIdentifiers(BAttributeType bType, Map<String, BAttributeType> allContentTypes) {
        String identifier = bType.getIdentifier();
        if (!allContentTypes.containsKey(identifier)) {
            allContentTypes.put(identifier, bType);
        } else if (!bType.getClass().equals(allContentTypes.get(identifier).getClass())) {
            BAttributeType oldType = allContentTypes.get(identifier);
            if (oldType != null && !(oldType instanceof BStringAttributeType)) {
                oldType.addTypeSuffixToIdentifier();
                allContentTypes.put(oldType.getIdentifier(), oldType);
            }
            if (!(bType instanceof BStringAttributeType))
                bType.addTypeSuffixToIdentifier();
            String suffixIdentifier = bType.getIdentifier();
            allContentTypes.put(suffixIdentifier, bType);
            allContentTypes.put(identifier, bType.getStringAttributeType());
        }
    }

    @Override
    protected List<PSet> getEnumSets(List<String> usedIdentifiers) {
        return new ArrayList<>();
    }

    private BAttributeType getAttribute(String elementType, String attributeName, String attributeValue) {
        try {
            Duration.parse(attributeValue);
            return new BRealAttributeType(elementType, attributeName, true);
        } catch (DateTimeParseException dtpe) {
            try {
                Double.parseDouble(attributeValue);
                return new BRealAttributeType(elementType, attributeName);
            } catch (NumberFormatException nfe) {
                if (attributeValue.equals("true") || attributeValue.equals("false")) {
                    return new BBoolAttributeType(elementType, attributeName);
                }
            }
        }
        return new BStringAttributeType(elementType, attributeName);
    }

    private BAttributeType getContent(String elementType, String content) {
        try {
            Duration.parse(content);
            return new BRealAttributeType(elementType, null, true);
        } catch (DateTimeParseException dtpe) {
            try {
                Double.parseDouble(content);
                return new BRealAttributeType(elementType, null);
            } catch (NumberFormatException nfe) {
                if (content.equals("true") || content.equals("false")) {
                    return new BBoolAttributeType(elementType, null);
                }
            }
        }
        return new BStringAttributeType(elementType, null);
    }
}
