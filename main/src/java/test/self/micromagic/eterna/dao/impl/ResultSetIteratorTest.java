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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import self.micromagic.dbvm.InitTest;
import self.micromagic.eterna.dao.ConnectionWrapper;
import self.micromagic.eterna.dao.Query;
import self.micromagic.eterna.dao.ResultIterator;
import self.micromagic.eterna.model.AppData;
import tool.ConnectionTool;

public class ResultSetIteratorTest extends TestBase
{
	public void testClose()
			throws Exception
	{
		Query q = f.createQuery("testQuery");
		ResultIterator ritr = q.executeQueryHoldConnection(getConnection());
		assertEquals(1, CheckedConnection.connCache.size());
		ritr.close();
		assertEquals(0, CheckedConnection.connCache.size());
	}

	public void testToEnd()
			throws Exception
	{
		Query q = f.createQuery("testQuery");
		ResultIterator ritr = q.executeQueryHoldConnection(getConnection());
		assertEquals(1, CheckedConnection.connCache.size());
		int count = 0;
		while (ritr.hasNextRow())
		{
			ritr.nextRow();
			count++;
		}
		assertEquals(8, count);
		assertEquals(0, CheckedConnection.connCache.size());
	}

	public void testTimeout()
			throws Exception
	{
		Query q = f.createQuery("testQuery");
		Connection conn = getConnection();
		AppData data = AppData.getCurrentData();
		data.maps[AppData.REQUEST_ATTRIBUTE_MAP] = new HashMap(2);
		data.getRequestAttributeMap().put(ResultIterator.DONT_CLOSE_CONNECTION, "1");
		q.executeQueryHoldConnection(conn).close();
		(new QueryRunner(q)).start();
		Thread.sleep(5000L);
		int count = CheckedConnection.connCache.size();
		while (count > 1)
		{
			System.out.println("count:" + count);
			Thread.sleep(3000L);
			count = CheckedConnection.connCache.size();
		}
		assertEquals(1, CheckedConnection.connCache.size());
		conn.close();
		assertEquals(0, CheckedConnection.connCache.size());
	}

	static Connection getConnection()
			throws Exception
	{
		return new CheckedConnection(ConnectionTool.getConnection());
	}

	static
	{
		try
		{
			(new InitTest()).testTestdb();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

}

class QueryRunner extends Thread
{
	public QueryRunner(Query query)
	{
		this.query = query;
	}
	private final Query query;

	public void run()
	{
		try
		{
			for (int i = 0; i < 50; i++)
			{
				Connection conn = ResultSetIteratorTest.getConnection();
				this.query.executeQueryHoldConnection(conn);
				sleep(900L);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

}

class CheckedConnection extends ConnectionWrapper
{
	static Map connCache = new IdentityHashMap();

	public CheckedConnection(Connection base)
	{
		super(base);
		synchronized (CheckedConnection.class)
		{
			connCache.put(this, Boolean.TRUE);
		}
	}

	public void close() throws SQLException
	{
		if (!this.isClosed())
		{
			synchronized (CheckedConnection.class)
			{
				connCache.remove(this);
			}
		}
		super.close();
	}

}
