# XML2B

XML2B aims to generate a typed translation of XML data to classical B machines for data validation.
The main motivation is the derivation of attribute types, which cannot be provided by ProB's generic external function `READ_XML`.
Type derivation can be carried out either standalone or with the help of XSD schema files.

### Usage

A standalone JAR file (`XML2B.jar`) can be built using `./gradlew shadowJar`.

#### Command Line

The first argument always has to be a path to the XML file.
Optionally, the following options can be provided:

- `-xsd`: path to an XSD file that should be used for schema validation and type extraction
- `-frw`: specify Prolog system for fast read-write: SICSTUS, SWI; NONE for standard output; default is SICSTUS,
- `-o`: specify an output path for the generated machine file; the data file (`.probdata`) is written next to it. Without `-o` the machine and the data file are written next to the XML file (`<xml file name>.mch`, existing files are overwritten)
- `-ac`: add useful abstract constants to the generated machine (see below)
- `-update`: only update the generated `.probdata` file, the machine file is not touched
- `-version`: print the current version of XML2B

#### Java

XML2B can also be used from within Java projects.
For this, the `XML2B` class can be used. Its method `translate()` returns a ProB Java AST object of the translated XML data in B representation.

### Translation with XSD Schema

First, the XML file is read and validated against the provided XSD Schema using an SAXParser.
Then, the elements together with their attributes and B types are derived from the XSD Schema.

#### XSD Elements

All references schema files are traversed recursively.
Attribute type information is collected from simple and complex type declaration for elements.
All types are collected from the schemas at the beginning before element extraction

###### Enumerated Sets

`xs:restriction`s are used to determine enumerated sets from `xs:enumeration`.

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
| xs:byte               | INTEGER |
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

- Namespaces are available if used in the XML file, e.g. `<exampleNS:exampleTag>` is mapped to a record with field `element: "exampleNS:exampleTag"`, while the same without a namespace prefix (`<exampleTag>`) is mapped to a record with field `element: "exampleTag"`.
- `INF`, `+INF`, `-INF` and `NaN` (valid for `xs:double` and `xs:float`) cannot be represented as B reals and are reported as errors.
- Get value of an attribute wrapped in a free type: e.g., `b : XmlBool~[e'attributes]`. Caution: if an attribute is present, but has another type than `BOOL` it is ignored this way!
- Text content is kept as it is, only leading and trailing white space is removed and white-space-only content is ignored. Text around child elements (mixed content) is concatenated.

### Provided Abstract Constants

The following abstract constants will be added to the generated B machine if the option `-ac` is provided:

- `XML_getElementsOfType = %t.(t : STRING | { e | e : ran(XML_DATA) & e'Element = t })`
- `XML_getElementOfId = %(i).(i : STRING | { e | e : ran(XML_DATA) & XmlString(i) : e'attributes[{"id"}] })`
- `XML_getChilds = %(e).(e : ran(XML_DATA) | { c | c : ran(XML_DATA) & last(c'pIds) = e'recId })` (direct children; all descendants of `e` are the elements with `recId` in `e'recId+1..e'maxCId`)
- `XML_getChildsOfType = %(e,t).(e : ran(XML_DATA) & t : STRING | { c | c : ran(XML_DATA) & last(c'pIds) = e'recId & c'Element = t })`
- `XML_getIdOfElement = %(e).(e : ran(XML_DATA) | { i | i : STRING & XmlString(i) : e'attributes[{"id"}] })`
- `XML_allIdsOfType = %(t).(t : STRING | dom({ i,e | e : ran(XML_DATA) & e'Element = t & XmlString(i) : e'attributes[{"id"}] }))`
