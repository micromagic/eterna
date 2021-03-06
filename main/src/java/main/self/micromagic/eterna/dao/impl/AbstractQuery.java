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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import self.micromagic.eterna.dao.Dao;
import self.micromagic.eterna.dao.EntityRef;
import self.micromagic.eterna.dao.Query;
import self.micromagic.eterna.dao.ResultIterator;
import self.micromagic.eterna.dao.ResultReader;
import self.micromagic.eterna.dao.ResultReaderManager;
import self.micromagic.eterna.dao.ResultRow;
import self.micromagic.eterna.dao.reader.ObjectReader;
import self.micromagic.eterna.dao.reader.ReaderFactory;
import self.micromagic.eterna.security.Permission;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.StringTool;
import self.micromagic.util.ref.ObjectRef;
import self.micromagic.util.ref.StringRef;

/**
 * 抽象的数据库查询对象.
 */
public abstract class AbstractQuery extends BaseDao
		implements Query
{
	private String readerOrder;
	private ResultReaderManager readerManager;
	private ReaderManagerImpl tmpReaderManager;
	/**
	 * 单个query对象的全局readerManager.
	 */
	private ObjectRef globalReaderManager;
	private boolean readerManagerSetted;

	protected Permission permission;
	private int orderIndex = -1;
	private boolean forwardOnly = true;
	private String[] orderStrs;
	private String[] orderNames;

	private QueryHelper queryHelper;

	/**
	 * 获取查询辅助工具时, 是否要检查数据库的名称.
	 */
	private boolean checkDatabaseName = true;

	protected void initElse(EternaFactory factory)
			throws EternaException
	{
		this.globalReaderManager = new ObjectRef();
		this.readerManager = this.createTempReaderManager();

		String tmp = (String) this.getAttribute(CHECK_DATABASE_NAME_FLAG);
		if (tmp != null)
		{
			this.checkDatabaseName = "true".equalsIgnoreCase(tmp);
		}
	}

	public Class getObjectType()
	{
		return Query.class;
	}

	public String getType()
	{
		return DAO_TYPE_QUERY;
	}

	/**
	 * 创建一个ResultReaderManager的实现类.
	 *
	 * @param init  创建完后是否需要立刻执行初始化
	 */
	private ReaderManagerImpl createTempReaderManager0(boolean init)
			throws EternaException
	{
		ReaderManagerImpl temp = new ReaderManagerImpl();
		temp.setName("<query>/" + this.getName());
		if (init)
		{
			temp.initialize(this.getFactory());
		}
		return temp;
	}

	private ReaderManagerImpl createTempReaderManager()
			throws EternaException
	{
		ReaderManagerImpl temp = this.tmpReaderManager;
		this.tmpReaderManager = null;
		if (temp == null)
		{
			temp = this.createTempReaderManager0(false);
		}
		temp.setReaderOrder(this.readerOrder);
		temp.initialize(this.getFactory());
		if (temp.getReaderCount() > 0)
		{
			temp.lock();
			if (!this.getBooleanAttr(CHECK_READER_FLAG, false))
			{
				// 当设置了reader且没有设置检查时就添加到全局中
				this.globalReaderManager.setObject(temp);
			}
		}
		return temp;
	}

	public String getReaderOrder()
	{
		return this.readerOrder;
	}

	public void setReaderOrder(String readerOrder)
	{
		this.readerOrder = readerOrder;
	}

	public ResultReaderManager getReaderManager()
			throws EternaException
	{
		return this.readerManager.copy();
	}

	public void setReaderManager(ResultReaderManager readerManager)
			throws EternaException
	{
		if (this.readerManager != readerManager)
		{
			String name1 = readerManager.getName();
			String name2 = this.readerManager.getName();
			if (!name1.equals(name2))
			{
				String msg = "The setted readerManager [" + name1 + "], not same as ["
						+ name2 + "] in query[" + this.getName() + "].";
				throw new EternaException(msg);
			}
			if (!(readerManager instanceof ReaderManagerImpl))
			{
				String msg = "The setted readerManager [" + name1
						+ "] isn't getted from the query [" + this.getName() + "].";
				throw new EternaException(msg);
			}
			if (readerManager instanceof ReaderManagerImpl)
			{
				((ReaderManagerImpl) readerManager).lock();
			}
			this.readerManager = readerManager;
			this.readerManagerSetted = true;
			/*
				这里不需要不需要锁上, 因为在ResultMetaDataImpl中, 会判断ResultReaderManager
				未锁上的话, 会重新构造一个“名称-索引值”的对应表
			*/
		}
		if (this.orderIndex != -1)
		{
			String orderStr = this.readerManager.getOrderByString();
			if (this.orderStrs != null)
			{
				if (StringTool.isEmpty(orderStr))
				{
					orderStr = StringTool.linkStringArr(this.orderStrs, ", ");
				}
				else
				{
					orderStr = StringTool.linkStringArr(this.orderStrs, ", ")
							.concat(", ".concat(orderStr));
				}
			}
			if (log.isDebugEnabled())
			{
				log.debug("Set order at(" + this.orderIndex + "):" + orderStr);
			}
			this.setSubScript(this.orderIndex, orderStr);
		}
	}

	/**
	 * 添加一个实体的引用.
	 */
	public void addReaderEntityRef(EntityRef ref)
			throws EternaException
	{
		if (this.initialized)
		{
			throw new EternaException("You can't invoke addEntityRef after initialized.");
		}
		if (this.tmpReaderManager == null)
		{
			this.tmpReaderManager = this.createTempReaderManager0(false);
		}
		this.tmpReaderManager.addEntityRef(ref);
	}

	public void addResultReader(ResultReader reader)
			throws EternaException
	{
		if (this.initialized)
		{
			throw new EternaException("You can't add reader in initialized "
					+ this.getType() + " [" + this.getName() + "].");
		}
		if (this.tmpReaderManager == null)
		{
			this.tmpReaderManager = this.createTempReaderManager0(false);
		}
		this.tmpReaderManager.addReader(reader);
	}

	protected void copy(Dao copyObj)
	{
		super.copy(copyObj);
		AbstractQuery other = (AbstractQuery) copyObj;
		other.readerOrder = this.readerOrder;
		other.globalReaderManager = this.globalReaderManager;
		if (this.globalReaderManager.getObject() == null)
		{
			other.readerManager = this.readerManager;
		}
		else
		{
			// 如果当前query存在全局的ResultReaderManager则复制这个全局的
			other.readerManager = (ReaderManagerImpl) this.globalReaderManager.getObject();
		}
		other.forwardOnly = this.forwardOnly;
		other.checkDatabaseName = this.checkDatabaseName;
		other.orderIndex = this.orderIndex;
		if (other.orderIndex != -1)
		{
			// 如果设置了orderIndex, 先置上默认的值
			try
			{
				other.setSubScript(other.orderIndex, "");
			}
			catch (EternaException ex) {}
		}
	}

	public void setOrderIndex(int orderIndex)
	{
		this.orderIndex = orderIndex;
	}

	protected int getOrderIndex()
	{
		return this.orderIndex;
	}

	public boolean canOrder()
	{
		return this.orderIndex != -1;
	}

	public void setForwardOnly(boolean forwardOnly)
	{
		this.forwardOnly = forwardOnly;
	}

	public boolean isForwardOnly()
	{
		return this.forwardOnly;
	}

	protected Permission getPermission0()
	{
		return this.permission;
	}

	public void setPermission(Permission permission)
	{
		this.permission = permission;
	}

	/**
	 * 获取一个查询辅助工具.
	 */
	protected QueryHelper getQueryHelper(Connection conn)
			throws SQLException
	{
		if (this.checkDatabaseName)
		{
			this.queryHelper = QueryHelper.getInstance(this, conn, this.queryHelper);
		}
		else if (this.queryHelper == null)
		{
			this.queryHelper = new QueryHelper(this);
		}
		super.setExecuteConnection(conn);
		return this.queryHelper;
	}

	protected void setExecuteConnection(Connection conn)
			throws SQLException
	{
		// 调用这个方法是在保存数据库连接时, 所以要清空queryHelper
		this.queryHelper = null;
		super.setExecuteConnection(conn);
	}

	public String getPreparedScript()
			throws EternaException
	{
		String preparedScript = super.getPreparedScript();
		return this.queryHelper == null ? preparedScript
				: this.queryHelper.getQueryScript(preparedScript);
	}

	public String getPrimitiveQueryScript()
			throws EternaException
	{
		return super.getPreparedScript();
	}

	private ResultReader getReader0(String name)
	{
		if (name == null)
		{
			return null;
		}
		ResultReader reader = this.readerManager.getReader(name);
		if (reader == null && this.readerManagerSetted)
		{
			ResultReaderManager tmp = (ResultReaderManager) this.globalReaderManager.getObject();
			if (tmp != null)
			{
				return tmp.getReader(name);
			}
			// 如果没有找到reader且readerManager被设置过, 则从所有的reader中查找
			Iterator itr = this.readerManager.getReaderList().iterator();
			if (!this.readerManager.isColNameSensitive())
			{
				name = name.toUpperCase();
			}
			while (itr.hasNext())
			{
				ResultReader r = (ResultReader) itr.next();
				if (name.equals(r.getName()))
				{
					return r;
				}
			}
		}
		return reader;
	}

	public void setSingleOrder(String readerName)
			throws EternaException
	{
		if (this.orderIndex != -1)
		{
			this.setSingleOrder(readerName, 0);
		}
	}

	public void setSingleOrder(String readerName, int orderFlag)
			throws EternaException
	{
		if (this.orderIndex != -1)
		{
			ResultReader reader = this.getReader0(readerName);
			if (reader == null)
			{
				log.error("Single order, not found the reader: [" + readerName
						+ "] in query[" + this.getName() + "].");
				return;
			}
			String orderStr = reader.getOrderCol();
			if (orderFlag == 0)
			{
				String flag;
				if (this.orderStrs != null && orderStr.equals(this.orderStrs[0]))
				{
					orderStr = orderStr.concat(" DESC");
					flag = String.valueOf(ResultReaderManager.ORDER_FLAG_DESC);
				}
				else
				{
					flag = String.valueOf(ResultReaderManager.ORDER_FLAG_ASC);
				}
				this.orderNames = new String[]{flag.concat(readerName)};
			}
			else
			{
				orderStr = orderFlag < 0 ? orderStr + " DESC" : orderStr;
				char tmpChar = orderFlag < 0 ? ResultReaderManager.ORDER_FLAG_DESC
						: ResultReaderManager.ORDER_FLAG_ASC;
				this.orderNames = new String[]{String.valueOf(tmpChar).concat(readerName)};
			}
			this.orderStrs = new String[]{orderStr};
			String settingOrder = this.readerManager.getOrderByString();
			if (!StringTool.isEmpty(settingOrder))
			{
				// 与readerManager中设置的排序合并
				orderStr = orderStr + ", " + settingOrder;
			}
			if (log.isDebugEnabled())
			{
				log.debug("Set order at(" + this.orderIndex + "):" + orderStr);
			}
			this.setSubScript(this.orderIndex, orderStr);
		}
	}

	public String getOrderConfig()
	{
		return StringTool.linkStringArr(this.orderNames, ", ");
	}

	public void setMultipleOrder(String[] orderNames)
			throws EternaException
	{
		if (orderNames == null)
		{
			orderNames = StringTool.EMPTY_STRING_ARRAY;
		}
		if (this.orderIndex != -1)
		{
			List tmpOrderNames = new ArrayList();
			List tmpOrderStrs = new ArrayList();
			for (int i = 0; i < orderNames.length; i++)
			{
				StringRef name = new StringRef(orderNames[i]);
				int orderFlag = ReaderManagerImpl.checkOrderFlag(name);
				ResultReader reader = this.getReader0(name.getString());
				if (reader == null || orderFlag == 0)
				{
					log.error("Multiple order, not found the reader [" + name
							+ "] or order flag in query [" + this.getName() + "].");
					continue;
				}
				String orderStr = reader.getOrderCol();
				orderStr = orderFlag < 0 ? orderStr.concat(" DESC") : orderStr;
				tmpOrderNames.add(orderNames[i]);
				tmpOrderStrs.add(orderStr);
			}
			int count = tmpOrderNames.size();
			if (count > 0)
			{
				this.orderNames = new String[count];
				this.orderStrs = new String[count];
				tmpOrderNames.toArray(this.orderNames);
				tmpOrderStrs.toArray(this.orderStrs);
			}
			else
			{
				this.orderNames = null;
				this.orderStrs = null;
			}

			String realOrderStr = StringTool.linkStringArr(this.orderStrs, ", ");
			String settingOrder = this.readerManager.getOrderByString();
			if (!StringTool.isEmpty(settingOrder))
			{
				// 与readerManager中设置的排序合并
				realOrderStr = realOrderStr + ", " + settingOrder;
			}
			if (log.isDebugEnabled())
			{
				log.debug("Set order at(" + this.orderIndex + "):" + realOrderStr);
			}
			this.setSubScript(this.orderIndex, realOrderStr);
		}
	}

	/**
	 * @param rs   如果参数rs不为空, 则要判断readerManager中的reader个数
	 *             是否为0, 为0的话就要根据rs给readerManager初始化默认的列
	 */
	protected ResultReaderManager getReaderManager0(ResultSet rs)
	{
		if (rs == null)
		{
			return this.readerManager;
		}
		try
		{
			if (this.globalReaderManager.getObject() == null)
			{
				if (this.readerManager.getReaderCount() == 0)
				{
					this.readerManager = this.initDefaultResultReaders(rs);
				}
				else
				{
					this.readerManager = this.checkResultReaders(rs);
				}
			}
		}
		catch (Exception ex)
		{
			log.warn("Init default ResultReaders error.", ex);
		}
		return this.readerManager;
	}

	/**
	 * 当需要检查reader时, 根据查询结果进行检查.
	 */
	private ResultReaderManager checkResultReaders(ResultSet rs)
			throws EternaException, SQLException
	{
		ResultSetMetaData meta = rs.getMetaData();
		int count = meta.getColumnCount();
		List rReaders = new ArrayList(count);
		Map readerMap = new HashMap(count * 2);
		for (int i = 1; i <= count; i++)
		{
			String colName = meta.getColumnLabel(i).toUpperCase();  // 这个取到的是别名
			int typeId = TypeManager.transSQLType(meta.getColumnType(i));
			if (!needAutoAddColumn(colName) || readerMap.containsKey(colName))
			{
				// 如果列的别名已存在, 或不需要自动添加的列, 则添加一个空值
				rReaders.add(null);
			}
			else
			{
				ColumnInfo ci = new ColumnInfo(colName, typeId);
				rReaders.add(ci);
				readerMap.put(colName, ci);
			}
		}

		// 检查reader中的配置是否在结果中存在
		boolean hasMissing = false;
		List oldReaders = this.readerManager.getReaderList();
		List newReaders = new ArrayList(count);
		Iterator itr = oldReaders.iterator();
		while (itr.hasNext())
		{
			ResultReader r = (ResultReader) itr.next();
			ColumnInfo tmp = null;
			if (r.isUseAlias())
			{
				tmp = (ColumnInfo) readerMap.get(r.getAlias().toUpperCase());
			}
			else if (r.isUseColumnIndex())
			{
				int tmpIndex = r.getColumnIndex();
				if (tmpIndex > 0 && tmpIndex <= count)
				{
					tmp = (ColumnInfo) rReaders.get(tmpIndex - 1);
				}
			}
			if (tmp != null)
			{
				// reader的设置在结果中存在
				tmp.exists = true;
				newReaders.add(r);
			}
			else
			{
				hasMissing = true;
			}
			tmp = (ColumnInfo) readerMap.get(r.getName().toUpperCase());
			if (tmp != null)
			{
				// reader的名字在结果中存在, 也要去除
				tmp.exists = true;
			}
		}

		// 将剩余的查询结果构造成reader
		boolean hasColumn = false;
		ReaderManagerImpl tmpRm = this.createTempReaderManager0(false);
		tmpRm.setColNameSensitive(false);
		itr = rReaders.iterator();
		for (int i = 1; itr.hasNext(); i++)
		{
			ColumnInfo tmp = (ColumnInfo) itr.next();
			if (tmp != null && !tmp.exists)
			{
				hasColumn = true;
				// 如果结果中的列不在reader配置中, 则添加
				String typeName = TypeManager.getTypeName(tmp.typeId);
				ObjectReader reader = (ObjectReader) ReaderFactory.createReader(
						typeName, tmp.colName);
				reader.setColumnIndex(i);
				tmpRm.addReader(reader);
				newReaders.add(reader);
			}
		}
		tmpRm.initialize(this.getFactory());

		// 生成最终的reader列表
		ResultReaderManager rm;
		if (hasMissing || hasColumn)
		{
			tmpRm = this.createTempReaderManager0(true);
			tmpRm.setColNameSensitive(!hasColumn);
			itr = newReaders.iterator();
			while (itr.hasNext())
			{
				tmpRm.addReader((ResultReader) itr.next());
			}
			// 这里所有的reader已在前面初始化过了
			tmpRm.lock();
			rm = tmpRm;
		}
		else
		{
			rm = this.readerManager;
		}
		synchronized (AbstractQuery.class)
		{
			if (this.globalReaderManager.getObject() == null && !this.readerManagerSetted)
			{
				// 如果全局和本地都未设置, 则更新全局的
				this.globalReaderManager.setObject(rm);
			}
		}
		return rm;
	}

	/**
	 * 当没有设置reader时, 根据查询结果初始化.
	 */
	private ResultReaderManager initDefaultResultReaders(ResultSet rs)
			throws EternaException, SQLException
	{
		ReaderManagerImpl rm = this.createTempReaderManager0(false);
		rm.setColNameSensitive(false);
		Map temp = new HashMap();
		ResultSetMetaData meta = rs.getMetaData();
		int count = meta.getColumnCount();
		for (int i = 1; i <= count; i++)
		{
			//String colName = meta.getColumnName(i); // 这个在有些数据库取到的是原始列名
			String colName = meta.getColumnLabel(i).toUpperCase();  // 这个取到的是别名
			if (needAutoAddColumn(colName))
			{
				String name = colName;
				if (temp.get(name) != null)
				{
					// 当存在重复的列名时, 后面的列加上索引号
					name = colName + "+" + i;
				}
				temp.put(name, colName);
				int typeId = TypeManager.transSQLType(meta.getColumnType(i));
				String typeName = TypeManager.getTypeName(typeId);
				ObjectReader reader = (ObjectReader) ReaderFactory.createReader(typeName, name);
				reader.setColumnIndex(i);
				rm.addReader(reader);
			}
		}
		rm.initialize(this.getFactory());
		rm.lock();
		synchronized (AbstractQuery.class)
		{
			if (this.globalReaderManager.getObject() == null)
			{
				// 如果全局未设置, 则更新全局的
				this.globalReaderManager.setObject(rm);
			}
		}
		return rm;
	}

	/**
	 * 是否为需要自动添加的列名.
	 */
	private static boolean needAutoAddColumn(String colName)
	{
		return !QueryHelper.ORACLE_ROW_NUM.equalsIgnoreCase(colName);
	}

	public void execute(Connection conn)
			throws EternaException, SQLException
	{
		this.executeQuery(conn);
	}

	protected static Object[] getResults(Query query, List readerList, ResultSet rs)
			throws EternaException, SQLException
	{
		int count = readerList.size();
		Iterator itr = readerList.iterator();
		Object[] values = new Object[count];
		ResultReader reader = null;
		try
		{
			for (int i = 0; i < count; i++)
			{
				reader = (ResultReader) itr.next();
				values[i] = reader.readResult(rs);
			}
		}
		catch (Throwable ex)
		{
			if (reader != null)
			{
				log.error("Error when read result, reader [" + reader.getName()
						+ "], query [" + query.getName() + "].");
			}
			if (ex instanceof SQLException)
			{
				throw (SQLException) ex;
			}
			else if (ex instanceof EternaException)
			{
				throw (EternaException) ex;
			}
			else if (ex instanceof RuntimeException)
			{
				throw (RuntimeException) ex;
			}
			else
			{
				throw new EternaException(ex);
			}
		}
		return values;
	}

	protected abstract ResultRow readResults(ResultReaderManager readerManager, Object[] row,
			ResultIterator resultIterator, int rowNum)
			throws EternaException, SQLException;

}

/**
 * 存放查询结果中某列的信息.
 */
class ColumnInfo
{
	/**
	 * 查询结果中的列名.
	 */
	public final String colName;
	/**
	 * 类型id.
	 */
	public final int typeId;

	/**
	 * 此列是否已在reader中存在.
	 */
	public boolean exists;

	public ColumnInfo(String colName, int typeId)
	{
		this.colName = colName;
		this.typeId = typeId;
	}

}
