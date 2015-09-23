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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import self.micromagic.eterna.dao.Entity;
import self.micromagic.eterna.dao.EntityItem;
import self.micromagic.eterna.dao.EntityRef;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.OrderManager;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;
import self.micromagic.util.container.UnmodifiableIterator;

public class EntityImpl extends AbstractGenerator
		implements Entity
{
	public Object create()
	{
		return this;
	}

	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		if (this.eternaFactory != null)
		{
			return true;
		}
		this.eternaFactory = factory;
		// 缩小items的存储空间
		List tmp = new ArrayList(this.items.size() + 16);
		int count = this.items.size();
		boolean resetNameCache = false;
		for (int i = 0; i < count; i++)
		{
			Object tmpObj = this.items.get(i);
			if (tmpObj instanceof EntityItem)
			{
				if (resetNameCache)
				{
					this.nameCache.put(((EntityItem) tmpObj).getName(),
							Utility.createInteger(tmp.size()));
				}
				tmp.add(tmpObj);
			}
			else
			{
				EntityRef ref = (EntityRef) tmpObj;
				addItems(factory, ref, new EntityContainer(this, this.nameCache, tmp, this.items));
				resetNameCache = true;
			}
		}
		if (this.orderConfig != null)
		{
			tmp = OrderManager.doOrder(tmp, this.orderConfig, new ItemNameHandler());
			// 重新排序后要重设名称和索引的对应关系
			this.nameCache.clear();
			for (int i = 0; i < count; i++)
			{
				EntityItem item = (EntityItem) tmp.get(i);
				this.nameCache.put(item.getName(), Utility.createInteger(i));
			}
		}
		this.beforeItemInit(tmp);
		// 重新构造items并初始化实体元素
		this.items = new ArrayList(tmp.size());
		this.items.addAll(tmp);
		count = this.items.size();
		for (int i = 0; i < count; i++)
		{
			((EntityItem) this.items.get(i)).initialize(this);
		}
		return false;
	}
	private EternaFactory eternaFactory;

	/**
	 * 在初始化所有的元素前会调用此方法.
	 *
	 * @param items  待初始化的所有元素
	 */
	protected void beforeItemInit(List items)
	{
	}

	public EternaFactory getFactory()
	{
		return this.eternaFactory;
	}

	public String getOrder()
	{
		return this.orderConfig;
	}
	private String orderConfig;
	public void setOrder(String order)
	{
		this.orderConfig = order;
	}

	public int getItemCount()
			throws EternaException
	{
		return this.items.size();
	}

	/**
	 * 添加一个实体的引用.
	 */
	public void addEntityRef(EntityRef ref)
			throws EternaException
	{
		if (this.eternaFactory != null)
		{
			throw new EternaException("You can't invoke addEntityRef after initialized.");
		}
		this.items.add(ref);
	}

	public void addItem(EntityItem item)
	{
		if (this.eternaFactory != null)
		{
			throw new EternaException("You can't invoke addItem after initialized.");
		}
		if (this.nameCache.containsKey(item.getName()))
		{
			String msg = "Duplicate item name [" + item.getName()
					+ "] in entity [" + this.getName() + "].";
			throw new EternaException(msg);
		}
		this.nameCache.put(item.getName(), Utility.createInteger(this.items.size()));
		this.items.add(item);
	}

	/**
	 * 所有的元素列表.
	 */
	private List items = new ArrayList();
	/**
	 * 存放元素名称与位置的对应关系.
	 */
	private final Map nameCache = new HashMap();

	public EntityItem getItem(String name)
	{
		Integer i = (Integer) this.nameCache.get(name);
		if (i == null)
		{
			String msg = "Not found item [" + name
					+ "] in entity [" + this.getName() + "].";
			throw new EternaException(msg);
		}
		return (EntityItem) this.items.get(i.intValue());
	}

	public EntityItem getItem(int index)
	{
		if (index < 0 || index >= this.getItemCount())
		{
			String msg = "Not found item at index [" + index
					+ "] in entity [" + this.getName() + "].";
			throw new EternaException(msg);
		}
		return (EntityItem) this.items.get(index);
	}

	public Iterator getItemIterator()
	{
		return new UnmodifiableIterator(this.items.iterator());
	}

	/**
	 * 根据一个EntityRef的定义, 获取对应实体中的元素并添加到容器中.
	 */
	public static void addItems(EternaFactory factory, EntityRef ref, Container handler)
	{
		Entity entity = factory.getEntity(ref.getEntityName());
		Map excludeSet = null, includeMap = null;
		if (ref.getInclude() != null)
		{
			String[] arr = StringTool.separateString(ref.getInclude(), ",", true);
			includeMap = new HashMap();
			for (int i = 0; i < arr.length; i++)
			{
				String tmp = arr[i];
				int index = tmp.indexOf(':');
				if (index == -1)
				{
					includeMap.put(tmp, tmp);
				}
				else
				{
					includeMap.put(tmp.substring(0, index), tmp.substring(index + 1));
				}
			}
		}
		else if (ref.getExclude() != null)
		{
			String[] arr = StringTool.separateString(ref.getExclude(), ",", true);
			excludeSet = new HashMap();
			for (int i = 0; i < arr.length; i++)
			{
				excludeSet.put(arr[i], Boolean.TRUE);
			}
		}
		Iterator tmpItr = entity.getItemIterator();
		while (tmpItr.hasNext())
		{
			EntityItem item = (EntityItem) tmpItr.next();
			String newName = null, oldName = item.getName();
			if (includeMap != null)
			{
				newName = (String) includeMap.get(oldName);
				if (newName == null)
				{
					continue;
				}
				if (!newName.equals(oldName))
				{
					if (ref instanceof ItemGenerator)
					{
						item = ((ItemGenerator) ref).create(newName, item);
					}
					else
					{
						EntityItemGenerator tmp = new EntityItemGenerator();
						tmp.setName(newName);
						tmp.setTypeName(TypeManager.getTypeName(TypeManager.TYPE_OBJECT));
						tmp = (EntityItemGenerator) tmp.create();
						tmp.merge(item);
						item = tmp;
					}
				}
			}
			else if (excludeSet != null)
			{
				if (excludeSet.containsKey(oldName))
				{
					continue;
				}
			}
			if (handler.contains(item))
			{
				if (ref.isIgnoreSame())
				{
					continue;
				}
				String msg = "Duplicate name [" + (newName == null ? oldName : newName)
						+ "] from Entity [" + entity.getName() + "] in "
						+ handler.getType() + " [" + handler.getName() + "].";
				throw new EternaException(msg);
			}
			handler.add(item, ref.getTableAlias());
		}
	}

	/**
	 * 元素的创建接口.
	 */
	public interface ItemGenerator
	{
		/**
		 * 根据基础元素对象, 使用新的名称创建.
		 */
		EntityItem create(String newName, EntityItem base);

	}

	/**
	 * 实体元素需要添加到的容器.
	 */
	public interface Container
	{
		/**
		 * 获取容器的名称.
		 */
		String getName();

		/**
		 * 获取容器的类型.
		 */
		String getType();

		/**
		 * 容器中是否有与将要添加的元素同名的对象.
		 *
		 * @param item  将要添加元素对象
		 */
		boolean contains(EntityItem item);

		/**
		 * 将一个实体元素添加到容器中.
		 * 该方法需要将实体元素转换成目标对象.
		 */
		void add(EntityItem item, String tableAlias);

	}

}

