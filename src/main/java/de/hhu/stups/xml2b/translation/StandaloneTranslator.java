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
            Map<String, BAttributeType> bAttributeTypesSet = individualAttributeTypes.getOrDefault(element.recId(), new HashMap<>());
            for (String attribute : element.attributes().keySet()) {
                BAttributeType bAttributeType = getAttribute(element.elementType(), attribute, element.attributes().get(attribute));

                // TODO: cleanup and simplify
                String identifier = bAttributeType.getIdentifier();
                if (!allAttributeTypes.containsKey(identifier)) {
                    allAttributeTypes.put(identifier, bAttributeType);
                } else {
                    if (allAttributeTypes.get(identifier) instanceof BStringAttributeType && !(bAttributeType instanceof BStringAttributeType)) {
                        bAttributeType.addTypeSuffixToIdentifier();
                        allAttributeTypes.put(bAttributeType.getIdentifier(), bAttributeType);
                    } else if (!bAttributeType.getClass().equals(allAttributeTypes.get(identifier).getClass())) {
                        bAttributeType.addTypeSuffixToIdentifier();
                        if (!allAttributeTypes.containsKey(bAttributeType.getIdentifier())) {
                            allAttributeTypes.put(bAttributeType.getIdentifier(), bAttributeType);
                        }
                        BAttributeType oldType = allAttributeTypes.get(identifier);
                        if (oldType != null && !(oldType instanceof BStringAttributeType)) {
                            oldType.addTypeSuffixToIdentifier();
                            allAttributeTypes.put(oldType.getIdentifier(), oldType);
                            BAttributeType stringType = new BStringAttributeType(bAttributeType.getElementType(), bAttributeType.getAttributeName());
                            allAttributeTypes.put(stringType.getIdentifier(), stringType);
                        }

                    }
                }

                bAttributeTypesSet.put(attribute, bAttributeType);
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

                // TODO: cleanup and simplify
                String identifier = bContentType.getIdentifier();
                if (!allContentTypes.containsKey(identifier)) {
                    allContentTypes.put(identifier, bContentType);
                } else {
                    if (allContentTypes.get(identifier) instanceof BStringAttributeType && !(bContentType instanceof BStringAttributeType)) {
                        bContentType.addTypeSuffixToIdentifier();
                        allContentTypes.put(bContentType.getIdentifier(), bContentType);
                    } else if (!bContentType.getClass().equals(allContentTypes.get(identifier).getClass())) {
                        bContentType.addTypeSuffixToIdentifier();
                        if (!allContentTypes.containsKey(bContentType.getIdentifier())) {
                            allContentTypes.put(bContentType.getIdentifier(), bContentType);
                        }
                        BAttributeType oldType = allContentTypes.get(identifier);
                        if (oldType != null && !(oldType instanceof BStringAttributeType)) {
                            oldType.addTypeSuffixToIdentifier();
                            allContentTypes.put(oldType.getIdentifier(), oldType);
                            BAttributeType stringType = new BStringAttributeType(bContentType.getElementType(), bContentType.getAttributeName());
                            allContentTypes.put(stringType.getIdentifier(), stringType);
                        }

                    }
                }

                individualContentTypes.put(element.recId(), bContentType);

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
