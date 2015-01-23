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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import self.micromagic.eterna.base.EntityItem;
import self.micromagic.eterna.base.EntityRef;
import self.micromagic.eterna.base.ResultReader;
import self.micromagic.eterna.base.ResultReaderManager;
import self.micromagic.eterna.base.reader.InvalidReader;
import self.micromagic.eterna.base.reader.ObjectReader;
import self.micromagic.eterna.base.reader.ReaderManager;
import self.micromagic.eterna.security.Permission;
import self.micromagic.eterna.security.PermissionSet;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.OrderManager;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;

public class ReaderManagerImpl
		implements ResultReaderManager
{
	private boolean initialized;
	private boolean locked;

	/**
	 * 锁住 readerMap 的标志, 比如在调用query的getReaderManager方法时
	 * 就不需要复制 readerMap.
	 */
	private boolean readListLocked;

	private boolean nonePermission;
	private boolean colNameSensitive;

	private String name;
	private EternaFactory factory;

	/**
	 * 定义reader顺序的字符串
	 */
	private String readerOrder;

	private Map nameToIndexMap;
	private List readerList;
	private final List allReaderList;

	private List orderList;

	/**
	 * 生成的sql语句中的"order by"子句
	 */
	private String orderStr;

	public ReaderManagerImpl()
	{
		this.initialized = false;
		this.nonePermission = true;
		this.colNameSensitive = true;

		this.nameToIndexMap = new HashMap();
		this.readerList = new ArrayList();
		this.allReaderList = new ArrayList();
		this.orderList = new ArrayList(0);
		this.orderStr = null;
	}

	protected ReaderManagerImpl(ReaderManagerImpl other)
	{
		this.initialized = true;
		this.nonePermission = other.nonePermission;
		this.colNameSensitive = other.colNameSensitive;

		this.locked = false;
		this.readListLocked = true;

		this.readerOrder = other.readerOrder;
		this.nameToIndexMap = other.nameToIndexMap;
		this.allReaderList = other.allReaderList;
		this.readerList = other.readerList;
		this.orderList = other.orderList;
		this.orderStr = other.orderStr;
	}

	public void initialize(EternaFactory factory)
			throws EternaException
	{
		if (!this.initialized)
		{
			this.initialized = true;
			this.factory = factory;

			this.readerList.clear();
			Iterator itr = this.allReaderList.iterator();
			int count = this.allReaderList.size();
			boolean resetNameCache = false;
			for (int i = 0; i < count; i++)
			{
				Object tmpObj = itr.next();
				if (tmpObj instanceof ResultReader)
				{
					ResultReader reader = (ResultReader) tmpObj;
					String readerName = this.colNameSensitive ?
							reader.getName() : reader.getName().toUpperCase();
					if (resetNameCache)
					{
						this.nameToIndexMap.put(readerName,
								Utility.createInteger(this.readerList.size()));
					}
					this.readerList.add(reader);
				}
				else
				{
					EntityRef ref = (EntityRef) tmpObj;
					ReaderManagerContainer rmc = new ReaderManagerContainer(this.getName(),
							this.nameToIndexMap, this.readerList, this.colNameSensitive);
					EntityImpl.addItems(factory, ref, rmc);
					resetNameCache = true;
				}
			}
			// 重新构造allReaderList并进行初始化
			this.allReaderList.clear();
			this.allReaderList.addAll(this.readerList);
			itr = this.allReaderList.iterator();
			count = this.allReaderList.size();
			for (int i = 0; i < count; i++)
			{
				ResultReader reader = (ResultReader) itr.next();
				reader.initialize(factory);
				if (this.nonePermission && reader.getPermissionSet() != null)
				{
					this.nonePermission = false;
				}
			}
		}
	}

	public void setName(String name)
			throws EternaException
	{
		if (this.initialized)
		{
			throw new EternaException("You can't set name at initialized ReaderManager.");
		}
		this.name = name;
	}

	public String getName()
	{
		return this.name;
	}

	public EternaFactory getFactory()
	{
		return this.factory;
	}

	public String getReaderOrder()
	{
		return this.readerOrder;
	}

	public void setReaderOrder(String readerOrder)
	{
		this.readerOrder = readerOrder;
	}

	public int getReaderCount()
			throws EternaException
	{
		return this.allReaderList.size();
	}

	public ResultReader getReader(String name)
			throws EternaException
	{
		int index = this.getIndexByName(name, true);
		if (index == -1)
		{
			return null;
		}
		return (ResultReader) this.readerList.get(index);
	}

	public ResultReader getReader(int index)
			throws EternaException
	{
		if (index < 0 || index >= this.readerList.size())
		{
			String msg = "Not found ResultReader at index [" + index
					+ "] in ReaderManager [" + this.getName() + "].";
			throw new EternaException(msg);
		}
		return (ResultReader) this.readerList.get(index);
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
		this.allReaderList.add(ref);
	}

	public void addReader(ResultReader reader)
			throws EternaException
	{
		if (this.readListLocked)
		{
			throw new EternaException("You can't add reader at initialized ReaderManager.");
		}
		if (this.locked)
		{
			throw new EternaException("You can't invoke addReader when ReaderManager locked.");
		}

		if (this.nonePermission && reader.getPermissionSet() != null)
		{
			this.nonePermission = false;
		}
		String readerName = this.colNameSensitive ? reader.getName() : reader.getName().toUpperCase();

		if (this.nameToIndexMap.containsKey(readerName))
		{
			throw new EternaException("Duplicate ResultReader name ["
					+ readerName + "] at ReaderManager [" + this.getName() + "].");
		}
		this.allReaderList.add(reader);
		this.nameToIndexMap.put(readerName, Utility.createInteger(this.readerList.size()));
		this.readerList.add(reader);
	}

	public void setColNameSensitive(boolean colNameSensitive)
			throws EternaException
	{
		if (this.getReaderCount() == 0)
		{
			this.colNameSensitive = colNameSensitive;
		}
		else
		{
			throw new EternaException("You can't set column name sensitive when ReaderManager has readers.");
		}
	}

	public boolean isColNameSensitive()
	{
		return this.colNameSensitive;
	}

	public void setReaderList(String[] names)
			throws EternaException
	{
		if (this.locked)
		{
			throw new EternaException("You can't invoke setReaderList when ReaderManager locked.");
		}

		this.readerList = new ArrayList(names.length);
		this.orderList = new ArrayList(5);
		this.nameToIndexMap = new HashMap(names.length * 2);
		this.orderStr = null;
		for (int i = 0; i < names.length; i++)
		{
			String name = names[i];
			char orderType = name.charAt(name.length() - 1);
			name = name.substring(0, name.length() - 1);
			ResultReader reader = this.getReader(name);
			if (reader == null)
			{
				throw new EternaException("Invalid ResultReader name [" + name
						+ "] at ReaderManager [" + this.getName() + "].");
			}
			if (orderType != '-')
			{
				this.orderList.add(reader.getColumnName() + (orderType == 'D' ? " DESC" : "" ));
			}
			this.readerList.add(reader);
			if (this.colNameSensitive)
			{
				this.nameToIndexMap.put(reader.getName(),
						Utility.createInteger(this.readerList.size()));
			}
			else
			{
				this.nameToIndexMap.put(reader.getName().toUpperCase(),
						Utility.createInteger(this.readerList.size()));
			}
		}
	}

	public int getIndexByName(String name, boolean notThrow)
			throws EternaException
	{
		Integer i = (Integer) this.nameToIndexMap.get(
				this.colNameSensitive ? name : name.toUpperCase());
		if (i == null)
		{
			if (notThrow)
			{
				return -1;
			}
			else
			{
				String msg = "Invalid ResultReader name [" + name
						+ "] at ReaderManager [" + this.getName() + "].";
				throw new EternaException(msg);
			}
		}
		return i.intValue();
	}

	public int getIndexByName(String name)
			throws EternaException
	{
		return this.getIndexByName(name, false);
	}

	public String getOrderByString()
	{
		if (this.orderStr == null)
		{
			StringAppender temp = StringTool.createStringAppender(this.orderList.size() * 16);
			Iterator itr = this.orderList.iterator();
			if (itr.hasNext())
			{
				temp.append(itr.next());
			}
			while (itr.hasNext())
			{
				temp.append(", ").append(itr.next());
			}
			this.orderStr = temp.toString();
		}
		return this.orderStr;
	}

	/**
	 * 获得一个<code>ResultReader</code>的列表.
	 * 此方法列出的是所有的<code>ResultReader</code>.
	 * 无论setReaderList设置了怎样的值, 都是返回所有的.
	 *
	 * @return  用于读取数据的所有<code>ResultReader</code>的列表.
	 * @throws EternaException  当相关配置出错时
	 * @see #setReaderList
	 */
	public List getReaderList()
			throws EternaException
	{
		return Collections.unmodifiableList(this.allReaderList);
	}

	/**
	 * 根据权限, 获得一个<code>ResultReader</code>的列表.
	 * 如果setReaderList设置了显示的<code>ResultReader</code>, 那返回的列表只会在
	 * 此范围内.
	 * 如果某个列没有读取权限的话, 那相应的列会替换为<code>NullResultReader</code>
	 * 的实例.
	 *
	 * @return  正式用于读取数据的<code>ResultReader</code>的列表.
	 * @throws EternaException  当相关配置出错时
	 * @see #setReaderList
	 */
	public List getReaderList(Permission permission)
			throws EternaException
	{
		if (this.nonePermission || permission == null)
		{
			return Collections.unmodifiableList(this.readerList);
		}

		int count = 0;
		Iterator srcItr = this.readerList.iterator();
		ArrayList temp = null;
		while (srcItr.hasNext())
		{
			ResultReader reader = (ResultReader) srcItr.next();
			if (!checkPermission(reader, permission))
			{
				if (temp == null)
				{
					// 如果temp为空, 需要构造list, 并添加之前的reader
					temp = new ArrayList(this.readerList.size());
					srcItr = this.readerList.iterator();
					for (int i = 0; i < count; i++)
					{
						temp.add(srcItr.next());
					}
					srcItr.next();
				}
				String tmpName = this.colNameSensitive ?
						reader.getName() : reader.getName().toUpperCase();
				temp.add(new InvalidReader(tmpName));
			}
			else
			{
				if (temp != null)
				{
					temp.add(reader);
				}
			}
			count++;
		}
		return Collections.unmodifiableList(temp == null ? this.readerList : temp);
	}

	private boolean checkPermission(ResultReader reader, Permission permission)
			throws EternaException
	{
		PermissionSet ps = reader.getPermissionSet();
		if (ps == null)
		{
			return true;
		}
		return ps.checkPermission(permission);
	}

	/**
	 * 锁住自己的所有属性, 这样使用者只能读取, 而不能修改. <p>
	 * 一般用在通过xml装载后, 在EternaFactory的初始化中调用此方法.
	 * 注:在调用了copy方法后, 新复制的ResultReaderManager是不被锁住的.
	 *
	 * @see #copy(String)
	 */
	public void lock()
	{
		this.locked = true;
	}

	/**
	 * 判断是否已锁住所有属性, 这样使用者只能读取, 而不能修改. <p>
	 *
	 * @return  true表示已锁, false表示未锁
	 * @see #lock
	 */
	public boolean isLocked()
	{
		return this.locked;
	}

	/**
	 * 复制自身的所有属性, 并返回.
	 */
	public ResultReaderManager copy()
			throws EternaException
	{
		if (!this.initialized)
		{
			throw new EternaException("The ReaderManager [" + this.getName()
					+ "] hasn't initialized, can't copy.");
		}
		ReaderManagerImpl other = new ReaderManagerImpl(this);
		other.name = this.name;
		return other;
	}

	/**
	 * 将一个实体元素转换成reader对象.
	 *
	 * @param item        需要转换的实体元素
	 * @param tableAlias  数据库表的别名
	 */
	static ResultReader item2Reader(EntityItem item, String tableAlias)
	{
		String type = TypeManager.getPureTypeName(item.getType());
		ObjectReader reader = (ObjectReader) ReaderManager.createReader(
				type, item.getName());
		if (item.getCaption() != null)
		{
			reader.setCaption(item.getCaption());
		}
		reader.setAlias(item.getName());
		String colName = item.getColumnName();
		if (tableAlias != null)
		{
			colName = tableAlias.concat(".").concat(colName);
		}
		reader.setColumnName(colName);
		String[] attrNames = item.getAttributeNames();
		for (int i = 0; i < attrNames.length; i++)
		{
			String n = attrNames[i];
			if (FORMAT_FLAG.equals(n))
			{
				reader.setFormatName((String) item.getAttribute(n));
			}
			else
			{
				reader.setAttribute(n, item.getAttribute(n));
			}
		}
		return reader;
	}

}

