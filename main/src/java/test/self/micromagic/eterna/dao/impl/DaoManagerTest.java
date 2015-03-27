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

import java.util.Arrays;
import java.util.Iterator;

import junit.framework.TestCase;
import self.micromagic.eterna.dao.Parameter;
import self.micromagic.eterna.dao.Query;
import self.micromagic.eterna.dao.ResultReaderManager;
import self.micromagic.eterna.share.AttributeManager;
import self.micromagic.eterna.share.EternaException;
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
					null, new AttributeManager());
		}
		query = new TestQuery(paramArray);
		//this.factory = EternaFactoryCreater.getEternaFactory(this.getClass());
	}


	public void testFrontParse1()
	{
		try
		{
			String str = daoManager.frontParse("#auto[update;1,3]", query);
			String result = "col_0 = ?, col_1 = ?, col_2 = ?";
			assertEquals(result, str);

			str = daoManager.frontParse("##auto[update,1,3] #? #param(t)[t = ?] #sub[($)]", query);
			result = "##auto[update,1,3] #? #param(t)[t = ?] #sub[($)]";
			assertEquals(result, str);

			str = daoManager.frontParse("#auto[and;6,-2]", query);
			result = "col_5 = ? and col_6 = ? and col_7 = ? and col_8 = ?";
			assertEquals(result, str);

			str = daoManager.frontParse("#auto[insertN;1,3] ###auto[insertV;1,-1]", query);
			result = "col_0, col_1, col_2 ##?, ?, ?, ?, ?, ?, ?, ?, ?, ?";
			assertEquals(result, str);

			str = daoManager.frontParse("#auto[or;i-name_2,i+name_3] ###auto[and;i=name_5,i+name_5]", query);
			result = "col_1 = ? or col_2 = ? or col_3 = ? or col_4 = ? ##col_5 = ? and col_6 = ?";
			assertEquals(result, str);
		}
		catch (EternaException ex)
		{
			fail("执行时不应抛出异常");
		}
	}

	public void testFrontParse2()
	{
		try
		{
			String str = daoManager.frontParse("#auto[update,dynamic;1,3]", query);
			String result = "#param(dAuto_0)[, col_0 = ?]#param(dAuto_1)[, col_1 = ?]#param(dAuto_2)[, col_2 = ?]";
			assertEquals(result, str);

			str = daoManager.frontParse("##auto[update;1,3] #? #param(t)[t = ?] #sub[($)]", query);
			result = "##auto[update;1,3] #? #param(t)[t = ?] #sub[($)]";
			assertEquals(result, str);

			str = daoManager.frontParse("#auto[and,dynamic;6,-2]", query);
			result = "#param(dAuto_5)[ and col_5 = ?]#param(dAuto_6)[ and col_6 = ?]"
					+ "#param(dAuto_7)[ and col_7 = ?]#param(dAuto_8)[ and col_8 = ?]";
			assertEquals(result, str);

			str = daoManager.frontParse("#auto[insertN,dynamic;1,3] ###auto[insertV,dynamic;1,-1]", query);
			result = "#param(dAuto_0)[, col_0]#param(dAuto_1)[, col_1]#param(dAuto_2)[, col_2] ##"
					+ "#param(dAuto_0)[, ?]#param(dAuto_1)[, ?]#param(dAuto_2)[, ?]#param(dAuto_3)[, ?]"
					+ "#param(dAuto_4)[, ?]#param(dAuto_5)[, ?]#param(dAuto_6)[, ?]#param(dAuto_7)[, ?]"
					+ "#param(dAuto_8)[, ?]#param(dAuto_9)[, ?]";
			assertEquals(result, str);
		}
		catch (EternaException ex)
		{
			ex.printStackTrace();
			fail("执行时不应抛出异常");
		}
	}

	public void testFrontParse_error()
	{
		try
		{
			daoManager.frontParse("#auto[other,1,3]", query);
			fail("设置类型错误时应抛出异常");
		}
		catch (EternaException ex) {}
		try
		{
			daoManager.frontParse("#auto[other,1]", query);
			fail("参数个数错误时应抛出异常");
		}
		catch (EternaException ex) {}
		try
		{
			daoManager.frontParse("#auto[other,1,3,a]", query);
			fail("参数个数错误时应抛出异常");
		}
		catch (EternaException ex) {}
		try
		{
			daoManager.frontParse("#auto[or,i-name_1,i+col]", query);
			fail("参数名错误时应抛出异常");
		}
		catch (EternaException ex) {}
		try
		{
			daoManager.frontParse("#auto[or,iXname_1,i+name_3]", query);
			fail("标签错误时应抛出异常");
		}
		catch (EternaException ex) {}
	}

}

class TestQuery extends QueryImpl
{
	public TestQuery(Parameter[] parameters)
	{
		this.parameters = parameters;
	}
	private final Parameter[] parameters;

	public ResultReaderManager getReaderManager()
	{
		return super.getReaderManager();
	}

	public Iterator getParameterIterator()
	{
		return new PreFetchIterator(Arrays.asList(this.parameters).iterator(), false);
	}

}