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

import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.search.ConditionBuilderGenerator;
import self.micromagic.eterna.search.ConditionBuilder;
import self.micromagic.eterna.search.ConditionProperty;
import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.sql.preparer.ValuePreparer;

public class InConditionBuilder extends AbstractGenerator
		implements ConditionBuilderGenerator, ConditionBuilder
{
	private String caption;
	private String seperate = " \t\r\n";
	private boolean arrayParam = false;

	public void initialize(EternaFactory factory)
	{
		String needSpace = (String) this.getAttribute("needSpace");
		if (needSpace != null && "false".equalsIgnoreCase(needSpace))
		{
			this.seperate = "";
		}
		String seperate = (String) this.getAttribute("seperate");
		if (seperate != null)
		{
			this.seperate += seperate;
		}
		this.arrayParam = "true".equalsIgnoreCase("arrayParam");
	}

	public void setCaption(String caption)
	{
		this.caption = caption;
	}

	public String getCaption()
	{
		return this.caption;
	}

	public void setOperator(String operator) {}

	public ConditionBuilder createConditionBuilder()
	{
		return this;
	}

	public Object create()
	{
		return this;
	}

	public Condition buildeCondition(String colName, String value, ConditionProperty cp)
			throws ConfigurationException
	{
		if (value != null && value.length() > 0)
		{
			String[] values;
			if (!this.arrayParam)
			{
				values = StringTool.separateString(value, this.seperate);
			}
			else
			{
				AppData data = AppData.getCurrentData();
				values = (String[]) data.getRequestParameterMap().get(cp.getName());
			}
			if (values.length > 0)
			{
				StringAppender sqlPart = StringTool.createStringAppender(
						values.length * 3 + colName.length() + 6);
				ValuePreparer[] preparers = new ValuePreparer[values.length];
				sqlPart.append(colName).append(" IN (");
				for (int i = 0; i < values.length; i++)
				{
					String temp = values[i];
					if (i > 0)
					{
						sqlPart.append(", ");
					}
					sqlPart.append('?');
					preparers[i] = cp.createValuePreparer(temp);
				}
				sqlPart.append(')');
				return new Condition(sqlPart.toString(), preparers);
			}
		}
		return null;
	}

}