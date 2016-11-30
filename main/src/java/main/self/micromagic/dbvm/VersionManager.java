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
import self.micromagic.eterna.share.Factory;
import self.micromagic.eterna.share.FactoryContainer;
import self.micromagic.util.ResManager;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;
import self.micromagic.util.container.ThreadCache;
import self.micromagic.util.ref.StringRef;

/**
 * 数据库的版本管理者.
 */
public class VersionManager
{
	/**
	 * 操作状态-步骤开始.
	 */
	public static final String OPT_STATUS_BEGIN = "BEGIN";
	/**
	 * 操作状态-步骤完成.
	 */
	public static final String OPT_STATUS_DONE = "DONE";
	/**
	 * 操作状态-版本完成.
	 */
	public static final String OPT_STATUS_FINISH = "FINISH";

	/**
	 * 版本系统表的最高版本.
	 */
	private static final int MAX_ETERNA_VERSION = 3;

	/**
	 * 没有版本表的版本值.
	 */
	private static final int VERSION_VALUE_NONE = -1;
	/**
	 * 出错的版本值.
	 */
	private static final int VERSION_VALUE_ERROR = -2;

	/**
	 * 版本2.
	 */
	public static final int V2 = 2;

	/**
	 * 数据库锁的等待时间, 37秒.
	 */
	static final long FLUSH_LOCK_GAP = 37 * 1000L;
	/**
	 * 需要锁定时间的延长值(21hour).
	 */
	private static final long BEGIN_LOCK_TIME_PLUS = 21 * 3600 * 1000L;


	/**
	 * 用于数据库版本升级的锁.
	 */
	static final String VERSION_LOCK_NAME = "eterna.version.lock";

	/**
	 * 版本系统表的版本标识.
	 */
	private static final String ETERNA_VERSION_TABLE = "__eterna_version_table";

	/**
	 * 当前版本系统表的版本.
	 */
	private int currentEternaVersion;

	/**
	 * 检查连接所对应的数据库版本, 如果未达到要求则进行版本升级.
	 *
	 * @param conn         数据库连接, 注: 执行完毕后此连接会被关闭
	 * @param packagePath  版本信息所在的包路径
	 * @param loader       版本信息所属的classloader
	 */
	public static boolean checkVersion(Connection conn, String packagePath, ClassLoader loader)
	{
		return instance.doCheck(conn, packagePath, loader);
	}
	private static VersionManager instance = new VersionManager();

	/**
	 * 检查连接所对应的数据库版本, 如果未达到要求则进行版本升级.
	 *
	 * @param conn         数据库连接, 注: 执行完毕后此连接会被关闭
	 * @param packagePath  版本信息所在的包路径
	 * @param loader       版本信息所属的classloader
	 */
	public boolean doCheck(Connection conn, String packagePath, ClassLoader loader)
	{
		ConfigResource res = ContainerManager.createClassPathResource(packagePath, loader);
		return this.doCheck(conn, res, loader);
	}

