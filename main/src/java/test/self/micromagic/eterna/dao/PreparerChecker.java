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

package self.micromagic.eterna.dao;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;

import junit.framework.Assert;

/**
 * 用于检查参数的设置情况.
 */
public class PreparerChecker
		implements PreparedStatementWrap
{
	private final Object[] values;
	private final int sqlType;

	public PreparerChecker(Object[] values)
	{
		this.values = values;
		this.sqlType = Types.NULL;
	}

	public PreparerChecker(int sqlType)
	{
		this.values = new Object[]{null};
		this.sqlType = sqlType;
	}

	public void setNull(String parameterName, int index, int sqlType)
	{
		Assert.assertEquals("i-" + index, values[index - 1], null);
		if (this.sqlType != Types.NULL)
		{
			Assert.assertEquals("i-" + index, this.sqlType, sqlType);
		}
	}

	public void setBoolean(String parameterName, int index, boolean x)
	{
		Assert.assertEquals("i-" + index, values[index - 1],
				x ? Boolean.TRUE : Boolean.FALSE);
	}

	public void setByte(String parameterName, int index, byte x)
	{
		Assert.assertEquals("i-" + index, values[index - 1], new Byte(x));
	}

	public void setShort(String parameterName, int index, short x)
	{
		Assert.assertEquals("i-" + index, values[index - 1], new Short(x));
	}

	public void setInt(String parameterName, int index, int x)
	{
		Assert.assertEquals("i-" + index, values[index - 1], new Integer(x));
	}

	public void setLong(String parameterName, int index, long x)
	{
		Assert.assertEquals("i-" + index, values[index - 1], new Long(x));
	}

	public void setFloat(String parameterName, int index, float x)
	{
		Assert.assertEquals("i-" + index, values[index - 1], new Float(x));
	}

	public void setDouble(String parameterName, int index, double x)
	{
		Assert.assertEquals("i-" + index, values[index - 1], new Double(x));
	}

	public void setString(String parameterName, int index, String x)
	{
		Assert.assertEquals("i-" + index, values[index - 1], x);
	}

	public void setBytes(String parameterName, int index, byte x[])
	{
		throw new UnsupportedOperationException();
	}

	public void setDate(String parameterName, int index, Date x)
	{
		Assert.assertEquals("i-" + index, values[index - 1], x);
	}

	public void setDate(String parameterName, int index, Date x, Calendar cal)
	{
		Assert.assertEquals("i-" + index, values[index - 1], x);
	}

	public void setTime(String parameterName, int index, Time x)
	{
		Assert.assertEquals("i-" + index, values[index - 1], x);
	}

	public void setTime(String parameterName, int index, Time x, Calendar cal)
	{
		Assert.assertEquals("i-" + index, values[index - 1], x);
	}

	public void setTimestamp(String parameterName, int index, Timestamp x)
	{
		Assert.assertEquals("i-" + index, values[index - 1], x);
	}

	public void setTimestamp(String parameterName, int index, Timestamp x, Calendar cal)
	{
		Assert.assertEquals("i-" + index, values[index - 1], x);
	}

	public void setBinaryStream(String parameterName, int index, InputStream x, int length)
	{
		throw new UnsupportedOperationException();
	}

	public void setCharacterStream(String parameterName, int index, Reader reader, int length)
	{
		throw new UnsupportedOperationException();
	}

	public void setBlob(String parameterName, int index, Blob blob)
	{
		throw new UnsupportedOperationException();
	}

	public void setClob(String parameterName, int index, Clob clob)
	{
		throw new UnsupportedOperationException();
	}

	public void setObject(String parameterName, int index, Object x, int targetSqlType, int scale)
	{
		Assert.assertEquals("i-" + index, values[index - 1], x);
	}

	public void setObject(String parameterName, int index, Object x, int targetSqlType)
	{
		Assert.assertEquals("i-" + index, values[index - 1], x);
	}

	public void setObject(String parameterName, int index, Object x)
	{
		Assert.assertEquals("i-" + index, values[index - 1], x);
	}

}
