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

package self.micromagic.eterna.share;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;

import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;

public interface DataSourceManager
{
	/**
	 * 在初始化缓存(即工厂管理器的实例)中放置数据源映射表的名称.
	 */
	public static final String DATA_SOURCE_MAP = "dataSourceMap";
	/**
	 * 在初始化缓存(即工厂管理器的实例)中放置默认使用的数据源的名称.
	 */
	public static final String DEFAULT_DATA_SOURCE_NAME = "defaultDataSourceName";

	/**
	 * 初始化这个DataSourceManager.
	 */
	void initialize(EternaFactory factory) throws EternaException;

	DataSource getDefaultDataSource() throws EternaException;

	DataSource getDataSource(String name) throws EternaException;

	Map getDataSourceMap() throws EternaException;

	String getDefaultDataSourceName() throws EternaException;

	void setDefaultDataSourceName(String name) throws EternaException;

	void addDataSource(Context context, String dataSourceConfig)
			throws EternaException;

}

class DataSourceManagerImpl implements DataSourceManager
{
	private String defaultDataSourceName = null;
	private DataSource defaultDataSource = null;

	private Map dataSourceMap = null;

	/**
	 * 初始化这个DataSourceManager.
	 */
	public void initialize(EternaFactory factory)
			throws EternaException
	{
		if (this.dataSourceMap == null)
		{
			throw new EternaException("Not registe any data source.");
		}
		if (this.defaultDataSourceName == null)
		{
			throw new EternaException("Must give this default data source name.");
		}
		this.defaultDataSource = (DataSource) this.dataSourceMap.get(this.defaultDataSourceName);
		if (this.defaultDataSource == null)
		{
			throw new EternaException("Not found the data source:" + this.defaultDataSourceName + ".");
		}

	}

	public DataSource getDefaultDataSource()
	{
		return this.defaultDataSource;
	}

	public DataSource getDataSource(String name)
	{
		return (DataSource) this.dataSourceMap.get(name);
	}

	public Map getDataSourceMap()
	{
		return Collections.unmodifiableMap(this.dataSourceMap);
	}

	protected boolean hasDataSource(String name)
	{
		if (this.dataSourceMap == null)
		{
			return false;
		}
		return this.dataSourceMap.containsKey(name);
	}

	public String getDefaultDataSourceName()
	{
		return this.defaultDataSourceName;
	}

	public void setDefaultDataSourceName(String name)
			throws EternaException
	{
		if (this.defaultDataSource != null)
		{
			throw new EternaException("Can't set default data source name after Initialization.");
		}
		this.defaultDataSourceName = name;
	}

	protected void addDataSource(String name, DataSource ds)
	{
		if (this.dataSourceMap == null)
		{
			this.dataSourceMap = new HashMap();
		}
		this.dataSourceMap.put(name, ds);
	}

	public void addDataSource(Context context, String dataSourceConfig)
			throws EternaException
	{
		String[] items = StringTool.separateString(
				Utility.resolveDynamicPropnames(dataSourceConfig), ";", true);
		try
		{
			for (int i = 0; i < items.length; i++)
			{
				if (this.dataSourceMap == null)
				{
					this.dataSourceMap = new HashMap();
				}
				String item = items[i];
				if (item.length() > 0)
				{
					int index = item.indexOf('=');
					if (index == -1)
					{
						throw new EternaException("Error DataSource define:" + item + ".");
					}
					String key = item.substring(0, index).trim();
					if (this.dataSourceMap.containsKey(key))
					{
						throw new EternaException("Duplicate DataSource name:" + key + ".");
					}
					String name = item.substring(index + 1).trim();
					if (name.length() > 0)
					{
						this.dataSourceMap.put(key, context.lookup(name));
					}
					else
					{
						this.dataSourceMap.put(key, Utility.getDataSource());
					}
				}
			}
		}
		catch (NamingException ex)
		{
			EternaFactoryImpl.log.error("Error when get jdbc in jndi.", ex);
			throw new EternaException(ex);
		}
	}

}
