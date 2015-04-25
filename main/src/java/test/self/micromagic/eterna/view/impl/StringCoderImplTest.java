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

import java.io.Writer;

import junit.framework.TestCase;
import self.micromagic.util.StringTool;

public class StringCoderImplTest extends TestCase
{
	StringCoderImpl stringCoder;

	protected void setUp()
			throws Exception
	{
		this.stringCoder = new StringCoderImpl();
		//this.factory = EternaFactoryCreater.getEternaFactory(this.getClass());
	}

	protected void tearDown()
			throws Exception
	{
		this.stringCoder = null;
	}

	public void testParseJsonRefName()
	{
		assertEquals("", this.stringCoder.parseJsonRefName(null));
		assertEquals(".test", this.stringCoder.parseJsonRefName("test"));
		assertEquals("[\"1test\"]", this.stringCoder.parseJsonRefName("1test"));
		assertEquals("._T", this.stringCoder.parseJsonRefName("_T"));
		assertEquals(".$Z", this.stringCoder.parseJsonRefName("$Z"));
		assertEquals(".a0", this.stringCoder.parseJsonRefName("a0"));
		assertEquals(".z9", this.stringCoder.parseJsonRefName("z9"));
		assertEquals(".A_$", this.stringCoder.parseJsonRefName("A_$"));
		assertEquals("[\"a.9\"]", this.stringCoder.parseJsonRefName("a.9"));
		assertEquals(".__", this.stringCoder.parseJsonRefName("__"));
		assertEquals(".$$", this.stringCoder.parseJsonRefName("$$"));
		assertEquals(".abcdefghijklmnopqrstuvwxyz", this.stringCoder.parseJsonRefName("abcdefghijklmnopqrstuvwxyz"));
		assertEquals(".ABCDEFGHIJKLMNOPQRSTUVWXYZ", this.stringCoder.parseJsonRefName("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
		assertEquals("._$0123456789", this.stringCoder.parseJsonRefName("_$0123456789"));
		assertEquals("[\"abcdefghijklmnopqrstuvwxyz0123456789\"]", this.stringCoder.parseJsonRefName("abcdefghijklmnopqrstuvwxyz0123456789"));
	}

	public void testToJsonString()
			throws Exception
	{
		Writer sw = StringTool.createWriter();
		this.stringCoder.toJsonString(sw, "12\"ab.c");
		this.stringCoder.toJsonString(sw, "\n|\f|\\|/");
		this.stringCoder.toJsonString(sw, "<");
		this.stringCoder.toJsonString(sw, "'");
		assertEquals("12\\\"ab.c\\n|\\f|\\\\|\\/\\u003c\\'", sw.toString());

		String str1 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		str1 += str1;
		String str2 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";
		str2 += str2;
		sw = StringTool.createWriter();
		this.stringCoder.toJsonString(sw, str1 + "'" + str2);
		System.out.println(sw.toString());
		System.out.println(str1 + "\\'" + str2);
		assertEquals(str1 + "\\'" + str2, sw.toString());
	}

}