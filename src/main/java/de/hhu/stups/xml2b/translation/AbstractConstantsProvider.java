package de.hhu.stups.xml2b.translation;

import de.be4.classicalb.core.parser.node.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static de.hhu.stups.xml2b.translation.ASTUtils.*;
import static de.hhu.stups.xml2b.translation.Translator.*;

public class AbstractConstantsProvider {

	private static final String XML_GET_ELEMENTS_OF_TYPE_NAME = "XML_getElementsOfType",
			XML_GET_ELEMENT_OF_ID_NAME = "XML_getElementOfId",
			XML_GET_CHILDS_NAME = "XML_getChilds",
			XML_GET_CHILDS_OF_TYPE_NAME = "XML_getChildsOfType",
			XML_GET_ID_OF_ELEMENT_NAME = "XML_getIdOfElement",
			XML_ALL_IDS_OF_TYPE_NAME = "XML_allIdsOfType";

	public static List<String> getIdentifiers() {
		List<String> identifiers = new ArrayList<>();
		identifiers.add(XML_GET_ELEMENTS_OF_TYPE_NAME);
		identifiers.add(XML_GET_ELEMENT_OF_ID_NAME);
		identifiers.add(XML_GET_CHILDS_NAME);
		identifiers.add(XML_GET_CHILDS_OF_TYPE_NAME);
		identifiers.add(XML_GET_ID_OF_ELEMENT_NAME);
		identifiers.add(XML_ALL_IDS_OF_TYPE_NAME);
		return identifiers;
	}

	public static AAbstractConstantsMachineClause createAbstractConstantsClause() {
		List<PExpression> identifiers = createIdentifierList(XML_GET_ELEMENTS_OF_TYPE_NAME, XML_GET_ELEMENT_OF_ID_NAME, XML_GET_CHILDS_NAME, XML_GET_CHILDS_OF_TYPE_NAME,
				XML_GET_ID_OF_ELEMENT_NAME, XML_ALL_IDS_OF_TYPE_NAME);
		// activate memoization for all abstract constants
		//identifiers = identifiers.stream().map(identifier -> new ADescriptionExpression(new TPragmaFreeText("memo"), identifier)).collect(Collectors.toList());
		return new AAbstractConstantsMachineClause(identifiers);
	}

	public static PPredicate createAbstractConstantsProperties() {
		// XML_getElementsOfType = %t.(t : STRING | { e | e : ran(XML_DATA) & e'element = t })
		AEqualPredicate getElementsOfType = new AEqualPredicate();
		getElementsOfType.setLeft(createIdentifier(XML_GET_ELEMENTS_OF_TYPE_NAME));
		getElementsOfType.setRight(new ALambdaExpression(
				createIdentifierList("t"),
				new AMemberPredicate(
						createIdentifier("t"),
						new AStringSetExpression()
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
												createIdentifier(ELEMENT_NAME)
										),
										createIdentifier("t")
								)
						)
				)
		));

		// XML_getElementOfId = %(i).(i : STRING | dom({ e, el | e : ran(XML_DATA) & `STRING__VALUE`(i) : e'attributes[{"id"}] }))
		AEqualPredicate getElementOfId = new AEqualPredicate();
		getElementOfId.setLeft(createIdentifier(XML_GET_ELEMENT_OF_ID_NAME));
		getElementOfId.setRight(new ALambdaExpression(
				createIdentifierList("i"),
				new AMemberPredicate(
						createIdentifier("i"),
						new AStringSetExpression()
				),
				new AComprehensionSetExpression(
						createIdentifierList("e"),
						new AConjunctPredicate(
								new AMemberPredicate(
										createIdentifier("e"),
										new ARangeExpression(createIdentifier(XML_DATA_NAME))
								),
								new AMemberPredicate(
										new AFunctionExpression(
												createIdentifier("STRING__VALUE"),
												createIdentifierList("i")
										),
										new AImageExpression(
												new ARecordFieldExpression(
														createIdentifier("e"),
														createIdentifier(ATTRIBUTES_NAME)
												),
												new ASetExtensionExpression(Collections.singletonList(createString(ID_NAME)))
										)

								)
						)
				)
		));

		// XML_getChilds = %(e).(e : ran(XML_DATA) | { c | c : ran(XML_DATA) & e'recId : ran(c'pIds) })
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
								new AMemberPredicate(
										new ARecordFieldExpression(
												createIdentifier("e"),
												createIdentifier(REC_ID_NAME)
										),
										new ARangeExpression(
											new ARecordFieldExpression(
													createIdentifier("c"),
													createIdentifier(P_IDS_NAME)
											)
										)
								)
						)
				)
		));

		// XML_getChildsOfType = %(e,t).(e : ran(XML_DATA) & t : STRING | { c | c : ran(XML_DATA) & e'recId : ran(c'pIds) & c'element = t })
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
								new AStringSetExpression()
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
										new AMemberPredicate(
												new ARecordFieldExpression(
														createIdentifier("e"),
														createIdentifier(REC_ID_NAME)
												),
												new ARangeExpression(
														new ARecordFieldExpression(
																createIdentifier("c"),
																createIdentifier(P_IDS_NAME)
														)
												)
										),
										new AEqualPredicate(
												new ARecordFieldExpression(
														createIdentifier("c"),
														createIdentifier(ELEMENT_NAME)
												),
												createIdentifier("t")
										)
								)
						)
				)
		));

		// XML_getIdOfElement = %(e).(e : ran(XML_DATA) | { i | `STRING__VALUE`(i) : e'attributes[{"id"}] })
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
												createIdentifier("STRING__VALUE"),
												createIdentifierList("i")
										),
										new AImageExpression(
												new ARecordFieldExpression(
														createIdentifier("e"),
														createIdentifier(ATTRIBUTES_NAME)
												),
												new ASetExtensionExpression(Collections.singletonList(createString(ID_NAME)))
										)

								)
						)
				)
		));

		// XML_allIdsOfType = %(t).(t : STRING | dom({ i,e | e : ran(XML_DATA) & e'type = t & `STRING__VALUE`(i) : e'attributes[{"id"}] }))
		AEqualPredicate allIdsOfType = new AEqualPredicate();
		allIdsOfType.setLeft(createIdentifier(XML_ALL_IDS_OF_TYPE_NAME));
		allIdsOfType.setRight(new ALambdaExpression(
				createIdentifierList("t"),
				new AMemberPredicate(
						createIdentifier("t"),
						new AStringSetExpression()
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
																createIdentifier(ELEMENT_NAME)
														),
														createIdentifier("t")
												),
												new AMemberPredicate(
														new AFunctionExpression(
																createIdentifier("STRING__VALUE"),
																createIdentifierList("i")
														),
														new AImageExpression(
																new ARecordFieldExpression(
																		createIdentifier("e"),
																		createIdentifier(ATTRIBUTES_NAME)
																),
																new ASetExtensionExpression(Collections.singletonList(createString(ID_NAME)))
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
}
