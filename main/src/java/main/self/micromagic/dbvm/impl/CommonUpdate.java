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

import self.micromagic.dbvm.VersionManager;
import self.micromagic.eterna.dao.Update;
import self.micromagic.eterna.dao.impl.UpdateImpl;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.util.container.ThreadCache;

/**
 * 公共执行器的实现类, 用于处理执行时的日志记录.
 */
public class CommonUpdate extends UpdateImpl
{
	/**
	 * 线程缓存中存放线程信息的名称.
	 */
	public static final String THREAD_INFO_NAME = "dbvm.update.threadInfo";

	/**
	 * 初始化线程信息.
	 */
	public static void initThreadInfo(Update insert, int version, long timeFix)
	{
		ThreadCache cache = ThreadCache.getInstance();
		cache.setProperty(THREAD_INFO_NAME, new UpdateThreadInfo(insert, version, timeFix));
	}

	/**
	 * 清除线程信息.
	 */
	public static void clearThreadInfo()
	{
		ThreadCache cache = ThreadCache.getInstance();
		cache.removeProperty(THREAD_INFO_NAME);
	}

	private static UpdateThreadInfo getThreadInfo()
	{
		ThreadCache cache = ThreadCache.getInstance();
		return (UpdateThreadInfo) cache.getProperty(THREAD_INFO_NAME);
	}

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
		this.executeWithInfo(conn, false, getThreadInfo());
	}

	public int executeUpdate(Connection conn)
			throws EternaException, SQLException
	{
		return this.executeWithInfo(conn, true, getThreadInfo());
	}

	private int executeWithInfo(Connection conn, boolean needResult, UpdateThreadInfo info)
			throws EternaException, SQLException
	{
		this.setExecuteConnection(conn);
		String script = this.getPreparedScript();
		boolean hasParam = this.hasActiveParam();
		boolean success = false;
		int result = -1;
		long now = System.currentTimeMillis() + info.timeFix;
		try
		{
			if (!info.hasError)
			{
				if (needResult)
				{
					result = super.executeUpdate(conn);
				}
				else
				{
					super.execute(conn);
				}
				success = true;
			}
			return result;
		}
		finally
		{
			if (success)
			{
				String msg = "exec sql:".concat(script);
				VersionManager.log(msg, null);
				info.insert.setObject("executed", new Byte((byte) 1));
			}
			else
			{
				info.hasError = true;
				info.insert.setObject("executed", new Byte((byte) 0));
			}
			info.insert.setObject("execTime", new java.sql.Timestamp(now));
			info.insert.setObject("hasParam", hasParam ? new Byte((byte) 1) : new Byte((byte) 0));
			info.insert.setString("scriptText", script);
			info.insert.setObject("scriptIndex", new Integer(info.index++));
			if (info.version > VersionManager.V2)
			{
				try
				{
					info.insert.execute(conn);
					// 每句执行后都提交, 这样强制中断后对不支持DDL事务的更容易恢复
					conn.commit();
				}
				catch (Exception ex)
				{
					VersionManager.log("Error in log script [" + script + "].", ex);
				}
			}
		}
	}

}

/**
 * 更新过程中的线程信息.
 */
class UpdateThreadInfo
{
	final Update insert;
	final int version;
	final long timeFix;
	int index;
	boolean hasError;

	public UpdateThreadInfo(Update insert, int version, long timeFix)
	{
		this.version = version;
		this.insert = insert;
		this.timeFix = timeFix;
		this.index = 1;
	}

}
