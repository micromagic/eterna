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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Blob;
import java.util.HashMap;
import java.util.Map;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.security.PermissionSet;
import self.micromagic.eterna.share.AttributeManager;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.Tool;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.eterna.sql.NullResultReader;
import self.micromagic.eterna.sql.ResultFormat;
import self.micromagic.eterna.sql.ResultReader;
import self.micromagic.util.converter.BooleanConverter;
import self.micromagic.util.converter.ByteConverter;
import self.micromagic.util.converter.BytesConverter;
import self.micromagic.util.converter.DateConverter;
import self.micromagic.util.converter.DoubleConverter;
import self.micromagic.util.converter.FloatConverter;
import self.micromagic.util.converter.IntegerConverter;
import self.micromagic.util.converter.LongConverter;
import self.micromagic.util.converter.ReaderConverter;
import self.micromagic.util.converter.ShortConverter;
import self.micromagic.util.converter.StreamConverter;
import self.micromagic.util.converter.StringConverter;
import self.micromagic.util.converter.TimeConverter;
import self.micromagic.util.converter.TimestampConverter;
import self.micromagic.util.converter.ValueConverter;
import self.micromagic.util.MemoryChars;
import self.micromagic.util.MemoryStream;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;
import self.micromagic.util.StringAppender;
import self.micromagic.cg.ClassGenerator;

/**
 * @author micromagic@sina.com
 */
public abstract class ResultReaders
{
	/**
	 * 在arrtibute中设置读取时是否需要获取列的索引值.
	 */
	public static final String CHECK_INDEX_FLAG = "checkIndex";

	/**
	 * 在arrtibute中设置导出时使用的标题.
	 */
	public static final String PRINT_CAPTION = "print.caption";

	/**
	 * ResultReader默认的空属性集.
	 */
	private static final AttributeManager EMPTY_ATTRIBUTES = new AttributeManager();

