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

import self.micromagic.eterna.search.ConditionBuilder;
import self.micromagic.eterna.search.ConditionBuilderGenerator;
import self.micromagic.eterna.search.ConditionProperty;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.sql.preparer.ValuePreparer;

public class MultiConditionBuilder extends AbstractGenerator
		implements ConditionBuilder, ConditionBuilderGenerator
{
	private static final String PARAMETER_FLAG = "?";

	private String caption;
	private String template;
	private int paramCount;

	public void initialize() {}

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
			throws EternaException
	{
		this.template = (String) this.getAttribute("template");
		if (this.template == null)
		{
			throw new EternaException("You must give param template.");
		}
		this.paramCount = 0;
		int index = this.template.indexOf(PARAMETER_FLAG);
		while (index != -1)
		{
			this.paramCount ++;
			index = this.template.indexOf(PARAMETER_FLAG, index + 1);
		}
	}

	public String getCaption()
	{
		return this.caption;
	}

	public Condition buildeCondition(String colName, String value, ConditionProperty cp)
			throws EternaException
	{
		String temp = value.length() == 0 || this.paramCount == 0 ? "%" : "%" + value + "%";
		ValuePreparer[] preparers = new ValuePreparer[this.paramCount];
		for (int i = 0; i < preparers.length; i++)
		{
			preparers[i] = cp.createValuePreparer(temp);
		}
		return new Condition(this.template, preparers);
	}

}