package de.hhu.stups.xml2b.translation;

import de.be4.classicalb.core.parser.node.*;
import de.hhu.stups.xml2b.bTypes.BAttribute;
import de.hhu.stups.xml2b.readXml.XMLElement;
import de.hhu.stups.xml2b.readXml.XMLReader;
import de.hhu.stups.xml2b.readXsd.XSDReader;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public abstract class Translator {

	private final List<PMachineClause> machineClauseList = new ArrayList<>();
	protected final List<XMLElement> xmlElements;
	protected Map<String, BAttribute> attributeTypes = new HashMap<>();
	protected Map<String, Map<String, BAttribute>> xmlAttributes = new HashMap<>();
	protected final XSDReader xsdReader;

	public Translator(final File xmlFile, final File xsdFile) {
		XMLReader xmlReader = new XMLReader();
		this.xmlElements = xmlReader.readXML(xmlFile);
		this.xsdReader = xsdFile != null ? new XSDReader(xsdFile) : null;
		this.getAttributeTypes();
	}

	protected abstract void getAttributeTypes();

	public Start createBAst(String machineName) {
		AAbstractMachineParseUnit aAbstractMachineParseUnit = new AAbstractMachineParseUnit();
		aAbstractMachineParseUnit.setVariant(new AMachineMachineVariant());
		AMachineHeader machineHeader = new AMachineHeader();
		List<TIdentifierLiteral> headerName = ASTUtils.createTIdentifierLiteral(machineName);
		machineHeader.setName(headerName);
		aAbstractMachineParseUnit.setHeader(machineHeader);

		createDefinitionClause();
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
		AAbstractConstantsMachineClause constantsClause = new AAbstractConstantsMachineClause(ASTUtils.createIdentifierList("getElementForId"));
		machineClauseList.add(constantsClause);
	}

	private void createConstantsClause() {
		AConstantsMachineClause constantsClause = new AConstantsMachineClause(ASTUtils.createIdentifierList("xml_data"));
		machineClauseList.add(constantsClause);
	}

	private void createPropertyClause() {
		// TYPE:
		AMemberPredicate typification = new AMemberPredicate();
		typification.setLeft(ASTUtils.createIdentifier("xml_data"));
		List<PRecEntry> recTypes = new ArrayList<>();
		recTypes.add(new ARecEntry(
				ASTUtils.createIdentifier("pId"),
				new ANaturalSetExpression()
		));
		recTypes.add(new ARecEntry(
				ASTUtils.createIdentifier("recId"),
				new ANatural1SetExpression()
		));
		recTypes.add(new ARecEntry(
				ASTUtils.createIdentifier("type"),
				ASTUtils.createIdentifier("xml_types")
		));
		recTypes.add(new ARecEntry(
				ASTUtils.createIdentifier("attributes"),
				new APowSubsetExpression(ASTUtils.createIdentifier("xml_attributes"))
		));
		typification.setRight(new ASeqExpression(new AStructExpression(recTypes)));

		// VALUE:
		AEqualPredicate value = new AEqualPredicate();
		value.setLeft(ASTUtils.createIdentifier("xml_data"));

		List<PExpression> sequenceOfRecords = new ArrayList<>();
		for (XMLElement xmlElement : xmlElements) {
			List<PRecEntry> recValues = new ArrayList<>();
			recValues.add(new ARecEntry(
					ASTUtils.createIdentifier("pId"),
					new AIntegerExpression(new TIntegerLiteral(String.valueOf(xmlElement.pId())))
			));
			recValues.add(new ARecEntry(
					ASTUtils.createIdentifier("recId"),
					new AIntegerExpression(new TIntegerLiteral(String.valueOf(xmlElement.recId())))
			));
			recValues.add(new ARecEntry(
					ASTUtils.createIdentifier("type"),
					ASTUtils.createIdentifier(xmlElement.elementType() + "_")
			));
			List<PExpression> attributes = new ArrayList<>();
			for(String attribute : xmlElement.attributes().keySet()) {
				PExpression attrValue = xmlAttributes.get(xmlElement.elementType()).get(attribute).getDataExpression();
				attributes.add(new AFunctionExpression(ASTUtils.createIdentifier(attribute), Collections.singletonList(attrValue)));
			}
			recValues.add(new ARecEntry(
					ASTUtils.createIdentifier("attributes"),
					new ASetExtensionExpression(attributes)
			));
			ARecExpression rec = new ARecExpression(recValues);
			AIntegerExpression recIndex = new AIntegerExpression(new TIntegerLiteral(String.valueOf(xmlElement.recId())));
			List<PExpression> couple = new ArrayList<>();
			couple.add(recIndex);
			couple.add(rec);
			sequenceOfRecords.add(new ACoupleExpression(couple));
		}
		value.setRight(new ASetExtensionExpression(sequenceOfRecords));

		PPredicate abstractConstants = new AEqualPredicate(new AIntegerExpression(new TIntegerLiteral("1")), new AIntegerExpression(new TIntegerLiteral("1")));//createAbstractConstantsProperties();

		APropertiesMachineClause propertiesClause = new APropertiesMachineClause(new AConjunctPredicate(abstractConstants, new AConjunctPredicate(typification, value)));
		//APropertiesMachineClause propertiesClause = new APropertiesMachineClause(value);
		machineClauseList.add(propertiesClause);
	}

	/*private PPredicate createAbstractConstantsProperties() {
		// TODO: abstractConstants?
		// typesOfSet(FTID, Set) == dom({ val, el | val |-> el : FTID & el : Set })
		AEqualPredicate getElementForId = new AEqualPredicate();
		getElementForId.setLeft(createIdentifier("getElementForId"));
		getElementForId.setRight(new AComprehensionSetExpression(
				createIdentifierList("id", "e"),
				new AExistsPredicate(
						createIdentifierList("id_val"),
						new AConjunctPredicate(
								new AMemberPredicate(
										new ACoupleExpression(
                                            createIdentifier("id_val"),
												createIdentifier("")
										)
								)
						)
				)));
		return getElementForId;
	}*/

	private void createFreetypeClause() {
		List<PFreetypeConstructor> constructors = getConstructorsForAttributes();
		AFreetype freetype = new AFreetype(new TIdentifierLiteral("xml_attributes"), new ArrayList<>(), constructors);
		AFreetypesMachineClause freetypesMachineClause = new AFreetypesMachineClause(Collections.singletonList(freetype));
		machineClauseList.add(freetypesMachineClause);
	}

	private void createSetsClause() {
		Set<String> elementTypes = new HashSet<>();
		for (XMLElement xmlElement : xmlElements) {
			elementTypes.add(xmlElement.elementType());
		}
		PSet typeSet = new AEnumeratedSetSet(ASTUtils.createTIdentifierLiteral("xml_types"),
				elementTypes.stream().map(e -> e + "_").map(ASTUtils::createIdentifier).collect(Collectors.toList()));
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
		typesOfSetDef.setParameters(ASTUtils.createIdentifierList("FTID", "Set"));
		typesOfSetDef.setRhs(new ADomainExpression(
				new AComprehensionSetExpression(
						ASTUtils.createIdentifierList("val", "el"),
						new AConjunctPredicate(
								new AMemberPredicate(
										new ACoupleExpression(ASTUtils.createIdentifierList("val", "el")),
										ASTUtils.createIdentifier("FTID")),
								new AMemberPredicate(
										ASTUtils.createIdentifier("el"),
										ASTUtils.createIdentifier("Set")
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
					new TIdentifierLiteral(attribute),
					attributeTypes.get(attribute).getSetExpression()
			));
		}
		return freetypeConstructors;
	}
}
