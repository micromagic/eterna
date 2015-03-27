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

package self.micromagic.eterna.share;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import self.micromagic.util.converter.BooleanConverter;
import self.micromagic.util.converter.ByteConverter;
import self.micromagic.util.converter.DoubleConverter;
import self.micromagic.util.converter.LongConverter;
import self.micromagic.util.converter.UtilDateConverter;
import tool.PrivateAccessor;

public class ToolTest extends TestCase
{
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
