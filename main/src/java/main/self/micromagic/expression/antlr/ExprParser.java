// $ANTLR : "Expression.g" -> "ExprParser.java"$
 
	package self.micromagic.expression.antlr;

import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;
import antlr.collections.AST;
import java.util.Hashtable;
import antlr.ASTFactory;
import antlr.ASTPair;
import antlr.collections.impl.ASTArray;

public class ExprParser extends antlr.LLkParser       implements ExprTokenTypes
 {

protected ExprParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public ExprParser(TokenBuffer tokenBuf) {
  this(tokenBuf,2);
}

protected ExprParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public ExprParser(TokenStream lexer) {
  this(lexer,2);
}

public ExprParser(ParserSharedInputState state) {
  super(state,2);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

	public final void compoundStatement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST compoundStatement_AST = null;
		
		{
		int _cnt3=0;
		_loop3:
		do {
			if ((_tokenSet_0.member(LA(1)))) {
				statement();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				if ( _cnt3>=1 ) { break _loop3; } else {throw new NoViableAltException(LT(1), getFilename());}
			}
			
			_cnt3++;
		} while (true);
		}
		compoundStatement_AST = (AST)currentAST.root;
		returnAST = compoundStatement_AST;
	}
	
	public final void statement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST statement_AST = null;
		Token  s = null;
		AST s_AST = null;
		
		switch ( LA(1)) {
		case OBJ_MAP:
		case OBJ_LIST:
		case OBJ_SET:
		case PLUS:
		case MINUS:
		case INC:
		case DEC:
		case DELETE:
		case BNOT:
		case LNOT:
		case IDENT:
		case LITERAL_true:
		case LITERAL_false:
		case LITERAL_null:
		case LPAREN:
		case CONST:
		case LCURLY:
		case NUM_INT:
		case CHAR_LITERAL:
		case STRING_LITERAL:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		{
			expression();
			astFactory.addASTChild(currentAST, returnAST);
			match(SEMI);
			statement_AST = (AST)currentAST.root;
			break;
		}
		case SEMI:
		{
			s = LT(1);
			s_AST = astFactory.create(s);
			astFactory.addASTChild(currentAST, s_AST);
			match(SEMI);
			if ( inputState.guessing==0 ) {
				s_AST.setType(EMPTY_STAT);
			}
			statement_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = statement_AST;
	}
	
	public final void expression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expression_AST = null;
		
		assignmentExpression();
		astFactory.addASTChild(currentAST, returnAST);
		if ( inputState.guessing==0 ) {
			expression_AST = (AST)currentAST.root;
			expression_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(EXPR,"EXPR")).add(expression_AST));
			currentAST.root = expression_AST;
			currentAST.child = expression_AST!=null &&expression_AST.getFirstChild()!=null ?
				expression_AST.getFirstChild() : expression_AST;
			currentAST.advanceChildToEnd();
		}
		expression_AST = (AST)currentAST.root;
		returnAST = expression_AST;
	}
	
	public final void assignmentExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST assignmentExpression_AST = null;
		
		conditionalExpression();
		astFactory.addASTChild(currentAST, returnAST);
		{
		switch ( LA(1)) {
		case ASSIGN:
		case PLUS_ASSIGN:
		{
			{
			switch ( LA(1)) {
			case ASSIGN:
			{
				AST tmp2_AST = null;
				tmp2_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp2_AST);
				match(ASSIGN);
				break;
			}
			case PLUS_ASSIGN:
			{
				AST tmp3_AST = null;
				tmp3_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp3_AST);
				match(PLUS_ASSIGN);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			assignmentExpression();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case SEMI:
		case COMMA:
		case COLON:
		case RBRACK:
		case RPAREN:
		case RCURLY:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		assignmentExpression_AST = (AST)currentAST.root;
		returnAST = assignmentExpression_AST;
	}
	
	public final void expressionList() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST expressionList_AST = null;
		
		expression();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop8:
		do {
			if ((LA(1)==COMMA)) {
				match(COMMA);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop8;
			}
			
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			expressionList_AST = (AST)currentAST.root;
			expressionList_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(ELIST,"ELIST")).add(expressionList_AST));
			currentAST.root = expressionList_AST;
			currentAST.child = expressionList_AST!=null &&expressionList_AST.getFirstChild()!=null ?
				expressionList_AST.getFirstChild() : expressionList_AST;
			currentAST.advanceChildToEnd();
		}
		expressionList_AST = (AST)currentAST.root;
		returnAST = expressionList_AST;
	}
	
	public final void argList() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST argList_AST = null;
		
		{
		switch ( LA(1)) {
		case OBJ_MAP:
		case OBJ_LIST:
		case OBJ_SET:
		case PLUS:
		case MINUS:
		case INC:
		case DEC:
		case DELETE:
		case BNOT:
		case LNOT:
		case IDENT:
		case LITERAL_true:
		case LITERAL_false:
		case LITERAL_null:
		case LPAREN:
		case CONST:
		case LCURLY:
		case NUM_INT:
		case CHAR_LITERAL:
		case STRING_LITERAL:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		{
			expressionList();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case RPAREN:
		{
			if ( inputState.guessing==0 ) {
				argList_AST = (AST)currentAST.root;
				argList_AST = astFactory.create(ELIST,"ELIST");
				currentAST.root = argList_AST;
				currentAST.child = argList_AST!=null &&argList_AST.getFirstChild()!=null ?
					argList_AST.getFirstChild() : argList_AST;
				currentAST.advanceChildToEnd();
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		argList_AST = (AST)currentAST.root;
		returnAST = argList_AST;
	}
	
	public final void conditionalExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST conditionalExpression_AST = null;
		
		logicalOrExpression();
		astFactory.addASTChild(currentAST, returnAST);
		{
		switch ( LA(1)) {
		case QUESTION:
		{
			AST tmp5_AST = null;
			tmp5_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp5_AST);
			match(QUESTION);
			assignmentExpression();
			astFactory.addASTChild(currentAST, returnAST);
			match(COLON);
			conditionalExpression();
			astFactory.addASTChild(currentAST, returnAST);
			break;
		}
		case SEMI:
		case COMMA:
		case ASSIGN:
		case PLUS_ASSIGN:
		case COLON:
		case RBRACK:
		case RPAREN:
		case RCURLY:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		conditionalExpression_AST = (AST)currentAST.root;
		returnAST = conditionalExpression_AST;
	}
	
	public final void logicalOrExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST logicalOrExpression_AST = null;
		
		logicalAndExpression();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop18:
		do {
			if ((LA(1)==LOR)) {
				AST tmp7_AST = null;
				tmp7_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp7_AST);
				match(LOR);
				logicalAndExpression();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop18;
			}
			
		} while (true);
		}
		logicalOrExpression_AST = (AST)currentAST.root;
		returnAST = logicalOrExpression_AST;
	}
	
	public final void logicalAndExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST logicalAndExpression_AST = null;
		
		inclusiveOrExpression();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop21:
		do {
			if ((LA(1)==LAND)) {
				AST tmp8_AST = null;
				tmp8_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp8_AST);
				match(LAND);
				inclusiveOrExpression();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop21;
			}
			
		} while (true);
		}
		logicalAndExpression_AST = (AST)currentAST.root;
		returnAST = logicalAndExpression_AST;
	}
	
	public final void inclusiveOrExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST inclusiveOrExpression_AST = null;
		
		exclusiveOrExpression();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop24:
		do {
			if ((LA(1)==BOR)) {
				AST tmp9_AST = null;
				tmp9_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp9_AST);
				match(BOR);
				exclusiveOrExpression();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop24;
			}
			
		} while (true);
		}
		inclusiveOrExpression_AST = (AST)currentAST.root;
		returnAST = inclusiveOrExpression_AST;
	}
	
	public final void exclusiveOrExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST exclusiveOrExpression_AST = null;
		
		andExpression();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop27:
		do {
			if ((LA(1)==BXOR)) {
				AST tmp10_AST = null;
				tmp10_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp10_AST);
				match(BXOR);
				andExpression();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop27;
			}
			
		} while (true);
		}
		exclusiveOrExpression_AST = (AST)currentAST.root;
		returnAST = exclusiveOrExpression_AST;
	}
	
	public final void andExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST andExpression_AST = null;
		
		equalityExpression();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop30:
		do {
			if ((LA(1)==BAND)) {
				AST tmp11_AST = null;
				tmp11_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp11_AST);
				match(BAND);
				equalityExpression();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop30;
			}
			
		} while (true);
		}
		andExpression_AST = (AST)currentAST.root;
		returnAST = andExpression_AST;
	}
	
	public final void equalityExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST equalityExpression_AST = null;
		
		relationalExpression();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop34:
		do {
			if ((LA(1)==NOT_EQUAL||LA(1)==EQUAL)) {
				{
				switch ( LA(1)) {
				case NOT_EQUAL:
				{
					AST tmp12_AST = null;
					tmp12_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp12_AST);
					match(NOT_EQUAL);
					break;
				}
				case EQUAL:
				{
					AST tmp13_AST = null;
					tmp13_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp13_AST);
					match(EQUAL);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				relationalExpression();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop34;
			}
			
		} while (true);
		}
		equalityExpression_AST = (AST)currentAST.root;
		returnAST = equalityExpression_AST;
	}
	
	public final void relationalExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST relationalExpression_AST = null;
		
		shiftExpression();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop38:
		do {
			if (((LA(1) >= LT && LA(1) <= GE))) {
				{
				switch ( LA(1)) {
				case LT:
				{
					AST tmp14_AST = null;
					tmp14_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp14_AST);
					match(LT);
					break;
				}
				case GT:
				{
					AST tmp15_AST = null;
					tmp15_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp15_AST);
					match(GT);
					break;
				}
				case LE:
				{
					AST tmp16_AST = null;
					tmp16_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp16_AST);
					match(LE);
					break;
				}
				case GE:
				{
					AST tmp17_AST = null;
					tmp17_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp17_AST);
					match(GE);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				shiftExpression();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop38;
			}
			
		} while (true);
		}
		relationalExpression_AST = (AST)currentAST.root;
		returnAST = relationalExpression_AST;
	}
	
	public final void shiftExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST shiftExpression_AST = null;
		
		additiveExpression();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop42:
		do {
			if (((LA(1) >= SL && LA(1) <= BSR))) {
				{
				switch ( LA(1)) {
				case SL:
				{
					AST tmp18_AST = null;
					tmp18_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp18_AST);
					match(SL);
					break;
				}
				case SR:
				{
					AST tmp19_AST = null;
					tmp19_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp19_AST);
					match(SR);
					break;
				}
				case BSR:
				{
					AST tmp20_AST = null;
					tmp20_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp20_AST);
					match(BSR);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				additiveExpression();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop42;
			}
			
		} while (true);
		}
		shiftExpression_AST = (AST)currentAST.root;
		returnAST = shiftExpression_AST;
	}
	
	public final void additiveExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST additiveExpression_AST = null;
		
		multiplicativeExpression();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop46:
		do {
			if ((LA(1)==PLUS||LA(1)==MINUS)) {
				{
				switch ( LA(1)) {
				case PLUS:
				{
					AST tmp21_AST = null;
					tmp21_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp21_AST);
					match(PLUS);
					break;
				}
				case MINUS:
				{
					AST tmp22_AST = null;
					tmp22_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp22_AST);
					match(MINUS);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				multiplicativeExpression();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop46;
			}
			
		} while (true);
		}
		additiveExpression_AST = (AST)currentAST.root;
		returnAST = additiveExpression_AST;
	}
	
	public final void multiplicativeExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST multiplicativeExpression_AST = null;
		
		unaryExpression();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop50:
		do {
			if (((LA(1) >= STAR && LA(1) <= MOD))) {
				{
				switch ( LA(1)) {
				case STAR:
				{
					AST tmp23_AST = null;
					tmp23_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp23_AST);
					match(STAR);
					break;
				}
				case DIV:
				{
					AST tmp24_AST = null;
					tmp24_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp24_AST);
					match(DIV);
					break;
				}
				case MOD:
				{
					AST tmp25_AST = null;
					tmp25_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp25_AST);
					match(MOD);
					break;
				}
				default:
				{
					throw new NoViableAltException(LT(1), getFilename());
				}
				}
				}
				unaryExpression();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop50;
			}
			
		} while (true);
		}
		multiplicativeExpression_AST = (AST)currentAST.root;
		returnAST = multiplicativeExpression_AST;
	}
	
	public final void unaryExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST unaryExpression_AST = null;
		
		switch ( LA(1)) {
		case INC:
		{
			AST tmp26_AST = null;
			tmp26_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp26_AST);
			match(INC);
			unaryExpression();
			astFactory.addASTChild(currentAST, returnAST);
			unaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case DEC:
		{
			AST tmp27_AST = null;
			tmp27_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp27_AST);
			match(DEC);
			unaryExpression();
			astFactory.addASTChild(currentAST, returnAST);
			unaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case DELETE:
		{
			AST tmp28_AST = null;
			tmp28_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp28_AST);
			match(DELETE);
			unaryExpression();
			astFactory.addASTChild(currentAST, returnAST);
			unaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case MINUS:
		{
			AST tmp29_AST = null;
			tmp29_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp29_AST);
			match(MINUS);
			if ( inputState.guessing==0 ) {
				tmp29_AST.setType(UNARY_MINUS);
			}
			unaryExpression();
			astFactory.addASTChild(currentAST, returnAST);
			unaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case PLUS:
		{
			AST tmp30_AST = null;
			tmp30_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp30_AST);
			match(PLUS);
			if ( inputState.guessing==0 ) {
				tmp30_AST.setType(UNARY_PLUS);
			}
			unaryExpression();
			astFactory.addASTChild(currentAST, returnAST);
			unaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case OBJ_MAP:
		case OBJ_LIST:
		case OBJ_SET:
		case BNOT:
		case LNOT:
		case IDENT:
		case LITERAL_true:
		case LITERAL_false:
		case LITERAL_null:
		case LPAREN:
		case CONST:
		case LCURLY:
		case NUM_INT:
		case CHAR_LITERAL:
		case STRING_LITERAL:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		{
			unaryExpressionNotPlusMinus();
			astFactory.addASTChild(currentAST, returnAST);
			unaryExpression_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = unaryExpression_AST;
	}
	
	public final void unaryExpressionNotPlusMinus() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST unaryExpressionNotPlusMinus_AST = null;
		
		switch ( LA(1)) {
		case BNOT:
		{
			AST tmp31_AST = null;
			tmp31_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp31_AST);
			match(BNOT);
			unaryExpression();
			astFactory.addASTChild(currentAST, returnAST);
			unaryExpressionNotPlusMinus_AST = (AST)currentAST.root;
			break;
		}
		case LNOT:
		{
			AST tmp32_AST = null;
			tmp32_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp32_AST);
			match(LNOT);
			unaryExpression();
			astFactory.addASTChild(currentAST, returnAST);
			unaryExpressionNotPlusMinus_AST = (AST)currentAST.root;
			break;
		}
		case OBJ_MAP:
		case OBJ_LIST:
		case OBJ_SET:
		case IDENT:
		case LITERAL_true:
		case LITERAL_false:
		case LITERAL_null:
		case LPAREN:
		case CONST:
		case LCURLY:
		case NUM_INT:
		case CHAR_LITERAL:
		case STRING_LITERAL:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		{
			postfixExpression();
			astFactory.addASTChild(currentAST, returnAST);
			unaryExpressionNotPlusMinus_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = unaryExpressionNotPlusMinus_AST;
	}
	
	public final void postfixExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST postfixExpression_AST = null;
		AST preExpr_AST = null;
		Token  in = null;
		AST in_AST = null;
		Token  de = null;
		AST de_AST = null;
		
		primaryExpression();
		preExpr_AST = (AST)returnAST;
		astFactory.addASTChild(currentAST, returnAST);
		{
		switch ( LA(1)) {
		case DOT:
		case LBRACK:
		{
			if ( inputState.guessing==0 ) {
				postfixExpression_AST = (AST)currentAST.root;
				
								preExpr_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(EXPR,"EXPR")).add(preExpr_AST));
								postfixExpression_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(VAR,"VAR")).add(preExpr_AST));
							
				currentAST.root = postfixExpression_AST;
				currentAST.child = postfixExpression_AST!=null &&postfixExpression_AST.getFirstChild()!=null ?
					postfixExpression_AST.getFirstChild() : postfixExpression_AST;
				currentAST.advanceChildToEnd();
			}
			{
			switch ( LA(1)) {
			case DOT:
			{
				AST tmp33_AST = null;
				tmp33_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp33_AST);
				match(DOT);
				AST tmp34_AST = null;
				tmp34_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp34_AST);
				match(IDENT);
				break;
			}
			case LBRACK:
			{
				AST tmp35_AST = null;
				tmp35_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp35_AST);
				match(LBRACK);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				match(RBRACK);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			{
			_loop57:
			do {
				switch ( LA(1)) {
				case DOT:
				{
					AST tmp37_AST = null;
					tmp37_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp37_AST);
					match(DOT);
					AST tmp38_AST = null;
					tmp38_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp38_AST);
					match(IDENT);
					break;
				}
				case LBRACK:
				{
					AST tmp39_AST = null;
					tmp39_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp39_AST);
					match(LBRACK);
					expression();
					astFactory.addASTChild(currentAST, returnAST);
					match(RBRACK);
					break;
				}
				default:
				{
					break _loop57;
				}
				}
			} while (true);
			}
			break;
		}
		case SEMI:
		case COMMA:
		case ASSIGN:
		case PLUS_ASSIGN:
		case QUESTION:
		case COLON:
		case LOR:
		case LAND:
		case BOR:
		case BXOR:
		case BAND:
		case NOT_EQUAL:
		case EQUAL:
		case LT:
		case GT:
		case LE:
		case GE:
		case SL:
		case SR:
		case BSR:
		case PLUS:
		case MINUS:
		case STAR:
		case DIV:
		case MOD:
		case INC:
		case DEC:
		case RBRACK:
		case RPAREN:
		case RCURLY:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		switch ( LA(1)) {
		case INC:
		{
			in = LT(1);
			in_AST = astFactory.create(in);
			astFactory.makeASTRoot(currentAST, in_AST);
			match(INC);
			if ( inputState.guessing==0 ) {
				in_AST.setType(POST_INC);
			}
			break;
		}
		case DEC:
		{
			de = LT(1);
			de_AST = astFactory.create(de);
			astFactory.makeASTRoot(currentAST, de_AST);
			match(DEC);
			if ( inputState.guessing==0 ) {
				de_AST.setType(POST_DEC);
			}
			break;
		}
		case SEMI:
		case COMMA:
		case ASSIGN:
		case PLUS_ASSIGN:
		case QUESTION:
		case COLON:
		case LOR:
		case LAND:
		case BOR:
		case BXOR:
		case BAND:
		case NOT_EQUAL:
		case EQUAL:
		case LT:
		case GT:
		case LE:
		case GE:
		case SL:
		case SR:
		case BSR:
		case PLUS:
		case MINUS:
		case STAR:
		case DIV:
		case MOD:
		case RBRACK:
		case RPAREN:
		case RCURLY:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		postfixExpression_AST = (AST)currentAST.root;
		returnAST = postfixExpression_AST;
	}
	
	public final void primaryExpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST primaryExpression_AST = null;
		
		switch ( LA(1)) {
		case IDENT:
		{
			identPrimary();
			astFactory.addASTChild(currentAST, returnAST);
			primaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case NUM_INT:
		case CHAR_LITERAL:
		case STRING_LITERAL:
		case NUM_FLOAT:
		case NUM_LONG:
		case NUM_DOUBLE:
		{
			constant();
			astFactory.addASTChild(currentAST, returnAST);
			primaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_true:
		{
			AST tmp41_AST = null;
			tmp41_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp41_AST);
			match(LITERAL_true);
			primaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_false:
		{
			AST tmp42_AST = null;
			tmp42_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp42_AST);
			match(LITERAL_false);
			primaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_null:
		{
			AST tmp43_AST = null;
			tmp43_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp43_AST);
			match(LITERAL_null);
			primaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case OBJ_MAP:
		case OBJ_LIST:
		case OBJ_SET:
		case CONST:
		case LCURLY:
		{
			primaryObject();
			astFactory.addASTChild(currentAST, returnAST);
			primaryExpression_AST = (AST)currentAST.root;
			break;
		}
		case LPAREN:
		{
			match(LPAREN);
			assignmentExpression();
			astFactory.addASTChild(currentAST, returnAST);
			match(RPAREN);
			primaryExpression_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = primaryExpression_AST;
	}
	
	public final void identPrimary() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST identPrimary_AST = null;
		Token  lp = null;
		AST lp_AST = null;
		
		boolean synPredMatched82 = false;
		if (((LA(1)==IDENT) && (LA(2)==LPAREN))) {
			int _m82 = mark();
			synPredMatched82 = true;
			inputState.guessing++;
			try {
				{
				match(IDENT);
				match(LPAREN);
				}
			}
			catch (RecognitionException pe) {
				synPredMatched82 = false;
			}
			rewind(_m82);
inputState.guessing--;
		}
		if ( synPredMatched82 ) {
			AST tmp46_AST = null;
			tmp46_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp46_AST);
			match(IDENT);
			lp = LT(1);
			lp_AST = astFactory.create(lp);
			astFactory.makeASTRoot(currentAST, lp_AST);
			match(LPAREN);
			if ( inputState.guessing==0 ) {
				lp_AST.setType(SPECIAL_OPT);
			}
			argList();
			astFactory.addASTChild(currentAST, returnAST);
			match(RPAREN);
			identPrimary_AST = (AST)currentAST.root;
		}
		else if ((LA(1)==IDENT) && (_tokenSet_1.member(LA(2)))) {
			AST tmp48_AST = null;
			tmp48_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp48_AST);
			match(IDENT);
			{
			_loop84:
			do {
				if ((LA(1)==DOT) && (LA(2)==IDENT)) {
					AST tmp49_AST = null;
					tmp49_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp49_AST);
					match(DOT);
					AST tmp50_AST = null;
					tmp50_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp50_AST);
					match(IDENT);
				}
				else if ((LA(1)==LBRACK) && (_tokenSet_2.member(LA(2)))) {
					AST tmp51_AST = null;
					tmp51_AST = astFactory.create(LT(1));
					astFactory.addASTChild(currentAST, tmp51_AST);
					match(LBRACK);
					expression();
					astFactory.addASTChild(currentAST, returnAST);
					match(RBRACK);
				}
				else {
					break _loop84;
				}
				
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				identPrimary_AST = (AST)currentAST.root;
				identPrimary_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(VAR,"VAR")).add(identPrimary_AST));
				currentAST.root = identPrimary_AST;
				currentAST.child = identPrimary_AST!=null &&identPrimary_AST.getFirstChild()!=null ?
					identPrimary_AST.getFirstChild() : identPrimary_AST;
				currentAST.advanceChildToEnd();
			}
			identPrimary_AST = (AST)currentAST.root;
		}
		else {
			throw new NoViableAltException(LT(1), getFilename());
		}
		
		returnAST = identPrimary_AST;
	}
	
	public final void constant() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST constant_AST = null;
		
		switch ( LA(1)) {
		case NUM_INT:
		{
			AST tmp53_AST = null;
			tmp53_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp53_AST);
			match(NUM_INT);
			constant_AST = (AST)currentAST.root;
			break;
		}
		case CHAR_LITERAL:
		{
			AST tmp54_AST = null;
			tmp54_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp54_AST);
			match(CHAR_LITERAL);
			constant_AST = (AST)currentAST.root;
			break;
		}
		case STRING_LITERAL:
		{
			AST tmp55_AST = null;
			tmp55_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp55_AST);
			match(STRING_LITERAL);
			constant_AST = (AST)currentAST.root;
			break;
		}
		case NUM_FLOAT:
		{
			AST tmp56_AST = null;
			tmp56_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp56_AST);
			match(NUM_FLOAT);
			constant_AST = (AST)currentAST.root;
			break;
		}
		case NUM_LONG:
		{
			AST tmp57_AST = null;
			tmp57_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp57_AST);
			match(NUM_LONG);
			constant_AST = (AST)currentAST.root;
			break;
		}
		case NUM_DOUBLE:
		{
			AST tmp58_AST = null;
			tmp58_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp58_AST);
			match(NUM_DOUBLE);
			constant_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = constant_AST;
	}
	
	public final void primaryObject() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST primaryObject_AST = null;
		Token  objMap = null;
		AST objMap_AST = null;
		Token  objSet = null;
		AST objSet_AST = null;
		Token  objList = null;
		AST objList_AST = null;
		
		{
		switch ( LA(1)) {
		case CONST:
		{
			AST tmp59_AST = null;
			tmp59_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp59_AST);
			match(CONST);
			break;
		}
		case OBJ_MAP:
		case OBJ_LIST:
		case OBJ_SET:
		case LCURLY:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		{
		switch ( LA(1)) {
		case OBJ_MAP:
		{
			{
			match(OBJ_MAP);
			match(AT);
			objMap = LT(1);
			objMap_AST = astFactory.create(objMap);
			astFactory.makeASTRoot(currentAST, objMap_AST);
			match(LCURLY);
			{
			switch ( LA(1)) {
			case OBJ_MAP:
			case OBJ_LIST:
			case OBJ_SET:
			case PLUS:
			case MINUS:
			case INC:
			case DEC:
			case DELETE:
			case BNOT:
			case LNOT:
			case IDENT:
			case LITERAL_true:
			case LITERAL_false:
			case LITERAL_null:
			case LPAREN:
			case CONST:
			case LCURLY:
			case NUM_INT:
			case CHAR_LITERAL:
			case STRING_LITERAL:
			case NUM_FLOAT:
			case NUM_LONG:
			case NUM_DOUBLE:
			{
				assignmentExpression();
				astFactory.addASTChild(currentAST, returnAST);
				match(COLON);
				assignmentExpression();
				astFactory.addASTChild(currentAST, returnAST);
				{
				_loop68:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						assignmentExpression();
						astFactory.addASTChild(currentAST, returnAST);
						match(COLON);
						assignmentExpression();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop68;
					}
					
				} while (true);
				}
				break;
			}
			case RCURLY:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			if ( inputState.guessing==0 ) {
				objMap_AST.setType(OBJ_MAP);
			}
			match(RCURLY);
			}
			break;
		}
		case OBJ_SET:
		{
			{
			match(OBJ_SET);
			match(AT);
			objSet = LT(1);
			objSet_AST = astFactory.create(objSet);
			astFactory.makeASTRoot(currentAST, objSet_AST);
			match(LCURLY);
			{
			switch ( LA(1)) {
			case OBJ_MAP:
			case OBJ_LIST:
			case OBJ_SET:
			case PLUS:
			case MINUS:
			case INC:
			case DEC:
			case DELETE:
			case BNOT:
			case LNOT:
			case IDENT:
			case LITERAL_true:
			case LITERAL_false:
			case LITERAL_null:
			case LPAREN:
			case CONST:
			case LCURLY:
			case NUM_INT:
			case CHAR_LITERAL:
			case STRING_LITERAL:
			case NUM_FLOAT:
			case NUM_LONG:
			case NUM_DOUBLE:
			{
				assignmentExpression();
				astFactory.addASTChild(currentAST, returnAST);
				{
				_loop74:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						assignmentExpression();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop74;
					}
					
				} while (true);
				}
				break;
			}
			case RCURLY:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			if ( inputState.guessing==0 ) {
				objSet_AST.setType(OBJ_SET);
			}
			match(RCURLY);
			}
			break;
		}
		case OBJ_LIST:
		case LCURLY:
		{
			{
			{
			switch ( LA(1)) {
			case OBJ_LIST:
			{
				match(OBJ_LIST);
				match(AT);
				break;
			}
			case LCURLY:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			objList = LT(1);
			objList_AST = astFactory.create(objList);
			astFactory.makeASTRoot(currentAST, objList_AST);
			match(LCURLY);
			{
			switch ( LA(1)) {
			case OBJ_MAP:
			case OBJ_LIST:
			case OBJ_SET:
			case PLUS:
			case MINUS:
			case INC:
			case DEC:
			case DELETE:
			case BNOT:
			case LNOT:
			case IDENT:
			case LITERAL_true:
			case LITERAL_false:
			case LITERAL_null:
			case LPAREN:
			case CONST:
			case LCURLY:
			case NUM_INT:
			case CHAR_LITERAL:
			case STRING_LITERAL:
			case NUM_FLOAT:
			case NUM_LONG:
			case NUM_DOUBLE:
			{
				assignmentExpression();
				astFactory.addASTChild(currentAST, returnAST);
				{
				_loop79:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						assignmentExpression();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop79;
					}
					
				} while (true);
				}
				break;
			}
			case RCURLY:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			if ( inputState.guessing==0 ) {
				objList_AST.setType(OBJ_LIST);
			}
			match(RCURLY);
			}
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		primaryObject_AST = (AST)currentAST.root;
		returnAST = primaryObject_AST;
	}
	
	public final void identVar() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST identVar_AST = null;
		
		AST tmp74_AST = null;
		tmp74_AST = astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp74_AST);
		match(IDENT);
		{
		_loop87:
		do {
			switch ( LA(1)) {
			case DOT:
			{
				AST tmp75_AST = null;
				tmp75_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp75_AST);
				match(DOT);
				AST tmp76_AST = null;
				tmp76_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp76_AST);
				match(IDENT);
				break;
			}
			case LBRACK:
			{
				AST tmp77_AST = null;
				tmp77_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp77_AST);
				match(LBRACK);
				expression();
				astFactory.addASTChild(currentAST, returnAST);
				match(RBRACK);
				break;
			}
			default:
			{
				break _loop87;
			}
			}
		} while (true);
		}
		if ( inputState.guessing==0 ) {
			identVar_AST = (AST)currentAST.root;
			identVar_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(VAR,"VAR")).add(identVar_AST));
			currentAST.root = identVar_AST;
			currentAST.child = identVar_AST!=null &&identVar_AST.getFirstChild()!=null ?
				identVar_AST.getFirstChild() : identVar_AST;
			currentAST.advanceChildToEnd();
		}
		identVar_AST = (AST)currentAST.root;
		returnAST = identVar_AST;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"\"map\"",
		"\"list\"",
		"\"set\"",
		"EMPTY_STAT",
		"ELIST",
		"EXPR",
		"VAR",
		"SPECIAL_OPT",
		"POST_INC",
		"POST_DEC",
		"UNARY_MINUS",
		"UNARY_PLUS",
		"SEMI",
		"COMMA",
		"ASSIGN",
		"PLUS_ASSIGN",
		"QUESTION",
		"COLON",
		"LOR",
		"LAND",
		"BOR",
		"BXOR",
		"BAND",
		"NOT_EQUAL",
		"EQUAL",
		"LT",
		"GT",
		"LE",
		"GE",
		"SL",
		"SR",
		"BSR",
		"PLUS",
		"MINUS",
		"STAR",
		"DIV",
		"MOD",
		"INC",
		"DEC",
		"\"delete\"",
		"BNOT",
		"LNOT",
		"\".\"",
		"IDENT",
		"LBRACK",
		"RBRACK",
		"\"true\"",
		"\"false\"",
		"\"null\"",
		"LPAREN",
		"RPAREN",
		"\"const\"",
		"AT",
		"LCURLY",
		"RCURLY",
		"NUM_INT",
		"CHAR_LITERAL",
		"STRING_LITERAL",
		"NUM_FLOAT",
		"NUM_LONG",
		"NUM_DOUBLE",
		"WS",
		"SL_COMMENT",
		"ML_COMMENT",
		"ESC",
		"HEX_DIGIT",
		"EXPONENT",
		"FLOAT_SUFFIX"
	};
	
	protected void buildTokenTypeASTClassMap() {
		tokenTypeToASTClassMap=null;
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { -379219155238190992L, 1L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 307168364428460032L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { -379219155238256528L, 1L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	
	}
