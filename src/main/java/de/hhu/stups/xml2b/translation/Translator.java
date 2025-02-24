package de.hhu.stups.xml2b.translation;

import de.be4.classicalb.core.parser.analysis.prolog.PrologDataPrinter;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.node.*;
import de.be4.classicalb.core.parser.util.ASTBuilder;
import de.hhu.stups.xml2b.XML2BOptions;
import de.hhu.stups.xml2b.bTypes.BAttributeType;
import de.hhu.stups.xml2b.bTypes.BStringAttributeType;
import de.hhu.stups.xml2b.readXml.XMLElement;
import de.hhu.stups.xml2b.readXml.XMLReader;
import de.hhu.stups.xml2b.readXsd.XSDReader;
import de.prob.prolog.output.FastTermOutput;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.output.PrologTermOutput;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static de.be4.classicalb.core.parser.util.ASTBuilder.*;
import static de.hhu.stups.xml2b.translation.AbstractConstantsProvider.*;

public abstract class Translator {

	public static final String XML_DATA_NAME = "XML_DATA";
	public static final String XML_FREETYPE_ATTRIBUTES_NAME = "XML_ATTRIBUTE_TYPES";
	public static final String ID_NAME = "id";
	public static final String MAX_CID_NAME = "maxCId";
	public static final String P_IDS_NAME = "pIds";
	public static final String REC_ID_NAME = "recId";
	// Element instead of element: first field in records for fast search
	public static final String ELEMENT_NAME = "Element";
	public static final String CONTENT_NAME = "content";
	public static final String ATTRIBUTES_NAME = "attributes";
	public static final String LOCATION_NAME = "xmlLocation";
	public static final String PROBDATA_SUFFIX = ".probdata";

	private final List<PMachineClause> machineClauseList = new ArrayList<>();
	private final List<String> usedIdentifiers = new ArrayList<>();

	protected final List<XMLElement> xmlElements;
	protected final Map<String, BAttributeType> allAttributeTypes = new HashMap<>();
	protected final XSDReader xsdReader;

	// translation options:
	private XML2BOptions options;

