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
import self.micromagic.eterna.search.BuilderResult;
import self.micromagic.eterna.search.ConditionBuilder;
import self.micromagic.eterna.search.ConditionProperty;
import self.micromagic.eterna.share.EternaFactory;

/**
 * 条件构造器的外敷类.
 */
public class ConditionBuilderWrapper
		implements ConditionBuilder
{
	public ConditionBuilderWrapper(String caption, ConditionBuilder base)
	{
		this.caption = caption;
		this.base = base;
	}
	private final String caption;
	private final ConditionBuilder base;

	/**
	 * 如果所给的是外敷条件构造器, 会获取原始的条件构造器.
	 * 此方法会递归执行.
	 */
	public static ConditionBuilder getBaseBuilder(ConditionBuilder base)
	{
		if (base instanceof ConditionBuilderWrapper)
		{
			return getBaseBuilder(((ConditionBuilderWrapper) base).getBaseBuilder());
		}
		return base;
	}

	/**
	 * 获取原始的条件构造器.
	 */
	public ConditionBuilder getBaseBuilder()
	{
		return this.base;
	}

	public boolean initialize(EternaFactory factory)
	{
		return this.base.initialize(factory);
	}

	public String getName()
	{
		return this.base.getName();
	}

	public String getCaption()
	{
		return this.caption;
	}

	public BuilderResult buildeCondition(String colName, Object value, ConditionProperty cp)
	{
		return this.base.buildeCondition(colName, value, cp);
	}

	public PreparerCreater getPreparerCreater()
	{
		return this.base.getPreparerCreater();
	}

	public Object getAttribute(String name)
	{
		return this.base.getAttribute(name);
	}

	public String[] getAttributeNames()
	{
		return this.base.getAttributeNames();
	}

}
