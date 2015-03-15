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

package self.micromagic.util.logging;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.OptionHandler;
import org.apache.log4j.spi.ThrowableInformation;

/**
 * 在log4j中调用MemoryLogger进行日志记录.
 */
public class Log4jAppender
		implements Appender, OptionHandler
{
	private String name;
	private Filter filter;
	private ErrorHandler errorHandler;
	private Layout layout;

	private LoggerListener listener;

	public Log4jAppender()
	{
		this.listener = MemoryLogger.getInstance();
	}

	public Log4jAppender(String name)
	{
		this.listener = createLoggerListener(name);
	}

	private static LoggerListener createLoggerListener(String name)
	{
		if (name == null)
		{
			return MemoryLogger.getInstance();
		}
		String prefix = "class:";
		if (name.startsWith(prefix))
		{
			try
			{
				Object o = Class.forName(name.substring(prefix.length())).newInstance();
				return (LoggerListener) o;
			}
			catch (Exception ex)
			{
				System.err.println("Create logger listener error, " + name + ".");
				ex.printStackTrace();
			}
		}
		return MemoryLogger.getInstance(name);
	}

	/**
	 * 设置使用的LoggerListener的名称.
	 */
	public void setLoggerListener(String name)
	{
		this.listener = createLoggerListener(name);
	}

	public void doAppend(LoggingEvent event)
	{
		Throwable ex = null;
		ThrowableInformation ti = event.getThrowableInformation();
		if (ti != null)
		{
			ex = ti.getThrowable();
		}
		LocationInfo li = event.getLocationInformation();
		this.listener.afterLog(String.valueOf(event.getMessage()), ex,
				String.valueOf(event.getLevel()), event.getThreadName(),
				li.getClassName(), li.getMethodName(), li.getFileName(),
				li.getLineNumber());
	}

	public void setName(String initConfig)
	{
		this.name = initConfig;
	}

	public String getName()
	{
		return this.name;
	}

	public void addFilter(Filter filter)
	{
		if (this.filter != null)
		{
			this.filter = filter;
		}
	}

	public void clearFilters()
	{
		this.filter = null;
	}

	public Filter getFilter()
	{
		return this.filter;
	}

	public void setErrorHandler(ErrorHandler errorHandler)
	{
		this.errorHandler = errorHandler;
	}

	public ErrorHandler getErrorHandler()
	{
		return this.errorHandler;
	}

	public void setLayout(Layout layout)
	{
		this.layout = layout;
	}

	public Layout getLayout()
	{
		return this.layout;
	}

	public boolean requiresLayout()
	{
		return false;
	}

	public void activateOptions()
	{
	}

	public void close()
	{
	}

}