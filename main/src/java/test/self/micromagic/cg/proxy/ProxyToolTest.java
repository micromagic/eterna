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

package self.micromagic.cg.proxy;

import java.lang.reflect.Method;

import junit.framework.TestCase;

public class ProxyToolTest extends TestCase
{
	public void testErrors()
			throws Exception
	{
		Method m1 = Test01.class.getMethod("test01", new Class[0]);
		Method m2 = Test02.class.getDeclaredMethod("test02", new Class[0]);
		Method m3 = Test02.class.getMethod("test03", new Class[]{Test01.class});
		Method m4 = ClassLoader.class.getDeclaredMethod("defineClass", new Class[]{String.class, byte[].class, int.class, int.class});
		Method m5 = Class.forName("java.util.ResourceBundleEnumeration").getDeclaredMethod(
				"nextElement", new Class[]{});
		try
		{
			ProxyTool.createMethodProxy(m1);
			fail();
		}
		catch (Exception ex)
		{
			System.out.println(ex);
		}
		try
		{
			ProxyTool.createMethodProxy(m2);
			fail();
		}
		catch (Exception ex)
		{
			System.out.println(ex);
		}
		try
		{
			ProxyTool.createMethodProxy(m3);
			fail();
		}
		catch (Exception ex)
		{
			System.out.println(ex);
		}
		try
		{
			ProxyTool.createMethodProxy(m4);
			fail();
		}
		catch (Exception ex)
		{
			System.out.println(ex);
		}
		try
		{
			ProxyTool.createMethodProxy(m5);
			fail();
		}
		catch (Exception ex)
		{
			System.out.println(ex);
		}
	}

	public void testCheck()
			throws Exception
	{
		//self.micromagic.util.Utility.setProperty(self.micromagic.cg.CG.COMPILE_TYPE_PROPERTY, "ant");
		//self.micromagic.util.Utility.setProperty(self.micromagic.cg.AntCG.ANT_TOOL_UID_PATH_PROPERTY, "true");
		Method m1 = this.getClass().getDeclaredMethod("doOut1", new Class[]{String.class, int.class});
		MethodProxy p1 = ProxyTool.createMethodProxy(m1, true);
		MethodProxy p2 = ProxyTool.createMethodProxy(m1, false);
		assertNotSame(p1, p2);
		try
		{
			p1.invoke(null, null);
			fail();
		}
		catch (Throwable ex)
		{
			System.out.println(ex);
		}
		try
		{
			p1.invoke(null, new Object[]{"s", "2", "3"});
			fail();
		}
		catch (Throwable ex)
		{
			System.out.println(ex);
		}
		try
		{
			p1.invoke(null, new Object[]{"s", null});
			fail();
		}
		catch (Throwable ex)
		{
			System.out.println(ex);
		}
		try
		{
			p1.invoke(null, new Object[]{"s", new Double(1.0)});
			fail();
		}
		catch (Throwable ex)
		{
			System.out.println("无法将double转int:" + ex);
		}
		try
		{
			p1.invoke(null, new Object[]{"s", new Integer(1)});
		}
		catch (Throwable ex)
		{
			fail();
		}
		try
		{
			p2.invoke(null, new Object[]{"s", "1"});
			fail();
		}
		catch (Throwable ex)
		{
			System.out.println(ex);
		}
		try
		{
			p2.invoke(null, new Object[]{"s", new Character('1')});
			fail();
		}
		catch (Throwable ex)
		{
			System.out.println("没有检查类型不能设置类型不同的参数:" + ex);
		}
		try
		{
			p1.invoke(null, new Object[]{"s", new Character('1')});
		}
		catch (Throwable ex)
		{
			fail();
		}

		Method m2 = this.getClass().getDeclaredMethod("doOut2", new Class[]{});
		MethodProxy p3 = ProxyTool.createMethodProxy(m2, true);
		try
		{
			p3.invoke(null, null);
			fail();
		}
		catch (Throwable ex)
		{
			System.out.println(ex);
		}
		try
		{
			p3.invoke(new Object(), null);
			fail();
		}
		catch (Throwable ex)
		{
			System.out.println(ex);
		}
		try
		{
			System.out.println(p3.invoke(this, null));
		}
		catch (Throwable ex)
		{
			fail();
		}
	}

	public void testInJava()
			throws Exception
	{
		try
		{
			Method m = this.getClass().getMethod("toString", new Class[]{});
			MethodProxy p = ProxyTool.createMethodProxy(m, true);
			System.out.println(p.invoke(this, null));
		}
		catch (Throwable ex)
		{
			fail();
		}
		try
		{
			Method m = Object.class.getMethod("toString", new Class[]{});
			MethodProxy p = ProxyTool.createMethodProxy(m, true);
			System.out.println(p.invoke(this, null));
		}
		catch (Throwable ex)
		{
			fail();
		}
	}

	private static class Test01
	{
		public void test01() {}

	}

	public static class Test02
	{
		private void test02() {}

		public void test03(Test01 t)
		{
			test02();
		}

	}

	static void doOut1(String s, int i)
	{
		System.out.println("doOut1" + i + s);
	}

	int doOut2()
	{
		System.out.println("doOut2");
		return 1;
	}

	/*
		// 测试不同分支的ClassLoader
		URL[] urls = new URL[]{new File("D:\\project\\eterna\\tmp\\tmp_classes").toURL()};
		ClassLoader cl = new URLClassLoader(urls, this.getClass().getClassLoader());
		ClassGenerator cg = new ClassGenerator();
		//cg.setClassLoader(this.getClass().getClassLoader());
		cg.setClassLoader(cl);
		cg.setClassName("cg.test.T_Obj");
		Class parentClass = cl.loadClass("s.TestObject");
		cg.setSuperClass(parentClass);
		cg.addClassPath(parentClass);
		Object obj = cg.createClass().newInstance();
		System.out.println(obj);

	public void testInvoke()
			throws Exception
	{
		Class c = this.getClass();
		java.lang.reflect.Method m = c.getMethod("doT", new Class[]{String.class, int.class, Double.class});
		m.invoke(this, new Object[]{"1", new Integer(1), null});
		java.lang.reflect.Field f = c.getField("iii");
		f.getFloat(this);

		self.micromagic.cg.proxy.MethodProxy proxy = self.micromagic.cg.proxy.ProxyTool.createMethodProxy(
				c.getMethod("toString", new Class[0]));
		System.out.println(proxy.invoke(this, null) + ":" + proxy);
	}

	public boolean b = true;
	public int iii = 123;
	public void doT(String str, int i, Double d) {}
	*/
}