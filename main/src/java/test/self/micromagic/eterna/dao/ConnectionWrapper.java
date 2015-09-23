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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Map;

public class ConnectionWrapper
		implements Connection
{
	private final Connection base;

	public ConnectionWrapper(Connection base)
	{
		this.base = base;
	}

	public Statement createStatement()
			throws SQLException
	{
		return this.base.createStatement();
	}

	public PreparedStatement prepareStatement(String sql)
			throws SQLException
	{
		return this.base.prepareStatement(sql);
	}

	public CallableStatement prepareCall(String sql)
			throws SQLException
	{
		return this.base.prepareCall(sql);
	}

	public String nativeSQL(String sql)
			throws SQLException
	{
		return this.base.nativeSQL(sql);
	}

	public void setAutoCommit(boolean autoCommit)
			throws SQLException
	{
		this.base.setAutoCommit(autoCommit);
	}

	public boolean getAutoCommit()
			throws SQLException
	{
		return this.base.getAutoCommit();
	}

	public void commit()
			throws SQLException
	{
		this.base.commit();
	}

	public void rollback()
			throws SQLException
	{
		this.base.rollback();
	}

	public void close()
			throws SQLException
	{
		this.base.close();
	}

	public boolean isClosed()
			throws SQLException
	{
		return this.base.isClosed();
	}

	public DatabaseMetaData getMetaData()
			throws SQLException
	{
		return this.base.getMetaData();
	}

	public void setReadOnly(boolean readOnly)
			throws SQLException
	{
		this.base.setReadOnly(readOnly);
	}

	public boolean isReadOnly()
			throws SQLException
	{
		return this.base.isReadOnly();
	}

	public void setCatalog(String catalog)
			throws SQLException
	{
		this.base.setCatalog(catalog);
	}

	public String getCatalog()
			throws SQLException
	{
		return this.base.getCatalog();
	}

	public void setTransactionIsolation(int level)
			throws SQLException
	{
		this.base.setTransactionIsolation(level);
	}

	public int getTransactionIsolation()
			throws SQLException
	{
		return this.base.getTransactionIsolation();
	}

	public SQLWarning getWarnings()
			throws SQLException
	{
		return this.base.getWarnings();
	}

	public void clearWarnings()
			throws SQLException
	{
		this.base.clearWarnings();
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency)
			throws SQLException
	{
		return this.base.createStatement(resultSetType, resultSetConcurrency);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
			throws SQLException
	{
		return this.base.prepareStatement(sql, resultSetType, resultSetConcurrency);
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
			throws SQLException
	{
		return this.base.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	public Map getTypeMap()
			throws SQLException
	{
		return this.base.getTypeMap();
	}

	public void setTypeMap(Map map)
			throws SQLException
	{
		this.base.setTypeMap(map);
	}

	public void setHoldability(int holdability)
			throws SQLException
	{
		this.base.setHoldability(holdability);
	}

	public int getHoldability()
			throws SQLException
	{
		return this.base.getHoldability();
	}

	public Savepoint setSavepoint()
			throws SQLException
	{
		return this.base.setSavepoint();
	}

	public Savepoint setSavepoint(String name)
			throws SQLException
	{
		return this.base.setSavepoint(name);
	}

	public void rollback(Savepoint savepoint)
			throws SQLException
	{
		this.base.rollback(savepoint);
	}

	public void releaseSavepoint(Savepoint savepoint)
			throws SQLException
	{
		this.base.releaseSavepoint(savepoint);
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
			throws SQLException
	{
		return this.base.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability)
			throws SQLException
	{
		return this.base.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability)
			throws SQLException
	{
		return this.base.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
			throws SQLException
	{
		return this.base.prepareStatement(sql, autoGeneratedKeys);
	}

	public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
			throws SQLException
	{
		return this.base.prepareStatement(sql, columnIndexes);
	}

	public PreparedStatement prepareStatement(String sql, String[] columnNames)
			throws SQLException
	{
		return this.base.prepareStatement(sql, columnNames);
	}

}
