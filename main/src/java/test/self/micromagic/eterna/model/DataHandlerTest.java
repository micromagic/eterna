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

package self.micromagic.eterna.model;

import junit.framework.TestCase;
import self.micromagic.util.Utility;
import tool.PrivateAccessor;

public class DataHandlerTest extends TestCase
{
	public void testSetMap1()
			throws Exception
	{
		DataHandler handler = new DataHandler("testMap1", true, true);

		handler.setConfig("RP.t1");
		assertEquals("RP.t1", Utility.createInteger(AppData.REQUEST_PARAMETER_MAP),
				this.getMapIndex(handler));
		assertEquals("RP.t1", "t1", ((Object[]) PrivateAccessor.get(handler, "subs"))[0]);
		handler.setConfig("RA.t2");
		assertEquals("RA.t2", Utility.createInteger(AppData.REQUEST_ATTRIBUTE_MAP),
				this.getMapIndex(handler));
		assertEquals("RA.t2", "t2", ((Object[]) PrivateAccessor.get(handler, "subs"))[0]);
		handler.setConfig("param.t3");
		assertEquals("param.t3", Utility.createInteger(AppData.REQUEST_PARAMETER_MAP),
				this.getMapIndex(handler));
		assertEquals("param.t3", "t3", ((Object[]) PrivateAccessor.get(handler, "subs"))[0]);
		handler.setConfig("data.t4");
		assertEquals("data.t4", Utility.createInteger(AppData.DATA_MAP),
				this.getMapIndex(handler));
		assertEquals("data.t4", "t4", ((Object[]) PrivateAccessor.get(handler, "subs"))[0]);

		try
		{
			handler.setConfig("data");
			fail("Need map data name.");
		}
		catch (Exception ex)
		{
			assertEquals("Error testMap1 main name [data].", ex.getMessage());
		}
	}

	public void testSetMap2()
		throws Exception
	{
		DataHandler handler = new DataHandler("testMap2", false, true);

		handler.setConfig("RP");
		assertEquals("RP", Utility.createInteger(AppData.REQUEST_PARAMETER_MAP),
				this.getMapIndex(handler));
		assertEquals("RP", null, (PrivateAccessor.get(handler, "subs")));
		handler.setConfig("RA.t2");
		assertEquals("RA.t2", Utility.createInteger(AppData.REQUEST_ATTRIBUTE_MAP),
				this.getMapIndex(handler));
		assertEquals("RA.t2", "t2", ((Object[]) PrivateAccessor.get(handler, "subs"))[0]);
		handler.setConfig("session.t3");
		assertEquals("session.t3", Utility.createInteger(AppData.SESSION_ATTRIBUTE_MAP),
				this.getMapIndex(handler));
		assertEquals("session.t3", "t3", ((Object[]) PrivateAccessor.get(handler, "subs"))[0]);
		handler.setConfig("data");
		assertEquals("data", Utility.createInteger(AppData.DATA_MAP),
				this.getMapIndex(handler));
		assertEquals("data", null, (PrivateAccessor.get(handler, "subs")));

		try
		{
			handler.setConfig("other");
			fail("Error map name.");
		}
		catch (Exception ex)
		{
			assertEquals("Error testMap2 main name [other].", ex.getMessage());
		}

	}

	private Object getMapIndex(DataHandler handler)
			throws Exception
	{
		Object obj = PrivateAccessor.get(handler, "mapGetter");
		return PrivateAccessor.get(obj, "mapIndex");
	}

	public void _testStack()
	{
		try
		{
			DataHandler handler = new DataHandler("testStack1", false, true);
			handler.setConfig("stack");
			assertEquals("stack", new Integer(-1), PrivateAccessor.get(handler, "peekIndex"));
			assertEquals("stack", Boolean.TRUE, PrivateAccessor.get(handler, "fromStack"));
			handler.setConfig("stack:pop");
			assertEquals("stack:pop", new Integer(-1), PrivateAccessor.get(handler, "peekIndex"));
			assertEquals("stack:pop", Boolean.TRUE, PrivateAccessor.get(handler, "fromStack"));
			handler.setConfig("stack:peek");
			assertEquals("stack:peek", Utility.createInteger(0), PrivateAccessor.get(handler, "peekIndex"));
			assertEquals("stack:peek", Boolean.TRUE, PrivateAccessor.get(handler, "fromStack"));
			handler.setConfig("stack:peek-3");
			assertEquals("stack:peek-3", Utility.createInteger(3), PrivateAccessor.get(handler, "peekIndex"));
			assertEquals("stack:peek-3", Boolean.TRUE, PrivateAccessor.get(handler, "fromStack"));
		}
		catch (Exception ex)
		{
			fail(ex.getMessage());
		}
		try
		{
			DataHandler handler = new DataHandler("testStack2", true, false);
			handler.setConfig("stack:peek");
			fail("Stack can't use in read only.");
		}
		catch (Exception ex)
		{
			assertEquals("Error testStack2 [stack:peek].", ex.getMessage());
		}
	}

	public void _testCache()
	{
		try
		{
			DataHandler handler = new DataHandler("testCache", false, true);
			handler.setConfig("cache");
			assertEquals("cache", Utility.createInteger(0), PrivateAccessor.get(handler, "cacheIndex"));
			handler.setConfig("cache:15");
			assertEquals("cache:15", Utility.createInteger(15), PrivateAccessor.get(handler, "cacheIndex"));
		}
		catch (Exception ex)
		{
			fail(ex.getMessage());
		}
	}

}