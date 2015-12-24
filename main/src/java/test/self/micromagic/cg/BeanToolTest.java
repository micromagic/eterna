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

package self.micromagic.cg;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import self.micromagic.eterna.dao.CustomResultIterator;
import self.micromagic.eterna.dao.ResultRow;
import self.micromagic.eterna.digester2.ContainerManager;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.FactoryContainer;
import self.micromagic.util.UtilityTest;
import self.micromagic.util.container.ValueContainerMap;
import self.micromagic.util.converter.IntegerConverter;
import self.micromagic.util.converter.LongConverter;
import tool.PrivateAccessor;

public class BeanToolTest extends TestCase
{
	protected void setUp()
			throws Exception
	{
		//Utility.setProperty(ClassGenerator.COMPILE_LOG_PROPERTY, "1");
		//Tool.registerBean(MyBean.class.getName());
		//Tool.registerBean(TestBean.class.getName());
		//Tool.registerBean(TestMainBean.class.getName());
		//Tool.registerBean(TestSubBean.class.getName());
	}

	public void testGetField()
	{
      Field f;

		f = BeanTool.getField(MyBean2.class, "name");
		assertEquals(f.getName(), "name");
		assertEquals(f.getDeclaringClass(), MyBean2.class);

		f = BeanTool.getField(ChildBean.class, "name2");
		assertEquals(f.getName(), "name2");
		assertEquals(f.getDeclaringClass(), ChildBean.class);

		f = BeanTool.getField(ChildBean.class, "name");
		assertEquals(f.getName(), "name");
		assertEquals(f.getDeclaringClass(), MyBean2.class);

		assertNull(BeanTool.getField(ChildBean.class, "name3"));
	}

	public void testReleaseBeanDescriptor()
			throws Exception
	{
		//Utility.setProperty(ClassGenerator.COMPILE_TYPE_PROPERTY, "ant");
		Map softM = (Map) PrivateAccessor.get(BeanMethodInfo.class, "beanMethodsCache");
		ClassGenerator cg = ClassGeneratorTest.getClassGenerator("test.release.Bean");
		ClassLoader cl = new UtilityTest.ReleaseTestClassLoader(this.getClass().getClassLoader());
		cg.setClassLoader(cl);
		Object obj = cg.createClass().newInstance();
		ClassKeyCache m = (ClassKeyCache) PrivateAccessor.get(BeanTool.class, "beanDescriptorCache");
		//Map m = (Map) PrivateCaller.getPrivateField(BeanTool.class, "beanDescriptorCache");
		int oldSize = m.size();
		BeanMap bm = BeanTool.getBeanMap(obj);
		assertEquals("size1:", oldSize + 1, m.size());
		bm.put("intValue", "100");
		assertEquals(new Integer(100), bm.get("intValue"));
		bm = null;
		obj = null;
		cg = null;
		cl = null;
		// 这里需要等待BeanMethodInfo中的SoftReference释放
		System.gc();
		Thread.sleep(3000L);
		System.gc();
		if (softM.size() > 0)
		{
			// 如果还没释放, 则直接清掉
			softM.clear();
			System.gc();
			Thread.sleep(2000L);
		}
		assertEquals("size2:", oldSize, m.size());
	}

	public void testResultRowSet()
			throws Exception
	{
		MyBean expTB = this.getMyBean();
		expTB.setValid(true);
		expTB.getMainBean().getSubInfo().setAddress(null);

		MyBean tb1 = new MyBean();
		Map map = ValueContainerMap.createResultRowMap(this.getResultRow());
		BeanTool.setBeanValues(tb1, map);
		assertEquals(expTB.toString(), tb1.toString());

		MyBean tb2 = new MyBean();
		BeanTool.getBeanMap(tb2).setValues(this.getResultRow());
		assertEquals(expTB.toString(), tb2.toString());
	}

	public void testSetBean5()
	{
		MyBean mb1 = this.getMyBean();
		mb1.setValid(true);
		MyBean2 mb2 = new MyBean2();
		BeanMap bm1 = BeanTool.getBeanMap(mb1);
		BeanTool.getBeanMap(mb2).setValues(bm1);
		assertEquals(this.getMyBean2().toString(), mb2.toString());
	}