/**
 * 处理EntityRef的容器.
 */
class EntityContainer
		implements EntityImpl.Container
{
	public EntityContainer(EntityImpl entity, Map nameCache, List itemList, List originItems)
	{
		this.entity = entity;
		this.nameCache = nameCache;
		this.itemList = itemList;
		Iterator itr = originItems.iterator();
		this.originItems = new HashMap();
		while (itr.hasNext())
		{
			Object item = itr.next();
			if (item instanceof EntityItem)
			{
				this.originItems.put(((EntityItem) item).getName(), item);
			}
		}
	}
	private final Map originItems;
	private final Map nameCache;
	private final List itemList;
	private final EntityImpl entity;

	public String getName()
	{
		return this.entity.getName();
	}

	public String getType()
	{
		return "Entity";
	}

	public boolean contains(EntityItem item)
	{
		boolean result = this.nameCache.containsKey(item.getName());
		if (result)
		{
			EntityItem oldItem = (EntityItem) this.originItems.remove(item.getName());
			if (oldItem != null)
			{
				oldItem.merge(item);
			}
		}
		return result;
	}

	public void add(EntityItem item, String tableAlias)
	{
		this.nameCache.put(item.getName(), Utility.createInteger(this.itemList.size()));
		this.itemList.add(item);
	}

}

class ItemNameHandler
		implements OrderManager.NameHandler
{
	public String getName(Object obj)
	{
		return ((EntityItem) obj).getName();
	}

}
