/*
 * Copyright 2009-2015 xinjunli (micromagic@sina.com).
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

package self.micromagic.util;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;

import self.micromagic.cg.ClassGenerator;
import self.micromagic.util.container.SynHashMap;
import self.micromagic.util.logging.LoggerListener;
import self.micromagic.util.logging.MemoryLogger;

/**
 * 配置说明:
 * 默认的日志配置有
 * self.micromagic.defaultLogger.console.off            是否关闭控制台输出
 * self.micromagic.defaultLogger.console.delay_time     控制台输出的刷新间隔(毫秒)
 * self.micromagic.defaultLogger.name                   默认日志的名称
 * self.micromagic.defaultLogger.file                   日志的文件名
 * self.micromagic.defaultLogger.file.size              日志的大小限制(单位KB)
 * self.micromagic.defaultLogger.file.count             日志的文件个数
 * self.micromagic.defaultLogger.level                  日志的屏蔽等级
 *
 * 其它的日志配置有
 * self.micromagic.logger.names
 * 其它日志的名称, 多个名称之间用","分割, 名称只能是: 字母 数字 "_" "-", 且名称不能为"default"
 *
 * self.micromagic.[name]Logger.file                   日志的文件名
 * self.micromagic.[name]Logger.file.size              日志的大小限制(单位KB)
 * self.micromagic.[name]Logger.file.count             日志的文件个数
 * self.micromagic.[name]Logger.level                  日志的屏蔽等级
 */
public class Jdk14Factory extends LogFactory
{
	public static final String USE_JDK_LOG_FLAG = "useJdkLog";

	private static ConsoleFlushTimer consoleFlushTimer;
	private static int consoleFlushDelay = 5000;
	private static StreamHandler consoleLoggerHander;
	private static Logger defaultLogger;
	private static Map otherLoggerMap = new SynHashMap();

	private final Map attributes = new SynHashMap();
	private final Map instances = new SynHashMap();

	public static final String EXCEPTION_LOG_PROPERTY = "self.micromagic.eterna.exception.logType";
	protected static int EXCEPTION_LOG_TYPE = 0;
	private static MemoryLogger memoryLogger = MemoryLogger.getInstance();
	private static LoggerListener[] loggerListeners = new LoggerListener[]{memoryLogger};

	/**
	 * 设置使用的MemoryLogger的名称.
	 */
	public static void setMemoryLogger(String name)
	{
		memoryLogger = MemoryLogger.getInstance(name);
		loggerListeners[0] = memoryLogger;
	}

	public static void printException(Writer out, boolean clear)
			throws IOException
	{
		memoryLogger.printLog(out, clear);
	}

	static
	{
		try
		{
			Utility.addFieldPropertyManager(EXCEPTION_LOG_PROPERTY, Jdk14Factory.class, "EXCEPTION_LOG_TYPE");
		}
		catch (Throwable ex)
		{
			System.err.println("Error in init exception log type.");
			ex.printStackTrace();
		}

		try
		{
			String listeners = Utility.getProperty("logger.listeners");
			if (!StringTool.isEmpty(listeners))
			{
				String[] lNames = StringTool.separateString(listeners, ",", true);
				loggerListeners = new LoggerListener[lNames.length + 1];
				loggerListeners[0] = memoryLogger;
				for (int i = 0; i < lNames.length; i++)
				{
					Object o = Class.forName(lNames[i]).newInstance();
					loggerListeners[i + 1] = (LoggerListener) o;
				}
			}
		}
		catch (Throwable ex)
		{
			loggerListeners = new LoggerListener[]{memoryLogger};
			System.err.println("Error in init logger listeners.");
			ex.printStackTrace();
		}
		initLog(null, true);
		String lognames = Utility.getProperty("self.micromagic.logger.names");
		if (lognames != null)
		{
			StringTokenizer token = new StringTokenizer(lognames, ",");
			while (token.hasMoreTokens())
			{
				String temp = token.nextToken().trim();
				if (temp.length() == 0)
				{
					continue;
				}
				Jdk14Factory.initLog(temp, false);
			}
		}
	}

