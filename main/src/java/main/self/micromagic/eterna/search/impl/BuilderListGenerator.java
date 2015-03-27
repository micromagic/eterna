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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.EternaCreater;

/**
 * 条件构造器列表的创建者.
 */
public class BuilderListGenerator extends AbstractGenerator
		implements EternaCreater
{
	/**
	 * 添加一个条件构造者.
	 */
	public void addBuilder(String name)
	{
		List tmp = this.builderNames;
		if (tmp != null)
		{
			tmp.add(name);
		}
	}
	private List builderNames = new ArrayList();

	public Object create()
			throws EternaException
	{
		if (this.builders == null)
		{
			throw new EternaException("The BuilderList hasn't initialized.");
		}
		return this.builders;
	}

	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		List builderNames = this.builderNames;
		if (builderNames == null)
		{
			return true;
		}
		this.builderNames = null;
		int count = builderNames.size();
		this.builders = new ArrayList(count);
		Iterator nameItr = builderNames.iterator();
		for (int i = 0; i < count; i++)
		{
			String bName = (String) nameItr.next();
			this.builders.add(factory.getConditionBuilder(bName));
		}
		this.builders = Collections.unmodifiableList(this.builders);
		return false;
	}
	private List builders;

	public Class getObjectType()
	{
		return List.class;
	}

	public boolean isSingleton()
	{
		return true;
	}

	public EternaFactory getFactory()
	{
		return (EternaFactory) this.factory;
	}

	public void destroy()
	{
	}

}