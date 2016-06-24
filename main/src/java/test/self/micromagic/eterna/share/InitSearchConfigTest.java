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

import junit.framework.TestCase;
import self.micromagic.eterna.digester2.ContainerManager;

public class InitSearchConfigTest extends TestCase
{
	public void test01()
	{
		EternaFactory f = this.getFactory("test01.xml", null);
		EternaFactory share = f.getShareFactory();
		assertNotNull(share);
		assertEquals(2, f.getSearchAttributes().pageStart);
		assertEquals("num", f.getSearchAttributes().pageNumTag);
		assertEquals(0, share.getSearchAttributes().pageStart);
		assertEquals("pageNum", share.getSearchAttributes().pageNumTag);
	}

	public void test02()
	{
		EternaFactory f = this.getFactory("test02.xml", "test01.xml");
		EternaFactory share = f.getShareFactory();
		assertNotNull(share);
		assertEquals(2, f.getSearchAttributes().pageStart);
		assertEquals("num", f.getSearchAttributes().pageNumTag);
		assertEquals(2, share.getSearchAttributes().pageStart);
		assertEquals("num", share.getSearchAttributes().pageNumTag);
	}

	public void test03()
	{
		EternaFactory f = this.getFactory("test03.xml", "test01.xml");
		EternaFactory share = f.getShareFactory();
		assertNotNull(share);
		assertEquals(2, f.getSearchAttributes().pageStart);
		assertEquals("num", f.getSearchAttributes().pageNumTag);
		assertEquals(2, share.getSearchAttributes().pageStart);
		assertEquals("num", share.getSearchAttributes().pageNumTag);
		assertTrue(f.createSearchManager() instanceof TestSearchManager);
		assertFalse(share.createSearchManager() instanceof TestSearchManager);
		assertEquals(0, f.findObjectId(EternaFactory.SEARCH_MANAGER_GENERATOR_NAME));
	}

	private static int index = 100;

	private EternaFactory getFactory(String fileName, String share)
	{
		if (share == null)
		{
			FactoryContainer c = ContainerManager.createFactoryContainer(fileName + "-" + index++,
					"cp:/self/micromagic/eterna/share/" + fileName, null);
			return (EternaFactory) c.getFactory();
		}
		FactoryContainer shareContainer = ContainerManager.createFactoryContainer(share + "-" + index++,
				"cp:/self/micromagic/eterna/share/" + share, null);
		FactoryContainer c = ContainerManager.createFactoryContainer(fileName + "-" + index++,
				"cp:/self/micromagic/eterna/share/" + fileName, null, null, null, null,
				shareContainer, true);
		return (EternaFactory) c.getFactory();
	}

}
