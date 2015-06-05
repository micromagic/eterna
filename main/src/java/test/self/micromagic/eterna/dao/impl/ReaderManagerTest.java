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
import self.micromagic.eterna.dao.ResultReaderManager;
import self.micromagic.eterna.dao.reader.ReaderFactory;
import self.micromagic.eterna.digester2.ContainerManager;
import self.micromagic.eterna.share.EternaFactory;

public class ReaderManagerTest extends TestCase
{
	public void testSetReaders()
	{
		this.testSetReaders(true);
		this.testSetReaders(false);
	}

	private void testSetReaders(boolean colNameSensitive)
	{
		String[] names = {"A", "B", "C", "D", "E", "F"};
		ResultReaderManager m = create(names, colNameSensitive);
		checkReaders(names, m);

		String[] tmpArr = new String[]{"B", "F", "A", "D"};
		m.setReaderList(tmpArr);
		checkReaders(tmpArr, m);

		tmpArr = new String[]{"E", "B", "D", "A"};
		m.setReaderList(tmpArr);
		checkReaders(tmpArr, m);

		if (!colNameSensitive)
		{
			tmpArr = new String[]{"C", "a", "f"};
			m.setReaderList(tmpArr);
			tmpArr = new String[]{"C", "A", "F"};
			checkReaders(tmpArr, m);
		}
	}

	private void checkReaders(String[] names, ResultReaderManager manager)
	{
		for (int i = 0; i < names.length; i++)
		{
			String name = manager.getReader(i).getName();
			assertEquals("Check reader name " + i, names[i], name);
			assertEquals("Check reader index " + i, i, manager.getReaderIndex(name));
		}
	}

	public static ResultReaderManager create(String[] names, boolean colNameSensitive)
	{
		ReaderManagerImpl r = new ReaderManagerImpl();
		r.setColNameSensitive(colNameSensitive);
		for (int i = 0; i < names.length; i++)
		{
			r.addReader(ReaderFactory.createReader("String", names[i]));
		}
		r.initialize((EternaFactory) ContainerManager.getGlobalContainer().getFactory());
		return r;
	}

}
