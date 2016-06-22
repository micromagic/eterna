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

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.XMLWriter;

import self.micromagic.coder.Base64;
import self.micromagic.eterna.dao.Dao;
import self.micromagic.eterna.dao.DaoLogger;
import self.micromagic.eterna.dao.PreparedStatementWrap;
import self.micromagic.eterna.digester2.ContainerManager;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.Factory;
import self.micromagic.util.FormatTool;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;
import self.micromagic.util.logging.TimeLogger;

/**
 * 带有日志记录等基础功能的数据库操作对象.
 */
public abstract class BaseDao extends AbstractDao
{
	/**
	 * 全局的日志的记录类型.
	 */
	protected static int LOG_TYPE = 0;
	/**
	 * 系统日志时是否仅仅输出语句.
	 */
	protected static boolean simplePrint;

	/**
	 * 当前对象的日志记录类型.
	 */
	protected int objLogType;

	static
	{
		try
		{
			Utility.addMethodPropertyManager(LOG_TYPE_FLAG,
					BaseDao.class, "setGlobalLogType");
			Utility.addFieldPropertyManager(SIMPLE_PRINT_FLAG,
					BaseDao.class, "simplePrint");
		}
		catch (Throwable ex)
		{
			log.warn("Error in init log type.", ex);
		}
	}

	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		if (super.initialize(factory))
		{
			return true;
		}
		if (this.logTypeName != null)
		{
			this.objLogType = parseLogType(this.logTypeName, factory);
		}
		return false;
	}

	protected void copy(Dao copyObj)
	{
		super.copy(copyObj);
		BaseDao other = (BaseDao) copyObj;
		other.logTypeName = this.logTypeName;
		other.objLogType = this.objLogType;
	}

	public String getType()
	{
		return DAO_TYPE_UNKNOW;
	}

	public int getLogType()
	{
		return this.objLogType | LOG_TYPE;
	}

	public void setLogTypeName(String logType)
	{
		this.logTypeName = logType;
	}
	private String logTypeName;

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
		if (logs.nodeCount() > 2048)
		{
			// 当节点过多时, 清除最先添加的几个节点
			Iterator itr = logs.nodeIterator();
			try
			{
				for (int i = 0; i < 1536; i++)
				{
					itr.next();
				}
				Element newLogs = DocumentHelper.createElement("logs");
				while (itr.hasNext())
				{
					newLogs.add((Node) itr.next());
				}
				Element root = logs.getParent();
				root.remove(logs);
				root.add(newLogs);
				logs = newLogs;
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
	static void setGlobalLogType(String type)
	{
		Factory f = ContainerManager.getGlobalContainer().getFactory();
		LOG_TYPE = parseLogType(type, f instanceof EternaFactory ? (EternaFactory) f : null);
	}

	private static int parseLogType(String logType, EternaFactory factory)
	{
		try
		{
			return Integer.parseInt(logType);
		}
		catch (Exception ex)
		{
			if (log.isDebugEnabled())
			{
				log.debug("Dao log type [" + logType + "] isn't a number.", ex);
			}
			String[] arr = StringTool.separateString(logType, ", ");
			int tmpType = 0;
			for (int i = 0; i < arr.length; i++)
			{
				String tmp = arr[i];
				if ("save".equalsIgnoreCase(tmp) || "2".equals(tmp))
				{
					tmpType |= DAO_LOG_TYPE_SAVE;
				}
				else if ("print".equalsIgnoreCase(tmp) || "1".equals(tmp))
				{
					tmpType |= DAO_LOG_TYPE_PRINT;
				}
				else if ("none".equalsIgnoreCase(tmp))
				{
					tmpType |= DAO_LOG_TYPE_NONE;
					// 如果有忽略设置, 则其它的都无效
					break;
				}
				else
				{
					if (factory == null)
					{
						log.error("Error dao log type [" + tmp + "].");
					}
					else
					{
						int index = factory.getDaoLoggerIndex(tmp);
						if (index == -1)
						{
							log.error("Error dao log type [" + tmp + "].");
						}
						else
						{
							// 最低3位作为默认的类型
							tmpType |= (1 << (index + 3));
						}
					}
				}
			}
			return tmpType;
		}
	}

	private static void log(Dao base, TimeLogger usedTime, Throwable error, Element logNode)
			throws SQLException, EternaException
	{
		logNode.addAttribute("name", base.getName());
		logNode.addAttribute("time", FormatTool.getCurrentDatetimeString());
		logNode.addAttribute("usedTime", usedTime.formatPassTime(false));
		if (error != null)
		{
			logNode.addElement("error").addText(error.toString());
		}
		logNode.addElement("script").addText(base.getPreparedScript());
		Element params = logNode.addElement("parameters");
		PreparedValueReader rpv = new PreparedValueReader(params);
		base.prepareValues(rpv);
	}

	/**
	 * 记录sql日志.
	 *
	 * @param base      发生日志的数据库操作对象
	 * @param usedTime  数据操作执行用时, 请使用formatPassTime方法格式化后的时间
	 * @param error     执行时出现的异常
	 * @param conn      执行此数据操作使用的数据库连接
	 * @return  是否成功记录了数据操作日志
	 * @see TimeLogger#formatPassTime(boolean)
	 */
	protected static boolean log(Dao base, TimeLogger usedTime, Throwable error, Connection conn)
			throws SQLException, EternaException
	{
		int logType = base.getLogType();
		if (logType == 0 || logType == -1)
		{
			return false;
		}
		Element theLog;
		if ((logType & DAO_LOG_TYPE_SAVE) != 0)
		{
			theLog = createLogNode(base.getType());
			AppData data = AppData.getCurrentData();
			if (data.getLogType() > 0)
			{
				Element nowNode = data.getCurrentNode();
				if (nowNode != null)
				{
					log(base, usedTime, error, nowNode.addElement(base.getType()));
				}
			}
		}
		else
		{
			theLog = DocumentHelper.createElement(base.getType());
		}
		log(base, usedTime, error, theLog);
		if ((logType & SPECIAL_MASK) != 0)
		{
			int flag = 0x8;
			EternaFactory factory = base.getFactory();
			int count = factory.getDaoLoggerCount();
			for (int i = 0; i < count; i++)
			{
				if ((logType & flag) != 0)
				{
					DaoLogger sLog = factory.getDaoLogger(i);
					if (sLog != null)
					{
						sLog.log(base, theLog, usedTime, error, conn);
					}
				}
				flag <<= 1;
			}
		}
		if ((logType & DAO_LOG_TYPE_PRINT) != 0 && log.isInfoEnabled())
		{
			if (simplePrint)
			{
				try
				{
					log.info("Dao script:".concat(base.getPreparedScript()));
				}
				catch (Exception ex) {}
			}
			else
			{
				log.info("Dao log:\n".concat(theLog.asXML()));
			}
		}
		return (logType & DAO_LOG_TYPE_SAVE) != 0;
	}

	/**
	 * 记录sql日志.
	 *
	 * @param usedTime  数据操作执行用时, 请使用formatPassTime方法格式化后的时间
	 * @param error     执行时出现的异常
	 * @param conn      执行此数据操作使用的数据库连接
	 * @return  是否成功记录了数据操作日志
	 * @see TimeLogger#formatPassTime(boolean)
	 */
	protected boolean log(TimeLogger usedTime, Throwable error, Connection conn)
			throws SQLException, EternaException
	{
		return log(this, usedTime, error, conn);
	}

	public void execute(Connection conn)
			throws EternaException, SQLException
	{
		this.log(new TimeLogger(), null, conn);
	}

}

class PreparedValueReader
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