	public static class ObjectReader
			implements ResultReader, Cloneable
	{
		protected String name;
		protected String orderName;
		protected String formatName;
		protected ResultFormat format;

		private int width = -1;
		private String caption;

		protected PermissionSet permissionSet;
		protected boolean htmlFilter = true;
		protected boolean visible = true;

		protected boolean useIndexOrName;
		protected String columnName;
		protected int columnIndex = -1;
		protected boolean checkIndex;

		protected ValueConverter converter;
		protected AttributeManager attributes = EMPTY_ATTRIBUTES;

		public int getType()
		{
			return TypeManager.TYPE_OBJECT;
		}

		public ObjectReader(String name)
		{
			this.name = this.columnName = name;
		}

		public void initialize(EternaFactory factory)
				throws EternaException
		{
			if (this.orderName == null)
			{
				this.orderName = this.columnName == null ? this.name : this.columnName;
			}
			if (this.formatName != null)
			{
				this.format = factory.getFormat(this.formatName);
				if (this.format == null)
				{
					SQLManager.log.warn("The format [" + this.formatName + "] not found.");
				}
			}
			if (this.permissionSet != null)
			{
				this.permissionSet.initialize(factory);
			}
			if (this.caption == null)
			{
				this.caption = Tool.translateCaption(factory, this.getName());
			}
			String tmpStr = (String) this.getAttribute(CHECK_INDEX_FLAG);
			if (tmpStr != null)
			{
				this.checkIndex = "true".equalsIgnoreCase(tmpStr);
			}
			// 检查有没有设置导出时使用的标题, 如果设置了则要尝试翻译
			tmpStr = (String) this.getAttribute(PRINT_CAPTION);
			if (tmpStr != null)
			{
				String pCaption = Tool.translateCaption(factory, tmpStr);
				if (pCaption != null)
				{
					this.attributes.setAttribute(PRINT_CAPTION, pCaption);
				}
			}
		}

		/**
		 * 复制当前的ObjectReader.
		 */
		public ObjectReader copy()
		{
			try
			{
				return (ObjectReader) super.clone();
			}
			catch (CloneNotSupportedException ex)
			{
				// assert false
				throw new Error();
			}
		}

		public boolean isIgnore()
		{
			return this.getType() == TypeManager.TYPE_IGNORE;
		}

		public ResultFormat getFormat()
		{
			return this.format;
		}

		public void setFormatName(String format)
		{
			this.formatName = format;
		}

		public String getFormatName()
		{
			return this.formatName;
		}

		public String getName()
		{
			return this.name;
		}

		public String getOrderName()
		{
			return this.orderName;
		}

		public void setOrderName(String name)
		{
			this.orderName = name;
		}

		public String getColumnName()
		{
			return this.columnName;
		}

		public void setColumnName(String columnName)
		{
			this.columnName = columnName;
			this.columnIndex = -1;
			this.useIndexOrName = false;
		}

		public boolean isUseColumnName()
		{
			return !this.useIndexOrName;
		}

		public int getColumnIndex()
		{
			return this.columnIndex;
		}

		public void setColumnIndex(int columnIndex)
		{
			this.columnName = null;
			this.columnIndex = columnIndex;
			this.useIndexOrName = true;
		}

		public boolean isUseColumnIndex()
		{
			return this.useIndexOrName;
		}

		/**
		 * 设置在读取时是否需要获取列的索引值, 之后对数据的读取都是通过这个列的索引值.
		 */
		public void setCheckIndex(boolean checkIndex)
		{
			this.checkIndex = checkIndex;
		}

		/**
		 * 在以列名方式读取数据时, 是否要获取此列的索引值.
		 */
		public boolean isCheckIndex()
		{
			return this.checkIndex;
		}

		public void setHtmlFilter(boolean htmlFilter)
		{
			this.htmlFilter = htmlFilter;
		}

		public boolean needHtmlFilter()
		{
			return this.htmlFilter;
		}

		public boolean isVisible()
		{
			return this.visible;
		}

		public void setVisible(boolean visible)
		{
			this.visible = visible;
		}

		public boolean isValid()
		{
			return true;
		}

		public void setPermission(String permission)
		{
			if (permission == null || permission.trim().length() == 0)
			{
				return;
			}
			this.permissionSet = new PermissionSet(
					StringTool.separateString(permission, ",", true));
		}

		public PermissionSet getPermissionSet()
				throws EternaException
		{
			return this.permissionSet;
		}

		public int getWidth()
		{
			return this.width;
		}

		public void setWidth(int width)
		{
			this.width = width;
		}

		public String getCaption()
		{
			return this.caption;
		}

		public String getFilledCaption()
		{
			if (this.caption == null)
			{
				return this.isUseColumnIndex() ?
						"col_" + this.getColumnIndex() : this.getColumnName();
			}
			return this.caption;
		}

		public void setCaption(String caption)
		{
			this.caption = caption;
		}

		public void setAttributes(AttributeManager attributes)
		{
			this.attributes = attributes;
		}

		public Object getAttribute(String name)
		{
			return this.attributes.getAttribute(name);
		}

		public String[] getAttributeNames()
		{
			return this.attributes.getAttributeNames();
		}

		public Object readResult(ResultSet rs)
				throws SQLException
		{
			return this.useIndexOrName || this.transIndex(rs) ?
					rs.getObject(this.columnIndex) : rs.getObject(this.columnName);
		}

		/**
		 * 尝试将列名转换成索引值.
		 */
		protected boolean transIndex(ResultSet rs)
		{
			if (this.checkIndex)
			{
				try
				{
					this.columnIndex = rs.findColumn(this.columnName);
					return this.useIndexOrName = true;
				}
				catch (SQLException ex)
				{
					this.checkIndex = false;
				}
			}
			return false;
		}

		public Object readCall(CallableStatement call, int index)
				throws SQLException
		{
			return call.getObject(index);
		}

		public Object readObject(Object obj)
				throws EternaException
		{
			if (obj == null)
			{
				return null;
			}
			if (obj instanceof ResultSet)
			{
				try
				{
					return this.readResult((ResultSet) obj);
				}
				catch (SQLException ex)
				{
					throw new EternaException(ex);
				}
			}
			return this.converter == null ? obj : this.converter.convert(obj);
		}

	}


