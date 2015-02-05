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

package self.micromagic.eterna.dao.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Map;

import org.apache.commons.logging.Log;

import self.micromagic.eterna.dao.ResultIterator;
import self.micromagic.eterna.dao.ResultRow;
import self.micromagic.eterna.model.AppData;

public abstract class AbstractResultSetIterator
		implements ResultIterator, Runnable
{
	protected static final Log log = DaoManager.log;

	private int idleTime;
	private Connection conn;
	private Statement stmt;
	private ResultSet resultSet;
	protected List preFetchList;
	protected ResultRow currentRow;
	protected int rowNum;

	private boolean hasNext;
	private boolean isMovedNext;
	private boolean closed;
	private boolean dontClose;

	public AbstractResultSetIterator(Connection conn, Statement stmt, ResultSet rs)
	{
		this.conn = conn;
		this.stmt = stmt;
		this.resultSet = rs;
		Map attrMap = AppData.getCurrentData().getRequestAttributeMap();
		this.dontClose = attrMap != null && "1".equals(attrMap.get(DONT_CLOSE_CONNECTION));
		if (!this.dontClose)
		{
			Thread t = new Thread(this);
			t.start();
		}
	}

	private void moveToNext()
			throws SQLException
	{
		if (this.isMovedNext)
		{
			return;
		}
		this.isMovedNext = true;
		this.hasNext = this.resultSet.next();
	}

	public int getTotalCount() throws SQLException
	{
		return -1;
	}

	public int getCount() throws SQLException
	{
		return -1;
	}

	public boolean isTotalCountAvailable() throws SQLException
	{
		return false;
	}

	public boolean hasMoreRecord() throws SQLException
	{
		return false;
	}

	public boolean hasNextRow()
			throws SQLException
	{
		this.idleTime = 0;
		if (this.preFetchList != null && this.preFetchList.size() > 0)
		{
			return true;
		}
		this.moveToNext();
		return this.hasNext;
	}

	private boolean hasMoreRow0()
			throws SQLException
	{
		this.idleTime = 0;
		this.moveToNext();
		return this.hasNext;
	}

	public ResultRow preFetch()
			throws SQLException
	{
		return this.preFetch(1);
	}

	public ResultRow preFetch(int index)
			throws SQLException
	{
		this.idleTime = 0;
		if (this.preFetchList != null && this.preFetchList.size() >= index)
		{
			return (ResultRow) this.preFetchList.get(index - 1);
		}
		if (this.preFetchList == null)
		{
			this.preFetchList = new LinkedList();
		}
		for (int i = this.preFetchList.size(); i < index; i++)
		{
			if (this.hasMoreRow0())
			{
				this.isMovedNext = false;
				this.rowNum++;
				this.preFetchList.add(this.getResultRow(this.resultSet, this.rowNum));
			}
			else
			{
				return null;
			}
		}
		return (ResultRow) this.preFetchList.get(index - 1);
	}

	public ResultRow getCurrentRow()
	{
		return this.currentRow;
	}

	public ResultRow nextRow()
			throws SQLException
	{
		this.idleTime = 0;
		if (this.preFetchList != null && this.preFetchList.size() > 0)
		{
			this.currentRow  = (ResultRow) this.preFetchList.remove(0);
			return this.currentRow;
		}
		if (this.hasNextRow())
		{
			this.isMovedNext = false;
			this.rowNum++;
			this.currentRow = this.getResultRow(this.resultSet, this.rowNum);
			return this.currentRow;
		}
		throw new NoSuchElementException();
	}

	protected abstract ResultRow getResultRow(ResultSet rs, int rowNum) throws SQLException;

	public boolean beforeFirst()
	{
		try
		{
			this.resultSet.beforeFirst();
			this.currentRow = null;
			this.rowNum = 0;
			this.preFetchList = null;
			return true;
		}
		catch (SQLException ex)
		{
			return false;
		}
	}

	public void close()
			throws SQLException
	{
		if (this.closed)
		{
			return;
		}
		if (this.dontClose)
		{
			this.closed = true;
			return;
		}
		this.resultSet.close();
		this.stmt.close();
		this.conn.close();
		if (log.isDebugEnabled())
		{
			log.debug("I am closed [" + this.hashCode() + "].");
		}
		this.closed = true;
	}

	public ResultIterator copy()
	{
		return null;
	}

	public boolean hasNext()
	{
		try
		{
			return this.hasNextRow();
		}
		catch (SQLException ex)
		{
			return false;
		}
	}

	public Object next()
	{
		try
		{
			return this.nextRow();
		}
		catch (SQLException ex)
		{
			throw new NoSuchElementException();
		}
	}

	public void remove()
	{
		throw new UnsupportedOperationException();
	}

	public void run()
	{
		while (!this.closed)
		{
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException ex) {}
			this.idleTime += 100;
			if (this.idleTime > 30000)
			{
				// 如果30妙后仍未有操作, 则退出循环, 关闭数据库链接
				break;
			}
		}
		try
		{
			this.close();
		}
		catch (SQLException e) {}
	}

}