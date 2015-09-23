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

import self.micromagic.eterna.dao.Dao;
import self.micromagic.eterna.dao.Query;
import self.micromagic.eterna.dao.ResultReaderManager;
import self.micromagic.eterna.dao.Update;
import tool.ConnectionTool;

public class DaoTest extends TestBase
{
	public void test01()
			throws Exception
	{
		Update u = f.createUpdate("t1");
		Query q = f.createQuery("t2");
		this.setParams(u);
		this.setParams(q);
		q.setIgnore("err");
		Connection conn = ConnectionTool.getSingletonConnection();
		u.execute(conn);
		q.execute(conn);
		try
		{
			q.setString("err", "x");
			q.execute(conn);
			fail();
		}
		catch (Exception ex) {}
		conn.close();
	}

	public void test02()
			throws Exception
	{
		Query q = f.createQuery("t3");
		q.setMultipleOrder(new String[]{"+b","-c"});
		assertEquals("|b, c DESC|", q.getPreparedScript().trim());

		q = f.createQuery("t3");
		ResultReaderManager m = q.getReaderManager();
		assertEquals(7, m.getReaderCount());
		m.setReaderList(new String[]{"a","b"});
		q.setReaderManager(m);
		q.setMultipleOrder(new String[]{"+c"});
		assertEquals("|c|", q.getPreparedScript().trim());
		q.setMultipleOrder(new String[]{"+b"});
		assertEquals("|b|", q.getPreparedScript().trim());
		try
		{
			m.setReaderList(new String[]{"a","b", "c", "-a"});
			fail();
		}
		catch (Exception ex) {}

		q = f.createQuery("t3");
		m = q.getReaderManager();
		m.setReaderList(new String[]{"a","b"});
		assertEquals(3, m.getReaderList(null).size());
		assertEquals("x", m.getReader(2).getName());
		m.setReaderList(new String[]{"a","-y","b","d"});
		assertEquals(5, m.getReaderList(null).size());
		assertEquals(4, m.getReaderIndex("x"));
		assertEquals("y", m.getReader(1).getName());
	}

	private void setParams(Dao dao)
	{
		dao.setObject("key", "id");
		dao.setObject("str", "str");
		dao.setObject("int", new Integer(1));
		dao.setObject("double", "3.14");
	}

}
