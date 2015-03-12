/*
 * Copyright 2009-2015 xinjunli (micromagic@sina.com).
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

package self.micromagic.util;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class ResManagerTest extends TestCase
{
	private static ResManager res;

	protected void setUp()
			throws Exception
	{
		synchronized (ResManagerTest.class)
		{
			if (res == null)
			{
				res = new ResManager();
				res.load(this.getClass().getResourceAsStream("ResManagerTest.res"));
			}
		}
	}

	public void testIndentCode()
			throws Exception
	{
		StringAppender result, sa;
		String indentCode;

		sa = res.printRes("indent1", null, 0, null);
		indentCode = ResManager.indentCode(sa.toString(), 1);
		result = res.printRes("exp1", null, 0, null);
		assertEquals(indentCode, result.toString());

		sa = res.printRes("indent2", null, 0, null);
		indentCode = ResManager.indentCode(sa.toString(), 1);
		result = res.printRes("exp2", null, 0, null);
		assertEquals(indentCode, result.toString());
	}

	public void testPrint()
			throws Exception
	{
		StringAppender result, sa;

		sa = StringTool.createStringAppender();
		sa.append("void test()").appendln();
		sa.append('{').appendln();
		sa.append("   int i = 0;").appendln();
		sa.append("   i++;").appendln();
		sa.append('}');
		result = res.printRes("res1", null, 0, null);
		assertEquals(sa.toString(), result.toString());

		sa = StringTool.createStringAppender();
		sa.append("测试测试").appendln();
		sa.append("abcde ${param1} 12345").appendln();
		sa.append("结束 ${param2}").appendln();
		sa.append("${param3}");
		result = res.printRes("res2", null, 0, null);
		assertEquals(sa.toString(), result.toString());

		Map params = new HashMap();
		params.put("param1", "1");
		params.put("param2", "二");
		params.put("param3", "c");
		sa = StringTool.createStringAppender();
		sa.append("测试测试").appendln();
		sa.append("abcde 1 12345").appendln();
		sa.append("结束 二").appendln();
		sa.append("c");
		result = res.printRes("res2", params, 0, null);
		assertEquals(sa.toString(), result.toString());

		sa = StringTool.createStringAppender();
		sa.append("#test").appendln();
		sa.append("over");
		result = res.printRes("res3", null, 0, null);
		assertEquals(sa.toString(), result.toString());

		sa = StringTool.createStringAppender();
		sa.append("   #test").appendln();
		sa.append("   over");
		result = res.printRes("res3", null, 1, null);
		assertEquals(sa.toString(), result.toString());

		sa = StringTool.createStringAppender();
		sa.append("               #test").appendln();
		sa.append("               over");
		result = res.printRes("res3", null, 5, null);
		assertEquals(sa.toString(), result.toString());
	}

}