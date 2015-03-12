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

package self.micromagic.util;

import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import self.micromagic.cg.ClassGenerator;
import tool.PrivateAccessor;

public class UtilityTest extends TestCase
{
	protected static boolean TP1;
	protected static int TP2 = 20;
	protected static String TP3;
	protected static String TP2_2;
	protected static String TP1_2;
	protected static java.sql.Date TP3_2;
	protected static long TP2_3;
	protected static double TP2_4;
	protected static short TP2_5;

	protected String notStatic;
	protected static final String finalField = "1";


	public void testAddPropertyManager()
	{
		try
		{
			// 键值不能为null
			Utility.addFieldPropertyManager(null, this.getClass(), "TP1");
			fail();
		}
		catch (Exception ex)
		{
			//System.out.println(ex);
		}
		try
		{
			// 必需是静态的属性
			Utility.addFieldPropertyManager("test.p1", this.getClass(), "notStatic");
			fail();
		}
		catch (Exception ex)
		{
			//System.out.println(ex);
		}
		try
		{
			// 必需是非final的属性
			Utility.addFieldPropertyManager("test.p1", this.getClass(), "finalField");
			fail();
		}
		catch (Exception ex)
		{
			//System.out.println(ex);
		}
		try
		{
			// 正常的方法绑定
			Utility.addMethodPropertyManager("test.p1", this.getClass(), "setString");
		}
		catch (Exception ex)
		{
			fail();
		}
		try
		{
			// 绑定的方法参数必需是一个
			Utility.addMethodPropertyManager("test.p1", this.getClass(), "setString2");
			fail();
		}
		catch (Exception ex)
		{
			//System.out.println(ex);
		}
		try
		{
			// 绑定的方法的参数必需是一个String类型的
			Utility.addMethodPropertyManager("test.p1", this.getClass(), "setInt");
			fail();
		}
		catch (Exception ex)
		{
			//System.out.println(ex);
		}
		try
		{
			// 必需是静态的方法
			Utility.addMethodPropertyManager("test.p1", this.getClass(), "setString3");
			fail();
		}
		catch (Exception ex)
		{
			//System.out.println(ex);
		}
		try
		{
			// 属性不能以方法的方式绑定
			Utility.addMethodPropertyManager("test.p1", this.getClass(), "TP1");
			fail();
		}
		catch (Exception ex)
		{
			//System.out.println(ex);
		}
		try
		{
			// 方法不能以属性的方式绑定
			Utility.addFieldPropertyManager("test.p1", this.getClass(), "setString");
			fail();
		}
		catch (Exception ex)
		{
			//System.out.println(ex);
		}
	}
	public static void setString(String s)
	{
	}
	public static void setString2(String s1, String s2)
	{
	}
	public static void setInt(int i)
	{
	}
	public void setString3(String s)
	{
	}

