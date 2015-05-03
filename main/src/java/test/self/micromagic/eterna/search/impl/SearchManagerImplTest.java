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

package self.micromagic.eterna.search.impl;

import java.util.ArrayList;
import java.util.HashMap;

import junit.framework.TestCase;
import self.micromagic.eterna.TestTool;
import self.micromagic.eterna.digester2.ContainerManager;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.search.ConditionInfo;
import self.micromagic.eterna.search.Search;
import self.micromagic.eterna.search.SearchManager;
import self.micromagic.eterna.share.FactoryContainer;
import self.micromagic.util.ref.StringRef;
import tool.PrivateAccessor;

public class SearchManagerImplTest extends TestCase
{
	public void testStructCondition()
			throws Exception
	{
		FactoryContainer c = ContainerManager.createFactoryContainer("searchTest1",
				"cp:/self/micromagic/eterna/search/impl/search_test1.xml", null);
		Search s = (Search) c.getFactory().createObject("s1");
		AppData data = TestTool.getAppData(new HashMap());
		SearchManager m = s.getSearchManager(data);
		Object[] params = new Object[5];
		params[1] = Boolean.FALSE;
		params[2] = s;
		params[3] = new ArrayList();
		params[4] = new StringRef();

		params[0] = makeStruct("c1", "1", "and", "", 1);
		Object r = PrivateAccessor.invoke(m, "makeCondition", params);
		assertEquals("(c1 = ?)", r);
		assertEquals("AND", params[4].toString());

		params[0] = makeStruct("c1", "1", "or", "include", 2);
		r = PrivateAccessor.invoke(m, "makeCondition", params);
		assertEquals("(c1 LIKE ? OR c1 LIKE ?)", r);
		assertEquals("OR", params[4].toString());

		Object[] tmp1 = makeStruct("c1", "1", "and", "", 3);
		tmp1[0] = new Object[]{new Object[0]};
		params[0] = tmp1;
		r = PrivateAccessor.invoke(m, "makeCondition", params);
		assertEquals("(c1 = ? AND c1 = ?)", r);
		assertEquals("AND", params[4].toString());

		Object[] tmp2 = makeStruct("c1", "1", "and", "", 3);
		tmp2[1] = new Object[]{makeStruct("c1", "1", "or", "", 1)};
		params[0] = tmp2;
		r = PrivateAccessor.invoke(m, "makeCondition", params);
		assertEquals("(c1 = ? OR ((c1 = ?)) AND c1 = ?)", r);
		assertEquals("AND", params[4].toString());

		tmp2[1] = makeStruct("c1", "1", "or", "less", 2);
		tmp1[0] = tmp2;
		params[0] = tmp1;
		r = PrivateAccessor.invoke(m, "makeCondition", params);
		assertEquals("((c1 = ? OR (c1 < ? OR c1 < ?) AND c1 = ?) AND c1 = ? AND c1 = ?)", r);
		assertEquals("AND", params[4].toString());
	}

	/**
	 * 构造一个结构化的条件.
	 */
	static Object[] makeStruct(String name, String value,
			String linkOpt, String builderName, int count)
	{
		Object[] arr = new Object[count];
		for (int i = 0; i < count; i++)
		{
			arr[i] = new ConditionInfo(name, linkOpt, value, builderName);
		}
		return arr;
	}

}
