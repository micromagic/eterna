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

import java.util.Map;

import javax.naming.Context;
import javax.sql.DataSource;

import self.micromagic.eterna.share.EternaException;

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