	public Translator(final File xmlFile, final File xsdFile) throws BCompoundException {
		this.options = XML2BOptions.defaultOptions(xmlFile);

		XMLReader xmlReader = new XMLReader();
		this.xmlElements = xmlReader.readXML(xmlFile, xsdFile);
		this.handleValidationErrors(xmlFile, xmlReader.getErrors());
		this.xsdReader = xsdFile != null ? new XSDReader(xsdFile) : null;
		this.getTypes();
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

	public void setCustomOptions(XML2BOptions options) {
		this.options = options;
	}

	protected abstract void getTypes();

	public Start createBAst() {
		AFileDefinitionDefinition probLibDefinition = new AFileDefinitionDefinition(new TStringLiteral("LibraryProB.def"));
		machineClauseList.add(new ADefinitionsMachineClause(Collections.singletonList(probLibDefinition)));
		AFreetypesMachineClause freetypesClause = createFreetypeClause();
		if (options.generateAbstractConstants()) {
			machineClauseList.add(createAbstractConstantsClause());
			usedIdentifiers.addAll(getIdentifiers());
		}
		createConstantsClause();

		File dataValuePrologFile = new File(String.valueOf(options.directory().resolve(options.machineName() + PROBDATA_SUFFIX)));
		PExpression dataValues = createPropertyClause(dataValuePrologFile);

		// SETS clause must be created after PROPERTIES: extensible enum sets can be extended while data is added to AST
		ASetsMachineClause setsClause = createSetsClause();

		checkForDuplicateIdentifiers();

		// TODO: check if file already exists
		try (BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(dataValuePrologFile.toPath()))) {
			IPrologTermOutput pout = options.frwPrologSystem() != null ?
					new FastTermOutput(options.frwPrologSystem(), out) : new PrologTermOutput(out, false);
			dataValues.apply(new PrologDataPrinter(pout, setsClause, freetypesClause));
			pout.fullstop();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// TODO: machineName can be different from output file name!
		AAbstractMachineParseUnit aAbstractMachineParseUnit = new AAbstractMachineParseUnit(
				new AMachineMachineVariant(),
				new AMachineHeader(createIdentifier(options.machineName()).getIdentifier(), new LinkedList<>()),
				machineClauseList
		);
		return new Start(new AGeneratedParseUnit(aAbstractMachineParseUnit), new EOF());
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

	private PExpression createPropertyClause(final File dataValuePrologFile) {
		// TYPE:
		AMemberPredicate typification = new AMemberPredicate();
		typification.setLeft(createIdentifier(XML_DATA_NAME));
		List<PRecEntry> recTypes = new ArrayList<>();
		recTypes.add(new ARecEntry(
				new TIdentifierLiteral(MAX_CID_NAME),
				new AIntegerSetExpression()
		));
		recTypes.add(new ARecEntry(
				new TIdentifierLiteral(P_IDS_NAME),
				new ASeq1Expression(new AIntegerSetExpression())
		));
		recTypes.add(new ARecEntry(
				new TIdentifierLiteral(REC_ID_NAME),
				new AIntegerSetExpression()
		));
		recTypes.add(new ARecEntry(
				new TIdentifierLiteral(ELEMENT_NAME),
				new AStringSetExpression()
		));
		recTypes.add(new ARecEntry(
				new TIdentifierLiteral(CONTENT_NAME),
				new APowSubsetExpression(createIdentifier(XML_FREETYPE_ATTRIBUTES_NAME))
		));
		recTypes.add(new ARecEntry(
				new TIdentifierLiteral(ATTRIBUTES_NAME),
				new APartialFunctionExpression(new AStringSetExpression(), createIdentifier(XML_FREETYPE_ATTRIBUTES_NAME))
		));
		recTypes.add(new ARecEntry(
				new TIdentifierLiteral(LOCATION_NAME),
				new ACartesianProductExpression(new ACartesianProductExpression(new AIntegerSetExpression(), new AIntegerSetExpression()), new ACartesianProductExpression(new AIntegerSetExpression(), new AIntegerSetExpression()))
		));
		PExpression typeExpression = new ASeqExpression(new AStructExpression(recTypes));
		typification.setRight(typeExpression.clone());

		// VALUE:
		AEqualPredicate value = new AEqualPredicate();
		value.setLeft(createIdentifier(XML_DATA_NAME));

		List<PExpression> sequenceOfRecords = new ArrayList<>();
		for (XMLElement xmlElement : xmlElements) {
			List<PRecEntry> recValues = new ArrayList<>();
			recValues.add(new ARecEntry(
					new TIdentifierLiteral(MAX_CID_NAME),
					createIntegerExpression(xmlElement.maxCId())
			));
			recValues.add(new ARecEntry(
					new TIdentifierLiteral(P_IDS_NAME),
					new ASequenceExtensionExpression(xmlElement.pIds().stream().map(ASTBuilder::createIntegerExpression).collect(Collectors.toList()))
			));
			recValues.add(new ARecEntry(
					new TIdentifierLiteral(REC_ID_NAME),
					createIntegerExpression(xmlElement.recId())
			));
			recValues.add(new ARecEntry(
					new TIdentifierLiteral(ELEMENT_NAME),
					createStringExpression(xmlElement.elementType())
			));
			// TODO: add default values for contents and attributes if not present
			// Content:
			PExpression contentExpression;
			if (xmlElement.content().isEmpty()) {
				contentExpression = new AEmptySetExpression();
			} else {
				BAttributeType type = xmlElement.typeInformation().getContentType();
				if (type == null) {
					type = new BStringAttributeType(null);
				}
				List<PExpression> contents = new ArrayList<>();
				contents.add(type.getDataExpression(xmlElement.content()));
				contentExpression = new ASetExtensionExpression(contents);
			}
			recValues.add(new ARecEntry(
					new TIdentifierLiteral(CONTENT_NAME),
					contentExpression
			));
			// Attributes:
			List<PExpression> attributes = new ArrayList<>();
			Map<String, BAttributeType> attributeTypes = xmlElement.typeInformation().getAttributeTypes();
			for (String attribute : xmlElement.attributes().keySet()) {
				if (attributeTypes.containsKey(attribute)) {
					BAttributeType type = attributeTypes.get(attribute);
					if (type == null) {
						// should never happen
						throw new IllegalStateException("identifier of attribute type does not exist");
					}
					attributes.add(type.getDataExpression(xmlElement.attributes().get(attribute)));
				}
			}
			recValues.add(new ARecEntry(
					new TIdentifierLiteral(ATTRIBUTES_NAME),
					!attributes.isEmpty() ? new ASetExtensionExpression(attributes) : new AEmptySetExpression()
			));
			List<PExpression> locations = new ArrayList<>();
			locations.add(new ACoupleExpression(Arrays.asList(createIntegerExpression(xmlElement.startLine()), createIntegerExpression(xmlElement.startColumn()))));
			locations.add(new ACoupleExpression(Arrays.asList(createIntegerExpression(xmlElement.endLine()), createIntegerExpression(xmlElement.endColumn()))));
			recValues.add(new ARecEntry(
					new TIdentifierLiteral(LOCATION_NAME),
					new ACoupleExpression(locations)
			));
			sequenceOfRecords.add(new ACoupleExpression(Arrays.asList(
					createIntegerExpression(xmlElement.recId()),
					new ARecExpression(recValues))
			));
		}
		PExpression right = !sequenceOfRecords.isEmpty() ? new ASetExtensionExpression(sequenceOfRecords) : new AEmptySetExpression();
		value.setRight(right.clone());

		PPredicate abstractConstants = createAbstractConstantsProperties();

		ADefinitionExpression readProbData = new ADefinitionExpression(
				new TIdentifierLiteral("READ_PROB_DATA_FILE"),
				Arrays.asList(typeExpression.clone(), createStringExpression(dataValuePrologFile.getName()))
		);

		APropertiesMachineClause propertiesClause = new APropertiesMachineClause(
				new AConjunctPredicate(
						new AEqualPredicate(createIdentifier(XML_DATA_NAME), readProbData),
						abstractConstants
				)
		);
		machineClauseList.add(propertiesClause);
		return right.clone();
	}

	private AFreetypesMachineClause createFreetypeClause() {
		AFreetypesMachineClause freetypesClause = new AFreetypesMachineClause(Collections.singletonList(
				new AFreetype(new TIdentifierLiteral(XML_FREETYPE_ATTRIBUTES_NAME), new ArrayList<>(), getConstructorsForAttributes())));
		machineClauseList.add(freetypesClause);
		return freetypesClause;
	}

	private ASetsMachineClause createSetsClause() {
		List<PSet> enumSets = getEnumSets(usedIdentifiers);
		ASetsMachineClause setsClause = new ASetsMachineClause(enumSets);
		if (!enumSets.isEmpty())
			machineClauseList.add(setsClause);
		return setsClause;
	}

	protected abstract List<PSet> getEnumSets(List<String> usedIdentifiers);

	private List<PFreetypeConstructor> getConstructorsForAttributes() {
		List<PFreetypeConstructor> freetypeConstructors = new ArrayList<>();
		for (BAttributeType attributeType : allAttributeTypes.values()) {
			String identifier = attributeType.getIdentifier();
			freetypeConstructors.add(new AConstructorFreetypeConstructor(
					new TIdentifierLiteral(identifier),
					attributeType.getSetExpression()
			));
			usedIdentifiers.add(identifier);
		}
		return freetypeConstructors;
	}
}