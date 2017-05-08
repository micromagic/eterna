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

package self.micromagic.eterna.dao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;

import junit.framework.TestCase;

import org.dom4j.Element;

import self.micromagic.dbvm.VersionManager;
import self.micromagic.eterna.digester2.ConfigResource;
import self.micromagic.eterna.digester2.ContainerManager;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.FactoryContainer;
import self.micromagic.util.Utility;
import tool.ConnectionTool;

public class BatchUpdateTest extends TestCase
{
	private static final int COUNT = 20;

	public void testBatch()
			throws Exception
	{
		Connection conn = ConnectionTool.getConnection();
		conn.setAutoCommit(false);
		VersionManager.checkVersion(conn, "cp:/self/micromagic/eterna/dao/batchtest/", null);

		conn = ConnectionTool.getConnection();
		FactoryContainer container = ContainerManager.createFactoryContainer("batchTest",
				"cp:self/micromagic/eterna/dao/batchTest.xml", null);
		EternaFactory f = (EternaFactory) container.getFactory();
		f.createUpdate("clearAll").execute(conn);
		conn.commit();

		Utility.setProperty(AppData.APP_LOG_PROPERTY, "1");
		Utility.setProperty(Dao.LOG_TYPE_FLAG, "2");
		AppData data = AppData.getCurrentData();
		Element beginNode = data.beginNode("batch", "test", null);

		Update[] updates = new Update[COUNT];
		for (int i = 0; i < COUNT; i++)
		{
			updates[i] = f.createUpdate("addRow");
			updates[i].setString("key", "key" + i);
			updates[i].setString("strValue", "v" + i);
			updates[i].setObject("index", Utility.createInteger(i));
		}
		BatchUpdate tmp = new BatchUpdate(updates);
		tmp.execute(conn);
		conn.commit();

		updates = new Update[COUNT];
		for (int i = 0; i < COUNT; i++)
		{
			updates[i] = f.createUpdate("modifyRow");
			updates[i].setString("key", "key" + i);
			updates[i].setString("strValue", (i & 0x1) == 1 ? "-- v" + i : "++ v" + i);
		}
		tmp = new BatchUpdate(updates);
		tmp.execute(conn);
		conn.commit();

		ResultIterator ritr = f.createQuery("listAll").executeQuery(conn);
		int index = 0;
		while (ritr.hasNext())
		{
			ResultRow row = ritr.nextRow();
			assertEquals("key" + index, row.getString("key"));
			assertEquals((index & 0x1) == 1 ? "-- v" + index : "++ v" + index,
					row.getString("strValue"));
			index++;
		}

		data.endNode(beginNode, null, null);
		ConfigResource res = ContainerManager.createResource("file:/");
		FileOutputStream out = new FileOutputStream(new File(res.getURI(), "batchUpdate_log.xml"));
		OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
		AppData.printLog(writer, true);
		writer.close();
		out.close();
	}

}
