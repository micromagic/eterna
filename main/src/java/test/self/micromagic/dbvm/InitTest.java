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

package self.micromagic.dbvm;

import java.sql.Connection;
import java.sql.DriverManager;

import junit.framework.TestCase;
import self.micromagic.eterna.digester2.ContainerManager;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.FactoryContainer;
import self.micromagic.util.StringTool;
import self.micromagic.util.ref.StringRef;

public class InitTest extends TestCase
		implements ConstantDef
{
	public void testInit()
	{
		ClassLoader loader = DataBaseLock.class.getClassLoader();
		String config = DataBaseLock.CONFIG_PREFIX + "test01.xml";
		FactoryContainer c = ContainerManager.createFactoryContainer("mysql",
				config, null, VersionManager.getDigester(), null, loader,
				DataBaseLock.getContainer("MySQL"), false);
		StringRef msg = new StringRef();
		c.reInit(msg);
		if (!StringTool.isEmpty(msg.getString()))
		{
			fail(msg.getString());
		}
		EternaFactory f = (EternaFactory) c.getFactory();
		String[] names;
		ColumnDefiner columnDefiner = (ColumnDefiner) f.createObject(COLUMN_DEF_NAME);

		names = f.getObjectNames(TableDesc.class);
		TableDesc tableDesc = (TableDesc) f.createObject(names[0]);
		ColumnDesc columnDesc = (ColumnDesc) tableDesc.columns.get(0);
		System.out.println(columnDefiner.getColumnDefine(tableDesc, columnDesc, null));

		names = f.getObjectNames(DataDesc.class);
		DataDesc dataDesc = (DataDesc) f.createObject(names[0]);
		System.out.println(names[0] + ":" + dataDesc.script);
	}

	public void testTestdb()
			throws Exception
	{
		VersionManager.checkVersion(
				getConnection(), "cp:/self/micromagic/dbvm/testdb/", null);
	}

	static Connection getConnection()
			throws Exception
	{
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection("jdbc:h2:~/vTest", "sa", "sa");
		return conn;
	}

}
