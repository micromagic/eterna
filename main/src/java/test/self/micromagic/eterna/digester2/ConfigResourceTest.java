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

package self.micromagic.eterna.digester2;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;
import self.micromagic.eterna.share.FactoryContainer;
import self.micromagic.util.ref.IntegerRef;

public class ConfigResourceTest extends TestCase
{
	public void testDir1()
			throws Exception
	{
		FactoryContainer fc = new FactoryContainerImpl();
		ConfigResource cr = ContainerManager.createResource("classpath:/", fc);
		assertEquals("", cr.getName());
		ConfigResource[] arr = cr.listResources(false);
		boolean hasJavax = false;
		for (int i = 0; i < arr.length; i++)
		{
			if ("classpath:/javax/".equals(arr[i].getConfig()))
			{
				assertEquals("javax", arr[i].getName());
				hasJavax = true;
			}
			System.out.println(arr[i].getConfig() + ", " + arr[i].getURI());
		}
		assertTrue(hasJavax);
		cr = ContainerManager.createResource("cp:/org/dom4j/", fc);
		arr = cr.listResources(false);
		assertEquals(38, arr.length);  // org/dom4j/ 目录下只有38个对象
	}

	public void testURL1()
			throws Exception
	{
		String tmp = "jar:file:/D:/project/eterna2/code/pub/libs/dom4j-1.6.jar!/org/dom4j/dom/DOMAttribute.class";
		FactoryContainer fc = new FactoryContainerImpl();
		ConfigResource cr = ContainerManager.createResource(tmp, fc);
		assertNotNull(cr.getAsStream());
		cr = cr.getResource("../bean/BeanElement.class");
		assertEquals("BeanElement.class", cr.getName());
		assertNotNull(cr.getAsStream());
		cr = cr.getResource("../");
		assertTrue(cr.getURI().endsWith("org/dom4j/"));
		assertEquals("dom4j", cr.getName());
		assertNull(cr.getAsStream());
		assertNull(cr.listResources(false));
	}

	public void testParsePath1()
			throws Exception
	{
		IntegerRef pCount = new IntegerRef();
		String[] pArr;
		pArr = AbstractResource.parsePath("/a", pCount);
		assertEquals(-1, pCount.value);
		assertEquals(1, pArr.length);
		pArr = AbstractResource.parsePath("/a/b//c", pCount);
		assertEquals(-1, pCount.value);
		assertEquals(3, pArr.length);
		pArr = AbstractResource.parsePath("a/b//c", pCount);
		assertEquals(0, pCount.value);
		assertEquals(3, pArr.length);
		pArr = AbstractResource.parsePath("a/b/../c", pCount);
		assertEquals(0, pCount.value);
		assertEquals(2, pArr.length);
		pArr = AbstractResource.parsePath("a/b/../../../../c", pCount);
		assertEquals(2, pCount.value);
		assertEquals(1, pArr.length);
		pArr = AbstractResource.parsePath(".././a/b/../../../../c", pCount);
		assertEquals(3, pCount.value);
		assertEquals(1, pArr.length);
	}

	public void testParsePath2()
			throws Exception
	{
		IntegerRef pCount = new IntegerRef();
		String[] pArr;
		pArr = AbstractResource.parsePath("d:\\p\\x\\f.xml".replace('\\', '/'), pCount);
		assertEquals(0, pCount.value);
		assertEquals("/d:/p/x/f.xml", AbstractResource.mergePath(null, 0, pArr, false));
		pArr = AbstractResource.parsePath("d:\\p\\..\\f.xml".replace('\\', '/'), pCount);
		assertEquals(0, pCount.value);
		assertEquals("/d:/f.xml", AbstractResource.mergePath(null, 0, pArr, false));
		pArr = AbstractResource.parsePath("/n/p/../f.xml".replace('\\', '/'), pCount);
		assertEquals(-1, pCount.value);
		assertEquals("/n/f.xml", AbstractResource.mergePath(null, 0, pArr, false));
	}

	public void testMergePath()
	{
		String[] rootArr = AbstractResource.parsePath("/a/b/c", null);
		IntegerRef pCount = new IntegerRef();
		String[] pArr;
		String path;
		pArr = AbstractResource.parsePath("d/x", pCount);
		path = AbstractResource.mergePath(rootArr, pCount.value, pArr, false);
		assertEquals("/a/b/c/d/x", path);
		pArr = AbstractResource.parsePath("d/../../x", pCount);
		path = AbstractResource.mergePath(rootArr, pCount.value, pArr, false);
		assertEquals("/a/b/x", path);
		path = AbstractResource.mergePath(rootArr, pCount.value, pArr, true);
		assertEquals("/a/b/x/", path);
	}

	public void testTrimBeginSplit()
	{
		assertEquals("a/b/x", AbstractResource.trimBeginSplit("/a/b/x"));
		assertEquals("a/b/x", AbstractResource.trimBeginSplit("//a/b/x"));
		assertEquals("a/b/x", AbstractResource.trimBeginSplit("a/b/x"));
		assertEquals("  a/b/x", AbstractResource.trimBeginSplit("  a/b/x"));
		assertEquals("\\a/b/x", AbstractResource.trimBeginSplit("\\a/b/x"));
		assertEquals("", AbstractResource.trimBeginSplit("/"));
	}