	/**
	 * 检查连接所对应的数据库版本, 如果未达到要求则进行版本升级.
	 *
	 * @param conn    数据库连接, 注: 执行完毕后此连接会被关闭
	 * @param res     版本信息所在的配置资源路径
	 * @param loader  版本信息所属的classloader
	 */
	public boolean doCheck(Connection conn, ConfigResource res, ClassLoader loader)
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
		boolean result = false;
		try
		{
			conn.setAutoCommit(false);
			DataBaseLocker.lock(conn, VERSION_LOCK_NAME, null, true, FLUSH_LOCK_GAP);
			synchronized (VersionManager.class)
			{
				result = this.checkVersion0(conn, res, loader);
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
				DataBaseLocker.releaseDB(conn, VERSION_LOCK_NAME);
				conn.close();
			}
			catch (Exception ex) {}
		}
		return result;
	}

	private boolean checkVersion0(Connection conn, ConfigResource res, ClassLoader loader)
			throws Exception
	{
		String vName = StringTool.isEmpty(this.versionNamePrefix) ? res.getName()
				: this.versionNamePrefix.concat(res.getName());
		String dbName = conn.getMetaData().getDatabaseProductName();
		Factory f = DataBaseLocker.getFactory(dbName);
		int version = getVersionValue(conn, vName, f);
		if (version == VERSION_VALUE_ERROR)
		{
			// 版本有错误直接退出
			return false;
		}
		if (version == VERSION_VALUE_NONE)
		{
			// 没有版本表, 需要创建版本表
			this.upperVersion(conn, DataBaseLocker.CONFIG_PREFIX + "impl/version1.xml",
					ETERNA_VERSION_TABLE, 1, 0, VersionManager.class.getClassLoader());
			version = 0;
		}
		int tmpEternaVersion = getVersionValue(conn, ETERNA_VERSION_TABLE, f);
		this.currentEternaVersion = tmpEternaVersion;
		if (tmpEternaVersion < MAX_ETERNA_VERSION)
		{
			if (tmpEternaVersion == VERSION_VALUE_ERROR)
			{
				log.error("Version tables has error.");
				return false;
			}
			// 版本表的版本小于目前系统定义的最大版本, 需要升级
			ClassLoader tmpLoader = VersionManager.class.getClassLoader();
			ConfigResource tmp = ContainerManager.createClassPathResource(
					DataBaseLocker.CONFIG_PREFIX + "impl/", tmpLoader);
			this.checkVersion0(f, conn, tmpEternaVersion + 1, ETERNA_VERSION_TABLE,
					tmp, tmpLoader, true);
		}
		this.checkVersion0(f, conn, version + 1, vName, res, loader, false);
		return true;
	}
	private void checkVersion0(Factory factory, Connection conn, int version, String vName,
			ConfigResource res, ClassLoader loader, boolean eternaVersion)
			throws Exception
	{
		int beginStep = this.getStepIndex(conn, vName, version, factory);
		if (beginStep > 0)
		{
			// 存在未执行的步骤, 需要降一个版本
			version--;
		}
		// 循环执行更新到最新版本
		String versionFile = "version".concat(Integer.toString(version).concat(".xml"));
		ConfigResource vRes = res.getResource(versionFile);
		InputStream tmpStream = vRes.getAsStream();
		while (tmpStream != null)
		{
			try
			{
				tmpStream.close();
			}
			catch (IOException ex) {}
			this.upperVersion(conn, vRes.getConfig(), vName, version, beginStep, loader);
			if (eternaVersion)
			{
				this.currentEternaVersion = version;
			}
			version++;
			beginStep = 0;
			versionFile = "version".concat(Integer.toString(version).concat(".xml"));
			vRes = res.getResource(versionFile);
			tmpStream = vRes.getAsStream();
		}
	}
	private void upperVersion(Connection conn, String config, String vName,
			int version, int beginStep, ClassLoader loader)
			throws Exception
	{
		if (log.isInfoEnabled())
		{
			log.info("Begin " + vName + " up to " + version + ". --------------------");
		}
		String dbName = conn.getMetaData().getDatabaseProductName();
		FactoryContainer share = DataBaseLocker.getContainer(dbName);
		FactoryContainer c = ContainerManager.createFactoryContainer("v" + version,
				config, null, getDigester(), null, loader, share, false);
		IgnoreConfig.clearCurrentConfig();
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
			success = this.upperVersion0(conn, config, vName, version, beginStep, loader, f);
		}
		catch (Exception ex)
		{
			errMsg = ex.getMessage();
			throw ex;
		}
		finally
		{
			c.destroy();
			if (success)
			{
				if (log.isInfoEnabled())
				{
					log.info("End   " + vName + " up to " + version + ". --------------------");
				}
			}
			else
			{
				log.warn("Error " + vName + " up to " + version + ". --------------------");
				if (StringTool.isEmpty(errMsg))
				{
					errMsg = "Other error.";
				}
			}
			this.addVersionLog(conn, vName, version, errMsg, f);
		}
	}
	private boolean upperVersion0(Connection conn, String config, String vName,
			int version, int beginStep, ClassLoader loader, Factory factory)
			throws Exception
	{
		int index = beginStep > 0 ? beginStep - 1 : 0;
		int count = factory.getObjectCount();
		Update insert = (Update) factory.createObject("addStepScript");
		long now = System.currentTimeMillis();
		long timeFix = DataBaseLocker.getDataBaseTime(conn).getTime() - now;
		DataBaseLocker.flushLockTime(conn, VERSION_LOCK_NAME, now + timeFix);
		Connection checkConn = this.getLockTimeFlushConnection();
		LockTimeChecker checker = null;
		if (checkConn != null)
		{
			checker = LockTimeChecker.getCurrentChecker(true, timeFix, checkConn, conn);
		}
		try
		{
			for (; index < count; index++)
			{
				Object obj;
				try
				{
					obj = factory.createObject(index);
				}
				catch (EternaException ex)
				{
					// 如果中间有对象不存在则继续下一个
					continue;
				}
				if (obj instanceof OptDesc)
				{
					int step = index + 1;
					try
					{
						insert.setString("versionName", vName);
						insert.setObject("versionValue", Utility.createInteger(version));
						insert.setObject("step", Utility.createInteger(step));
						CommonUpdate.initThreadInfo(insert, this.currentEternaVersion, timeFix);
						this.setStepInfo(conn, factory, vName, version, step,
								OPT_STATUS_BEGIN, timeFix);
						((OptDesc) obj).exec(conn);
						this.setStepInfo(conn, factory, vName, version, step,
								OPT_STATUS_DONE, timeFix);
					}
					catch (Exception ex)
					{
						boolean ignore = this.addStepError(conn, vName, version, step, ex,
								factory, (OptDesc) obj, timeFix);
						if (ignore)
						{
							this.setStepInfo(conn, factory, vName, version, step,
									OPT_STATUS_DONE, timeFix);
						}
						else
						{
							String msg = "Error in [" + vName + "]'s version ["
									+ version + "] step [" + step + "].";
							log.error(msg, ex);
							throw ex;
						}
					}
				}
			}
		}
		finally
		{
			CommonUpdate.clearThreadInfo();
			if (checker != null)
			{
				checker.finish();
			}
		}
		this.setStepInfo(conn, factory, vName, version, -1, OPT_STATUS_FINISH, 0L);
		return true;
	}

	/**
	 * 添加步骤的出错信息.
	 */
	private boolean addStepError(Connection conn, String vName, int version, int step,
			Throwable error, Factory factory, OptDesc opt, long timeFix)
			throws SQLException
	{
		if (this.currentEternaVersion <= V2)
		{
			return false;
		}
		boolean ignore = opt.isIgnoreError(error);
		Update insert = (Update) factory.createObject("addStepError");
		insert.setString("versionName", vName);
		insert.setObject("versionValue", Utility.createInteger(version));
		insert.setObject("step", Utility.createInteger(step));
		insert.setString("optContent", opt.getElement().asXML());
		StringAppender buf = StringTool.createStringAppender(128);
		buf.append(ignore ? "WARN: " : "ERROR: ").append(error.getClass())
				.appendln().append(error.getMessage());
		insert.setString("optMessage", buf.toString());
		insert.setString("optUser", DataBaseLocker.getRuntimeName());
		insert.executeUpdate(conn);
		if (!ignore)
		{
			flushLockTime(OPT_STATUS_DONE, timeFix, conn);
			conn.commit();
		}
		return ignore;
	}

	private void setStepInfo(Connection conn,  Factory factory, String vName, int version,
			int step, String optStatus, long timeFix)
			throws SQLException
	{
		if (this.currentEternaVersion <= V2)
		{
			return;
		}
		Update u = (Update) factory.createObject("setStepInfo");
		u.setObject("step", step == -1 ? null : Utility.createInteger(step));
		u.setString("optStatus", optStatus);
		u.setString("versionName", vName);
		u.setObject("versionValue", Utility.createInteger(version));
		u.execute(conn);
		if (optStatus == OPT_STATUS_BEGIN || optStatus == OPT_STATUS_DONE)
		{
			u = (Update) factory.createObject("clearStepScript");
			u.setString("versionName", vName);
			u.executeUpdate(conn);
			if (!flushLockTime(optStatus, timeFix, conn))
			{
				log.error("Flush lock time error for version [" + vName + "].");
			}
		}
		conn.commit();
	}

	private static boolean flushLockTime(String optStatus, long timeFix, Connection conn)
	{
		boolean result;
		LockTimeChecker checker = LockTimeChecker.getCurrentChecker(
				false, timeFix, null, null);
		if (checker != null)
		{
			result = true;
		}
		else if (optStatus == OPT_STATUS_BEGIN)
		{
			// 步骤起始时将锁定时间延长, 这样脚本执行时间过长也不会有问题
			long newTime = System.currentTimeMillis() + timeFix + BEGIN_LOCK_TIME_PLUS;
			result = DataBaseLocker.flushLockTime(conn, VERSION_LOCK_NAME, newTime);
		}
		else
		{
			// 步骤结束时将锁定时间变为当前时间
			long newTime = System.currentTimeMillis() + timeFix;
			result = DataBaseLocker.flushLockTime(conn, VERSION_LOCK_NAME, newTime);
		}
		return result;
	}

	/**
	 * 获取当前应该执行哪一步.
	 */
	private int getStepIndex(Connection conn, String vName, int version, Factory factory)
			throws SQLException
	{
		if (this.currentEternaVersion <= V2)
		{
			return 0;
		}
		Query q = (Query) factory.createObject("getVersionInfo");
		q.setString("versionName", vName);
		ResultIterator ritr = q.executeQuery(conn);
		if (ritr.hasNext())
		{
			ResultRow row = ritr.nextRow();
			int step = row.getInt("step");
			String optStatus = row.getString("optStatus");
			return OPT_STATUS_DONE.equalsIgnoreCase(optStatus) ? step + 1 : step;
		}
		else
		{
			// 版本记录不存在, 则需要添加
			Update u = (Update) factory.createObject("addStepInfo");
			u.setString("versionName", vName);
			u.setObject("versionValue", Utility.createInteger(version));
			u.executeUpdate(conn);
		}
		return 0;
	}

	/**
	 * 获取版本值.
	 */
	private static int getVersionValue(Connection conn, String vName, Factory factory)
	{
		Query q = (Query) factory.createObject("getVersionValue");
		int version = VERSION_VALUE_NONE;
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
					return VERSION_VALUE_ERROR;
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
	private void addVersionLog(Connection conn, String vName, int version,
			String errInfo, Factory factory)
			throws SQLException
	{
		Integer v = Utility.createInteger(version);
		Update u;
		if (version == 1 && this.currentEternaVersion <= V2)
		{
			u = (Update) factory.createObject("addVersionValue");
		}
		else
		{
			u = (Update) factory.createObject("setVersionValue");
		}
		if (this.currentEternaVersion <= V2 || errInfo != null)
		{
			// 大于2版本时, 版本信息已记录, 只有出错时才需要修改
			u.setString("versionName", vName);
			u.setString("errInfo", errInfo);
			u.setObject("versionValue", v);
			u.executeUpdate(conn);
		}
		u = (Update) factory.createObject("addVersionLog");
		u.setString("versionName", vName);
		u.setString("errInfo", errInfo);
		u.setObject("versionValue", v);
		u.executeUpdate(conn);
		conn.commit();
	}

	/**
	 * 获取版本名称的前缀.
	 */
	public String getVersionNamePrefix()
	{
		return this.versionNamePrefix;
	}

	/**
	 * 设置版本名称的前缀.
	 */
	public void setVersionNamePrefix(String prefix)
	{
		this.versionNamePrefix = prefix;
	}
	private String versionNamePrefix;

	/**
	 * 获取刷新锁定时间的数据库连接.
	 */
	public Connection getLockTimeFlushConnection()
	{
		return this.lockTimeFlushConn;
	}

	/**
	 * 设置刷新锁定时间的数据库连接.
	 * 此连接需要在版本升级完成后自行关闭.
	 */
	public void setLockTimeFlushConnection(Connection conn)
	{
		this.lockTimeFlushConn = conn;
	}
	private Connection lockTimeFlushConn;

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
			rm2.load(DataBaseLocker.class.getResourceAsStream("dbvm_rules.res"));
			digester = new Digester(new ResManager[]{rm1, rm2}, rConfig);
		}
		catch (Exception ex)
		{
			log.error("Error in init dbvm rules", ex);
		}
	}

}

