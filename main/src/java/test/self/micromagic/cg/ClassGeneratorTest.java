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

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import junit.framework.TestCase;
import self.micromagic.eterna.share.FactoryContainer;
import self.micromagic.eterna.share.InitializeListener;
import self.micromagic.eterna.view.DataPrinter;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;
import self.micromagic.util.UtilityTest;
import self.micromagic.util.container.SynHashMap;
import tool.PrivateAccessor;

public class ClassGeneratorTest extends TestCase
{
	public void testClassKeyCache1()
			throws Exception
	{
		ClassKeyCache cache = ClassKeyCache.getInstance();
		ClassLoader thisLoader = this.getClass().getClassLoader();
		ClassLoader loader = new UtilityTest.ReleaseTestClassLoader(thisLoader);
		WeakReference ref = checkRelease(cache, loader);
		Object obj = PrivateAccessor.invoke(cache, "getCacheCell", new Object[]{loader});
		assertTrue(obj.getClass().getName().endsWith("CacheCellImpl1"));
		loader = null;
		int index = 0;
		while (ref.get() != null)
		{
			System.out.println("t1:" + index++);
			System.gc();
			Thread.sleep(1200L);
		}
		assertEquals(0, cache.size());
		System.out.println("--- end1 -----------");

		loader = new TestCachedClassLoader(thisLoader);
		ref = checkRelease(cache, loader);
		obj = PrivateAccessor.invoke(cache, "getCacheCell", new Object[]{loader});
		assertTrue(obj.getClass().getName().endsWith("CacheCellImpl2"));
		loader = null;
		index = 0;
		while (ref.get() != null)
		{
			System.out.println("t2:" + index++);
			System.gc();
			Thread.sleep(1200L);
		}
		assertEquals(0, cache.size());
		System.out.println("--- end2 -----------");
	}

	private WeakReference checkRelease(ClassKeyCache cache, ClassLoader loader)
			throws Exception
	{
		ClassGenerator cg = getClassGenerator("cg.test.KeyAnt");
		cg.setCompileType(CG.COMPILE_TYPE_ANT);
		cg.setClassLoader(loader);
		Class key1 = cg.createClass();
		cache.setProperty(key1, key1.newInstance());
		assertEquals(1, cache.size());
		return new WeakReference(key1);
	}

	public void testClassKeyCache2()
			throws Exception
	{
		ClassKeyCache ckc = ClassKeyCache.getInstance();
		ClassGenerator cg;

		cg = getClassGenerator("cg.test.KeyAnt");
		cg.setCompileType(CG.COMPILE_TYPE_ANT);
		ClassLoader cl1 = new UtilityTest.ReleaseTestClassLoader(this.getClass().getClassLoader());
		cg.setClassLoader(cl1);
		Class key1 = cg.createClass();
		ckc.setProperty(key1, key1.newInstance());
		assertEquals(1, ckc.size());

		cg = getClassGenerator("cg.test.KeyJavassist");
		cg.setCompileType(CG.COMPILE_TYPE_JAVASSIST);
		ClassLoader cl2 = new UtilityTest.ReleaseTestClassLoader(this.getClass().getClassLoader());
		cg.setClassLoader(cl2);
		Class key2 = cg.createClass();
		ckc.setProperty(key2, key2.newInstance());
		assertEquals(2, ckc.size());

		cg = getClassGenerator("cg.test.KeyBean");
		cg.setCompileType(CG.COMPILE_TYPE_JAVASSIST);
		ClassLoader cl3 = new UtilityTest.ReleaseTestClassLoader(this.getClass().getClassLoader());
		cg.setClassLoader(cl3);
		Class key3 = cg.createClass();
		DataPrinter.BeanPrinter bp = (DataPrinter.BeanPrinter) PrivateAccessor.create(
				Class.forName("self.micromagic.eterna.view.impl.DataPrinterImpl$BeanPrinterImpl"),
				new Class[]{key3});
		ckc.setProperty(key3, bp);
		assertEquals(3, ckc.size());

		ClassKeyCache ckc2 = ClassKeyCache.getInstance();
		ckc2.setProperty(key3, "key3 test");
		assertEquals(1, ckc2.size());

		cg = null;
		key1 = null;
		cl1 = null;
		System.gc();
		Thread.sleep(1000L);
		assertEquals(2, ckc.size());

		key2 = null;
		cl2 = null;
		System.gc();
		Thread.sleep(1000L);
		assertEquals(1, ckc.size());

		key3 = null;
		cl3 = null;
		bp = null;
		System.gc();
		Thread.sleep(1000L);
		Map softM = (Map) PrivateAccessor.get(BeanMethodInfo.class, "beanMethodsCache");
		if (softM.size() > 0)
		{
			// 如果还没释放, 则直接清掉
			softM.clear();
			System.gc();
			Thread.sleep(2000L);
		}
		assertEquals(0, ckc2.size());
		assertEquals(0, ckc.size());
	}

