package de.hhu.stups.xml2b.readXml;

import java.util.Map;
import java.util.Objects;

public final class XMLElement {
	private final String elementType;
	private final int pId;
	private final int recId;
	private final Map<String, String> attributes;
	private final String content;
	private final int startLine, startColumn, endLine, endColumn;

	public XMLElement(String elementType, int pId, int recId, Map<String, String> attributes, String content,
	                  int startLine, int startColumn, int endLine, int endColumn) {
		this.elementType = elementType;
		this.pId = pId;
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
					&& pId == xmlElement.pId()
					&& recId == xmlElement.recId()
					&& attributes.equals(xmlElement.attributes());
		} else {
			return false;
		}
	}

	public String elementType() {
		return elementType;
	}

	public int pId() {
		return pId;
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
		return Objects.hash(elementType, pId, recId, attributes);
	}

	@Override
	public String toString() {
		return "XMLElement[" +
				"elementType=" + elementType + ", " +
				"pId=" + pId + ", " +
				"recId=" + recId + ", " +
				"attributes=" + attributes + ", " +
				"content=" + content + ", " +
				"(" + startLine + "," + startColumn + ") -> (" + endLine + "," + endColumn + ")]";
	}
}
