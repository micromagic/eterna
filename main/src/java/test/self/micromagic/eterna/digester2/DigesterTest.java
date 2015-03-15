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

package self.micromagic.eterna.digester2;

import junit.framework.TestCase;
import self.micromagic.eterna.dao.Entity;
import self.micromagic.eterna.dao.Query;
import self.micromagic.eterna.dao.ResultFormat;
import self.micromagic.eterna.dao.ResultReader;
import self.micromagic.eterna.dao.ResultReaderManager;
import self.micromagic.eterna.dao.Update;
import self.micromagic.eterna.model.Model;
import self.micromagic.eterna.model.ModelExport;
import self.micromagic.eterna.search.Search;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.FactoryContainer;
import self.micromagic.eterna.view.Component;
import self.micromagic.eterna.view.Resource;
import self.micromagic.eterna.view.View;
import self.micromagic.util.FormatTool;
import self.micromagic.util.ref.StringRef;

public class DigesterTest extends TestCase
{
	public void testFormat()
			throws Exception
	{
		ResultFormat format = f.getFormat("f1");
		assertNotNull(format);
		assertEquals("f1", format.getName());
	}

	public void testQuery()
			throws Exception
	{
		Query q1 = f.createQuery("q1");
		assertEquals("0", q1.getAttribute("v"));
		assertEquals(new Integer(-1), q1.getAttribute("i"));
		java.sql.Timestamp st = new java.sql.Timestamp(
				FormatTool.parseDatetime("2015-1-1 01:01:01").getTime());
		assertEquals(st, q1.getAttribute("t"));

		Query q2 = f.createQuery("q1");
		assertFalse(q1 == q2);
		assertTrue(q1.getPreparedSQL().startsWith("sql 123 "));
		ResultReaderManager readerManager = q1.getReaderManager();
		ResultReader reader = readerManager.getReader("r1");
		assertEquals("r1", reader.getAlias());
		reader = readerManager.getReader("r2");
		assertEquals("test", reader.getCaption());
		assertEquals("1\n3", reader.getAttribute("b"));
		assertEquals("c1", reader.getColumnName());
		assertEquals(3, reader.getColumnIndex());
		reader = readerManager.getReader("r3");
		assertEquals("r3", reader.getName());
		assertEquals("c2", reader.getAlias());
		assertEquals(3, q1.getParameter("i1").getIndex());
		assertEquals(5, q1.getParameter("p3").getIndex());

		assertEquals(readerManager.getReaderIndex("i6") + 1,
				readerManager.getReaderIndex("r3"));
		assertEquals(readerManager.getReaderIndex("r2") + 1,
				readerManager.getReaderIndex("i2"));

		assertEquals("a", f.getAttribute("1"));
		//assertEquals("x", f.getAttribute("3"));
		assertNull(f.getAttribute("3"));
		//System.out.println(doc.asXML());
		q1.setString("p1", "");
		q1.setString("p2", "2015");
		q1.setString("p3", "2015-02");
	}

	public void testEntity()
			throws Exception
	{
		Entity entity = f.getEntity("e1");
		assertEquals("2015-1-1 01:01:01", entity.getItem("i2").getAttribute("t"));
		assertEquals(Boolean.FALSE, entity.getItem("i2").getAttribute("v"));
		assertEquals("2", entity.getItem("i2").getAttribute("a"));
		assertEquals("c1", entity.getItem("i1").getColumnName());
		assertEquals("i2", entity.getItem("i2").getColumnName());
		entity = f.getEntity("e2");
		assertEquals(6, entity.getItemCount());
		assertEquals("i2", entity.getItem(0).getName());
		assertEquals("i2_1", entity.getItem(3).getName());
		assertEquals("i6", entity.getItem(5).getName());

		entity = f.getEntity("e3");
		assertEquals("1 a\n2\n3", entity.getItem("tmp1").getAttribute("x"));
		entity = f.getEntity("entity5");
		assertEquals("v", entity.getItem("tmp2").getAttribute("x"));
	}

	public void testQuery2()
			throws Exception
	{
		Query query2 = f.createQuery("q2");
		query2.setObject("p3", "2015-02");
		query2.setString("pEnd", "1");
		String tmpSQL = query2.getPreparedSQL();
		//System.out.println(tmpSQL);
		assertTrue(tmpSQL.indexOf("t.x1 as \"c3\", Tx.i6 as \"i6\", Tx.c3 as \"p3\", t.x2 as \"c2\", t.end as \"cEnd\"") != -1);
		assertTrue(tmpSQL.endsWith(")  and p3 = ? and t.x2 = ?"));
		query2.setIgnore("p3");
		assertTrue(query2.getPreparedSQL().endsWith(")  and t.x2 = ?"));
	}

