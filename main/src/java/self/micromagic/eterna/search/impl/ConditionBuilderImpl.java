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

package self.micromagic.eterna.search.impl;

import self.micromagic.eterna.search.ConditionBuilder;
import self.micromagic.eterna.search.ConditionProperty;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.sql.preparer.ValuePreparer;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;

/**
 * @author micromagic@sina.com
 */
class ConditionBuilderImpl
		implements ConditionBuilder
{
	static final String EQUALS_OPT_TAG = "=";
	static final String LIKE_OPT_TAG = "LIKE";
	static final String CHECK_OPT_TAG = "CHECK";

	String name;
	String caption;
	private String operator;

	/**
	 *   -1 : 是否为空的判断
	 *    0 : 一般情况
	 *  0x1 : 针对字符串, 在后面加上通配符
	 *  0x2 : 针对字符串, 在前面加上通配符
	 *  0x3 : 针对字符串, 在两边加上通配符
	 */
	private int optType;

	public ConditionBuilderImpl(String operator, int optType)
	{
		this.operator = operator;
		this.optType = optType;
	}

	public void initialize(EternaFactory factory)
			throws ConfigurationException
	{
	}

	public String getName()
			throws ConfigurationException
	{
		return this.name;
	}

	public String getCaption()
			throws ConfigurationException
	{
		return this.caption;
	}

	public Condition buildeCondition(String colName, String value, ConditionProperty cp)
			throws ConfigurationException
	{
		if (this.optType == -1)
		{
			if (CHECK_OPT_TAG.equalsIgnoreCase(this.operator))
			{
				return "1".equals(value) ? new Condition(colName + " IS NULL")
						: new Condition(colName + " IS NOT NULL");
			}
			else
			{
				int count = colName.length() + this.operator.length() + 1;
				StringAppender temp = StringTool.createStringAppender(count);
				temp.append(colName).append(' ').append(this.operator);
				return new Condition(temp.toString());
			}
		}

		if (value == null || value.length() == 0)
		{
			return this.getNullCheckCondition(colName);
		}

		int count = colName.length() + this.operator.length() + 3;
		StringAppender sqlPart = StringTool.createStringAppender(count);
		sqlPart.append(colName).append(' ').append(this.operator).append(" ?");
		ValuePreparer[] preparers = new ValuePreparer[1];
		if (TypeManager.isTypeString(cp.getColumnType()))
		{
			if (LIKE_OPT_TAG.equalsIgnoreCase(this.operator))
			{
				if (this.optType == 0)
				{
					// 对于match, 默认加上escape
					sqlPart.append(" escape '\\'");
				}
				else
				{
					String newStr = ConditionBuilderGeneratorImpl.dealEscapeString(value);
					if (newStr != value)
					{
						value = newStr;
						sqlPart.append(" escape '\\'");
					}
				}
			}
			String strValue = "";
			if (this.optType == 0)
			{
				strValue = value;
			}
			else
			{
				StringAppender temp = StringTool.createStringAppender(value.length() + 2);
				if ((this.optType & 0x2) != 0)
				{
					temp.append('%');
				}
				temp.append(value);
				if ((this.optType & 0x1) != 0)
				{
					temp.append('%');
				}
				strValue = temp.toString();
			}
			preparers[0] = cp.createValuePreparer(strValue);
		}
		else
		{
			String opt = this.operator;
			if (LIKE_OPT_TAG.equalsIgnoreCase(opt))
			{
				opt = "=";
			}
			preparers[0] = cp.createValuePreparer(value);
		}
		return new Condition(sqlPart.toString(), preparers);
	}

	protected Condition getNullCheckCondition(String colName)
	{
		boolean equalsFlag = EQUALS_OPT_TAG.equalsIgnoreCase(this.operator)
				|| LIKE_OPT_TAG.equalsIgnoreCase(this.operator);
		String temp = equalsFlag ? colName + " IS NULL" : colName + " IS NOT NULL";
		return new Condition(temp);
		/*
		对于字符串的情况, 这里不做是否为空字符串的判断, 如果需要可以重写这个方法,
		然后在配置中定义自己的builder
		if (TypeManager.isTypeString(cp.getColumnType()))
		{
			String tempOp1, tempOp2;
			String linkOp;
			if ("=".equals(this.operator))
			{
				tempOp1 = " IS NULL";
				tempOp2 = " = ''";
				linkOp = " OR ";
			}
			else if (LIKE_OPT_TAG.equalsIgnoreCase(this.operator))
			{
				tempOp1 = " LIKE '%'";
				tempOp2 = " IS NULL";
				linkOp = " OR ";
			}
			else
			{
				tempOp1 = " IS NOT NULL";
				tempOp2 = " <> ''";
				linkOp = " AND ";
			}
			int count = colName.length() + tempOp1.length() + tempOp2.length() + 8;
			count = count * 2;
			StringAppender temp = StringTool.createStringAppender(count);
			temp.append('(').append(colName).append(tempOp1).append(linkOp);
			temp.append(colName).append(tempOp2).append(')');
			return new Condition(temp.toString());
		}
		*/
	}

	protected ConditionBuilderImpl copy()
	{
		ConditionBuilderImpl result = new ConditionBuilderImpl(this.operator, this.optType);
		result.name = this.name;
		result.caption = this.caption;
		return result;
	}

}