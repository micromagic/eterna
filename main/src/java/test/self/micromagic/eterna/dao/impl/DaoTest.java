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

import junit.framework.TestCase;
import self.micromagic.eterna.dao.Dao;
import self.micromagic.eterna.dao.Query;
import self.micromagic.eterna.dao.Update;
import self.micromagic.eterna.db.ConnectionTool;
import self.micromagic.eterna.digester2.ContainerManager;
import self.micromagic.eterna.digester2.ParseException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.FactoryContainer;

public class DaoTest extends TestCase
{
	public void test01()
			throws Exception
	{
		Update u = f.createUpdate("t1");
		Query q = f.createQuery("t2");
		this.setParams(u);
		this.setParams(q);
		q.setIgnore("err");
		Connection conn = ConnectionTool.getConnection();
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

	private void setParams(Dao dao)
	{
		dao.setObject("key", "id");
		dao.setObject("str", "str");
		dao.setObject("int", new Integer(1));
		dao.setObject("double", "3.14");
	}

	static void init()
	{
		try
		{
			container = ContainerManager.createFactoryContainer("daoTest",
					"cp:self/micromagic/eterna/dao/impl/daoTest.xml", null);
			f = (EternaFactory) container.getFactory();
		}
		catch (Throwable ex)
		{
			if (ex instanceof ParseException)
			{
				ex.printStackTrace();
			}
			else
			{
				(new ParseException(ex.getMessage())).printStackTrace();
			}
		}
	}
	static FactoryContainer container;
	static EternaFactory f;

	static
	{
		init();
	}

}
