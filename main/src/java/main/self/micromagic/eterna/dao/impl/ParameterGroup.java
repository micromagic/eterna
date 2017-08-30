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

package self.micromagic.eterna.dao.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import self.micromagic.eterna.dao.Dao;
import self.micromagic.eterna.dao.Entity;
import self.micromagic.eterna.dao.EntityItem;
import self.micromagic.eterna.dao.EntityRef;
import self.micromagic.eterna.dao.Parameter;
import self.micromagic.eterna.dao.ParameterGenerator;
import self.micromagic.eterna.security.PermissionSet;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.Generator;
import self.micromagic.eterna.share.Tool;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.StringTool;
import self.micromagic.util.container.UnmodifiableIterator;

/**
 * @author micromagic@sina.com
 */
public class ParameterGroup
{
	/**
	 * 在item的attribute中设置使用prepare的名称.
	 */
	public static final String PREPARE_FLAG = "prepare";

	/**
	 * 在parameter的attribute中设置使用caption的名称.
	 */
	public static final String CAPTION_FLAG = "caption";

	/**
	 * 在EntityItem的attribute中设置构造ParameterGenerator对象的名称.
	 */
	public static final String GENERATOR_FLAG = "parameter.generator";

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
	 * 初始化Parameter对象列表.
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
		ParameterGenerator pg;
		Object generator = item.getAttribute(GENERATOR_FLAG);
		if (generator instanceof Generator)
		{
			pg = (ParameterGenerator) ((Generator) generator).create();
		}
		else if (generator instanceof ParameterGenerator)
		{
			pg = (ParameterGenerator) generator;
		}
		else
		{
			pg = new ParameterGeneratorImpl();
		}
		pg.setName(item.getName());
		String colName = EntityImpl.getColumnNameWithTableAlias(tableAlias, item.getColumnName());
		pg.setColumnName(colName);
		PermissionSet pSet = item.getPermissionSet();
		if (pSet != null)
		{
			pg.setPermission(pSet.toString());
		}
		pg.setParamType(TypeManager.getTypeName(item.getType()));
		if (!StringTool.isEmpty(item.getCaption()))
		{
			pg.setAttribute(CAPTION_FLAG, item.getCaption());
		}
		String[] attrNames = item.getAttributeNames();
		boolean hasPrepare = false;
		for (int i = 0; i < attrNames.length; i++)
		{
			String n = attrNames[i];
			if (PREPARE_FLAG.equals(n))
			{
				hasPrepare = true;
				pg.setPrepareName((String) item.getAttribute(n));
			}
			else if (Tool.PATTERN_FLAG.equals(n))
			{
				pg.setAttribute(n, item.getAttribute(n));
				if (!hasPrepare)
				{
					pg.setPrepareName(Tool.PATTERN_PREFIX.concat((String) item.getAttribute(n)));
				}
			}
			else
			{
				pg.setAttribute(n, item.getAttribute(n));
			}
		}
		return pg;
	}

	/**
	 * 将一个Dao对象转换成实体.
	 *
	 * @param dao  需要转换的Dao对象
	 */
	public static Entity dao2Entity(Dao dao)
	{
		String eName = "___tmpEntity.".concat(dao.getName());
		EternaFactory f = dao.getFactory();
		EntityImpl entity = (EntityImpl) f.getAttribute(eName);
		if (entity != null)
		{
			return entity;
		}
		entity = new EntityImpl();
		entity.setName(eName);
		entity.setFactory(f);
		Iterator itr = dao.getParameterIterator();
		while (itr.hasNext())
		{
			entity.addItem(parameter2Item((Parameter) itr.next()));
		}
		String[] attrNames = dao.getAttributeNames();
		for (int i = 0; i < attrNames.length; i++)
		{
			String n = attrNames[i];
			entity.setAttribute(n, dao.getAttribute(n));
		}
		entity.initialize(f);
		f.setAttribute(eName, entity);
		return entity;
	}

	/**
	 * 将一个Parameter对象转换成实体元素.
	 *
	 * @param param  需要转换的Parameter对象
	 */
	public static EntityItem parameter2Item(Parameter param)
	{
		EntityItemGenerator itemG = new EntityItemGenerator();
		itemG.setName(param.getName());
		itemG.setColumnName(param.getColumnName());
		itemG.setTypeName(TypeManager.getTypeName(param.getType()));
		if (param instanceof ParameterImpl)
		{
			String tmp = ((ParameterImpl) param).getPepareName();
			if (tmp != null)
			{
				itemG.setAttribute(PREPARE_FLAG, tmp);
			}
		}
		PermissionSet pSet = param.getPermissionSet();
		if (pSet != null)
		{
			itemG.setPermission(pSet.toString());
		}

		String[] attrNames = param.getAttributeNames();
		for (int i = 0; i < attrNames.length; i++)
		{
			String n = attrNames[i];
			if (CAPTION_FLAG.equals(n))
			{
				Object caption = param.getAttribute(n);
				if (!StringTool.isEmpty(caption));
				itemG.setCaption(caption.toString());
			}
			else
			{
				itemG.setAttribute(n, param.getAttribute(n));
			}
		}
		return (EntityItem) itemG.create();
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

	public boolean contains(EntityItem item)
	{
		return this.nameCache.containsKey(item.getName());
	}

	public void add(EntityItem item, String tableAlias)
	{
		this.nameCache.put(item.getName(), Boolean.TRUE);
		this.itemList.add(ParameterGroup.item2Parameter(item, tableAlias));
	}

}