	public void testSetBean4()
	{
		//Utility.setProperty(CG.COMPILE_TYPE_PROPERTY, "ant");
		Map map = new HashMap();
		map.put("valid", "true");
		map.put("name", "aaa");
		map.put("mainBean.id", "001");
		map.put("mainBean.subInfo.id", "001");
		map.put("mainBean.subInfo.amount", new String[]{"1000.01", "2.13"});
		map.put("mainBean.other", "1");
		map.put("mainBean.birth", "2012-5-27");
		map.put("mainBean.subInfo.address", "地点不明");
		map.put("mainBean.subInfo.phone", new String[]{"123", "abc"});
		map.put("mainBean.subInfo.otherInfo", Arrays.asList(new String[]{"a", "1", "b", "2"}));

		MyBean expTB = this.getMyBean();
		expTB.getMainBean().setName("new test");
		expTB.setValid(true);

		MyBean tb = new MyBean();
		TestMainBean tmb = new TestMainBean();
		tmb.setGradeYear(2016);
		tmb.setName("new test");
		tb.setMainBean(tmb);
		BeanTool.getBeanMap(tb).setValues(map);

		assertEquals(expTB.toString(), tb.toString());
	}

	public void testShowMyBean2Info()
			throws Exception
	{
		//System.out.println(BeanTool.getBeanMap(this.getMyBean()));
		MyBean tb = new MyBean();
		BeanMap bm = BeanTool.getBeanMap(tb);
		bm.put("mainBean.gradeYear", "2012");
		assertEquals("1:", tb.getMainBean().getGradeYear(), 2012);
		bm.put("mainBean.gradeYear", null);
		assertEquals("2:", tb.getMainBean().getGradeYear(), 0);
		bm.put("mainBean.subInfo.amount", "100");
		assertEquals("3:", (int) tb.getMainBean().getSubInfo().getAmount(), 100);
		bm.clear();
		assertEquals("4:", (int) tb.getMainBean().getSubInfo().getAmount(), 0);
	}

	public void testChangeConverter()
	{
		DecimalFormat f = new DecimalFormat("#,##0");
		IntegerConverter ic = new IntegerConverter();
		ic.setNumberFormat(f);
		LongConverter lc = new LongConverter();
		lc.setNumberFormat(f);
		TestBean tb;
		TestBean expTB = new TestBean();
		Map map = new HashMap();
		map.put("intValue", "1,200");
		map.put("longValue", "1,500");

		tb = new TestBean();
		assertEquals(0, BeanTool.getBeanMap(tb).setValues(map));
		assertEquals("1", expTB.toString(), tb.toString());

		BeanTool.registerConverter(TestBean.class, int.class, ic);
		tb = new TestBean();
		assertEquals(0, BeanTool.setBeanValues(tb, map));
		assertEquals("2", expTB.toString(), tb.toString());
		expTB.setIntValue(1200);
		assertEquals(1, BeanTool.getBeanMap(tb).setValues(map));
		assertEquals("3", expTB.toString(), tb.toString());

		tb = new TestBean();
		BeanMap bm = BeanTool.getBeanMap(tb);
		bm.registerConverter(long.class, lc);
		assertEquals(1, BeanTool.getBeanMap(tb).setValues(map));
		assertEquals("4", expTB.toString(), tb.toString());
		expTB.setLongValue(1500);
		assertEquals(2, bm.setValues(map));
		assertEquals("5", expTB.toString(), tb.toString());

		BeanTool.registerConverter(TestBean.class, int.class, new IntegerConverter());
	}

	public void testRegisterConverter()
	{
		try
		{
			BeanTool.registerConverter(int.class, new LongConverter());
			fail("基本类型不能设定其他类型的转换器");
		}
		catch (IllegalArgumentException ex)
		{
			System.out.println("ok: " + ex);
		}
		try
		{
			BeanTool.registerConverter(int.class, new IntegerConverter());
		}
		catch (IllegalArgumentException ex)
		{
			fail("基本类型可以设置同类型的转换器");
		}
	}

