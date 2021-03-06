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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
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
import self.micromagic.util.FormatTool;
import self.micromagic.util.StringTool;
import self.micromagic.util.ref.StringRef;

/**
 * 数据库锁.
 */
public class DataBaseLocker
{
	/**
	 * oracle数据库名称.
	 */
	public static final String DB_NAME_ORACLE = "Oracle";

	/**
	 * H2数据库名称.
	 */
	public static final String DB_NAME_H2 = "H2";

	/**
	 * mysql数据库名称.
	 */
	public static final String DB_NAME_MYSQL = "MySQL";

	/**
	 * PostgreSQL数据库名称.
	 */
	public static final String DB_NAME_POSTGRES = "PostgreSQL";

	/**
	 * 配置文件的前缀.
	 */
	public static final String CONFIG_PREFIX = "cp:/self/micromagic/dbvm/";

	/**
	 * 数据库名称的索引表.
	 * 奇数位为key, 偶数位为对应的名称.
	 */
	private static final String[] DB_NAME_INDEX = {
		"ORACLE", DB_NAME_ORACLE,
		"POSTGRESQL", DB_NAME_POSTGRES,
		"H2", DB_NAME_H2,
		"MYSQL", DB_NAME_MYSQL
	};

	/**
	 * 各类型数据库相关的工厂容器的缓存.
	 */
	private static Map containerCache = new HashMap();

	/**
	 * 锁已被释放的值.
	 */
	private static final String RELEASED_VALUE = "<released>";

	/**
	 * 默认的最大等待时间, 15min.
	 */
	private static long defaultMaxWaitTime =  15 * 60 * 1000L;

	// 存放已加锁的数据库连接, 用于生成id
	private static Reference[] connsInLock = new Reference[8];
	// 存放已数组中已使用的数量
	private static int connsInLockUsedCount;

	// 运行时名称
	private static String runtimeName = getRuntimeName0();

	private DataBaseLocker()
	{
	}

	/**
	 * 根据数据库连接获取数据库产品的名称.
	 */
	public static String getDataBaseProductName(Connection conn)
			throws SQLException
	{
		return conn.getMetaData().getDatabaseProductName();
	}

	/**
	 * 获取标准的数据库名称.
	 */
	public static String getStandardDataBaseName(String name)
	{
		if (StringTool.isEmpty(name))
		{
			return null;
		}
		String upName = name.toUpperCase();
		for (int i = 0; i < DB_NAME_INDEX.length; i += 2)
		{
			if (upName.equals(DB_NAME_INDEX[i]))
			{
				return DB_NAME_INDEX[i + 1];
			}
		}
		return null;
	}

