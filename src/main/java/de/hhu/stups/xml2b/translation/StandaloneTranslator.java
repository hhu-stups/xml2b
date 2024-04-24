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
            Map<String, BAttributeType> bAttributeTypesSet = attributeTypes.getOrDefault(element.elementType(), new HashMap<>());
            for (String attribute : element.attributes().keySet()) {
                BAttributeType bAttributeType = getAttribute(element.elementType(), attribute, element.attributes().get(attribute));
                if (bAttributeTypesSet.containsKey(attribute) && !bAttributeTypesSet.get(attribute).getClass().equals(bAttributeType.getClass())) {
                    // if there is at least one type mismatch -> fall back to string
                    bAttributeTypesSet.put(attribute, new BStringAttributeType(element.elementType(), attribute));
                } else {
                    bAttributeTypesSet.put(attribute, bAttributeType);
                }
            }
            attributeTypes.put(element.elementType(), bAttributeTypesSet);
        }
    }

    @Override
    protected void getContentTypes() {
        for (XMLElement element : xmlElements) {
            String content = element.content();
            if (!content.isEmpty()) {
                BAttributeType bContentType = getContent(element.elementType(), content);
                if (contentTypes.containsKey(element.elementType()) && !contentTypes.get(element.elementType()).getClass().equals(bContentType.getClass())) {
                    // if there is at least one type mismatch -> fall back to string
                    contentTypes.put(element.elementType(), new BStringAttributeType(element.elementType(), "STRING"));
                } else {
                    contentTypes.put(element.elementType(), bContentType);
                }
            }
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
            return new BRealAttributeType(elementType, "REAL", true);
        } catch (DateTimeParseException dtpe) {
            try {
                Double.parseDouble(content);
                return new BRealAttributeType(elementType, "REAL");
            } catch (NumberFormatException nfe) {
                if (content.equals("true") || content.equals("false")) {
                    return new BBoolAttributeType(elementType, "BOOL");
                }
            }
        }
        return new BStringAttributeType(elementType, "STRING");
    }
}
