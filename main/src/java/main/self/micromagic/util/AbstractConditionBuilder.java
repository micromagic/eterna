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

import self.micromagic.eterna.dao.preparer.PreparerCreater;
import self.micromagic.eterna.dao.preparer.ValuePreparer;
import self.micromagic.eterna.search.ConditionBuilder;
import self.micromagic.eterna.search.ConditionProperty;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;

/**
 * 一个抽象的ConditionBuilder的实现.
 */
public abstract class AbstractConditionBuilder extends AbstractGenerator
		implements ConditionBuilder
{
	protected EternaFactory factory;
	protected String caption;
	protected String operator;
	private String prepareName;
	private PreparerCreater prepare;

	public boolean initialize(EternaFactory factory)
	{
		if (this.factory == null)
		{
			this.factory = factory;
			if (!StringTool.isEmpty(this.prepareName))
			{
				this.prepare = factory.getPrepare(this.prepareName);
			}
			return false;
		}
		return true;
	}

	public String getCaption()
	{
		return this.caption;
	}

	public void setCaption(String caption)
	{
		this.caption = caption;
	}

	public void setOperator(String operator)
	{
		this.operator = operator;
	}

	/**
	 * 构造一个值准备器.
	 * 如果当前条件构造器设置了值准备器, 则使用它生成值准备器.
	 * 如果未设置, 则使用条件配置对象生成值准备器.
	 */
	protected ValuePreparer createValuePreparer(ConditionProperty cp, String value)
	{
		return this.prepare == null ? cp.createValuePreparer(value)
				: this.prepare.createPreparer(value);
	}

	/**
	 * 构造一个值准备器.
	 * 如果当前条件构造器设置了值准备器, 则使用它生成值准备器.
	 * 如果未设置, 则使用条件配置对象生成值准备器.
	 */
	protected ValuePreparer createValuePreparer(ConditionProperty cp, Object value)
	{
		return this.prepare == null ? cp.createValuePreparer(value)
				: this.prepare.createPreparer(value);
	}

	public void setPrepare(String prepare)
			throws EternaException
	{
		this.prepareName = prepare;
	}

	public PreparerCreater getPreparerCreater()
			throws EternaException
	{
		return this.prepare;
	}

	public Object create()
			throws EternaException
	{
		return this;
	}

}
