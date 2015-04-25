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

package self.micromagic.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.TestCase;
import self.micromagic.cg.BeanMapTest;
import self.micromagic.cg.TestArrayBean;
import self.micromagic.cg.TestMainBean;
import tool.PrivateAccessor;

public class ToolsTest extends TestCase
{

	public void testEquals2()
	{
		EqualBean eb1 = getEqualBeanWithArr("Teb");
		EqualBean eb2 = getEqualBeanWithArr("Teb");
		assertEquals(eb1, eb2);
		EqualTool.setTestModel(false);
		assertNotSame(eb1, eb2);
		EqualTool.setTestModel(true);
		eb1.getArr2()[1] = eb1;
		eb2.getArr2()[1] = eb2;
		assertEquals(eb1, eb2);

		Map m1 = new HashMap();
		m1.put("1", "2");
		m1.put("2", new Integer(1));
		m1.put("obj", getEqualBean("sub"));
		Map m2 = new TreeMap();
		m2.put("1", "2");
		m2.put("2", new Integer(1));
		m2.put("obj", getEqualBean("sub"));
		eb1.setMapValue(m1);
		eb2.setMapValue(m2);
		assertEquals(eb1, eb2);
	}

	public void testEquals1()
	{
		TestArrayBean ta1 = BeanMapTest.getArrayBean(null);
		TestArrayBean ta2 = BeanMapTest.getArrayBean(null);
		assertFalse(ta1.equals(ta2));
		assertTrue(EqualTool.checkEquals(ta1, ta2));
		EqualTool.setTestModel(false);
		assertFalse(EqualTool.checkEquals(ta1, ta2));
		EqualTool.setTestModel(true);
		ta2.getSubBean2()[0][0][0].setAmount(1.1);
		assertFalse(EqualTool.checkEquals(ta1, ta2));
	}

	public void testPrivateCaller()
			throws Exception
	{
		Object r;
		Object obj = PrivateAccessor.create(
				Class.forName("self.micromagic.util.UtilityTest$Private2"), new Object[0]);
		System.out.println(obj);
		PrivateAccessor.invoke(obj, "setD", new Object[]{new Double(100.2)});
		PrivateAccessor.set(obj, "i1", new Integer(200));
		r = PrivateAccessor.invoke(obj, "getI", new Object[0]);
		assertEquals(new Integer(200), r);
		PrivateAccessor.set(obj, "s1", "123");
		r = PrivateAccessor.get(obj, "d1");
		assertEquals(new Double(100.2), r);
		r = PrivateAccessor.get(obj, "s1");
		assertEquals("123", r);
		try
		{
			PrivateAccessor.get(obj, "notExists");
			fail();
		}
		catch (Exception ex)
		{
			System.out.println(ex);
		}

	}

	public static EqualBean getEqualBeanWithArr(String id)
	{
		EqualBean eb = new EqualBean();
		eb.setMyId(id);
		eb.setArrBean(BeanMapTest.getArrayBean(null));
		EqualBean[] ebArr = new EqualBean[3];
		ebArr[0] = getEqualBean(id + "1");
		ebArr[1] = getEqualBean(id + "2");
		ebArr[2] = getEqualBean(id + "3");
		eb.setArr2(ebArr);
		List l = new ArrayList();
		l.add("1");
		l.add(new Integer(2));
		l.add(getEqualBean("sub"));
		eb.setcValue(l);
		return eb;
	}

	public static EqualBean getEqualBean(String id)
	{
		EqualBean eb = new EqualBean();
		eb.setMyId(id);
		eb.setArrBean(BeanMapTest.getArrayBean(null));
		return eb;
	}

	public static class EqualBean extends TestMainBean
	{
		private TestArrayBean arrBean;
		private String myId;
		private EqualBean[] arr2;
		private Map mapValue;
		private Collection cValue;

		public Map getMapValue()
		{
			return this.mapValue;
		}

		public void setMapValue(Map mapValue)
		{
			this.mapValue = mapValue;
		}

		public Collection getcValue()
		{
			return this.cValue;
		}

		public void setcValue(Collection cValue)
		{
			this.cValue = cValue;
		}

		public EqualBean[] getArr2()
		{
			return this.arr2;
		}

		public void setArr2(EqualBean[] arr2)
		{
			this.arr2 = arr2;
		}

		public TestArrayBean getArrBean()
		{
			return this.arrBean;
		}

		public void setArrBean(TestArrayBean arrBean)
		{
			this.arrBean = arrBean;
		}

		public String getMyId()
		{
			return this.myId;
		}

		public void setMyId(String myId)
		{
			this.myId = myId;
		}

		public boolean equals(Object obj)
		{
			return EqualTool.checkEquals(this, obj);
		}

	}

}