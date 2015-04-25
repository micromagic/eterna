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

package self.micromagic.eterna.model.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import self.micromagic.eterna.model.AppData;
import self.micromagic.grammer.GrammerElement;
import self.micromagic.grammer.GrammerManager;
import self.micromagic.grammer.ParserData;
import self.micromagic.util.Utility;

public class CheckExpressionTest extends TestCase
{
	public void test1()
	{
		CheckExpression e = CheckExpression.parseExpression("param.a isNull && (param.b > 2 | param.c hasNext)");
		AppData data = AppData.getCurrentData();
		Map map = new HashMap();
		map.put("b", Utility.createInteger(1));
		map.put("c", Arrays.asList(new String[1]).iterator());
		data.maps[AppData.REQUEST_PARAMETER_MAP] = map;
		assertEquals(true, e.execCheck(data));
		map.put("c", null);
		assertEquals(false, e.execCheck(data));
		map.put("b", Utility.createInteger(3));
		assertEquals(true, e.execCheck(data));
	}

	public void test2()
	{
		CheckExpression e = CheckExpression.parseExpression(
				"param.a notNull || param.d > 0 && param.b > 0 | param.c > 0"
				+ "|| param.e > 0 & param.f > 0 & param.g = 0");
		AppData data = AppData.getCurrentData();
		Map map = new HashMap();
		data.maps[AppData.REQUEST_PARAMETER_MAP] = map;
		assertEquals(false, e.execCheck(data));
		map.put("d", Utility.createInteger(1));
		map.put("b", Utility.createInteger(1));
		assertEquals(true, e.execCheck(data));
		map.put("b", Utility.createInteger(0));
		assertEquals(false, e.execCheck(data));
		map.put("c", Utility.createInteger(1));
		assertEquals(true, e.execCheck(data));
		map.put("c", Utility.createInteger(0));
		map.put("e", Utility.createInteger(1));
		assertEquals(false, e.execCheck(data));
		map.put("f", Utility.createInteger(1));
		map.put("g", Utility.createInteger(0));
		assertEquals(true, e.execCheck(data));
	}

	public void test3()
	{
		CheckExpression e = CheckExpression.parseExpression(
				"param.a notNull || param[d] > 0 && (param[b] > 0 | param.c > 0"
				+ "& param.e > 0)& param[f] > 0 & param.g = 0");
		AppData data = AppData.getCurrentData();
		Map map = new HashMap();
		data.maps[AppData.REQUEST_PARAMETER_MAP] = map;
		assertEquals(false, e.execCheck(data));
		map.put("d", Utility.createInteger(1));
		map.put("b", Utility.createInteger(1));
		map.put("c", Utility.createInteger(1));
		map.put("e", Utility.createInteger(1));
		map.put("f", Utility.createInteger(1));
		map.put("g", Utility.createInteger(0));
		assertEquals(true, e.execCheck(data));
		map.put("d", Utility.createInteger(0));
		assertEquals(false, e.execCheck(data));
		map.put("d", Utility.createInteger(1));
		map.put("b", Utility.createInteger(0));
		assertEquals(true, e.execCheck(data));
		map.put("c", Utility.createInteger(0));
		assertEquals(false, e.execCheck(data));
		map.put("c", Utility.createInteger(1));
		map.put("e", Utility.createInteger(0));
		assertEquals(false, e.execCheck(data));
		map.put("e", Utility.createInteger(1));
		map.put("f", Utility.createInteger(0));
		assertEquals(false, e.execCheck(data));
		map.put("f", Utility.createInteger(1));
		map.put("g", Utility.createInteger(1));
		assertEquals(false, e.execCheck(data));
	}

