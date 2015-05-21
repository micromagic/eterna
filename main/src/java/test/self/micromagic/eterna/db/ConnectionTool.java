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

package self.micromagic.eterna.db;

import java.sql.Connection;
import java.sql.DriverManager;

import self.micromagic.dbvm.VersionManager;

public class ConnectionTool
{
	public static synchronized Connection getConnection()
			throws Exception
	{
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection("jdbc:h2:~/vTest", "sa", "sa");
		if (first)
		{
			first = false;
			VersionManager.checkVersion(conn, "cp:/self/micromagic/eterna/db/", null);
			conn = DriverManager.getConnection("jdbc:h2:~/vTest", "sa", "sa");
		}
		return conn;
	}
	private static boolean first = true;

}
