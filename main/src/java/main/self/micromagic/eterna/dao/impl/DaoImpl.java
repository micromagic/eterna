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

package self.micromagic.eterna.dao.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.XMLWriter;

import self.micromagic.coder.Base64;
import self.micromagic.eterna.dao.Dao;
import self.micromagic.eterna.dao.Parameter;
import self.micromagic.eterna.dao.PreparedStatementWrap;
import self.micromagic.eterna.dao.SpecialLog;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.util.FormatTool;
import self.micromagic.util.Utility;
import self.micromagic.util.logging.TimeLogger;

public class DaoImpl extends AbstractDao
{
	/**
	 * 全局的日志的记录类型.
	 */
	protected static int LOG_TYPE = 0;

	/**
	 * 当前对象的日志记录类型.
	 */
	protected int objLogType;

	static
	{
		try
		{
			Utility.addMethodPropertyManager(LOG_TYPE_FLAG,
					DaoImpl.class, "setGlobalLogType");
		}
		catch (Throwable ex)
		{
			log.warn("Error in init log type.", ex);
		}
	}

	public Object create()
			throws EternaException
	{
		DaoImpl other = new DaoImpl();
		this.copy(other);
		return other;
	}

	protected void copy(Dao copyObj)
	{
		super.copy(copyObj);
		DaoImpl other = (DaoImpl) copyObj;
		other.objLogType = this.objLogType;
	}

	public String getType()
	{
		return SQL_TYPE_SQL;
	}

	public int getLogType()
	{
		return this.objLogType | LOG_TYPE;
	}

	public void setLogType(String logType)
	{
		this.objLogType = parseLogType(logType);
	}

