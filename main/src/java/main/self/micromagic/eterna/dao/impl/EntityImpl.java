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
import java.util.Collections;
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
import self.micromagic.util.StringAppender;
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
			tmp = OrderManager.doOrder(tmp, this.orderConfig, new ItemNameHandler(this.getName()));
			// 重新排序后要重设名称和索引的对应关系
			this.nameCache.clear();
			count = tmp.size();
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
		Entity entity = ref.getEntity(factory);
		Map excludeSet = null, includeMap = null;
		if (!StringTool.isEmpty(ref.getInclude()))
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
		String excludeStr = ref.getExclude();
		if (excludeStr != null)
		{
			// 如果存在exclude属性, 且设置为空的话表示包含所有列
			if (excludeStr.length() == 0)
			{
				excludeSet = Collections.EMPTY_MAP;
			}
			else
			{
				String[] arr = StringTool.separateString(excludeStr, ",", true);
				excludeSet = new HashMap();
				for (int i = 0; i < arr.length; i++)
				{
					excludeSet.put(arr[i], Boolean.TRUE);
				}
			}
		}
		// 表别名先检查是否要添加引号
		String tableAlias = ScriptParser.checkNameForQuote(ref.getTableAlias());
		Iterator tmpItr = entity.getItemIterator();
		while (tmpItr.hasNext())
		{
			EntityItem item = (EntityItem) tmpItr.next();
			String newName = null, oldName = item.getName();
			if (excludeSet != null)
			{
				if (excludeSet.containsKey(oldName))
				{
					continue;
				}
			}
			if (includeMap != null)
			{
				newName = (String) includeMap.get(oldName);
				if (newName == null)
				{
					if (excludeSet == null)
					{
						// 如果未设置排除, 则没有包含的需去除
						continue;
					}
				}
				else if (newName.equals(oldName))
				{
					// 如果新的名称和当前名称相同, 则将新名称设为null
					newName = null;
				}
			}
			boolean needCreate = false;
			if (newName != null || ref instanceof ItemForceCreater)
			{
				needCreate = true;
			}
			else
			{
				needCreate = checkColumnNameWithTableAlias(tableAlias, item.getColumnName());
			}
			if (needCreate)
			{
				item = createItem(ref, newName, item, tableAlias);
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
			if (!needCreate && Entity.TYPE.equals(handler.getType()))
			{
				// 如果未创建元素且处理的是实体, 需要创建新的元素
				item = createItem(ref, null, item, tableAlias);
			}
			handler.add(item, tableAlias);
		}
	}

	/**
	 * 创建一个元素.
	 */
	private static EntityItem createItem(EntityRef ref,
			String newName, EntityItem base, String tableAlias)
	{
		String tmpName = newName == null ? base.getName() : newName;
		if (ref instanceof ItemGenerator)
		{
			return ((ItemGenerator) ref).create(tmpName, base, tableAlias);
		}
		else
		{
			EntityItemGenerator tmp = new EntityItemGenerator();
			tmp.setName(tmpName);
			tmp.setTypeName(TypeManager.getTypeName(TypeManager.TYPE_NONE));
			tmp = (EntityItemGenerator) tmp.create();
			tmp.merge(base);
			String colName = tmp.getColumnName();
			String newColName = getColumnNameWithTableAlias(tableAlias, colName);
			if (newColName != colName)
			{
				tmp.setColumnName(newColName);
			}
			return tmp;
		}
	}

	/**
	 * 根据表别名获取列名. <p>
	 * 如果没有表别名或者不列名前不能添加表别名, 则返回原始列名.
	 */
	public static String getColumnNameWithTableAlias(String tableAlias, String colName)
	{
		if (!StringTool.isEmpty(tableAlias) && isValidColumnName(colName))
		{
			// 原始列中只是单独的列名(关键字列名或不需要添加引号的列名), 且设置了表别名, 需要添加表别名
			boolean mainKey = ScriptParser.isKey(colName) == Boolean.TRUE;
			String tmpAlias = checkTableAliasForQuote(tableAlias);
			StringAppender buf = StringTool.createStringAppender(
					tmpAlias.length() + colName.length() + (mainKey ? 3 : 1));
			buf.append(tmpAlias).append('.');
			if (mainKey)
			{
				buf.append(ScriptParser.QUOTE);
			}
			buf.append(colName);
			if (mainKey)
			{
				buf.append(ScriptParser.QUOTE);
			}
			return buf.toString();
		}
		return colName;
	}

	/**
	 * 根据表别名及原始列名检查是否需要创建新的列名.
	 */
	private static boolean checkColumnNameWithTableAlias(String tableAlias, String colName)
	{
		return !StringTool.isEmpty(tableAlias) && isValidColumnName(colName);
	}


	/**
	 * 检查名称是否为有效的列标识.
	 */
	private static boolean isValidColumnName(String name)
	{
		if (StringTool.isEmpty(name))
		{
			return false;
		}
		int len = name.length();
		int begin = 0;
		if (len > 2 && name.charAt(0) == ScriptParser.QUOTE_CHAR
				&& name.charAt(len - 1) == ScriptParser.QUOTE_CHAR)
		{
			begin = 1;
			len -= 1;
		}
		for (int i = begin; i < len; i++)
		{
			char c = name.charAt(i);
			if (c == '\"')
			{
				// 内部包含引号的名称不是有效的列名
				return false;
			}
			if (c != '_' && (c < 'A' || c > 'Z') && (c < 'a' || c > 'z'))
			{
				if (i == 0 || (c < '0' || c > '9'))
				{
					return false;
				}
			}
		}
		// 这里不需要检查关键字, 由外部代码处理
		return true;
	}

	/**
	 * 检查表别名是否需要添加引号.
	 */
	private static String checkTableAliasForQuote(String tableAlias)
	{
		return ScriptParser.checkNameForQuote(tableAlias);
	}

	/**
	 * 元素的创建接口.
	 */
	public interface ItemGenerator
	{
		/**
		 * 根据基础元素对象, 使用新的名称创建.
		 */
		EntityItem create(String newName, EntityItem base, String tableAlias);

	}

	/**
	 * 一个标识接口, 实现了这个接口将会强制创建元素.
	 */
	public interface ItemForceCreater
	{
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
		return Entity.TYPE;
	}

	public boolean contains(EntityItem item)
	{
		boolean result = this.nameCache.containsKey(item.getName());
		if (result)
		{
			// 如果已存在, 需要尝试和当前实体中的元素合并
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
	public ItemNameHandler(String name)
	{
		this.containerName = name;
	}

	public String getContainerName()
	{
		return this.containerName;
	}
	private final String containerName;

	public String getName(Object obj)
	{
		return ((EntityItem) obj).getName();
	}

}
