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
import self.micromagic.eterna.dao.PreparerChecker;
import self.micromagic.eterna.dao.Query;
import self.micromagic.eterna.dao.ResultReaderManager;
import self.micromagic.eterna.dao.Update;
import self.micromagic.eterna.dao.preparer.PreparerManager;
import self.micromagic.eterna.dao.preparer.ValuePreparer;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.Utility;
import tool.ConnectionTool;

public class DaoTest extends TestBase
{
	public void testBindWithName()
			throws Exception
	{
		EntityRefImpl ref = new EntityRefImpl();
		ref.setEntityName("e_bind");
		UpdateImpl u = new UpdateImpl();
		u.setAttribute(Dao.PARAM_BIND_WITH_NAME_FLAG, "1");
		u.setName("b_t_03");
		u.addEntityRef(ref);
		u.setPreparedSQL("#param(n1) #sub #auto[and;5] #sub #auto[and,dynamic;2,6]");
		u.setFactory(f);
		f.registerObject(u);

		Object[] results;
		Update update = f.createUpdate("b_t_03");
		update.setSubScript(1, "");
		update.setSubScript(2, "");
		update.setString("n1", "a");
		update.setIgnore("n2");
		update.setIgnore("n3");
		update.setIgnore("n4");
		update.setString("n5", "e");
		update.setString("n6", "f");
		update.setString("n7", "g");
		results = new Object[]{"a", "e", "f", "g", "e", "f"};
		update.prepareValues(new PreparerChecker(results));

		PreparerManager pm;
		ValuePreparer vp;
		pm = new PreparerManager(1);
		vp = f.createValuePreparer(TypeManager.TYPE_INTEGER, "1");
		vp.setRelativeIndex(1);
		pm.setValuePreparer(vp);
		update.setSubScript(1, "?", pm);
		pm = new PreparerManager(2);
		vp = f.createValuePreparer(TypeManager.TYPE_INTEGER, "2");
		vp.setRelativeIndex(1);
		pm.setValuePreparer(vp);
		vp = f.createValuePreparer(TypeManager.TYPE_INTEGER, "3");
		vp.setRelativeIndex(2);
		pm.setValuePreparer(vp);
		update.setSubScript(2, "? ?", pm);;
		update.setObject("n3", Utility.INTEGER_10);
		results = new Object[]{
			"a", Utility.INTEGER_1, "e", "f", "g",
			Utility.INTEGER_2, Utility.INTEGER_3, "10", "e", "f"
		};
		update.prepareValues(new PreparerChecker(results));
	}

	public void testBindWithIndex2()
			throws Exception
	{
		EntityRefImpl ref = new EntityRefImpl();
		ref.setEntityName("e_bind");
		UpdateImpl u = new UpdateImpl();
		u.setName("b_t_02");
		u.addEntityRef(ref);
		u.setPreparedSQL("insert into T (#auto[insertN;1,2] #auto[insertN,d;3])"
				+ " values (#auto[insertV;1,2] #auto[insertV,d;3])");
		u.setFactory(f);
		f.registerObject(u);

		Object[] results;
		Update update = f.createUpdate("b_t_02");
		update.setString("n1", "a");
		update.setString("n2", "b");
		update.setIgnore("n3");
		update.setIgnore("n4");
		update.setIgnore("n5");
		update.setIgnore("n6");
		update.setString("n7", "g");
		results = new Object[]{"a", "b", "g"};
		update.prepareValues(new PreparerChecker(results));
		assertEquals("insert into T (col1, col2 , col7) values (?, ? , ?)",
				update.getPreparedScript());
	}

	public void testBindWithIndex1()
			throws Exception
	{
		EntityRefImpl ref = new EntityRefImpl();
		ref.setEntityName("e_bind");
		UpdateImpl u = new UpdateImpl();
		u.setName("b_t_01");
		u.addEntityRef(ref);
		u.setPreparedSQL("? #sub #auto[and,dynamic;2,5] #sub #auto[and;6]");
		u.setFactory(f);
		f.registerObject(u);

		Object[] results;
		Update update = f.createUpdate("b_t_01");
		update.setSubScript(1, "");
		update.setSubScript(2, "");
		update.setString("n1", "a");
		update.setIgnore("n2");
		update.setIgnore("n3");
		update.setIgnore("n4");
		update.setIgnore("n5");
		update.setString("n6", "f");
		update.setString("n7", "g");
		results = new Object[]{"a", "f", "g"};
		update.prepareValues(new PreparerChecker(results));

		PreparerManager pm;
		ValuePreparer vp;
		pm = new PreparerManager(1);
		vp = f.createValuePreparer(TypeManager.TYPE_INTEGER, "1");
		vp.setRelativeIndex(1);
		pm.setValuePreparer(vp);
		update.setSubScript(1, "?", pm);
		pm = new PreparerManager(2);
		vp = f.createValuePreparer(TypeManager.TYPE_INTEGER, "2");
		vp.setRelativeIndex(1);
		pm.setValuePreparer(vp);
		vp = f.createValuePreparer(TypeManager.TYPE_INTEGER, "3");
		vp.setRelativeIndex(2);
		pm.setValuePreparer(vp);
		update.setSubScript(2, "? ?", pm);;
		update.setObject("n3", Utility.INTEGER_10);
		results = new Object[]{
			"a", Utility.INTEGER_1, "10",
			Utility.INTEGER_2, Utility.INTEGER_3, "f", "g"
		};
		update.prepareValues(new PreparerChecker(results));
	}

	public void testErrorDynamic()
	{
		EntityRefImpl ref = new EntityRefImpl();
		ref.setEntityName("e_bind");
		UpdateImpl u = new UpdateImpl();
		u.setName("b_t_err");
		u.addEntityRef(ref);
		u.setPreparedSQL("? #sub #auto[insertN,dynamic;2,5] #sub #auto[and;2]");
		u.setFactory(f);
		try
		{
			f.registerObject(u);
			fail("动态参数有未绑定的应抛出异常");
		}
		catch (EternaException ex) {}
	}

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
