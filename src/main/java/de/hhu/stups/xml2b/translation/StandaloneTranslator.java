package de.hhu.stups.xml2b.translation;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.PExpression;
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
            Map<String, BAttributeType> bAttributeTypesSet = new HashMap<>();
            for (String attribute : element.attributes().keySet()) {
                BAttributeType bAttributeType = getAttribute(element.elementType(), attribute, element.attributes().get(attribute));
                if (attributeTypes.containsKey(attribute) && !attributeTypes.get(attribute).getClass().equals(bAttributeType.getClass())) {
                    // if there is at least one type mismatch -> fall back to string
                    bAttributeTypesSet.put(attribute, new BStringAttributeType(element.elementType(), attribute));
                } else {
                    bAttributeTypesSet.put(attribute, bAttributeType);
                }
            }
            attributeTypes.put(element.elementType(), bAttributeTypesSet);
            //xmlAttributes.put(element, bAttributes);
        }
    }

    @Override
    protected List<PSet> getEnumSets() {
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
}
