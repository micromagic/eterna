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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ListIterator;

import org.dom4j.Element;

import self.micromagic.eterna.dao.Dao;
import self.micromagic.eterna.dao.Query;
import self.micromagic.eterna.dao.ResultIterator;
import self.micromagic.eterna.dao.ResultMetaData;
import self.micromagic.eterna.dao.ResultReaderManager;
import self.micromagic.eterna.dao.ResultRow;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.AppDataLogExecute;
import self.micromagic.eterna.model.Model;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.util.logging.TimeLogger;

public class QueryImpl extends AbstractQuery
		implements Query
{
	public Class getObjectType()
	{
		return QueryImpl.class;
	}

	public Object create()
			throws EternaException
	{
		QueryImpl other = new QueryImpl();
		this.copy(other);
		return other;
	}

	private int startRow = 1;
	private int maxRows = -1;
	private int totalCount = TOTAL_COUNT_NONE;
	private TotalCountInfo totalCountExt;

	protected void copy(Dao copyObj)
	{
		super.copy(copyObj);
		QueryImpl other = (QueryImpl) copyObj;
		other.totalCount = this.totalCount;
		other.totalCountExt = this.totalCountExt;
		other.startRow = this.startRow;
		other.maxRows = this.maxRows;
	}

	public int getStartRow()
	{
		return this.startRow;
	}

	/**
	 * 设置从第几条记录开始取值（从1开始计数）
	 */
	public void setStartRow(int startRow)
	{
		this.startRow = startRow < 1 ? 1 : startRow;
	}

	public int getMaxCount()
	{
		return this.maxRows;
	}

	/**
	 * 设置取出几条记录，-1表示取完为止
	 */
	public void setMaxCount(int maxRows)
	{
		this.maxRows = maxRows < -1 ? -1 : maxRows;
	}

	public int getTotalCount()
	{
		return this.totalCount;
	}

	public void setTotalCount(int totalCount)
			throws EternaException
	{
		this.setTotalCount(totalCount, null);
	}

	public void setTotalCount(int totalCount, TotalCountInfo ext)
			throws EternaException
	{
		if (this.totalCount < -3)
		{
			throw new EternaException("Error total count:" + totalCount + ".");
		}
		this.totalCount = totalCount;
		this.totalCountExt = ext;
	}

	public TotalCountInfo getTotalCountInfo()
	{
		return this.totalCountExt;
	}

	public ResultIterator executeQuery(Connection conn)
			throws EternaException, SQLException
	{
		long startTime = TimeLogger.getTime();
		QueryHelper qh = this.getQueryHelper(conn);
		Statement stmt = null;
		ResultSet rs = null;
		Throwable exception = null;
		ResultIterator result = null;
		try
		{
			if (this.hasActiveParam())
			{
				PreparedStatement temp;
				if (this.isForwardOnly())
				{
					temp = conn.prepareStatement(this.getPreparedSQL());
				}
				else
				{
					temp = conn.prepareStatement(this.getPreparedSQL(),
							ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				}
				stmt = temp;
				this.prepareValues(temp);
				rs = temp.executeQuery();
			}
			else
			{
				if (this.isForwardOnly())
				{
					stmt = conn.createStatement();
				}
				else
				{
					stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
				}
				rs = stmt.executeQuery(this.getPreparedSQL());
			}
			ResultReaderManager readerManager = this.getReaderManager0(rs);
			List readerList = readerManager.getReaderList(this.getPermission0());
			List tmpList = qh.readResults(rs, readerList);
			ResultIteratorImpl ritr = new ResultIteratorImpl(readerManager, readerList, this);
			ListIterator litr = tmpList.listIterator();
			int rowNum = 1;
			while (litr.hasNext())
			{
				ResultRow row = this.readResults(readerManager, (Object[]) litr.next(), ritr, rowNum++);
				litr.set(row);
			}
			ritr.setResult(tmpList);
			ritr.realRecordCount = qh.getRealRecordCount();
			ritr.recordCount = qh.getRecordCount();
			ritr.realRecordCountAvailable = qh.isRealRecordCountAvailable();
			ritr.hasMoreRecord = qh.isHasMoreRecord();
			if (qh.needCount())
			{
				rs.close();
				stmt.close();
				rs = null;
				stmt = null;
				if (this.countQuery == null)
				{
					this.countQuery = new CountQuery(this);
				}
				int count = this.countQuery.executeQuery(conn).nextRow().getInt(1);
				ritr.realRecordCount = count;
				ritr.realRecordCountAvailable = true;
			}
			result = ritr;
			return ritr;
		}
		catch (EternaException ex)
		{
			exception = ex;
			throw ex;
		}
		catch (SQLException ex)
		{
			exception = ex;
			throw ex;
		}
		catch (RuntimeException ex)
		{
			exception = ex;
			throw ex;
		}
		catch (Error ex)
		{
			exception = ex;
			throw ex;
		}
		finally
		{
			if (logSQL(this, TimeLogger.getTime() - startTime, exception, conn))
			{
				if (result != null)
				{
					AppData data = AppData.getCurrentData();
					if (data.getLogType() > 0)
					{
						Element nowNode = data.getCurrentNode();
						if (nowNode != null)
						{
							AppDataLogExecute.printObject(nowNode.addElement(this.getType() + "-result"), result);
						}
					}
				}
			}
			if (rs != null)
			{
				rs.close();
			}
			if (stmt != null)
			{
				stmt.close();
			}
		}
	}
	private Query countQuery;

	public ResultIterator executeQueryHoldConnection(Connection conn)
			throws EternaException, SQLException
	{
		long startTime = TimeLogger.getTime();
		Statement stmt = null;
		Throwable exception = null;
		ResultIterator result = null;
		try
		{
			ResultSet rs;
			if (this.hasActiveParam())
			{
				PreparedStatement temp;
				if (this.isForwardOnly())
				{
					temp = conn.prepareStatement(this.getPreparedSQL());
				}
				else
				{
					temp = conn.prepareStatement(this.getPreparedSQL(),
							ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				}
				stmt = temp;
				this.prepareValues(temp);
				rs = temp.executeQuery();
			}
			else
			{
				if (this.isForwardOnly())
				{
					stmt = conn.createStatement();
				}
				else
				{
					stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
				}
				rs = stmt.executeQuery(this.getPreparedSQL());
			}
			ResultReaderManager rm = this.getReaderManager0(rs);
			List readerList = rm.getReaderList(this.getPermission0());
			result = new ResultSetIteratorImpl(conn, stmt, rs, rm, readerList, this);
			// 查询执行完成, 表示已接管了数据库链接的控制, 可以设置链接接管标志
			AppData.getCurrentData().addSpcialData(Model.MODEL_CACHE, Model.CONN_HOLDED, "1");
			return result;
		}
		catch (EternaException ex)
		{
			exception = ex;
			throw ex;
		}
		catch (SQLException ex)
		{
			exception = ex;
			throw ex;
		}
		catch (RuntimeException ex)
		{
			exception = ex;
			throw ex;
		}
		catch (Error ex)
		{
			exception = ex;
			throw ex;
		}
		finally
		{
			if (logSQL(this, TimeLogger.getTime() - startTime, exception, conn))
			{
				if (result != null)
				{
					AppData data = AppData.getCurrentData();
					if (data.getLogType() > 0)
					{
						Element nowNode = data.getCurrentNode();
						if (nowNode != null)
						{
							AppDataLogExecute.printObject(nowNode.addElement(this.getType() + "-result"), result);
						}
					}
				}
			}
			// 这里需要保持连接，所以stmt不关闭
		}
	}

	protected ResultRow readResults(ResultReaderManager readerManager, Object[] row,
			ResultIterator resultIterator, int rowNum)
			throws EternaException, SQLException
	{
		return new ResultRowImpl(row, resultIterator, rowNum, this.getPermission0());
	}

}

class ResultIteratorImpl extends AbstractResultIterator
		implements ResultIterator
{
	int realRecordCount;
	int recordCount;
	boolean realRecordCountAvailable;
	boolean hasMoreRecord;

	public ResultIteratorImpl(ResultReaderManager readarManager, List readerList, Query query)
	{
		super(readerList);
		this.readerManager = readarManager;
		this.query = query;
	}

	private ResultIteratorImpl()
	{
	}

	public void setResult(List result)
	{
		this.result = result;
		this.resultItr = this.result.iterator();
	}

	public int getTotalCount()
	{
		return this.realRecordCount;
	}

	public int getCount()
	{
		return this.recordCount;
	}

	public boolean isTotalCountAvailable()
	{
		return this.realRecordCountAvailable;
	}

	public boolean hasMoreRecord()
	{
		return this.hasMoreRecord;
	}

	public ResultIterator copy()
			throws EternaException
	{
		ResultIteratorImpl ritr = new ResultIteratorImpl();
		this.copy(ritr);
		return ritr;
	}

	protected void copy(ResultIterator copyObj)
			throws EternaException
	{
		super.copy(copyObj);
		ResultIteratorImpl ritr = (ResultIteratorImpl) copyObj;
		ritr.realRecordCount = this.realRecordCount;
		ritr.recordCount = this.recordCount;
		ritr.realRecordCountAvailable = this.realRecordCountAvailable;
		ritr.hasMoreRecord = this.hasMoreRecord;
	}

}

class ResultSetIteratorImpl extends AbstractResultSetIterator
{
	private final ResultReaderManager readerManager;
	private final List readerList;
	private ResultMetaData metaData = null;
	private final QueryImpl query;

	public ResultSetIteratorImpl(Connection conn, Statement stmt, ResultSet rs,
			ResultReaderManager readerManager, List readerList, QueryImpl query)
	{
		super(conn, stmt, rs);
		this.readerManager = readerManager;
		this.readerList = readerList;
		this.query = query;
	}

	public ResultMetaData getMetaData()
			throws EternaException
	{
		if (this.metaData == null)
		{
			this.metaData = new MetaDataImpl(
					this.readerList, this.readerManager, this.query);
		}
		return this.metaData;
	}

	protected ResultRow getResultRow(ResultSet rs, int rowNum)
			throws SQLException
	{
		Object[] values;
		try
		{
			values = AbstractQuery.getResults(this.query, this.readerList, rs);
			return new ResultRowImpl(values, this, rowNum, this.query.getPermission0());
		}
		catch (EternaException ex)
		{
			log.error("Error in get results.", ex);
			throw new SQLException(ex.getMessage());
		}
	}

}