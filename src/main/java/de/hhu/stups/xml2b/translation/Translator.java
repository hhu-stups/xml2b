package de.hhu.stups.xml2b.translation;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.node.*;
import de.hhu.stups.xml2b.bTypes.BAttribute;
import de.hhu.stups.xml2b.bTypes.BAttributeType;
import de.hhu.stups.xml2b.bTypes.BStringAttribute;
import de.hhu.stups.xml2b.readXml.XMLReader;
import de.hhu.stups.xml2b.readXml.XMLElement;
import de.hhu.stups.xml2b.readXsd.XSDReader;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static de.hhu.stups.xml2b.translation.ASTUtils.createIdentifier;
import static de.hhu.stups.xml2b.translation.ASTUtils.createIdentifierList;

public abstract class Translator {

	public static final String XML_DATA_NAME = "XML_DATA", XML_ELEMENT_TYPES_NAME = "XML_ELEMENT_TYPES", XML_FREETYPE_ATTRIBUTES_NAME = "XML_ATTRIBUTE_TYPES",
			ID_NAME = "id", P_ID_NAME = "pId", REC_ID_NAME = "recId", TYPE_NAME = "type", ATTRIBUTES_NAME = "attributes", LOCATION_NAME = "xmlLocation";
	public static final String XML_GET_ELEMENTS_OF_TYPE_NAME = "XML_getElementsOfType", XML_GET_ELEMENT_OF_ID_NAME = "XML_getElementOfId",
			XML_GET_CHILDS_NAME = "XML_getChilds", XML_GET_CHILDS_OF_TYPE_NAME = "XML_getChildsOfType", XML_GET_ID_OF_ELEMENT_NAME = "XML_getIdOfElement", XML_ALL_IDS_OF_TYPE_NAME = "XML_allIdsOfType";
	private final List<PMachineClause> machineClauseList = new ArrayList<>();
	protected final List<XMLElement> xmlElements;
	protected Map<String, Set<BAttributeType>> attributeTypes = new HashMap<>();
	protected Map<XMLElement, Map<String, BAttribute>> xmlAttributes = new HashMap<>();
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

		createFreetypeClause();
		createSetsClause();
		createAbstractConstantsClause();
		createConstantsClause();
		createPropertyClause();

        /*createVariableClause();        createInvariantClause();
        createInitClause();
        createOperationsClause();*/

