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

package self.micromagic.eterna.sql.impl;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.StringTokenizer;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import self.micromagic.coder.Base64;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.digester.FactoryManager;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.sql.PreparedStatementWrap;
import self.micromagic.eterna.sql.SQLAdapter;
import self.micromagic.eterna.sql.SpecialLog;
import self.micromagic.util.FormatTool;
import self.micromagic.util.Utility;
import self.micromagic.util.logging.TimeLogger;

public class SQLAdapterImpl extends AbstractSQLAdapter
{
	/**
	 * 全局的sql日志的记录类型.
	 */
	protected static int SQL_LOG_TYPE = 0;

	/**
	 * 当前对象的sql日志记录类型.
	 */
	protected int sqlLogType;

	static
	{
		try
		{
			Utility.addMethodPropertyManager(SQL_LOG_PROPERTY, SQLAdapterImpl.class, "setSQLLogType");
		}
		catch (Throwable ex)
		{
			log.warn("Error in init sql log type.", ex);
		}
	}

	public SQLAdapter createSQLAdapter()
			throws EternaException
	{
		SQLAdapterImpl other = new SQLAdapterImpl();
		this.copy(other);
		return other;
	}

	protected void copy(SQLAdapter copyObj)
	{
		super.copy(copyObj);
		SQLAdapterImpl other = (SQLAdapterImpl) copyObj;
		other.sqlLogType = this.sqlLogType;
	}

	public String getType()
	{
		return SQL_TYPE_SQL;
	}

	public int getLogType()
	{
		return this.sqlLogType | SQL_LOG_TYPE;
	}

	public void setLogType(String logType)
	{
		this.sqlLogType = parseLogType(logType);
	}