	public static class StringReader extends ObjectReader
	{
		public int getType()
		{
			return TypeManager.TYPE_STRING;
		}

		public StringReader(String name)
		{
			super(name);
			this.converter = new StringConverter();
			this.converter.setNeedThrow(true);
		}

		public Object readCall(CallableStatement call, int index)
				throws SQLException
		{
			return call.getString(index);
		}

		public Object readResult(ResultSet rs)
				throws SQLException
		{
			return this.useIndexOrName || this.transIndex(rs) ?
					rs.getString(this.columnIndex) : rs.getString(this.columnName);
		}

	}

	public static class BigStringReader extends ObjectReader
	{
		public int getType()
		{
			return TypeManager.TYPE_STRING;
		}

		public BigStringReader(String name)
		{
			super(name);
			this.converter = new StringConverter();
			this.converter.setNeedThrow(true);
		}

		public Object readCall(CallableStatement call, int index)
				throws SQLException
		{
			Clob clob = call.getClob(index);
			if (clob == null)
			{
				return null;
			}
			Reader reader = clob.getCharacterStream();
			return this.readFromReader(reader);
		}

		public Object readResult(ResultSet rs)
				throws SQLException
		{
			Reader reader = this.useIndexOrName || this.transIndex(rs) ?
					rs.getCharacterStream(this.columnIndex) : rs.getCharacterStream(this.columnName);
			if (reader == null)
			{
				return null;
			}
			return this.readFromReader(reader);
		}

		private Object readFromReader(Reader reader)
				throws SQLException
		{
			StringAppender result;
			char[] buf = new char[1024];
			try
			{
				int count = reader.read(buf);
				if (count < 1024)
				{
					result = StringTool.createStringAppender(count > 0 ? count : 2);
				}
				else
				{
					result = StringTool.createStringAppender(3072);
				}
				while (count > 0)
				{
					result.append(buf, 0, count);
					count = reader.read(buf);
				}
				reader.close();
			}
			catch (IOException ex)
			{
				SQLManager.log.error("IO error at BigStringReader.", ex);
				throw new SQLException(ex.getMessage());
			}
			return result.toString();
		}

	}

	public static class StreamReader extends ObjectReader
	{
		public int getType()
		{
			return TypeManager.TYPE_STREAM;
		}

		public StreamReader(String name)
		{
			super(name);
			this.converter = new StreamConverter();
			this.converter.setNeedThrow(true);
		}

		public Object readCall(CallableStatement call, int index)
				throws SQLException
		{
			Blob blob = call.getBlob(index);
			if (blob == null)
			{
				return null;
			}
			InputStream ins = blob.getBinaryStream();
			return this.readFromStream(ins);
		}

		public Object readResult(ResultSet rs)
				throws SQLException
		{
			InputStream ins = this.useIndexOrName || this.transIndex(rs) ?
					rs.getBinaryStream(this.columnIndex) : rs.getBinaryStream(this.columnName);
			if (ins == null)
			{
				return null;
			}
			return this.readFromStream(ins);
		}

		private Object readFromStream(InputStream ins)
				throws SQLException
		{
			MemoryStream ms = new MemoryStream(1, 512);
			try
			{
				Utility.copyStream(ins, ms.getOutputStream());
				ins.close();
			}
			catch (IOException ex)
			{
				SQLManager.log.error("IO error at StreamReader.", ex);
				throw new SQLException(ex.getMessage());
			}
			return ms;
		}

	}

