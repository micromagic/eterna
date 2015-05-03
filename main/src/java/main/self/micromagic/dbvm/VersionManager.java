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

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.logging.Log;

import self.micromagic.dbvm.impl.CommonUpdate;
import self.micromagic.eterna.dao.Query;
import self.micromagic.eterna.dao.ResultIterator;
import self.micromagic.eterna.dao.ResultRow;
import self.micromagic.eterna.dao.Update;
import self.micromagic.eterna.digester2.ConfigResource;
import self.micromagic.eterna.digester2.ContainerManager;
import self.micromagic.eterna.digester2.Digester;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.Factory;
import self.micromagic.eterna.share.FactoryContainer;
import self.micromagic.util.ResManager;
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
		if (loader == null)
		{
			loader = Thread.currentThread().getContextClassLoader();
		}
		if (loader == null)
		{
			loader = VersionManager.class.getClassLoader();
		}
		boolean success = false;
		try
		{
			conn.setAutoCommit(false);
			DataBaseLock.lock(conn, VERSION_LOCK_NAME, null, true);
			synchronized (VersionManager.class)
			{
				checkVersion0(conn, packagePath, loader);
			}
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
	private static void checkVersion0(Connection conn, String packagePath, ClassLoader loader)
			throws Exception
	{
		ConfigResource res = ContainerManager.createClassPathResource(packagePath, loader);
		String vName = isFullPackageName() ? res.getConfig() : res.getName();
		String dbName = conn.getMetaData().getDatabaseProductName();
		EternaFactory f = DataBaseLock.getFactory(dbName);
		int version = getVersionValue(conn, vName, f);
		if (version == -2)
		{
			// 版本有错误直接退出
			return;
		}
		if (version == -1)
		{
			// 版本信息为-1, 表示没有版本表, 需要创建版本表
			upperVersion(conn, DataBaseLock.CONFIG_PREFIX + "impl/version1.xml",
					ETERNA_VERSION_TABLE, 1, VersionManager.class.getClassLoader());
			version = 0;
		}
		int tVersion = getVersionValue(conn, ETERNA_VERSION_TABLE, f);
		if (tVersion < eternaMaxVersion)
		{
			// 版本表的版本小于目前系统定义的最大版本, 需要升级
			if (tVersion == -2)
			{
				log.error("Version tables has error.");
				return;
			}
			ClassLoader tmpLoader = VersionManager.class.getClassLoader();
			ConfigResource tmp = ContainerManager.createClassPathResource(
					DataBaseLock.CONFIG_PREFIX + "impl/", tmpLoader);
			checkVersion0(conn, tVersion + 1, ETERNA_VERSION_TABLE, tmp, tmpLoader);
		}
		version++;
		checkVersion0(conn, version, vName, res, loader);
	}
	private static void checkVersion0(Connection conn, int version, String vName,
			ConfigResource packagePath, ClassLoader loader)
			throws Exception
	{
		// 循环执行更新到最新版本
		String versionFile = "version".concat(version + ".xml");
		ConfigResource vRes = packagePath.getResource(versionFile);
		InputStream tmpStream = vRes.getAsStream();
		while (tmpStream != null)
		{
			try
			{
				tmpStream.close();
			}
			catch (IOException ex) {}
			try
			{
				upperVersion(conn, vRes.getConfig(), vName, version, loader);
			}
			finally
			{
				CommonUpdate.clearScript();
			}
			version++;
			versionFile = "version".concat(version + ".xml");
			vRes = packagePath.getResource(versionFile);
			tmpStream = vRes.getAsStream();
		}
	}
	private static void upperVersion(Connection conn, String config, String vName,
			int version, ClassLoader loader)
			throws Exception
	{
		log.info("Begin " + vName + " up to " + version + ". --------------------");
		String dbName = conn.getMetaData().getDatabaseProductName();
		FactoryContainer share = DataBaseLock.getContainer(dbName);
		FactoryContainer c = ContainerManager.createFactoryContainer("v" + version,
				config, null, getDigester(), null, loader, share, false);
		StringRef msg = new StringRef();
		c.reInit(msg);
		if (!StringTool.isEmpty(msg.getString()))
		{
			throw new EternaException(msg.getString());
		}
		Factory f = c.getFactory();
		boolean success = false;
		String errMsg = null;
		try
		{
			success = upperVersion0(conn, config, vName, version, loader, f);
		}
		catch (Exception ex)
		{
			errMsg = ex.getMessage();
			throw ex;
		}
		finally
		{
			if (success)
			{
				log.info("End   " + vName + " up to " + version + ". --------------------");
			}
			else
			{
				log.warn("Error " + vName + " up to " + version + ". --------------------");
				if (StringTool.isEmpty(errMsg))
				{
					errMsg = "Other error.";
				}
			}
			// 更新版本信息后提交
			addVersionLog(conn, vName, version, errMsg, f);
			conn.commit();
			if (!success)
			{
				// 如果没有执行成功, 添加脚本信息
				Update insert = (Update) f.createObject("addVersionScript");
				insert.setString("versionName", vName);
				insert.setObject("versionValue", Utility.createInteger(version));
				insert.setObject("execTime", new java.sql.Timestamp(System.currentTimeMillis()));
				CommonUpdate.saveScript(insert, conn);
				conn.commit();
			}
		}
	}
	private static boolean upperVersion0(Connection conn, String config, String vName,
			int version, ClassLoader loader, Factory factory)
			throws Exception
	{
		String[] names = factory.getObjectNames(OptDesc.class);
		Exception err = null;
		for (int i = 0; i < names.length; i++)
		{
			Object obj = factory.createObject(names[i]);
			if (obj instanceof OptDesc)
			{
				try
				{
					((OptDesc) obj).exec(conn);
				}
				catch (Exception ex)
				{
					err = ex;
					// 如果出错, 接下来的执行设置为记录模式
					CommonUpdate.setLogScript(true);
				}
			}
		}
		if (err != null)
		{
			throw err;
		}
		return true;
	}

	/**
	 * 获取版本值.
	 */
	private static int getVersionValue(Connection conn, String vName, Factory factory)
	{
		Query q = (Query) factory.createObject("getVersionValue");
		int version = -1;
		try
		{
			q.setString("versionName", vName);
			ResultIterator ritr = q.executeQuery(conn);
			if (ritr.hasNext())
			{
				ResultRow row = ritr.nextRow();
				version = row.getInt("versionValue");
				String errInfo = row.getString("errInfo");
				if (!StringTool.isEmpty(errInfo))
				{
					log.warn("The db [" + vName + "] can't upper, because has error: "
							+ errInfo + ".");
					return -2;
				}
			}
			else
			{
				version = 0;
			}
		}
		catch (Exception ex) {}
		return version;
	}

	/**
	 * 添加版本更新日志及设置版本记录.
	 */
	private static void addVersionLog(Connection conn, String vName, int version,
			String errInfo, Factory factory)
			throws SQLException
	{
		Integer v = Utility.createInteger(version);
		Update u;
		if (version == 1)
		{
			u = (Update) factory.createObject("addVersionValue");
		}
		else
		{
			u = (Update) factory.createObject("setVersionValue");
		}
		u.setString("versionName", vName);
		u.setString("errInfo", errInfo);
		u.setObject("versionValue", v);
		u.execute(conn);
		u = (Update) factory.createObject("addVersionLog");
		u.setString("versionName", vName);
		u.setString("errInfo", errInfo);
		u.setObject("versionValue", v);
		u.execute(conn);
	}

	/**
	 * 是否使用全的包路径作为版本名称.
	 */
	public static boolean isFullPackageName()
	{
		return fullPackageName;
	}

	/**
	 * 设置是否使用全的包路径作为版本名称.
	 */
	public static void setFullPackageName(boolean full)
	{
		fullPackageName = full;
	}
	private static boolean fullPackageName;

	/**
	 * 用于数据库版本升级的锁.
	 */
	private static final String VERSION_LOCK_NAME = "eterna.version.lock";

	/**
	 * 版本系统表的当前最高版本.
	 */
	private static final int eternaMaxVersion = 2;

	/**
	 * 版本系统表的版本标识.
	 */
	private static final String ETERNA_VERSION_TABLE = "__eterna_version_table";

	/**
	 * 获取配置文件的解析对象.
	 */
	public static Digester getDigester()
	{
		return digester;
	}
	private static Digester digester;
	static
	{
		try
		{
			ResManager rm1 = new ResManager();
			rm1.load(Digester.class.getResourceAsStream(Digester.DEFAULT_RULES));
			Properties rConfig = new Properties();
			rConfig.load(Digester.class.getResourceAsStream(Digester.DEFAULT_CINFIG));
			ResManager rm2 = new ResManager();
			rm2.load(DataBaseLock.class.getResourceAsStream("dbvm_rules.res"));
			digester = new Digester(new ResManager[]{rm1, rm2}, rConfig);
		}
		catch (Exception ex)
		{
			Utility.createLog("eterna.dbvm").error("Error in init dbvm rules", ex);
		}
	}

	/**
	 * 记录版本更新时的日志.
	 */
	public static void log(String msg, Throwable err)
	{
		if (err == null)
		{
			log.info(msg);
		}
		else
		{
			log.error(msg, err);
		}
	}

	/**
	 * 设置使用的日志对象.
	 */
	public static void setLog(Log log)
	{
		VersionManager.log = log;
	}
	private static Log log = Utility.createLog("eterna.dbvm");

}
