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

import java.io.InputStream;
import java.io.Reader;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.AppDataLogExecute;
import self.micromagic.eterna.security.Permission;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.sql.PreparedStatementWrap;
import self.micromagic.eterna.sql.QueryAdapter;
import self.micromagic.eterna.sql.ResultIterator;
import self.micromagic.eterna.sql.ResultReaderManager;
import self.micromagic.eterna.sql.SQLParameter;
import self.micromagic.eterna.sql.preparer.PreparerManager;
import self.micromagic.eterna.sql.preparer.ValuePreparer;
import self.micromagic.util.BooleanRef;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.logging.TimeLogger;

class CountQueryAdapter
		implements QueryAdapter
{
	private QueryAdapter query;
	private String name;
	private ResultReaderManager readerManager;
	private String cacheSQL;
	private String oldSQL;

	public CountQueryAdapter(QueryAdapter query)
			throws ConfigurationException
	{
		this.query = query;
		this.name = "<count>/" + query.getName();
		ResultReaders.ObjectReader tmpReader
				= (ResultReaders.ObjectReader) ResultReaders.createReader("int", "theCount");
		tmpReader.setColumnIndex(1);
		ResultReaderManagerImpl temp = new ResultReaderManagerImpl();
		temp.setName("<readers>/" + this.name);
		temp.addReader(tmpReader);
		temp.initialize(query.getFactory());
		this.readerManager = temp;
	}

	public String getName()
	{
		return this.name;
	}

	public String getType()
	{
		return SQL_TYPE_COUNT;
	}

	public ResultIterator executeQuery(Connection conn)
			throws ConfigurationException, SQLException
	{
		long startTime = TimeLogger.getTime();
		Statement stmt = null;
		ResultSet rs = null;
		Throwable exception = null;
		ResultIterator result = null;
		try
		{
			if (this.hasActiveParam())
			{
				PreparedStatement temp = conn.prepareStatement(this.getPreparedSQL());
				stmt = temp;
				this.prepareValues(temp);
				rs = temp.executeQuery();
			}
			else
			{
				stmt = conn.createStatement();
				rs = stmt.executeQuery(this.getPreparedSQL());
			}

			rs.next();
			Object[] values = new Object[]{new Integer(rs.getInt(1))};
			CountResultIterator critr = new CountResultIterator(this.readerManager, this);
			ResultRowImpl rowSet = new ResultRowImpl(values, critr, 1, null);
			ArrayList results = new ArrayList(2);
			results.add(rowSet);
			critr.setResult(results);
			result = critr;
			return critr;
		}
		catch (ConfigurationException ex)
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
			if (SQLAdapterImpl.logSQL(this, TimeLogger.getTime() - startTime, exception, conn))
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

	public String getPreparedSQL()
			throws ConfigurationException
	{
		String tmpSQL = this.query.getPrimitiveQuerySQL();
		if (this.oldSQL == tmpSQL)
		{
			return this.cacheSQL;
		}
		this.oldSQL = tmpSQL;
		String part1 = "select count(*) as theCount from (";
		String part2 = ") tmpTable";
		StringAppender buf = StringTool.createStringAppender(
				part1.length() + part2.length() + tmpSQL.length());
		buf.append(part1).append(tmpSQL).append(part2);
		this.cacheSQL = buf.toString();
		return this.cacheSQL;
	}

	public String getPrimitiveQuerySQL()
			throws ConfigurationException
	{
		return this.query.getPrimitiveQuerySQL();
	}

	public ResultReaderManager getReaderManager()
			throws ConfigurationException
	{
		return this.readerManager.copy(null);
	}

	public void prepareValues(PreparedStatement stmt)
			throws ConfigurationException, SQLException
	{
		this.query.prepareValues(stmt);
	}

	public void prepareValues(PreparedStatementWrap stmtWrap)
			throws ConfigurationException, SQLException
	{
		this.query.prepareValues(stmtWrap);
	}

	public PreparerManager getPreparerManager()
			throws ConfigurationException
	{
		return this.query.getPreparerManager();
	}

	public boolean isDynamicParameter(int index)
			throws ConfigurationException
	{
		return this.query.isDynamicParameter(index);
	}

	public boolean isDynamicParameter(String name)
			throws ConfigurationException
	{
		return this.query.isDynamicParameter(name);
	}

	public String getReaderOrder()
			throws ConfigurationException
	{
		return this.query.getReaderOrder();
	}

	public String getSingleOrder(BooleanRef desc)
			throws ConfigurationException
	{
		return this.query.getSingleOrder(desc);
	}

	public boolean canOrder()
			throws ConfigurationException
	{
		return this.query.canOrder();
	}

	public boolean isForwardOnly()
			throws ConfigurationException
	{
		return true;
	}

	public ResultIterator executeQueryHoldConnection(Connection conn)
			throws ConfigurationException, SQLException
	{
		ResultIterator ritr = this.executeQuery(conn);
		conn.close();
		return ritr;
	}

	public void execute(Connection conn)
			throws ConfigurationException, SQLException
	{
		this.executeQuery(conn);
	}

	public Object getAttribute(String name)
			throws ConfigurationException
	{
		return this.query.getAttribute(name);
	}

	public String[] getAttributeNames()
			throws ConfigurationException
	{
		return this.query.getAttributeNames();
	}

	public int getLogType()
			throws ConfigurationException
	{
		return this.query.getLogType();
	}

	public EternaFactory getFactory()
			throws ConfigurationException
	{
		return this.query.getFactory();
	}

	public int getParameterCount()
			throws ConfigurationException
	{
		return this.query.getParameterCount();
	}

	public int getActiveParamCount()
			throws ConfigurationException
	{
		return this.query.getActiveParamCount();
	}

	public boolean hasActiveParam()
			throws ConfigurationException
	{
		return this.query.hasActiveParam();
	}

	public SQLParameter getParameter(String paramName)
			throws ConfigurationException
	{
		return this.query.getParameter(paramName);
	}

	public Iterator getParameterIterator()
			throws ConfigurationException
	{
		return this.query.getParameterIterator();
	}

	public int getSubSQLCount()
			throws ConfigurationException
	{
		return this.query.getSubSQLCount();
	}

	public int getStartRow()
	{
		return 1;
	}
	public int getMaxRows()
	{
		return -1;
	}

	public int getTotalCount()
	{
		return 1;
	}

	public TotalCountExt getTotalCountExt()
	{
		return null;
	}

	public void setStartRow(int startRow)
	{
		throw new UnsupportedOperationException();
	}

	public void setMaxRows(int maxRows)
	{
		throw new UnsupportedOperationException();
	}

	public void setTotalCount(int totalCount)
	{
		throw new UnsupportedOperationException();
	}

	public void setTotalCount(int totalCount, TotalCountExt ext)
	{
		throw new UnsupportedOperationException();
	}

	public void setPermission(Permission permission)
	{
		throw new UnsupportedOperationException();
	}

	public void setReaderManager(ResultReaderManager readerManager)
	{
		throw new UnsupportedOperationException();
	}

	public void setSingleOrder(String readerName)
	{
		throw new UnsupportedOperationException();
	}

	public void setSingleOrder(String readerName, int orderType)
	{
		throw new UnsupportedOperationException();
	}

	public void setMultipleOrder(String[] orderNames)
	{
		throw new UnsupportedOperationException();
	}

	public void setSubSQL(int index, String subPart)
	{
		throw new UnsupportedOperationException();
	}

	public void setSubSQL(int index, String subPart, PreparerManager pm)
	{
		throw new UnsupportedOperationException();
	}

	public void setValuePreparer(ValuePreparer preparer)
	{
		throw new UnsupportedOperationException();
	}

	public void setIgnore(int parameterIndex)
	{
		throw new UnsupportedOperationException();
	}

	public void setIgnore(String parameterName)
	{
		throw new UnsupportedOperationException();
	}

	public void setNull(int parameterIndex, int sqlType)
	{
		throw new UnsupportedOperationException();
	}

	public void setNull(String parameterName, int sqlType)
	{
		throw new UnsupportedOperationException();
	}

	public void setBoolean(int parameterIndex, boolean x)
	{
		throw new UnsupportedOperationException();
	}

	public void setBoolean(String parameterName, boolean x)
	{
		throw new UnsupportedOperationException();
	}

	public void setByte(int parameterIndex, byte x)
	{
		throw new UnsupportedOperationException();
	}

	public void setByte(String parameterName, byte x)
	{
		throw new UnsupportedOperationException();
	}

	public void setShort(int parameterIndex, short x)
	{
		throw new UnsupportedOperationException();
	}

	public void setShort(String parameterName, short x)
	{
		throw new UnsupportedOperationException();
	}

	public void setInt(int parameterIndex, int x)
	{
		throw new UnsupportedOperationException();
	}

	public void setInt(String parameterName, int x)
	{
		throw new UnsupportedOperationException();
	}

	public void setLong(int parameterIndex, long x)
	{
		throw new UnsupportedOperationException();
	}

	public void setLong(String parameterName, long x)
	{
		throw new UnsupportedOperationException();
	}

	public void setFloat(int parameterIndex, float x)
	{
		throw new UnsupportedOperationException();
	}

	public void setFloat(String parameterName, float x)
	{
		throw new UnsupportedOperationException();
	}

	public void setDouble(int parameterIndex, double x)
	{
		throw new UnsupportedOperationException();
	}

	public void setDouble(String parameterName, double x)
	{
		throw new UnsupportedOperationException();
	}

	public void setString(int parameterIndex, String x)
	{
		throw new UnsupportedOperationException();
	}

	public void setString(String parameterName, String x)
	{
		throw new UnsupportedOperationException();
	}

	public void setBytes(int parameterIndex, byte[] x)
	{
		throw new UnsupportedOperationException();
	}

	public void setBytes(String parameterName, byte[] x)
	{
		throw new UnsupportedOperationException();
	}

	public void setDate(int parameterIndex, Date x)
	{
		throw new UnsupportedOperationException();
	}

	public void setDate(String parameterName, Date x)
	{
		throw new UnsupportedOperationException();
	}

	public void setTime(int parameterIndex, Time x)
	{
		throw new UnsupportedOperationException();
	}

	public void setTime(String parameterName, Time x)
	{
		throw new UnsupportedOperationException();
	}

	public void setTimestamp(int parameterIndex, Timestamp x)
	{
		throw new UnsupportedOperationException();
	}

	public void setTimestamp(String parameterName, Timestamp x)
	{
		throw new UnsupportedOperationException();
	}

	public void setBinaryStream(int parameterIndex, InputStream x, int length)
	{
		throw new UnsupportedOperationException();
	}

	public void setBinaryStream(String parameterName, InputStream x, int length)
	{
		throw new UnsupportedOperationException();
	}

	public void setObject(int parameterIndex, Object x)
	{
		throw new UnsupportedOperationException();
	}

	public void setObject(String parameterName, Object x)
	{
		throw new UnsupportedOperationException();
	}

	public void setCharacterStream(int parameterIndex, Reader reader, int length)
	{
		throw new UnsupportedOperationException();
	}

	public void setCharacterStream(String parameterName, Reader reader, int length)
	{
		throw new UnsupportedOperationException();
	}

	private static class CountResultIterator extends AbstractResultIterator
			implements ResultIterator
	{
		public CountResultIterator(ResultReaderManager readerManager, QueryAdapter query)
				throws ConfigurationException
		{
			super(readerManager, null);
			this.query = query;
		}

		private CountResultIterator()
		{
		}

		public void setResult(List result)
		{
			this.result = result;
			this.resultItr = this.result.iterator();
		}

		public int getRealRecordCount()
		{
			return 1;
		}

		public int getRecordCount()
		{
			return 1;
		}

		public boolean isRealRecordCountAvailable()
		{
			return true;
		}

		public boolean isHasMoreRecord()
		{
			return false;
		}

		public ResultIterator copy()
				throws ConfigurationException
		{
			CountResultIterator ritr = new CountResultIterator();
			super.copy(ritr);
			return ritr;
		}

	}

}