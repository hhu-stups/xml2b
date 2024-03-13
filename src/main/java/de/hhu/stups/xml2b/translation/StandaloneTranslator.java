package de.hhu.stups.xml2b.translation;

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

    public StandaloneTranslator(final File xmlFile) {
        super(xmlFile, null);
    }

    @Override
    protected void getAttributeTypes() {
        /*for (XMLElement xmlElement : xmlElements) {
            for (String attribute : xmlElement.attributes().keySet()) {
                PExpression expression;
                Object value = xmlElement.attributes().get(attribute);
                if (value instanceof Double) {
                    expression = new ARealSetExpression();
                } else if (value instanceof Boolean) {
                    expression = new ABoolSetExpression();
                } else {
                    expression = new AStringSetExpression();
                }
                this.attributeTypes.put(attribute, expression);
            }
        }*/
        for (XMLElement element : xmlElements) {
            Map<String, BAttribute> bAttributes = new HashMap<>();
            for (String attribute : element.attributes().keySet()) {
                bAttributes.put(attribute, getAttributeObject(element.attributes().get(attribute)));
                //attributeTypes.put()
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
