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

package self.micromagic.util.container;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import junit.framework.TestCase;

public class RequestParameterMapTest extends TestCase
{
	public void testEntrySet()
	{
		HashMap map = new HashMap();
		map.put("k1", "v1");
		map.put("k2", new Integer(2));
		map.put("k3", new String[]{"a", "b", "c"});
		map.put("k4", Arrays.asList(new Object[]{new Integer(100), "200", new Long(300L)}));
		map.put("k5", new Object[]{new Integer(110), "220", new Long(330L)});
		RequestParameterMap rmap = RequestParameterMap.create(map);
		rmap.setParseValue(true);

		Iterator itr = rmap.entrySet().iterator();
		while (itr.hasNext())
		{
			Map.Entry entry = (Map.Entry) itr.next();
			assertTrue(entry.getValue() instanceof String);
		}

		itr = rmap.values().iterator();
		while (itr.hasNext())
		{
			assertTrue(itr.next() instanceof String);
		}

	}

	public void testGetValue()
	{
		HashMap map = new HashMap();
		map.put("k1", "v1");
		map.put("k2", new Integer(2));
		map.put("k3", new String[]{"a", "b", "c"});
		map.put("k4", Arrays.asList(new Object[]{new Integer(100), "200", new Long(300L)}));
		map.put("k5", new Object[]{new Integer(110), "220", new Long(330L)});
		RequestParameterMap rmap = RequestParameterMap.create(map);
		rmap.setParseValue(true);

		assertEquals(rmap.get("k1"), "v1");
		assertEquals(rmap.get("k2"), "2");
		assertEquals(rmap.get("k3"), "a");
		assertEquals(rmap.get("k4"), "100");
		assertEquals(rmap.get("k5"), "110");
		assertEquals(rmap.get("k6"), null);

		String[] arr;

		arr = (String[]) rmap.get("k1[]");
		assertEquals(arr.length, 1);
		assertEquals(arr[0], "v1");

		arr = (String[]) rmap.get("k2[]");
		assertEquals(arr.length, 1);
		assertEquals(arr[0], "2");

		arr = (String[]) rmap.get("k3[]");
		assertEquals(arr.length, 3);
		assertTrue(Arrays.equals(arr, new String[]{"a", "b", "c"}));

		arr = (String[]) rmap.get("k4[]");
		assertEquals(arr.length, 3);
		assertTrue(Arrays.equals(arr, new String[]{"100", "200", "300"}));

		arr = (String[]) rmap.get("k5[]");
		assertEquals(arr.length, 3);
		assertTrue(Arrays.equals(arr, new String[]{"110", "220", "330"}));

		arr = (String[]) rmap.get("k6[]");
		assertEquals(arr, null);
	}

}