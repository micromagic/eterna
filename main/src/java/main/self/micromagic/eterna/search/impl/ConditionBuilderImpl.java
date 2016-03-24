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

import self.micromagic.eterna.dao.preparer.PreparerCreater;
import self.micromagic.eterna.dao.preparer.ValuePreparer;
import self.micromagic.eterna.search.BuilderResult;
import self.micromagic.eterna.search.ConditionBuilder;
import self.micromagic.eterna.search.ConditionProperty;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.Tool;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.converter.BooleanConverter;

/**
 * builder的实现类.
 */
class ConditionBuilderImpl
		implements ConditionBuilder
{
	static final String EQUALS_OPT_TAG = "=";
	static final String LIKE_OPT_TAG = "LIKE";

	public ConditionBuilderImpl(String operator, boolean needValue)
	{
		this.operator = operator;
		this.needValue = needValue;
	}
	private final String operator;
	private final boolean needValue;
	boolean emptyToNull = true;

	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		if (this.factory != null)
		{
			return true;
		}
		this.factory = factory;
		if (!StringTool.isEmpty(this.prepareName))
		{
			this.prepare = factory.getPrepare(this.prepareName);
		}
		String tStr = (String) this.getAttribute(Tool.EMPTY_TO_NULL_FLAG);
		if (tStr == null)
		{
			tStr = (String) factory.getAttribute(Tool.EMPTY_TO_NULL_FLAG);
		}
		if (tStr != null)
		{
			this.emptyToNull = BooleanConverter.toBoolean(tStr);
		}
		return false;
	}
	private EternaFactory factory;
	String prepareName;

	public EternaFactory getFactory()
	{
		return this.factory;
	}

	public String getName()
			throws EternaException
	{
		return this.name;
	}
	String name;

	public String getCaption()
			throws EternaException
	{
		return this.caption;
	}
	String caption;

	public PreparerCreater getPreparerCreater()
			throws EternaException
	{
		return this.prepare;
	}
	private PreparerCreater prepare;

	public BuilderResult buildeCondition(String colName, Object value, ConditionProperty cp)
			throws EternaException
	{
		if (this.needValue)
		{
			if (value == null || (this.emptyToNull && StringTool.isEmpty(value)))
			{
				return this.getNullCheckCondition(colName);
			}

			int count = colName.length() + this.operator.length() + 3;
			StringAppender sqlPart = StringTool.createStringAppender(count);
			sqlPart.append(colName).append(' ').append(this.operator).append(" ?");
			PreparerCreater pCreater = this.getPreparerCreater();
			if (LIKE_OPT_TAG.equalsIgnoreCase(this.operator))
			{
				String strValue = value.toString();
				if (pCreater != null)
				{
					String newStr = BuilderGenerator.dealEscapeString(strValue);
					if (newStr != strValue)
					{
						value = strValue = newStr;
						sqlPart.append(" escape '\\'");
					}
				}
				else
				{
					// 对于没有设置prepare的, 默认加上escape
					sqlPart.append(" escape '\\'");
				}
			}
			ValuePreparer[] preparers = new ValuePreparer[1];
			if (pCreater == null)
			{
				preparers[0] = cp.createValuePreparer(value);
			}
			else
			{
				preparers[0] = pCreater.createPreparer(value);
			}
			return new BuilderResult(sqlPart.toString(), preparers);
		}
		else
		{
			int count = colName.length() + this.operator.length() + 1;
			StringAppender temp = StringTool.createStringAppender(count);
			temp.append(colName).append(' ').append(this.operator);
			return new BuilderResult(temp.toString());
		}
	}

	protected BuilderResult getNullCheckCondition(String colName)
	{
		boolean equalsFlag = EQUALS_OPT_TAG.equalsIgnoreCase(this.operator)
				|| LIKE_OPT_TAG.equalsIgnoreCase(this.operator);
		String temp = equalsFlag ? colName + " IS NULL" : colName + " IS NOT NULL";
		return new BuilderResult(temp);
	}

	public Object getAttribute(String name)
			throws EternaException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getAttributeNames()
			throws EternaException
	{
		// TODO Auto-generated method stub
		return null;
	}

}