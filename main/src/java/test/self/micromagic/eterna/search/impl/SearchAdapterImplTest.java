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

package self.micromagic.eterna.search.impl;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import self.micromagic.eterna.TestTool;
import self.micromagic.eterna.dao.Dao;
import self.micromagic.eterna.digester2.ContainerManager;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.search.SearchResult;
import self.micromagic.eterna.search.Search;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.FactoryContainer;
import self.micromagic.util.Utility;

public class SearchAdapterImplTest extends TestCase
{
	private EternaFactory factory;

	public void testMulti()
			throws Exception
	{
		Utility.setProperty(Dao.LOG_TYPE_FLAG, "1");
		Map param = new HashMap();
		param.put("paperType", "1");
		param.put("defaultMemo", "1");
		param.put("queryType", "set");
		Search search = this.factory.createSearch("multi.search");
		AppData data = TestTool.getAppData(param);
		SearchResult r = search.doSearch(data, null);
	}


	protected void setUp()
			throws Exception
	{
		this.factory = (EternaFactory) instance.getFactory();
	}

	protected void tearDown()
			throws Exception
	{
		this.factory = null;
	}

	static FactoryContainer instance;
	static
	{
		instance = ContainerManager.createFactoryContainer("search.test",
				"cp:self/micromagic/eterna/search/impl/search.xml", null);
	}

}