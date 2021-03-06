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

import java.io.InputStream;
import java.io.Reader;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

import self.micromagic.eterna.dao.ModifiableResultRow;
import self.micromagic.eterna.dao.ResultFormat;
import self.micromagic.eterna.dao.ResultIterator;
import self.micromagic.eterna.dao.ResultMetaData;
import self.micromagic.eterna.dao.ResultReader;
import self.micromagic.eterna.security.Permission;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
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

public class ResultRowImpl implements ModifiableResultRow
{
	private static final Object NULL_FLAG = new Object();

	private final Object[] values;
	private final Object[] formateds;
	private final Permission permission;
	private final ResultMetaData metaData;
	private final ResultIterator resultIterator;

	private int rowNum;
	private Object[] oldValues;
	private boolean wasNull;

	public ResultRowImpl(Object[] values, ResultIterator resultIterator, int rowNum, Permission permission)
			throws EternaException, SQLException
	{
		this.values = values;
		this.formateds = new Object[values.length];
		this.permission = permission;
		this.resultIterator = resultIterator;
		this.rowNum = rowNum;
		this.metaData = resultIterator.getMetaData();
	}

	/**
	 * 用于复制一个ResultRowImpl, 用于更新ResultIterator.
	 *
	 * @param old     原始的ResultRowImpl
	 * @param newItr  新的ResultIterator
	 */
	ResultRowImpl(ResultRowImpl old, ResultIterator newItr)
	{
		this.values = old.values;
		this.oldValues = old.oldValues;
		this.formateds = old.formateds;
		this.permission = old.permission;
		this.resultIterator = newItr;
		this.rowNum = old.rowNum;
		this.metaData = old.metaData;
	}

	public ResultIterator getResultIterator()
	{
		return this.resultIterator;
	}

	public int getRowNum()
	{
		return this.rowNum;
	}

	public void setRowNum(int num)
	{
		this.rowNum = num;
	}

	public boolean isModified()
	{
		return this.oldValues != null;
	}

	public void setValue(int columnIndex, Object v)
			throws EternaException
	{
		if (this.oldValues == null)
		{
			this.oldValues = new Object[this.values.length];
			System.arraycopy(this.values, 0, this.oldValues, 0, this.values.length);
		}
		this.wasNull = v == null;
		this.values[columnIndex - 1] = v;
		this.formateds[columnIndex - 1] = null;
		if (v != null)
		{
			ResultReader reader = this.metaData.getColumnReader(columnIndex);
			if (reader.getFormat() != null && !this.checkType(reader.getType(), v))
			{
				// 设置了format且数据类型与定义的类型不匹配, 直接设置掉format的结果
				this.formateds[columnIndex - 1] = v;
			}
		}
	}

	/**
	 * 检查给出的数据是否与定义的类型相符.
	 */
	private boolean checkType(int defineType, Object v)
	{
		if (TypeManager.isBoolean(defineType) && v instanceof Boolean)
		{
			return true;
		}
		if (TypeManager.isNumber(defineType) && v instanceof Number)
		{
			return true;
		}
		if (TypeManager.isDate(defineType) && v instanceof java.util.Date)
		{
			return true;
		}
		if (TypeManager.isString(defineType) && v instanceof CharSequence)
		{
			return true;
		}
		return false;
	}

	public void setValue(String columnName, Object v)
			throws EternaException
	{
		int index = this.metaData.findColumn(columnName, false);
		this.setValue(index, v);
	}

	public boolean hasFormated(int columnIndex)
	{
		return this.formateds[columnIndex - 1] != null;
	}

	public void setFormated(int columnIndex, Object v)
	{
		this.formateds[columnIndex - 1] = v == null ? NULL_FLAG : v;
	}

	public void setFormated(String columnName, Object v)
	{
		int index = this.metaData.findColumn(columnName, false);
		this.setFormated(index, v);
	}

	public Object getSmartValue(int columnIndex)
			throws EternaException, SQLException
	{
		ResultReader reader = this.metaData.getColumnReader(columnIndex);
		if (reader.getFormat() == null)
		{
			return this.getObject(columnIndex);
		}
		return this.getFormated(columnIndex, false);
	}

	public Object getSmartValue(String columnName, boolean notThrow)
			throws EternaException, SQLException
	{
		int index = this.metaData.findColumn(columnName, notThrow);
		if (index == -1)
		{
			return null;
		}
		return this.getSmartValue(index);
	}

	public Object getFormated(int columnIndex)
			throws SQLException, EternaException
	{
		return this.getFormated(columnIndex, false);
	}