	/**
	 * 通过一个数据库连接获取数据库时间.
	 */
	public static java.sql.Timestamp getDataBaseTime(Connection conn)
	{
		try
		{
			String dbName = getDataBaseProductName(conn);
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
	 * 释放连接所对应的数据库. <p>
	 * 注: 释放锁时会提交事务.
	 *
	 * @param conn      数据库连接
	 * @param lockName  数据库锁的名称
	 */
	public static void releaseDB(Connection conn, String lockName)
			throws SQLException, EternaException
	{
		String dbName = getDataBaseProductName(conn);
		EternaFactory f = getFactory(dbName);
		Update modify = f.createUpdate("modifyLockInfo");
		modify.setString("lockValue1", RELEASED_VALUE);
		modify.setString("lockName", lockName);
		String nowLockValue = makeLockValue(conn);
		modify.setString("lockValue2", nowLockValue);
		modify.setIgnore("userId");
		modify.executeUpdate(conn);
		conn.commit();
		synchronized (DataBaseLocker.class)
		{
			for (int i = connsInLockUsedCount - 1; i >= 0; i--)
			{
				Reference ref = connsInLock[i];
				if ((ref == null || ref.get() == null) && i == connsInLockUsedCount - 1)
				{
					// 如果在末尾的已被清空, 则使用值-1
					connsInLock[i] = null;
					connsInLockUsedCount--;
				}
				if (ref != null && ref.get() == conn)
				{
					connsInLock[i] = null;
					if (i == connsInLockUsedCount - 1)
					{
						// 如果释放的在末尾, 则使用值-1
						connsInLockUsedCount--;
					}
					break;
				}
			}
		}
	}

	/**
	 * 刷新锁数据库的时间.
	 *
	 * @param conn      数据库连接
	 * @param lockName  数据库锁的名称
	 * @return  刷新时间是否成功
	 */
	public static boolean flushLockTime(Connection conn, String lockName)
	{
		try
		{
			String dbName = getDataBaseProductName(conn);
			EternaFactory f = getFactory(dbName);
			Update modify = f.createUpdate("modifyLockInfo");
			String nowLockValue = makeLockValue(conn);
			modify.setString("lockValue2", nowLockValue);
			modify.setString("lockName", lockName);
			modify.setIgnore("userId");
			modify.setIgnore("lockValue1");
			return modify.executeUpdate(conn) > 0;
		}
		catch (Exception ex)
		{
			return false;
		}
	}

	/**
	 * 刷新锁数据库的时间.
	 *
	 * @param conn      数据库连接
	 * @param lockName  数据库锁的名称
	 * @param time      数据库锁需要刷新到什么时间
	 * @return  刷新时间是否成功
	 */
	public static boolean flushLockTime(Connection conn, String lockName, long time)
	{
		return flushLockTime(conn, lockName, time, conn);
	}

	/**
	 * 刷新锁数据库的时间.
	 *
	 * @param conn      执行刷新的数据库连接
	 * @param lockName  数据库锁的名称
	 * @param time      数据库锁需要刷新到什么时间
	 * @param lockConn  加锁的数据库连接
	 * @return  刷新时间是否成功
	 */
	static boolean flushLockTime(Connection conn, String lockName, long time,
			Connection lockConn)
	{
		try
		{
			String dbName = getDataBaseProductName(conn);
			EternaFactory f = getFactory(dbName);
			Update modify = f.createUpdate("flushLockTime");
			String nowLockValue = makeLockValue(lockConn);
			modify.setString("lockValue", nowLockValue);
			modify.setString("lockName", lockName);
			modify.setObject("lockTime", new java.sql.Timestamp(time));
			String msg = "Flush lock [" + lockName + "]=[" + nowLockValue + "]'s time to ["
					+ FormatTool.formatFullDate(new Long(time)) + "].";
			VersionManager.log(msg, null);
			return modify.executeUpdate(conn) > 0;
		}
		catch (Exception ex)
		{
			return false;
		}
	}

	/**
	 * 锁住连接所对应的数据库. <p>
	 * 注: 同一个名称的锁不能同时用在事务锁和跨事务锁上.
	 *
	 * @param conn             数据库连接
	 * @param lockName         数据库锁的名称
	 * @param userId           当前登录的用户名
	 */
	public static void lock(Connection conn, String lockName, String userId)
			throws SQLException, EternaException
	{
		String dbName = getDataBaseProductName(conn);
		EternaFactory f = getFactory(dbName);
		Update modify = f.createUpdate("modifyLockInfo");
		modify.setString("userId", userId);
		modify.setIgnore("lockValue1");
		modify.setIgnore("lockValue2");
		modify.setString("lockName", lockName);
		if (!tryGetLock(conn, modify, null))
		{
			// 如果没有获取到锁, 那可能记录或表不存在
			lock(conn, lockName, userId, false);
		}
	}

	/**
	 * 锁住连接所对应的数据库. <p>
	 * 注: 如果是跨事务的锁, 可能会提交或回滚事务.
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
		lock(conn, lockName, userId, overTransaction, defaultMaxWaitTime);
	}

	/**
	 * 锁住连接所对应的数据库. <p>
	 * 注: 如果是跨事务的锁, 可能会提交或回滚事务.
	 *
	 * @param conn             数据库连接
	 * @param lockName         数据库锁的名称
	 * @param userId           当前登录的用户名
	 * @param overTransaction  是否要添加一个跨事务的锁
	 * @param maxWaitTime      强行获取锁的最大等待时间(毫秒)
	 */
	public static void lock(Connection conn, String lockName, String userId,
			boolean overTransaction, long maxWaitTime)
			throws SQLException, EternaException
	{
		String dbName = getDataBaseProductName(conn);
		EternaFactory f = getFactory(dbName);
		Query query = f.createQuery("getLockInfo");
		query.setString("lockName", lockName);
		Update modify = f.createUpdate("modifyLockInfo");
		String nowLockValue = makeLockValue(conn);
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
					update.setString("lockValue",
							overTransaction ? nowLockValue : RELEASED_VALUE);
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
				long timpMargin = row == null ? 0L
						: row.getLong("nowTime") - row.getLong("lockTime");
				if (timpMargin > maxWaitTime)
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

	/**
	 * 创建数据库锁的值.
	 */
	private static String makeLockValue(Connection conn)
	{
		int index = -1;
		synchronized (DataBaseLocker.class)
		{
			int emptyIndex = -1;
			for (int i = 0; i < connsInLockUsedCount; i++)
			{
				Reference ref = connsInLock[i];
				if (ref != null && ref.get() == conn)
				{
					// 找到匹配的conn
					index = i;
					break;
				}
				else if (emptyIndex == -1 && (ref == null || ref.get() == null))
				{
					// 找到空项
					emptyIndex = i;
				}
			}
			if (index == -1)
			{
				if (emptyIndex == -1 && connsInLockUsedCount < connsInLock.length)
				{
					// 未找到匹配项或空项
					emptyIndex = connsInLockUsedCount++;
				}
				if (emptyIndex != -1)
				{
					connsInLock[emptyIndex] = new WeakReference(conn);
					index = emptyIndex;
				}
				else
				{
					// 数组长度不够需要扩展
					int count = connsInLock.length;
					Reference[] arr = new Reference[count + 2];
					System.arraycopy(connsInLock, 0, arr, 0, count);
					connsInLock = arr;
					index = count;
					connsInLockUsedCount++;
					connsInLock[index] = new WeakReference(conn);
				}
			}
		}
		return getRuntimeName().concat(":".concat(Integer.toString(index)));
	}

	static FactoryContainer getContainer(String dbName, FactoryContainer share)
	{
		if (share != null)
		{
			// 有共享工厂容器时不能从缓存中取
			return getContainer0(dbName, share);
		}
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
			c = getContainer0(dbName, share);
			containerCache.put(dbName, c);
			return c;
		}
	}

	private static FactoryContainer getContainer0(String dbName, FactoryContainer share)
	{
		ClassLoader loader = DataBaseLocker.class.getClassLoader();
		String config = CONFIG_PREFIX + "def_" + dbName + ".xml;"
				+ CONFIG_PREFIX + "core/db_lock.xml;"
				+ CONFIG_PREFIX + "core/db_common.xml;";
		if (share == null)
		{
			share = ContainerManager.getGlobalContainer();
		}
		FactoryContainer c = ContainerManager.createFactoryContainer(dbName, config, null,
				VersionManager.getDigester(), null, loader, share, false);
		StringRef msg = new StringRef();
		c.reInit(msg);
		if (!StringTool.isEmpty(msg.getString()))
		{
			throw new EternaException(msg.getString());
		}
		return c;
	}

	static EternaFactory getFactory(String dbName)
			throws EternaException
	{
		return (EternaFactory) getContainer(dbName, null).getFactory();
	}

	/**
	 * 设置强行获取锁的默认最大等待时间.
	 */
	public static void setMaxWaitTime(long time)
	{
		if (time < 1000L)
		{
			return;
		}
		defaultMaxWaitTime = time;
	}

	/**
	 * 获取当前运行环境的名称.
	 */
	public static String getRuntimeName()
	{
		return runtimeName;
	}
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
			name += "@" + address;
		}
		catch (Throwable ex)
		{
			name = (new UID()) + "@" + address;
		}
		return name;
	}

}