	private static void initLog(String name, boolean isDefault)
	{
		if (!isDefault)
		{
			// 检查名称的合法性
			if (name == null || name.length() == 0)
			{
				System.err.println("Error log name, null or empty.");
				return;
			}
			for (int i = 0; i < name.length(); i++)
			{
				char c = name.charAt(i);
				if (c >= 'a' && c <= 'z')
				{
					continue;
				}
				if (c >= '0' && c <= '9')
				{
					continue;
				}
				if (c >= 'A' && c <= 'Z')
				{
					continue;
				}
				if (c == '_' || c == '-')
				{
					continue;
				}
				System.err.println("Error log name: " + name);
				return;
			}
			if ("default".equals(name))
			{
				System.err.println("Error log name can not be [default].");
				return;
			}
		}

		if (isDefault)
		{
			//是否关闭控制台的log
			if (!("true".equalsIgnoreCase(Utility.getProperty("self.micromagic.defaultLogger.console.off"))))
			{
				consoleLoggerHander = new StreamHandler(System.out, new SimpleFormatter());
				try
				{
					int delay = Integer.parseInt(
							Utility.getProperty("self.micromagic.defaultLogger.console.delay_time"));
					consoleFlushDelay = delay >= 0 && delay < 500 ? 500 : delay;
				}
				catch (Throwable ex) {}
				try
				{
					consoleLoggerHander.setLevel(Level.ALL);
				}
				catch (Throwable ex) {}  // 防止安全方面的异常
				if (consoleFlushDelay != -1)
				{
					consoleFlushTimer = new ConsoleFlushTimer();
					consoleFlushTimer.start();
				}
			}
		}

		//创建文件日志的handler
		String propertyName;
		String logFile = null;
		int filesize = 1024 * 1024;
		int filecount = 5;
		FileHandler fileHander = null;
		try
		{
			propertyName = isDefault ? "self.micromagic.defaultLogger.file" :
					"self.micromagic." + name + "Logger.file";
			logFile = Utility.getProperty(propertyName);
			if (logFile != null)
			{
				File f = new File(Utility.resolveDynamicPropnames(logFile));
				f = f.getParentFile();
				if (!f.isDirectory())
				{
					f.mkdirs();
				}
				try
				{
					propertyName = isDefault ? "self.micromagic.defaultLogger.file.size" :
							"self.micromagic." + name + "Logger.file.size";
					filesize = (int) (Double.parseDouble(Utility.getProperty(propertyName)) * 1024);
					filesize = filesize < 1024 ? 1024 : filesize;
				}
				catch (Throwable ex) {}
				try
				{
					propertyName = isDefault ? "self.micromagic.defaultLogger.file.count" :
							"self.micromagic." + name + "Logger.file.count";
					filecount = Integer.parseInt(Utility.getProperty(propertyName));
					filecount = filecount < 1 ? 1 : filecount;
				}
				catch (Throwable ex) {}
				fileHander = new FileHandler(logFile, filesize, filecount, true);
				fileHander.setFormatter(new SimpleFormatter());
				fileHander.setLevel(Level.ALL);
			}
		}
		catch (Throwable ex)
		{
			System.err.println(FormatTool.getCurrentDatetimeString()
					+ ": Error when create file log handler.");
			ex.printStackTrace(System.err);
		}

		//创建日志logger
		try
		{
			String logName;
			if (isDefault)
			{
				logName = Utility.getProperty("self.micromagic.defaultLogger.name");
				if (logName == null)
				{
					logName = "default:sid_" + Thread.currentThread().getName()
							+ "." + System.currentTimeMillis();
				}
				else
				{
					logName = "default:" + logName;
				}
			}
			else
			{
				logName = name;
			}
			Logger logger = Logger.getLogger(logName);
			try
			{
				logger.setUseParentHandlers(false);
			}
			catch (Throwable ex) {}  // 防止安全方面的异常
			if (isDefault && consoleLoggerHander != null)
			{
				logger.addHandler(consoleLoggerHander);
			}
			if (fileHander != null)
			{
				logger.addHandler(fileHander);
			}
			//设置log的level
			propertyName = isDefault ? "self.micromagic.defaultLogger.level" :
					"self.micromagic." + name + "Logger.level";
			String levelName = Utility.getProperty(propertyName, "INFO");
			try
			{
				Level level = Level.parse(levelName);
				StringAppender temp = StringTool.createStringAppender(128);
				temp.append("Jdk14 log  name:").append(logName).append(", Level:").append(level);
				if (logFile != null)
				{
					temp.append(", file(").append(logFile).append(',');
					int tempI = ((filesize % 1024) * 10 + 512) / 1024;
					if (tempI == 10)
					{
						temp.append(filesize / 1024 + 1);
					}
					else
					{
						temp.append(filesize / 1024);
						if (tempI > 0)
						{
							temp.append('.').append(tempI);
						}
					}
					temp.append("k,").append(filecount).append(')');
				}
				System.out.println(temp);
				logger.setLevel(level);
			}
			catch (Throwable ex) {}
			if (isDefault)
			{
				defaultLogger = logger;
			}
			else
			{
				registerLogger(name, logger);
			}
		}
		catch (Throwable ex)
		{
			System.err.println(FormatTool.getCurrentDatetimeString()
					+ ": Error when create log.");
			ex.printStackTrace(System.err);
		}
	}

