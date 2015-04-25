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

package self.micromagic.eterna.model.impl;

import java.util.Map;
import java.util.HashMap;

import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.EternaFactoryCreater;
import self.micromagic.eterna.dao.Update;
import self.micromagic.eterna.model.Model;
import self.micromagic.eterna.model.Execute;
import self.micromagic.eterna.model.AppData;
import junit.framework.TestCase;

public class ParamBindImplTest extends TestCase
{
	EternaFactory factory;

	protected void setUp() throws Exception
	{
		this.factory = EternaFactoryCreater.getEternaFactory(this.getClass());
	}

	protected void tearDown() throws Exception
	{
		this.factory = null;
	}

	public void testSetSub()
			 throws Exception
	{
		Model model = this.factory.createModel("sub.test.model");
		Execute exe = model.getExecute(1);
		AppData data = AppData.getCurrentData();
		data.caches[2] = "E";
		exe.execute(data, null);
		Update update = (Update) data.caches[0];
		assertEquals("E (E) T E", update.getPreparedSQL());

		Map map = new HashMap();
		map.put("t1", "A");
		map.put("t2", "B");
		map.put("t3", "C");
		data.caches[2] = map;
		data.caches[0] = null;
		exe.execute(data, null);
		update = (Update) data.caches[0];
		assertEquals("C (A) T B", update.getPreparedSQL());
	}

}