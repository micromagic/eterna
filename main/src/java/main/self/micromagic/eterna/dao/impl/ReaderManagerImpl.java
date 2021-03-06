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
import self.micromagic.eterna.dao.Query;
import self.micromagic.eterna.dao.ResultReader;
import self.micromagic.eterna.dao.ResultReaderManager;
import self.micromagic.eterna.dao.reader.InvalidReader;
import self.micromagic.eterna.dao.reader.ObjectReader;
import self.micromagic.eterna.dao.reader.ReaderFactory;
import self.micromagic.eterna.dao.reader.ReaderWrapper;
import self.micromagic.eterna.security.Permission;
import self.micromagic.eterna.security.PermissionSet;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.OrderManager;
import self.micromagic.eterna.share.Tool;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;
import self.micromagic.util.converter.BooleanConverter;
import self.micromagic.util.ref.StringRef;

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
		this.factory = other.factory;
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
			if (this.readerOrder != null)
			{
				this.readerList = OrderManager.doOrder(this.readerList,
						this.readerOrder, new ReaderNameHandler(this.getName()));
				// 重新排序后要重设名称和索引的对应关系
				this.nameToIndexMap.clear();
				itr = this.readerList.iterator();
				for (int i = 0; i < count; i++)
				{
					ResultReader reader = (ResultReader) itr.next();
					this.nameToIndexMap.put(reader.getName(), Utility.createInteger(i));
				}
			}
			// 重新构造allReaderList并进行初始化
			this.allReaderList.clear();
			this.allReaderList.addAll(this.readerList);
			count = this.allReaderList.size();
			// 初始化完后, 需要将readerList和allReaderList设成一样的
			this.readerList = this.allReaderList;
			Map colNameMap = new HashMap();
			boolean checkSameCol = BooleanConverter.toBoolean(factory.getAttribute(CHECK_SAME_COL));
			for (int i = 0; i < count; i++)
			{
				// 这里不能用迭代, 因为有可能需要添加值
				ResultReader reader = (ResultReader) this.allReaderList.get(i);
				String colName = reader.getColumnName();
				if (checkSameCol && reader.isUseAlias() && !StringTool.isEmpty(colName))
				{
					colName = colName.toUpperCase();
					String oldAlias = (String) colNameMap.get(colName);
					if (oldAlias != null)
					{
						// 如果是通过别名获取, 且设置了列名并已出现过, 则将别名设置成与之前相同的
						if (reader instanceof ObjectReader)
						{
							((ObjectReader) reader).setRealAlias(oldAlias);
						}
					}
					else
					{
						colNameMap.put(colName, reader.getAlias());
					}
				}
				reader.initialize(factory);
				String showName = (String) reader.getAttribute(SHOW_NAME_FLAG);
				if (!StringTool.isEmpty(showName))
				{
					if (this.nameToIndexMap.containsKey(showName))
					{
						String msg = "The ReaderManager [" + reader.getName()
								+ "]'s attribute showName [" + showName
								+ "] is exists in ReaderManager [" + this.getName() + "].";
						DaoManager.log.error(msg);
					}
					else
					{
						ReaderWrapper tmp = new ReaderWrapper(reader, reader.getName(), false);
						tmp.setNeedFormat(false);
						this.allReaderList.set(i, tmp);
						ReaderWrapper link = new ReaderWrapper(reader, showName, true);
						link.setNeedFormat(true);
						this.nameToIndexMap.put(showName,
								Utility.createInteger(this.allReaderList.size()));
						this.allReaderList.add(link);
					}
				}
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
		int index = this.getReaderIndex(name, true);
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
		this.nameToIndexMap.put(readerName, Utility.createInteger(this.readerList.size()));
		this.readerList.add(reader);
		if (this.allReaderList != this.readerList)
		{
			// allReaderList和readerList不同时, 需要同时添加到allReaderList
			this.allReaderList.add(reader);
		}
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

		Map tmpNameToIndexMap = new HashMap(names.length * 2);
		List tmpReaderList = new ArrayList(names.length);
		this.orderList = new ArrayList(5);
		this.orderStr = null;
		List showNames = null;
		int showNameCount = 0;
		for (int i = 0; i < names.length; i++)
		{
			StringRef name = new StringRef(names[i]);
			int orderFlag = checkOrderFlag(name);
			ResultReader reader = this.getReader0(name.getString());
			if (reader == null)
			{
				throw new EternaException("Invalid ResultReader name [" + name
						+ "] at ReaderManager [" + this.getName() + "].");
			}
			String rName = this.colNameSensitive ? reader.getName() : reader.getName().toUpperCase();
			if (tmpNameToIndexMap.containsKey(rName))
			{
				throw new EternaException("Duplicated ResultReader name [" + name
						+ "] in parameter [" + StringTool.linkStringArr(names, ", ") + "].");
			}
			String showName = (String) reader.getAttribute(SHOW_NAME_FLAG);
			if (showName != null && !showName.equalsIgnoreCase(rName))
			{
				// 存在绑定的显示列, 需要添加到列表中
				ResultReader tmp = this.getReader0(showName);
				if (tmp != null)
				{
					if (showNames == null)
					{
						showNames = new ArrayList();
					}
					showNames.add(tmp);
					showNameCount++;
				}
			}
			if (orderFlag != 0)
			{
				String orderCol = reader.getOrderCol();
				this.orderList.add(orderFlag < 0 ? orderCol.concat(" DESC") : orderCol);
			}
			tmpNameToIndexMap.put(rName, Utility.createInteger(tmpReaderList.size()));
			tmpReaderList.add(reader);
		}
		// 添加绑定的显示列
		for (int i = 0; i < showNameCount; i++)
		{
			ResultReader reader = (ResultReader) showNames.get(i);
			String name = this.colNameSensitive ? reader.getName() : reader.getName().toUpperCase();
			if (!tmpNameToIndexMap.containsKey(name))
			{
				tmpNameToIndexMap.put(name, Utility.createInteger(tmpReaderList.size()));
				tmpReaderList.add(reader);
			}
		}
		// 这两个变量需要最后改变, 因为执行的中间会需要使用
		this.nameToIndexMap = tmpNameToIndexMap;
		this.readerList = tmpReaderList;
	}

	/**
	 * 在重设reader列表时获取reader.
	 */
	private ResultReader getReader0(String name)
	{
		if (this.allReaderList == this.readerList)
		{
			// allReaderList和readerList相同时, 可直接调用getReader
			return this.getReader(name);
		}
		if (!this.colNameSensitive)
		{
			name = name.toUpperCase();
		}
		Iterator itr = this.allReaderList.iterator();
		int count = this.allReaderList.size();
		for (int i = 0; i < count; i++)
		{
			ResultReader r = (ResultReader) itr.next();
			if (name.equals(r.getName()))
			{
				return r;
			}
		}
		String msg = "Invalid ResultReader name [" + name
				+ "] at ReaderManager [" + this.getName() + "].";
		throw new EternaException(msg);
	}

	/**
	 * 检查名称前的排序标记, 如果有排序标记, 会将名称前的排序标记去除.
	 *
	 * @param name  需要判断是否有排序标记的名称
	 * @return  0 无排序 1 升序 -1 降序
	 */
	public static int checkOrderFlag(StringRef name)
			throws EternaException
	{
		String tmp = name.getString();
		if (tmp.length() >= 1)
		{
			char flag = tmp.charAt(0);
			int result = 0;
			if (flag == ORDER_FLAG_ASC)
			{
				// 有升序排序标记
				result = 1;
			}
			else if (flag == ORDER_FLAG_DESC)
			{
				// 有降序排序标记
				result = -1;
			}
			if (result != 0)
			{
				name.setString(tmp.substring(1));
			}
			return result;
		}
		return 0;
	}

	public int getReaderIndex(String name, boolean notThrow)
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

	public int getReaderIndex(String name)
			throws EternaException
	{
		return this.getReaderIndex(name, false);
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
	 * 将一个Query对象转换成实体.
	 *
	 * @param dao  需要转换的Query对象
	 */
	public static Entity query2Entity(Query query)
	{
		String eName = "___tmpEntity.".concat(query.getName());
		EternaFactory f = query.getFactory();
		EntityImpl entity = (EntityImpl) f.getAttribute(eName);
		if (entity != null)
		{
			return entity;
		}
		entity = new EntityImpl();
		entity.setName(eName);
		entity.setFactory(f);
		Iterator itr = query.getReaderManager().getReaderList().iterator();
		while (itr.hasNext())
		{
			entity.addItem(reader2Item((ResultReader) itr.next()));
		}
		String[] attrNames = query.getAttributeNames();
		for (int i = 0; i < attrNames.length; i++)
		{
			String n = attrNames[i];
			entity.setAttribute(n, query.getAttribute(n));
		}
		entity.initialize(f);
		f.setAttribute(eName, entity);
		return entity;
	}

	/**
	 * 将一个ResultReader对象转换成实体元素.
	 *
	 * @param reader  需要转换的ResultReader对象
	 */
	public static EntityItem reader2Item(ResultReader reader)
	{
		EntityItemGenerator itemG = new EntityItemGenerator();
		itemG.setName(reader.getName());
		itemG.setColumnName(reader.getColumnName());
		itemG.setTypeName(TypeManager.getTypeName(reader.getType()));
		String alias = reader.getAlias();
		if (reader.getColumnName() != reader.getOrderCol())
		{
			itemG.setAttribute(ORDER_COL, reader.getOrderCol());
		}
		if (!StringTool.isEmpty(alias) && !alias.equals(reader.getName()))
		{
			itemG.setAttribute(ALIAS_FLAG, alias);
		}
		if (reader.getFormatName() != null)
		{
			itemG.setAttribute(FORMAT_FLAG, reader.getFormatName());
		}
		Object pSet = reader.getPermissionSet();
		if (pSet != null)
		{
			itemG.setPermission(pSet.toString());
		}
		String[] attrNames = reader.getAttributeNames();
		for (int i = 0; i < attrNames.length; i++)
		{
			String n = attrNames[i];
			itemG.setAttribute(n, reader.getAttribute(n));
		}
		return (EntityItem) itemG.create();
	}

	/**
	 * 将一个实体元素转换成reader对象.
	 *
	 * @param item        需要转换的实体元素
	 * @param tableAlias  数据库表的别名
	 */
	public static ResultReader item2Reader(EntityItem item, String tableAlias)
	{
		String type = TypeManager.getPureTypeName(item.getType());
		ObjectReader reader = (ObjectReader) ReaderFactory.createReader(
				type, item.getName());
		if (item.getCaption() != null)
		{
			reader.setCaption(item.getCaption());
		}
		reader.setAlias(item.getName());
		reader.setPermissionSet(item.getPermissionSet());
		String colName = EntityImpl.getColumnNameWithTableAlias(tableAlias, item.getColumnName());
		reader.setColumnName(colName);
		String[] attrNames = item.getAttributeNames();
		boolean hasFormat = false;
		for (int i = 0; i < attrNames.length; i++)
		{
			String n = attrNames[i];
			if (FORMAT_FLAG.equals(n))
			{
				hasFormat = true;
				reader.setFormatName((String) item.getAttribute(n));
			}
			else if (ORDER_COL.equals(n))
			{
				reader.setOrderCol((String) item.getAttribute(n));
			}
			else if (ALIAS_FLAG.equals(n))
			{
				reader.setAlias((String) item.getAttribute(n));
			}
			else if (Tool.PATTERN_FLAG.equals(n))
			{
				reader.setAttribute(n, item.getAttribute(n));
				if (!hasFormat)
				{
					reader.setFormatName(Tool.PATTERN_PREFIX.concat((String) item.getAttribute(n)));
				}
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

	public boolean contains(EntityItem item)
	{
		String name = item.getName();
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

class ReaderNameHandler
		implements OrderManager.NameHandler
{
	public ReaderNameHandler(String name)
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
		return ((ResultReader) obj).getName();
	}

}