	protected static void setSQLLogType(String type)
	{
		SQL_LOG_TYPE = parseLogType(type);
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

	private static void logSQL(SQLAdapter sql, long usedTime, Throwable exception, Element logNode)
			throws SQLException, EternaException
	{
		logNode.addAttribute("name", sql.getName());
		logNode.addAttribute("time", FormatTool.formatDatetime(new java.util.Date(System.currentTimeMillis())));
		logNode.addAttribute("usedTime", TimeLogger.formatPassTime(usedTime));
		if (exception != null)
		{
			logNode.addElement("error").addText(exception.toString());
		}
		logNode.addElement("prepared-sql").addText(sql.getPreparedSQL());
		Element params = logNode.addElement("parameters");
		PreparedValueReader rpv = new PreparedValueReader(params);
		sql.prepareValues(rpv);
	}

	/**
	 * 记录sql日志.
	 *
	 * @param sql        要记录的sql适配器对象
	 * @param usedTime   sql执行用时, 会根据jdk版本给出毫秒或纳秒, 请使用
	 *                   TimeLogger的formatPassTime方法格式化
	 *                   执行时间可使用TimeLogger的getTime方法, 并计算其差值
	 * @param exception  执行时出现的异常
	 * @param conn       执行此sql使用的数据库连接
	 * @return  是否保存了sql日志
	 * @see TimeLogger#formatPassTime(long)
	 * @see TimeLogger#getTime()
	 */
	protected static boolean logSQL(SQLAdapter sql, long usedTime, Throwable exception,
			Connection conn)
			throws SQLException, EternaException
	{
		int logType = sql.getLogType();
		if (logType == 0 || logType == -1)
		{
			return false;
		}
		Element theLog;
		if ((logType & SQL_LOG_TYPE_SAVE) != 0)
		{
			theLog = FactoryManager.createLogNode(sql.getType());
			AppData data = AppData.getCurrentData();
			if (data.getLogType() > 0)
			{
				Element nowNode = data.getCurrentNode();
				if (nowNode != null)
				{
					logSQL(sql, usedTime, exception, nowNode.addElement(sql.getType()));
				}
			}
		}
		else
		{
			theLog = DocumentHelper.createElement(sql.getType());
		}
		logSQL(sql, usedTime, exception, theLog);
		if ((logType & SQL_LOG_TYPE_SPECIAL) != 0)
		{
			SpecialLog sl = sql.getFactory().getSpecialLog();
			if (sl != null)
			{
				sl.logSQL(sql, theLog, usedTime, exception, conn);
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

	public void setNull(int parameterIndex, int sqlType) throws EternaException
	{
		this.setValuePreparer(this.vpcg.createNullPreparer(parameterIndex, sqlType));
	}

	public void setNull(String parameterName, int sqlType) throws EternaException
	{
		this.setValuePreparer(
				this.vpcg.createNullPreparer(this.getIndexByParameterName(parameterName), sqlType));
	}

	public void setBoolean(int parameterIndex, boolean x) throws EternaException
	{
		this.setValuePreparer(this.vpcg.createBooleanPreparer(parameterIndex, x));
	}

	public void setBoolean(String parameterName, boolean x) throws EternaException
	{
		this.setValuePreparer(
			  this.vpcg.createBooleanPreparer(this.getIndexByParameterName(parameterName), x));
	}

	public void setByte(int parameterIndex, byte x) throws EternaException
	{
		this.setValuePreparer(this.vpcg.createBytePreparer(parameterIndex, x));
	}

	public void setByte(String parameterName, byte x) throws EternaException
	{
		this.setValuePreparer(
				this.vpcg.createBytePreparer(this.getIndexByParameterName(parameterName), x));
	}

	public void setShort(int parameterIndex, short x) throws EternaException
	{
		this.setValuePreparer(this.vpcg.createShortPreparer(parameterIndex, x));
	}

	public void setShort(String parameterName, short x) throws EternaException
	{
		this.setValuePreparer(
				this.vpcg.createShortPreparer(this.getIndexByParameterName(parameterName), x));
	}

	public void setInt(int parameterIndex, int x) throws EternaException
	{
		this.setValuePreparer(this.vpcg.createIntPreparer(parameterIndex, x));
	}

	public void setInt(String parameterName, int x) throws EternaException
	{
		this.setValuePreparer(
				this.vpcg.createIntPreparer(this.getIndexByParameterName(parameterName), x));
	}

	public void setLong(int parameterIndex, long x) throws EternaException
	{
		this.setValuePreparer(this.vpcg.createLongPreparer(parameterIndex, x));
	}

	public void setLong(String parameterName, long x) throws EternaException
	{
		this.setValuePreparer(
				this.vpcg.createLongPreparer(this.getIndexByParameterName(parameterName), x));
	}

	public void setFloat(int parameterIndex, float x) throws EternaException
	{
		this.setValuePreparer(this.vpcg.createFloatPreparer(parameterIndex, x));
	}

	public void setFloat(String parameterName, float x) throws EternaException
	{
		this.setValuePreparer(
				this.vpcg.createFloatPreparer(this.getIndexByParameterName(parameterName), x));
	}

	public void setDouble(int parameterIndex, double x) throws EternaException
	{
		this.setValuePreparer(this.vpcg.createDoublePreparer(parameterIndex, x));
	}

	public void setDouble(String parameterName, double x) throws EternaException
	{
		this.setValuePreparer(
				this.vpcg.createDoublePreparer(this.getIndexByParameterName(parameterName), x));
	}

	public void setString(int parameterIndex, String x) throws EternaException
	{
		this.setValuePreparer(this.vpcg.createStringPreparer(parameterIndex, x));
	}

	public void setString(String parameterName, String x) throws EternaException
	{
		this.setValuePreparer(
				this.vpcg.createStringPreparer(this.getIndexByParameterName(parameterName), x));
	}

	public void setBytes(int parameterIndex, byte[] x) throws EternaException
	{
		this.setValuePreparer(this.vpcg.createBytesPreparer(parameterIndex, x));
	}

	public void setBytes(String parameterName, byte[] x) throws EternaException
	{
		this.setValuePreparer(
				this.vpcg.createBytesPreparer(this.getIndexByParameterName(parameterName), x));
	}

	public void setDate(int parameterIndex, Date x) throws EternaException
	{
		this.setValuePreparer(this.vpcg.createDatePreparer(parameterIndex, x));
	}

	public void setDate(String parameterName, Date x) throws EternaException
	{
		this.setValuePreparer(
				this.vpcg.createDatePreparer(this.getIndexByParameterName(parameterName), x));
	}

	public void setTime(int parameterIndex, Time x) throws EternaException
	{
		this.setValuePreparer(this.vpcg.createTimePreparer(parameterIndex, x));
	}

	public void setTime(String parameterName, Time x) throws EternaException
	{
		this.setValuePreparer(
				this.vpcg.createTimePreparer(this.getIndexByParameterName(parameterName), x));
	}

	public void setTimestamp(int parameterIndex, Timestamp x)
			throws EternaException
	{
		this.setValuePreparer(this.vpcg.createTimestampPreparer(parameterIndex, x));
	}

	public void setTimestamp(String parameterName, Timestamp x)
			throws EternaException
	{
		this.setValuePreparer(
				this.vpcg.createTimestampPreparer(this.getIndexByParameterName(parameterName), x));
	}

	public void setObject(int parameterIndex, Object x)
			throws EternaException
	{
		this.setValuePreparer(this.vpcg.createObjectPreparer(parameterIndex, x));
	}

	public void setObject(String parameterName, Object x)
			throws EternaException
	{
		this.setValuePreparer(
				this.vpcg.createObjectPreparer(this.getIndexByParameterName(parameterName), x));
	}

	public void setBinaryStream(int parameterIndex, InputStream x, int length)
			throws EternaException
	{
		this.setValuePreparer(this.vpcg.createStreamPreparer(parameterIndex, x, length));
	}

	public void setBinaryStream(String parameterName, InputStream x, int length)
			throws EternaException
	{
		this.setValuePreparer(
				this.vpcg.createStreamPreparer(this.getIndexByParameterName(parameterName), x, length));
	}

	public void setCharacterStream(int parameterIndex, Reader reader, int length)
			throws EternaException
	{
		this.setValuePreparer(this.vpcg.createReaderPreparer(parameterIndex, reader, length));
	}

	public void setCharacterStream(String parameterName, Reader reader, int length)
			throws EternaException
	{
		this.setValuePreparer(
				this.vpcg.createReaderPreparer(this.getIndexByParameterName(parameterName), reader, length));
	}

	/*
	public void setObject(int parameterIndex, Object x, int targetSqlType)
			throws EternaException
	{
		this.setValuePreparer(new ObjectPreparer(parameterIndex, x, new Integer(targetSqlType)));
	}

	public void setObject(String parameterName, Object x, int targetSqlType)
			throws EternaException
	{
		this.setValuePreparer(
				new ObjectPreparer(this.getIndexByParameterName(parameterName),
						x, new Integer(targetSqlType)));

	}

	public void setObject(int parameterIndex, Object x, int targetSqlType, int scale)
			throws EternaException
	{
		this.setValuePreparer(new ObjectPreparer(parameterIndex, x,
				new Integer(targetSqlType), new Integer(scale)));
	}

	public void setObject(String parameterName, Object x, int targetSqlType, int scale)
			throws EternaException
	{
		this.setValuePreparer(
				new ObjectPreparer(this.getIndexByParameterName(parameterName),
						x, new Integer(targetSqlType), new Integer(scale)));
	}
	*/

	private static class PreparedValueReader
			implements PreparedStatementWrap
	{
		private Element paramsRoot;
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
			parameter.addAttribute("index", parameterIndex + "");
			parameter.addAttribute("sqlType", sqlType + "");
			parameter.addAttribute("isNull", "true");
		}

		public void setBoolean(String parameterName, int parameterIndex, boolean x)
		{
			Element parameter = this.paramsRoot.addElement("parameter");
			this.addParameterName(parameter, parameterName);
			parameter.addAttribute("index", parameterIndex + "");
			parameter.addAttribute("type", "boolean");
			parameter.addText(x ? "true" : "false");
		}

		public void setByte(String parameterName, int parameterIndex, byte x)
		{
			Element parameter = this.paramsRoot.addElement("parameter");
			this.addParameterName(parameter, parameterName);
			parameter.addAttribute("index", parameterIndex + "");
			parameter.addAttribute("type", "byte");
			parameter.addText(x + "");
		}

		public void setShort(String parameterName, int parameterIndex, short x)
		{
			Element parameter = this.paramsRoot.addElement("parameter");
			this.addParameterName(parameter, parameterName);
			parameter.addAttribute("index", parameterIndex + "");
			parameter.addAttribute("type", "short");
			parameter.addText(x + "");
		}

		public void setInt(String parameterName, int parameterIndex, int x)
		{
			Element parameter = this.paramsRoot.addElement("parameter");
			this.addParameterName(parameter, parameterName);
			parameter.addAttribute("index", parameterIndex + "");
			parameter.addAttribute("type", "int");
			parameter.addText(x + "");
		}

		public void setLong(String parameterName, int parameterIndex, long x)
		{
			Element parameter = this.paramsRoot.addElement("parameter");
			this.addParameterName(parameter, parameterName);
			parameter.addAttribute("index", parameterIndex + "");
			parameter.addAttribute("type", "long");
			parameter.addText(x + "");
		}

		public void setFloat(String parameterName, int parameterIndex, float x)
		{
			Element parameter = this.paramsRoot.addElement("parameter");
			this.addParameterName(parameter, parameterName);
			parameter.addAttribute("index", parameterIndex + "");
			parameter.addAttribute("type", "float");
			parameter.addText(x + "");
		}

		public void setDouble(String parameterName, int parameterIndex, double x)
		{
			Element parameter = this.paramsRoot.addElement("parameter");
			this.addParameterName(parameter, parameterName);
			parameter.addAttribute("index", parameterIndex + "");
			parameter.addAttribute("type", "double");
			parameter.addText(x + "");
		}

		public void setString(String parameterName, int parameterIndex, String x)
		{
			Element parameter = this.paramsRoot.addElement("parameter");
			this.addParameterName(parameter, parameterName);
			parameter.addAttribute("index", parameterIndex + "");
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
			parameter.addAttribute("index", parameterIndex + "");
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
			parameter.addAttribute("index", parameterIndex + "");
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
			parameter.addAttribute("index", parameterIndex + "");
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
			parameter.addAttribute("index", parameterIndex + "");
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
			parameter.addAttribute("index", parameterIndex + "");
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
			parameter.addAttribute("index", parameterIndex + "");
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
			parameter.addAttribute("index", parameterIndex + "");
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
			parameter.addAttribute("index", parameterIndex + "");
			parameter.addAttribute("type", "Stream");
			parameter.addAttribute("length", length + "");
			if (x != null)
			{
				//parameter.addText(this.getStr(x, length));
			}
			else
			{
				parameter.addAttribute("isNull", "true");
			}
		}

		public void setCharacterStream(String parameterName, int parameterIndex, Reader reader, int length)
		{
			Element parameter = this.paramsRoot.addElement("parameter");
			this.addParameterName(parameter, parameterName);
			parameter.addAttribute("index", parameterIndex + "");
			parameter.addAttribute("type", "Reader");
			parameter.addAttribute("length", length + "");
			if (reader != null)
			{
				/*StringAppender result = StringTool.createStringAppender(length < 512 ? length : 512);
				char[] buf = new char[256];
				int size = length;
				try
				{
					int count = reader.read(buf);
					while (count > 0 && size > 0)
					{
						if (size < count)
						{
							count = size;
						}
						result.append(buf, 0, count);
						size -= count;
						if (size > 0)
						{
							count = reader.read(buf);
						}
					}
				}
				catch (IOException ex)
				{
					log.warn("Error get string from resder.", ex);
				}
				parameter.addText(result.toString());*/
			}
			else
			{
				parameter.addAttribute("isNull", "true");
			}
		}

		public void setObject(String parameterName, int parameterIndex, Object x, int targetSqlType, int scale)
		{
			Element parameter = this.paramsRoot.addElement("parameter");
			this.addParameterName(parameter, parameterName);
			parameter.addAttribute("index", parameterIndex + "");
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
			parameter.addAttribute("index", parameterIndex + "");
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
			parameter.addAttribute("index", parameterIndex + "");
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