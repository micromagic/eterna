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

import junit.framework.TestCase;

public class MultiLineTextTest extends TestCase
{
	MultiLineText bodyText;

	protected void setUp()
	{
		this.bodyText = new MultiLineText();
	}

	protected void tearDown()
	{
		this.bodyText = null;
	}

	public void testSkipEmptyEndsLine()
	{
		assertEquals("a", MultiLineText.skipEmptyEndsLine("\na\n"));
		assertEquals("a", MultiLineText.skipEmptyEndsLine("\r\na\r\n"));
		assertEquals("", MultiLineText.skipEmptyEndsLine("\r\n\r\n"));
		assertEquals("", MultiLineText.skipEmptyEndsLine("\r\n\n"));
		assertEquals("", MultiLineText.skipEmptyEndsLine("\n\n"));
		assertEquals("", MultiLineText.skipEmptyEndsLine("\r\n"));
		assertEquals("", MultiLineText.skipEmptyEndsLine("\n"));
		assertEquals("\ra", MultiLineText.skipEmptyEndsLine("\n\ra"));
		assertEquals("\na\r\n", MultiLineText.skipEmptyEndsLine("\n\na\r\n\r\n"));
	}

	public void testAppend001()
	{
		String str1 = "  001   ";
		this.bodyText.append(str1.toCharArray(), 0, str1.length());
		String str2 = "    \n      002";
		this.bodyText.append(str2.toCharArray(), 0, str2.length());
		String str3 = "    ";
		this.bodyText.append(str3.toCharArray(), 0, str3.length());
		String str4 = "                 ";
		this.bodyText.append(str4.toCharArray(), 0, str4.length());
		String str5 = "      \n      end     ";
		this.bodyText.append(str5.toCharArray(), 0, str5.length());
		assertEquals(str1 + str2 + str3 + str4 + str5, this.bodyText.toString());
		assertEquals("001\n002\nend     ", this.bodyText.trimEveryLineSpace(false));
		assertEquals("001 002 end     ", this.bodyText.trimEveryLineSpace(true));
	}

}