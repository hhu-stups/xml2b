package de.hhu.stups.xml2b.readXml;

import java.util.Map;

public record XMLElement(String elementType, int pId, int recId, Map<String, String> attributes) {
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof XMLElement xmlElement) {
            return elementType.equals(xmlElement.elementType())
                    && pId == xmlElement.pId()
                    && recId == xmlElement.recId()
                    && attributes.equals(xmlElement.attributes());
        } else {
            return false;
        }
    }
}
