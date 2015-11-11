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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import self.micromagic.eterna.dao.Parameter;
import self.micromagic.eterna.dao.Query;
import self.micromagic.eterna.dao.ResultReader;
import self.micromagic.eterna.dao.ResultReaderManager;
import self.micromagic.eterna.dao.reader.ObjectReader;
import self.micromagic.eterna.dao.reader.ReaderFactory;
import self.micromagic.eterna.dao.reader.ReaderWrapper;
import self.micromagic.eterna.digester2.ContainerManager;
import self.micromagic.eterna.share.AttributeManager;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.util.container.PreFetchIterator;

public class DaoManagerTest extends TestCase
{
	static DaoManager daoManager;
	static Parameter[] paramArray;
	static Query query;

	static
	{
		daoManager = new DaoManager();
		paramArray = new Parameter[10];
		for (int i = 0; i < paramArray.length; i++)
		{
			paramArray[i] = new ParameterImpl( "name_" + i, "col_" + i, "String", i + 1,
					null, null, new AttributeManager());
		}
		TestQuery tmpQuery = new TestQuery(paramArray);
		tmpQuery.setReaderManager(createReaderManager());
		query  = tmpQuery;
		//this.factory = EternaFactoryCreater.getEternaFactory(this.getClass());
	}

	public void testFrontParse1()
	{
		String str = daoManager.preParse("#auto[update;1,3]", query);
		String result = "col_0 = ?, col_1 = ?, col_2 = ?";
		assertEquals(result, str);

		str = daoManager.preParse("##auto[update,1,3] #? #param(t)[t = ?] #sub[($)]", query);
		result = "##auto[update,1,3] #? #param(t)[t = ?] #sub[($)]";
		assertEquals(result, str);

		str = daoManager.preParse("#auto[and;6,-2]", query);
		result = "col_5 = ? and col_6 = ? and col_7 = ? and col_8 = ?";
		assertEquals(result, str);

		str = daoManager.preParse("#auto[insertN;1,3] ###auto[insertV;1,-1]", query);
		result = "col_0, col_1, col_2 ##?, ?, ?, ?, ?, ?, ?, ?, ?, ?";
		assertEquals(result, str);

		str = daoManager.preParse("#auto[or;i-name_2,i+name_3] ###auto[and;i=name_5,i+name_5]", query);
		result = "col_1 = ? or col_2 = ? or col_3 = ? or col_4 = ? ##col_5 = ? and col_6 = ?";
		assertEquals(result, str);
	}

	public void testFrontParse2()
	{
		try
		{
			String str = daoManager.preParse("#auto[update,dynamic;1,3]", query);
			String result = "#param(dAuto_0)[, col_0 = ?]#param(dAuto_1)[, col_1 = ?]#param(dAuto_2)[, col_2 = ?]";
			assertEquals(result, str);

			str = daoManager.preParse("##auto[update;1,3] #? #param(t)[t = ?] #sub[($)]", query);
			result = "##auto[update;1,3] #? #param(t)[t = ?] #sub[($)]";
			assertEquals(result, str);

			str = daoManager.preParse("#auto[and,dynamic;6,-2]", query);
			result = "#param(dAuto_5)[ and col_5 = ?]#param(dAuto_6)[ and col_6 = ?]"
					+ "#param(dAuto_7)[ and col_7 = ?]#param(dAuto_8)[ and col_8 = ?]";
			assertEquals(result, str);

			str = daoManager.preParse("#auto[insertN,dynamic;1,3] ###auto[insertV,dynamic;1,-1]", query);
			result = "#param(dAuto_0)[, col_0]#param(dAuto_1)[, col_1]#param(dAuto_2)[, col_2] ##"
					+ "#param(dAuto_0)[, ?]#param(dAuto_1)[, ?]#param(dAuto_2)[, ?]#param(dAuto_3)[, ?]"
					+ "#param(dAuto_4)[, ?]#param(dAuto_5)[, ?]#param(dAuto_6)[, ?]#param(dAuto_7)[, ?]"
					+ "#param(dAuto_8)[, ?]#param(dAuto_9)[, ?]";
			assertEquals(result, str);
		}
		catch (EternaException ex)
		{
			fail("执行时不应抛出异常");
		}
	}

