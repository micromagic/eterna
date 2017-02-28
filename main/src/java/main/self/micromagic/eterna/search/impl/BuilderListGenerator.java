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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import self.micromagic.eterna.search.ConditionBuilder;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaCreater;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.util.StringTool;

/**
 * 条件构造器列表的创建者.
 */
public class BuilderListGenerator extends AbstractGenerator
		implements EternaCreater
{
	/**
	 * 添加一个条件构造者.
	 */
	public void addBuilder(String name, String caption)
	{
		List tmp = this.builderInfos;
		if (tmp != null)
		{
			tmp.add(new BuilderInfoContainer(name, caption));
		}
	}
	private List builderInfos = new ArrayList();

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
		List tmpBuilderInfos = this.builderInfos;
		if (tmpBuilderInfos == null)
		{
			return true;
		}
		this.builderInfos = null;
		int count = tmpBuilderInfos.size();
		this.builders = new ArrayList(count);
		Iterator infoItr = tmpBuilderInfos.iterator();
		for (int i = 0; i < count; i++)
		{
			BuilderInfoContainer info = (BuilderInfoContainer) infoItr.next();
			ConditionBuilder builder = factory.getConditionBuilder(info.name);
			if (!StringTool.isEmpty(info.caption) && !info.caption.equals(builder.getCaption()))
			{
				builder = new ConditionBuilderWrapper(info.caption, builder);
			}
			this.builders.add(builder);
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

class BuilderInfoContainer
{
	public BuilderInfoContainer(String name, String caption)
	{
		this.name = name;
		this.caption = caption;
	}
	final String name;
	final String caption;

}
