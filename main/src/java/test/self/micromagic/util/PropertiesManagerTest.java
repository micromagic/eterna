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

import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;
import self.micromagic.eterna.digester2.ContainerManager;
import tool.PrivateAccessor;

public class PropertiesManagerTest extends TestCase
{
	public void testPropteriesValue()
	{
		System.out.println(50 * 60 * 100);
		int mask = (1 << 18) - 1;
		System.out.println(((long) mask));
		System.out.println(Long.toHexString(System.currentTimeMillis() & ~( mask)));

		PropertiesManager pm = new PropertiesManager("conf/main1.txt", this.getClass().getClassLoader());
		assertEquals("z", pm.getProperty("p1"));
		assertEquals("y", pm.getProperty("p2"));
		assertEquals("sub", pm.getProperty("p3"));
		assertEquals("d", pm.getProperty("p4"));
		assertEquals("e", pm.getProperty("p5"));
		assertEquals("f", pm.getProperty("p6"));
		assertEquals("g", pm.getProperty("p7"));
		assertEquals("h", pm.getProperty("p8"));

		pm.contains("flag", true);
		assertEquals("", pm.getProperty("flag"));
		System.out.println(pm);
	}

	public void testPreRead()
			throws Exception
	{
		PropertiesManager pm = new PropertiesManager(
				ContainerManager.createResource("cp:/conf/preRead.txt"), null, false);
		nowPM = pm;
		pm.addMethodPropertyManager("baseName", this.getClass(), "setBaseName");
		pm.reload();
		assertEquals("test_abc", pm.getResolvedProperty("testName"));
		pm = new PropertiesManager(ContainerManager.createResource("cp:/conf/preRead.txt"), null, false);
		nowPM = pm;
		pm.addMethodPropertyManager("baseName", this.getClass(), "setBaseName");
		System.out.println("When pre read, get other property is null.");
		pm.reload(true, null, new String[]{"baseName"});
		System.out.println("StringAppender:" + StringTool.createStringAppender().getClass());
		nowPM = null;
	}

	public void testDynamicRes()
	{
		PropertiesManager pm = new PropertiesManager(
				ContainerManager.createResource("cp:/conf/dResMain.txt"), null, false);
		pm.reload();
		assertEquals("1", pm.getProperty("testValue"));
	}

	public void testBindDefaultValue()
			throws Exception
	{
		PropertiesManager pm = new PropertiesManager("conf/main1.txt", this.getClass().getClassLoader());
		pm.addFieldPropertyManager("notExists", PropertiesManagerTest.class, "t_prop_bindDefault", "001");
		assertEquals("001", t_prop_bindDefault);
		pm.setProperty("notExists", "002");
		assertEquals("002", t_prop_bindDefault);
		pm.setProperty("other", "003");
		pm.reload();
		assertEquals("001", t_prop_bindDefault);
		assertNull(pm.getProperty("other"));
	}
	public static String t_prop_bindDefault;

	public void testReload()
			throws Exception
	{
		PropertiesManager pm = new PropertiesManager("conf/main2.txt", this.getClass().getClassLoader());
		System.out.println(pm);
		pm.addFieldPropertyManager("p5", PropertiesManagerTest.class, "t_prop_reload");
		assertEquals("temp", t_prop_reload);
		pm.setProperty("p5", "2");
		assertEquals("2", t_prop_reload);
		pm.setProperty("_child.properties", "cp:/conf/parent1.txt");
		pm.reload();
		assertEquals("none", t_prop_reload);
		System.out.println(pm);
	}
	public static String t_prop_reload;

	public void testSystemDefault()
			throws Exception
	{
		System.setProperty("test.001", "a");
		PropertiesManager pm1 = new PropertiesManager("conf/main1.txt", this.getClass().getClassLoader());
		assertEquals("ab", pm1.resolveDynamicPropnames("${test.001}b"));
		PropertiesManager pm2 = new PropertiesManager("conf/main2.txt", this.getClass().getClassLoader());
		assertEquals("${test.001}btest", pm2.resolveDynamicPropnames("${test.001}b${p1}"));

		pm1.addFieldPropertyManager("test.001", PropertiesManagerTest.class, "t_prop_sd");
		assertEquals("a", t_prop_sd);
		System.setProperty("test.001", "c");
		assertEquals("a", t_prop_sd);
		pm1.setProperty("test.001", "d");
		assertEquals("d", t_prop_sd);

		Properties d = new Properties();
		d.setProperty("test.001", "x");
		PropertiesManager.setDefaultProperties(d);
		pm1 = new PropertiesManager("conf/main1.txt", this.getClass().getClassLoader());
		assertEquals("xb", pm1.resolveDynamicPropnames("${test.001}b"));
		pm2 = new PropertiesManager("conf/main2.txt", this.getClass().getClassLoader());
		assertEquals("${test.001}btest", pm2.resolveDynamicPropnames("${test.001}b${p1}"));
	}
	public static String t_prop_sd;

	public void testParent()
			throws Exception
	{
		PropertiesManager pm1 = new PropertiesManager("conf/main1.txt", this.getClass().getClassLoader());
		PropertiesManager pm2 = new PropertiesManager("conf/main2.txt", this.getClass().getClassLoader(), pm1);
		pm1.setProperty("p.test", "a");
		pm2.addFieldPropertyManager("p.test", PropertiesManagerTest.class, "t_prop_parent");
		assertEquals("a", t_prop_parent);
		pm1.setProperty("p.test", "b");
		assertEquals("b", t_prop_parent);
		pm2.setProperty("p.test", "c");
		assertEquals("c", t_prop_parent);
		// 由于pm2中已有值, 所以父配置中的修改不会影响当前值
		pm1.setProperty("p.test", "d");
		assertEquals("c", t_prop_parent);
		pm2.removeProperty("p.test");
		assertEquals("d", t_prop_parent);
		pm1.removeProperty("p.test");
		assertNull(t_prop_parent);

		t_prop_parent_setted = false;
		pm2.addMethodPropertyManager("p.test", PropertiesManagerTest.class, "setPropParent");
		pm1.setProperty("p.test", "a");
		assertTrue(t_prop_parent_setted);
		// 由于父配置中已有a值, 子配置中再设置a值, 这时值无变化,
		// 不会触发setPropParent方法
		t_prop_parent_setted = false;
		pm2.setProperty("p.test", "a");
		assertFalse(t_prop_parent_setted);

		List plList = (List) PrivateAccessor.get(pm1, "plList");
		assertEquals(2, plList.size());
		pm2 = null;
		System.gc();
		Thread.sleep(500);
		System.gc();
		// 当子配置释放后, 再次触发父配置的事件后, 会将注册的事件删除
		pm1.setProperty("p.test", "b");
		assertEquals(1, plList.size());
	}

	public static String t_prop_parent;
	public static boolean t_prop_parent_setted;
	public static void setPropParent(String v)
	{
		t_prop_parent_setted = true;
	}

	static PropertiesManager nowPM;
	static void setBaseName(String name)
	{
		System.out.println("preReadBaseNameTest:" + nowPM.getResolvedProperty("testName"));
	}

}