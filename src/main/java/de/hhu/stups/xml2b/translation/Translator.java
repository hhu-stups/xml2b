package de.hhu.stups.xml2b.translation;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.node.*;
import de.hhu.stups.xml2b.bTypes.BAttributeType;
import de.hhu.stups.xml2b.bTypes.BStringAttributeType;
import de.hhu.stups.xml2b.readXml.XMLReader;
import de.hhu.stups.xml2b.readXml.XMLElement;
import de.hhu.stups.xml2b.readXsd.XSDReader;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import static de.hhu.stups.xml2b.translation.ASTUtils.createIdentifier;
import static de.hhu.stups.xml2b.translation.ASTUtils.createIdentifierList;
import static de.hhu.stups.xml2b.translation.AbstractConstantsProvider.createAbstractConstantsClause;
import static de.hhu.stups.xml2b.translation.AbstractConstantsProvider.createAbstractConstantsProperties;

public abstract class Translator {

	public static final String XML_DATA_NAME = "XML_DATA", XML_ELEMENT_TYPES_NAME = "XML_ELEMENT_TYPES", XML_FREETYPE_ATTRIBUTES_NAME = "XML_ATTRIBUTE_TYPES",
			ID_NAME = "id", P_ID_NAME = "pId", REC_ID_NAME = "recId", TYPE_NAME = "type", ATTRIBUTES_NAME = "attributes", LOCATION_NAME = "xmlLocation";
	private final List<PMachineClause> machineClauseList = new ArrayList<>();
	protected final List<XMLElement> xmlElements;
	protected Map<String, Map<String, BAttributeType>> attributeTypes = new HashMap<>();
	protected Map<XMLElement, Map<String, PExpression>> xmlAttributes = new HashMap<>();
	protected final XSDReader xsdReader;

	private final String machineName;

	public Translator(final File xmlFile, final File xsdFile) throws BCompoundException {
		XMLReader xmlReader = new XMLReader();
		this.xmlElements = xmlReader.readXML(xmlFile, xsdFile);
		this.handleValidationErrors(xmlFile, xmlReader.getErrors());
		this.machineName = xmlFile.getName().split("\\.")[0];
		this.xsdReader = xsdFile != null ? new XSDReader(xsdFile) : null;
		this.getAttributeTypes();
	}

	private void handleValidationErrors(File xmlFile, List<XMLReader.ValidationError> errors) throws BCompoundException {
		List<BException> bExceptions = new ArrayList<>();
		for (XMLReader.ValidationError error : errors) {
			bExceptions.add(error.getBException(xmlFile.getAbsolutePath()));
		}		if (!bExceptions.isEmpty()) {
			throw new BCompoundException(bExceptions);
		}
	}

	protected abstract void getAttributeTypes();

	public Start createBAst() {
		AAbstractMachineParseUnit aAbstractMachineParseUnit = new AAbstractMachineParseUnit();
		aAbstractMachineParseUnit.setVariant(new AMachineMachineVariant());
		AMachineHeader machineHeader = new AMachineHeader();
		List<TIdentifierLiteral> headerName = ASTUtils.createTIdentifierLiteral(machineName);
		machineHeader.setName(headerName);
		aAbstractMachineParseUnit.setHeader(machineHeader);

		createFreetypeClause();
		createSetsClause();
		machineClauseList.add(createAbstractConstantsClause());
		createConstantsClause();
		createPropertyClause();

        /*createVariableClause();
        createInvariantClause();
        createInitClause();
        createOperationsClause();*/

		aAbstractMachineParseUnit.setMachineClauses(machineClauseList);
		return new Start(aAbstractMachineParseUnit, new EOF());
	}

