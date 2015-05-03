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

package self.micromagic.dbvm.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import self.micromagic.dbvm.VersionManager;
import self.micromagic.eterna.dao.Update;
import self.micromagic.eterna.dao.impl.UpdateImpl;
import self.micromagic.eterna.share.EternaException;

/**
 * 公共执行器的实现类, 用于处理执行时的日志记录.
 */
public class CommonUpdate extends UpdateImpl
{
	public Object create()
			throws EternaException
	{
		CommonUpdate other = new CommonUpdate();
		this.copy(other);
		return other;
	}

	public void execute(Connection conn)
			throws EternaException, SQLException
	{
		this.log(this.getPreparedSQL());
		boolean success = false;
		try
		{
			if (!logScriptMode)
			{
				super.execute(conn);
				success = true;
			}
		}
		finally
		{
			if (success && !logScriptMode)
			{
				executedScript.add(this.getPreparedSQL());
			}
			else
			{
				leftScript.add(this.getPreparedSQL());
			}
		}
	}

	public int executeUpdate(Connection conn)
			throws EternaException, SQLException
	{
		this.log(this.getPreparedSQL());
		boolean success = false;
		try
		{
			if (!logScriptMode)
			{
				int r = super.executeUpdate(conn);
				success = true;
				return r;
			}
			return 0;
		}
		finally
		{
			if (success && !logScriptMode)
			{
				executedScript.add(this.getPreparedSQL());
			}
			else
			{
				leftScript.add(this.getPreparedSQL());
			}
		}
	}

	private void log(String sql)
	{
		if (!logScriptMode)
		{
			// 不记录脚本模式才输出日志
			String msg = "exec sql:".concat(sql);
			VersionManager.log(msg, null);
		}
		//System.out.println(msg);
	}

	public static void saveScript(Update insert, Connection conn)
			throws SQLException
	{
		insert.setObject("executed", new Byte((byte) 1));
		saveScript0(executedScript, insert, conn);
		insert.setObject("executed", new Byte((byte) 0));
		saveScript0(leftScript, insert, conn);
	}
	private static void saveScript0(List scripts, Update insert, Connection conn)
			throws SQLException
	{
		int index = 1;
		Iterator itr = scripts.iterator();
		while (itr.hasNext())
		{
			insert.setObject("scriptText", itr.next());
			insert.setObject("scriptIndex", new Integer(index++));
			insert.execute(conn);
		}
	}

	/**
	 * 设置是否为记录脚本的模式.
	 */
	public static void setLogScript(boolean mode)
	{
		logScriptMode = mode;
	}
	private static boolean logScriptMode;

	/**
	 * 清除所有的脚本.
	 */
	public static void clearScript()
	{
		executedScript.clear();
		leftScript.clear();
		logScriptMode = false;
	}

	/**
	 * 已执行的脚本.
	 */
	private static List executedScript = new LinkedList();

	/**
	 * 未执行的脚本.
	 */
	private static List leftScript = new LinkedList();

}
