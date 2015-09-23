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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;

import self.micromagic.eterna.dao.ResultIterator;
import self.micromagic.eterna.dao.ResultRow;
import self.micromagic.eterna.model.AppData;
import self.micromagic.util.converter.BooleanConverter;

public abstract class AbstractResultSetIterator
		implements ResultIterator
{
	protected static final Log log = DaoManager.log;

	int idleTime;
	private final Connection conn;
	private final Statement stmt;
	private final ResultSet resultSet;
	protected List preFetchList;
	protected ResultRow currentRow;
	protected int rowNum;

	private boolean hasNext = true;
	private boolean isMovedNext;
	boolean closed;
	private final boolean dontClose;

	public AbstractResultSetIterator(Connection conn, Statement stmt, ResultSet rs)
	{
		this.conn = conn;
		this.stmt = stmt;
		this.resultSet = rs;
		Map attrMap = AppData.getCurrentData().getRequestAttributeMap();
		this.dontClose = attrMap != null && BooleanConverter.toBoolean(attrMap.get(DONT_CLOSE_CONNECTION));
		if (!this.dontClose)
		{
			CheckRunner.addResult(this);
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
		if (this.hasNext)
		{
			this.hasNext = this.resultSet.next();
			if (!this.hasNext)
			{
				// 当没有下一条记录时可以关闭连接
				this.close();
			}
		}
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
		if (this.hasMoreRow0())
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
		this.resultSet.close();
		this.stmt.close();
		if (this.dontClose)
		{
			this.closed = true;
			return;
		}
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

}

class CheckRunner
		implements Runnable
{
	// 30 second
	private static final int MAX_IDLE_TIME = 30000;

	private static List results = new LinkedList();
	private static Thread checkThread;

	public static void addResult(AbstractResultSetIterator result)
	{
		synchronized (results)
		{
			results.add(result);
			if (checkThread == null)
			{
				checkThread = new Thread(new CheckRunner());
				checkThread.start();
			}
		}
	}

	public void run()
	{
		while (true)
		{
			try
			{
				Thread.sleep(500);
			}
			catch (InterruptedException ex) {}
			synchronized (results)
			{
				Iterator itr = results.iterator();
				while (itr.hasNext())
				{
					AbstractResultSetIterator r = (AbstractResultSetIterator) itr.next();
					r.idleTime += 500;
					if (r.idleTime > MAX_IDLE_TIME || r.closed)
					{
						// 如果超过闲置时间或已被关闭, 则从列表中移除, 未关闭的需要关闭数据库链接
						if (!r.closed)
						{
							try
							{
								r.close();
							}
							catch (SQLException e) {}
						}
						itr.remove();
					}
				}
				if (results.isEmpty())
				{
					checkThread = null;
					break;
				}
			}
		}
	}

}