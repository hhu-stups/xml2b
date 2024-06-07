package de.hhu.stups.xml2b.translation;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.PSet;
import de.hhu.stups.xml2b.bTypes.*;
import de.hhu.stups.xml2b.readXml.XMLElement;
import de.hhu.stups.xml2b.readXsd.XSDElement;

import java.io.File;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.*;

public class StandaloneTranslator extends Translator {

    public StandaloneTranslator(final File xmlFile) throws BCompoundException {
        super(xmlFile, null);
    }

    @Override
    protected void getTypes() {
        for (XMLElement element : xmlElements) {
            element.addTypeInformation(getXsdElement(element, allAttributeTypes));
        }
    }

    protected static XSDElement getXsdElement(XMLElement element, Map<String, BAttributeType> allAttributeTypes) {
        Map<String, BAttributeType> bAttributeTypesSet = new HashMap<>();
        for (String attribute : element.attributes().keySet()) {
            BAttributeType bAttributeType = getAttribute(attribute, element.attributes().get(attribute));
            bAttributeTypesSet.put(attribute, bAttributeType);
            allAttributeTypes.put(bAttributeType.getIdentifier(), bAttributeType);
        }
        BAttributeType bContentType = null;
        String content = element.content();
        if (!content.isEmpty()) {
            bContentType = getAttribute(null, content);
            allAttributeTypes.put(bContentType.getIdentifier(), bContentType);
        }
	    return new XSDElement(element.elementType(), element.pNames(), bContentType, bAttributeTypesSet);
    }

    @Override
    protected List<PSet> getEnumSets(List<String> usedIdentifiers) {
        return new ArrayList<>();
    }

    private static BAttributeType getAttribute(String attributeName, String attributeValue) {
        try {
            Duration.parse(attributeValue);
            return new BRealAttributeType(attributeName, true);
        } catch (DateTimeParseException dtpe) {
            try {
                Double.parseDouble(attributeValue);
                return new BRealAttributeType(attributeName);
            } catch (NumberFormatException nfe) {
                if (attributeValue.equals("true") || attributeValue.equals("false")) {
                    return new BBoolAttributeType(attributeName);
                }
            }
        }
        return new BStringAttributeType(attributeName);
    }
}
