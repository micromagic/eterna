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
import java.sql.SQLException;
import java.sql.Driver;
import java.io.PrintWriter;
import java.util.Properties;

import javax.sql.DataSource;

import self.micromagic.util.ext.SimpleConnection;
import self.micromagic.util.Utility;

/**
 * 一个简易的数据源实现, 不使用数据库连接缓冲池, 关闭后直接释放.
 */
public class SimpleDataSource
		implements DataSource
{
	protected PrintWriter logWriter;
	protected int loginTimeout;

	protected String description;
	protected String url;
	protected String driverClass;
	protected String user;
	protected String password;
	protected boolean autoCommit = true;
	protected boolean autoCommitSetted;

	/**
	 * 数据库的驱动类.
	 */
	protected Driver driver;

	/**
	 * 默认的连接属性.
	 */
	protected Properties defaultProperties;

	/**
	 * 获取数据库连接.
	 */
	public Connection getConnection()
			throws SQLException
	{
		return this.getConnection(null, null);
	}

	/**
	 * 根据指定的用户名及密码获取数据库连接.
	 */
	public Connection getConnection(String username, String password)
			throws SQLException
	{
		if (this.url == null)
		{
			throw new SQLException("The connetion url hasn't setted!");
		}
		if (this.driverClass == null)
		{
			throw new SQLException("The connetion driverClass hasn't setted!");
		}
		if (this.driver == null)
		{
			try
			{
				this.driver = createDriver(this.driverClass);
			}
			catch (Exception ex)
			{
				throw new SQLException("open: " + ex);
			}
		}
		Connection conn;
		if (username == null && password == null)
		{
			conn = new SimpleConnection(this.autoCommitSetted,
					this.driver.connect(this.url, this.getDefaultProperties()));
		}
		else
		{
			Properties p = new Properties();
			p.setProperty("user", user);
			p.setProperty("password", password);
			conn = new SimpleConnection(this.autoCommitSetted, this.driver.connect(this.url, p));
		}
		conn.setAutoCommit(this.autoCommit);
		return conn;
	}

	/**
	 * 设置获取的数据库连接默认是否需要自动提交.
	 */
	public void setAutoCommit(boolean autoCommit)
	{
		this.autoCommitSetted = true;
		this.autoCommit = autoCommit;
	}

	/**
	 * 设置连接数据库使用的驱动类.
	 */
	public void setDriverClass(String driverClass)
	{
		this.driverClass = driverClass;
	}

	/**
	 * 设置连接数据库使用的连接字符串.
	 */
	public void setUrl(String url)
	{
		this.url = url;
		if (this.description == null)
		{
			this.description = url;
		}
	}

	/**
	 * 设置连接数据库使用的用户名.
	 */
	public void setUser(String user)
	{
		this.user = user;
	}

	/**
	 * 设置连接数据库使用的密码.
	 */
	public void setPassword(String password)
	{
		this.password = password;
	}

	/**
	 * 获取此数据源的说明.
	 */
	public String getDescription()
	{
		return this.description;
	}

	/**
	 * 设置对此数据源的说明.
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 * 获取autoCommit属性是否被设置过, <code>true</code>表示设置过.
	 */
	public boolean isAutoCommitSetted()
	{
		return autoCommitSetted;
	}

	/**
	 * 根据给出的类名, 创建数据库的驱动类.
	 */
	private static Driver createDriver(String className)
			throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		return (Driver) Utility.getContextClassLoader().loadClass(className).newInstance();
	}

	/**
	 * 获取默认的连接属性.
	 */
	private Properties getDefaultProperties()
	{
		if (this.defaultProperties == null)
		{
			synchronized (this)
			{
				if (this.defaultProperties == null)
				{
					this.defaultProperties = new Properties();
					this.defaultProperties.setProperty("user", this.user);
					this.defaultProperties.setProperty("password", this.password);
				}
			}
		}
		return this.defaultProperties;
	}

	public PrintWriter getLogWriter()
			throws SQLException
	{
		return this.logWriter;
	}

	public void setLogWriter(PrintWriter out)
			throws SQLException
	{
		this.logWriter = out;
	}

	public void setLoginTimeout(int seconds)
			throws SQLException
	{
		this.loginTimeout = seconds;
	}

	public int getLoginTimeout()
			throws SQLException
	{
		return this.loginTimeout;
	}

}