	public static Logger registerLogger(String name, Logger logger)
	{
		if (logger == null)
		{
			return (Logger) Jdk14Factory.otherLoggerMap.remove(name);
		}
		return (Logger) Jdk14Factory.otherLoggerMap.put(name, logger);
	}

	public static void stopFlushConsale()
	{
		if (consoleFlushTimer != null)
		{
			consoleFlushTimer.consoleFlushOver = true;
		}
	}

	public static void startFlushConsale()
	{
		if (consoleLoggerHander != null && (consoleFlushTimer == null || consoleFlushTimer.consoleFlushOver))
		{
			consoleFlushTimer = new ConsoleFlushTimer();
			consoleFlushTimer.start();
		}
	}

	public Object getAttribute(String name)
	{
		return this.attributes.get(name);
	}

	public String[] getAttributeNames()
	{
		Set keys = attributes.keySet();
		return (String[]) keys.toArray(StringTool.EMPTY_STRING_ARRAY);
	}

	public void setAttribute(String name, Object value)
	{
		this.attributes.put(name, value);
	}

	public void removeAttribute(String name)
	{
		this.attributes.remove(name);
	}

	public Log getInstance(Class clazz)
			throws LogConfigurationException
	{
		return this.getInstance(ClassGenerator.getClassName(clazz));
	}

	public Log getInstance(String name)
			throws LogConfigurationException
	{
		Log instance = (Log) this.instances.get(name);
		if (instance == null)
		{
			Logger tempLogger = (Logger) Jdk14Factory.otherLoggerMap.get(name);
			if (tempLogger == null)
			{
				tempLogger = Jdk14Factory.defaultLogger;
			}
			instance = new MyJdk14Logger(tempLogger);
			this.instances.put(name, instance);
		}
		return instance;
	}

	public void release()
	{
		this.instances.clear();
	}