	public static class ReaderReader extends ObjectReader
	{
		public int getType()
		{
			return TypeManager.TYPE_READER;
		}

		public ReaderReader(String name)
		{
			super(name);
			this.converter = new ReaderConverter();
			this.converter.setNeedThrow(true);
		}

		public Object readCall(CallableStatement call, int index)
				throws SQLException
		{
			Clob clob = call.getClob(index);
			if (clob == null)
			{
				return null;
			}
			Reader reader = clob.getCharacterStream();
			return this.readFromReader(reader);
		}

		public Object readResult(ResultSet rs)
				throws SQLException
		{
			Reader reader = this.useIndexOrName || this.transIndex(rs) ?
					rs.getCharacterStream(this.columnIndex) : rs.getCharacterStream(this.columnName);
			if (reader == null)
			{
				return null;
			}
			return this.readFromReader(reader);
		}

		private Object readFromReader(Reader reader)
				throws SQLException
		{
			MemoryChars mcs = new MemoryChars(1, 512);
			try
			{
				Utility.copyChars(reader, mcs.getWriter());
				reader.close();
			}
			catch (IOException ex)
			{
				SQLManager.log.error("IO error at ReaderReader.", ex);
				throw new SQLException(ex.getMessage());
			}
			return mcs;
		}

	}

	public static class BooleanReader extends ObjectReader
	{
		public int getType()
		{
			return TypeManager.TYPE_BOOLEAN;
		}

		public BooleanReader(String name)
		{
			super(name);
			this.converter = new BooleanConverter();
			this.converter.setNeedThrow(true);
		}

		public Object readCall(CallableStatement call, int index)
				throws SQLException
		{
			boolean booleanValue = call.getBoolean(index);
			return call.wasNull() ? null : new Boolean(booleanValue);
		}

		public Object readResult(ResultSet rs)
				throws SQLException
		{
			boolean booleanValue = this.useIndexOrName || this.transIndex(rs) ?
					rs.getBoolean(this.columnIndex) : rs.getBoolean(this.columnName);
			return rs.wasNull() ? null : new Boolean(booleanValue);
		}

	}

	public static class ByteReader extends ObjectReader
	{
		public int getType()
		{
			return TypeManager.TYPE_BYTE;
		}

		public ByteReader(String name)
		{
			super(name);
			this.converter = new ByteConverter();
			this.converter.setNeedThrow(true);
		}

		public Object readCall(CallableStatement call, int index)
				throws SQLException
		{
			byte byteValue = call.getByte(index);
			return call.wasNull() ? null : new Byte(byteValue);
		}

		public Object readResult(ResultSet rs)
				throws SQLException
		{
			byte byteValue = this.useIndexOrName || this.transIndex(rs) ?
					rs.getByte(this.columnIndex) : rs.getByte(this.columnName);
			return rs.wasNull() ? null : new Byte(byteValue);
		}

	}

	public static class ShortReader extends ObjectReader
	{
		public int getType()
		{
			return TypeManager.TYPE_SHORT;
		}

		public ShortReader(String name)
		{
			super(name);
			this.converter = new ShortConverter();
			this.converter.setNeedThrow(true);
		}

		public Object readCall(CallableStatement call, int index)
				throws SQLException
		{
			short shortValue = call.getShort(index);
			return call.wasNull() ? null : new Short(shortValue);
		}

		public Object readResult(ResultSet rs)
				throws SQLException
		{
			short shortValue = this.useIndexOrName || this.transIndex(rs) ?
					rs.getShort(this.columnIndex) : rs.getShort(this.columnName);
			return rs.wasNull() ? null : new Short(shortValue);
		}

	}

	public static class IntegerReader extends ObjectReader
	{
		public int getType()
		{
			return TypeManager.TYPE_INTEGER;
		}

		public IntegerReader(String name)
		{
			super(name);
			this.converter = new IntegerConverter();
			this.converter.setNeedThrow(true);
		}

