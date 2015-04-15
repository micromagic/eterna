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

import self.micromagic.eterna.share.EternaException;

public interface QueryAdapterGenerator extends SQLAdapterGenerator
{
	/**
	 * 设置排序的子sql语句所在的位置.
	 */
	void setOrderIndex(int index) throws EternaException;

	/**
	 * 设置这句查询是否只能用forwardOnly模式来执行.
	 */
	void setForwardOnly(boolean forwardOnly) throws EternaException;

	/**
	 * 设置ResultReader的排序方式字符串.
	 */
	void setReaderOrder(String readerOrder) throws EternaException;

	/**
	 * 设置继承的ResultReaderManager的名称.
	 */
	void setReaderManagerName(String name) throws EternaException;

	/**
	 * 在继承的ResultReaderManager的基础上添加一个ResultReader, 如果
	 * 名称与ResultReaderManager中的重复, 则覆盖原来的.
	 */
	void addResultReader(ResultReader reader) throws EternaException;

	/**
	 * 获得一个<code>QueryAdapter</code>的实例. <p>
	 *
	 * @return <code>QueryAdapter</code>的实例.
	 * @throws EternaException     当相关配置出错时.
	 */
	QueryAdapter createQueryAdapter() throws EternaException;

}