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

package self.micromagic.eterna.dao.preparer;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

import self.micromagic.eterna.dao.PreparedStatementWrap;

public class PreparedStatementWrapImpl
		implements PreparedStatementWrap
{
	private final PreparedStatement stmt;

	public PreparedStatementWrapImpl(PreparedStatement stmt)
	{
		this.stmt = stmt;
	}

	public void setNull(String parameterName, int parameterIndex, int sqlType)
			throws SQLException
	{
		this.stmt.setNull(parameterIndex, sqlType);
	}

	public void setBoolean(String parameterName, int parameterIndex, boolean x)
			throws SQLException
	{
		this.stmt.setBoolean(parameterIndex, x);
	}

	public void setByte(String parameterName, int parameterIndex, byte x)
			throws SQLException
	{
		this.stmt.setByte(parameterIndex, x);
	}

	public void setShort(String parameterName, int parameterIndex, short x)
			throws SQLException
	{
		this.stmt.setShort(parameterIndex, x);
	}

	public void setInt(String parameterName, int parameterIndex, int x)
			throws SQLException
	{
		this.stmt.setInt(parameterIndex, x);
	}

	public void setLong(String parameterName, int parameterIndex, long x)
			throws SQLException
	{
		this.stmt.setLong(parameterIndex, x);
	}

	public void setFloat(String parameterName, int parameterIndex, float x)
			throws SQLException
	{
		this.stmt.setFloat(parameterIndex, x);
	}

	public void setDouble(String parameterName, int parameterIndex, double x)
			throws SQLException
	{
		this.stmt.setDouble(parameterIndex, x);
	}

	public void setString(String parameterName, int parameterIndex, String x)
			throws SQLException
	{
		this.stmt.setString(parameterIndex, x);
	}

	public void setBytes(String parameterName, int parameterIndex, byte[] x)
			throws SQLException
	{
		this.stmt.setBytes(parameterIndex, x);
	}

	public void setDate(String parameterName, int parameterIndex, Date x)
			throws SQLException
	{
		this.stmt.setDate(parameterIndex, x);
	}

	public void setDate(String parameterName, int parameterIndex, Date x, Calendar cal)
			throws SQLException
	{
		this.stmt.setDate(parameterIndex, x, cal);
	}

	public void setTime(String parameterName, int parameterIndex, Time x)
			throws SQLException
	{
		this.stmt.setTime(parameterIndex, x);
	}

	public void setTime(String parameterName, int parameterIndex, Time x, Calendar cal)
			throws SQLException
	{
		this.stmt.setTime(parameterIndex, x, cal);
	}

	public void setTimestamp(String parameterName, int parameterIndex, Timestamp x)
			throws SQLException
	{
		this.stmt.setTimestamp(parameterIndex, x);
	}

	public void setTimestamp(String parameterName, int parameterIndex, Timestamp x, Calendar cal)
			throws SQLException
	{
		this.stmt.setTimestamp(parameterIndex, x, cal);
	}

	public void setBinaryStream(String parameterName, int parameterIndex, InputStream x, int length)
			throws SQLException
	{
		this.stmt.setBinaryStream(parameterIndex, x, length);
	}

	public void setCharacterStream(String parameterName, int parameterIndex, Reader x, int length)
			throws SQLException
	{
		this.stmt.setCharacterStream(parameterIndex, x, length);
	}

	public void setBlob(String parameterName, int parameterIndex, Blob blob) throws SQLException
	{
		this.stmt.setBlob(parameterIndex, blob);
	}

	public void setClob(String parameterName, int parameterIndex, Clob clob) throws SQLException
	{
		this.stmt.setClob(parameterIndex, clob);
	}

	public void setObject(String parameterName, int parameterIndex, Object x)
			throws SQLException
	{
		this.stmt.setObject(parameterIndex, x);
	}

	public void setObject(String parameterName, int parameterIndex, Object x, int targetSqlType)
			throws SQLException
	{
		this.stmt.setObject(parameterIndex, x, targetSqlType);
	}

	public void setObject(String parameterName, int parameterIndex, Object x, int targetSqlType, int scale)
			throws SQLException
	{
		this.stmt.setObject(parameterIndex, x, targetSqlType, scale);
	}

}