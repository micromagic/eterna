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

import junit.framework.TestCase;

import org.dom4j.Document;
import org.dom4j.Element;

import self.micromagic.eterna.digester2.ContainerManager;
import self.micromagic.eterna.digester2.dom.DocumentTool;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.FactoryContainer;
import self.micromagic.util.StringTool;
import self.micromagic.util.ref.StringRef;
import tool.ConnectionTool;

public class InitTest extends TestCase
		implements ConstantDef
{
	public void testInit1()
			throws Exception
	{
		Document doc = DocumentTool.createDoc(
				this.getClass().getResourceAsStream("testdb/version2.xml"));
		Element e = (Element) doc.getRootElement().element("table").elements("column").get(1);
		System.out.println(e.attributeValue("name"));
		System.out.println(e.attribute("desc").getStringValue());

		EternaFactory f = initTestFactory("test01.xml");
		String[] names;
		ColumnDefiner columnDefiner = (ColumnDefiner) f.createObject(COLUMN_DEF_NAME);

		names = f.getObjectNames(TableDesc.class);
		TableDesc tableDesc = (TableDesc) f.createObject(names[0]);
		ColumnDesc columnDesc = (ColumnDesc) tableDesc.columns.get(0);
		System.out.println(columnDefiner.getColumnDefine(tableDesc, columnDesc, null));

		names = f.getObjectNames(ScriptDesc.class);
		ScriptDesc dataDesc = (ScriptDesc) f.createObject(names[0]);
		System.out.println(names[0] + ":" + dataDesc.script);
		assertTrue(dataDesc.script.endsWith(" now()"));
	}

	public void testInit2()
	{
		EternaFactory f = initTestFactory("test02.xml");
		String[] names;

		names = f.getObjectNames(ScriptDesc.class);
		ScriptDesc dataDesc = (ScriptDesc) f.createObject(names[0]);
		System.out.println(names[0] + ":" + dataDesc.script);
		assertEquals("set c1,= now()", dataDesc.script);
	}

	public void testTestdb()
			throws Exception
	{
		VersionManager.checkVersion(ConnectionTool.getConnection(),
				"cp:/self/micromagic/dbvm/testdb/", null);
	}

	static EternaFactory initTestFactory(String file)
	{
		ClassLoader loader = DataBaseLocker.class.getClassLoader();
		String config = DataBaseLocker.CONFIG_PREFIX + file;
		FactoryContainer c = ContainerManager.createFactoryContainer(file,
				config, null, VersionManager.getDigester(), null, loader,
				DataBaseLocker.getContainer("MySQL"), false);
		StringRef msg = new StringRef();
		c.reInit(msg);
		if (!StringTool.isEmpty(msg.getString()))
		{
			fail(msg.getString());
		}
		return (EternaFactory) c.getFactory();
	}

}
