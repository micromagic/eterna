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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;

import self.micromagic.eterna.dao.Parameter;
import self.micromagic.eterna.dao.PreparedStatementWrap;
import self.micromagic.eterna.dao.Query;
import self.micromagic.eterna.dao.ResultIterator;
import self.micromagic.eterna.dao.ResultReaderManager;
import self.micromagic.eterna.dao.preparer.PreparerManager;
import self.micromagic.eterna.dao.preparer.ValuePreparer;
import self.micromagic.eterna.dao.reader.ObjectReader;
import self.micromagic.eterna.dao.reader.ReaderFactory;
import self.micromagic.eterna.digester2.ContainerManager;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.Model;
import self.micromagic.eterna.security.Permission;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.Tool;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.logging.TimeLogger;

class CountQuery
		implements Query
{
	private final Query query;
	private final String name;
	private String cacheSQL;
	private String oldSQL;

	private static ResultReaderManager readerManager;
	static
	{
		try
		{
			ObjectReader tmpReader
					= (ObjectReader) ReaderFactory.createReader("long", "theCount");
			tmpReader.setColumnIndex(1);
			ReaderManagerImpl temp = new ReaderManagerImpl();
			temp.setName("<readers>/count");
			temp.addReader(tmpReader);
			EternaFactory factory = (EternaFactory) ContainerManager
					.getGlobalContainer().getFactory();
			temp.initialize(factory);
			readerManager = temp;
		}
		catch (Exception ex)
		{
			Tool.log.error("Error in init count reader manager.", ex);
		}
	}

	public CountQuery(Query query)
			throws EternaException
	{
		this.query = query;
		this.name = "<count>/".concat(query.getName());
	}

	public String getName()
	{
		return this.name;
	}

	public String getType()
	{
		return DAO_TYPE_COUNT;
	}

	public ResultIterator getExecutedResult()
	{
		return this.executedResult;
	}
	private ResultIterator executedResult;

	public ResultIterator executeQuery(Connection conn)
			throws EternaException, SQLException
	{
		TimeLogger startTime = new TimeLogger();
		Statement stmt = null;
		ResultSet rs = null;
		Throwable error = null;
		ResultIterator result = null;
		this.executedResult = null;
		try
		{
			if (this.hasActiveParam())
			{
				PreparedStatement temp = conn.prepareStatement(this.getPreparedScript());
				stmt = temp;
				this.prepareValues(temp);
				rs = temp.executeQuery();
			}
			else
			{
				stmt = conn.createStatement();
				rs = stmt.executeQuery(this.getPreparedScript());
			}
			rs.next();
			Object[] values = new Object[]{new Integer(rs.getInt(1))};
			CountResultIterator critr = new CountResultIterator(readerManager, this);
			ResultRowImpl rowSet = new ResultRowImpl(values, critr, 1, null);
			ArrayList results = new ArrayList(2);
			results.add(rowSet);
			critr.setResult(results);
			this.executedResult = result = critr;
			return critr;
		}
		catch (SQLException ex)
		{
			error = ex;
			throw ex;
		}
		catch (RuntimeException ex)
		{
			error = ex;
			throw ex;
		}
		catch (Error ex)
		{
			error = ex;
			throw ex;
		}
		finally
		{
			BaseDao.doClose(rs, stmt);
			Element node = BaseDao.log(this, startTime, error, conn);
			if (node != null)
			{
				QueryImpl.logResult(node, this.getType(), result);
			}
		}
	}

	public String getPreparedScript()
			throws EternaException
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
			throws EternaException
	{
		return this.query.getPrimitiveQuerySQL();
	}

	public ResultReaderManager getReaderManager()
			throws EternaException
	{
		return readerManager.copy();
	}

	public void prepareValues(PreparedStatement stmt)
			throws EternaException, SQLException
	{
		this.query.prepareValues(stmt);
	}

	public void prepareValues(PreparedStatementWrap stmtWrap)
			throws EternaException, SQLException
	{
		this.query.prepareValues(stmtWrap);
	}

	public PreparerManager getPreparerManager()
			throws EternaException
	{
		return this.query.getPreparerManager();
	}

	public boolean isParameterSetted(int index)
	{
		return this.query.isParameterSetted(index);
	}

	public boolean isParameterSetted(String name)
	{
		return this.query.isParameterSetted(name);
	}

	public boolean isDynamicParameter(int index)
			throws EternaException
	{
		return this.query.isDynamicParameter(index);
	}

	public boolean isDynamicParameter(String name)
			throws EternaException
	{
		return this.query.isDynamicParameter(name);
	}

	public String getReaderOrder()
			throws EternaException
	{
		return this.query.getReaderOrder();
	}

	public String getOrderConfig()
			throws EternaException
	{
		return this.query.getOrderConfig();
	}

	public boolean canOrder()
			throws EternaException
	{
		return this.query.canOrder();
	}

	public boolean isForwardOnly()
			throws EternaException
	{
		return true;
	}

	public ResultIterator executeQueryHoldConnection(Connection conn)
			throws EternaException, SQLException
	{
		ResultIterator ritr = this.executeQuery(conn);
		conn.close();
		// 查询执行完成, 并关闭了链接, 可以设置链接接管标志
		AppData.getCurrentData().addSpcialData(Model.MODEL_CACHE, Model.CONN_HOLDED, "1");
		return ritr;
	}

	public void execute(Connection conn)
			throws EternaException, SQLException
	{
		this.executeQuery(conn);
	}

	public Object getAttribute(String name)
			throws EternaException
	{
		return this.query.getAttribute(name);
	}

	public String[] getAttributeNames()
			throws EternaException
	{
		return this.query.getAttributeNames();
	}

	public int getLogType()
			throws EternaException
	{
		return this.query.getLogType();
	}

	public EternaFactory getFactory()
			throws EternaException
	{
		return this.query.getFactory();
	}

	public int getParameterCount()
			throws EternaException
	{
		return this.query.getParameterCount();
	}

	public int getActiveParamCount()
			throws EternaException
	{
		return this.query.getActiveParamCount();
	}

	public boolean hasActiveParam()
			throws EternaException
	{
		return this.query.hasActiveParam();
	}

	public Parameter getParameter(int paramIndex)
			throws EternaException
	{
		return this.query.getParameter(paramIndex);
	}

	public Parameter getParameter(String paramName)
			throws EternaException
	{
		return this.query.getParameter(paramName);
	}

	public Iterator getParameterIterator()
			throws EternaException
	{
		return this.query.getParameterIterator();
	}

	public int getSubScriptCount()
			throws EternaException
	{
		return this.query.getSubScriptCount();
	}

	public int getStartRow()
	{
		return 1;
	}
	public int getMaxCount()
	{
		return -1;
	}

	public int getTotalCountModel()
	{
		return 1;
	}

	public TotalCountInfo getTotalCountInfo()
	{
		return null;
	}

	public void setStartRow(int startRow)
	{
		throw new UnsupportedOperationException();
	}

	public void setMaxCount(int maxRows)
	{
		throw new UnsupportedOperationException();
	}

	public void setTotalCountModel(int totalCount)
	{
		throw new UnsupportedOperationException();
	}

	public void setTotalCountModel(int totalCount, TotalCountInfo ext)
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

	public void setSubScript(int index, String subPart)
	{
		throw new UnsupportedOperationException();
	}

	public void setSubScript(int index, String subPart, PreparerManager pm)
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

	public void setString(int parameterIndex, String x)
	{
		throw new UnsupportedOperationException();
	}

	public void setString(String parameterName, String x)
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

}

class CountResultIterator extends AbstractResultIterator
		implements ResultIterator
{
	public CountResultIterator(ResultReaderManager readerManager, Query query)
			throws EternaException
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

	public int getTotalCount()
	{
		return 1;
	}

	public int getCount()
	{
		return 1;
	}

	public boolean isTotalCountAvailable()
	{
		return true;
	}

	public boolean hasMoreRecord()
	{
		return false;
	}

	public ResultIterator copy()
			throws EternaException
	{
		CountResultIterator ritr = new CountResultIterator();
		super.copyTo(ritr);
		return ritr;
	}

}