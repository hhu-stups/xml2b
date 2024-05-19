package de.hhu.stups.xml2b.readXml;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class XMLElement {
	private final String elementType;
	private final List<Integer> pIds;
	private final List<String> pNames;
	private final int recId;
	private final Map<String, String> attributes;
	private final String content;
	private final int startLine, startColumn, endLine, endColumn;

	public XMLElement(String elementType, List<Integer> pIds, List<String> pNames, int recId, Map<String, String> attributes,
	                  String content, int startLine, int startColumn, int endLine, int endColumn) {
		this.elementType = elementType;
		this.pIds = pIds;
		this.pNames = pNames;
		this.recId = recId;
		this.attributes = attributes;
		this.content = content;
		this.startLine = startLine;
		this.startColumn = startColumn;
		this.endLine = endLine;
		this.endColumn = endColumn;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof XMLElement) {
			XMLElement xmlElement = (XMLElement) obj;
			return elementType.equals(xmlElement.elementType())
					&& pIds.equals(xmlElement.pIds())
					&& recId == xmlElement.recId()
					&& attributes.equals(xmlElement.attributes());
		} else {
			return false;
		}
	}

	public String elementType() {
		return elementType;
	}

	public List<Integer> pIds() {
		return pIds;
	}

	public List<String> pNames() {
		return pNames;
	}

	public int recId() {
		return recId;
	}

	public Map<String, String> attributes() {
		return attributes;
	}

	public String content() {
		return content;
	}

	public int startLine() {
		return startLine;
	}

	public int startColumn() {
		return startColumn;
	}

	public int endLine() {
		return endLine;
	}

	public int endColumn() {
		return endColumn;
	}

	@Override
	public int hashCode() {
		return Objects.hash(elementType, pIds, recId, attributes);
	}

	@Override
	public String toString() {
		return "XMLElement[" +
				"elementType=" + elementType + ", " +
				"pIds=" + pIds + ", " +
				"pNames=" + pNames + ", " +
				"recId=" + recId + ", " +
				"attributes=" + attributes + ", " +
				"content=" + content + ", " +
				"(" + startLine + "," + startColumn + ") -> (" + endLine + "," + endColumn + ")]";
	}
}
