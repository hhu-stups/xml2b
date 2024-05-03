package de.hhu.stups.xml2b.translation;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.node.*;
import de.hhu.stups.xml2b.bTypes.BAttributeType;
import de.hhu.stups.xml2b.readXml.XMLElement;
import de.hhu.stups.xml2b.readXml.XMLReader;
import de.hhu.stups.xml2b.readXsd.XSDReader;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static de.hhu.stups.xml2b.translation.ASTUtils.*;
import static de.hhu.stups.xml2b.translation.AbstractConstantsProvider.*;

public abstract class Translator {

	public static final String XML_DATA_NAME = "XML_DATA", XML_ELEMENT_TYPES_NAME = "XML_ELEMENT_TYPES", XML_FREETYPE_ATTRIBUTES_NAME = "XML_ATTRIBUTE_TYPES", XML_CONTENT_TYPES_NAME = "XML_CONTENT_TYPES",
			ID_NAME = "id", P_ID_NAME = "pId", REC_ID_NAME = "recId", TYPE_NAME = "type", CONTENT_NAME = "content", ATTRIBUTES_NAME = "attributes", LOCATION_NAME = "xmlLocation";
	private final List<PMachineClause> machineClauseList = new ArrayList<>();
	protected final List<XMLElement> xmlElements;
	// individualAttributeTypes: elementRecId -> (attributeName -> bAttributeType)
	protected Map<Integer, Map<String, BAttributeType>> individualAttributeTypes = new HashMap<>();
	protected Map<String, BAttributeType> allAttributeTypes = new HashMap<>();
	// individualContentTypes: elementRecId -> bAttributeType
	protected Map<Integer, BAttributeType> individualContentTypes = new HashMap<>();
	protected Map<String, BAttributeType> allContentTypes = new HashMap<>();
	protected final XSDReader xsdReader;

	private final String machineName;
	private final List<String> usedIdentifiers = new ArrayList<>();

	public Translator(final File xmlFile, final File xsdFile) throws BCompoundException {
		XMLReader xmlReader = new XMLReader();
		this.xmlElements = xmlReader.readXML(xmlFile, xsdFile);
		this.handleValidationErrors(xmlFile, xmlReader.getErrors());
		this.machineName = xmlFile.getName().split("\\.")[0];
		this.xsdReader = xsdFile != null ? new XSDReader(xsdFile) : null;
		this.getAttributeTypes();
		this.getContentTypes();
	}

	private void handleValidationErrors(File xmlFile, List<XMLReader.ValidationError> errors) throws BCompoundException {
		List<BException> bExceptions = new ArrayList<>();
		for (XMLReader.ValidationError error : errors) {
			bExceptions.add(error.getBException(xmlFile.getAbsolutePath()));
		}
		if (!bExceptions.isEmpty()) {
			throw new BCompoundException(bExceptions);
		}
	}

	protected abstract void getAttributeTypes();

	protected abstract void getContentTypes();

	public Start createBAst() {
		AGeneratedParseUnit aGeneratedParseUnit = new AGeneratedParseUnit();
		AAbstractMachineParseUnit aAbstractMachineParseUnit = new AAbstractMachineParseUnit();
		aAbstractMachineParseUnit.setVariant(new AMachineMachineVariant());
		AMachineHeader machineHeader = new AMachineHeader();
		List<TIdentifierLiteral> headerName = ASTUtils.createTIdentifierLiteral(machineName);
		machineHeader.setName(headerName);
		aAbstractMachineParseUnit.setHeader(machineHeader);

		createFreetypeClause();
		createSetsClause();
		machineClauseList.add(createAbstractConstantsClause());
		usedIdentifiers.addAll(getIdentifiers());
		createConstantsClause();
		createPropertyClause();

		checkForDuplicateIdentifiers();

		aAbstractMachineParseUnit.setMachineClauses(machineClauseList);
		aGeneratedParseUnit.setParseUnit(aAbstractMachineParseUnit);
		return new Start(aGeneratedParseUnit, new EOF());
	}

