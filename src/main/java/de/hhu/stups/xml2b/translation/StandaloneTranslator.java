package de.hhu.stups.xml2b.translation;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.PSet;
import de.hhu.stups.xml2b.bTypes.BAttribute;
import de.hhu.stups.xml2b.bTypes.BBoolAttribute;
import de.hhu.stups.xml2b.bTypes.BRealAttribute;
import de.hhu.stups.xml2b.bTypes.BStringAttribute;
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
            Map<String, BAttribute> bAttributes = new HashMap<>();
            for (String attribute : element.attributes().keySet()) {
                BAttribute bAttribute = getAttributeObject(element.attributes().get(attribute));
                bAttributes.put(attribute, bAttribute);
                if (attributeTypes.containsKey(attribute) && !attributeTypes.get(attribute).getClass().equals(bAttribute.getClass())) {
                    // if there is at least one type mismatch -> fall back to string
                    attributeTypes.put(attribute, new BStringAttribute(element.attributes().get(attribute)));
                } else {
                    attributeTypes.put(attribute, bAttribute);
                }
            }
            xmlAttributes.put(element.elementType(), bAttributes);
        }
    }

    @Override
    protected List<PSet> getEnumSets() {
        return new ArrayList<>();
    }

    private BAttribute getAttributeObject(String attribute) {
        try {
            double parsedDuration = (double) Duration.parse(attribute).withNanos(0).toMillis();
            return new BRealAttribute(Double.toString(parsedDuration));
        } catch (DateTimeParseException dtpe) {
            try {
                double parsedDouble = Double.parseDouble(attribute);
                return new BRealAttribute(Double.toString(parsedDouble));
            } catch (NumberFormatException nfe) {
                if (attribute.equals("true") || attribute.equals("false")) {
                    return new BBoolAttribute(attribute);
                }
            }
        }
        return new BStringAttribute(attribute);
    }
}