	private static class MyJdk14Logger
			implements Log
	{
		protected Logger logger;

		public MyJdk14Logger(Logger logger)
		{
			this.logger = logger;
		}

		public Logger getLogger()
		{
			return this.logger;
		}

		public boolean isDebugEnabled()
		{
		  return this.getLogger().isLoggable(Level.FINE);
		}

		public boolean isErrorEnabled()
		{
		  return this.getLogger().isLoggable(Level.SEVERE);
		}

		public boolean isFatalEnabled()
		{
		  return this.getLogger().isLoggable(Level.SEVERE);
		}

		public boolean isInfoEnabled()
		{
		  return this.getLogger().isLoggable(Level.INFO);
		}

		public boolean isTraceEnabled()
		{
		  return this.getLogger().isLoggable(Level.FINEST);
		}

		public boolean isWarnEnabled()
		{
		  return this.getLogger().isLoggable(Level.WARNING);
		}

		public void debug(Object message, Throwable exception)
		{
			this.log(Level.FINE, String.valueOf(message), exception);
		}

		public void error(Object message, Throwable exception)
		{
			this.log(Level.SEVERE, String.valueOf(message), exception);
		}

		public void fatal(Object message, Throwable exception)
		{
			this.log(Level.SEVERE, String.valueOf(message), exception);
		}

		public void info(Object message, Throwable exception)
		{
			this.log(Level.INFO, String.valueOf(message), exception);
		}

		public void trace(Object message, Throwable exception)
		{
			this.log(Level.FINEST, String.valueOf(message), exception);
		}

		public void warn(Object message, Throwable exception)
		{
			this.log(Level.WARNING, String.valueOf(message), exception);
		}

		public void trace(Object message)
		{
			this.log(Level.FINEST, String.valueOf(message), null);
		}

		public void debug(Object message)
		{
			this.log(Level.FINE, String.valueOf(message), null);
		}

		public void info(Object message)
		{
			this.log(Level.INFO, String.valueOf(message), null);
		}

		public void warn(Object message)
		{
			this.log(Level.WARNING, String.valueOf(message), null);
		}

		public void error(Object message)
		{
			this.log(Level.SEVERE, String.valueOf(message), null);
		}

		public void fatal(Object message)
		{
			this.log(Level.SEVERE, String.valueOf(message), null);
		}

		protected void log(Level level, String msg, Throwable ex)
		{
			Logger logger = this.getLogger();
			if (logger.isLoggable(level))
			{
				// 获取 在哪个方法中记录日志
				Throwable dummyException = new Throwable();
				StackTraceElement locations[] = dummyException.getStackTrace();
				String cname = "unknow";
				String method = "unknow";
				String fileName = "unknow";
				int lineNumber = -1;
				int local = -1;
				if (locations != null && locations.length > 2)
				{
					String myName = this.getClass().getName();
					for (int i = 0; i < locations.length; i++)
					{
						if (myName.equals(locations[i].getClassName()))
						{
							if (locations.length > i + 2)
							{
								local = i + 2;
							}
							break;
						}
					}
				}
				if (local != -1)
				{
					StackTraceElement caller = locations[local];
					cname = caller.getClassName();
					method = caller.getMethodName();
					fileName = caller.getFileName();
					lineNumber = caller.getLineNumber();
				}
				int begin = EXCEPTION_LOG_TYPE > 0 ? 0 : 1;
				if (begin == 0)
				{
					memoryLogger.setLogValid(true);
				}
				if (begin == 0 || loggerListeners.length > 1)
				{
					String threadName = Thread.currentThread().getName();
					String lineNumStr = lineNumber == -1 ? "unknow" : String.valueOf(lineNumber);
					for (int i = begin; i < loggerListeners.length; i++)
					{
						loggerListeners[i].afterLog(msg, ex, level.getName(), threadName,
								cname, method, fileName, lineNumStr);
					}
				}
				if (ex == null)
				{
					logger.logp(level, cname, method, msg);
				}
				else
				{
					logger.logp(level, cname, method, msg, ex);
				}
			}
		}

	}

	private static class ConsoleFlushTimer extends Thread
	{
		private boolean consoleFlushOver = false;

		public void run()
		{
			while (!this.consoleFlushOver)
			{
				try
				{
					Thread.sleep(Jdk14Factory.consoleFlushDelay);
					consoleLoggerHander.flush();
				}
				catch (InterruptedException ex)
				{
					System.err.println(FormatTool.getCurrentDatetimeString()
							+ ": Error when flush log console.");
				}
			}
			System.out.println("Jdk14 log  console timer end.");
		}

	}

}