	private void checkForDuplicateIdentifiers() {
		Set<String> setOfUsedIdentifiers = new HashSet<>();
		Set<String> duplicates = new HashSet<>();
		for (String identifier : usedIdentifiers) {
			if (!setOfUsedIdentifiers.add(identifier)) {
				duplicates.add(identifier);
			}
		}
		if (setOfUsedIdentifiers.size() != usedIdentifiers.size()) {
			throw new RuntimeException("Duplicate identifiers found in generated AST: " + duplicates);
		}
	}

	private void createConstantsClause() {
		AConstantsMachineClause constantsClause = new AConstantsMachineClause(createIdentifierList(XML_DATA_NAME));
		machineClauseList.add(constantsClause);
		usedIdentifiers.add(XML_DATA_NAME);
	}

	private void createPropertyClause() {
		// TYPE:
		AMemberPredicate typification = new AMemberPredicate();
		typification.setLeft(createIdentifier(XML_DATA_NAME));
		List<PRecEntry> recTypes = new ArrayList<>();
		recTypes.add(new ARecEntry(
				createIdentifier(P_ID_NAME),
				new ANaturalSetExpression()
		));
		recTypes.add(new ARecEntry(
				createIdentifier(REC_ID_NAME),
				new ANatural1SetExpression()
		));
		recTypes.add(new ARecEntry(
				createIdentifier(TYPE_NAME),
				createIdentifier(XML_ELEMENT_TYPES_NAME)
		));
		recTypes.add(new ARecEntry(
				createIdentifier(CONTENT_NAME),
				new APowSubsetExpression(createIdentifier(XML_CONTENT_TYPES_NAME))
		));
		recTypes.add(new ARecEntry(
				createIdentifier(ATTRIBUTES_NAME),
				new APowSubsetExpression(createIdentifier(XML_FREETYPE_ATTRIBUTES_NAME))
		));
		recTypes.add(new ARecEntry(
				createIdentifier(LOCATION_NAME),
				new ACartesianProductExpression(new ACartesianProductExpression(new ANatural1SetExpression(), new ANatural1SetExpression()), new ACartesianProductExpression(new ANatural1SetExpression(), new ANatural1SetExpression()))
		));
		typification.setRight(new ASeqExpression(new AStructExpression(recTypes)));

		// VALUE:
		AEqualPredicate value = new AEqualPredicate();
		value.setLeft(createIdentifier(XML_DATA_NAME));

		List<PExpression> sequenceOfRecords = new ArrayList<>();
		for (XMLElement xmlElement : xmlElements) {
			List<PRecEntry> recValues = new ArrayList<>();
			recValues.add(new ARecEntry(
					createIdentifier(P_ID_NAME),
					createInteger(xmlElement.pId())
			));
			recValues.add(new ARecEntry(
					createIdentifier(REC_ID_NAME),
					createInteger(xmlElement.recId())
			));
			recValues.add(new ARecEntry(
					createIdentifier(TYPE_NAME),
					createIdentifier(xmlElement.elementType())
			));
			recValues.add(new ARecEntry(
					createIdentifier(CONTENT_NAME),
					xmlElement.content().isEmpty() ? new AEmptySetExpression() : new ASetExtensionExpression(Collections.singletonList(individualContentTypes.get(xmlElement.recId()).getDataExpression(xmlElement.content())))
			));
			List<PExpression> attributes = new ArrayList<>();
			Map<String, BAttributeType> currentAttributes = individualAttributeTypes.getOrDefault(xmlElement.recId(), new HashMap<>());
			for (String attribute : xmlElement.attributes().keySet()) { // TODO: ignore attributes not present! (otherwise null)
				if (currentAttributes.containsKey(attribute)) {
					BAttributeType type = currentAttributes.get(attribute);
					attributes.add(type.getDataExpression(xmlElement.attributes().get(attribute)));
					if (type.hasTypeSuffix()) {
						attributes.add(type.getStringAttributeType().getDataExpression(xmlElement.attributes().get(attribute)));
					}
				}
			}
			recValues.add(new ARecEntry(
					createIdentifier(ATTRIBUTES_NAME),
					!attributes.isEmpty() ? new ASetExtensionExpression(attributes) : new AEmptySetExpression()
			));
			List<PExpression> startLocation = new ArrayList<>();
			startLocation.add(createInteger(xmlElement.startLine()));
			startLocation.add(createInteger(xmlElement.startColumn()));
			List<PExpression> endLocation = new ArrayList<>();
			endLocation.add(createInteger(xmlElement.endLine()));
			endLocation.add(createInteger(xmlElement.endColumn()));
			List<PExpression> locations = new ArrayList<>();
			locations.add(new ACoupleExpression(startLocation));
			locations.add(new ACoupleExpression(endLocation));
			recValues.add(new ARecEntry(
					createIdentifier(LOCATION_NAME),
					new ACoupleExpression(locations)
			));
			ARecExpression rec = new ARecExpression(recValues);
			AIntegerExpression recIndex = createInteger(xmlElement.recId());
			List<PExpression> couple = new ArrayList<>();
			couple.add(recIndex);
			couple.add(rec);
			sequenceOfRecords.add(new ACoupleExpression(couple));
		}
		value.setRight(!sequenceOfRecords.isEmpty() ? new ASetExtensionExpression(sequenceOfRecords) : new AEmptySetExpression());

		PPredicate abstractConstants = createAbstractConstantsProperties();
		APropertiesMachineClause propertiesClause = new APropertiesMachineClause(new AConjunctPredicate(abstractConstants, new AConjunctPredicate(typification, value)));
		machineClauseList.add(propertiesClause);
	}

