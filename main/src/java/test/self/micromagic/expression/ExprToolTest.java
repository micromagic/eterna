
package self.micromagic.expression;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.DataHandler;
import self.micromagic.eterna.model.VarCache.VarInfo;
import self.micromagic.expression.antlr.ExprLexer;
import self.micromagic.expression.antlr.ExprParser;
import self.micromagic.expression.antlr.ExprTokenTypes;
import self.micromagic.expression.impl.NumValueCreater;
import self.micromagic.expression.impl.StringCreater;
import self.micromagic.util.FormatTool;
import self.micromagic.util.Utility;
import self.micromagic.util.ref.BooleanRef;
import antlr.CommonAST;

public class ExprToolTest extends TestCase
		implements ExprTokenTypes
{
	public void testParse()
			throws Exception
	{
		int i = 3;
		int test = 0;
		int tmpr = (i++ + 2) + (test = i);
		System.out.println(tmpr + "," + test);
		String t = (i > 2 && i++ > 0) ? "t" + i++ : "f" + i++;
		System.out.println(i + ":" + t);
		System.out.println(link(i++, i++, i++));
		DataHandler tmp = new DataHandler("tmp", false, false);
		tmp.setConfig("$test");
		AppData data = AppData.getCurrentData();
		String exp = "\"x:\" + (($test < 2 ? ($test := 9) : ($test += 5)) + 100);";
		ExprLexer lex = new ExprLexer(new StringReader(exp));
		ExprParser parser = new ExprParser(lex);
		parser.compoundStatement();
		Object obj = ExprTool.parseExpNode(parser.getAST());
		data.modelVars = data.varCache.createCache();
		setValue("$test", new Double(3.2));
		System.out.println("r:" + ((Expression) obj).getResult(data));
		System.out.println(tmp.getData(data, false));
		data.clearData();
	}

	private static void setValue(String name, Object value)
	{
		AppData data = AppData.getCurrentData();
		VarInfo[] varInfos = data.varCache.getInfos();
		for (int i = 0; i < varInfos.length; i++)
		{
			if (name.equals(varInfos[i].getName()))
			{
				data.modelVars[i] = value;
				break;
			}
		}
	}
	private static Object getValue(String name)
	{
		AppData data = AppData.getCurrentData();
		VarInfo[] varInfos = data.varCache.getInfos();
		for (int i = 0; i < varInfos.length; i++)
		{
			if (name.equals(varInfos[i].getName()))
			{
				return data.modelVars[i];
			}
		}
		return null;
	}

	public void testParseStatement2()
			throws Exception
	{
		AppData data = AppData.getCurrentData();
		String exprs = "$list := {1, 2};\n"
				+ "merge($list, 3, 4, 5)[2]--;\n"
				+ "$check1 := ++{1, 2}[$list[0] - 1];\n"
				+ "($map2 := {map@ name1:1,name2:2}).nameX := \"abc\";\n"
				+ "$check2 := ($map3 := {map@}).test1[2][$map2.nameX].name2 := 'c';\n"
				+ "$checkMap.a := $checkList[1] := 1;\n"
				+ "merge({map@ name1:1,name2:2}, const {map@ name3:3});\n";
		Object[] objs = ExprTool.parseExps(exprs, false);
		data.modelVars = data.varCache.createCache();
		for (int i = 0; i < objs.length; i++)
		{
			((Expression) objs[i]).getResult(data);
		}
		List checkList = new ArrayList();
		checkList.add(Utility.INTEGER_1);
		checkList.add(Utility.INTEGER_2);
		checkList.add(Utility.INTEGER_2);
		checkList.add(Utility.INTEGER_4);
		checkList.add(Utility.INTEGER_5);
		List list = (List) getValue("$list");
		assertEquals(5, list.size());
		assertEquals(checkList, list);
		Map checkMap = new HashMap();
		checkMap.put("name1", Utility.INTEGER_1);
		checkMap.put("name2", Utility.INTEGER_2);
		checkMap.put("nameX", "abc");
		assertEquals(checkMap, getValue("$map2"));
		assertEquals(Utility.INTEGER_2, getValue("$check1"));
		assertEquals(new Character('c'), getValue("$check2"));
		System.out.println(getValue("$map3"));
		checkMap.clear();
		checkMap.put("a", Utility.INTEGER_1);
		assertEquals(checkMap, getValue("$checkMap"));
		list = new ArrayList();
		list.add(null);
		list.add(Utility.INTEGER_1);
		assertEquals(list, getValue("$checkList"));
		data.clearData();
	}

	public void testParseStatement1()
			throws Exception
	{
		AppData data = AppData.getCurrentData();
		String exprs = "$list := {1, 2, 3, 4, 5};\n"
				+ "$map1 := {map@ \"a\":\"test\",'b':$list,'c':toDate(\"2015-12-12\")};\n"
				+ "$testName := $map1.a;\n"
				+ ";\n" // none
				+ "$map2 := {map@ $testName:1,name2:2,name3:3};\n"
				+ "link('a', 'b');\n" // const
				+ "$map1.b[5] := $map2;\n"
				+ "$check1 := 3 == $map1.b[5].name3 && $map2.test == 1;\n"
				+ "$check2 := isDate($map1[\"c\"]);\n"
				+ "$check3 := delete $map1.b[1] == 2;\n"
				+ "$check4 := length($map1.b) == 5;\n"
				+ "$check5 := length($map1.b[4]) == 3;\n"
				+ "$v1 := delete $map1.b[4].name2;\n"
				+ "$check6 := length($map1.b[4]) == 2;\n";
		Object[] objs = ExprTool.parseExps(exprs, false);
		assertEquals(12, objs.length);
		data.modelVars = data.varCache.createCache();
		for (int i = 0; i < objs.length; i++)
		{
			((Expression) objs[i]).getResult(data);
		}
		assertEquals(Boolean.TRUE, getValue("$check1"));
		assertEquals(Boolean.TRUE, getValue("$check2"));
		assertEquals(Boolean.TRUE, getValue("$check3"));
		assertEquals(Boolean.TRUE, getValue("$check4"));
		assertEquals(Boolean.TRUE, getValue("$check5"));
		assertEquals(Boolean.TRUE, getValue("$check6"));
		assertEquals(Utility.INTEGER_2, getValue("$v1"));
		data.clearData();
	}

	public void testParseDelete()
			throws Exception
	{
		AppData data = AppData.getCurrentData();
		Expression expression;
		String exp = "delete data.test";
		expression = createExp(exp);
		data.dataMap.put("test", "1");
		Object expResult = expression.getResult(data);

		assertEquals("1",  expResult);
		assertFalse(data.dataMap.containsKey("test"));

		exp = "delete data.test[2]";
		expression = createExp(exp);
		List tmpList = new ArrayList();
		tmpList.add("1");
		tmpList.add("2");
		tmpList.add("3");
		tmpList.add("4");
		data.dataMap.put("test", tmpList);
		expResult = expression.getResult(data);

		assertEquals("3",  expResult);
		assertEquals(3, tmpList.size());
		System.out.println("list: " + tmpList);
		data.clearData();
	}

	public void testParseExp2()
			throws Exception
	{
		AppData data = AppData.getCurrentData();
		Expression expression;
		Object r = FormatTool.parseDatetime("2015-12-12 00:00:00");
		String exp = "toDate(\"2015-12-12\")";
		expression = createExp(exp);
		Object expResult = expression.tryGetResult(null);
		assertEquals(r,  expResult);

		exp = "toDate(\"20151212000000\", \"yyyyMMddHHmmss\")";
		expression = createExp(exp);
		expResult = expression.tryGetResult(null);
		assertEquals(r,  expResult);

		exp = "toDate(\"20151212\", $format)";
		expression = createExp(exp);
		data.modelVars = data.varCache.createCache();
		setValue("$format", FormatTool.getCachedFormat("yyyyMMdd"));
		BooleanRef getted = new BooleanRef();
		expResult = expression.tryGetResult(getted);
		assertFalse(getted.value);
		expResult = expression.getResult(data);
		assertEquals(r,  expResult);
		data.clearData();
	}

	public void testParseExp1()
			throws Exception
	{
		Expression expression;
		int $i = 3;
		AppData data = AppData.getCurrentData();
		String exp = "($i > 2 && $i++ > 0) ? \"t\" + $i++ : \"f\" + $i++";
		expression = createExp(exp);
		data.modelVars = data.varCache.createCache();
		setValue("$i", Utility.createInteger($i));
		Object expResult = expression.getResult(data);

		String t = ($i > 2 && $i++ > 0) ? "t" + $i++ : "f" + $i++;
		assertEquals($i + ":" + t,  getValue("$i") + ":" + expResult);

		$i = 3;
		int $test = 0;
		exp = "$tmpR := ($i++ + 2) + ($test := $i)";
		expression = createExp(exp);
		data.modelVars = data.varCache.createCache();
		setValue("$i", Utility.createInteger($i));
		setValue("$test", Utility.createInteger($test));
		expResult = expression.getResult(data);

		int $tmpR = ($i++ + 2) + ($test = $i);
		assertEquals(getValue("$tmpR"), expResult);
		assertEquals($tmpR + "," + $test,  expResult + "," + getValue("$test"));

		$i = 3;
		exp = "link($i++, ',', ($i)++, ',', (($i))++)";
		expression = createExp(exp);
		data.modelVars = data.varCache.createCache();
		setValue("$i", Utility.createInteger($i));
		expResult = expression.getResult(data);

		assertEquals(link($i++, $i++, $i++),  expResult);

		data.clearData();
	}

	private Expression createExp(String exp)
			throws Exception
	{
		ExprLexer lex = new ExprLexer(new StringReader(exp + ";"));
		ExprParser parser = new ExprParser(lex);
		parser.compoundStatement();
		return (Expression) ExprTool.parseExpNode(parser.getAST());
	}

	public void testParseNumValue()
			throws Exception
	{
		NumValueCreater creater = new NumValueCreater();
		CommonAST node = new CommonAST();
		node.setText("1");
		node.setType(NUM_INT);
		assertEquals(new Integer(1), creater.create(node));
		node.setText("0xf");
		assertEquals(new Integer(0xf), creater.create(node));
		node.setText("0X1f");
		assertEquals(new Integer(0X1f), creater.create(node));

		node.setText("0X1fl");
		node.setType(NUM_LONG);
		assertEquals(new Long(0X1fl), creater.create(node));
		node.setText("-0X1fl");
		assertEquals(new Long(-0X1fl), creater.create(node));

		node.setText(".5f");
		node.setType(NUM_FLOAT);
		assertEquals(new Float(.5), creater.create(node));

		node.setText(".5d");
		node.setType(NUM_DOUBLE);
		assertEquals(new Double(.5), creater.create(node));
		node.setText(".5");
		assertEquals(new Double(.5), creater.create(node));
		node.setText("1.5E3");
		assertEquals(new Double(1.5E3), creater.create(node));

		node.setType(LITERAL_true);
		assertEquals(Boolean.TRUE, creater.create(node));

		node.setType(LITERAL_null);
		assertNull(creater.create(node));
	}

	public void testParseStr()
			throws Exception
	{
		StringCreater creater = new StringCreater();
		CommonAST node = new CommonAST();
		node.setText("\"d\\u003A:\\473\\047,\"");
		node.setType(STRING_LITERAL);
		assertEquals("d\u003a:\473\047,", creater.create(node));

		node.setText("\'x\';");
		node.setType(CHAR_LITERAL);
		assertEquals(new Character('x'), creater.create(node));

		node.setText("\'\\f\';");
		assertEquals(new Character('\f'), creater.create(node));
	}

	public void testNumberOpt()
	{
		testNumberOptAdd(ExprTool.DOUBLE_LEVEL, new Double(123.22), new Double(2.3), new Double(123.22 + 2.3));
		testNumberOptAdd(ExprTool.LONG_LEVEL, new Long(123345L), new Long(9323L), new Long(123345L + 9323L));
		testNumberOptNot(ExprTool.LONG_LEVEL, new Long(123345L), new Long(~123345L));
		testNumberOptNot(ExprTool.INTEGER_LEVEL, new Integer(123345), new Integer(~123345));
		testNumberOptNot(ExprTool.INTEGER_LEVEL, new Integer(123345), new Integer(~123345));
		testNumberOptMethod(ExprTool.DOUBLE_LEVEL, new Double(123.22), new Double(2.3), new Double(123.22));
		testNumberOptMethod(ExprTool.INTEGER_LEVEL, new Integer(1), new Integer(2), new Integer(2));
		testNumberOptCompare(ExprTool.INTEGER_LEVEL, new Integer(1), new Integer(2), Boolean.TRUE);
		testNumberOptCompare(ExprTool.DOUBLE_LEVEL, new Double(123.22), new Double(2.3), Boolean.FALSE);
	}

	private static void testNumberOptAdd(int level, Number num1, Number num2, Number result)
	{
		Operation opt = ExprTool.getNumberOpt(level, "add2", "+");
		Object tmp = opt.exec(num1, num2);
		assertEquals(result, tmp);
	}

	private static void testNumberOptNot(int level, Number num1, Number result)
	{
		Operation opt = ExprTool.getNumberOpt(level, "not1", "~");
		Object tmp = opt.exec(num1, null);
		assertEquals(result, tmp);
	}

	private static void testNumberOptMethod(int level, Number num1, Number num2, Number result)
	{
		Operation opt = ExprTool.getNumberOpt(level, "max4", "Math.max");
		Object tmp = opt.exec(num1, num2);
		assertEquals(result, tmp);
	}

	private static void testNumberOptCompare(int level, Number num1, Number num2, Boolean result)
	{
		Operation opt = ExprTool.getNumberOpt(level, "less3", "<");
		Object tmp = opt.exec(num1, num2);
		assertEquals(result, tmp);
	}

	private static String link(int a, int b, int c)
	{
		return a + "," + b + "," + c;
	}

	public void testParseOptStatement()
			throws Exception
	{
		AppData data = AppData.getCurrentData();
		String exprs = "$date := toDate(\"20151225121103\", \"yyyyMMddHHmmss\");\n"
				+ "$str := format($date, \"yyyy-MM-dd HH:mm:ss\");\n";
		Object[] objs = ExprTool.parseExps(exprs, false);
		data.modelVars = data.varCache.createCache();
		for (int i = 0; i < objs.length; i++)
		{
			((Expression) objs[i]).getResult(data);
		}
		assertEquals("2015-12-25 12:11:03", getValue("$str"));
	}

	public void testParseBaseStatement()
			throws Exception
	{
		AppData data = AppData.getCurrentData();
		String exprs = "$base1 := 1;\n"
				+ "$base2 := true;"
				+ "$r1 := $base1 + 2;\n"
				+ "$r2 := 3 - $base1;\n"
				+ "$base1 := $r3 := $base1 + $base1 * 2;\n"
				+ "$r4 := 2 * $base1;\n"
				+ "$r5 := $base1 / 2;\n"
				+ "$r6 := $base1 / 2.0;\n"
				+ "$r7 := $base1 % 2;\n"
				+ "$r8 := $base1 >> 1;\n"
				+ "$r9 := -$base1 >> 1;\n"
				+ "$r10 := -$base1 >>> 1;\n"
				+ "$r11 := $base1 << 2;\n"
				+ "$r12 := $base1 ^ 6;\n"
				+ "$r13 := $base2 ^ true;\n"
				+ "$r14 := $base1 | 6;\n"
				+ "$r15 := $base1 & 6;\n"
				+ "$r16 := $base2 | false;\n"
				+ "$r17 := $base2 & false;\n"
				+ "$r18 := $base2 || false;\n"
				+ "$r19 := $base2 && false;\n"
				+ "$r20 := $base1 == 3;\n"
				+ "$r21 := $base1 != 3;\n"
				+ "$r22 := $base1 > 3;\n"
				+ "$r23 := $base1 >= 3;\n"
				+ "$r24 := $base1 < 3;\n"
				+ "$r25 := $base1 <= 3;\n"
				+ "$r26 := +$base1;\n"
				+ "$r27 := -$base1;\n"
				+ "$r28 := ~$base1;\n"
				+ "$r29 := !$base2;\n"
				+ "$r30 := $base2 || $base1++ > 0;\n"
				+ "$r31 := $base1 == 3;\n"
				+ "$r32 := !$base2 && $base1++ > 0;\n"
				+ "$r33 := $base1 == 3;\n"
				+ "$r34 := $base2 | $base1++ > 0;\n"
				+ "$r35 := $base1 == 4;\n"
				+ "$r36 := !$base2 & $base1++ > 0;\n"
				+ "$r37 := $base1 == 5;\n"
				+ "$r38 := '0' + $base1;\n"
				+ "$r39 := $base1 + null;\n"
				+ "$r40 := '0' - $base1;\n";
		Object[] objs = ExprTool.parseExps(exprs, false);
		data.modelVars = data.varCache.createCache();
		for (int i = 0; i < objs.length; i++)
		{
			((Expression) objs[i]).getResult(data);
		}
		int base = 3;
		boolean baseBool = true;
		assertEquals(Utility.INTEGER_3, getValue("$r1"));
		assertEquals(Utility.INTEGER_2, getValue("$r2"));
		assertEquals(Utility.INTEGER_3, getValue("$r3"));
		assertEquals(Utility.INTEGER_6, getValue("$r4"));
		assertEquals(Utility.INTEGER_1, getValue("$r5"));
		assertEquals(new Double(1.5), getValue("$r6"));
		assertEquals(Utility.INTEGER_1, getValue("$r7"));
		assertEquals(Utility.INTEGER_1, getValue("$r8"));
		assertEquals(new Integer(-base >> 1), getValue("$r9"));
		assertEquals(new Integer(-base >>> 1), getValue("$r10"));
		assertEquals(new Integer(base << 2), getValue("$r11"));
		assertEquals(new Integer(base ^ 6), getValue("$r12"));
		assertEquals(Boolean.FALSE, getValue("$r13"));
		assertEquals(new Integer(base | 6), getValue("$r14"));
		assertEquals(new Integer(base & 6), getValue("$r15"));
		assertEquals(new Boolean(baseBool | false), getValue("$r16"));
		assertEquals(new Boolean(baseBool & false), getValue("$r17"));
		assertEquals(new Boolean(baseBool || false), getValue("$r18"));
		assertEquals(new Boolean(baseBool && false), getValue("$r19"));
		assertEquals(new Boolean(base == 3), getValue("$r20"));
		assertEquals(new Boolean(base != 3), getValue("$r21"));
		assertEquals(new Boolean(base > 3), getValue("$r22"));
		assertEquals(new Boolean(base >= 3), getValue("$r23"));
		assertEquals(new Boolean(base < 3), getValue("$r24"));
		assertEquals(new Boolean(base <= 3), getValue("$r25"));
		assertEquals(new Integer(+base), getValue("$r26"));
		assertEquals(new Integer(-base), getValue("$r27"));
		assertEquals(new Integer(~base), getValue("$r28"));
		assertEquals(new Boolean(!baseBool), getValue("$r29"));
		assertEquals(Boolean.TRUE, getValue("$r30"));
		assertEquals(Boolean.TRUE, getValue("$r31"));
		assertEquals(Boolean.FALSE, getValue("$r32"));
		assertEquals(Boolean.TRUE, getValue("$r33"));
		assertEquals(Boolean.TRUE, getValue("$r34"));
		assertEquals(Boolean.TRUE, getValue("$r35"));
		assertEquals(Boolean.FALSE, getValue("$r36"));
		assertEquals(Boolean.TRUE, getValue("$r37"));
		base = 5;
		assertEquals(new Integer('0' + base), getValue("$r38"));
		assertEquals(new Integer(base), getValue("$r39"));
		assertEquals(new Integer('0' - base), getValue("$r40"));
		data.clearData();
	}

}
