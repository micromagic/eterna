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

package self.micromagic.eterna.sql.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.security.Permission;
import self.micromagic.eterna.security.PermissionSet;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.OrderManager;
import self.micromagic.eterna.sql.NullResultReader;
import self.micromagic.eterna.sql.ResultReader;
import self.micromagic.eterna.sql.ResultReaderManager;
import self.micromagic.util.Utility;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;

public class ResultReaderManagerImpl
		implements ResultReaderManager
{
	private boolean initialized;
	private boolean locked;

	/**
	 * 锁住 readerMap 的标志, 比如在调用query的getReaderManager方法时
	 * 就不需要复制 readerMap.
	 */
	private boolean readMapLocked;

	private boolean nonePermission;
	private boolean colNameSensitive;

	private String name;
	private String parentName;
	private ResultReaderManager[] parents;
	private EternaFactory factory;

	/**
	 * 定义reader顺序的字符串
	 */
	private String readerOrder = null;

	private Map readerMap;
	private Map nameToIndexMap;

	private List readerList;
	private List allReaderList;

	private List orderList;

	/**
	 * 生成的sql语句中的"order by"子句
	 */
	private String orderStr;

	public ResultReaderManagerImpl()
	{
		this.initialized = false;
		this.nonePermission = true;
		this.colNameSensitive = true;

		this.readerMap = new HashMap();
		this.nameToIndexMap = new HashMap();
		this.readerList = new ArrayList();
		this.orderList = new ArrayList(0);
		this.orderStr = null;
	}

	protected ResultReaderManagerImpl(ResultReaderManagerImpl other, boolean readMapLocked)
	{
		this.initialized = true;
		this.nonePermission = other.nonePermission;
		this.colNameSensitive = other.colNameSensitive;

		this.locked = false;
		this.readMapLocked = readMapLocked;

		this.readerOrder = other.readerOrder;
		this.readerMap = readMapLocked ? other.readerMap : new HashMap(other.readerMap);
		this.nameToIndexMap = readMapLocked ? other.nameToIndexMap : new HashMap(other.nameToIndexMap);
		this.allReaderList = readMapLocked ? other.allReaderList : null;
		this.readerList = readMapLocked ? other.readerList : new ArrayList(other.readerList);
		this.orderList = other.orderList;
		this.orderStr = other.orderStr;
	}

	protected ResultReaderManagerImpl(ResultReaderManagerImpl other)
	{
		this.initialized = false;
		this.nonePermission = other.nonePermission;
		this.colNameSensitive = other.colNameSensitive;

		this.locked = false;
		this.readMapLocked = false;

		this.readerOrder = other.readerOrder;
		this.readerMap = new HashMap(other.readerMap);
		this.nameToIndexMap = new HashMap(other.nameToIndexMap);
		this.allReaderList = null;
		this.readerList = new ArrayList(other.readerList);
		this.orderList = new ArrayList(other.orderList);
		this.orderStr = other.orderStr;
	}

	public void initialize(EternaFactory factory)
			throws EternaException
	{
		if (!this.initialized)
		{
			this.initialized = true;
			this.factory = factory;

			Iterator itr = this.readerMap.values().iterator();
			while (itr.hasNext())
			{
				((ResultReader) itr.next()).initialize(factory);
			}

			if (this.parentName != null)
			{
				if (this.parentName.indexOf(',') == -1)
				{
					this.parents = new ResultReaderManager[1];
					this.parents[0] = factory.getReaderManager(this.parentName);
					if (this.parents[0] == null)
					{
						SQLManager.log.warn(
								"The reader manager [" + this.parentName + "] not found.");
					}
				}
				else
				{
					StringTokenizer token = new StringTokenizer(this.parentName, ",");
					this.parents = new ResultReaderManager[token.countTokens()];
					for (int i = 0; i < this.parents.length; i++)
					{
						String temp = token.nextToken().trim();
						this.parents[i] = factory.getReaderManager(temp);
						if (this.parents[i] == null)
						{
							SQLManager.log.warn("The reader manager [" + temp + "] not found.");
						}
					}
				}
			}
		}
	}

	public void setName(String name)
			throws EternaException
	{
		if (this.initialized)
		{
			throw new EternaException("You can't set name at initialized ResultReaderManager.");
		}
		this.name = name;
	}

	public String getName()
	{
		return this.name;
	}

	public void setParentName(String name)
			throws EternaException
	{
		if (this.initialized)
		{
			throw new EternaException("You can't set parent name at initialized ResultReaderManager.");
		}
		this.parentName = name;
	}

	public String getParentName()
	{
		return this.parentName;
	}

	public ResultReaderManager getParent()
	{
		if (this.parents != null && this.parents.length > 0)
		{
			return this.parents[0];
		}
		return null;
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
		this.getReaderList0();
		return this.readerMap.size();
	}

	public ResultReader getReader(String name)
			throws EternaException
	{
		this.getReaderList0();
		return (ResultReader) this.readerMap.get(this.colNameSensitive ? name : name.toUpperCase());
	}

	public ResultReader addReader(ResultReader reader)
			throws EternaException
	{
		if (this.readMapLocked)
		{
			throw new EternaException("You can't add reader at initialized ResultReaderManager.");
		}
		if (this.locked)
		{
			throw new EternaException("You can't invoke addReader when ResultReaderManager locked.");
		}

		this.allReaderList = null;
		if (this.nonePermission && reader.getPermissionSet() != null)
		{
			this.nonePermission = false;
		}
		String readerName = this.colNameSensitive ? reader.getName() : reader.getName().toUpperCase();
		ResultReader temp = (ResultReader) this.readerMap.put(readerName, reader);

		if (temp != null)
		{
			throw new EternaException(
					"Duplicate [ResultReader] name:" + reader.getName() + ".");
		}
		this.readerList.add(reader);
		this.nameToIndexMap.put(readerName, Utility.createInteger(this.readerList.size()));
		return temp;
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
			throw new EternaException("You can't set column name sensitive when ResultReaderManager has readers.");
		}
	}

	public boolean isColNameSensitive()
	{
		return this.colNameSensitive;
	}

	public void setReaderList(String[] names)
			throws EternaException
	{
		this.getReaderList0();
		if (this.locked)
		{
			throw new EternaException("You can't invoke setReaderList when ResultReaderManager locked.");
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
				throw new EternaException(
						"Invalid ResultReader name:" + name + " at ResultReaderManager "
						+ this.getName() + ".");
			}
			if (orderType != '-')
			{
				this.orderList.add(reader.getOrderName() + (orderType == 'D' ? " DESC" : "" ));
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
		this.getReaderList0();
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
				throw new EternaException(
						"Invalid ResultReader name:[" + name + "] at ResultReaderManager ["
						+ this.getName() + "].");
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
		return this.getReaderList0();
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
		this.getReaderList0();
		if (this.nonePermission)
		{
			return Collections.unmodifiableList(this.readerList);
		}
		if (permission == null)
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
					temp = new ArrayList(this.readerList.size());
					srcItr = this.readerList.iterator();
					for (int i = 0; i < count; i++)
					{
						temp.add(srcItr.next());
					}
					srcItr.next();
				}
				temp.add(new NullResultReader(reader.getName()));
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

	public ResultReader getReaderInList(int index)
			throws EternaException
	{
		this.getReaderList0();
		try
		{
			return (ResultReader) this.readerList.get(index);
		}
		catch (Exception ex)
		{
			throw new EternaException(ex.getMessage());
		}
	}

	public void lock()
	{
		this.locked = true;
	}

	public boolean isLocked()
	{
		return this.locked;
	}

	public ResultReaderManager copy(String copyName)
			throws EternaException
	{
		ResultReaderManagerImpl other;
		if (this.initialized)
		{
			this.getReaderList0();
			other = new ResultReaderManagerImpl(this, copyName == null);
		}
		else
		{
			other = new ResultReaderManagerImpl(this);
		}
		other.name = copyName == null ? this.name : this.name + "/" + copyName;
		other.parentName = this.parentName;
		other.parents = this.parents;
		return other;
	}

	private List getReaderList0()
			throws EternaException
	{
		if (this.allReaderList != null)
		{
			return this.allReaderList;
		}
		OrderManager om = new OrderManager();
		List resultList = om.getOrder(new MyOrderItem(), this.parents, this.readerOrder,
				this.readerList, this.readerMap);
		Iterator itr = resultList.iterator();
		int index = 1;
		while (itr.hasNext())
		{
			ResultReader reader = (ResultReader) itr.next();
			if (this.colNameSensitive)
			{
				this.nameToIndexMap.put(reader.getName(), Utility.createInteger(index));
			}
			else
			{
				this.nameToIndexMap.put(reader.getName().toUpperCase(), Utility.createInteger(index));
			}
			index++;
			if (this.nonePermission && reader.getPermissionSet() != null)
			{
				this.nonePermission = false;
			}
		}
		this.readerList = new ArrayList(resultList);
		this.allReaderList = Collections.unmodifiableList(resultList);
		return this.allReaderList;
	}

	private static class MyOrderItem extends OrderManager.OrderItem
	{
		private ResultReader reader;

		public MyOrderItem()
		{
			super("", null);
		}

		protected MyOrderItem(String name, Object obj)
		{
			super(name, obj);
			this.reader = (ResultReader) obj;
		}

		public boolean isIgnore()
				throws EternaException
		{
			return this.reader.isIgnore();
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

}