	public Object getFormated(int columnIndex, boolean old)
			throws EternaException
	{
		int cIndex = columnIndex - 1;
		Object v;
		Object tmp;
		if (old)
		{
			v = this.oldValues != null ? this.oldValues[cIndex] : this.values[cIndex];
			// 未修改过的值可以直接使用缓存的格式化结果
			tmp = v == this.values[cIndex] ? this.formateds[cIndex] : null;
			if (tmp != null)
			{
				return tmp == NULL_FLAG ? null : tmp;
			}
		}
		else
		{
			v = this.values[cIndex];
			this.wasNull = v == null;
			tmp = this.formateds[cIndex];
			if (tmp != null)
			{
				return tmp == NULL_FLAG ? null : tmp;
			}
		}
		ResultReader reader = this.metaData.getColumnReader(columnIndex);
		ResultFormat format = reader.getFormat();
		if (format == null)
		{
			if (v == null)
			{
				tmp = "";
			}
			else
			{
				tmp = strConvert.convertToString(this.values[cIndex]);
				if (tmp == null)
				{
					tmp = "";
				}
			}
		}
		else
		{
			try
			{
				tmp = format.format(v, this, reader, this.permission);
			}
			catch (Exception ex)
			{
				DaoManager.log.warn(this.getFormatErrMsg(columnIndex), ex);
			}
			if (tmp == null)
			{
				tmp = format.useEmptyString() ? "" : NULL_FLAG;
			}
		}
		if (!old)
		{
			this.formateds[cIndex] = tmp;
		}
		return tmp == NULL_FLAG ? null : tmp;
	}

	public Object getFormated(String columnName)
			throws EternaException
	{
		int index = this.metaData.findColumn(columnName, false);
		return this.getFormated(index, false);
	}

	private String getFormatErrMsg(int columnIndex)
			throws EternaException
	{
		StringAppender buf = StringTool.createStringAppender(128);
		buf.append("When format the column [").append(columnIndex).append(':')
				.append(this.metaData.getColumnReader(columnIndex).getName())
				.append("] in ").append(this.metaData.getName())
				.append(", value [").append(this.values[columnIndex - 1]).append("].");
		return buf.toString();
	}

	public boolean wasNull()
	{
		return this.wasNull;
	}

	public String getString(int columnIndex)
			throws EternaException
	{
		this.wasNull = this.values[columnIndex - 1] == null;
		return strConvert.convertToString(this.values[columnIndex - 1]);
	}

	public boolean getBoolean(int columnIndex)
			throws EternaException
	{
		this.wasNull = this.values[columnIndex - 1] == null;
		return boolConvert.getResult(this.values[columnIndex - 1]);
	}

	public byte getByte(int columnIndex)
			throws EternaException
	{
		this.wasNull = this.values[columnIndex - 1] == null;
		return byteConvert.getResult(this.values[columnIndex - 1]);
	}

	public short getShort(int columnIndex)
			throws EternaException
	{
		this.wasNull = this.values[columnIndex - 1] == null;
		return shortConvert.getResult(this.values[columnIndex - 1]);
	}

	public int getInt(int columnIndex)
			throws EternaException
	{
		this.wasNull = this.values[columnIndex - 1] == null;
		return intConvert.getResult(this.values[columnIndex - 1]);
	}

	public long getLong(int columnIndex)
			throws EternaException
	{
		this.wasNull = this.values[columnIndex - 1] == null;
		return longConvert.getResult(this.values[columnIndex - 1]);
	}

	public float getFloat(int columnIndex)
			throws EternaException
	{
		this.wasNull = this.values[columnIndex - 1] == null;
		return floatConvert.getResult(this.values[columnIndex - 1]);
	}

	public double getDouble(int columnIndex)
			throws EternaException
	{
		this.wasNull = this.values[columnIndex - 1] == null;
		return doubleConvert.getResult(this.values[columnIndex - 1]);
	}

	public byte[] getBytes(int columnIndex)
			throws EternaException
	{
		this.wasNull = this.values[columnIndex - 1] == null;
		return bytesConvert.getResult(this.values[columnIndex - 1]);
	}

	public Date getDate(int columnIndex)
			throws EternaException
	{
		this.wasNull = this.values[columnIndex - 1] == null;
		return dateConvert.getResult(this.values[columnIndex - 1]);
	}

	public Time getTime(int columnIndex)
			throws EternaException
	{
		this.wasNull = this.values[columnIndex - 1] == null;
		return timeConvert.getResult(this.values[columnIndex - 1]);
	}