	public void test4()
	{
		AppData data = AppData.getCurrentData();
		Map map = new HashMap();
		data.maps[AppData.REQUEST_PARAMETER_MAP] = map;
		CheckExpression e = CheckExpression.parseExpression("param.a = null");
		assertEquals(true, e.execCheck(data));
		map.put("a", "");
		assertEquals(false, e.execCheck(data));

		e = CheckExpression.parseExpression("param.a != null");
		assertEquals(true, e.execCheck(data));
		map.put("a", null);
		assertEquals(false, e.execCheck(data));

		e = CheckExpression.parseExpression("not param.a != null");
		assertEquals(true, e.execCheck(data));
		map.put("a", "");
		assertEquals(false, e.execCheck(data));

		e = CheckExpression.parseExpression("!param.a==null");
		assertEquals(true, e.execCheck(data));
		map.put("a", null);
		assertEquals(false, e.execCheck(data));

		e = CheckExpression.parseExpression("!(param.a == null)");
		assertEquals(false, e.execCheck(data));
		map.put("a", "");
		assertEquals(true, e.execCheck(data));
	}

	public void test5()
	{
		AppData data = AppData.getCurrentData();
		Map map = new HashMap();
		data.maps[AppData.REQUEST_PARAMETER_MAP] = map;
		CheckExpression e = CheckExpression.parseExpression("param.a = \"123\\nx\"");
		assertEquals(false, e.execCheck(data));
		map.put("a", "");
		assertEquals(false, e.execCheck(data));
		map.put("a", null);
		assertEquals(false, e.execCheck(data));
		map.put("a", "123\nx");
		assertEquals(true, e.execCheck(data));
		e = CheckExpression.parseExpression("param[a]=\"123\\nx\"");
		assertEquals(true, e.execCheck(data));
		e = CheckExpression.parseExpression("param[\"\\t\"]=\"123\\nx\"");
		map.put("\t", "123\nx");
		map.remove("a");
		assertEquals(true, e.execCheck(data));
	}

	public void test6()
	{
		AppData data = AppData.getCurrentData();
		Map map = new HashMap();
		data.maps[AppData.REQUEST_PARAMETER_MAP] = map;
		CheckExpression e = CheckExpression.parseExpression("param.a = true");
		assertEquals(false, e.execCheck(data));
		map.put("a", Boolean.TRUE);
		assertEquals(true, e.execCheck(data));
		e = CheckExpression.parseExpression("bool param.a = true");
		map.put("a", "true");
		assertEquals(true, e.execCheck(data));
		e = CheckExpression.parseExpression("bool param.a = true | true");
		map.put("a", null);
		assertEquals(true, e.execCheck(data));
		e = CheckExpression.parseExpression("$x = double -1");
		data.modelVars = new Object[]{new Double(-1.0)};
		assertEquals(true, e.execCheck(data));
	}

	public void testParse()
			throws Exception
	{
		int a = 1, b = 1, c = 1, d = 1;
		System.out.println(":::" + (a > 0 || b > 0 && c > 0 ? false : d > 0));
		GrammerManager grammerManager = new GrammerManager();
		grammerManager.init(this.getClass().getClassLoader().getResource(
				"self/micromagic/eterna/model/grammer.xml").openStream());
		GrammerElement ge = grammerManager.getGrammerElement("expression_checker");
		ParserData pd = new ParserData("param.x._2[se][\"00\"] = testX \"12\\t3\" 0xd 034 || ($x>5e-3&&$d hasNext)");
		if (!ge.verify(pd))
		{
			System.out.println(pd.getMaxErrorBuffer());
		}
		else
		{
			printGrammerCell(pd.getGrammerCellLst(), "");
		}
	}

	private void printGrammerCell(List list, String blank)
	{
		if (list == null)
		{
			return;
		}
		Iterator itr = list.iterator();
		while (itr.hasNext())
		{
			ParserData.GrammerCell cell = (ParserData.GrammerCell) itr.next();
			int type = cell.grammerElement.getType();
			System.out.print(blank);
			System.out.print(cell.grammerElement.getName() + "(" + type + ")   ");
			if (cell.subCells == null)
			{
				System.out.println(cell.textBuf);
			}
			else
			{
				System.out.println();
			}
			printGrammerCell(cell.subCells, blank + "  ");
		}
	}

}