	public void notestOther2()
			throws Exception
	{
		int count = 100000;
		long l1, l2;
		Class key1 = String.class;
		Class key2 = this.getClass();
		Class key3 = TestCase.class;
		String value13 = new String("v3");
		String value23 = new String("v3");

		WeakHashMap map1 = new WeakHashMap();
		HashMap map2 = new HashMap();
		ClassKeyCache map3 = ClassKeyCache.getInstance();

		l1 = System.currentTimeMillis();
		for (int i = 0; i < count; i++)
		{
			map1.put(key1, "v1");
			map1.put(key2, "v2");
			map1.put(key3, value13);
			map1.get(key1);
			map1.get(key2);
			map1.get(Utility.class);
		}
		l2 = System.currentTimeMillis();
		System.out.println("map1:" + (l2 - l1));

		l1 = System.currentTimeMillis();
		for (int i = 0; i < count; i++)
		{
			map2.put(key1, "v1");
			map2.put(key2, "v2");
			map2.put(key3, value23);
			map2.get(key1);
			map2.get(key2);
			map2.get(Utility.class);
		}
		l2 = System.currentTimeMillis();
		System.out.println("map2:" + (l2 - l1));

		map3.setProperty(key1, "v1");
		map3.setProperty(key2, "v2");
		map3.setProperty(key3, value23);
		l1 = System.currentTimeMillis();
		for (int i = 0; i < count; i++)
		{
			map3.setProperty(key1, "v1");
			map3.setProperty(key2, "v2");
			map3.setProperty(key3, value23);
			map3.getProperty(key1);
			map3.getProperty(key2);
			map3.getProperty(Utility.class);
		}
		l2 = System.currentTimeMillis();
		System.out.println("map3:" + (l2 - l1));
	}

	public void notestOther1()
			throws Exception
	{
		long l1, l2;
		int count = 1000000;
		Class c = Other.class;
		Object obj;
		Other o = new Other();
		Field f = c.getField("cache");

		l1 = System.currentTimeMillis();
		for (int i = 0; i < count; i++)
		{
			c.newInstance();
			//o.t2();
			//f = c.getField("cache");
			//obj = f.get(null);
		}
		l2 = System.currentTimeMillis();
		System.out.println("1:" + (l2 - l1));

		l1 = System.currentTimeMillis();
		for (int i = 0; i < count * 10; i++)
		{
			new Other();
			//o.t1();
			//obj = Other.cache;
		}
		l2 = System.currentTimeMillis();
		System.out.println("2:" + (l2 - l1));
	}

	public void testJavassistCreate()
			throws Exception
	{
		ClassGenerator cg = getClassGenerator("cg.test.JavassistTest");
		cg.setCompileType(CG.COMPILE_TYPE_JAVASSIST);
		this.checkClass("cg.test.JavassistTest", cg.createClass());
	}

	public void testAntCreate()
	{
		ClassGenerator cg = getClassGenerator("cg.test.AntTest");
		cg.setCompileType(CG.COMPILE_TYPE_ANT);
		this.checkClass("cg.test.AntTest", cg.createClass());
	}

	public void testCreateClass()
			throws Exception
	{
		//Utility.setProperty(CG.COMPILE_LOG_PROPERTY, "2");
		ClassGenerator cg = new ClassGenerator();
		cg.setClassName("test.Show");
		cg.addInterface(Show.class);
		StringAppender sa = StringTool.createStringAppender();
		sa.append("public String getShow()").appendln()
				.append('{').appendln()
				.append("int count = 10;").appendln()
				//.append("System.out.println(").append(Other.class.getName())
				.append("System.out.println(").append(ClassGenerator.getClassName(Other.class))
				.append(".class);").appendln()
				.append("String result = \"\";").appendln()
				.append("for (int i = 0; i < count; i++)").appendln()
				.append('{').appendln()
				.append("ResultRow row = null;").appendln()
				.append("result += i + \":\" + row + \";\";").appendln()
				.append('}').appendln()
				.append("return result;").appendln()
				.append('}').appendln();
		cg.addMethod(sa.toString());
		cg.importPackage("self.micromagic.eterna.dao");
		cg.addClassPath(Show.class);
		Show s = (Show) cg.createClass().newInstance();
		String exp = "0:null;1:null;2:null;3:null;4:null;5:null;6:null;7:null;8:null;9:null;";
		assertEquals(exp, s.getShow());
	}

