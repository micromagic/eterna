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

package self.micromagic.eterna.dao;

import self.micromagic.eterna.share.EternaException;

public interface ResultMetaData
{
	/**
	 * 获取<code>ResultIterator</code>对应的<code>QueryAdapter</code>.
	 */
	Query getQuery() throws EternaException;

	/**
	 * 获取<code>ResultIterator</code>对应的<code>ResultReaderManager</code>.
	 */
	ResultReaderManager getReaderManager() throws EternaException;

	/**
	 * 获取<code>ResultIterator</code>对应的名称.
	 * 此名称可以是对应query的名字, 也可以是对应reader-manager的名字.
	 */
	String getName() throws EternaException;

	/**
	 * 获取<code>ResultIterator</code>中列的个数.
	 */
	int getColumnCount() throws EternaException;

	/**
	 * 获取某列的显示标题.
	 *
	 * @param column 第一列为1, 第二列为2, ...
	 */
	String getColumnCaption(int column) throws EternaException;

	/**
	 * 获取某列的名称.
	 *
	 * @param column 第一列为1, 第二列为2, ...
	 */
	String getColumnName(int column) throws EternaException;

	/**
	 * 获取用于读取该列的ResultReader对象.
	 *
	 * @param column 第一列为1, 第二列为2, ...
	 */
	ResultReader getColumnReader(int column) throws EternaException;

	/**
	 * 根据列名查找此列所在的索引值.
	 *
	 * @param columnName 某列的名称
	 * @return  该列所在的索引值
	 *          第一列为1, 第二列为2, ...
	 */
	int findColumn(String columnName) throws EternaException;

	/**
	 * 根据列名查找此列所在的索引值.
	 *
	 * @param columnName  某列的名称
	 * @param notThrow    设为<code>true<code>时, 当该列名不存在时不会抛出异常,
	 *                    而只是返回-1
	 * @return  该列所在所在的索引值, 或-1(当该列名不存在时)
	 *          第一列为1, 第二列为2, ...
	 */
	int findColumn(String columnName, boolean notThrow) throws EternaException;

}