	/**
	 * 生成一个记录日志的节点.
	 *
	 * @param name   类型名称
	 */
	public static synchronized Element createLogNode(String name)
	{
		if (logDocument == null)
		{
			logDocument = DocumentHelper.createDocument();
			Element root = logDocument.addElement("eterna");
			logs = root.addElement("logs");
		}
		if (logs.elements().size() > 2048)
		{
			// 当节点过多时, 清除最先添加的几个节点
			Iterator itr = logs.elementIterator();
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
				log.warn("Remove sql log error.", ex);
				logDocument = null;
				return createLogNode(name);
			}
		}
		return logs.addElement(name);
	}
	private static Document logDocument = null;
	private static Element logs = null;

	/**
	 * 将记录的日志输出.
	 *
	 * @param out     日志的输出流
	 * @param clear   是否要在输出完后清空日志
	 */
	public static synchronized void printLog(Writer out, boolean clear)
			throws IOException
	{
		if (logDocument == null)
		{
			return;
		}
		XMLWriter writer = new XMLWriter(out);
		writer.write(logDocument);
		writer.flush();
		if (clear)
		{
			logDocument = null;
			logs = null;
		}
	}

	/**
	 * 设置全局的日志类型.
	 */
	protected static void setGlobalLogType(String type)
	{
		LOG_TYPE = parseLogType(type);
	}

	private static int parseLogType(String logType)
	{
		try
		{
			return Integer.parseInt(logType);
		}
		catch (Exception ex)
		{
			if (log.isDebugEnabled())
			{
				log.debug("SQL log type [" + logType + "] isn't a number.", ex);
			}
			StringTokenizer token = new StringTokenizer(logType, ", ");
			int tmpType = 0;
			while (token.hasMoreTokens())
			{
				String tmp = token.nextToken().trim();
				if ("".equals(tmp))
				{
					continue;
				}
				if ("save".equals(tmp))
				{
					tmpType |= SQL_LOG_TYPE_SAVE;
				}
				else if ("print".equals(tmp))
				{
					tmpType |= SQL_LOG_TYPE_PRINT;
				}
				else if ("special".equals(tmp))
				{
					tmpType |= SQL_LOG_TYPE_SPECIAL;
				}
				else if ("none".equals(tmp))
				{
					tmpType |= SQL_LOG_TYPE_NONE;
				}
			}
			return tmpType;
		}
	}

	private static void logSQL(Dao base, long usedTime, Throwable exception, Element logNode)
			throws SQLException, EternaException
	{
		logNode.addAttribute("name", base.getName());
		logNode.addAttribute("time", FormatTool.formatDatetime(new java.util.Date(System.currentTimeMillis())));
		logNode.addAttribute("usedTime", TimeLogger.formatPassTime(usedTime));
		if (exception != null)
		{
			logNode.addElement("error").addText(exception.toString());
		}
		logNode.addElement("prepared-sql").addText(base.getPreparedSQL());
		Element params = logNode.addElement("parameters");
		PreparedValueReader rpv = new PreparedValueReader(params);
		base.prepareValues(rpv);
	}

	/**
	 * 记录sql日志.
	 *
	 * @param base       要记录的数据库操作对象
	 * @param usedTime   sql执行用时, 会根据jdk版本给出毫秒或纳秒, 请使用
	 *                   TimeLogger的formatPassTime方法格式化
	 *                   执行时间可使用TimeLogger的getTime方法, 并计算其差值
	 * @param exception  执行时出现的异常
	 * @param conn       执行此sql使用的数据库连接
	 * @return  是否保存了sql日志
	 * @see TimeLogger#formatPassTime(long)
	 * @see TimeLogger#getTime()
	 */
	protected static boolean logSQL(Dao base, long usedTime, Throwable exception,
			Connection conn)
			throws SQLException, EternaException
	{
		int logType = base.getLogType();
		if (logType == 0 || logType == -1)
		{
			return false;
		}
		Element theLog;
		if ((logType & SQL_LOG_TYPE_SAVE) != 0)
		{
			theLog = createLogNode(base.getType());
			AppData data = AppData.getCurrentData();
			if (data.getLogType() > 0)
			{
				Element nowNode = data.getCurrentNode();
				if (nowNode != null)
				{
					logSQL(base, usedTime, exception, nowNode.addElement(base.getType()));
				}
			}
		}
		else
		{
			theLog = DocumentHelper.createElement(base.getType());
		}
		logSQL(base, usedTime, exception, theLog);
		if ((logType & SQL_LOG_TYPE_SPECIAL) != 0)
		{
			SpecialLog sl = base.getFactory().getSpecialLog();
			if (sl != null)
			{
				sl.logSQL(base, theLog, usedTime, exception, conn);
			}
		}
		if ((logType & SQL_LOG_TYPE_PRINT) != 0)
		{
			log.info("sql log:\n" + theLog.asXML());
		}
		return (logType & SQL_LOG_TYPE_SAVE) != 0;
	}

	/**
	 * 记录sql日志.
	 *
	 * @param usedTime   sql执行用时, 会根据jdk版本给出毫秒或纳秒, 请使用
	 *                   TimeLogger的formatPassTime方法格式化
	 *                   执行时间可使用TimeLogger的getTime方法, 并计算其差值
	 * @param exception  执行时出现的异常
	 * @param conn       执行此sql使用的数据库连接
	 * @return  是否保存了sql日志
	 * @see TimeLogger#formatPassTime(long)
	 * @see TimeLogger#getTime()
	 */
	protected boolean logSQL(long usedTime, Throwable exception, Connection conn)
			throws SQLException, EternaException
	{
		return logSQL(this, usedTime, exception, conn);
	}

	public void execute(Connection conn)
			throws EternaException, SQLException
	{
		this.logSQL(0L, null, conn);
	}

	public void setString(int parameterIndex, String x)
			throws EternaException
	{
		Parameter p = this.getParameter(parameterIndex);
		this.setValuePreparer(p.createValuePreparer(x));
	}

	public void setString(String parameterName, String x)
			throws EternaException
	{
		Parameter p = this.getParameter(parameterName);
		this.setValuePreparer(p.createValuePreparer(x));
	}

	public void setObject(int parameterIndex, Object x)
			throws EternaException
	{
		Parameter p = this.getParameter(parameterIndex);
		this.setValuePreparer(p.createValuePreparer(x));
	}

	public void setObject(String parameterName, Object x)
			throws EternaException
	{
		Parameter p = this.getParameter(parameterName);
		this.setValuePreparer(p.createValuePreparer(x));
	}

	private static class PreparedValueReader
			implements PreparedStatementWrap
	{
		private final Element paramsRoot;
		private Base64 base64;

		public PreparedValueReader(Element paramsRoot)
		{
			this.paramsRoot = paramsRoot;
		}

		private String getStr(byte[] buf)
		{
			if (this.base64 == null)
			{
				this.base64 = new Base64();
			}
			return this.base64.byteArrayToBase64(buf);
		}

		private void addParameterName(Element parameter, String parameterName)
		{
			if (parameterName != null && parameterName.length() > 0)
			{
				parameter.addAttribute("name", parameterName);
			}
		}

		public void setNull(String parameterName, int parameterIndex, int sqlType)
		{
			Element parameter = this.paramsRoot.addElement("parameter");
			this.addParameterName(parameter, parameterName);
			parameter.addAttribute("index", Integer.toString(parameterIndex));
			parameter.addAttribute("sqlType", sqlType + "");
			parameter.addAttribute("isNull", "true");
		}

		public void setBoolean(String parameterName, int parameterIndex, boolean x)
		{
			Element parameter = this.paramsRoot.addElement("parameter");
			this.addParameterName(parameter, parameterName);
			parameter.addAttribute("index", Integer.toString(parameterIndex));
			parameter.addAttribute("type", "boolean");
			parameter.addText(x ? "true" : "false");
		}

		public void setByte(String parameterName, int parameterIndex, byte x)
		{
			Element parameter = this.paramsRoot.addElement("parameter");
			this.addParameterName(parameter, parameterName);
			parameter.addAttribute("index", Integer.toString(parameterIndex));
			parameter.addAttribute("type", "byte");
			parameter.addText(x + "");
		}

		public void setShort(String parameterName, int parameterIndex, short x)
		{
			Element parameter = this.paramsRoot.addElement("parameter");
			this.addParameterName(parameter, parameterName);
			parameter.addAttribute("index", Integer.toString(parameterIndex));
			parameter.addAttribute("type", "short");
			parameter.addText(x + "");
		}

		public void setInt(String parameterName, int parameterIndex, int x)
		{
			Element parameter = this.paramsRoot.addElement("parameter");
			this.addParameterName(parameter, parameterName);
			parameter.addAttribute("index", Integer.toString(parameterIndex));
			parameter.addAttribute("type", "int");
			parameter.addText(x + "");
		}

		public void setLong(String parameterName, int parameterIndex, long x)
		{
			Element parameter = this.paramsRoot.addElement("parameter");
			this.addParameterName(parameter, parameterName);
			parameter.addAttribute("index", Integer.toString(parameterIndex));
			parameter.addAttribute("type", "long");
			parameter.addText(x + "");
		}

		public void setFloat(String parameterName, int parameterIndex, float x)
		{
			Element parameter = this.paramsRoot.addElement("parameter");
			this.addParameterName(parameter, parameterName);
			parameter.addAttribute("index", Integer.toString(parameterIndex));
			parameter.addAttribute("type", "float");
			parameter.addText(x + "");
		}

		public void setDouble(String parameterName, int parameterIndex, double x)
		{
			Element parameter = this.paramsRoot.addElement("parameter");
			this.addParameterName(parameter, parameterName);
			parameter.addAttribute("index", Integer.toString(parameterIndex));
			parameter.addAttribute("type", "double");
			parameter.addText(x + "");
		}

		public void setString(String parameterName, int parameterIndex, String x)
		{
			Element parameter = this.paramsRoot.addElement("parameter");
			this.addParameterName(parameter, parameterName);
			parameter.addAttribute("index", Integer.toString(parameterIndex));
			parameter.addAttribute("type", "String");
			if (x != null)
			{
				parameter.addText(x);
			}
			else
			{
				parameter.addAttribute("isNull", "true");
			}
		}

		public void setBytes(String parameterName, int parameterIndex, byte x[])
		{
			Element parameter = this.paramsRoot.addElement("parameter");
			this.addParameterName(parameter, parameterName);
			parameter.addAttribute("index", Integer.toString(parameterIndex));
			parameter.addAttribute("type", "Bytes");
			if (x != null)
			{
				parameter.addText(this.getStr(x));
			}
			else
			{
				parameter.addAttribute("isNull", "true");
			}
		}

		public void setDate(String parameterName, int parameterIndex, Date x)
		{
			Element parameter = this.paramsRoot.addElement("parameter");
			this.addParameterName(parameter, parameterName);
			parameter.addAttribute("index", Integer.toString(parameterIndex));
			parameter.addAttribute("type", "Date");
			if (x != null)
			{
				parameter.addText(FormatTool.formatDate(x));
			}
			else
			{
				parameter.addAttribute("isNull", "true");
			}
		}


		public void setDate(String parameterName, int parameterIndex, Date x, Calendar cal)
		{
			Element parameter = this.paramsRoot.addElement("parameter");
			this.addParameterName(parameter, parameterName);
			parameter.addAttribute("index", Integer.toString(parameterIndex));
			parameter.addAttribute("type", "Date");
			if (cal != null)
			{
				parameter.addAttribute("calendar", cal.toString());
			}
			if (x != null)
			{
				parameter.addText(FormatTool.formatDate(x));
			}
			else
			{
				parameter.addAttribute("isNull", "true");
			}
		}

		public void setTime(String parameterName, int parameterIndex, Time x)
		{
			Element parameter = this.paramsRoot.addElement("parameter");
			this.addParameterName(parameter, parameterName);
			parameter.addAttribute("index", Integer.toString(parameterIndex));
			parameter.addAttribute("type", "Time");
			if (x != null)
			{
				parameter.addText(FormatTool.formatTime(x));
			}
			else
			{
				parameter.addAttribute("isNull", "true");
			}
		}

		public void setTime(String parameterName, int parameterIndex, Time x, Calendar cal)
		{
			Element parameter = this.paramsRoot.addElement("parameter");
			this.addParameterName(parameter, parameterName);
			parameter.addAttribute("index", Integer.toString(parameterIndex));
			parameter.addAttribute("type", "Time");
			if (cal != null)
			{
				parameter.addAttribute("calendar", cal.toString());
			}
			if (x != null)
			{
				parameter.addText(FormatTool.formatTime(x));
			}
			else
			{
				parameter.addAttribute("isNull", "true");
			}
		}

		public void setTimestamp(String parameterName, int parameterIndex, Timestamp x)
		{
			Element parameter = this.paramsRoot.addElement("parameter");
			this.addParameterName(parameter, parameterName);
			parameter.addAttribute("index", Integer.toString(parameterIndex));
			parameter.addAttribute("type", "Datetime");
			if (x != null)
			{
				parameter.addText(FormatTool.formatDatetime(x));
			}
			else
			{
				parameter.addAttribute("isNull", "true");
			}
		}

		public void setTimestamp(String parameterName, int parameterIndex, Timestamp x, Calendar cal)
		{
			Element parameter = this.paramsRoot.addElement("parameter");
			this.addParameterName(parameter, parameterName);
			parameter.addAttribute("index", Integer.toString(parameterIndex));
			parameter.addAttribute("type", "Datetime");
			if (cal != null)
			{
				parameter.addAttribute("calendar", cal.toString());
			}
			if (x != null)
			{
				parameter.addText(FormatTool.formatDatetime(x));
			}
			else
			{
				parameter.addAttribute("isNull", "true");
			}
		}

		public void setBinaryStream(String parameterName, int parameterIndex, InputStream x, int length)
		{
			Element parameter = this.paramsRoot.addElement("parameter");
			this.addParameterName(parameter, parameterName);
			parameter.addAttribute("index", Integer.toString(parameterIndex));
			parameter.addAttribute("type", "Stream");
			parameter.addAttribute("length", Integer.toString(length));
			if (x == null)
			{
				parameter.addAttribute("isNull", "true");
			}
		}

		public void setCharacterStream(String parameterName, int parameterIndex, Reader reader, int length)
		{
			Element parameter = this.paramsRoot.addElement("parameter");
			this.addParameterName(parameter, parameterName);
			parameter.addAttribute("index", Integer.toString(parameterIndex));
			parameter.addAttribute("type", "Chars");
			parameter.addAttribute("length", Integer.toString(length));
			if (reader == null)
			{
				parameter.addAttribute("isNull", "true");
			}
		}

		public void setBlob(String parameterName, int parameterIndex, Blob blob)
				throws SQLException
		{
			Element parameter = this.paramsRoot.addElement("parameter");
			this.addParameterName(parameter, parameterName);
			parameter.addAttribute("index", Integer.toString(parameterIndex));
			parameter.addAttribute("type", "Blob");
			if (blob == null)
			{
				parameter.addAttribute("isNull", "true");
			}
			else
			{
				parameter.addAttribute("length", Long.toString(blob.length()));
			}
		}

		public void setClob(String parameterName, int parameterIndex, Clob clob)
				throws SQLException
		{
			Element parameter = this.paramsRoot.addElement("parameter");
			this.addParameterName(parameter, parameterName);
			parameter.addAttribute("index", Integer.toString(parameterIndex));
			parameter.addAttribute("type", "Clob");
			if (clob == null)
			{
				parameter.addAttribute("isNull", "true");
			}
			else
			{
				parameter.addAttribute("length", Long.toString(clob.length()));
			}
		}

		public void setObject(String parameterName, int parameterIndex, Object x, int targetSqlType, int scale)
		{
			Element parameter = this.paramsRoot.addElement("parameter");
			this.addParameterName(parameter, parameterName);
			parameter.addAttribute("index", Integer.toString(parameterIndex));
			parameter.addAttribute("type", "Object");
			parameter.addAttribute("sqlType", targetSqlType + "");
			parameter.addAttribute("scale", scale + "");
			if (x != null)
			{
				parameter.addText(x.toString());
			}
			else
			{
				parameter.addAttribute("isNull", "true");
			}
		}

		public void setObject(String parameterName, int parameterIndex, Object x, int targetSqlType)
		{
			Element parameter = this.paramsRoot.addElement("parameter");
			this.addParameterName(parameter, parameterName);
			parameter.addAttribute("index", Integer.toString(parameterIndex));
			parameter.addAttribute("type", "Object");
			parameter.addAttribute("sqlType", targetSqlType + "");
			if (x != null)
			{
				parameter.addText(x.toString());
			}
			else
			{
				parameter.addAttribute("isNull", "true");
			}
		}

		public void setObject(String parameterName, int parameterIndex, Object x) throws SQLException
		{
			Element parameter = this.paramsRoot.addElement("parameter");
			this.addParameterName(parameter, parameterName);
			parameter.addAttribute("index", Integer.toString(parameterIndex));
			parameter.addAttribute("type", "Object");
			if (x != null)
			{
				parameter.addText(x.toString());
			}
			else
			{
				parameter.addAttribute("isNull", "true");
			}
		}

	}

}