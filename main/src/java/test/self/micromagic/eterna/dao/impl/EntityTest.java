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

package self.micromagic.eterna.dao.impl;

import junit.framework.TestCase;
import self.micromagic.eterna.dao.Entity;
import self.micromagic.eterna.digester2.ContainerManager;
import self.micromagic.eterna.digester2.ParseException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.FactoryContainer;
import self.micromagic.eterna.share.TypeManager;

public class EntityTest extends TestCase
{
	public void testRef()
	{
		Entity e1 = f.getEntity("e1");
		assertEquals(3, e1.getItemCount());

		Entity e2 = f.getEntity("e2");
		assertEquals(2, e2.getItemCount());
		assertEquals(TypeManager.TYPE_STRING, e2.getItem("c1").getType());
		assertEquals("title1", e2.getItem("c1").getCaption());
		assertEquals("xxxxxxx", e2.getItem("c1").getAttribute("a1"));
		assertEquals(TypeManager.TYPE_LONG, e2.getItem("c2").getType());
		assertEquals("my_c2", e2.getItem("c2").getCaption());
		assertEquals("2", e2.getItem("c2").getAttribute("a2"));

		Entity e3 = f.getEntity("e3");
		assertEquals(2, e3.getItemCount());
		assertEquals(TypeManager.TYPE_OBJECT, e3.getItem("c2").getType());
		assertEquals("test", e3.getItem("c2").getColumnName());
		assertNull(e3.getItem("c2").getCaption());
		assertNull(e3.getItem("c2").getAttribute("a2"));
		assertEquals("?", e3.getItem("c2").getAttribute("x"));
		assertEquals(TypeManager.TYPE_DOUBLE, e3.getItem("c3").getType());
		assertEquals("title3", e3.getItem("c3").getCaption());
		assertEquals("3", e3.getItem("c3").getAttribute("a3"));
	}

	static void init()
	{
		try
		{
			container = ContainerManager.createFactoryContainer("daoTest",
					"cp:self/micromagic/eterna/dao/impl/daoTest.xml", null);
			f = (EternaFactory) container.getFactory();
		}
		catch (Throwable ex)
		{
			if (ex instanceof ParseException)
			{
				ex.printStackTrace();
			}
			else
			{
				(new ParseException(ex.getMessage())).printStackTrace();
			}
		}
	}
	static FactoryContainer container;
	static EternaFactory f;

	static
	{
		init();
	}

}