/**
 * 数据库锁时间的检查线程.
 */
class LockTimeChecker
		implements Runnable
{
	/**
	 * 线程缓存中放置此对象的名称.
	 */
	private static final String THREAD_CACHE_FLAG = "eterna.lockTimeChecker";

	/**
	 * 检查的间隔.
	 */
	private static final long CHECK_TIME_GAP = VersionManager.FLUSH_LOCK_GAP / 3;

	/**
	 * 检查线程结束的延迟时间(20second).
	 */
	private static final long FINISH_DELAY = 20 * 1000L;

	// 是否正在运行
	private boolean running = true;
	// 前一次锁定的时间
	private long preLockTime;
	// 结束检查线程的时间
	private long finishTime;
	// 时间偏移修复值
	private final long timeFix;
	// 刷新锁定时间的数据库连接
	private Connection conn;
	// 加锁的数据库连接
	private Connection lockConn;

	public LockTimeChecker(long timeFix)
	{
		this.timeFix = timeFix;
	}

	/**
	 * 获取当前的检查对象.
	 */
	public static LockTimeChecker getCurrentChecker(boolean create, long timeFix,
			Connection conn, Connection lockConn)
	{
		ThreadCache cache = ThreadCache.getInstance();
		LockTimeChecker tmp = (LockTimeChecker) cache.getProperty(THREAD_CACHE_FLAG);
		if (create && conn != null && lockConn != null && conn != lockConn)
		{
			if (tmp == null)
			{
				tmp = createChecker(cache, timeFix, conn, lockConn);
			}
			else
			{
				synchronized (tmp)
				{
					if (!tmp.running)
					{
						tmp = createChecker(cache, timeFix, conn, lockConn);
					}
					else
					{
						tmp.resetLockTime();
						tmp.initConn(conn, lockConn);
					}
				}
			}
		}
		return tmp != null && tmp.running ? tmp : null;
	}

	/**
	 * 创建检查对象.
	 */
	private static LockTimeChecker createChecker(ThreadCache cache, long timeFix,
			Connection conn, Connection lockConn)
	{
		LockTimeChecker tmp = new LockTimeChecker(timeFix);
		tmp.initConn(conn, lockConn);
		tmp.resetLockTime();
		Thread thread = new Thread(tmp, "eterna_dbvm_lockTimeChecker_" + tmp.hashCode());
		thread.start();
		cache.setProperty(THREAD_CACHE_FLAG, tmp);
		return tmp;
	}

	/**
	 * 初始化数据库连接.
	 */
	private void initConn(Connection conn, Connection lockConn)
	{
		this.conn = conn;
		this.lockConn = lockConn;
	}

	/**
	 * 重置锁定的时间.
	 */
	public synchronized void resetLockTime()
	{
		this.preLockTime = System.currentTimeMillis();
		this.finishTime = this.preLockTime + FINISH_DELAY;
	}

	/**
	 * 暂时结束检查.
	 */
	public synchronized void finish()
	{
		this.preLockTime = 0L;
		this.finishTime = System.currentTimeMillis() + FINISH_DELAY;
	}

	public void run()
	{
		while (this.running)
		{
			if (this.preLockTime > 0L)
			{
				long waitTime = CHECK_TIME_GAP + this.preLockTime
						- System.currentTimeMillis();
				if (waitTime > 10L)
				{
					doSleep(waitTime);
				}
				else
				{
					try
					{
						this.doLock();
					}
					finally
					{
						this.resetLockTime();
					}
				}
			}
			else
			{
				long now = System.currentTimeMillis();
				if (this.finishTime < now)
				{
					synchronized (this)
					{
						if (this.finishTime < now)
						{
							this.running = false;
						}
					}
				}
				else
				{
					doSleep(CHECK_TIME_GAP);
				}
			}
		}
	}

	private void doLock()
	{
		long begin = System.currentTimeMillis();
		long newTime = begin + this.timeFix;
		DataBaseLocker.flushLockTime(this.conn,
				VersionManager.VERSION_LOCK_NAME, newTime, this.lockConn);
		if (System.currentTimeMillis() - begin > 1000L)
		{
			// 如果执行时间大于1s, 则再执行一次
			newTime = System.currentTimeMillis() + this.timeFix;
			DataBaseLocker.flushLockTime(this.conn,
					VersionManager.VERSION_LOCK_NAME, newTime, this.lockConn);
		}
		try
		{
			this.conn.commit();
		}
		catch (Exception ex) {}
	}

	private static void doSleep(long time)
	{
		try
		{
			Thread.sleep(time);
		}
		catch (InterruptedException ex) {}
	}

}