	private void createFreetypeClause() {
		List<PFreetypeConstructor> contentConstructors = getConstructorsForContents();
		List<PFreetype> freetypes = new ArrayList<>();
		freetypes.add(new AFreetype(new TIdentifierLiteral(XML_FREETYPE_ATTRIBUTES_NAME), new ArrayList<>(), getConstructorsForAttributes()));
		if (!contentConstructors.isEmpty())
			freetypes.add(new AFreetype(new TIdentifierLiteral(XML_CONTENT_TYPES_NAME), new ArrayList<>(), contentConstructors));
		machineClauseList.add(new AFreetypesMachineClause(freetypes));
	}

	private void createSetsClause() {
		Set<String> elementTypes = new HashSet<>();
		for (XMLElement xmlElement : xmlElements) {
			elementTypes.add(xmlElement.elementType());
		}
		usedIdentifiers.addAll(elementTypes);
		PSet typeSet = new AEnumeratedSetSet(ASTUtils.createTIdentifierLiteral(XML_ELEMENT_TYPES_NAME),
				elementTypes.stream().map(ASTUtils::createIdentifier).collect(Collectors.toList()));
		List<PSet> enumSets = getEnumSets(usedIdentifiers);
		List<PSet> sets = new ArrayList<>();
		sets.add(typeSet);
		sets.addAll(enumSets);
		machineClauseList.add(new ASetsMachineClause(sets));
	}

	protected abstract List<PSet> getEnumSets(List<String> usedIdentifiers);

	private List<PFreetypeConstructor> getConstructorsForAttributes() {
		List<PFreetypeConstructor> freetypeConstructors = new ArrayList<>();
		for (BAttributeType attributeType : allAttributeTypes.values()) {
			String identifier = attributeType.getIdentifier();
			if (!identifier.equals(ID_NAME)) {
				freetypeConstructors.add(new AConstructorFreetypeConstructor(
						new TIdentifierLiteral(identifier),
						attributeType.getSetExpression()
				));
				usedIdentifiers.add(identifier);
			}
		}
		// add generic ID constructor; TODO: check if this is correct in general
		freetypeConstructors.add(new AConstructorFreetypeConstructor(
				new TIdentifierLiteral(ID_NAME),
				new AStringSetExpression()
		));
		usedIdentifiers.add(ID_NAME);
		return freetypeConstructors;
	}

	private List<PFreetypeConstructor> getConstructorsForContents() {
		List<PFreetypeConstructor> freetypeConstructors = new ArrayList<>();
		for (BAttributeType contentType : allContentTypes.values()) {
			String identifier = contentType.getIdentifier();
			freetypeConstructors.add(new AConstructorFreetypeConstructor(
					new TIdentifierLiteral(identifier),
					contentType.getSetExpression()
			));
			usedIdentifiers.add(identifier);
		}
		return freetypeConstructors;
	}
}