		aAbstractMachineParseUnit.setMachineClauses(machineClauseList);
		return new Start(aAbstractMachineParseUnit, new EOF());
	}

	private void createAbstractConstantsClause() {
		List<PExpression> identifiers = createIdentifierList(XML_GET_ELEMENTS_OF_TYPE_NAME, XML_GET_ELEMENT_OF_ID_NAME, XML_GET_CHILDS_NAME, XML_GET_CHILDS_OF_TYPE_NAME,
				XML_GET_ID_OF_ELEMENT_NAME, XML_ALL_IDS_OF_TYPE_NAME);
		// activate memoization for all abstract constants
		identifiers = identifiers.stream().map(identifier -> new ADescriptionExpression(new TPragmaFreeText("memo"), identifier)).collect(Collectors.toList());
		AAbstractConstantsMachineClause constantsClause = new AAbstractConstantsMachineClause(identifiers);
		machineClauseList.add(constantsClause);
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
			Map<String, BAttribute> currentAttributes = xmlAttributes.get(xmlElement);
			for(String attribute : currentAttributes.keySet()) {
				PExpression attrValue = currentAttributes.get(attribute).getDataExpression();
				// id attribute is XML standard and should not be considered individually for each element type
				String identifier = attribute.equals(ID_NAME) ? attribute : xmlElement.elementType() + ":" + attribute;
				attributes.add(new AFunctionExpression(createIdentifier(identifier), Collections.singletonList(attrValue)));
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
									new ARangeExpression(createIdentifier(XML_DATA_NAME))
							),
							new AEqualPredicate(
									new ARecordFieldExpression(
											createIdentifier("e"),
											createIdentifier(TYPE_NAME)
									),
									createIdentifier("t")
							)
					)
				)
		));

		// XML_getElementOfId = %(i).(i : dom(A_id) | dom({ e, el | e : ran(XML_DATA) & (i,el) : A_id & el : e'attributes }))
		AEqualPredicate getElementOfId = new AEqualPredicate();
		getElementOfId.setLeft(createIdentifier(XML_GET_ELEMENT_OF_ID_NAME));
		getElementOfId.setRight(new ALambdaExpression(
				createIdentifierList("i"),
				new AMemberPredicate(
						createIdentifier("i"),
						new ADomainExpression(createIdentifier(ID_NAME))
				),
				new ADomainExpression(
						new AComprehensionSetExpression(
								createIdentifierList("e", "el"),
								new AConjunctPredicate(
										new AMemberPredicate(
												createIdentifier("e"),
												new ARangeExpression(createIdentifier(XML_DATA_NAME))
										),
										new AConjunctPredicate(
												new AMemberPredicate(
														new ACoupleExpression(createIdentifierList("i","el")),
														createIdentifier(ID_NAME)
												),
												new AMemberPredicate(
														createIdentifier("el"),
														new ARecordFieldExpression(
																createIdentifier("e"),
																createIdentifier(ATTRIBUTES_NAME)
														)
												)
										)
								)
						)
				)
		));

		// XML_getChilds = %(e).(e : ran(XML_DATA) | { c | c : ran(XML_DATA) & c'pId = e'recId })
		AEqualPredicate getChilds = new AEqualPredicate();
		getChilds.setLeft(createIdentifier(XML_GET_CHILDS_NAME));
		getChilds.setRight(new ALambdaExpression(
				createIdentifierList("e"),
				new AMemberPredicate(
						createIdentifier("e"),
						new ARangeExpression(createIdentifier(XML_DATA_NAME))
				),
				new AComprehensionSetExpression(
						createIdentifierList("c"),
						new AConjunctPredicate(
								new AMemberPredicate(
										createIdentifier("c"),
										new ARangeExpression(createIdentifier(XML_DATA_NAME))
								),
								new AEqualPredicate(
										new ARecordFieldExpression(
												createIdentifier("c"),
												createIdentifier(P_ID_NAME)
										),
										new ARecordFieldExpression(
												createIdentifier("e"),
												createIdentifier(REC_ID_NAME)
										)
								)
						)
				)
		));

		// XML_getChildsOfType = %(e,t).(e : ran(XML_DATA) & t : XML_ELEMENT_TYPES | { c | c : ran(XML_DATA) & c'pId = e'recId & c'type = t })
		AEqualPredicate getChildsOfType = new AEqualPredicate();
		getChildsOfType.setLeft(createIdentifier(XML_GET_CHILDS_OF_TYPE_NAME));
		getChildsOfType.setRight(new ALambdaExpression(
				createIdentifierList("e","t"),
				new AConjunctPredicate(
						new AMemberPredicate(
								createIdentifier("e"),
								new ARangeExpression(createIdentifier(XML_DATA_NAME))
						),
						new AMemberPredicate(
								createIdentifier("t"),
								createIdentifier(XML_ELEMENT_TYPES_NAME)
						)
				),
				new AComprehensionSetExpression(
						createIdentifierList("c"),
						new AConjunctPredicate(
								new AMemberPredicate(
										createIdentifier("c"),
										new ARangeExpression(createIdentifier(XML_DATA_NAME))
								),
								new AConjunctPredicate(
										new AEqualPredicate(
												new ARecordFieldExpression(
														createIdentifier("c"),
														createIdentifier(P_ID_NAME)
												),
												new ARecordFieldExpression(
														createIdentifier("e"),
														createIdentifier(REC_ID_NAME)
												)
										),
										new AEqualPredicate(
												new ARecordFieldExpression(
														createIdentifier("c"),
														createIdentifier(TYPE_NAME)
												),
												createIdentifier("t")
										)
								)
						)
				)
		));

		// XML_getIdOfElement = %(e).(e : ran(XML_DATA) | { i | `id`(i) : e'attributes })
		AEqualPredicate getIdOfElement = new AEqualPredicate();
		getIdOfElement.setLeft(createIdentifier(XML_GET_ID_OF_ELEMENT_NAME));
		getIdOfElement.setRight(new ALambdaExpression(
				createIdentifierList("e"),
				new AMemberPredicate(
						createIdentifier("e"),
						new ARangeExpression(createIdentifier(XML_DATA_NAME))
				),
				new AComprehensionSetExpression(
						createIdentifierList("i"),
						new AConjunctPredicate(
								new AMemberPredicate(
										createIdentifier("i"),
										new AStringSetExpression()
								),
								new AMemberPredicate(
										new AFunctionExpression(
												createIdentifier(ID_NAME),
												createIdentifierList("i")
										),
										new ARecordFieldExpression(
												createIdentifier("e"),
												createIdentifier(ATTRIBUTES_NAME)
										)
								)
						)
				)
		));

		// XML_allIdsOfType = %(t).(t : XML_ELEMENT_TYPES | dom({ i,e | e : ran(XML_DATA) & e'type = t & `id`(i) : e'attributes }))
		AEqualPredicate allIdsOfType = new AEqualPredicate();
		allIdsOfType.setLeft(createIdentifier(XML_ALL_IDS_OF_TYPE_NAME));
		allIdsOfType.setRight(new ALambdaExpression(
				createIdentifierList("t"),
				new AMemberPredicate(
						createIdentifier("t"),
						createIdentifier(XML_ELEMENT_TYPES_NAME)
				),
				new ADomainExpression(
					new AComprehensionSetExpression(
							createIdentifierList("i","e"),
							new AConjunctPredicate(
									new AMemberPredicate(
											createIdentifier("e"),
											new ARangeExpression(createIdentifier(XML_DATA_NAME))
									),
									new AConjunctPredicate(
											new AEqualPredicate(
													new ARecordFieldExpression(
															createIdentifier("e"),
															createIdentifier(TYPE_NAME)
													),
													createIdentifier("t")
											),
											new AMemberPredicate(
													new AFunctionExpression(
															createIdentifier(ID_NAME),
															createIdentifierList("i")
													),
													new ARecordFieldExpression(
															createIdentifier("e"),
															createIdentifier(ATTRIBUTES_NAME)
													)
											)
									)
							)
					)
				)
		));

		return new AConjunctPredicate(
				getElementsOfType,
				new AConjunctPredicate(
						getElementOfId,
						new AConjunctPredicate(
								getChilds,
								new AConjunctPredicate(
										getChildsOfType,
										new AConjunctPredicate(
												getIdOfElement,
												allIdsOfType
										)

								)
						)
				)
		);
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
		for (Set<BAttributeType> attributeTypes : attributeTypes.values()) {
			for (BAttributeType attributeType : attributeTypes) {
				String identifier = attributeType.getIdentifier();
				freetypeConstructors.add(new AConstructorFreetypeConstructor(
						new TIdentifierLiteral(identifier),
						attributeType.getSetExpression()
				));
			}
		}
		return freetypeConstructors;
	}
}
