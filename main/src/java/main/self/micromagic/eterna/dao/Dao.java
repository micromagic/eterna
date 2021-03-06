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
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;

/**
 * 用于处理数据库操作的对象.
 */
public interface Dao
{
	/**
	 * 可设置参数是否通过名称来绑定. <p>
	 * 如果设为true表示通过名称绑定, 设为false(默认值)表示通过"?"的位置绑定.
	 * 在dao的attribute中设置, 仅对此dao有效.
	 * 在factory的attribute中设置, 将对此工厂中的所有未设置的dao有效.
	 */
	String PARAM_BIND_WITH_NAME_FLAG = "paramBindWithName";

	/**
	 * 配置中设置日志方式的标识.
	 */
	String LOG_TYPE_FLAG = "eterna.dao.logType";
	/**
	 * 日志等级, 不记录.
	 */
	int DAO_LOG_TYPE_NONE = -1;
	/**
	 * 日志等级, 记录到内存.
	 */
	int DAO_LOG_TYPE_SAVE = 0x2;
	/**
	 * 日志等级, 记录到系统日志.
	 */
	int DAO_LOG_TYPE_PRINT = 0x1;
	/**
	 * 判断是否有特殊日志类型的过滤符.
	 */
	int SPECIAL_MASK = ~(DAO_LOG_TYPE_SAVE | DAO_LOG_TYPE_PRINT);
	/**
	 * 系统日志记录时是否仅仅输出语句.
	 */
	String SIMPLE_PRINT_FLAG = "eterna.dao.log.simplePrint";

	String DAO_TYPE_UPDATE = "update";
	String DAO_TYPE_QUERY = "query";
	String DAO_TYPE_COUNT = "count";
	String DAO_TYPE_UNKNOW = "unknow";

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
	 * 获取本数据库操作对象日志的记录方式
	 */
	int getLogType() throws EternaException;

	/**
	 * 获取生成本适配器的工厂.
	 */
	EternaFactory getFactory() throws EternaException;

	/**
	 * 获得参数的个数.
	 *
	 * @return   数据操作对象中设置的参数个数
	 */
	int getParameterCount() throws EternaException;

	/**
	 * 获得真正的参数个数.
	 *
	 * @return  执行数据操作时真正需要的参数个数
	 */
	int getRealParameterCount() throws EternaException;

	/**
	 * 获得实际有效的参数个数.
	 */
	public int getActiveParamCount() throws EternaException;

	/**
	 * 判断是否有有效的参数.
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
	 * @return  此数据操作对象中子语句的个数
	 */
	int getSubScriptCount() throws EternaException;

	/**
	 * 获得预备数据操作脚本. <p>
	 * 该脚本是经过预处理，将子语句设置完后的数据操作脚本。
	 *
	 * @return  经过预处理的数据操作脚本
	 */
	String getPreparedScript() throws EternaException;

	/**
	 * 设置子语句.
	 *
	 * @param index    子语句的索引值
	 * @param subPart  要设置的子语句
	 */
	void setSubScript(int index, String subPart) throws EternaException;

	/**
	 * 设置子语句, 并为其配上相应的参数.
	 *
	 * @param index    子语句的索引值
	 * @param subPart  要设置的子语句
	 * @param pm       要配上的参数
	 */
	void setSubScript(int index, String subPart, PreparerManager pm) throws EternaException;

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
	 */
	void setIgnore(int parameterIndex) throws EternaException;

	/**
	 * 将定义好的参数设为忽略.
	 * 只有动态参数才可设置为忽略.
	 *
	 * @param parameterName  这个参数的名称
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
