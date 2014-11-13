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

package self.micromagic.eterna.model;

import self.micromagic.eterna.digester.ConfigurationException;

public interface SearchExecuteGenerator
{
	/**
	 * search相关的控制标识存入数据集的名称.
	 */
	public static final String SEARCH_MANAGER_ATTRIBUTES = "searchManager_attributes";

	/**
	 * 设置读取search名称的标签名
	 */
	void setSearchNameTag(String tag) throws ConfigurationException;

	void setSearchName(String name) throws ConfigurationException;

	void setQueryResultName(String name) throws ConfigurationException;

	void setSearchManagerName(String name) throws ConfigurationException;

	void setSearchCountName(String name) throws ConfigurationException;

	void setSaveCondition(boolean saveCondition) throws ConfigurationException;

	void setStart(int start) throws ConfigurationException;

	void setCount(int count) throws ConfigurationException;

	void setDoExecute(boolean execute) throws ConfigurationException;

	/**
	 * 设置是否以保持数据库链接的方式查询. <p>
	 * 如果此属性设置了true, 那么此SearchExecute必须在事务方式为hold的model下执行。
	 * 另：此属性设置为true后，会忽略saveCondition, start, count这3个属性。
	 */
	void setHoldConnection(boolean hold) throws ConfigurationException;

	Execute createExecute() throws ConfigurationException;

}