	static Map getFields()
	{
		Map map = new HashMap();
		map.put("pValue", "public long pValue;");
		map.put("intValue", "private int intValue;");
		map.put("stringValue", "private String stringValue;");
		map.put("thisValue", "private static ${thisName} thisValue = new ${thisName}();");
		return map;
	}

	static Map getMethods()
	{
		Map map = new HashMap();
		map.put("getIntValue", "public int getIntValue()\n{\nreturn this.intValue;\n}");
		map.put("setIntValue", "public void setIntValue(int v)\n{\nthis.intValue = v;\n}");
		map.put("getStringValue", "private String getStringValue()\n{\nreturn this.stringValue;\n}");
		map.put("createThis", "public static ${thisName} createThis(String name)\n{\nreturn new ${thisName}(name);\n}");
		map.put("afterInitialize", "public void afterInitialize(FactoryContainer factoryContainer) {}");
		return map;
	}

	static Set getInterfaces()
	{
		Set set = new HashSet();
		set.add(Cloneable.class);
		set.add(Serializable.class);
		set.add(InitializeListener.class);
		return set;
	}

	private void checkClass(String className, Class c)
	{
		assertEquals(className, ClassGenerator.getClassName(c));
		assertEquals(ClassGeneratorTest.class, c.getSuperclass());
		Set set;
		Class[] interfaces = c.getInterfaces();
		set = getInterfaces();
		assertEquals(set.size(), interfaces.length);
		for (int i = 0; i < interfaces.length; i++)
		{
			assertTrue(set.contains(interfaces[i]));
		}
		Field[] fields = c.getDeclaredFields();
		set = getFields().keySet();
		assertEquals(set.size(), fields.length);
		for (int i = 0; i < fields.length; i++)
		{
			assertTrue(set.contains(fields[i].getName()));
		}
		Method[] methods = c.getDeclaredMethods();
		set = getMethods().keySet();
		assertEquals(set.size(), methods.length);
		for (int i = 0; i < methods.length; i++)
		{
			assertTrue(set.contains(methods[i].getName()));
		}
		Constructor[] constructors = c.getDeclaredConstructors();
		assertEquals(2, constructors.length);
		for (int i = 0; i < constructors.length; i++)
		{
			Constructor constructor = constructors[i];
			if (constructor.getParameterTypes().length != 0)
			{
				assertEquals(String.class, constructor.getParameterTypes()[0]);
			}
		}
	}

	public void testJavassistCrossMethod()
			throws Exception
	{
		ClassGenerator cg = new ClassGenerator();
		//cg.setCompileType("ant");
		cg.setSuperClass(TestBean.class);
		cg.setClassName("cg.test.T");
		cg.addField("String v = \"\";");
		cg.addConstructor("public " + CG.THIS_NAME + "(){v = \"value|\";}");
		cg.addMethod("public void b(int i){if (i > 0) {a(i - 1);} v += \"b.\" + i + \", \";}");
		cg.addMethod("public void a(int i){if (i > 0) {b(i - 1);} v += \"a.\" + i + \", \";}");
		cg.addMethod("public String toString(){a(5);return this.getClass().toString() + \":\" + v;}");
		Class c = cg.createClass();
		System.out.println(c.newInstance());
	}

	static ClassGenerator getClassGenerator(String className)
	{
		ClassGenerator cg = new ClassGenerator();
		cg.setClassName(className);
		cg.importPackage(ClassGenerator.getPackageString(FactoryContainer.class));
		cg.setSuperClass(ClassGeneratorTest.class);
		cg.addConstructor("public ${thisName}(String name)\n{\nthis.stringValue = name;\n}");
		cg.addConstructor("public ${thisName}()\n{\n}");
		Iterator itr;
		itr = getInterfaces().iterator();
		while (itr.hasNext())
		{
			cg.addInterface((Class) itr.next());
		}
		itr = getFields().values().iterator();
		while (itr.hasNext())
		{
			cg.addField((String) itr.next());
		}
		itr = getMethods().values().iterator();
		while (itr.hasNext())
		{
			cg.addMethod((String) itr.next());
		}
		return cg;
	}

	public interface Show
	{
		public String getShow();
	}

	public static class Other
			implements Show
	{
		public static Map cache = new HashMap();

		public String getShow()
		{
			return "other";
		}

		public void t1()
		{

		}

		public synchronized void t2()
		{

		}

	}

	public static class TestCachedClassLoader extends ClassLoader
			implements CachedClassLoader
	{
		private final Map caches = new SynHashMap(3, SynHashMap.WEAK);

		public TestCachedClassLoader(ClassLoader parent)
		{
			super(parent);
		}

		public void addCache(Object key, Object cache)
		{
			this.caches.put(key, cache);
		}

	}

}