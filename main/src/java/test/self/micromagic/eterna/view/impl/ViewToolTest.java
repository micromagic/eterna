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

package self.micromagic.eterna.view.impl;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import self.micromagic.grammer.GrammerElement;
import self.micromagic.grammer.GrammerManager;
import self.micromagic.grammer.ParserData;
import self.micromagic.util.Utility;

public class ViewToolTest extends TestCase
{
	private static GrammerManager grammerManager;

	protected void setUp() throws Exception
	{
		Class c = ViewTool.class;
		Field f = c.getDeclaredField("grammerManager");
		f.setAccessible(true);
		grammerManager = (GrammerManager) f.get(null);
	}

	protected void tearDown() throws Exception
	{
		grammerManager = null;
	}

	public void testParse()
	{

	}

	public static void main(String[] args) throws Exception
	{
		ViewToolTest tbm = new ViewToolTest();
		tbm.setUp();
		GrammerElement ge = grammerManager.getGrammerElement("resource_parser");
		String script = "var i = 0;\n"
				+ "if (i > 0{0})\n"
				+ "{"
				+ "{$ef:ttrrr}(a {10} + b, c);"
				+ "{$data:ttrrr}(a +{02} b, c);"
				+ "{$ ef:ttrrr}(a {101}+ b, c);"
				+ "}";
		//script = "{$ef:ttrrr}";
		ParserData pd = new ParserData(script);
		if (ge.verify(pd))
		{
			parseGrammerCell(pd.getGrammerCellLst());
		}
		else
		{
			System.out.println("Grammer error:" + script + "\n[maxBuf:" + pd.getMaxErrorBuffer() + "].");
		}
		tbm.tearDown();
	}

	private static void parseGrammerCell(List gclist)
	{
		if (gclist == null)
		{
			return;
		}
		Iterator itr = gclist.iterator();
		while (itr.hasNext())
		{
			ParserData.GrammerCell cell = (ParserData.GrammerCell) itr.next();
			int type = cell.grammerElement.getType();
			String name = cell.grammerElement.getName();
			System.out.print(GrammerElement.TYPE_NAMES[type]);
			System.out.print("," + name + ":");
			if (cell.subCells != null)
			{
				System.out.println();
				parseGrammerCell(cell.subCells);
			}
			else
			{
				System.out.println(cell.textBuf.length());
				System.out.println(cell.textBuf);
			}
		}
	}

	public void testCheckGrammer()
			throws Exception
	{
		Utility.setProperty(ViewTool.CHECK_GRAMMER_FLAG, "0");
		assertEquals(false, ViewTool.isCheckGrammer());
		Utility.setProperty(ViewTool.CHECK_GRAMMER_FLAG, "true");
		assertEquals(true, ViewTool.isCheckGrammer());
	}

}