	public void testSetBean3()
	{
		MyBean expTB = this.getMyBean();

		Map sub = new HashMap();
		sub.put("id", "001");
		sub.put("amount", new String[]{"1000.01", "2.13"});
		sub.put("phone", new String[]{"123", "abc"});
		sub.put("otherInfo", Arrays.asList(new String[]{"a", "1", "b", "2"}));
		sub.put("address", "地点不明");
		Map map = new HashMap();
		map.put("valid", "false");
		map.put("name", "aaa");
		map.put("mainBean.id", "001");
		map.put("mainBean.name", "测试用户");
		map.put("mainBean.birth", "2012-5-27");
		map.put("mainBean.gradeYear", "2016");
		map.put("mainBean.subInfo", sub);
		MyBean tb = new MyBean();
		BeanTool.setBeanValues(tb, map);
		assertEquals("1", expTB.toString(), tb.toString());

		sub = new HashMap();
		sub.put("id", "001");
		sub.put("name", "测试用户");
		sub.put("birth", "2012-5-27");
		sub.put("gradeYear", "2016");
		sub.put("subInfo.id", "001");
		sub.put("subInfo.amount", new String[]{"1000.01", "2.13"});
		sub.put("subInfo.phone", new String[]{"123", "abc"});
		sub.put("subInfo.otherInfo", Arrays.asList(new String[]{"a", "1", "b", "2"}));
		sub.put("subInfo.address", "地点不明");
		map = new HashMap();
		map.put("valid", "false");
		map.put("name", "aaa");
		map.put("mainBean", sub);
		tb = new MyBean();
		BeanTool.setBeanValues(tb, map);
		assertEquals("2", expTB.toString(), tb.toString());

		Map sub2 = new HashMap();
		sub2.put("id", "001");
		sub2.put("amount", new String[]{"1000.01", "2.13"});
		sub2.put("phone", new String[]{"123", "abc"});
		sub2.put("otherInfo", Arrays.asList(new String[]{"a", "1", "b", "2"}));
		sub2.put("address", "地点不明");
		sub = new HashMap();
		sub.put("id", "001");
		sub.put("name", "测试用户");
		sub.put("birth", "2012-5-27");
		sub.put("gradeYear", "2016");
		sub.put("subInfo", sub2);
		map = new HashMap();
		map.put("valid", "false");
		map.put("name", "aaa");
		map.put("mainBean", sub);
		tb = new MyBean();
		BeanTool.setBeanValues(tb, map);
		assertEquals("3", expTB.toString(), tb.toString());
	}

	public void testSetBean3_2()
	{
		MyBean expTB = this.getMyBean();

		Map sub = new HashMap();
		sub.put("id", "001");
		sub.put("amount", new String[]{"1000.01", "2.13"});
		sub.put("phone", new String[]{"123", "abc"});
		sub.put("otherInfo", Arrays.asList(new String[]{"a", "1", "b", "2"}));
		sub.put("address", "地点不明");
		Map map = new HashMap();
		map.put("valid", "false");
		map.put("name", "aaa");
		map.put("mainBean.id", "001");
		map.put("mainBean.name", "测试用户");
		map.put("mainBean.birth", "2012-5-27");
		map.put("mainBean.gradeYear", "2016");
		map.put("mainBean.subInfo", sub);
		MyBean tb = new MyBean();
		BeanTool.getBeanMap(tb, "").setValues(map);
		assertEquals("1", expTB.toString(), tb.toString());

		sub = new HashMap();
		sub.put("id", "001");
		sub.put("name", "测试用户");
		sub.put("birth", "2012-5-27");
		sub.put("gradeYear", "2016");
		sub.put("subInfo.id", "001");
		sub.put("subInfo.amount", new String[]{"1000.01", "2.13"});
		sub.put("subInfo.phone", new String[]{"123", "abc"});
		sub.put("subInfo.otherInfo", Arrays.asList(new String[]{"a", "1", "b", "2"}));
		sub.put("subInfo.address", "地点不明");
		map = new HashMap();
		map.put("valid", "false");
		map.put("name", "aaa");
		map.put("mainBean", sub);
		tb = new MyBean();
		BeanTool.getBeanMap(tb, "").setValues(map);
		assertEquals("2", expTB.toString(), tb.toString());

		Map sub2 = new HashMap();
		sub2.put("id", "001");
		sub2.put("amount", new String[]{"1000.01", "2.13"});
		sub2.put("phone", new String[]{"123", "abc"});
		sub2.put("otherInfo", Arrays.asList(new String[]{"a", "1", "b", "2"}));
		sub2.put("address", "地点不明");
		sub = new HashMap();
		sub.put("id", "001");
		sub.put("name", "测试用户");
		sub.put("birth", "2012-5-27");
		sub.put("gradeYear", "2016");
		sub.put("subInfo", sub2);
		map = new HashMap();
		map.put("valid", "false");
		map.put("name", "aaa");
		map.put("mainBean", sub);
		tb = new MyBean();
		BeanTool.getBeanMap(tb, "").setValues(map);
		assertEquals("3", expTB.toString(), tb.toString());
	}

