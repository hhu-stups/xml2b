package de.hhu.stups.xml2b.translation;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.node.*;
import de.hhu.stups.xml2b.bTypes.BAttribute;
import de.hhu.stups.xml2b.readXml.XMLReader;
import de.hhu.stups.xml2b.readXml.XMLElement;
import de.hhu.stups.xml2b.readXsd.XSDReader;
import de.hhu.stups.xml2b.translation.ASTUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static de.hhu.stups.xml2b.translation.ASTUtils.createIdentifier;
import static de.hhu.stups.xml2b.translation.ASTUtils.createIdentifierList;

public abstract class Translator {

	private static final String XML_DATA_CONSTANT_NAME = "XML_DATA", XML_ELEMENT_TYPES_NAME = "XML_ELEMENT_TYPES", XML_FREETYPE_ATTRIBUTES_NAME = "XML_ATTRIBUTE_TYPES",
			P_ID_NAME = "pId", REC_ID_NAME = "recId", TYPE_NAME = "type", ATTRIBUTES_NAME = "attributes";
	private static final String ATTRIBUTE_PREFIX = "A_", ELEMENT_PREFIX = "E_";
	public static final String XML_GET_ELEMENTS_OF_TYPE_NAME = "XML_getElementsOfType";
	private final List<PMachineClause> machineClauseList = new ArrayList<>();
	protected final List<XMLElement> xmlElements;
	protected Map<String, BAttribute> attributeTypes = new HashMap<>();
	protected Map<String, Map<String, BAttribute>> xmlAttributes = new HashMap<>();
	private final List<String> usedIdentifiers = new ArrayList<>();
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
		}
		if (!bExceptions.isEmpty()) {
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

		//createDefinitionClause();
		createFreetypeClause();
		createSetsClause();
		createAbstractConstantsClause();
		createConstantsClause();
		createPropertyClause();

        /*createVariableClause();
        createInvariantClause();
        createInitClause();
        createOperationsClause();*/

		aAbstractMachineParseUnit.setMachineClauses(machineClauseList);
		return new Start(aAbstractMachineParseUnit, new EOF());
	}

	private void createAbstractConstantsClause() {
		AAbstractConstantsMachineClause constantsClause = new AAbstractConstantsMachineClause(createIdentifierList(XML_GET_ELEMENTS_OF_TYPE_NAME));
		machineClauseList.add(constantsClause);
	}

	private void createConstantsClause() {
		AConstantsMachineClause constantsClause = new AConstantsMachineClause(createIdentifierList(XML_DATA_CONSTANT_NAME));
		machineClauseList.add(constantsClause);
	}

	private void createPropertyClause() {
		// TYPE:
		AMemberPredicate typification = new AMemberPredicate();
		typification.setLeft(createIdentifier(XML_DATA_CONSTANT_NAME));
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
		typification.setRight(new ASeqExpression(new AStructExpression(recTypes)));

		// VALUE:
		AEqualPredicate value = new AEqualPredicate();
		value.setLeft(createIdentifier(XML_DATA_CONSTANT_NAME));

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
					createIdentifier(ELEMENT_PREFIX + xmlElement.elementType())
			));
			List<PExpression> attributes = new ArrayList<>();
			Map<String, BAttribute> currentAttributes = xmlAttributes.get(xmlElement.elementType());
			for(String attribute : currentAttributes.keySet()) {
				PExpression attrValue = currentAttributes.get(attribute).getDataExpression();
				attributes.add(new AFunctionExpression(createIdentifier(ATTRIBUTE_PREFIX + attribute), Collections.singletonList(attrValue)));
			}
			recValues.add(new ARecEntry(
					createIdentifier(ATTRIBUTES_NAME),
					!attributes.isEmpty() ? new ASetExtensionExpression(attributes) : new AEmptySetExpression()
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

	private PPredicate createAbstractConstantsProperties() {
		// XML_getElementsOfType = %t.(t : XML_ELEMENT_TYPES | { e | e : ran(XML_DATA) & e'elementType = t })
		AEqualPredicate getElementsOfType = new AEqualPredicate();
		getElementsOfType.setLeft(createIdentifier(XML_GET_ELEMENTS_OF_TYPE_NAME));
		getElementsOfType.setRight(new ALambdaExpression(
				createIdentifierList("t"),
				new AMemberPredicate(
						createIdentifier("t"),
						createIdentifier(XML_ELEMENT_TYPES_NAME)
				),
				new AComprehensionSetExpression(
				createIdentifierList("e"),
				new AConjunctPredicate(
						new AMemberPredicate(
								createIdentifier("e"),
								new ARangeExpression(createIdentifier(XML_DATA_CONSTANT_NAME))
						),
						new AEqualPredicate(
								new ARecordFieldExpression(
										createIdentifier("e"),
										createIdentifier(TYPE_NAME)
								),
								createIdentifier("t")
						)
				)
		)));

		// { e, i | e : ran(XML_DATA) & e'elementType = E_netElement & #(val, el).((val,el) : A_id & el : e'attributes & i = val) }

		return getElementsOfType;
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
				elementTypes.stream().map(e -> ELEMENT_PREFIX + e).map(ASTUtils::createIdentifier).collect(Collectors.toList()));
		List<PSet> enumSets = getEnumSets();
		List<PSet> sets = new ArrayList<>();
		sets.add(typeSet);
		sets.addAll(enumSets);
		machineClauseList.add(new ASetsMachineClause(sets));
	}

	protected abstract List<PSet> getEnumSets();

	private void createDefinitionClause() {
		// TODO: abstractFunctions?
		// typesOfSet(FTID, Set) == dom({ val, el | val |-> el : FTID & el : Set })
		AExpressionDefinitionDefinition typesOfSetDef = new AExpressionDefinitionDefinition();
		typesOfSetDef.setName(new TIdentifierLiteral("typesOfSet"));
		typesOfSetDef.setParameters(createIdentifierList("FTID", "Set"));
		typesOfSetDef.setRhs(new ADomainExpression(
				new AComprehensionSetExpression(
						createIdentifierList("val", "el"),
						new AConjunctPredicate(
								new AMemberPredicate(
										new ACoupleExpression(createIdentifierList("val", "el")),
										createIdentifier("FTID")),
								new AMemberPredicate(
										createIdentifier("el"),
										createIdentifier("Set")
								)
						)
				)
		));
		// TODO: getAllAttrForType(Type) == { s, v | #e.(s|->e : xml_data & e'type = Type & v = xml_data(s)'attributes) };
		//    getAttrForType(Type, Attr) == { s, v | #e.(s|->e : xml_data & e'type = Type & v : getArgument(xml_data(s)'attributes, Attr)) };
		machineClauseList.add(new ADefinitionsMachineClause(Collections.singletonList(typesOfSetDef)));
	}

	private List<PFreetypeConstructor> getConstructorsForAttributes() {
		List<PFreetypeConstructor> freetypeConstructors = new ArrayList<>();
		for (String attribute : attributeTypes.keySet()) {
			freetypeConstructors.add(new AConstructorFreetypeConstructor(
					new TIdentifierLiteral(ATTRIBUTE_PREFIX + attribute),
					attributeTypes.get(attribute).getSetExpression()
			));
		}
		return freetypeConstructors;
	}
}
