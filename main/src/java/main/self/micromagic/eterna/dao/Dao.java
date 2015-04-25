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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;

import self.micromagic.eterna.dao.preparer.PreparerManager;
import self.micromagic.eterna.dao.preparer.ValuePreparer;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;

/**
 * 用于处理数据库操作的对象.
 */
public interface Dao
{
	/**
	 * 配置中设置日志方式的标识.
	 */
	String LOG_TYPE_FLAG = "eterna.dao.logType";
	/**
	 * 日志等级, 不记录.
	 */
	int SQL_LOG_TYPE_NONE = -1;
	/**
	 * 日志等级, 记录到内存.
	 */
	int SQL_LOG_TYPE_SAVE = 0x2;
	/**
	 * 日志等级, 记录到系统日志.
	 */
	int SQL_LOG_TYPE_PRINT = 0x1;
	/**
	 * 日志等级, 调用指定的日志记录对象.
	 */
	int SQL_LOG_TYPE_SPECIAL = 0x4;


	String SQL_TYPE_UPDATE = "update";
	String SQL_TYPE_QUERY = "query";
	String SQL_TYPE_COUNT = "count";
	String SQL_TYPE_SQL = "SQL";

	/**
	 * 获取本数据库操作对象的名称.
	 */
	String getName() throws EternaException;

	/**
	 * 获取本数据库操作对象的类型.
	 * 如: query, update.
	 */
	String getType() throws EternaException;

	/**
	 * 获取本数据库操作对象某个设置的属性.
	 */
	Object getAttribute(String name) throws EternaException;

	/**
	 * 获取本数据库操作对象设置的所有属性的名称.
	 */
	String[] getAttributeNames() throws EternaException;

	/**
	 * 获取本数据库操作对象sql日志的记录方式
	 */
	int getLogType() throws EternaException;

	/**
	 * 获取生成本适配器的工厂.
	 */
	EternaFactory getFactory() throws EternaException;

	/**
	 * 获得参数的个数.
	 *
	 * @return    <code>SQLAdapter</code>中参数的个数.
	 * @throws EternaException     当相关配置出错时.
	 */
	int getParameterCount() throws EternaException;

	/**
	 * 获得实际有效的参数个数.
	 *
	 * @throws EternaException     当相关配置出错时.
	 */
	public int getActiveParamCount() throws EternaException;

	/**
	 * 判断是否有有效的参数.
	 *
	 * @throws EternaException     当相关配置出错时.
	 */
	public boolean hasActiveParam() throws EternaException;

	/**
	 * 根据Parameter的名称获取一个Parameter.
	 */
	Parameter getParameter(String paramName) throws EternaException;

	/**
	 * 根据Parameter的索引值获取一个Parameter.
	 */
	Parameter getParameter(int paramIndex) throws EternaException;

	/**
	 * 获取Parameter的迭代器.
	 * 迭代器中的所有Parameter按索引值从小到大顺序排列.
	 *
	 * @see Parameter#getIndex
	 */
	Iterator getParameterIterator() throws EternaException;

	/**
	 * 执行本数据库操作对象.
	 */
	void execute(Connection conn) throws EternaException, SQLException;

	/**
	 * 获得子语句的个数.
	 *
	 * @return    <code>SQLAdapter</code>中子语句的个数.
	 * @throws EternaException     当相关配置出错时.
	 */
	int getSubSQLCount() throws EternaException;

	/**
	 * 获得预备SQL语句. <p>
	 * 该预备SQL语句是经过第一步处理，将子语句设置完后的预备SQL语句。
	 *
	 * @return    经过第一步处理预备SQL语句.
	 * @throws EternaException     当相关配置出错时.
	 */
	String getPreparedSQL() throws EternaException;

	/**
	 * 设置子语句.
	 *
	 * @param index    子语句的索引值.
	 * @param subPart  要设置的子语句.
	 * @throws EternaException     当相关配置出错时.
	 */
	void setSubSQL(int index, String subPart) throws EternaException;

	/**
	 * 设置子语句, 并为其配上相应的参数.
	 *
	 * @param index    子语句的索引值.
	 * @param subPart  要设置的子语句.
	 * @param pm       要配上的参数.
	 * @throws EternaException     当相关配置出错时.
	 */
	void setSubSQL(int index, String subPart, PreparerManager pm) throws EternaException;

	/**
	 * 获得本数据库操作对象的参数准备器管理者.
	 */
	PreparerManager getPreparerManager() throws EternaException;

	/**
	 * 根据索引值判断对应的参数是否是动态参数.
	 *
	 * @param index    第一个参数是1, 第二个是2, ...
	 */
	boolean isDynamicParameter(int index) throws EternaException;

	/**
	 * 根据参数的名称判断对应的参数是否是动态参数.
	 *
	 * @param name    参数的名称
	 */
	boolean isDynamicParameter(String name) throws EternaException;

	/**
	 * 根据索引值判断对应的参数是否已设置.
	 *
	 * @param index    第一个参数是1, 第二个是2, ...
	 */
	boolean isParameterSetted(int index) throws EternaException;

	/**
	 * 根据参数的名称判断对应的参数是否已设置.
	 *
	 * @param name    参数的名称
	 */
	boolean isParameterSetted(String name) throws EternaException;

	/**
	 * 通过ValuePreparer来设置参数.
	 */
	void setValuePreparer(ValuePreparer preparer) throws EternaException;

	/**
	 * 将参数设置到PreparedStatement中.
	 */
	void prepareValues(PreparedStatement stmt) throws EternaException, SQLException;

	/**
	 * 将参数设置到PreparedStatementWrap中.
	 */
	void prepareValues(PreparedStatementWrap stmtWrap) throws EternaException, SQLException;

	/**
	 * 将定义好的参数设为忽略.
	 * 只有动态参数才可设置为忽略.
	 *
	 * @param parameterIndex 第一个参数是1, 第二个是2, ...
	 * @throws EternaException     当相关配置出错时.
	 */
	void setIgnore(int parameterIndex) throws EternaException;

	/**
	 * 将定义好的参数设为忽略.
	 * 只有动态参数才可设置为忽略.
	 *
	 * @param parameterName  这个参数的名称
	 * @throws EternaException     当相关配置出错时.
	 */
	void setIgnore(String parameterName) throws EternaException;

	/**
	 * 将一个String类型的值设置到参数中.
	 *
	 * @param parameterIndex  第一个参数是1, 第二个是2, ...
	 * @param x               参数的值
	 */
	void setString(int parameterIndex, String x) throws EternaException;

	/**
	 * 将一个String类型的值设置到参数中.
	 *
	 * @param parameterName  这个参数的名称
	 * @param x              参数的值
	 */
	void setString(String parameterName, String x) throws EternaException;

	/**
	 * 将一个Object类型的值设置到参数中.
	 *
	 * @param parameterIndex  第一个参数是1, 第二个是2, ...
	 * @param x               参数的值
	 */
	void setObject(int parameterIndex, Object x) throws EternaException;

	/**
	 * 将一个Object类型的值设置到参数中.
	 *
	 * @param parameterName  这个参数的名称
	 * @param x              参数的值
	 */
	void setObject(String parameterName, Object x) throws EternaException;

}