/**
 * 处理EntityRef的容器.
 */
class ReaderManagerContainer
		implements EntityImpl.Container
{
	public ReaderManagerContainer(String name, Map nameCache, List itemList,
			boolean colNameSensitive)
	{
		this.name = name;
		this.nameCache = nameCache;
		this.itemList = itemList;
		this.colNameSensitive = colNameSensitive;
	}
	private final Map nameCache;
	private final List itemList;
	private final String name;
	private final boolean colNameSensitive;

	public String getName()
	{
		return this.name;
	}

	public String getType()
	{
		return "ReaderManager";
	}

	public boolean contains(String name)
	{
		name = this.colNameSensitive || name == null ? name : name.toUpperCase();
		return this.nameCache.containsKey(name);
	}

	public void add(EntityItem item, String tableAlias)
	{
		String rName = this.colNameSensitive ? item.getName() : item.getName().toUpperCase();
		this.nameCache.put(rName, Utility.createInteger(this.itemList.size()));
		this.itemList.add(ReaderManagerImpl.item2Reader(item, tableAlias));
	}

}

class MyOrderItem extends OrderManager.OrderItem
{
	//private ResultReader reader;

	public MyOrderItem()
	{
		super("", null);
	}

	protected MyOrderItem(String name, Object obj)
	{
		super(name, obj);
		//this.reader = (ResultReader) obj;
	}

	public boolean isIgnore()
			throws EternaException
	{
		return false;
	}

	public OrderManager.OrderItem create(Object obj)
			throws EternaException
	{
		if (obj == null)
		{
			return null;
		}
		ResultReader reader = (ResultReader) obj;
		return new MyOrderItem(reader.getName(), reader);
	}

	public Iterator getOrderItemIterator(Object container)
			throws EternaException
	{
		ResultReaderManager rm = (ResultReaderManager) container;
		return rm.getReaderList().iterator();
	}

}