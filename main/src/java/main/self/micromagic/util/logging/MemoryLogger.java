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

import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.XMLWriter;

import self.micromagic.cg.ClassGenerator;
import self.micromagic.eterna.model.AppData;
import self.micromagic.util.FormatTool;

/**
 * 将日志以XML的形式记录在内存中.
 */
public class MemoryLogger
		implements LoggerListener
{
	/**
	 * 获取一个全局的内存日志实例.
	 */
	public static MemoryLogger getInstance()
	{
		return instance;
	}
	private static MemoryLogger instance = new MemoryLogger();

	/**
	 * 获取一个值得名称的内存日志实例.
	 */
	public static MemoryLogger getInstance(String name)
	{
		if (name == null)
		{
			return instance;
		}
		MemoryLogger ml = (MemoryLogger) instanceCache.get(name);
		if (ml == null)
		{
			synchronized (instanceCache)
			{
				ml = (MemoryLogger) instanceCache.get(name);
				if (ml == null)
				{
					ml = new MemoryLogger();
					instanceCache.put(name, ml);
				}
			}
		}
		return ml;
	}
	private static Map instanceCache = new HashMap();

	/**
	 * 存放日志信息的dom节点.
	 */
	private Document logDocument = null;
	private Element logNodes = null;

	private MemoryLogger()
	{
	}

	/**
	 * 设置当前内存日志是否有效.
	 */
	public void setLogValid(boolean valid)
	{
		this.logValid = valid;
	}
	private boolean logValid;

	/**
	 * 检查并初始化日志信息的dom节点.
	 */
	private void checkNodeInit()
	{
		if (this.logDocument == null)
		{
			this.logDocument = DocumentHelper.createDocument();
			Element root = this.logDocument.addElement("eterna");
			this.logNodes = root.addElement("logs");
		}

		if (this.logNodes.elements().size() > 2048)
		{
			// 当节点过多时, 清除最先添加的几个节点
			Iterator itr = this.logNodes.elementIterator();
			try
			{
				for (int i = 0; i < 1536; i++)
				{
					itr.next();
					itr.remove();
				}
			}
			catch (Exception ex)
			{
				// 当去除节点出错时, 则清空日志
				this.logDocument = null;
				this.checkNodeInit();
			}
		}
	}

	/**
	 * 添加一个记录日志信息的节点.
	 */
	private synchronized void addLogNode(Element logNode)
	{
		this.checkNodeInit();
		this.logNodes.add(logNode);
	}

	/**
	 * 创建一个记录日志信息的节点.
	 *
	 * @param nodeName  节点的名称
	 */
	private Element createLogNode(String nodeName)
	{
		return DocumentHelper.createElement(nodeName);
	}

	/**
	 * 添加一条日志信息.
	 *
	 * @param msg         日志的文本信息
	 * @param ex          异常信息
	 * @param isCause     是否为记录异常的产生者, 及产生这个异常的异常
	 * @param level       日志的等级
	 * @param threadName  记录日志的所在线程
	 * @param className   记录日志的所在类
	 * @param methodName  记录日志的所在方法
	 * @param fileName    记录日志的所在文件
	 * @param lineNumber  记录日志的所在行
	 * @param logNode     日志信息所添加的节点
	 */
	private void addLog(String msg, Throwable ex, boolean isCause, String level, String threadName,
			String className, String methodName, String fileName, String lineNumber, Element logNode)
	{
		if (!isCause)
		{
			logNode.addAttribute("level", level);
			logNode.addAttribute("thread", threadName);
			logNode.addAttribute("class", className);
			logNode.addAttribute("method", methodName);
			if (fileName != null)
			{
				logNode.addAttribute("fileName", fileName);
			}
			if (lineNumber != null)
			{
				logNode.addAttribute("lineNumber", lineNumber);
			}
			logNode.addAttribute("message", msg);
		}
		logNode.addAttribute("time", FormatTool.formatFullDate(new Date(System.currentTimeMillis())));
		if (ex != null)
		{
			logNode.addAttribute("exClass", ClassGenerator.getClassName(ex.getClass()));
			logNode.addAttribute("exMessage", ex.getMessage());
			Element stacks = logNode.addElement("stacks");
			StackTraceElement[] trace = ex.getStackTrace();
			for (int i = 0; i < trace.length; i++)
			{
				Element stack = stacks.addElement("stack");
				stack.setText(trace[i].toString());
			}
		}
	}

	public void afterLog(String msg, Throwable ex, String level, String threadName, String className,
			String methodName, String fileName, String lineNumber)
	{
		if (!this.logValid)
		{
			return;
		}
		Element logNode = this.createLogNode(ex == null ? "message" : "exception");
		this.addLog(msg, ex, false, level, threadName, className, methodName, fileName, lineNumber, logNode);
		if (ex != null)
		{
			Throwable cause = ex;
			while ((cause = cause.getCause()) != null)
			{
				Element causeNode = logNode.addElement("cause_by");
				this.addLog(null, cause, true, null, null, null, null, null, null, causeNode);
			}
		}
		AppData aData = AppData.getCurrentData();
		if (aData.getLogType() > 0)
		{
			Element nowNode = aData.getCurrentNode();
			if (nowNode != null)
			{
				nowNode.add(logNode.createCopy());
			}
		}
		this.addLogNode(logNode);
	}

	/**
	 * 打印日志信息.
	 *
	 * @param out    打印日子的输出流
	 * @param clear  是否需要清空现有的日志
	 */
	public void printLog(Writer out, boolean clear)
			throws IOException
	{
		if (this.logDocument == null)
		{
			return;
		}
		synchronized (this)
		{
			XMLWriter writer = new XMLWriter(out);
			writer.write(this.logDocument);
			writer.flush();
			if (clear)
			{
				logDocument = null;
				logNodes = null;
			}
		}
	}

}