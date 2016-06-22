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

package self.micromagic.util;

import java.util.Arrays;

import junit.framework.TestCase;

public class StringToolTest extends TestCase
{
	public void testSeparateString2()
	{
		String[] arr = {"a", "", "b", ""};
		assertEquals(Arrays.asList(arr), Arrays.asList(StringTool.separateString("a,,b,", ',', true, false)));
		assertEquals(Arrays.asList(arr), Arrays.asList(StringTool.separateString("a,,b,", ',', true, true)));
		arr = new String[]{"a", "", "b", "", ""};
		assertEquals(Arrays.asList(arr), Arrays.asList(StringTool.separateString("a ,  ,b ,  ,  ", ',', true, false)));
		assertEquals(Arrays.asList(arr), Arrays.asList(StringTool.separateString("a ,  ,b ,  ,  ", ',', true, true)));
		arr = new String[]{"a  b"};
		assertEquals(Arrays.asList(arr), Arrays.asList(StringTool.separateString("   a  b   ", ',', true, false)));
		assertEquals(Arrays.asList(arr), Arrays.asList(StringTool.separateString("   a  b   ", ',', true, true)));
		arr = new String[]{""};
		assertEquals(Arrays.asList(arr), Arrays.asList(StringTool.separateString("       ", ',', true, false)));
		assertEquals(Arrays.asList(arr), Arrays.asList(StringTool.separateString("       ", ',', true, true)));
		arr = new String[0];
		assertEquals(Arrays.asList(arr), Arrays.asList(StringTool.separateString("", ',', true, false)));
		assertEquals(Arrays.asList(arr), Arrays.asList(StringTool.separateString("", ',', true, true)));
		arr = new String[]{"\"a", "b \""};
		assertEquals(Arrays.asList(arr), Arrays.asList(StringTool.separateString(" \"a,b \"  ", ',', true, false)));

		arr = new String[]{"a,b "};
		assertEquals(Arrays.asList(arr), Arrays.asList(StringTool.separateString(" \"a,b \"  ", ',', true, true)));
		arr = new String[]{"a,b ", ""};
		assertEquals(Arrays.asList(arr), Arrays.asList(StringTool.separateString(" \"a,b \"  ,", ',', true, true)));
		arr = new String[]{"a,b "};
		assertEquals(Arrays.asList(arr), Arrays.asList(StringTool.separateString(" \"a,b \"", ',', true, true)));

		try
		{
			StringTool.separateString(" \"a,b \"x,", ',', true, true);
		}
		catch (RuntimeException ex)
		{
			System.out.println(ex);
		}
		try
		{
			StringTool.separateString(" \"a,b \",1\"\"", ',', true, true);
		}
		catch (RuntimeException ex)
		{
			System.out.println(ex);
		}
	}

	public void testCheck()
	{
		assertEquals("/a", StringTool.checkBegin("a", "/"));
		assertEquals("//b", StringTool.checkBegin("//b", "//"));
		assertEquals("b/", StringTool.checkEnd("b", "/"));
		assertEquals("000test", StringTool.checkEnd("000test", "test"));
		assertEquals("00ttest", StringTool.checkEnd("00t", "test"));
	}

	public void testRemove()
	{
		assertEquals("a", StringTool.removeBegin("a", "/"));
		assertEquals("b", StringTool.removeBegin("//b", "//"));
		assertEquals("b", StringTool.removeEnd("b/", "/"));
		assertEquals("000", StringTool.removeEnd("000test", "test"));
		assertEquals("00t", StringTool.removeEnd("00t", "test"));
	}

}

