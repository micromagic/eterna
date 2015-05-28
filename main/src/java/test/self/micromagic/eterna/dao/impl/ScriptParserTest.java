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
		List r = ScriptParser.parseScript("delete from a where 1 = 2");
		printElement(r);
		String script = " update TABLE t set t.a = ' 1\ns s'.75, t.b=0x3.14e12.a,"
				+ "t.c=t.d,\"x.d\"='d''d',#sub,#123?##?#?#param(a)[or x=?]#"
				+ " where t.b between 1.p? and 5 or a like '%a\\_b%' escape '\\'";
		r = ScriptParser.parseScript(script);
		printElement(r);
		r = ScriptParser.parseElement1(r);
		printElement(r);
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
