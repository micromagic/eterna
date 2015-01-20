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

package self.micromagic.eterna.base.impl;

import java.sql.SQLException;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.io.InputStream;
import java.io.Reader;

import self.micromagic.eterna.base.ResultIterator;
import self.micromagic.eterna.base.ResultRow;
import self.micromagic.eterna.share.EternaException;

/**
 * ResultRow的包装类, 用于修改所属的ResultIterator.
 */
public class ResultRowWrapper
		implements ResultRow
{
	public ResultRowWrapper(ResultIterator ritr, ResultRow base)
	{
		this.ritr = ritr;
		this.base = base;
	}
	private ResultIterator ritr;
	private ResultRow base;

	public ResultIterator getResultIterator()
	{
		return this.ritr;
	}

	public int getRowNum()
			throws SQLException, EternaException
	{
		return this.base.getRowNum();
	}

	public Object getSmartValue(int columnIndex)
			throws SQLException, EternaException
	{
		return this.base.getSmartValue(columnIndex);
	}

	public Object getSmartValue(String columnName, boolean notThrow)
			throws SQLException, EternaException
	{
		return this.base.getSmartValue(columnName, notThrow);
	}

	public Object getFormated(int columnIndex)
			throws SQLException, EternaException
	{
		return this.base.getFormated(columnIndex);
	}

	public Object getFormated(String columnName)
			throws SQLException, EternaException
	{
		return this.base.getFormated(columnName);
	}

	public boolean wasNull()
			throws SQLException, EternaException
	{
		return this.base.wasNull();
	}

	public String getString(int columnIndex)
			throws SQLException, EternaException
	{
		return this.base.getString(columnIndex);
	}

	public boolean getBoolean(int columnIndex)
			throws SQLException, EternaException
	{
		return this.base.getBoolean(columnIndex);
	}

	public byte getByte(int columnIndex)
			throws SQLException, EternaException
	{
		return this.base.getByte(columnIndex);
	}

	public short getShort(int columnIndex)
			throws SQLException, EternaException
	{
		return this.base.getShort(columnIndex);
	}

	public int getInt(int columnIndex)
			throws SQLException, EternaException
	{
		return this.base.getInt(columnIndex);
	}

	public long getLong(int columnIndex)
			throws SQLException, EternaException
	{
		return this.base.getLong(columnIndex);
	}

	public float getFloat(int columnIndex)
			throws SQLException, EternaException
	{
		return this.base.getFloat(columnIndex);
	}

	public double getDouble(int columnIndex)
			throws SQLException, EternaException
	{
		return this.base.getDouble(columnIndex);
	}

	public byte[] getBytes(int columnIndex)
			throws SQLException, EternaException
	{
		return this.base.getBytes(columnIndex);
	}

	public Date getDate(int columnIndex)
			throws SQLException, EternaException
	{
		return this.base.getDate(columnIndex);
	}

	public Time getTime(int columnIndex)
			throws SQLException, EternaException
	{
		return this.base.getTime(columnIndex);
	}

	public Timestamp getTimestamp(int columnIndex)
			throws SQLException, EternaException
	{
		return this.base.getTimestamp(columnIndex);
	}

	public InputStream getBinaryStream(int columnIndex)
			throws SQLException, EternaException
	{
		return this.base.getBinaryStream(columnIndex);
	}

	public String getString(String columnName)
			throws SQLException, EternaException
	{
		return this.base.getString(columnName);
	}

	public boolean getBoolean(String columnName)
			throws SQLException, EternaException
	{
		return this.base.getBoolean(columnName);
	}

	public byte getByte(String columnName)
			throws SQLException, EternaException
	{
		return this.base.getByte(columnName);
	}

	public short getShort(String columnName)
			throws SQLException, EternaException
	{
		return this.base.getShort(columnName);
	}

	public int getInt(String columnName)
			throws SQLException, EternaException
	{
		return this.base.getInt(columnName);
	}

	public long getLong(String columnName)
			throws SQLException, EternaException
	{
		return this.base.getLong(columnName);
	}

	public float getFloat(String columnName)
			throws SQLException, EternaException
	{
		return this.base.getFloat(columnName);
	}

	public double getDouble(String columnName)
			throws SQLException, EternaException
	{
		return this.base.getDouble(columnName);
	}

	public byte[] getBytes(String columnName)
			throws SQLException, EternaException
	{
		return this.base.getBytes(columnName);
	}

	public Date getDate(String columnName)
			throws SQLException, EternaException
	{
		return this.base.getDate(columnName);
	}

	public Time getTime(String columnName)
			throws SQLException, EternaException
	{
		return this.base.getTime(columnName);
	}

	public Timestamp getTimestamp(String columnName)
			throws SQLException, EternaException
	{
		return this.base.getTimestamp(columnName);
	}

	public InputStream getBinaryStream(String columnName)
			throws SQLException, EternaException
	{
		return this.base.getBinaryStream(columnName);
	}

	public Object getObject(int columnIndex)
			throws SQLException, EternaException
	{
		return this.base.getObject(columnIndex);
	}

	public Object getObject(String columnName)
			throws SQLException, EternaException
	{
		return this.base.getObject(columnName);
	}

	public Object getObject(String columnName, boolean notThrow)
			throws SQLException, EternaException
	{
		return this.base.getObject(columnName, notThrow);
	}

	public int findColumn(String columnName)
			throws SQLException, EternaException
	{
		return this.base.findColumn(columnName);
	}

	public int findColumn(String columnName, boolean notThrow)
			throws SQLException, EternaException
	{
		return this.base.findColumn(columnName, notThrow);
	}

	public Reader getCharacterStream(int columnIndex)
			throws SQLException, EternaException
	{
		return this.base.getCharacterStream(columnIndex);
	}

	public Reader getCharacterStream(String columnName)
			throws SQLException, EternaException
	{
		return this.base.getCharacterStream(columnName);
	}

}