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

package self.micromagic.eterna.share;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import self.micromagic.eterna.digester2.ContainerManager;
import self.micromagic.eterna.share.sub.TestClass;
import self.micromagic.util.converter.BooleanConverter;
import self.micromagic.util.converter.ByteConverter;
import self.micromagic.util.converter.DoubleConverter;
import self.micromagic.util.converter.LongConverter;
import self.micromagic.util.converter.UtilDateConverter;
import tool.PrivateAccessor;

public class ToolTest extends TestCase
{
	public void testInvoke()
			throws Exception
	{
		Object obj = new TestClass.TestClass3();
		Object r = Tool.invokeExactMethod(obj, "getValue", new Object[]{"0000"});
		System.out.println(r);
		obj = new TestClass.TestClass4();
		r = Tool.invokeExactMethod(obj, "getValue", new Object[]{"11111"});
		System.out.println(r);
	}

	public void testGetCheckEmptyAttrs()
	{
		Set checkSet = new HashSet();
		checkSet.add("a");
		checkSet.add("b");

		Set result, tmp;
		EternaFactory share = makeEmptyFactory(null);
		share.setAttribute(Tool.ATTR_CHECK_EMPTY_TAG, "a,b");
		result = Tool.getCheckEmptyAttrs(share);
		assertEquals(checkSet, result);

		EternaFactory sub = makeEmptyFactory(share.getFactoryContainer());
		tmp = Tool.getCheckEmptyAttrs(sub);
		assertTrue(result == tmp);
		sub.setAttribute(Tool.ATTR_CHECK_EMPTY_TAG, "c;b");
		result = Tool.getCheckEmptyAttrs(sub);
		checkSet.add("c");
		assertEquals(checkSet, result);

		sub.setAttribute(Tool.ATTR_CHECK_EMPTY_TAG, "c;b=0");
		result = Tool.getCheckEmptyAttrs(sub);
		checkSet.remove("b");
		assertEquals(checkSet, result);
	}

	public void testMakeAllAttrTypeDefMap()
			throws Exception
	{
		System.out.println(Long.toHexString(System.currentTimeMillis()));
		System.out.println(new java.util.Date(0xfffffffffffL));
		String defStr = "t1/a=String;t2/a=int;t1/b=long;c=double";
		Map share = makeShareMap();
		Map result = (Map) PrivateAccessor.invoke(
				Tool.class, "makeAllAttrTypeDefMap", new Object[]{defStr, share});
		assertNotNull(result.get("t2"));
		assertNull(share.get("t2"));
		assertTrue(result.get("t1") != share.get("t1"));
		assertTrue(result.get("t3") == share.get("t3"));
	}

	public void testGetCaptionTranslateMap()
	{
		Map result, tmp;
		EternaFactory share = makeEmptyFactory(null);
		share.setAttribute(Tool.CAPTION_TRANSLATE_TAG, "a=1;b=2");
		result = Tool.getCaptionTranslateMap(share);
		assertEquals(2, result.size());
		assertEquals("1", result.get("a"));
		assertEquals("2", result.get("b"));

		EternaFactory sub = makeEmptyFactory(share.getFactoryContainer());
		tmp = Tool.getCaptionTranslateMap(sub);
		assertTrue(result == tmp);
		sub.setAttribute(Tool.CAPTION_TRANSLATE_TAG, "c=3");
		result = Tool.getCaptionTranslateMap(sub);
		assertEquals(3, result.size());
		assertEquals("1", result.get("a"));
		assertEquals("2", result.get("b"));
		assertEquals("3", result.get("c"));

		share = makeEmptyFactory(null);
		share.setAttribute(Tool.CAPTION_TRANSLATE_TAG, "a=1;b=2");
		sub = makeEmptyFactory(share.getFactoryContainer());
		tmp = Tool.getCaptionTranslateMap(sub);
		result = Tool.getCaptionTranslateMap(share);
		assertTrue(result == tmp);

		assertEquals("2", Tool.translateCaption(sub, "b"));
		assertNull(Tool.translateCaption(sub, "c"));
		sub.setAttribute(Tool.CAPTION_TRANSLATE_TAG, "c=3;b=new");
		assertEquals("3", Tool.translateCaption(sub, "c"));
		assertEquals("new", Tool.translateCaption(sub, "b"));
	}

