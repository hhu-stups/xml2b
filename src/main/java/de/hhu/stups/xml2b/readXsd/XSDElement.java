package de.hhu.stups.xml2b.readXsd;

import de.hhu.stups.xml2b.bTypes.BAttributeType;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class XSDElement {

	private final String qName;
	private final List<String> parents;
	private final BAttributeType contentType;
	private final Map<String, BAttributeType> attributeTypes;
	private final BigInteger minOccurs, maxOccurs;

	public XSDElement(String name, List<String> parents, BAttributeType contentType, Map<String, BAttributeType> attributeTypes) {
		this(name, parents, contentType, attributeTypes, null, null);
	}

	public XSDElement(String name, List<String> parents, BAttributeType contentType, Map<String, BAttributeType> attributeTypes,
	                  BigInteger minOccurs, BigInteger maxOccurs) {
		this.qName = name;
		this.parents = parents;
		this.contentType = contentType;
		this.attributeTypes = attributeTypes;
		this.minOccurs = minOccurs;
		this.maxOccurs = maxOccurs;
	}

	public void addAttributeType(final String name, final BAttributeType attributeType) {
		this.attributeTypes.put(name, attributeType);
	}

	public String getQName() {
		return qName;
	}

	public List<String> getParents() {
		return parents;
	}

	public List<String> getParentsWithThis() {
		List<String> parentsWithThis = new ArrayList<>(this.parents);
		parentsWithThis.add(this.qName);
		return parentsWithThis;
	}

	public BAttributeType getContentType() {
		return contentType;
	}

	public Map<String, BAttributeType> getAttributeTypes() {
		return attributeTypes;
	}

	public BigInteger getMinOccurs() {
		return minOccurs;
	}

	public BigInteger getMaxOccurs() {
		return maxOccurs;
	}

	public boolean occursExactlyOnce() {
		if (minOccurs == null || maxOccurs == null) {
			return false;
		}
		return minOccurs.equals(maxOccurs);
	}

	@Override
	public String toString() {
		return "XSDElement[name: " + qName
				+ ", parents: " + parents
				+ ", contentType: " + contentType
				+ ", attributeTypes: " + attributeTypes + "]"
				+ ", minOccurs: " + minOccurs
				+ ", maxOccurs: " + maxOccurs;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof XSDElement) {
			final XSDElement other = (XSDElement) obj;
			return this.qName.equals(other.qName) && this.parents.equals(other.parents);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(qName, parents);
	}
}
