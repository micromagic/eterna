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

package self.micromagic.eterna.sql;

import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.share.AdapterGenerator;

public interface SQLAdapterGenerator extends AdapterGenerator
{
	/**
	 * 设置要构造的SQL适配器的名称.
	 */
	void setName(String name) throws ConfigurationException;

	/**
	 * 获取要构造的SQL适配器的名称.
	 */
	String getName() throws ConfigurationException;

	/**
	 * 设置本SQL适配器sql日志的记录方式
	 */
	void setLogType(String logType) throws ConfigurationException;

	/**
	 * 设置预备SQL语句. <p>
	 *
	 * @param sql      要设置的预备SQL语句.
	 * @throws ConfigurationException     当相关配置出错时.
	 */
	void setPreparedSQL(String sql) throws ConfigurationException;

	/**
	 * 清空参数表. <p>
	 *
	 * @throws ConfigurationException     当相关配置出错时.
	 */
	void clearParameters() throws ConfigurationException;

	/**
	 * 添加一个参数. <p>
	 *
	 * @param paramGenerator     参数构造器.
	 * @throws ConfigurationException     当相关配置出错时.
	 */
	void addParameter(SQLParameterGenerator paramGenerator) throws ConfigurationException;

	/**
	 * 添加一个参数组. <p>
	 *
	 * @param groupName     参数组名称.
	 * @param ignoreList    忽略的参数列表.
	 * @throws ConfigurationException     当相关配置出错时.
	 */
	void addParameterRef(String groupName, String ignoreList) throws ConfigurationException;

	/**
	 * 获得一个<code>SQLAdapter</code>的实例. <p>
	 *
	 * @return <code>SQLAdapter</code>的实例.
	 * @throws ConfigurationException     当相关配置出错时.
	 */
	SQLAdapter createSQLAdapter() throws ConfigurationException;

}