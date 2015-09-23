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
import self.micromagic.eterna.digester2.ContainerManager;
import self.micromagic.eterna.digester2.ParseException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.FactoryContainer;

/**
 * 数据操作测试的基础类.
 */
public class TestBase extends TestCase
{

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
	protected static FactoryContainer container;
	protected static EternaFactory f;

	static
	{
		init();
	}

}
