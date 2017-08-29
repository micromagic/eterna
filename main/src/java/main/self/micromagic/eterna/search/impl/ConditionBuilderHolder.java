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
 * builder的占位对象.
 * 在设置默认条件构造器名称时使用.
 */
public class ConditionBuilderHolder
		implements ConditionBuilder
{
	private final String name;

	public ConditionBuilderHolder(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return this.name;
	}

	public String toString()
	{
		return this.getName();
	}

	public boolean initialize(EternaFactory factory)
	{
		return false;
	}

	public BuilderResult buildeCondition(String colName, Object value,
			ConditionProperty cp)
	{
		return null;
	}

	public Object getAttribute(String name)
	{
		return null;
	}

	public String[] getAttributeNames()
	{
		return null;
	}

	public String getCaption()
	{
		return null;
	}

	public PreparerCreater getPreparerCreater()
	{
		return null;
	}

}
