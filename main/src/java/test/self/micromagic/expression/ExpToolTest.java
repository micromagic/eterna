
package self.micromagic.expression;

import java.io.StringReader;

import junit.framework.TestCase;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.DataHandler;
import self.micromagic.eterna.model.VarCache.VarInfo;
import self.micromagic.expression.antlr.ExpLexer;
import self.micromagic.expression.antlr.ExpParser;
import self.micromagic.expression.antlr.ExpTokenTypes;
import self.micromagic.expression.impl.NumValueCreater;
import self.micromagic.expression.impl.StringCreater;
import self.micromagic.util.Utility;
import antlr.CommonAST;

public class ExpToolTest extends TestCase
		implements ExpTokenTypes
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
		String exp = "\"x:\" + (($test < 2 ? ($test = 9) : ($test += 5)) + 100);";
		ExpLexer lex = new ExpLexer(new StringReader(exp));
		ExpParser parser = new ExpParser(lex);
		parser.compoundStatement();
		Object obj = ExpTool.parseExpNode(parser.getAST());
		data.modelVars = data.varCache.createCache();
		setValue("$test", new Double(3.2));
		System.out.println("r:" + ((Expression) obj).getResult(data));
		System.out.println(tmp.getData(data, false));
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


	public void testParseExp()
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
		exp = "$tmpR = ($i++ + 2) + ($test = $i)";
		expression = createExp(exp);
		data.modelVars = data.varCache.createCache();
		setValue("$i", Utility.createInteger($i));
		setValue("$test", Utility.createInteger($test));
		expResult = expression.getResult(data);

		int $tmpR = ($i++ + 2) + ($test = $i);
		assertEquals(getValue("$tmpR"), expResult);
		assertEquals($tmpR + "," + $test,  expResult + "," + getValue("$test"));

		$i = 3;
		exp = "link($i++, ',', $i++, ',', $i++)";
		expression = createExp(exp);
		data.modelVars = data.varCache.createCache();
		setValue("$i", Utility.createInteger($i));
		expResult = expression.getResult(data);

		assertEquals(link($i++, $i++, $i++),  expResult);
	}

	private Expression createExp(String exp)
			throws Exception
	{
		ExpLexer lex = new ExpLexer(new StringReader(exp + ";"));
		ExpParser parser = new ExpParser(lex);
		parser.compoundStatement();
		return (Expression) ExpTool.parseExpNode(parser.getAST());
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
		testNumberOptAdd(ExpTool.DOUBLE_LEVEL, new Double(123.22), new Double(2.3), new Double(123.22 + 2.3));
		testNumberOptAdd(ExpTool.LONG_LEVEL, new Long(123345L), new Long(9323L), new Long(123345L + 9323L));
		testNumberOptNot(ExpTool.LONG_LEVEL, new Long(123345L), new Long(~123345L));
		testNumberOptNot(ExpTool.INTEGER_LEVEL, new Integer(123345), new Integer(~123345));
		testNumberOptNot(ExpTool.INTEGER_LEVEL, new Integer(123345), new Integer(~123345));
		testNumberOptMethod(ExpTool.DOUBLE_LEVEL, new Double(123.22), new Double(2.3), new Double(123.22));
		testNumberOptMethod(ExpTool.INTEGER_LEVEL, new Integer(1), new Integer(2), new Integer(2));
		testNumberOptCompare(ExpTool.INTEGER_LEVEL, new Integer(1), new Integer(2), Boolean.TRUE);
		testNumberOptCompare(ExpTool.DOUBLE_LEVEL, new Double(123.22), new Double(2.3), Boolean.FALSE);
	}

	private static void testNumberOptAdd(int level, Number num1, Number num2, Number result)
	{
		Operation opt = ExpTool.getNumberOpt(level, "add2", "+");
		Object tmp = opt.exec(num1, num2);
		assertEquals(result, tmp);
	}

	private static void testNumberOptNot(int level, Number num1, Number result)
	{
		Operation opt = ExpTool.getNumberOpt(level, "not1", "~");
		Object tmp = opt.exec(num1, null);
		assertEquals(result, tmp);
	}

	private static void testNumberOptMethod(int level, Number num1, Number num2, Number result)
	{
		Operation opt = ExpTool.getNumberOpt(level, "max4", "Math.max");
		Object tmp = opt.exec(num1, num2);
		assertEquals(result, tmp);
	}

	private static void testNumberOptCompare(int level, Number num1, Number num2, Boolean result)
	{
		Operation opt = ExpTool.getNumberOpt(level, "less3", "<");
		Object tmp = opt.exec(num1, num2);
		assertEquals(result, tmp);
	}

	private static String link(int a, int b, int c)
	{
		return a + "," + b + "," + c;
	}

}
