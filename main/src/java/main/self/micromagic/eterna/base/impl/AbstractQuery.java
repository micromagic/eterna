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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import self.micromagic.eterna.base.Base;
import self.micromagic.eterna.base.EntityRef;
import self.micromagic.eterna.base.Query;
import self.micromagic.eterna.base.ResultIterator;
import self.micromagic.eterna.base.ResultReader;
import self.micromagic.eterna.base.ResultReaderManager;
import self.micromagic.eterna.base.ResultRow;
import self.micromagic.eterna.base.reader.ObjectReader;
import self.micromagic.eterna.base.reader.ReaderManager;
import self.micromagic.eterna.security.Permission;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.BooleanRef;
import self.micromagic.util.ObjectRef;
import self.micromagic.util.StringTool;
import self.micromagic.util.converter.BooleanConverter;

/**
 * @author micromagic@sina.com
 */
public abstract class AbstractQuery extends BaseImpl
		implements Query
{
	private String readerOrder;
	private ResultReaderManager readerManager;
	private ReaderManagerImpl tmpReaderManager;
	/**
	 * 正对某个query的全局readerManager.
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

	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		if (this.initialized)
		{
			return true;
		}
		if (super.initialize(factory))
		{
			return true;
		}

		this.globalReaderManager = new ObjectRef();
		this.readerManager = this.createTempReaderManager();

		String tmp = (String) this.getAttribute(CHECK_DATABASE_NAME_FLAG);
		if (tmp != null)
		{
			this.checkDatabaseName = "true".equalsIgnoreCase(tmp);
		}
		return false;
	}

	public Class getObjectType()
	{
		return Query.class;
	}

	public String getType()
	{
		return SQL_TYPE_QUERY;
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
			String checkStr = (String) this.getAttribute(CHECK_READER_FLAG);
			if (checkStr == null)
			{
				checkStr = (String) this.getFactory().getAttribute(CHECK_READER_FLAG);
			}
			boolean checkReader = false;
			if (checkStr != null)
			{
				checkReader = (new BooleanConverter()).convertToBoolean(checkStr);
			}
			if (!checkReader)
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
				orderStr = StringTool.linkStringArr(this.orderStrs, ", ") + ", " + orderStr;
			}
			if (log.isDebugEnabled())
			{
				log.debug("Set order at(" + this.orderIndex + "):" + orderStr);
			}
			this.setSubSQL(this.orderIndex, orderStr);
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

	protected void copy(Base copyObj)
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
				other.setSubSQL(other.orderIndex, "");
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
		return this.checkDatabaseName ?
				this.queryHelper = QueryHelper.getInstance(this, conn, this.queryHelper)
				: this.queryHelper == null ? this.queryHelper = new QueryHelper(this) : this.queryHelper;
	}

	public String getPreparedSQL()
			throws EternaException
	{
		String preparedSQL = super.getPreparedSQL();
		return this.queryHelper == null ? preparedSQL : this.queryHelper.getQuerySQL(preparedSQL);
	}

	public String getPrimitiveQuerySQL()
			throws EternaException
	{
		return super.getPreparedSQL();
	}

	public void setSingleOrder(String readerName)
			throws EternaException
	{
		if (this.orderIndex != -1)
		{
			this.setSingleOrder(readerName, 0);
		}
	}

	public void setSingleOrder(String readerName, int orderType)
			throws EternaException
	{
		if (this.orderIndex != -1)
		{
			ResultReader reader = this.readerManager.getReader(readerName);
			if (reader == null)
			{
				log.error("Single order, not found the reader: [" + readerName
						+ "] in query[" + this.getName() + "].");
				return;
			}
			String orderStr = reader.getColumnName();
			if (orderType == 0)
			{
				if (this.orderStrs != null && orderStr.equals(this.orderStrs[0]))
				{
					orderStr = orderStr + " DESC";
					this.orderNames = new String[]{readerName + "D"};
				}
				else
				{
					this.orderNames = new String[]{readerName + "A"};
				}
			}
			else
			{
				orderStr = orderType < 0 ? orderStr + " DESC" : orderStr;
				this.orderNames = orderType < 0 ?
						new String[]{readerName + "D"} : new String[]{readerName + "A"};
			}
			this.orderStrs = new String[]{orderStr};
			String settingOrder = this.readerManager.getOrderByString();
			if (settingOrder != null && settingOrder.length() > 0)
			{
				orderStr = orderStr + ", " + settingOrder;
			}
			if (log.isDebugEnabled())
			{
				log.debug("Set order at(" + this.orderIndex + "):" + orderStr);
			}
			this.setSubSQL(this.orderIndex, orderStr);
		}
	}

	public String getSingleOrder(BooleanRef desc)
	{
		if (this.orderNames == null)
		{
			return null;
		}
		int index = this.orderNames[0].length() - 1;
		if (desc != null)
		{
			desc.value = this.orderNames[0].charAt(index) == 'D';
		}
		return this.orderNames[0].substring(0, index);
	}

	public void setMultipleOrder(String[] orderNames)
			throws EternaException
	{
		if (orderNames == null || orderNames.length == 0)
		{
			this.orderNames = null;
			this.orderStrs = null;
		}
		if (this.orderIndex != -1)
		{
			this.orderNames = new String[orderNames.length];
			this.orderStrs = new String[orderNames.length];
			for (int i = 0; i < orderNames.length; i++)
			{
				String readerName = orderNames[i].substring(0, orderNames[i].length() - 1);
				char orderType = orderNames[i].charAt(orderNames[i].length() - 1);
				ResultReader reader = this.readerManager.getReader(readerName);
				if (reader == null)
				{
					log.error("Multiple order, not found the reader: [" + readerName
							+ "] in query[" + this.getName() + "].");
					return;
				}
				String orderStr = reader.getColumnName();
				orderStr = orderType == 'D' ? orderStr + " DESC" : orderStr;
				this.orderNames[i] = orderNames[i];
				this.orderStrs[i] = orderStr;
			}

			String realOrderStr = StringTool.linkStringArr(this.orderStrs, ", ");
			String settingOrder = this.readerManager.getOrderByString();
			if (settingOrder != null && settingOrder.length() > 0)
			{
				realOrderStr = realOrderStr + ", " + settingOrder;
			}
			if (log.isDebugEnabled())
			{
				log.debug("Set order at(" + this.orderIndex + "):" + realOrderStr);
			}
			this.setSubSQL(this.orderIndex, realOrderStr);
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
		for (int i = 0; i < count; i++)
		{
			String colName = meta.getColumnLabel(i + 1).toUpperCase();  // 这个取到的是别名
			int typeId = TypeManager.transSQLType(meta.getColumnType(i + 1));
			if (readerMap.containsKey(colName))
			{
				// 如果列的别名已存在, 则直接下一个
				continue;
			}
			ColumnInfo ci = new ColumnInfo(colName, typeId);
			rReaders.add(ci);
			readerMap.put(colName, ci);
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
			if (r.isUseColumnIndex())
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
				// reader的字在结果中存在, 也要去除
				tmp.exists = true;
			}
		}

		// 将剩余的查询结果构造成reader
		boolean hasColumn = false;
		ReaderManagerImpl tmpRm = this.createTempReaderManager0(false);
		tmpRm.setColNameSensitive(false);
		itr = rReaders.iterator();
		for (int i = 0; itr.hasNext(); i++)
		{
			ColumnInfo tmp = (ColumnInfo) itr.next();
			if (!tmp.exists)
			{
				hasColumn = true;
				// 如果结果中的列不在reader配置中, 则添加
				String typeName = TypeManager.getTypeName(tmp.typeId);
				ObjectReader reader = (ObjectReader) ReaderManager.createReader(typeName, tmp.colName);
				reader.setColumnIndex(i + 1);
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
		for (int i = 0; i < count; i++)
		{
			//String colName = meta.getColumnName(i + 1); // 这个在有些数据库取到的是原始列名
			String colName = meta.getColumnLabel(i + 1);  // 这个取到的是别名
			String name = colName;
			if (temp.get(name) != null)
			{
				// 当存在重复的列名时, 后面的列加上索引号
				name = colName + "+" + (i + 1);
			}
			temp.put(name, colName);
			int typeId = TypeManager.transSQLType(meta.getColumnType(i + 1));
			String typeName = TypeManager.getTypeName(typeId);
			ObjectReader reader = (ObjectReader) ReaderManager.createReader(typeName, name);
			reader.setColumnIndex(i + 1);
			rm.addReader(reader);
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

/*
	private ResultIteratorImpl executeQuery(ResultSet rs)
			throws EternaException, SQLException
	{
		int start = this.startRow - 1;
		int recordCount = 0;
		int realRecordCount = 0;
		boolean realRecordCountAvailable = false;
		boolean hasMoreRecord = false;
		boolean hasRecord = true;
		boolean isForwardOnly = rs.getType() == ResultSet.TYPE_FORWARD_ONLY;
		List readerList = this.getReaderManager0(rs).getReaderList(this.getPermission0());

		if (start > 0)
		{
			if (!isForwardOnly)
			{
				hasRecord = rs.absolute(start);
			}
			else
			{
				for (; recordCount < start && hasRecord; recordCount++, hasRecord = rs.next());
			}
		}
		ArrayList result;
		ResultIteratorImpl ritr;
		if (!hasRecord)
		{
			recordCount--;
			realRecordCountAvailable = true;
			hasMoreRecord = false;
			result = new ArrayList(0);
			ritr = new ResultIteratorImpl(readerList);
		}
		else
		{
			result = new ArrayList(this.maxRows == -1 ? 32 : this.maxRows);
			ritr = new ResultIteratorImpl(readerList);
			if (this.maxRows == -1)
			{
				while (rs.next())
				{
					recordCount++;
					result.add(this.readResults(readerList, rs, ritr));
				}
				realRecordCount = recordCount;
				realRecordCountAvailable = true;
				hasMoreRecord = false;
			}
			else
			{
				int i = 0;
				for (; i < this.maxRows && (hasMoreRecord = rs.next()); i++)
				{
					recordCount++;
					result.add(this.readResults(readerList, rs, ritr));
				}
				// 这么判断是防止某些jdbc在第一次next为false后, 后面的next又变回true
				if (hasMoreRecord && (hasMoreRecord = rs.next()))
				{
					recordCount++;
					realRecordCountAvailable = false;
				}
				else
				{
					realRecordCountAvailable = true;
				}
			}
		}
		if (this.totalCount == TOTAL_COUNT_AUTO)
		{
			if (!isForwardOnly)
			{
				realRecordCountAvailable = rs.last();
				realRecordCount = rs.getRow();
			}
			else
			{
				if (hasMoreRecord)
				{
					for (; rs.next(); recordCount++);
				}
				realRecordCount = recordCount;
			}
			realRecordCountAvailable = true;
		}
		else if (this.totalCount == TOTAL_COUNT_NONE)
		{
			realRecordCount = recordCount;
		}
		else if (this.totalCount == TOTAL_COUNT_COUNT)
		{
			if (!realRecordCountAvailable)
			{
				ritr.needCount = true;
			}
			else
			{
				realRecordCount = recordCount;
			}
		}
		else if (this.totalCount >= 0)
		{
			if (!realRecordCountAvailable)
			{
				realRecordCount = this.totalCount;
				realRecordCountAvailable = true;
			}
			else
			{
				realRecordCount = recordCount;
			}
		}

		ritr.setResult(result);
		ritr.realRecordCount = realRecordCount;
		ritr.recordCount = result.size();
		ritr.realRecordCountAvailable = realRecordCountAvailable;
		ritr.hasMoreRecord = hasMoreRecord;
		return ritr;
	}
*/