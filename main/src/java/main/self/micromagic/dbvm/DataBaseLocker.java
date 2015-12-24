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

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.rmi.server.UID;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import self.micromagic.eterna.dao.Query;
import self.micromagic.eterna.dao.ResultIterator;
import self.micromagic.eterna.dao.ResultRow;
import self.micromagic.eterna.dao.Update;
import self.micromagic.eterna.digester2.ContainerManager;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.FactoryContainer;
import self.micromagic.util.StringTool;
import self.micromagic.util.ref.StringRef;

/**
 * 数据库锁.
 */
public class DataBaseLocker
{
	/**
	 * 通过一个数据库连接获取数据库时间.
	 */
	public static java.sql.Timestamp getDataBaseTime(Connection conn)
	{
		try
		{
			String dbName = conn.getMetaData().getDatabaseProductName();
			EternaFactory f = getFactory(dbName);
			Query q = f.createQuery("getDataBaseTime");
			return q.executeQuery(conn).nextRow().getTimestamp(1);
		}
		catch (SQLException ex)
		{
			throw new EternaException(ex);
		}
	}

	/**
	 * 释放连接所对应的数据库.
	 *
	 * @param conn      数据库连接
	 * @param lockName  数据库锁的名称
	 */
	public static void releaseDB(Connection conn, String lockName)
			throws SQLException, EternaException
	{
		String dbName = conn.getMetaData().getDatabaseProductName();
		EternaFactory f = getFactory(dbName);
		Update modify = f.createUpdate("modifyLockInfo");
		modify.setString("lockValue1", RELEASED_VALUE);
		modify.setString("lockName", lockName);
		String nowLockValue = getRuntimeName() + ":" + conn.hashCode();
		modify.setString("lockValue2", nowLockValue);
		modify.setIgnore("userId");
		modify.executeUpdate(conn);
		conn.commit();
	}

	/**
	 * 锁住连接所对应的数据库.
	 *
	 * @param conn             数据库连接
	 * @param lockName         数据库锁的名称
	 * @param userId           当前登录的用户名
	 */
	public static void lock(Connection conn, String lockName, String userId)
			throws SQLException, EternaException
	{
		String dbName = conn.getMetaData().getDatabaseProductName();
		EternaFactory f = getFactory(dbName);
		Update modify = f.createUpdate("modifyLockInfo");
		modify.setString("userId", userId);
		modify.setIgnore("lockValue1");
		modify.setIgnore("lockValue2");
		modify.setString("lockName", lockName);
		if (tryGetLock(conn, modify, null))
		{
			// 如果没有获取到锁, 那可能记录或表不存在
			lock(conn, lockName, userId, false);
		}
	}

	/**
	 * 锁住连接所对应的数据库.
	 *
	 * @param conn             数据库连接
	 * @param lockName         数据库锁的名称
	 * @param userId           当前登录的用户名
	 * @param overTransaction  是否要添加一个跨事务的锁
	 */
	public static void lock(Connection conn, String lockName, String userId,
			boolean overTransaction)
			throws SQLException, EternaException
	{
		String dbName = conn.getMetaData().getDatabaseProductName();
		EternaFactory f = getFactory(dbName);
		Query query = f.createQuery("getLockInfo");
		query.setString("lockName", lockName);
		Update modify = f.createUpdate("modifyLockInfo");
		String nowLockValue = getRuntimeName() + ":" + conn.hashCode();
		modify.setString("userId", userId);
		if (overTransaction)
		{
			modify.setString("lockValue1", nowLockValue);
		}
		else
		{
			modify.setIgnore("lockValue1");
			modify.setIgnore("lockValue2");
		}
		modify.setString("lockName", lockName);
		boolean gettedLock = false;
		while (!gettedLock)
		{
			String lockValue = null;
			boolean needSleep = false;
			ResultRow row = null;
			try
			{
				ResultIterator ritr = query.executeQuery(conn);
				if (ritr.hasNext())
				{
					row = ritr.nextRow();
					lockValue = row.getString("lockValue");
				}
				else
				{
					lockValue = "";
				}
			}
			catch (Exception ex) {}
			if (lockValue == null)
			{
				// lockValue为null, 说明表不存在, 创建表, 然后重新执行查询
				try
				{
					Update update = f.createUpdate("createLockTable");
					update.execute(conn);
					conn.commit();
				}
				catch (Exception ex)
				{
					// 如果出现错误, 说明表已创建, 需要等待一段时间
					needSleep = true;
				}
			}
			else if ("".equals(lockValue))
			{
				// lockValue为"", 说明没有记录, 需要添加, 然后重新执行查询
				try
				{
					Update update = f.createUpdate("addLockInfo");
					update.setString("lockName", lockName);
					update.setString("lockValue", nowLockValue);
					update.setString("userId", userId);
					if (update.executeUpdate(conn) == 0)
					{
						// 未能插入记录, 需要等待
						needSleep = true;
					}
				}
				catch (Exception ex)
				{
					// 插入记录出错, 需要等待
					needSleep = true;
				}
			}
			else if (!overTransaction || RELEASED_VALUE.equals(lockValue))
			{
				// 不需要跨事务 或 lockValue为未锁, 可以对其进行锁操作
				if (tryGetLock(conn, modify, overTransaction ? RELEASED_VALUE : null))
				{
					gettedLock = true;
				}
				else
				{
					// 未更新到记录, 则等待并重新查询
					needSleep = true;
				}
			}
			else if (nowLockValue.equals(lockValue))
			{
				gettedLock = true;
			}
			else
			{
				if (row != null && row.getLong("nowTime") - row.getLong("lockTime") > maxWaitTime)
				{
					// 如果超过了最长等待时间, 则强制获取锁
					if (tryGetLock(conn, modify, lockValue))
					{
						gettedLock = true;
					}
					else
					{
						// 未更新到记录, 则等待并重新查询
						needSleep = true;
					}
				}
				else
				{
					// 时间未到, 则等待并重新查询
					needSleep = true;
				}
			}
			if (needSleep)
			{
				if (overTransaction)
				{
					// 如果是要跨事务的锁, sleep前要回滚
					conn.rollback();
				}
				try
				{
					Thread.sleep(73L);
				}
				catch (InterruptedException ex) {}
			}
		}
	}
	/**
	 * 尝试获取锁.
	 */
	private static boolean tryGetLock(Connection conn, Update modify, String lockValue)
	{
		try
		{
			if (lockValue != null)
			{
				modify.setString("lockValue2", lockValue);
			}
			return modify.executeUpdate(conn) == 0 ? false : true;
		}
		catch (Exception ex)
		{
			return false;
		}
	}

