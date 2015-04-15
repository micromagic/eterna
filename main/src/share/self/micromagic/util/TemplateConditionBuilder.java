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
import java.math.BigDecimal;

import self.micromagic.util.Utility;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.search.ConditionBuilder;
import self.micromagic.eterna.search.ConditionBuilderGenerator;
import self.micromagic.eterna.search.ConditionProperty;
import self.micromagic.eterna.sql.preparer.ValuePreparer;

/**
 * example
 * tamplet    [C] IN (SELECT col1 FROM table1 WHERE id = ?)
 * column     theCol
 * result     theCol IN (SELECT col1 FROM table1 WHERE id = ?)
 */
public class TemplateConditionBuilder extends AbstractGenerator
		implements ConditionBuilder, ConditionBuilderGenerator
{
	public static final String DEFAULT_COLUMN_NAME_TAG = "[C]";
	public static final char COLUMN_NAME_SPLIT = ',';

	private String caption;
	private String template;
	protected int[] indexs;
	protected int maxIndex;
	private String[] patterns;
	private int paramCount = 1;

	public void initialize(EternaFactory factory)
	{
		this.factory = factory;
	}

	public void setCaption(String caption)
	{
		this.caption = caption;
	}

	public void setOperator(String operator)
	{
	}

	public Object create()
			throws EternaException
	{
		return this.createConditionBuilder();
	}

	public ConditionBuilder createConditionBuilder()
			throws EternaException
	{
		this.parseTemplate();
		return this;
	}

	private void parseTemplate()
	{
		this.template = (String) this.getAttribute("template");
		if (this.template == null)
		{
			this.template = "";
			this.patterns = StringTool.EMPTY_STRING_ARRAY;
		}
		String pCount = (String) this.getAttribute("param_count");
		if (pCount != null)
		{
			this.paramCount = Integer.parseInt(pCount);
		}
		String columnTag = (String) this.getAttribute("column_name_tag");
		columnTag = columnTag == null ? DEFAULT_COLUMN_NAME_TAG : columnTag;
		String columnTagSub = columnTag.substring(0, columnTag.length() - 1);
		ArrayList temp = new ArrayList();
		ArrayList tempNum = new ArrayList();
		int ctLength = columnTagSub.length();
		String str = this.template;
		int index = str.indexOf(columnTagSub);
		while (index != -1)
		{
			if (str.charAt(index + ctLength + 1) == ']')
			{
				temp.add(str.substring(0, index));
				tempNum.add(new Integer(str.substring(index + ctLength, index + ctLength + 1)));
				str = str.substring(index + ctLength + 2);
				index = str.indexOf(columnTagSub);
			}
			else if (str.charAt(index + ctLength + 2) == ']')
			{
				temp.add(str.substring(0, index));
				tempNum.add(new Integer(str.substring(index + ctLength, index + ctLength + 2)));
				str = str.substring(index + ctLength + 3);
				index = str.indexOf(columnTagSub);
			}
			else if (str.charAt(index + ctLength) == ']')
			{
				temp.add(str.substring(0, index));
				tempNum.add(Utility.INTEGER_0);
				str = str.substring(index + ctLength + 1);
				index = str.indexOf(columnTagSub);
			}
			else
			{
				index = str.indexOf(columnTagSub, index + 1);
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

	public String getCaption()
	{
		return this.caption;
	}

	public Condition buildeCondition(String colName, String value, ConditionProperty cp)
			throws EternaException
	{
		if (this.template.length() == 0)
		{
			return null;
		}

		String temp = colName;
		String[] colNames = new String[this.maxIndex + 1];
		Arrays.fill(colNames, "");
		int index = 0;
		int strI = temp.indexOf(COLUMN_NAME_SPLIT);
		while (strI != -1)
		{
			colNames[index++] = temp.substring(0, strI);
			temp = temp.substring(strI + 1);
			strI = temp.indexOf(COLUMN_NAME_SPLIT);
		}
		colNames[index++] = temp;

		int count = this.template.length() + (this.patterns.length - 1) * colName.length();
		StringAppender sqlPart = StringTool.createStringAppender(count);
		for (int i = 0; i < this.patterns.length; i++)
		{
			if (i > 0)
			{
				sqlPart.append(colNames[this.indexs[i - 1]]);
			}
			sqlPart.append(this.patterns[i]);
		}

		ValuePreparer[] preparers = new ValuePreparer[this.paramCount];
		if (this.paramCount > 0)
		{
			Arrays.fill(preparers, cp.createValuePreparer(value));
		}
		return new Condition(sqlPart.toString(), preparers);
	}

	public static void main(String[] args)
	{
		TemplateConditionBuilder t = new TemplateConditionBuilder();
		t.setAttribute("template", "[C0] IN (SELECT tcode FROM TTI_SCLASS WHERE [C1] = ?)");
		t.parseTemplate();
		System.out.println(t.indexs.length);
		System.out.println(new BigDecimal("4545.45"));
	}

}