# XML2B
### EXPERIMENTAL

This project is still experimental.

XML2B aims to generate classical B machines for data validation from data stored in XML documents.
The main motivation is the derivation of attribute types, which cannot be provided by ProB's generic external function `READ_XML`.

### Translation with XSD Schema



#### XSD Elements

| XSD Element    | How translated | B Type |
|----------------|----------------|--------|
| all            |                |        |
| annotation     |                |        |
| any            |                |        |
| anyAttribute   |                |        |
| appinfo        |                |        |
| attribute      |                |        |
| attributeGroup |                |        |
| choice         |                |        |
| complexContent |                |        |
| complexType    |                |        |
| documentation  |                |        |
| element        |                |        |
| extension      |                |        |
| field          |                |        |
| group          |                |        |
| import         |                |        |
| include        |                |        |
| key            |                |        |
| keyref         |                |        |
| list           |                |        |
| notation       |                |        |
| redefine       |                |        |
| restriction    |                |        |
| schema         |                |        |
| selector       |                |        |
| sequence       |                |        |
| simpleContent  |                |        |
| simpleType     |                |        |
| union          |                |        |
| unique         |                |        |

#### XSD Restrictions/Facets

| XSD Restriction/Facet | How translated | B Type |
|-----------------------|----------------|--------|
| enumeration           |                |        |
| fractionDigits        |                |        |
| length                |                |        |
| maxExclusive          |                |        |
| maxInclusive          |                |        |
| maxLength             |                |        |
| minExclusive          |                |        |
| minInclusive          |                |        |
| minLength             |                |        |
| pattern               |                |        |
| totalDigits           |                |        |
| whiteSpace            |                |        |

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
| xs:float              | REAL   |
| xs:hexBinary          | STRING |
| xs:NOTATION           | STRING |
| xs:QName              | STRING |
| xs:untypedAtomic      | STRING |

### Translation without Schema

#### Type Derivation

| Type of String | B Type                          | Example | B Value |
|----------------|---------------------------------|---------|---------|
| duration       | REAL (converted duration in ms) | "PT6S"  | 6000.0  |
| number         | REAL                            | "-1"    | -1.0    |
|                |                                 | "27.5"  | 27.5    |
| boolean        | BOOL                            | "true"  | TRUE    |
| anything else  | STRING                          | "data"  | "data"  |
