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

package self.micromagic.eterna.share;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;

public class OrderManagerTest extends TestCase
{
	public void testDoOrder()
	{
		StrHandler h = new StrHandler();
		List list;
		try
		{
			list = OrderManager.doOrder(makeList("deafcgb"), "a,b,c;d,h;", h);
		}
		catch (Exception ex)
		{
			System.out.println(ex);
		}

		list = OrderManager.doOrder(makeList("deafcgb"), "a,b,c", h);
		assertEquals("abcdefg", printList(list));
		list = OrderManager.doOrder(makeList("cdemfganhbijkcl"), "a,b,c;m,n", h);
		assertEquals("abcdefghijklmn", printList(list));
		list = OrderManager.doOrder(makeList("cdemfganhbijkcl"), ";a,b,c,m,n;", h);
		assertEquals("defgabcmnhijkl", printList(list));
		list = OrderManager.doOrder(makeList("cdemfganhbijkcl"), "a,b,c;i,l;d,g;m,n", h);
		assertEquals("abcdgefhiljkmn", printList(list));
	}

	public static List makeList(String str)
	{
		List result = new ArrayList(str.length());
		int count = str.length();
		for (int i = 0; i < count; i++)
		{
			result.add(str.substring(i, i + 1));
		}
		return result;
	}

	public static String printList(List list)
	{
		StringAppender buf = StringTool.createStringAppender();
		Iterator itr = list.iterator();
		while (itr.hasNext())
		{
			buf.append(itr.next().toString());
		}
		return buf.toString();
	}

}

class StrHandler
		implements OrderManager.NameHandler
{
	public String getContainerName()
	{
		return "test";
	}

	public String getName(Object obj)
	{
		return (String) obj;
	}

}
