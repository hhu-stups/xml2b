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

| XSD Type  | B Type |
|-----------|--------|
| xs:string | STRING |

### Translation without Schema

#### Type Derivation

| Attribute Value | B Type  |
|-----------------|---------|
| "data"          | STRING  |
| "1"             | INTEGER |
| "1.0"           | REAL    |
| "true"          | BOOL    |