	public void testGetAllAttrTypeDefMap0()
			throws Exception
	{
		Map result, tmp;
		EternaFactory share = makeEmptyFactory(null);
		share.setAttribute(Tool.ATTR_TYPE_DEF_FLAG, "a=String;b=int");
		result = (Map) PrivateAccessor.invoke(
				Tool.class, "getAllAttrTypeDefMap0", new Object[]{share});
		assertEquals(1, result.size());
		tmp = (Map) result.get(Tool.ALL_OBJ_TYPE);
		assertEquals(2, tmp.size());
		assertTrue(tmp.containsKey("a"));
		assertTrue(tmp.containsKey("b"));

		EternaFactory sub = makeEmptyFactory(share.getFactoryContainer());
		tmp = (Map) PrivateAccessor.invoke(
				Tool.class, "getAllAttrTypeDefMap0", new Object[]{sub});
		assertTrue(result == tmp);
		sub.setAttribute(Tool.ATTR_TYPE_DEF_FLAG, "c=long");
		result = (Map) PrivateAccessor.invoke(
				Tool.class, "getAllAttrTypeDefMap0", new Object[]{sub});
		assertEquals(1, result.size());
		tmp = (Map) result.get(Tool.ALL_OBJ_TYPE);
		assertEquals(3, tmp.size());
		assertTrue(tmp.containsKey("a"));
		assertTrue(tmp.containsKey("b"));
		assertTrue(tmp.containsKey("c"));

		share = makeEmptyFactory(null);
		share.setAttribute(Tool.ATTR_TYPE_DEF_FLAG, "a=String;b=int");
		sub = makeEmptyFactory(share.getFactoryContainer());
		tmp = (Map) PrivateAccessor.invoke(
				Tool.class, "getAllAttrTypeDefMap0", new Object[]{sub});
		result = (Map) PrivateAccessor.invoke(
				Tool.class, "getAllAttrTypeDefMap0", new Object[]{share});
		assertTrue(result == tmp);
	}

	public static EternaFactory makeEmptyFactory(FactoryContainer share)
	{
		String id = share == null ? "test" : share.getId() + ".sub";
		FactoryContainer container = ContainerManager.createFactoryContainer(id,
				"cp:/self/micromagic/eterna/digester2/empty.xml",
				null, null, null, Tool.class.getClassLoader(), share, false);
		container.reInit();
		return (EternaFactory) container.getFactory();
	}

	public void testGetValueConverter()
			throws Exception
	{
		String defStr = "t1/a=String;t2/a=int;t1/b=long;c=double";
		Map share = makeShareMap();
		Map result = (Map) PrivateAccessor.invoke(
				Tool.class, "makeAllAttrTypeDefMap", new Object[]{defStr, share});
		Object obj;
		obj = PrivateAccessor.invoke(Tool.class, "getValueConverter",
				new Object[]{result, "t1", "b"});
		assertTrue(obj instanceof LongConverter);
		obj = PrivateAccessor.invoke(Tool.class, "getValueConverter",
				new Object[]{result, "t1", "c"});
		assertTrue(obj instanceof UtilDateConverter);
		obj = PrivateAccessor.invoke(Tool.class, "getValueConverter",
				new Object[]{result, "t2", "c"});
		assertTrue(obj instanceof DoubleConverter);
		obj = PrivateAccessor.invoke(Tool.class, "getValueConverter",
				new Object[]{result, "t1", "d"});
		assertTrue(obj instanceof ByteConverter);
		obj = PrivateAccessor.invoke(Tool.class, "getValueConverter",
				new Object[]{result, "t3", "d"});
		assertTrue(obj instanceof BooleanConverter);
		obj = PrivateAccessor.invoke(Tool.class, "getValueConverter",
				new Object[]{result, "t2", "b"});
		assertNull(obj);
	}

	private static Map makeShareMap()
			throws Exception
	{
		Map share = new HashMap();
		Map t_t1 = new HashMap();
		t_t1.put("b", new BooleanConverter());
		t_t1.put("c", new UtilDateConverter());
		Map t_t3 = new HashMap();
		t_t3.put("d", new BooleanConverter());
		Map t_all = new HashMap();
		t_all.put("d", new ByteConverter());
		share.put("t1", t_t1);
		share.put("t3", t_t3);
		share.put(PrivateAccessor.get(Tool.class, "ALL_OBJ_TYPE"), t_all);
		return share;
	}

}

