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

package self.micromagic.eterna.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;

import org.dom4j.Element;

import self.micromagic.eterna.dao.impl.AbstractDao;
import self.micromagic.eterna.dao.impl.BaseDao;
import self.micromagic.eterna.model.AppDataLogExecute;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.util.logging.TimeLogger;

/**
 * 用于处理批量更新.
 */
public class BatchUpdate extends AbstractDao
{
	/**
	 * 批量更新的类型名称.
	 */
	public static final String DAO_TYPE_BATCH_UPDATE = "batchUpdate";

	private final Update[] updates;
	private final int logType;
	private String script;

	public BatchUpdate(Update[] updates)
	{
		this.updates = updates;
		Update first = updates[0];
		this.name = first.getName();
		this.factory = first.getFactory();
		this.logType = first.getLogType();
		this.checkScript(updates);
		this.initialized = true;
	}

	public int[] getExecutedResult()
	{
		return this.executedResult;
	}
	private int[] executedResult = null;

	public String getPreparedScript()
			throws EternaException
	{
		return this.script;
	}

	public void prepareValues(PreparedStatement stmt)
			throws EternaException, SQLException
	{
		for (int i = 0; i < this.updates.length; i++)
		{
			this.updates[i].prepareValues(stmt);
		}
	}

	public void prepareValues(PreparedStatementWrap stmtWrap)
			throws EternaException, SQLException
	{
		for (int i = 0; i < this.updates.length; i++)
		{
			this.updates[i].prepareValues(stmtWrap);
		}
	}

	public void execute(Connection conn)
			throws EternaException, SQLException
	{
		TimeLogger startTime = new TimeLogger();
		PreparedStatement pstmt = null;
		Throwable error = null;
		int[] result = this.executedResult = null;
		Savepoint savepoint = null;
		try
		{
			savepoint = BaseDao.makeSavepoint(conn, this.getName());
			this.setExecuteConnection(conn);
			pstmt = conn.prepareStatement(this.script);
			for (int i = 0; i < this.updates.length; i++)
			{
				this.updates[i].prepareValues(pstmt);
				pstmt.addBatch();
			}
			this.executedResult = result = pstmt.executeBatch();
		}
		catch (SQLException ex)
		{
			error = ex;
			BaseDao.rollbackWithError(error, savepoint, conn);
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
			BaseDao.doClose(null, pstmt);
			Element node = BaseDao.log(this, startTime, error, conn);
			if (node != null && result != null)
			{
				Element resultNode = node.addElement(this.getType() + "-result");
				AppDataLogExecute.printObject(resultNode, result);
			}
		}
	}

	protected void checkScript(Update[] updates)
	{
		String tmpScript = updates[0].getPreparedScript();
		for (int i = 1; i < this.updates.length; i++)
		{
			if (!tmpScript.equals(updates[i].getPreparedScript()))
			{
				throw new EternaException("The script isn't same, [" + tmpScript
						+ "] != [" + updates[i].getPreparedScript() + "]");
			}
		}
		this.script = tmpScript;
	}

	public int getLogType()
	{
		return this.logType;
	}

	public Class getObjectType()
	{
		return this.getClass();
	}

	public String getType()
	{
		return DAO_TYPE_BATCH_UPDATE;
	}

	public Object create()
	{
		return this;
	}

	protected void initElse(EternaFactory factory)
	{
	}

}