		public Object readCall(CallableStatement call, int index)
				throws SQLException
		{
			int intValue = call.getInt(index);
			return call.wasNull() ? null : new Integer(intValue);
		}

		public Object readResult(ResultSet rs)
				throws SQLException
		{
			int intValue = this.useIndexOrName || this.transIndex(rs) ?
					rs.getInt(this.columnIndex) : rs.getInt(this.columnName);
			return rs.wasNull() ? null : new Integer(intValue);
		}

	}

	public static class LongReader extends ObjectReader
	{
		public int getType()
		{
			return TypeManager.TYPE_LONG;
		}

		public LongReader(String name)
		{
			super(name);
			this.converter = new LongConverter();
			this.converter.setNeedThrow(true);
		}

		public Object readCall(CallableStatement call, int index)
				throws SQLException
		{
			long longValue = call.getLong(index);
			return call.wasNull() ? null : new Long(longValue);
		}

		public Object readResult(ResultSet rs)
				throws SQLException
		{
			long longValue = this.useIndexOrName || this.transIndex(rs) ?
					rs.getLong(this.columnIndex) : rs.getLong(this.columnName);
			return rs.wasNull() ? null : new Long(longValue);
		}

	}

	public static class FloatReader extends ObjectReader
	{
		public int getType()
		{
			return TypeManager.TYPE_FLOAT;
		}

		public FloatReader(String name)
		{
			super(name);
			this.converter = new FloatConverter();
			this.converter.setNeedThrow(true);
		}

		public Object readCall(CallableStatement call, int index)
				throws SQLException
		{
			float floatValue = call.getFloat(index);
			return call.wasNull() ? null : new Float(floatValue);
		}

		public Object readResult(ResultSet rs)
				throws SQLException
		{
			float floatValue = this.useIndexOrName || this.transIndex(rs) ?
					rs.getFloat(this.columnIndex) : rs.getFloat(this.columnName);
			return rs.wasNull() ? null : new Float(floatValue);
		}

	}

	public static class DoubleReader extends ObjectReader
	{
		public int getType()
		{
			return TypeManager.TYPE_DOUBLE;
		}

		public DoubleReader(String name)
		{
			super(name);
			this.converter = new DoubleConverter();
			this.converter.setNeedThrow(true);
		}

		public Object readCall(CallableStatement call, int index)
				throws SQLException
		{
			double doubleValue = call.getDouble(index);
			return call.wasNull() ? null : new Double(doubleValue);
		}

		public Object readResult(ResultSet rs)
				throws SQLException
		{
			double doubleValue = this.useIndexOrName || this.transIndex(rs) ?
					rs.getDouble(this.columnIndex) : rs.getDouble(this.columnName);
			return rs.wasNull() ? null : new Double(doubleValue);
		}

	}

	public static class BytesReader extends ObjectReader
	{
		public int getType()
		{
			return TypeManager.TYPE_BYTES;
		}

		public BytesReader(String name)
		{
			super(name);
			this.converter = new BytesConverter();
			this.converter.setNeedThrow(true);
		}

		public Object readCall(CallableStatement call, int index)
				throws SQLException
		{
			return call.getBytes(index);
		}

		public Object readResult(ResultSet rs)
				throws SQLException
		{
			return this.useIndexOrName || this.transIndex(rs) ?
					rs.getBytes(this.columnIndex): rs.getBytes(this.columnName);
		}

	}

	public static class DateReader extends ObjectReader
	{
		public int getType()
		{
			return TypeManager.TYPE_DATE;
		}

		public DateReader(String name)
		{
			super(name);
			this.converter = new DateConverter();
			this.converter.setNeedThrow(true);
		}

		public Object readCall(CallableStatement call, int index)
				throws SQLException
		{
			return call.getDate(index);
		}

