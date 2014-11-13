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

package self.micromagic.util.ext;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.DatabaseMetaData;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.util.Map;

/**
 * 一个简易的数据库连接覆盖类, 当没有设置autoCommit属性时, 则忽略
 * 对setAutoCommit commit rollback这些方法的调用. 因为对一些不支持
 * 事务的数据库驱动, 调用这些方法时会出错, 在这里屏蔽掉, 在使用时
 * 就无需考虑事务相关的方法是否可调用了.
 */
public class SimpleConnection
		implements Connection
{
	private Connection oldConn;
	private boolean autoCommitSetted;

	public SimpleConnection(boolean autoCommitSetted, Connection oldConn)
	{
		this.autoCommitSetted = autoCommitSetted;
		this.oldConn = oldConn;
	}

	public Statement createStatement()
			throws SQLException
	{
		return this.oldConn.createStatement();
	}

	public PreparedStatement prepareStatement(String sql)
			throws SQLException
	{
		return this.oldConn.prepareStatement(sql);
	}

	public CallableStatement prepareCall(String sql)
			throws SQLException
	{
		return this.oldConn.prepareCall(sql);
	}

	public String nativeSQL(String sql)
			throws SQLException
	{
		return this.oldConn.nativeSQL(sql);
	}

	public void setAutoCommit(boolean autoCommit)
			throws SQLException
	{
		if (this.autoCommitSetted)
		{
			this.oldConn.setAutoCommit(autoCommit);
		}
	}

	public boolean getAutoCommit()
			throws SQLException
	{
		if (this.autoCommitSetted)
		{
			this.oldConn.getAutoCommit();
		}
		return true;
	}

	public void commit()
			throws SQLException
	{
		if (this.autoCommitSetted)
		{
			this.oldConn.commit();
		}
	}

	public void rollback()
			throws SQLException
	{
		if (this.autoCommitSetted)
		{
			this.oldConn.rollback();
		}
	}

	public void close()
			throws SQLException
	{
		this.oldConn.close();
	}

	public boolean isClosed()
			throws SQLException
	{
		return this.oldConn.isClosed();
	}

	public DatabaseMetaData getMetaData()
			throws SQLException
	{
		return this.oldConn.getMetaData();
	}

	public void setReadOnly(boolean readOnly)
			throws SQLException
	{
		this.oldConn.setReadOnly(readOnly);
	}

	public boolean isReadOnly()
			throws SQLException
	{
		return this.oldConn.isReadOnly();
	}

	public void setCatalog(String catalog)
			throws SQLException
	{
		this.oldConn.setCatalog(catalog);
	}

	public String getCatalog()
			throws SQLException
	{
		return this.oldConn.getCatalog();
	}

	public void setTransactionIsolation(int level)
			throws SQLException
	{
		if (this.autoCommitSetted)
		{
			this.oldConn.setTransactionIsolation(level);
		}
	}

	public int getTransactionIsolation()
			throws SQLException
	{
		return this.oldConn.getTransactionIsolation();
	}

	public SQLWarning getWarnings()
			throws SQLException
	{
		return this.oldConn.getWarnings();
	}

	public void clearWarnings()
			throws SQLException
	{
		this.oldConn.clearWarnings();
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency)
			throws SQLException
	{
		return this.oldConn.createStatement(resultSetType, resultSetConcurrency);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
			throws SQLException
	{
		return this.oldConn.prepareStatement(sql, resultSetType, resultSetConcurrency);
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
			throws SQLException
	{
		return this.oldConn.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	public Map getTypeMap()
			throws SQLException
	{
		return this.oldConn.getTypeMap();
	}

	public void setTypeMap(Map map)
			throws SQLException
	{
		this.oldConn.setTypeMap(map);
	}

	public void setHoldability(int holdability)
			throws SQLException
	{
		this.oldConn.setHoldability(holdability);
	}

	public int getHoldability()
			throws SQLException
	{
		return this.oldConn.getHoldability();
	}

	public Savepoint setSavepoint()
			throws SQLException
	{
		if (this.autoCommitSetted)
		{
			return this.oldConn.setSavepoint();
		}
		return null;
	}

	public Savepoint setSavepoint(String name)
			throws SQLException
	{
		if (this.autoCommitSetted)
		{
			return this.oldConn.setSavepoint(name);
		}
		return null;
	}

	public void rollback(Savepoint savepoint)
			throws SQLException
	{
		if (this.autoCommitSetted)
		{
			this.oldConn.rollback(savepoint);
		}
	}

	public void releaseSavepoint(Savepoint savepoint)
			throws SQLException
	{
		if (this.autoCommitSetted)
		{
			this.oldConn.releaseSavepoint(savepoint);
		}
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
			throws SQLException
	{
		return this.oldConn.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability)
			throws SQLException
	{
		return this.oldConn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability)
			throws SQLException
	{
		return this.oldConn.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
			throws SQLException
	{
		return this.oldConn.prepareStatement(sql, autoGeneratedKeys);
	}

	public PreparedStatement prepareStatement(String sql, int columnIndexes[])
			throws SQLException
	{
		return this.oldConn.prepareStatement(sql, columnIndexes);
	}

	public PreparedStatement prepareStatement(String sql, String columnNames[])
			throws SQLException
	{
		return this.oldConn.prepareStatement(sql, columnNames);
	}

}