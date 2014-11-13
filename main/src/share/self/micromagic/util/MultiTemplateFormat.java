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

import java.util.ArrayList;
import java.util.Arrays;

import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.security.Permission;
import self.micromagic.eterna.sql.ResultFormat;
import self.micromagic.eterna.sql.ResultFormatGenerator;
import self.micromagic.eterna.sql.ResultRow;

/**
 * example
 * tamplet    id=[v0],value=[v1]
 * value      001,book
 * result     id=001,value=book
 */
public class MultiTemplateFormat extends TemplateFormat
		implements ResultFormat, ResultFormatGenerator
{
	public static final String VALUE_TAG_PATTERN = "[{name}{number}]";
	public static final String DEFAULT_VALUE_SPLIT = ",";

	protected String valueSplit;
	protected int[] indexs;
	protected int maxIndex;

	public String format(Object obj, Permission permission)
			throws ConfigurationException
	{
		if (this.needPermission != null && permission != null)
		{
			if (!permission.hasPermission(this.needPermission))
			{
				return obj == null ? "" :
						this.htmlFilter ? Utils.dealString2HTML(obj.toString(), true) : obj.toString();
			}
		}
		String temp = obj == null ? "" : obj.toString();
		int count = this.pattern.length() + temp.length();
		String[] values = new String[this.maxIndex + 1];
		Arrays.fill(values, "");

		int index = 0;
		int strI = temp.indexOf(this.valueSplit);
		int vsLength = this.valueSplit.length();
		while (strI != -1)
		{
			values[index++] = this.htmlFilter ?
					Utils.dealString2HTML(temp.substring(0, strI), true) : temp.substring(0, strI);
			temp = temp.substring(strI + vsLength);
			strI = temp.indexOf(this.valueSplit);
		}
		values[index++] = this.htmlFilter ? Utils.dealString2HTML(temp, true) : temp;

		StringAppender buf = StringTool.createStringAppender(count);
		for (int i = 0; i < this.patterns.length; i++)
		{
			if (i > 0)
			{
				buf.append(values[this.indexs[i - 1]]);
			}
			buf.append(this.patterns[i]);
		}
		return buf.toString();
	}

	public String format(Object obj, ResultRow row, Permission permission)
			throws ConfigurationException
	{
		return this.format(obj, permission);
	}

	protected void parseTemplate()
	{
		if (this.pattern == null)
		{
			this.pattern = "";
			this.patterns = StringTool.EMPTY_STRING_ARRAY;
		}
		String valueTag = (String) this.getAttribute("insert_value_tag");
		valueTag = valueTag == null ? DEFAULT_INSERT_VALUE_TAG : valueTag;
		String valueTagSub = valueTag.substring(0, valueTag.length() - 1);
		this.valueSplit = (String) this.getAttribute("value_split");
		this.valueSplit = this.valueSplit == null || this.valueSplit.length() == 0 ?
				DEFAULT_VALUE_SPLIT : this.valueSplit;
		this.needPermission = (String) this.getAttribute("format_permission");
		String filter = (String) this.getAttribute("html_filter");
		if (filter != null)
		{
			this.htmlFilter = "true".equalsIgnoreCase(filter);
		}
		filter = (String) this.getAttribute("root_format");
		ArrayList temp = new ArrayList();
		ArrayList tempNum = new ArrayList();
		int vtLength = valueTagSub.length();
		String str = this.pattern;
		int index = str.indexOf(valueTagSub);
		while (index != -1)
		{
			if (str.charAt(index + vtLength + 1) == ']')
			{
				temp.add(str.substring(0, index));
				tempNum.add(new Integer(str.substring(index + vtLength, index + vtLength + 1)));
				str = str.substring(index + vtLength + 2);
				index = str.indexOf(valueTagSub);
			}
			else if (str.charAt(index + vtLength + 2) == ']')
			{
				temp.add(str.substring(0, index));
				tempNum.add(new Integer(str.substring(index + vtLength, index + vtLength + 2)));
				str = str.substring(index + vtLength + 3);
				index = str.indexOf(valueTagSub);
			}
			else
			{
				index = str.indexOf(valueTagSub, index + 1);
			}
		}
		temp.add(str);
		this.patterns = (String[]) temp.toArray(new String[temp.size()]);
		this.indexs = new int[tempNum.size()];
		this.maxIndex = 1;
		for (int i = 0; i < this.indexs.length; i++)
		{
			this.indexs[i] = ((Integer) tempNum.get(i)).intValue();
			if (this.indexs[i] > this.maxIndex)
			{
				this.maxIndex = this.indexs[i];
			}
		}
	}

	public static void main(String[] args)
			throws ConfigurationException
	{
		MultiTemplateFormat t = new MultiTemplateFormat();
		t.setPattern("s[v1]dfdfds[v0]df[v4]gg");
		t.parseTemplate();
		System.out.println(java.util.Arrays.asList(t.patterns));
		System.out.println(t.format("--0--,,,,-<\n4-", null));
	}

}