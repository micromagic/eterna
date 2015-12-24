
package self.micromagic.expression;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import self.micromagic.cg.BeanTool;
import self.micromagic.cg.ClassGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.Tool;
import self.micromagic.expression.antlr.ExprLexer;
import self.micromagic.expression.antlr.ExprParser;
import self.micromagic.expression.antlr.ExprTokenTypes;
import self.micromagic.expression.impl.AssignOptCreater;
import self.micromagic.expression.impl.NumValueCreater;
import self.micromagic.expression.impl.ObjValueCreater;
import self.micromagic.expression.impl.OneOptCreater;
import self.micromagic.expression.impl.PlusCreater;
import self.micromagic.expression.impl.SpecialExpression;
import self.micromagic.expression.impl.StringCreater;
import self.micromagic.expression.impl.ThreeOptCreater;
import self.micromagic.expression.impl.TwoOptCreater;
import self.micromagic.expression.impl.VarCreater;
import self.micromagic.expression.impl.VarOptCreater;
import self.micromagic.expression.opts.DefaultOptCreater;
import self.micromagic.util.ResManager;
import self.micromagic.util.Utility;
import antlr.ANTLRException;
import antlr.collections.AST;

/**
 * 表达式的工具类.
 */
public class ExprTool
		implements ExprTokenTypes
{
	public static final int INTEGER_LEVEL = 2;
	public static final int LONG_LEVEL = 3;
	public static final int DOUBLE_LEVEL = 5;
	public static final int NEED_CAST_LEVEL = 0x100;

	/**
	 * 解析一段脚本, 变为一个表达式列表.
	 *
	 * @param keepConst  是否需要保留常量表达式.
	 */
	public static Object[] parseExps(String statements, boolean keepConst)
	{
		ExprLexer lex = new ExprLexer(new StringReader(statements));
		ExprParser parser = new ExprParser(lex);
		try
		{
			parser.compoundStatement();
		}
		catch (ANTLRException ex)
		{
			throw new EternaException(ex);
		}
		AST tmp = parser.getAST();
		List result = new ArrayList();
		while (tmp != null)
		{
			if (tmp.getType() != EMPTY_STAT)
			{
				Object obj = AbstractExpression.tryGetValue(parseExpNode(tmp), null);
				if (keepConst || !isConstObject(obj))
				{
					result.add(obj);
				}
			}
			tmp = tmp.getNextSibling();
		}
		return result.toArray();
	}

	/**
	 * 解析一个表达式节点.
	 *
	 * @param extList  额外需要执行的表达式列表
	 */
	public static Object parseExpNode(AST node)
	{
		int type = node.getType();
		if (type == EXPR)
		{
			return parseExpNode(node.getFirstChild());
		}
		if (type == SPECIAL_OPT)
		{
			return createSpecialExp(node);
		}
		ExprCreater creater = createrCache[type];
		if (creater == null)
		{
			throw new EternaException("Error node type [" + type + "] [" + node.getText() + "].");
		}
		return creater.create(node);
	}

	private static ExprCreater[] createrCache = new ExprCreater[128];

	/**
	 * 获取字符的类型等级.
	 *
	 * @param null2Zero  是否要将空转换为0
	 */
	public static int getNumberLevel(Object number, boolean null2Zero)
	{
		if (number == null)
		{
			if (null2Zero)
			{
				return INTEGER_LEVEL | NEED_CAST_LEVEL;
			}
			return -1;
		}
		if (number instanceof Character)
		{
			return INTEGER_LEVEL | NEED_CAST_LEVEL;
		}
		if (!(number instanceof Number))
		{
			return -1;
		}
		return getNumberLevel((Number) number);
	}

	/**
	 * 转换成数字类型.
	 */
	public static Number cast2Number(int level, Object number)
	{
		if (number == null)
		{
			return Utility.INTEGER_0;
		}
		return Utility.createInteger(((Character) number).charValue());
	}

	/**
	 * 获取字符的类型等级.
	 */
	public static int getNumberLevel(Number number)
	{
		if (number instanceof Integer)
		{
			return INTEGER_LEVEL;
		}
		if (number instanceof Long)
		{
			return LONG_LEVEL;
		}
		if (number instanceof Double)
		{
			return DOUBLE_LEVEL;
		}
		if (number instanceof Byte)
		{
			return 0;
		}
		if (number instanceof Short)
		{
			return 1;
		}
		if (number instanceof Float)
		{
			return 4;
		}
		// BigXXX
		return DOUBLE_LEVEL;
	}

	/**
	 * 判断对象是否为常量.
	 */
	public static boolean isConstObject(Object obj)
	{
		if (obj == null)
		{
			return true;
		}
		return !(obj instanceof DynamicObject);
	}

	/**
	 * 创建特殊操作表达式.
	 */
	private static SpecialExpression createSpecialExp(AST node)
	{
		AST first = node.getFirstChild();
		String optName = first.getText();
		AST eList = first.getNextSibling();
		if (eList.getType() != ELIST)
		{
			throw new EternaException("Not found arg list for [" + optName + "].");
		}
		List argList = new ArrayList();
		AST tmp = eList.getFirstChild();
		while (tmp != null)
		{
			argList.add(parseExpNode(tmp));
			tmp = tmp.getNextSibling();
		}
		Object[] args = argList.toArray();
		boolean allArgConst = SpecialExpression.checkArgs(args);
		SpecialOpt opt = getSpecialOpt(optName, args);
		if (opt == null)
		{
			throw new EternaException("Not found special opt [" + optName + "].");
		}
		return new SpecialExpression(args, opt, allArgConst);
	}

	/**
	 * 根据操作名称获取一个特殊的操作对象.
	 */
	private static SpecialOpt getSpecialOpt(String name, Object[] args)
	{
		Iterator itr = specialList.iterator();
		while (itr.hasNext())
		{
			SpecialOpt opt = ((SpecialCreater) itr.next()).create(name, args);
			if (opt != null)
			{
				return opt;
			}
		}
		return null;
	}

	/**
	 * 添加一个特殊操作创建者.
	 */
	public static void addSpecialCreater(SpecialCreater creater)
	{
		specialList.add(0, creater);
	}
	private static List specialList = new LinkedList();

	/**
	 * 获取一个数字的操作对象。
	 *
	 * @param level    数字对象的等级
	 * @param optName  操作的名称
	 * @param optFlag  操作符
	 */
	public static Operation getNumberOpt(int level, String optName, String optFlag)
	{
		Operation[] optArr = (Operation[]) numOptCache.get(optName);
		if (optArr != null && optArr[level] != null)
		{
			return optArr[level];
		}
		return createNumberOpt(level, optName, optFlag);
	}
	private static synchronized Operation createNumberOpt(int level, String optName, String optFlag)
	{
		Operation[] optArr = (Operation[]) numOptCache.get(optName);
		if (optArr == null)
		{
			optArr = new Operation[NUM_TYPE_NAMES.length];
			numOptCache.put(optName, optArr);
		}
		Operation opt = optArr[level];
		if (opt == null)
		{
			String type = NUM_TYPE_NAMES[level];
			ClassGenerator cg = new ClassGenerator();
			cg.setClassName(ExprTool.class + "$numOpt" + level + "$" + optName);
			cg.setClassLoader(ExprTool.class.getClassLoader());
			cg.addInterface(Operation.class);
			char optType = optName.charAt(optName.length() - 1);
			String resName = optType == '1' ? "oneOpt_num" : optType == '2' ? "twoOpt_num"
					: optType == '3' ? "compareOpt_num" :"methodOpt_num";
			Map params = new HashMap();
			params.put("type", type);
			params.put("wrapType", BeanTool.getPrimitiveWrapClassName(type));
			params.put("opt", optFlag);
			cg.addMethod(codeRes.getRes(resName, params, 1));
			try
			{
				opt = (Operation) cg.createClass().newInstance();
				optArr[level] = opt;
			}
			catch (RuntimeException ex)
			{
				throw ex;
			}
			catch (Exception ex)
			{
				throw new EternaException(ex);
			}
		}
		return opt;
	}
	private static Map numOptCache = new HashMap();
	private static final String[] NUM_TYPE_NAMES = {
		"byte", "short", "int", "long", "float", "double"
	};

	/**
	 * 代码段资源.
	 */
	private static ResManager codeRes = new ResManager();
	static
	{
		try
		{
			codeRes.load(ExprTool.class.getResourceAsStream("ExprTool.res"));
		}
		catch (Exception ex)
		{
			Tool.log.error("Error in get code res.", ex);
		}
		specialList.add(new DefaultOptCreater());

		createrCache[VAR] = new VarCreater();
		createrCache[OBJ_MAP] = new ObjValueCreater();
		createrCache[OBJ_LIST] = createrCache[OBJ_MAP];

		createrCache[QUESTION] = new ThreeOptCreater();
		createrCache[ASSIGN] = new AssignOptCreater();
		createrCache[PLUS_ASSIGN] = createrCache[ASSIGN];

		createrCache[STRING_LITERAL] = new StringCreater();
		createrCache[CHAR_LITERAL] = createrCache[STRING_LITERAL];
		createrCache[NUM_INT] = new NumValueCreater();
		createrCache[NUM_LONG] = createrCache[NUM_INT];
		createrCache[NUM_DOUBLE] = createrCache[NUM_INT];
		createrCache[NUM_FLOAT] = createrCache[NUM_INT];
		createrCache[LITERAL_null] = createrCache[NUM_INT];
		createrCache[LITERAL_true] = createrCache[NUM_INT];
		createrCache[LITERAL_false] = createrCache[NUM_INT];

		createrCache[PLUS] = new PlusCreater();
		createrCache[MINUS] = new TwoOptCreater(
				AbstractExpression.TYPE_LEVLE_NUM, "MINUS2", "-");
		createrCache[STAR] = new TwoOptCreater(
				AbstractExpression.TYPE_LEVLE_NUM, "STAR2", "*");
		createrCache[DIV] = new TwoOptCreater(
				AbstractExpression.TYPE_LEVLE_NUM, "DIV2", "/");
		createrCache[MOD] = new TwoOptCreater(
				AbstractExpression.TYPE_LEVLE_NUM, "MOD2", "%");

		createrCache[SR] = new TwoOptCreater(
				AbstractExpression.TYPE_LEVLE_ONLY_INT, "SR2", ">>");
		createrCache[BSR] = new TwoOptCreater(
				AbstractExpression.TYPE_LEVLE_ONLY_INT, "BSR2", ">>>");
		createrCache[SL] = new TwoOptCreater(
				AbstractExpression.TYPE_LEVLE_ONLY_INT, "SL2", "<<");

		createrCache[BXOR] = new TwoOptCreater(
				AbstractExpression.TYPE_LEVLE_BOOL_NOT_INT, "BXOR2", "^");
		createrCache[BOR] = new TwoOptCreater(
				AbstractExpression.TYPE_LEVLE_BOOL_OR_INT, "BOR2", "|");
		createrCache[LOR] = new TwoOptCreater(
				AbstractExpression.TYPE_LEVLE_OR, "LOR2", "||");
		createrCache[BAND] = new TwoOptCreater(
				AbstractExpression.TYPE_LEVLE_BOOL_AND_INT, "BAND2", "&");
		createrCache[LAND] = new TwoOptCreater(
				AbstractExpression.TYPE_LEVLE_AND, "LAND2", "&&");

		createrCache[EQUAL] = new TwoOptCreater(
				AbstractExpression.TYPE_LEVLE_OBJ_EQUAL, "EQUAL3", "==");
		createrCache[NOT_EQUAL] = new TwoOptCreater(
				AbstractExpression.TYPE_LEVLE_OBJ_NOT_EQUAL, "NOT_EQUAL3", "!=");
		createrCache[GT] = new TwoOptCreater(
				AbstractExpression.TYPE_LEVLE_OBJ_MORE, "GT3", ">");
		createrCache[LT] = new TwoOptCreater(
				AbstractExpression.TYPE_LEVLE_OBJ_LESSL, "LT3", "<");
		createrCache[GE] = new TwoOptCreater(
				AbstractExpression.TYPE_LEVLE_OBJ_MORE_EQUAL, "GE3", ">=");
		createrCache[LE] = new TwoOptCreater(
				AbstractExpression.TYPE_LEVLE_OBJ_LESS_EQUAL, "LE3", "<=");

		createrCache[LNOT] = new OneOptCreater(
				AbstractExpression.TYPE_LEVLE_NOT, "LNOT1", "!");
		createrCache[BNOT] = new OneOptCreater(
				AbstractExpression.TYPE_LEVLE_ONLY_INT, "BNOT1", "~");
		createrCache[UNARY_MINUS] = new OneOptCreater(
				AbstractExpression.TYPE_LEVLE_NUM, "UNARY_MINUS1", "-");
		createrCache[UNARY_PLUS] = new OneOptCreater(
				AbstractExpression.TYPE_LEVLE_NUM, "UNARY_PLUS1", "+");

		createrCache[INC] = new VarOptCreater();
		createrCache[DEC] = createrCache[INC];
		createrCache[POST_INC] = createrCache[INC];
		createrCache[POST_DEC] = createrCache[INC];
		createrCache[DELETE] = createrCache[INC];
	}

}