	public void testFrontParse3()
	{
		List list = query.getReaderManager().getReaderList();
		assertEquals(7, list.size());
		assertEquals("a", ((ResultReader) list.get(0)).getName());
		assertEquals("b", ((ResultReader) list.get(1)).getName());
		assertEquals("y", ((ResultReader) list.get(2)).getName());
		assertEquals("c", ((ResultReader) list.get(3)).getName());
		assertEquals("z", ((ResultReader) list.get(4)).getName());
		assertEquals("d", ((ResultReader) list.get(5)).getName());
		assertEquals("x", ((ResultReader) list.get(6)).getName());

		assertEquals("b", query.getReaderManager().getReader("d").getAlias());
		String str = daoManager.preParse("select #auto[select]", query);
		String result = "select a as \"a\", b as \"b\", c as \"c\"";
		assertEquals(result, str);

		str = daoManager.preParse("select #auto[select;1,-2]", query);
		assertEquals(result, str);
		str = daoManager.preParse("select #auto[select;1,6]", query);
		assertEquals(result, str);
		str = daoManager.preParse("select #auto[select;1,4]", query);
		assertEquals(result, str);
		str = daoManager.preParse("select #auto[select;1,2]", query);
		result = "select a as \"a\", b as \"b\"";
		assertEquals(result, str);
		str = daoManager.preParse("select #auto[select;1,3]", query);
		assertEquals(result, str);
		str = daoManager.preParse("select #auto[select;2,4]", query);
		result = "select b as \"b\", c as \"c\"";
		assertEquals(result, str);
		str = daoManager.preParse("select #auto[select;4]", query);
		result = "select c as \"c\", b as \"b\"";
		assertEquals(result, str);

		try
		{
			str = daoManager.preParse("select #auto[select;1,7]", query);
			fail();
		}
		catch (Exception ex) {}
	}

	public void testFrontParse_error()
	{
		try
		{
			daoManager.preParse("#auto[other,1,3]", query);
			fail("设置类型错误时应抛出异常");
		}
		catch (EternaException ex) {}
		try
		{
			daoManager.preParse("#auto[other,1]", query);
			fail("参数个数错误时应抛出异常");
		}
		catch (EternaException ex) {}
		try
		{
			daoManager.preParse("#auto[other,1,3,a]", query);
			fail("参数个数错误时应抛出异常");
		}
		catch (EternaException ex) {}
		try
		{
			daoManager.preParse("#auto[or,i-name_1,i+col]", query);
			fail("参数名错误时应抛出异常");
		}
		catch (EternaException ex) {}
		try
		{
			daoManager.preParse("#auto[or,iXname_1,i+name_3]", query);
			fail("标签错误时应抛出异常");
		}
		catch (EternaException ex) {}
	}

	static ResultReaderManager createReaderManager()
	{
		ReaderManagerImpl r = new ReaderManagerImpl();
		ObjectReader reader = (ObjectReader) ReaderFactory.createReader("String", "a");
		reader.setAttribute(ResultReaderManager.SHOW_NAME_FLAG, "x");
		r.addReader(reader);
		reader = (ObjectReader) ReaderFactory.createReader("String", "b");
		r.addReader(reader);
		r.addReader(new ReaderWrapper(reader, "y", false));
		r.addReader(ReaderFactory.createReader("String", "c"));
		reader = (ObjectReader) ReaderFactory.createReader("String", "z");
		reader.setColumnIndex(1);
		r.addReader(reader);
		reader = (ObjectReader) ReaderFactory.createReader("String", "d");
		reader.setColumnName("b");
		r.addReader(reader);
		EternaFactory f = (EternaFactory) ContainerManager.getGlobalContainer().getFactory();
		f.setAttribute(ResultReaderManager.CHECK_SAME_COL, "1");
		r.initialize(f);
		return r;
	}

}

class TestQuery extends QueryImpl
{
	public TestQuery(Parameter[] parameters)
	{
		this.parameters = parameters;
	}
	private final Parameter[] parameters;

	public void setReaderManager(ResultReaderManager manager)
	{
		this.readerManager = manager;
	}

	public ResultReaderManager getReaderManager()
	{
		if (this.readerManager != null)
		{
			return this.readerManager;
		}
		return super.getReaderManager();
	}
	private ResultReaderManager readerManager;

	public Iterator getParameterIterator()
	{
		return new PreFetchIterator(Arrays.asList(this.parameters).iterator(), false);
	}

}