		public Object readResult(ResultSet rs)
				throws SQLException
		{
			return this.useIndexOrName || this.transIndex(rs) ?
					rs.getDate(this.columnIndex) : rs.getDate(this.columnName);
		}

	}

	public static class TimeReader extends ObjectReader
	{
		public int getType()
		{
			return TypeManager.TYPE_TIME;
		}

		public TimeReader(String name)
		{
			super(name);
			this.converter = new TimeConverter();
			this.converter.setNeedThrow(true);
		}

		public Object readCall(CallableStatement call, int index)
				throws SQLException
		{
			return call.getTime(index);
		}

		public Object readResult(ResultSet rs)
				throws SQLException
		{
			return this.useIndexOrName || this.transIndex(rs) ?
					rs.getTime(this.columnIndex) : rs.getTime(this.columnName);
		}

	}

	public static class TimestampReader extends ObjectReader
	{
		public int getType()
		{
			return TypeManager.TYPE_TIMPSTAMP;
		}

		public TimestampReader(String name)
		{
			super(name);
			this.converter = new TimestampConverter();
			this.converter.setNeedThrow(true);
		}

		public Object readCall(CallableStatement call, int index)
				throws SQLException
		{
			return call.getTimestamp(index);
		}

		public Object readResult(ResultSet rs)
				throws SQLException
		{
			return this.useIndexOrName || this.transIndex(rs) ?
					rs.getTimestamp(this.columnIndex) : rs.getTimestamp(this.columnName);
		}

	}

	private static Map typeClassMap = new HashMap();

	static
	{
		ResultReaders.typeClassMap.put("Object", ObjectReader.class);
		ResultReaders.typeClassMap.put("String", StringReader.class);
		ResultReaders.typeClassMap.put("boolean", BooleanReader.class);
		ResultReaders.typeClassMap.put("byte", ByteReader.class);
		ResultReaders.typeClassMap.put("short", ShortReader.class);
		ResultReaders.typeClassMap.put("int", IntegerReader.class);
		ResultReaders.typeClassMap.put("long", LongReader.class);
		ResultReaders.typeClassMap.put("float", FloatReader.class);
		ResultReaders.typeClassMap.put("double", DoubleReader.class);
		ResultReaders.typeClassMap.put("Bytes", BytesReader.class);
		ResultReaders.typeClassMap.put("Date", DateReader.class);
		ResultReaders.typeClassMap.put("Time", TimeReader.class);
		ResultReaders.typeClassMap.put("Datetime", TimestampReader.class);
		ResultReaders.typeClassMap.put("Timestamp", TimestampReader.class);
		ResultReaders.typeClassMap.put("BigString", BigStringReader.class);
		ResultReaders.typeClassMap.put("Stream", StreamReader.class);
		ResultReaders.typeClassMap.put("Reader", ReaderReader.class);
		ResultReaders.typeClassMap.put("ignore", NullResultReader.class);
	}

	public static ResultReader createReader(String type, String name)
			throws EternaException
	{
		Class c = (Class) ResultReaders.typeClassMap.get(type);
		if (c == null)
		{
			throw new EternaException("Can't create [ResultReader] type:" + type + ".");
		}
		return ResultReaders.createReader(c, name);
	}

	public static ResultReader createReader(Class type, String name)
			throws EternaException
	{
		if (type == null)
		{
			throw new NullPointerException();
		}
		if (!ResultReader.class.isAssignableFrom(type))
		{
			throw new EternaException(ClassGenerator.getClassName(type)
					+ " is not instance of " + ClassGenerator.getClassName(ObjectReader.class));
		}
		try
		{
			Constructor ct = type.getConstructor(new Class[]{String.class});
			return (ResultReader) ct.newInstance(new Object[]{name});
		}
		catch (Exception ex)
		{
			SQLManager.log.warn("createReader:" + name, ex);
			throw new EternaException("Can't create [ResultReader] class:"
					+ ClassGenerator.getClassName(type) + ".");
		}
	}

}