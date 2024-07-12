# XML2B
### EXPERIMENTAL

This project is still under development.

XML2B aims to generate classical B machines for data validation from data stored in XML documents.
The main motivation is the derivation of attribute types, which cannot be provided by ProB's generic external function `READ_XML`.
Type derivation can be carried out either standalone or with the help of XSD schema files.

### Usage
#### Command Line

#### Java

#### Get Value of an Attribute

`b : BOOL__VALUE~[e'attributes]` Caution: if an attribute is present, but has another type than `BOOL` it is ignored this way!

### Translation with XSD Schema

First, the XML file is read and validated against the provided XSD Schema using an SAXParser.
Then, the elements together with their attributes and B types are derived from the XSD Schema.

#### XSD Elements

| XSD Element    |                                    B Translation                                    |
|----------------|:-----------------------------------------------------------------------------------:|
| all            |                                                                                     |
| annotation     |                    _ignored_, can be used for `@desc` in future                     |
| any            |                                                                                     |
| anyAttribute   |                                                                                     |
| appinfo        |                                      _ignored_                                      |
| attribute      |                                                                                     |
| attributeGroup |                  attribute type information collected from schemas                  |
| choice         |                                                                                     |
| complexContent |                                                                                     |
| complexType    | all types are collected from the schemas at the beginning before element extraction |
| documentation  |                                                                                     |
| element        |                                                                                     |
| extension      |                                                                                     |
| field          |                                                                                     |
| group          |                        `XSDGroupUtils`: collect all elements                        |
| import         |                                                                                     |
| include        |                                                                                     |
| key            |                                      _ignored_                                      |
| keyref         |                                      _ignored_                                      |
| list           |                                                                                     |
| notation       |                                                                                     |
| redefine       |                                  <em>ignored</em>                                   |
| restriction    |                                                                                     |
| schema         |                  recursively traverse all schemas at the beginning                  |
| selector       |                                                                                     |
| sequence       |                                                                                     |
| simpleContent  |                                                                                     |
| simpleType     | all types are collected from the schemas at the beginning before element extraction |
| union          |                                                                                     |
| unique         |                                                                                     |

#### XSD Restrictions/Facets

###### Enumerated Sets

| XSD Restriction/Facet |                                                                                                                             B Translation                                                                                                                              |
|-----------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
| enumeration           |                                                                                                                 enumerated set of all provided values                                                                                                                  |
| fractionDigits        |                                                                                                                               _ignored_                                                                                                                                |
| length                |                                                                                                                               _ignored_                                                                                                                                |
| maxExclusive          |                                                                                                                               _ignored_                                                                                                                                |
| maxInclusive          |                                                                                                                               _ignored_                                                                                                                                |
| maxLength             |                                                                                                                               _ignored_                                                                                                                                |
| minExclusive          |                                                                                                                               _ignored_                                                                                                                                |
| minInclusive          |                                                                                                                               _ignored_                                                                                                                                |
| minLength             |                                                                                                                               _ignored_                                                                                                                                |
| pattern               | if in combination with a enumeration restriction or `xs:union` of another enumerated set: the enumerated set is marked extensible, i.e. values can be added dynamically during AST creation (since after XSD validation we can assume every present value to be valid) |
| totalDigits           |                                                                                                                               _ignored_                                                                                                                                |
| whiteSpace            |                                                                                                                               _ignored_                                                                                                                                |

The ignored restriction facets only provide upper/lower bounds for the length of values and are thus not relevant for definition of enumerated sets.

#### XSD Types

###### String Types
| XSD Type              | B Type |
|-----------------------|--------|
| xs:ENTITIES           | STRING |
| xs:ENTITY             | STRING |
| xs:ID                 | STRING |
| xs:IDREF              | STRING |
| xs:IDREFS             | STRING |
| xs:language           | STRING |
| xs:Name               | STRING |
| xs:NCName             | STRING |
| xs:NMTOKEN            | STRING |
| xs:NMTOKENS           | STRING |
| xs:normalizedString   | STRING |
| xs:string             | STRING |
| xs:token              | STRING |
| xs:date               | STRING |
| xs:dateTime           | STRING |
| xs:localDateTime      | STRING |

###### Date and Time Types
| XSD Type              | B Type |
|-----------------------|--------|
| xs:duration           | REAL   |
| xs:dayTimeDuration    | REAL   |
| xs:yearMonthDuration  | REAL   |
| xs:gDay               | STRING |
| xs:gMonth             | STRING |
| xs:gMonthDay          | STRING |
| xs:gYear              | STRING |
| xs:gYearMonth         | STRING |
| xs:time               | STRING |

###### Numeric Types
| XSD Type              | B Type  |
|-----------------------|---------|
| xs:byte               | STRING  |
| xs:decimal            | REAL    |
| xs:int                | INTEGER |
| xs:integer            | INTEGER |
| xs:long               | INTEGER |
| xs:negativeInteger    | INTEGER |
| xs:nonNegativeInteger | INTEGER |
| xs:nonPositiveInteger | INTEGER |
| xs:positiveInteger    | INTEGER |
| xs:short              | INTEGER |
| xs:unsignedLong       | INTEGER |
| xs:unsignedInt        | INTEGER |
| xs:unsignedShort      | INTEGER |
| xs:unsignedByte       | INTEGER |

###### Misc. Types
| XSD Type              | B Type |
|-----------------------|--------|
| xs:anyURI             | STRING |
| xs:base64Binary       | STRING |
| xs:boolean            | BOOL   |
| xs:double             | REAL   |
| xs:float              | FLOAT  |
| xs:hexBinary          | STRING |
| xs:NOTATION           | STRING |
| xs:QName              | STRING |
| xs:untypedAtomic      | STRING |

### Translation without Schema

First, the XML file is read using an SAXParser.
Then, the B types of attributes and contents are derived by application of the following conversion.

#### Type Derivation

| Type of String | B Type                          | Example | B Value |
|----------------|---------------------------------|---------|---------|
| duration       | REAL (converted duration in ms) | "PT6S"  | 6000.0  |
| number         | REAL                            | "-1"    | -1.0    |
|                |                                 | "27.5"  | 27.5    |
| boolean        | BOOL                            | "true"  | TRUE    |
| anything else  | STRING                          | "data"  | "data"  |


### General remarks

- Namespaces are available if used in the XML file, e.g. `<exampleNS:exampleTag>` is mapped to a record with field `element: "exampleNS:exampleTag"`, while the same without namespace prefix (`<exampleTag>`) is mapped to a record with field `element: "exampleTag"`.

### Provided Abstract Constants

- `XML_getElementsOfType = %t.(t : XML_ELEMENT_TYPES | { e | e : ran(XML_DATA) & e'elementType = t })`
- `XML_getElementOfId = %(i).(i : dom('id') | dom({ e, el | e : ran(XML_DATA) & (i,el) : 'id' & el : e'attributes }))`
- `XML_getChilds = %(e).(e : ran(XML_DATA) | { c | c : ran(XML_DATA) & c'pId = e'recId })`
- `XML_getChildsOfType = %(e,t).(e : ran(XML_DATA) & t : XML_ELEMENT_TYPES | { c | c : ran(XML_DATA) & c'pId = e'recId & c'type = t })`
- `XML_getIdOfElement = %(e).(e : ran(XML_DATA) | { i | 'id'(i) : e'attributes })`
- `XML_allIdsOfType = %(t).(t : XML_ELEMENT_TYPES | dom({ i,e | e : ran(XML_DATA) & e'type = t & 'id'(i) : e'attributes }))`
