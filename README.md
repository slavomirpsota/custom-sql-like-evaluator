# Custom SQL like evaluator

I made this as a work assignment, where the request was to develop simple query engine from input string

## Description

Requirement was that this engine has to parse expression from simple String and evaluate basic expressions, nested expressions with SQL like operands. 
This repo was used for initial implementation as it was easier to test the behavior of algorithms this way. It is build on top of Spring-Boot which is
not necessary for this use case, but it is what it is. Further optimization took place when I was actually implementing this in working repo. 

#### Working operands:

+ LESS_THAN("lt")
+ GREATER_THAN("gt"),
+ LESS_THAN_EQUAL("le"),
+ GREATER_THAN_EQUAL("ge"),
+ EQUALS("eq"),
+ NOT_EQUALS("ne"),
+ IN("in");

#### Working data types:

+ STRING 
+ INTEGER 
+ DATE 
+ BOOLEAN

**No real names, field names or data were used**

## Getting Started

### Dependencies

* Maven
* JDK semeru-1.8

### Installing

* Importing as Maven project should do the trick

## Author
Slavomir Psota