	static FactoryContainer getContainer(String dbName)
	{
		FactoryContainer c = (FactoryContainer) containerCache.get(dbName);
		if (c != null)
		{
			return c;
		}
		synchronized (containerCache)
		{
			c = (FactoryContainer) containerCache.get(dbName);
			if (c != null)
			{
				return c;
			}
			ClassLoader loader = DataBaseLocker.class.getClassLoader();
			String config = CONFIG_PREFIX + "def_" + dbName + ".xml;"
					+ CONFIG_PREFIX + "db_lock.xml;" + CONFIG_PREFIX + "db_common.xml;";
			c = ContainerManager.createFactoryContainer(dbName, config, null,
					VersionManager.getDigester(), null, loader,
					ContainerManager.getGlobalContainer(), false);
			StringRef msg = new StringRef();
			c.reInit(msg);
			if (!StringTool.isEmpty(msg.getString()))
			{
				throw new EternaException(msg.getString());
			}
			containerCache.put(dbName, c);
			return c;
		}
	}

	static EternaFactory getFactory(String dbName)
			throws EternaException
	{

		return (EternaFactory) getContainer(dbName).getFactory();
	}

	/**
	 * 各类型数据库相关的工厂容器的缓存.
	 */
	private static Map containerCache = new HashMap();

	/**
	 * 配置文件的前缀.
	 */
	public static final String CONFIG_PREFIX = "cp:/self/micromagic/dbvm/";

	/**
	 * 锁已被释放的值.
	 */
	private static final String RELEASED_VALUE = "<released>";

	/**
	 * 最大等待时间, 15min.
	 */
	private static long maxWaitTime =  15 * 60 * 1000L;

	/**
	 * 设置获取锁的最大等待时间.
	 */
	public static void setMaxWaitTime(long time)
	{
		if (time < 1000L)
		{
			return;
		}
		maxWaitTime = time;
	}

	/**
	 * 获取当前运行环境的名称.
	 */
	public static String getRuntimeName()
	{
		return runtimeName;
	}
	private static String runtimeName = getRuntimeName0();
	private static String getRuntimeName0()
	{
		String name = null;
		String address = "localhost";
		try
		{
			address = InetAddress.getLocalHost().getHostAddress();
			Class c = Class.forName("java.lang.management.ManagementFactory");
			Method m = c.getMethod("getRuntimeMXBean", new Class[0]);
			Object runtime = m.invoke(null, new Object[0]);
			c = Class.forName("java.lang.management.RuntimeMXBean");
			m = c.getMethod("getName", new Class[0]);
			name = String.valueOf(m.invoke(runtime, new Object[0]));
			name += ":" + address;
		}
		catch (Throwable ex)
		{
			name = (new UID()) + ":" + address;
		}
		return name;
	}

}
