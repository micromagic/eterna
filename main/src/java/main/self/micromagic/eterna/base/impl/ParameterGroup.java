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

package self.micromagic.eterna.base.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import self.micromagic.eterna.base.EntityItem;
import self.micromagic.eterna.base.EntityRef;
import self.micromagic.eterna.base.ParameterGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.util.container.UnmodifiableIterator;

/**
 * @author micromagic@sina.com
 */
public class ParameterGroup
{
	/**
	 * 在item的arrtibute中设置使用prepare的名称.
	 */
	public static final String PREPARE_FLAG = "prepare";

	private boolean initialized;

	private List tmpParamList = new LinkedList();
	private Map tmpParamSet = new HashMap();
	private final List paramGeneratorList = new LinkedList();

	public ParameterGroup(String name, String type)
	{
		this.name = name;
		this.objType = type;
	}
	private final String name;
	private final String objType;

	/**
	 * 初始化相关Parameter对象.
	 */
	public void initialize(EternaFactory factory)
			throws EternaException
	{
		if (!this.initialized)
		{
			this.initialized = true;

			Iterator itr = this.tmpParamList.iterator();
			while (itr.hasNext())
			{
				Object tmp = itr.next();
				if (tmp instanceof ParameterGenerator)
				{
					this.paramGeneratorList.add(tmp);
				}
				else
				{
					EntityRef ref = (EntityRef) tmp;
					GroupContainer gc = new GroupContainer(this.name,
							this.objType, this.tmpParamSet, this.paramGeneratorList);
					EntityImpl.addItems(factory, ref, gc);
				}
			}

			this.tmpParamList = null;
			this.tmpParamSet = null;
		}
	}

	/**
	 * 以迭代的方式获取所有的参数.
	 */
	public Iterator getParameterGeneratorIterator()
	{
		return new UnmodifiableIterator(this.paramGeneratorList.iterator());
	}

	/**
	 * 添加一个参数构造器.
	 */
	public void addParameter(ParameterGenerator paramGenerator)
			throws EternaException
	{
		if (this.initialized)
		{
			throw new EternaException("You can't invoke addParameter after initialized.");
		}
		if (this.tmpParamSet.containsKey(paramGenerator.getName()))
		{
			String msg = "Duplicate Parameter name [" + paramGenerator.getName()
					+ "] in " + this.objType + " [" + this.name + "].";
			throw new EternaException(msg);
		}
		this.tmpParamList.add(paramGenerator);
		this.tmpParamSet.put(paramGenerator.getName(), Boolean.TRUE);
	}

	/**
	 * 添加一个实体的引用.
	 */
	public void addEntityRef(EntityRef ref)
			throws EternaException
	{
		if (this.initialized)
		{
			throw new EternaException("You can't invoke addEntityRef after initialized.");
		}
		this.tmpParamList.add(ref);
	}

	/**
	 * 将一个实体元素转换成ParameterGenerator对象.
	 *
	 * @param item        需要转换的实体元素
	 * @param tableAlias  数据库表的别名
	 */
	static ParameterGenerator item2Parameter(EntityItem item, String tableAlias)
	{
		ParameterGeneratorImpl pg = new ParameterGeneratorImpl();
		pg.setName(item.getName());
		String colName = item.getColumnName();
		if (tableAlias != null)
		{
			colName = tableAlias.concat(".").concat(colName);
		}
		pg.setColumnName(colName);
		String[] attrNames = item.getAttributeNames();
		for (int i = 0; i < attrNames.length; i++)
		{
			String n = attrNames[i];
			if (PREPARE_FLAG.equals(n))
			{
				pg.setPrepareName((String) item.getAttribute(n));
			}
			else
			{
				pg.setAttribute(n, item.getAttribute(n));
			}
		}
		return pg;
	}

}

/**
 * 处理EntityRef的容器.
 */
class GroupContainer
		implements EntityImpl.Container
{
	public GroupContainer(String name, String type, Map nameCache, List itemList)
	{
		this.name = name;
		this.type = type;
		this.nameCache = nameCache;
		this.itemList = itemList;
	}
	private final Map nameCache;
	private final List itemList;
	private final String name;
	private final String type;

	public String getName()
	{
		return this.name;
	}

	public String getType()
	{
		return this.type;
	}

	public boolean contains(String name)
	{
		return this.nameCache.containsKey(name);
	}

	public void add(EntityItem item, String tableAlias)
	{
		this.nameCache.put(item.getName(), Boolean.TRUE);
		this.itemList.add(ParameterGroup.item2Parameter(item, tableAlias));
	}

}