	public void testPropertyChange()
			throws Exception
	{
		Utility.addFieldPropertyManager("test.p1", this.getClass(), "TP1");
		assertEquals(TP1, true);
		Utility.addFieldPropertyManager("test.p2", this.getClass(), "TP2");
		// 配置中没有设置值时, 保持属性原来的值
		assertEquals(TP2, 20);
		Utility.addFieldPropertyManager("test.p3", this.getClass(), "TP3");
		assertEquals(TP3, null);
		Utility.addFieldPropertyManager("test.p1", this.getClass(), "TP1_2");
		assertEquals(TP1_2, "true");
		Utility.addFieldPropertyManager("test.p2", this.getClass(), "TP2_2");
		assertEquals(TP2_2, null);
		Utility.addFieldPropertyManager("test.p3", this.getClass(), "TP3_2");
		assertEquals(TP3_2, null);
		Utility.addFieldPropertyManager("test.p2", this.getClass(), "TP2_3");
		assertEquals(TP2_3, 0L);
		Utility.addFieldPropertyManager("test.p2", this.getClass(), "TP2_4");
		assertEquals(TP2_4, 0.0, 0.00000001);
		Utility.addFieldPropertyManager("test.p2", this.getClass(), "TP2_5");
		assertEquals(TP2_5, (short) 0);

		Utility.setProperty("test.p1", "0");
		assertEquals(TP1, false);
		assertEquals(TP1_2, "0");
		Utility.setProperty("test.p1", "1");
		assertEquals(TP1, true);
		assertEquals(TP1_2, "1");
		Utility.setProperty("test.p1", "fAlse");
		assertEquals(TP1, false);
		assertEquals(TP1_2, "fAlse");
		Utility.setProperty("test.p1", "TRUE");
		assertEquals(TP1, true);
		assertEquals(TP1_2, "TRUE");

		Utility.setProperty("test.p2", "100");
		assertEquals(TP2, 100);
		assertEquals(TP2_2, "100");
		assertEquals(TP2_3, 100L);
		assertEquals(TP2_4, 100.0, 0.00000001);
		assertEquals(TP2_5, (short) 100.0);
		Utility.setProperty("test.p2", "a100");
		assertEquals(TP2, 100);
		assertEquals(TP2_2, "a100");
		assertEquals(TP2_3, 100L);
		assertEquals(TP2_4, 100.0, 0.00000001);
		assertEquals(TP2_5, (short) 100.0);

		Utility.setProperty("test.p3", "haha哈哈");
		assertEquals(TP3, "haha哈哈");
		assertEquals(TP3_2, null);
		Utility.setProperty("test.p3", "2010-10-23");
		assertEquals(TP3, "2010-10-23");
		Calendar calendar = Calendar.getInstance();
		calendar.set(2010, 10 - 1, 23, 0, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		assertEquals(TP3_2, new java.sql.Date(calendar.getTimeInMillis()));
	}

	public void testAddSamePropertyManager()
			throws Exception
	{
		String propName = "test.addSame.value1";
		Object propsManager = PrivateAccessor.get(Utility.class, "propertiesManager");
		Object defaultPL = PrivateAccessor.get(propsManager, "defaultPL");
		Map propertyMap = (Map) PrivateAccessor.get(defaultPL, "propertyMap");
		Object[] arr = (Object[]) propertyMap.get(propName);
		assertNull(arr);

		Utility.addFieldPropertyManager(propName, this.getClass(), "TP1");
		arr = (Object[]) propertyMap.get(propName);
		assertEquals(1, arr.length);
		Utility.addFieldPropertyManager(propName, this.getClass(), "TP1_2");
		arr = (Object[]) propertyMap.get(propName);
		assertEquals(2, arr.length);
		Utility.addFieldPropertyManager(propName, this.getClass(), "TP1");
		arr = (Object[]) propertyMap.get(propName);
		assertEquals(2, arr.length);
	}

	public void testReleasePropertyManager()
			throws Exception
	{
		PropertiesManager.weakRefMember = true;
		this.doTestReleasePropertyManager("ant");
		this.doTestReleasePropertyManager("javassist");
		PropertiesManager.weakRefMember = false;
	}

	public void doTestReleasePropertyManager(String compileType)
			throws Exception
	{
		String propName = "test.release.value1";
		Object propsManager = PrivateAccessor.get(Utility.class, "propertiesManager");
		Object defaultPL = PrivateAccessor.get(propsManager, "defaultPL");
		Map propertyMap = (Map) PrivateAccessor.get(defaultPL, "propertyMap");
		Object[] arr = (Object[]) propertyMap.get(propName);
		assertNull(arr);

		ResManager res = new ResManager();
		res.load(this.getClass().getResourceAsStream("ResManagerTest.res"));
		ClassGenerator cg = new ClassGenerator();
		cg.setCompileType(compileType);
		cg.importPackage(ClassGenerator.getPackageString(this.getClass()));
		cg.setClassName(ClassGenerator.getPackageString(this.getClass()) + ".ReleaseTestObj");
		cg.addField("static int TEST1 = -1;");
		cg.addField("static String TEST_PROP_NAME1 = \"" + propName + "\";");
		StringAppender sa = StringTool.createStringAppender();
		res.printRes("PropertyManager.Release.Constructor", null, 0, sa);
		cg.addConstructor(sa.toString());
		ClassLoader cl = new ReleaseTestClassLoader(this.getClass().getClassLoader());
		cg.setClassLoader(cl);
		cg.createClass().newInstance();
		arr = (Object[]) propertyMap.get(propName);
		assertEquals(1, arr.length);
		cl = null;
		cg = null;
		System.gc();
		Thread.sleep(2000L);
		Utility.setProperty(propName, "5");
		arr = (Object[]) propertyMap.get(propName);
		assertNull(arr);
	}

	public void testResolveDynamicPropnames()
	{
		String str, exp;

		Utility.setProperty(Utility.SHOW_RDP_FAIL_PROPERTY, "true");
		exp = "1 my_test1 ${no.value} 2;";
		str = "1 ${test.value1} ${no.value} 2;";
		assertEquals(exp, Utility.resolveDynamicPropnames(str));

		exp = "my_test2 1 my_test1 2;\nd my_test1";
		str = "${test.value2} 1 ${test.value1} 2;\nd ${test.value1}";
		assertEquals(exp, Utility.resolveDynamicPropnames(str));

		Map map = new HashMap();
		map.put("test.value3", "t3\t哈哈");

		exp = "my_test2 1 my_test1 :t3\t哈哈: 2;\nd my_test1";
		str = "${test.value2} 1 ${test.value1} :${test.value3}: 2;\nd ${test.value1}";
		assertEquals(exp, Utility.resolveDynamicPropnames(str, map));

		Utility.setProperty(Utility.SHOW_RDP_FAIL_PROPERTY, "false");
		exp = "${test.value2}1 ${test.value1} :t3\t哈哈: 2;\nd ${test.value1}";
		str = "${test.value2}1 ${test.value1} :${test.value3}: 2;\nd ${test.value1}";
		assertEquals(exp, Utility.resolveDynamicPropnames(str, map, true));
	}

	public static void main(String[] args)
			throws Exception
	{
		URL url = UtilityTest.class.getResource("/java/lang/String.class");
		System.out.println(url);
		System.out.println(new URL(url, "test.java"));
		System.out.println(new URL(url, "../test.java"));
		System.out.println(new URL(url, "other/test.java"));
		System.out.println(new URL(url, "./other/test.java"));
		System.out.println(new URL(url, "/other/test.java"));
		System.out.println(new URL(url, "./../../../../../other/test.java"));
		Class tc = Utility[][][][].class;
		System.out.println(tc);
		System.out.println(tc.getComponentType());
		System.out.println(tc.getComponentType().getComponentType());
		System.out.println(tc.getComponentType().getComponentType().getComponentType());
		System.out.println(tc.getComponentType().getComponentType().getComponentType()
				.getComponentType());
		System.out.println(tc.getComponentType().getComponentType().getComponentType()
				.getComponentType().getComponentType());
		/*
		int count = 1;
		long l1, l2;
		String key1 = new String("111111");//.intern();
		String key2 = new String("222222");
		String key3 = new String("333333");
		String value13 = new String("v3");
		String value23 = new String("v3");

		WeakHashMap map1 = new WeakHashMap();
		ReferenceMap map2 = new ReferenceMap(ReferenceMap.WEAK, ReferenceMap.WEAK);

		l1 = System.currentTimeMillis();
		for (int i = 0; i < count; i++)
		{
			map1.put(key1, "v1");
			map1.put(key2, "v2");
			map1.put(key3, value13);
			map1.get("123");
			map1.get("1111112");
			map1.get("222222");
		}
		l2 = System.currentTimeMillis();
		System.out.println("map1:" + (l2 - l1));

		l1 = System.currentTimeMillis();
		for (int i = 0; i < count; i++)
		{
			map2.put(key1, "v1");
			map2.put(key2, "v2");
			map2.put(key3, value23);
			map2.get("123");
			map2.get("1111112");
			map2.get("222222");
		}
		l2 = System.currentTimeMillis();
		System.out.println("map2:" + (l2 - l1));

		System.out.println("map1:" + map1);
		System.out.println("map2:" + map2);
		key1 = null;
		value13 = null;
		value23 = null;
		System.gc();
		System.out.println("map1:" + map1);
		System.out.println("map2:" + map2);
		*/
	}

	public static class ReleaseTestClassLoader extends ClassLoader
	{
		public ReleaseTestClassLoader(ClassLoader parent)
		{
			super(parent);
		}

	}

	public static class Private1
	{
		private int i1;
		private String s1;

		protected int getI()
		{
			return this.i1;
		}

	}

	private static class Private2 extends Private1
	{
		private double d1;

		protected void setD(double num)
		{
			this.d1 = num;
		}

	}

}