	public void testSetBean2()
	{
		Map map = new HashMap();
		map.put("valid", "false");
		map.put("name", "aaa");
		map.put("mainBean.id", "001");
		map.put("mainBean.subInfo.id", "001");
		map.put("mainBean.name", "测试用户");
		map.put("mainBean.subInfo.amount", new String[]{"1000.01", "2.13"});
		map.put("mainBean.other", "1");
		map.put("mainBean.birth", "2012-5-27");
		map.put("mainBean.gradeYear", "2016");
		map.put("mainBean.subInfo.address", "地点不明");
		map.put("mainBean.subInfo.phone", new String[]{"123", "abc"});
		map.put("mainBean.subInfo.otherInfo", Arrays.asList(new String[]{"a", "1", "b", "2"}));
		MyBean expTB = this.getMyBean();
		MyBean tb = new MyBean();
		BeanTool.setBeanValues(tb, map);
		assertEquals(expTB.toString(), tb.toString());
	}

	public void testSetBean2_2()
	{
		Map map = new HashMap();
		map.put("valid", "true");
		map.put("name", "aaa");
		map.put("mainBean.id", "001");
		map.put("mainBean.subInfo.id", "001");
		map.put("mainBean.name", "测试用户");
		map.put("mainBean.subInfo.amount", new String[]{"1000.01", "2.13"});
		map.put("mainBean.other", "1");
		map.put("mainBean.birth", "2012-5-27");
		map.put("mainBean.gradeYear", "2016");
		map.put("mainBean.subInfo.address", "地点不明");
		map.put("mainBean.subInfo.phone", new String[]{"123", "abc"});
		map.put("mainBean.subInfo.otherInfo", Arrays.asList(new String[]{"a", "1", "b", "2"}));
		MyBean expTB = this.getMyBean();
		expTB.setValid(true);
		MyBean tb = new MyBean();
		BeanTool.getBeanMap(tb).setValues(map);
		assertEquals(expTB.toString(), tb.toString());
	}

	public void testSetBean1()
	{
		Map map = new HashMap();
		TestBean expTB = this.getTestBean(map);
		TestBean tb = new TestBean();
		BeanTool.setBeanValues(tb, map);
		assertEquals(expTB.toString(), tb.toString());
	}

	public void testSetBean1_2()
	{
		Map map = new HashMap();
		TestBean expTB = this.getTestBean(map);
		TestBean tb = new TestBean();
		BeanTool.getBeanMap(tb, "").setValues(map);
		assertEquals(expTB.toString(), tb.toString());
	}

	private static FactoryContainer instance;
	private ResultRow getResultRow()
			throws Exception
	{
		if (instance == null)
		{
			instance = ContainerManager.createFactoryContainer(
					"beanTool.shareTest", "cp:self/micromagic/cg/shareTest.xml", null);
		}
		EternaFactory f = (EternaFactory) instance.getFactory();
		CustomResultIterator cri = new CustomResultIterator(f.getEntity("beanTool.test"), null);
		cri.createRow(new Object[]{
			"true", "aaa", "001", "001", "测试用户", new String[]{"1000.01", "2.13"}, "1", "2012-5-27",
			"2016", new String[]{"123", "abc"}, Arrays.asList(new String[]{"a", "1", "b", "2"})
		});
		cri.finishCreateRow();
		return cri.nextRow();
	}

	private MyBean2 getMyBean2()
	{
		MyBean2 mb2 = new MyBean2();
		mb2.setValid(true);
		mb2.setName("aaa");
		TestSubBean tsb = new TestSubBean();
		tsb.setId("001");
		mb2.setMainBean(tsb);
		return mb2;
	}