	public Timestamp getTimestamp(int columnIndex)
			throws EternaException
	{
		this.wasNull = this.values[columnIndex - 1] == null;
		return timestampConvert.getResult(this.values[columnIndex - 1]);
	}

	public String getString(String columnName)
			throws EternaException
	{
		int index = this.metaData.findColumn(columnName, false);
		return this.getString(index);
	}

	public boolean getBoolean(String columnName)
			throws EternaException
	{
		int index = this.metaData.findColumn(columnName, false);
		return this.getBoolean(index);
	}

	public byte getByte(String columnName)
			throws EternaException
	{
		int index = this.metaData.findColumn(columnName, false);
		return this.getByte(index);
	}

	public short getShort(String columnName)
			throws EternaException
	{
		int index = this.metaData.findColumn(columnName, false);
		return this.getShort(index);
	}

	public int getInt(String columnName)
			throws EternaException
	{
		int index = this.metaData.findColumn(columnName, false);
		return this.getInt(index);
	}

	public long getLong(String columnName)
			throws EternaException
	{
		int index = this.metaData.findColumn(columnName, false);
		return this.getLong(index);
	}

	public float getFloat(String columnName)
			throws EternaException
	{
		int index = this.metaData.findColumn(columnName, false);
		return this.getFloat(index);
	}

	public double getDouble(String columnName)
			throws EternaException
	{
		int index = this.metaData.findColumn(columnName, false);
		return this.getDouble(index);
	}

	public byte[] getBytes(String columnName)
			throws EternaException
	{
		int index = this.metaData.findColumn(columnName, false);
		return this.getBytes(index);
	}

	public Date getDate(String columnName)
			throws EternaException
	{
		int index = this.metaData.findColumn(columnName, false);
		return this.getDate(index);
	}

	public Time getTime(String columnName)
			throws EternaException
	{
		int index = this.metaData.findColumn(columnName, false);
		return this.getTime(index);
	}

	public Timestamp getTimestamp(String columnName)
			throws EternaException
	{
		int index = this.metaData.findColumn(columnName, false);
		return this.getTimestamp(index);
	}

	public Object getObject(int columnIndex)
	{
		this.wasNull = this.values[columnIndex - 1] == null;
		return this.values[columnIndex - 1];
	}

	public Object getObject(String columnName)
			throws EternaException
	{
		int index = this.metaData.findColumn(columnName, false);
		return this.getObject(index);
	}

	public Object getObject(String columnName, boolean notThrow)
			throws SQLException, EternaException
	{
		int index = this.metaData.findColumn(columnName, notThrow);
		if (index == -1)
		{
			return null;
		}
		return this.getObject(index);
	}

	public InputStream getBinaryStream(int columnIndex)
			throws EternaException
	{
		this.wasNull = this.values[columnIndex - 1] == null;
		return streamConvert.getResult(this.values[columnIndex - 1]);
	}

	public InputStream getBinaryStream(String columnName)
			throws EternaException
	{
		int index = this.metaData.findColumn(columnName, false);
		return this.getBinaryStream(index);
	}

	public Reader getCharacterStream(int columnIndex)
			throws EternaException
	{
		this.wasNull = this.values[columnIndex - 1] == null;
		return readerConvert.getResult(this.values[columnIndex - 1]);
	}

	public Reader getCharacterStream(String columnName)
			throws EternaException
	{
		int index = this.metaData.findColumn(columnName, false);
		return this.getCharacterStream(index);
	}

	public int findColumn(String columnName)
			throws EternaException
	{
		return this.metaData.findColumn(columnName, false);
	}

	public int findColumn(String columnName, boolean notThrow)
			throws EternaException
	{
		return this.metaData.findColumn(columnName, notThrow);
	}

	static StringConverter strConvert = new StringConverter();
	static BooleanConverter boolConvert = new BooleanConverter();
	static ByteConverter byteConvert = new ByteConverter();
	static ShortConverter shortConvert = new ShortConverter();
	static IntegerConverter intConvert = new IntegerConverter();
	static LongConverter longConvert = new LongConverter();
	static FloatConverter floatConvert = new FloatConverter();
	static DoubleConverter doubleConvert = new DoubleConverter();
	static BytesConverter bytesConvert = new BytesConverter();
	static DateConverter dateConvert = new DateConverter();
	static TimeConverter timeConvert = new TimeConverter();
	static TimestampConverter timestampConvert = new TimestampConverter();
	static StreamConverter streamConvert = new StreamConverter();
	static ReaderConverter readerConvert = new ReaderConverter();

}