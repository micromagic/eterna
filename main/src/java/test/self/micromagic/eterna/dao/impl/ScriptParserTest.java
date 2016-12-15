/*
 * Copyright 2015 xinjunli (micromagic@sina.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package self.micromagic.eterna.dao.impl;

import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import tool.PrivateAccessor;

public class ScriptParserTest extends TestCase
{
	public void testCheckNeedQuote()
	{
		String name;

		name = "0";
		assertEquals(true, ScriptParser.checkNeedQuote(name));
		assertEquals("\"0\"", ScriptParser.checkNameForQuote(name));

		name = "a";
		assertEquals(false, ScriptParser.checkNeedQuote(name));
		assertEquals("a", ScriptParser.checkNameForQuote(name));

		name = "z";
		assertEquals(false, ScriptParser.checkNeedQuote(name));
		assertEquals("z", ScriptParser.checkNameForQuote(name));

		name = "A";
		assertEquals(false, ScriptParser.checkNeedQuote(name));
		assertEquals("A", ScriptParser.checkNameForQuote(name));

		name = "Z";
		assertEquals(false, ScriptParser.checkNeedQuote(name));
		assertEquals("Z", ScriptParser.checkNameForQuote(name));

		name = "a0";
		assertEquals(false, ScriptParser.checkNeedQuote(name));
		assertEquals("a0", ScriptParser.checkNameForQuote(name));

		name = "Z9";
		assertEquals(false, ScriptParser.checkNeedQuote(name));
		assertEquals("Z9", ScriptParser.checkNameForQuote(name));

		name = "0a";
		assertEquals(true, ScriptParser.checkNeedQuote(name));
		assertEquals("\"0a\"", ScriptParser.checkNameForQuote(name));

		name = "b_01";
		assertEquals(false, ScriptParser.checkNeedQuote(name));
		assertEquals("b_01", ScriptParser.checkNameForQuote(name));

		name = "T_01";
		assertEquals(false, ScriptParser.checkNeedQuote(name));
		assertEquals("T_01", ScriptParser.checkNameForQuote(name));

		name = "c.x";
		assertEquals(true, ScriptParser.checkNeedQuote(name));
		assertEquals("\"c.x\"", ScriptParser.checkNameForQuote(name));

		name = "order";
		assertEquals(true, ScriptParser.checkNeedQuote(name));
		assertEquals("\"order\"", ScriptParser.checkNameForQuote(name));

		name = "order2";
		assertEquals(false, ScriptParser.checkNeedQuote(name));
		assertEquals("order2", ScriptParser.checkNameForQuote(name));
	}

	public void testIsNumber()
			throws Exception
	{
		Object[] param;
		Class c = ScriptParser.class;
		param = new Object[]{"1"};
		assertEquals(Boolean.TRUE, PrivateAccessor.invoke(c, "isNumber", param));
		param = new Object[]{"0X1"};
		assertEquals(Boolean.TRUE, PrivateAccessor.invoke(c, "isNumber", param));
		param = new Object[]{"0Z1"};
		assertEquals(Boolean.FALSE, PrivateAccessor.invoke(c, "isNumber", param));
		param = new Object[]{"3X24E323"};
		assertEquals(Boolean.TRUE, PrivateAccessor.invoke(c, "isNumber", param));
		param = new Object[]{"33X2"};
		assertEquals(Boolean.FALSE, PrivateAccessor.invoke(c, "isNumber", param));
		param = new Object[]{"X2"};
		assertEquals(Boolean.FALSE, PrivateAccessor.invoke(c, "isNumber", param));
		param = new Object[]{"AF"};
		assertEquals(Boolean.FALSE, PrivateAccessor.invoke(c, "isNumber", param));
		param = new Object[]{"0XAF"};
		assertEquals(Boolean.TRUE, PrivateAccessor.invoke(c, "isNumber", param));
		param = new Object[]{"0XAFG"};
		assertEquals(Boolean.FALSE, PrivateAccessor.invoke(c, "isNumber", param));
	}

	public void testParseScript()
	{
		ScriptParser p = new ScriptParser();
		String text = "delete from a where 1 = 2";
		ScriptParser.Element[] r1 = p.parseScript(text, 0);
		assertEquals("delete", r1[0].getText());
		assertEquals(" ", r1[1].getText());
		assertEquals("=", r1[r1.length - 3].getText());
		r1 = p.parseScript(text, 1);
		assertEquals("DELETE", r1[0].getText());
		assertEquals("FROM", r1[1].getText());
		assertEquals("=", r1[r1.length - 2].getText());

		String script = " update TABLE t set t.a = ' 1\ns s'.75, t.b=0x3.14e12.a,"
				+ "t.c=t.d,\"x.d\"='d''d',#sub,#123?##?#?#param(a)[or x=?]#"
				+ " where t.b between 1.p? and 5 or a like '%a\\_b%' escape '\\'";
		List r = p.parseScript0(script);
		printElement(r);
		r = p.parseElement1(r);
		printElement(r);
		assertEquals(".75", ((ScriptParser.Element) r.get(9)).getText());
		assertEquals("0X3.14E12", ((ScriptParser.Element) r.get(15)).getText());
		assertEquals("'d''d'", ((ScriptParser.Element) r.get(29)).getText());
		assertEquals("#sub", ((ScriptParser.Element) r.get(31)).getText());

	}

	private static void printElement(List elements)
	{
		System.out.println("-----------------------------------------");
		Iterator itr = elements.iterator();
		while (itr.hasNext())
		{
			System.out.println(itr.next());
		}
	}

}
