
header { 
	package self.micromagic.expression.antlr;
}
class ExprParser extends Parser;
options {
	k = 2;                            // two token lookahead
	exportVocab=Expr;                 // Call its vocabulary "Expr"
	codeGenMakeSwitchThreshold = 2;   // Some optimizations
	codeGenBitsetTestThreshold = 3;
	defaultErrorHandler = false;      // Don't generate parser error handlers
	buildAST = true;
}

tokens {
	OBJ_MAP = "map"; OBJ_LIST = "list"; OBJ_SET = "set";
	EMPTY_STAT; ELIST; EXPR; VAR; 
	SPECIAL_OPT; POST_INC; POST_DEC; UNARY_MINUS; UNARY_PLUS;
}


compoundStatement
	:	(statement)+
	;

statement
	// An expression statement.  This could be a method call,
	// assignment statement, or any other expression evaluated for
	// side-effects.
	:	expression SEMI!

	// empty statement
	|	s:SEMI {#s.setType(EMPTY_STAT);}
	;

// the mother of all expressions
expression
	:	assignmentExpression
		{#expression = #(#[EXPR,"EXPR"],#expression);}
	;

// This is a list of expressions.
expressionList
	:	expression (COMMA! expression)*
		{#expressionList = #(#[ELIST,"ELIST"], expressionList);}
	;

argList
	:	(	expressionList
		|	/*nothing*/
			{#argList = #[ELIST,"ELIST"];}
		)
	;

// assignment expression (level 13)
assignmentExpression
	:	conditionalExpression
		(	( ASSIGN^ |  PLUS_ASSIGN^ )
			assignmentExpression
		)?
	;

// conditional test (level 12)
conditionalExpression
	:	logicalOrExpression
		( QUESTION^ assignmentExpression COLON! conditionalExpression )?
	;

// logical or (||)  (level 11)
logicalOrExpression
	:	logicalAndExpression (LOR^ logicalAndExpression)*
	;

// logical and (&&)  (level 10)
logicalAndExpression
	:	inclusiveOrExpression (LAND^ inclusiveOrExpression)*
	;

// bitwise or non-short-circuiting or (|)  (level 9)
inclusiveOrExpression
	:	exclusiveOrExpression (BOR^ exclusiveOrExpression)*
	;

// exclusive or (^)  (level 8)
exclusiveOrExpression
	:	andExpression (BXOR^ andExpression)*
	;

// bitwise or non-short-circuiting and (&)  (level 7)
andExpression
	:	equalityExpression (BAND^ equalityExpression)*
	;

// equality/inequality (==/!=) (level 6)
equalityExpression
	:	relationalExpression ((NOT_EQUAL^ | EQUAL^) relationalExpression)*
	;

// boolean relational expressions (level 5)
relationalExpression
	:	shiftExpression
		(	(	LT^
			|	GT^
			|	LE^
			|	GE^
			)
			shiftExpression
		)*
	;

// bit shift expressions (level 4)
shiftExpression
	:	additiveExpression ((SL^ | SR^ | BSR^) additiveExpression)*
	;


// binary addition/subtraction (level 3)
additiveExpression
	:	multiplicativeExpression ((PLUS^ | MINUS^) multiplicativeExpression)*
	;


// multiplication/division/modulo (level 2)
multiplicativeExpression
	:	unaryExpression ((STAR^ | DIV^ | MOD^ ) unaryExpression)*
	;

unaryExpression
	:	INC^ unaryExpression
	|	DEC^ unaryExpression
	|	DELETE^ unaryExpression
	|	MINUS^ {#MINUS.setType(UNARY_MINUS);} unaryExpression
	|	PLUS^  {#PLUS.setType(UNARY_PLUS);} unaryExpression
	|	unaryExpressionNotPlusMinus
	;

unaryExpressionNotPlusMinus
	:	BNOT^ unaryExpression
	|	LNOT^ unaryExpression
	|	postfixExpression
	;

postfixExpression
	:	preExpr:primaryExpression
		(
			// make primaryExpression as expr and change to var
			{
				#preExpr = #(#[EXPR,"EXPR"], #preExpr);
				#postfixExpression = #(#[VAR,"VAR"], #preExpr);
			}
			(DOT IDENT | LBRACK expression RBRACK!)
			(
				DOT IDENT | LBRACK expression RBRACK!
			)*
		)?
		(	// possibly add on a post-increment or post-decrement.
			// allows INC/DEC on too much, but semantics can check
			in:INC^ {#in.setType(POST_INC);}
		|	de:DEC^ {#de.setType(POST_DEC);}
		)?
	;

// the basic element of an expression
primaryExpression
	: identPrimary
	| constant
	| "true"
	| "false"
	| "null"
	| primaryObject
	| LPAREN! assignmentExpression RPAREN!
	;

// Match {map@ a:b,c:d} or {set@ a,b,c} or {a,b,c}
primaryObject
	:	(CONST)?
		(
			(OBJ_MAP AT LCURLY) =>
			(
				OBJ_MAP! AT! objMap:LCURLY^
				(
					assignmentExpression COLON! assignmentExpression
					(COMMA! assignmentExpression COLON! assignmentExpression)*
				)?
				{#objMap.setType(OBJ_MAP);}
				RCURLY!
			)
		|	(OBJ_SET AT LCURLY) =>
			(
				OBJ_SET! AT! objSet:LCURLY^
				(assignmentExpression (COMMA! assignmentExpression)*)?
				{#objSet.setType(OBJ_SET);}
				RCURLY!
			)
		|	(
				(OBJ_LIST! AT!)? objList:LCURLY^
				(assignmentExpression (COMMA! assignmentExpression)*)?
				{#objList.setType(OBJ_LIST);}
				RCURLY!
			)
		)
		
	;

// Match a, a.b.c, a.b.c[], a.b[][].c[] or method call
identPrimary
	:	(IDENT LPAREN) =>
		IDENT lp:LPAREN^ {#lp.setType(SPECIAL_OPT);} argList RPAREN!
	|
		IDENT
		(
			options {
				// .ident or [expression] could match here or in postfixExpression.
				// We do want to match here.  Turn off warning.
				greedy=true;
			}
			:	DOT IDENT | LBRACK expression RBRACK!
		)*
		{#identPrimary = #(#[VAR,"VAR"], identPrimary);}
	;

identVar
	:	IDENT
		(
			DOT IDENT |	LBRACK expression RBRACK!
		)*
		{#identVar = #(#[VAR,"VAR"], identVar);}
	;
	
constant
	:	NUM_INT
	|	CHAR_LITERAL
	|	STRING_LITERAL
	|	NUM_FLOAT
	|	NUM_LONG
	|	NUM_DOUBLE
	;



class ExprLexer extends Lexer;

options {
	exportVocab=Expr;     // call the vocabulary "Expr"
	testLiterals=false;   // don't automatically test for literals
	k=4;                  // four characters of lookahead
	charVocabulary='\u0003'..'\u7FFE';
	// without inlining some bitset tests, couldn't do unicode;
	// I need to make ANTLR generate smaller bitsets; see
	// bottom of JavaLexer.java
	codeGenBitsetTestThreshold=20;
}

tokens {
	DOT       = ".";
	DELETE    = "delete";
	//NEW       = "new";
	CONST       = "const";
}



// OPERATORS
LPAREN      : '('   ;
RPAREN      : ')'   ;
QUESTION    : '?'   ;
LBRACK      : '['   ;
RBRACK      : ']'   ;
LCURLY      : '{'   ;
RCURLY      : '}'   ;
COLON       : ':'   ;
COMMA       : ','   ;
ASSIGN      : ":="  ;
EQUAL       : "=="  ;
LNOT        : '!'   ;
BNOT        : '~'   ;
NOT_EQUAL   : "!="  ;
DIV         : '/'   ;
PLUS        : '+'   ;
PLUS_ASSIGN : "+="  ;
INC         : "++"  ;
MINUS       : '-'   ;
DEC         : "--"  ;
STAR        : '*'   ;
MOD         : '%'   ;
SR          : ">>"  ;
BSR         : ">>>" ;
GE          : ">="  ;
GT          : ">"   ;
SL          : "<<"  ;
LE          : "<="  ;
LT          : '<'   ;
BXOR        : '^'   ;
BOR         : '|'   ;
LOR         : "||"  ;
BAND        : '&'   ;
LAND        : "&&"  ;
AT          : '@'   ;
SEMI        : ';'   ;


// Whitespace -- ignored
WS	:	(	' '
		|	'\t'
		|	'\f'
			// handle newlines
		|	(	options {generateAmbigWarnings=false;}
			:	"\r\n"  // Evil DOS
			|	'\r'    // Macintosh
			|	'\n'    // Unix (the right way)
			)
			{ newline(); }
		)+
		{ _ttype = Token.SKIP; }
	;

// Single-line comments
SL_COMMENT
	:	"//"
		(~('\n'|'\r'))* ('\n'|'\r'('\n')?)?
		{$setType(Token.SKIP); newline();}
	;

// multiple-line comments
ML_COMMENT
	:	"/*"
		(	/*	'\r' '\n' can be matched in one alternative or by matching
				'\r' in one iteration and '\n' in another.  I am trying to
				handle any flavor of newline that comes in, but the language
				that allows both "\r\n" and "\r" and "\n" to all be valid
				newline is ambiguous.  Consequently, the resulting grammar
				must be ambiguous.  I'm shutting this warning off.
			 */
			options {
				generateAmbigWarnings=false;
			}
		:
			{ LA(2)!='/' }? '*'
		|	'\r' '\n'		{newline();}
		|	'\r'			{newline();}
		|	'\n'			{newline();}
		|	~('*'|'\n'|'\r')
		)*
		"*/"
		{$setType(Token.SKIP);}
	;


// character literals
CHAR_LITERAL
	:	'\'' ( ESC | ~('\''|'\n'|'\r'|'\\') ) '\''
	;

// string literals
STRING_LITERAL
	:	'"' (ESC|~('"'|'\\'|'\n'|'\r'))* '"'
	;


// escape sequence -- note that this is protected; it can only be called
//   from another lexer rule -- it will not ever directly return a token to
//   the parser
// There are various ambiguities hushed in this rule.  The optional
// '0'...'9' digit matches should be matched here rather than letting
// them go back to STRING_LITERAL to be matched.  ANTLR does the
// right thing by matching immediately; hence, it's ok to shut off
// the FOLLOW ambig warnings.
protected
ESC
	:	'\\'
		(	'n'
		|	'r'
		|	't'
		|	'b'
		|	'f'
		|	'"'
		|	'\''
		|	'\\'
		|	('u')+ HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
		|	'0'..'3'
			(
				options {
					warnWhenFollowAmbig = false;
				}
			:	'0'..'7'
				(
					options {
						warnWhenFollowAmbig = false;
					}
				:	'0'..'7'
				)?
			)?
		|	'4'..'7'
			(
				options {
					warnWhenFollowAmbig = false;
				}
			:	'0'..'7'
			)?
		)
	;


// hexadecimal digit (again, note it's protected!)
protected
HEX_DIGIT
	:	('0'..'9'|'A'..'F'|'a'..'f')
	;


// an identifier.  Note that testLiterals is set to true!  This means
// that after we match the rule, we look in the literals table to see
// if it's a literal or really an identifer
IDENT
	options {testLiterals=true;}
	:	('a'..'z'|'A'..'Z'|'_'|'$') ('a'..'z'|'A'..'Z'|'_'|'0'..'9'|'$')*
	;


// a numeric literal
NUM_INT
	{boolean isDecimal=false; Token t=null;}
	:	'.' {_ttype = DOT;}
		(	('0'..'9')+ (EXPONENT)? (f1:FLOAT_SUFFIX {t=f1;})?
			{
				if (t != null && t.getText().toUpperCase().indexOf('F')>=0) 
				{
					_ttype = NUM_FLOAT;
				}
				else 
				{
					_ttype = NUM_DOUBLE; // assume double
				}
			}
		)?

	|	(	'0' {isDecimal = true;} // special case for just '0'
			(	('x'|'X')
				(											// hex
					// the 'e'|'E' and float suffix stuff look
					// like hex digits, hence the (...)+ doesn't
					// know when to stop: ambig.  ANTLR resolves
					// it correctly by matching immediately.  It
					// is therefor ok to hush warning.
					options {
						warnWhenFollowAmbig=false;
					}
				:	HEX_DIGIT
				)+

			|	//float or double with leading zero
				(('0'..'9')+ ('.'|EXPONENT|FLOAT_SUFFIX)) => ('0'..'9')+

			|	('0'..'7')+									// octal
			)?
		|	('1'..'9') ('0'..'9')*  {isDecimal=true;}		// non-zero decimal
		)
		(	('l'|'L') { _ttype = NUM_LONG; }

			// only check to see if it's a float if looks like decimal so far
			|	{isDecimal}?
				(   '.' ('0'..'9')* (EXPONENT)? (f2:FLOAT_SUFFIX {t=f2;})?
			|	EXPONENT (f3:FLOAT_SUFFIX {t=f3;})?
			|	f4:FLOAT_SUFFIX {t=f4;}
		)
		{
			if (t != null && t.getText().toUpperCase() .indexOf('F') >= 0) 
			{
				_ttype = NUM_FLOAT;
			}
			else 
			{
				_ttype = NUM_DOUBLE; // assume double
			}
		}
	)?
	;


// a couple protected methods to assist in matching floating point numbers
protected
EXPONENT
	:	('e'|'E') ('+'|'-')? ('0'..'9')+
	;


protected
FLOAT_SUFFIX
	:	'f'|'F'|'d'|'D'
	;