	private void createConstantsClause() {
		AConstantsMachineClause constantsClause = new AConstantsMachineClause(createIdentifierList(XML_DATA_NAME));
		machineClauseList.add(constantsClause);
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
					new AIntegerExpression(new TIntegerLiteral(String.valueOf(xmlElement.pId())))
			));
			recValues.add(new ARecEntry(
					createIdentifier(REC_ID_NAME),
					new AIntegerExpression(new TIntegerLiteral(String.valueOf(xmlElement.recId())))
			));
			recValues.add(new ARecEntry(
					createIdentifier(TYPE_NAME),
					createIdentifier(xmlElement.elementType())
			));
			List<PExpression> attributes = new ArrayList<>();
			Map<String, BAttributeType> currentAttributes = attributeTypes.getOrDefault(xmlElement.elementType(), new HashMap<>());
			for (String attribute : xmlElement.attributes().keySet()) { // TODO: ignore attributes not present! (otherwise null)
				attributes.add(currentAttributes.getOrDefault(attribute, new BStringAttributeType(xmlElement.elementType(), attribute))
						.getDataExpression(xmlElement.attributes().get(attribute)));
			}
			recValues.add(new ARecEntry(
					createIdentifier(ATTRIBUTES_NAME),
					!attributes.isEmpty() ? new ASetExtensionExpression(attributes) : new AEmptySetExpression()
			));
			List<PExpression> startLocation = new ArrayList<>();
			startLocation.add(new AIntegerExpression(new TIntegerLiteral(String.valueOf(xmlElement.startLine()))));
			startLocation.add(new AIntegerExpression(new TIntegerLiteral(String.valueOf(xmlElement.startColumn()))));
			List<PExpression> endLocation = new ArrayList<>();
			endLocation.add(new AIntegerExpression(new TIntegerLiteral(String.valueOf(xmlElement.endLine()))));
			endLocation.add(new AIntegerExpression(new TIntegerLiteral(String.valueOf(xmlElement.endColumn()))));
			List<PExpression> locations = new ArrayList<>();
			locations.add(new ACoupleExpression(startLocation));
			locations.add(new ACoupleExpression(endLocation));
			recValues.add(new ARecEntry(
					createIdentifier(LOCATION_NAME),
					new ACoupleExpression(locations)
			));
			ARecExpression rec = new ARecExpression(recValues);
			AIntegerExpression recIndex = new AIntegerExpression(new TIntegerLiteral(String.valueOf(xmlElement.recId())));
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
		List<PFreetypeConstructor> constructors = getConstructorsForAttributes();
		AFreetype freetype = new AFreetype(new TIdentifierLiteral(XML_FREETYPE_ATTRIBUTES_NAME), new ArrayList<>(), constructors);
		AFreetypesMachineClause freetypesMachineClause = new AFreetypesMachineClause(Collections.singletonList(freetype));
		machineClauseList.add(freetypesMachineClause);
	}

	private void createSetsClause() {
		Set<String> elementTypes = new HashSet<>();
		for (XMLElement xmlElement : xmlElements) {
			elementTypes.add(xmlElement.elementType());
		}
		PSet typeSet = new AEnumeratedSetSet(ASTUtils.createTIdentifierLiteral(XML_ELEMENT_TYPES_NAME),
				elementTypes.stream().map(ASTUtils::createIdentifier).collect(Collectors.toList()));
		List<PSet> enumSets = getEnumSets();
		List<PSet> sets = new ArrayList<>();
		sets.add(typeSet);
		sets.addAll(enumSets);
		machineClauseList.add(new ASetsMachineClause(sets));
	}

	protected abstract List<PSet> getEnumSets();

	private List<PFreetypeConstructor> getConstructorsForAttributes() {
		List<PFreetypeConstructor> freetypeConstructors = new ArrayList<>();
		for (Map<String, BAttributeType> attributeTypes : attributeTypes.values()) {
			for (String attribute : attributeTypes.keySet()) {
				BAttributeType attributeType = attributeTypes.get(attribute);
				String identifier = attributeType.getIdentifier();
				if (!identifier.equals(ID_NAME)) {
					freetypeConstructors.add(new AConstructorFreetypeConstructor(
							new TIdentifierLiteral(identifier),
							attributeType.getSetExpression()
					));
				}
			}
		}
		// add generic ID constructor; TODO: check if this is correct in general
		freetypeConstructors.add(new AConstructorFreetypeConstructor(
				new TIdentifierLiteral(ID_NAME),
				new AStringSetExpression()
		));
		return freetypeConstructors;
	}
}