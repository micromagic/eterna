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
import java.sql.SQLException;

import org.apache.commons.logging.Log;

import self.micromagic.eterna.digester2.ContainerManager;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.Factory;
import self.micromagic.eterna.share.FactoryContainer;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;
import self.micromagic.util.ref.StringRef;

/**
 * 数据库的版本管理者.
 */
public class VersionManager
{
	/**
	 * 检查连接所对应的数据库版本, 如果未达到要求则进行版本升级.
	 *
	 * @param conn         数据库连接
	 * @param packagePath  版本信息所在的包路径
	 * @param loader       版本信息所属的classloader
	 */
	public static void checkVersion(Connection conn, String packagePath, ClassLoader loader)
	{
		boolean success = false;
		synchronized (conn)
		{
			try
			{
				conn.setAutoCommit(false);
				DataBaseLock.lock(conn, VERSION_LOCK_NAME, null, true);
				checkVersion0(conn, packagePath, loader);
				success = true;
			}
			catch (Throwable ex)
			{
				log.error("Upper data base version fail!", ex);
			}
			finally
			{
				try
				{
					if (success)
					{
						conn.commit();
					}
					else
					{
						conn.rollback();
					}
					DataBaseLock.releaseDB(conn, VERSION_LOCK_NAME);
					conn.close();
				}
				catch (Exception ex) {}
			}
		}
	}
	private static void checkVersion0(Connection conn, String packagePath, ClassLoader loader)
	{

	}
	private static void upperVersion(Connection conn, String config, String vName,
			int version, ClassLoader loader)
			throws SQLException
	{
		if (loader == null)
		{
			loader = Thread.currentThread().getContextClassLoader();
		}
		if (loader == null)
		{
			loader = VersionManager.class.getClassLoader();
		}
		String dbName = conn.getMetaData().getDatabaseProductName();
		FactoryContainer c = ContainerManager.createFactoryContainer("v" + version,
				config, null, DataBaseLock.getDigester(), null, loader,
				DataBaseLock.getContainer(dbName), false);
		StringRef msg = new StringRef();
		c.reInit(msg);
		if (!StringTool.isEmpty(msg.getString()))
		{
			throw new EternaException(msg.getString());
		}
		Factory f = c.getFactory();
		String[] names = f.getObjectNames(null);
		for (int i = 0; i < names.length; i++)
		{
			Object obj = f.createObject(names[i]);
			if (obj instanceof OptDesc)
			{
				((OptDesc) obj).exec(conn);
			}
		}
		// TODO end version
		/*

			<update name="exec.6">
				<prepared-sql noLine="true">
					update PLATFORM_SYS_CONFIG set configValue = '7', lastModified = #const(now)
					where configName = 'db_platform_version'
				</prepared-sql>
			</update>
			<update name="exec.7">
				<prepared-sql noLine="true">
					insert into PLATFORM_VERSION_LOG (versionName, versionValue, lastModified)
					values ('db_platform_version', 7, #const(now))
				</prepared-sql>
			</update>
		 */
	}

	/**
	 * 用于数据库版本升级的锁.
	 */
	private static final String VERSION_LOCK_NAME = "eterna.version.lock";

	/**
	 * 设置使用的日志对象.
	 */
	public static void setLog(Log log)
	{
		VersionManager.log = log;
	}
	private static Log log = Utility.createLog("eterna.dbvm");;

}