	public void testCreateResource1()
	{
		System.out.println("--testCreateResource1----------------------------------");
		ConfigResource cr;
		cr = ContainerManager.createResource("../../src/java/.project");
		assertEquals(ConfigResource.RES_TYPE_DIR, cr.getResource("../").getType());
		assertEquals(ConfigResource.RES_TYPE_DIR, cr.getResource("./").getType());
		assertEquals("../../src/java/.project", cr.getConfig());
		assertEquals(".project", cr.getName());
		System.out.println(cr.getURI());
		assertEquals("../../src/java/./.classpath", cr.getResource("./.classpath").getConfig());
		System.out.println(cr.getResource("./.classpath").getURI());
		cr = ContainerManager.createResource("../../src/");
		assertEquals("src", cr.getName());
		assertEquals("../../src/java/main", cr.getResource("java/main").getConfig());
		System.out.println(cr.getResource("java/test/eterna.config").getURI());
		cr = ContainerManager.createResource(".classpath");
		cr = cr.getResource("test/eterna.config");
		assertEquals("test/eterna.config", cr.getConfig());
		assertEquals("test/tool/PrivateAccessor.java", cr.getResource("tool/PrivateAccessor.java").getConfig());
		System.out.println(cr.getResource("tool/PrivateAccessor.java").getURI());
		cr = ContainerManager.createResource("test/");
		assertEquals("test/tool/PrivateAccessor.java", cr.getResource("tool/PrivateAccessor.java").getConfig());
	}

	public void testCreateResource2()
	{
		System.out.println("--testCreateResource2----------------------------------");
		FactoryContainer fc = new FactoryContainerImpl();
		ConfigResource cr;
		cr = ContainerManager.createResource("cp:eterna.config", fc);
		assertEquals("cp:eterna.config", cr.getConfig());
		System.out.println(cr.getURI());
		cr = cr.getResource("tool/PrivateAccessor.class");
		assertEquals("cp:/tool/PrivateAccessor.class", cr.getConfig());
		System.out.println(cr.getURI());
		assertEquals("cp:/tool/tmp.res", cr.getResource("tmp.res").getConfig());
		cr = ContainerManager.createResource("classpath:/tool/", fc);
		assertEquals("classpath:/tool/", cr.getConfig());
		assertEquals("classpath:/tool/tmp.res", cr.getResource("tmp.res").getConfig());
		assertEquals("classpath:/", cr.getResource("../").getConfig());
		assertEquals(ConfigResource.RES_TYPE_DIR, cr.getResource("../").getType());
		assertEquals("classpath:/eterna.config", cr.getResource("../eterna.config").getConfig());
		cr = cr.getResource("tool/test/tmp.txt");
		assertEquals("classpath:/tool/tool/test/tmp.txt", cr.getConfig());
		assertEquals(ConfigResource.RES_TYPE_DIR, cr.getResource("../").getType());
		assertEquals("classpath:/tool/tool/", cr.getResource("../").getConfig());
		assertEquals(ConfigResource.RES_TYPE_DIR, cr.getResource("./").getType());
		assertEquals("classpath:/tool/tool/test/", cr.getResource("./").getConfig());
	}

	public void testCreateResource5()
	{
		System.out.println("--testCreateResource5----------------------------------");
		ConfigResource cr;
		String tmpPath;
		FactoryContainer fc = new FactoryContainerImpl();

		cr = ContainerManager.createResource("cp:/eterna.config", fc);
		assertEquals("eterna.config", cr.getName());
		assertEquals(ClassPathResource.class, cr.getClass());
		System.out.println(cr.getURI());
		cr = ContainerManager.createResource("classpath:eterna.config", fc);
		assertEquals(ClassPathResource.class, cr.getClass());
		System.out.println(tmpPath = cr.getURI());

		cr = ContainerManager.createResource(tmpPath, fc);
		assertEquals(FileResource.class, cr.getClass());
		System.out.println(cr.getURI());
		cr = cr.getResource("rules_ext.res");
		System.out.println(cr.getURI());
		System.out.println(cr.getConfig());
		System.out.println(cr.getResource("/test_error00.log").getURI());
		System.out.println(cr.getResource("/test_error00.log").getConfig());
		System.out.println(cr.getResource("../../.classpath").getURI());
		System.out.println(cr.getResource("../../.classpath").getConfig());
		cr = ContainerManager.createResource("D:\\test_error00.log", fc);
		assertEquals("test_error00.log", cr.getName());
		assertEquals(FileResource.class, cr.getClass());
		System.out.println(cr.getURI());
		assertEquals("", cr.getResource("/").getName());
	}

	public void xtestFilePath()
			throws Exception
	{
		System.out.println(new File(new URL("file:///d:/project").getFile()).isDirectory());
		System.out.println(new URL("file:///d:/project").getFile());
		System.out.println(new URL("FiLe:///d:/project").getProtocol());
		System.out.println(new File("").getAbsolutePath());
		System.out.println(new File("\\c:\\a\\..\\b\\").getAbsolutePath());
		System.out.println(new File("/a/../b/x/x").getParentFile().getAbsolutePath());
		System.out.println(new File("/a/../b/x/x").getParentFile().getAbsoluteFile()
				.getParentFile());
		System.out.println(new File("../b/x/x").getParentFile().getAbsoluteFile()
				.getParentFile().getParentFile());
		System.out.println(new File("/a/../b/x/x").getParentFile().getAbsoluteFile()
				.getParentFile().getParentFile().getParentFile());
	}

}