	private MyBean getMyBean()
	{
		MyBean mb = new MyBean();
		mb.setValid(false);
		mb.setName("aaa");
		TestMainBean tmb = new TestMainBean();
		tmb.setId("001");
		tmb.setName("测试用户");
		tmb.setGradeYear(2016);
		tmb.setBirth(BeanTool.utilDateConverter.convertToDate("2012-5-27"));
		TestSubBean tsb = new TestSubBean();
		tsb.setAddress("地点不明");
		tsb.setAmount(1000.01);
		tsb.setId("001");
		tsb.setPhone("123,abc");
		tsb.setOtherInfo(Arrays.asList(new String[]{"a", "1", "b", "2"}));
		tmb.setSubInfo(tsb);
		mb.setMainBean(tmb);
		return mb;
	}

	private TestBean getTestBean(Map mapValue)
	{
		TestBean tb = new TestBean();
		tb.setBooleanValue(false);
		mapValue.put("booleanValue", "false");
		tb.setBooleanValue2(Boolean.TRUE);
		mapValue.put("booleanValue2", "TRUE");
		tb.setByteValue((byte) 12);
		mapValue.put("byteValue", "12");
		tb.setByteValue2(new Byte((byte) 22));
		mapValue.put("byteValue2", "22");
		tb.setShortValue((short) 55);
		mapValue.put("shortValue", "55");
		tb.setShortValue2(new Short((short) 66));
		mapValue.put("shortValue2", "66");
		tb.setCharValue('a');
		mapValue.put("charValue", "a");
		tb.setCharValue2(new Character('是'));
		mapValue.put("charValue2", "是");
		tb.setDoubleValue(3.5);
		mapValue.put("doubleValue", "3.5");
		tb.setDoubleValue2(new Double(12.725));
		mapValue.put("doubleValue2", "12.725");
		tb.setFloatValue(6.25f);
		mapValue.put("floatValue", "6.25");
		tb.setFloatValue2(new Float(126.325f));
		mapValue.put("floatValue2", "126.325");
		tb.setIntValue(123);
		mapValue.put("intValue", "123");
		tb.setIntValue2(new Integer(678));
		mapValue.put("intValue2", "678");
		tb.setLongValue(100000000000000L);
		mapValue.put("longValue", "100000000000000");
		tb.setLongValue2(new Long(200000000000000L));
		mapValue.put("longValue2", "200000000000000");
		tb.setStringValue("123abv哈哈");
		mapValue.put("stringValue", "123abv哈哈");
		tb.publicBooleanValue = Boolean.TRUE;
		mapValue.put("publicBooleanValue", "1");
		tb.publicCharValue = '1';
		mapValue.put("publicCharValue", "1");
		tb.publicDoubleValue = 100000000000000.25;
		mapValue.put("publicDoubleValue", "100000000000000.25");
		tb.publicIntValue = 111112223;
		mapValue.put("publicIntValue", "111112223");
		tb.publicStringValue = "test 123 end.";
		mapValue.put("publicStringValue", "test 123 end.");
		return tb;
	}

	public static class MyBean
	{
		private boolean valid;
		private String name;
		private TestMainBean mainBean;

		public boolean isValid()
		{
			return this.valid;
		}

		public void setValid(boolean valid)
		{
			this.valid = valid;
		}

		public String getName()
		{
			return this.name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public TestMainBean getMainBean()
		{
			return this.mainBean;
		}

		public void setMainBean(TestMainBean mainBean)
		{
			this.mainBean = mainBean;
		}

		public String toString()
		{
			String s = "v:" + this.valid + ", name:" + this.name + "; " + this.mainBean;
			return s;
		}

	}

	public static class MyBean2
	{
		private boolean valid;
		private String name;

		private TestSubBean subBean;

		public boolean isValid()
		{
			return this.valid;
		}

		public void setValid(boolean valid)
		{
			this.valid = valid;
		}

		public String getName()
		{
			return this.name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public TestSubBean getMainBean()
		{
			return this.subBean;
		}

		public void setMainBean(TestSubBean mainBean)
		{
			this.subBean = mainBean;
		}

		public String toString()
		{
			String s = "v:" + this.valid + ", name:" + this.name + "; " + this.subBean;
			return s;
		}

	}

	public static class ChildBean extends MyBean2
	{
		private String name2;
		private int tmp1;

	}


}