	public void testSearch()
			throws Exception
	{
		Search search = f.createSearch("s1");
		assertEquals(2, search.getConditionIndex());
		assertEquals("c1", search.getConditionProperty(2).getName());
		assertEquals(3, search.getConditionPropertyCount());
		search = f.createSearch("s2");
		Search[] sArr = search.getOtherSearchs();
		assertEquals(2, sArr.length);
		assertEquals("s3", sArr[0].getName());
		assertEquals("s1", sArr[1].getName());
	}

	public void testUpdate()
			throws Exception
	{
		String tmpSQL;
		Update update = f.createUpdate("update1");
		update.setString("p2", "1");
		tmpSQL = update.getPreparedSQL();
		assertTrue(tmpSQL.indexOf(", p2 =") == -1);
		update.setString("p1", "1");
		tmpSQL = update.getPreparedSQL();
		assertTrue(tmpSQL.indexOf(", p2 =") != -1);

		update = f.createUpdate("update2");
		update.setSubSQL(1, "");
		tmpSQL = update.getPreparedSQL();
		assertTrue(tmpSQL.trim().length() == 0);
		update.setSubSQL(1, "X");
		tmpSQL = update.getPreparedSQL();
		assertTrue(tmpSQL.indexOf("where") != -1);
		update.setString("k2", "1");
		tmpSQL = update.getPreparedSQL();
		assertTrue(tmpSQL.indexOf("and") == -1);
		assertTrue(tmpSQL.indexOf(" or ") != -1);
		update.setString("k1", "1");
		tmpSQL = update.getPreparedSQL();
		assertTrue(tmpSQL.indexOf(" and ") != -1);

		update = f.createUpdate("update3");
		update.setSubSQL(1, "");
		update.setSubSQL(2, "");
		tmpSQL = update.getPreparedSQL();
		assertTrue(tmpSQL.indexOf("T2") != -1);
		assertTrue(tmpSQL.indexOf("test") != -1);
		assertTrue(tmpSQL.indexOf("Y") != -1);
		update.setSubSQL(2, "ssss");
		tmpSQL = update.getPreparedSQL();
		assertTrue(tmpSQL.indexOf("X ssss") != -1);
		update.setSubSQL(1, "a = 1");
		tmpSQL = update.getPreparedSQL();
		assertTrue(tmpSQL.indexOf("and (") != -1);
		assertTrue(tmpSQL.indexOf(")") != -1);
		update.setString("k1", "1");
		update.setString("k2", "2");
		tmpSQL = update.getPreparedSQL();
		assertTrue(tmpSQL.indexOf(" or ") != -1);

		update = f.createUpdate("update5");
		update.setSubSQL(1, "");
		update.setSubSQL(2, "");
		tmpSQL = update.getPreparedSQL();
		assertTrue(tmpSQL.indexOf("tt2") != -1);
		assertTrue(tmpSQL.indexOf("tt1") == -1);

		update = f.createUpdate("update6");
		update.setSubSQL(1, "");
		update.setSubSQL(2, "");
		tmpSQL = update.getPreparedSQL();
		assertTrue(tmpSQL.indexOf("endW") == -1);
		update.setString("k2", "2");
		tmpSQL = update.getPreparedSQL();
		assertTrue(tmpSQL.indexOf("endW") != -1);
		update.setIgnore("k2");
		update.setSubSQL(1, "a = 1");
		tmpSQL = update.getPreparedSQL();
		assertTrue(tmpSQL.indexOf("endW") != -1);
	}

	public void testElse()
			throws Exception
	{
		Model model = f.createModel("m1");
		assertEquals("s1", model.getExecute(1).getName());

		View view = f.createView("v1");
		assertEquals("div", ((Component) view.getComponents().next()).getType());

		ModelExport export = f.getModelExport("export");
		assertEquals("t.jsp", export.getPath());

		Resource resource = f.getResource("testR");
		assertEquals("begint.jspend", resource.getValue());
	}

	static void init()
	{
		StringRef msg = new StringRef();
		try
		{
			FactoryContainer container = ContainerManager.createFactoryContainer("tmp",
					"cp:self/micromagic/eterna/digester2/d_test1.xml",
					null, null, null, null, null, false);
			//container.reInit(msg);
			//TimeLogger tl = new TimeLogger();
			container.reInit(msg);
			//System.out.println("init time:" + tl.formatPassTime(false));
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
				System.out.println(msg);
				(new ParseException(ex.getMessage())).printStackTrace();
			}
		}
	}
	static EternaFactory f;

	static
	{
		init();
	}

}