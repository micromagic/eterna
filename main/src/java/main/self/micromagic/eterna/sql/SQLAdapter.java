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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.sql.preparer.PreparerManager;
import self.micromagic.eterna.sql.preparer.ValuePreparer;

/**
 * 用于处理预备SQL语句.
 *
 * @author  micromagic
 * @version 1.0, 2002-10-13
 */
public interface SQLAdapter
{
	/**
	 * 配置中设置SQL日志等级的标识.
	 */
	String SQL_LOG_PROPERTY = "self.micromagic.eterna.sql.logType";
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
	 * 获取本SQL适配器的名称.
	 */
	String getName() throws EternaException;

	/**
	 * 获取本SQL适配器的类型.
	 * 如: query, update.
	 */
	String getType() throws EternaException;

	/**
	 * 获取本SQL适配器某个设置的属性.
	 */
	Object getAttribute(String name) throws EternaException;

	/**
	 * 获取本SQL适配器设置的所有属性的名称.
	 */
	String[] getAttributeNames() throws EternaException;

	/**
	 * 获取本SQL适配器sql日志的记录方式
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
	 * 根据Parameter的名称获取一个SQLParameter.
	 */
	SQLParameter getParameter(String paramName) throws EternaException;

	/**
	 * 获取包含所有SQLParameter的迭代器.
	 * 迭代器中的所有SQLParameter必需按索引值从小到大顺序排列.
	 *
	 * @see SQLParameter#getIndex
	 */
	Iterator getParameterIterator() throws EternaException;

	/**
	 * 执行本SQL适配器.
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
	 * 获得本sql适配器的配置参数.
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

	/* *
	 * 用一个<code>Timestamp</code>来设置目标参数.
	 *
	 * @param parameterIndex   参数的索引值.
	 * @param x                参数的值.
	 * @param cal              用来构造Timestamp的<code>Calendar</code>.
	 * @throws EternaException     当相关配置出错时.
	 *
	public void setTimestamp(int parameterIndex, java.sql.Timestamp x, Calendar cal)
			throws EternaException;
	*/

	/* *
	 * 用一个<code>Timestamp</code>来设置目标参数.
	 *
	 * @param parameterName    参数的名称.
	 * @param x                参数的值.
	 * @param cal              用来构造Timestamp的<code>Calendar</code>.
	 * @throws EternaException     当相关配置出错时.
	 *
	public void setTimestamp(String parameterName, java.sql.Timestamp x, Calendar cal)
			throws EternaException;
	*/

	/* *
	 * 用一个<code>Object</code>来设置目标参数.
	 *
	 * @param parameterIndex   参数的索引值.
	 * @param x                参数的值.
	 * @throws EternaException     当相关配置出错时.
	 *
	public void setObject(int parameterIndex, Object x)
			throws EternaException;
	*/

	/*
	 * 用一个<code>Object</code>来设置目标参数.
	 *
	 * @param parameterName    参数的名称.
	 * @param x                参数的值.
	 * @throws EternaException     当相关配置出错时.
	 *
	public void setObject(String parameterName, Object x)
			throws EternaException;
	*/

	/* *
	 * 用一个<code>Object</code>来设置目标参数.
	 *
	 * @param parameterIndex   参数的索引值.
	 * @param x                参数的值.
	 * @param targetSqlType    SQL中的类型(在java.sql.Types中定义).
	 * @throws EternaException     当相关配置出错时.
	 *
	public void setObject(int parameterIndex, Object x, int targetSqlType)
			throws EternaException;
	*/

	/* *
	 * 用一个<code>Object</code>来设置目标参数.
	 *
	 * @param parameterName    参数的名称.
	 * @param x                参数的值.
	 * @param targetSqlType    SQL中的类型(在java.sql.Types中定义).
	 * @throws EternaException     当相关配置出错时.
	 *
	public void setObject(String parameterName, Object x, int targetSqlType)
			throws EternaException;
	*/

	/* *
	 * 用一个<code>Object</code>来设置目标参数.
	 *
	 * @param parameterIndex   参数的索引值.
	 * @param x                参数的值.
	 * @param targetSqlType    SQL中的类型(在java.sql.Types中定义).
	 * @param scale            设置DECIMAL和NUMERIC的小数位数.
	 * @throws EternaException     当相关配置出错时.
	 *
	public void setObject(int parameterIndex, Object x, int targetSqlType, int scale)
			throws EternaException;
	*/

	/* *
	 * 用一个<code>Object</code>来设置目标参数.
	 *
	 * @param parameterName    参数的名称.
	 * @param x                参数的值.
	 * @param targetSqlType    SQL中的类型(在java.sql.Types中定义).
	 * @param scale            设置DECIMAL和NUMERIC的小数位数.
	 * @throws EternaException     当相关配置出错时.
	 *
	public void setObject(String parameterName, Object x, int targetSqlType, int scale)
			throws EternaException;
	*/

	/**
	 * Sets the designated parameter to SQL <code>NULL</code>.
	 * 将定义好的参数设为SQL的<code>NULL</code>.
	 *
	 * <P><B>Note:</B> You must specify the parameter's SQL type.
	 * <P><B>注:</B> 你必须指定参数的SQL类型.
	 *
	 * @param parameterIndex the first parameter is 1, the second is 2, ...
	 *                       <p>第一个参数是1, 第二个是2, ...
	 * @param sqlType the SQL type code defined in <code>java.sql.Types</code>
	 *                <p>在<code>java.sql.Types</code>中定义的SQL型
	 * @throws EternaException     当相关配置出错时.
	 */
	void setNull(int parameterIndex, int sqlType) throws EternaException;

	/**
	 * Sets the designated parameter to SQL <code>NULL</code>.
	 * 将定义好的参数设为SQL的<code>NULL</code>.
	 *
	 * <P><B>Note:</B> You must specify the parameter's SQL type.
	 * <P><B>注:</B> 你必须指定参数的SQL类型.
	 *
	 * @param parameterName  the parameter name
	 *                       <p>这个参数的名称
	 * @param sqlType the SQL type code defined in <code>java.sql.Types</code>
	 *                <p>在<code>java.sql.Types</code>中定义的SQL型
	 * @throws EternaException     当相关配置出错时.
	 */
	void setNull(String parameterName, int sqlType) throws EternaException;

	/**
	 * Sets the designated parameter to the given Java <code>boolean</code> value.
	 * 将定义好的参数设为Java的<code>boolean</code>值.
	 * The driver converts this
	 * to an SQL <code>BIT</code> value when it sends it to the database.
	 * SQL驱动要在将这个值发送到数据库时转为SQL的<code>BIT</code>类型.
	 *
	 * @param parameterIndex the first parameter is 1, the second is 2, ...
	 *                       <p>第一个参数是1, 第二个是2, ...
	 * @param x the parameter value
	 *          <p> 这个参数的值
	 * @throws EternaException     当相关配置出错时.
	 */
	void setBoolean(int parameterIndex, boolean x) throws EternaException;

	/**
	 * Sets the designated parameter to the given Java <code>boolean</code> value.
	 * 将定义好的参数设为Java的<code>boolean</code>值.
	 * to an SQL <code>BIT</code> value when it sends it to the database.
	 * SQL驱动要在将这个值发送到数据库时转为SQL的<code>BIT</code>类型.
	 *
	 * @param parameterName  the parameter name
	 *                       <p>这个参数的名称
	 * @param x the parameter value
	 *          <p> 这个参数的值
	 * @throws EternaException     当相关配置出错时.
	 */
	void setBoolean(String parameterName, boolean x) throws EternaException;

	/**
	 * Sets the designated parameter to the given Java <code>byte</code> value.
	 * 将定义好的参数设为Java的<code>byte</code>值.
	 * The driver converts this
	 * to an SQL <code>TINYINT</code> value when it sends it to the database.
	 * SQL驱动要在将这个值发送到数据库时转为SQL的<code>TINYINT</code>类型.
	 *
	 * @param parameterIndex the first parameter is 1, the second is 2, ...
	 *                       <p>第一个参数是1, 第二个是2, ...
	 * @param x the parameter value
	 *          <p> 这个参数的值
	 * @throws EternaException     当相关配置出错时.
	 */
	void setByte(int parameterIndex, byte x) throws EternaException;

	/**
	 * Sets the designated parameter to the given Java <code>byte</code> value.
	 * 将定义好的参数设为Java的<code>byte</code>值.
	 * The driver converts this
	 * to an SQL <code>TINYINT</code> value when it sends it to the database.
	 * SQL驱动要在将这个值发送到数据库时转为SQL的<code>TINYINT</code>类型.
	 *
	 * @param parameterName  the parameter name
	 *                       <p>这个参数的名称
	 * @param x the parameter value
	 *          <p> 这个参数的值
	 * @throws EternaException     当相关配置出错时.
	 */
	void setByte(String parameterName, byte x) throws EternaException;

	/**
	 * Sets the designated parameter to the given Java <code>short</code> value.
	 * 将定义好的参数设为Java的<code>short</code>值.
	 * The driver converts this
	 * to an SQL <code>SMALLINT</code> value when it sends it to the database.
	 * SQL驱动要在将这个值发送到数据库时转为SQL的<code>SMALLINT</code>类型.
	 *
	 * @param parameterIndex the first parameter is 1, the second is 2, ...
	 *                       <p>第一个参数是1, 第二个是2, ...
	 * @param x the parameter value
	 *          <p> 这个参数的值
	 * @throws EternaException     当相关配置出错时.
	 */
	void setShort(int parameterIndex, short x) throws EternaException;

	/**
	 * Sets the designated parameter to the given Java <code>short</code> value.
	 * 将定义好的参数设为Java的<code>short</code>值.
	 * The driver converts this
	 * to an SQL <code>SMALLINT</code> value when it sends it to the database.
	 * SQL驱动要在将这个值发送到数据库时转为SQL的<code>SMALLINT</code>类型.
	 *
	 * @param parameterName  the parameter name
	 *                       <p>这个参数的名称
	 * @param x the parameter value
	 *          <p> 这个参数的值
	 * @throws EternaException     当相关配置出错时.
	 */
	void setShort(String parameterName, short x) throws EternaException;

	/**
	 * Sets the designated parameter to the given Java <code>int</code> value.
	 * 将定义好的参数设为Java的<code>int</code>值.
	 * The driver converts this
	 * to an SQL <code>INTEGER</code> value when it sends it to the database.
	 * SQL驱动要在将这个值发送到数据库时转为SQL的<code>INTEGER</code>类型.
	 *
	 * @param parameterIndex the first parameter is 1, the second is 2, ...
	 *                       <p>第一个参数是1, 第二个是2, ...
	 * @param x the parameter value
	 *          <p> 这个参数的值
	 * @throws EternaException     当相关配置出错时.
	 */
	void setInt(int parameterIndex, int x) throws EternaException;

	/**
	 * Sets the designated parameter to the given Java <code>int</code> value.
	 * 将定义好的参数设为Java的<code>int</code>值.
	 * The driver converts this
	 * to an SQL <code>INTEGER</code> value when it sends it to the database.
	 * SQL驱动要在将这个值发送到数据库时转为SQL的<code>INTEGER</code>类型.
	 *
	 * @param parameterName  the parameter name
	 *                       <p>这个参数的名称
	 * @param x the parameter value
	 *          <p> 这个参数的值
	 * @throws EternaException     当相关配置出错时.
	 */
	void setInt(String parameterName, int x) throws EternaException;

	/**
	 * Sets the designated parameter to the given Java <code>long</code> value.
	 * 将定义好的参数设为Java的<code>long</code>值.
	 * The driver converts this
	 * to an SQL <code>BIGINT</code> value when it sends it to the database.
	 * SQL驱动要在将这个值发送到数据库时转为SQL的<code>BIGINT</code>类型.
	 *
	 * @param parameterIndex the first parameter is 1, the second is 2, ...
	 *                       <p>第一个参数是1, 第二个是2, ...
	 * @param x the parameter value
	 *          <p> 这个参数的值
	 * @throws EternaException     当相关配置出错时.
	 */
	void setLong(int parameterIndex, long x) throws EternaException;

	/**
	 * Sets the designated parameter to the given Java <code>long</code> value.
	 * 将定义好的参数设为Java的<code>long</code>值.
	 * The driver converts this
	 * to an SQL <code>BIGINT</code> value when it sends it to the database.
	 * SQL驱动要在将这个值发送到数据库时转为SQL的<code>BIGINT</code>类型.
	 *
	 * @param parameterName  the parameter name
	 *                       <p>这个参数的名称
	 * @param x the parameter value
	 *          <p> 这个参数的值
	 * @throws EternaException     当相关配置出错时.
	 */
	void setLong(String parameterName, long x) throws EternaException;

	/**
	 * Sets the designated parameter to the given Java <code>float</code> value.
	 * 将定义好的参数设为Java的<code>float</code>值.
	 * The driver converts this
	 * to an SQL <code>FLOAT</code> value when it sends it to the database.
	 * SQL驱动要在将这个值发送到数据库时转为SQL的<code>FLOAT</code>类型.
	 *
	 * @param parameterIndex the first parameter is 1, the second is 2, ...
	 *                       <p>第一个参数是1, 第二个是2, ...
	 * @param x the parameter value
	 *          <p> 这个参数的值
	 * @throws EternaException     当相关配置出错时.
	 */
	void setFloat(int parameterIndex, float x) throws EternaException;

	/**
	 * Sets the designated parameter to the given Java <code>float</code> value.
	 * 将定义好的参数设为Java的<code>float</code>值.
	 * The driver converts this
	 * to an SQL <code>FLOAT</code> value when it sends it to the database.
	 * SQL驱动要在将这个值发送到数据库时转为SQL的<code>FLOAT</code>类型.
	 *
	 * @param parameterName  the parameter name
	 *                       <p>这个参数的名称
	 * @param x the parameter value
	 *          <p> 这个参数的值
	 * @throws EternaException     当相关配置出错时.
	 */
	void setFloat(String parameterName, float x) throws EternaException;

	/**
	 * Sets the designated parameter to the given Java <code>double</code> value.
	 * 将定义好的参数设为Java的<code>double</code>值.
	 * The driver converts this
	 * to an SQL <code>DOUBLE</code> value when it sends it to the database.
	 * SQL驱动要在将这个值发送到数据库时转为SQL的<code>DOUBLE</code>类型.
	 *
	 * @param parameterIndex the first parameter is 1, the second is 2, ...
	 *                       <p>第一个参数是1, 第二个是2, ...
	 * @param x the parameter value
	 *          <p> 这个参数的值
	 * @throws EternaException     当相关配置出错时.
	 */
	void setDouble(int parameterIndex, double x) throws EternaException;

	/**
	 * Sets the designated parameter to the given Java <code>double</code> value.
	 * 将定义好的参数设为Java的<code>double</code>值.
	 * The driver converts this
	 * to an SQL <code>DOUBLE</code> value when it sends it to the database.
	 * SQL驱动要在将这个值发送到数据库时转为SQL的<code>DOUBLE</code>类型.
	 *
	 * @param parameterName  the parameter name
	 *                       <p>这个参数的名称
	 * @param x the parameter value
	 *          <p> 这个参数的值
	 * @throws EternaException     当相关配置出错时.
	 */
	void setDouble(String parameterName, double x) throws EternaException;

	/* *
	 * Sets the designated parameter to the given <code>java.math.BigDecimal</code> value.
	 * 将定义好的参数设为Java的<code>java.math.BigDecimal</code>值.
	 * The driver converts this to an SQL <code>NUMERIC</code> value when
	 * it sends it to the database.
	 * SQL驱动要在将这个值发送到数据库时转为SQL的<code>NUMERIC</code>类型.
	 *
	 * @param parameterIndex the first parameter is 1, the second is 2, ...
	 *                       <p>第一个参数是1, 第二个是2, ...
	 * @param x the parameter value
	 *          <p> 这个参数的值
	 * @throws EternaException     当相关配置出错时.
	 * 未支持
	 *
	void setBigDecimal(int parameterIndex, BigDecimal x) throws EternaException;
	*/

	/* *
	 * Sets the designated parameter to the given <code>java.math.BigDecimal</code> value.
	 * 将定义好的参数设为Java的<code>java.math.BigDecimal</code>值.
	 * The driver converts this to an SQL <code>NUMERIC</code> value when
	 * it sends it to the database.
	 * SQL驱动要在将这个值发送到数据库时转为SQL的<code>NUMERIC</code>类型.
	 *
	 * @param parameterName  the parameter name
	 *                       <p>这个参数的名称
	 * @param x the parameter value
	 *          <p> 这个参数的值
	 * @throws EternaException     当相关配置出错时.
	 * 未支持
	 *
	void setBigDecimal(String parameterName, BigDecimal x) throws EternaException;
	*/

	/**
	 * Sets the designated parameter to the given Java <code>String</code> value.
	 * 将定义好的参数设为Java的<code>String</code>值.
	 * The driver converts this
	 * to an SQL <code>VARCHAR</code> or <code>LONGVARCHAR</code> value
	 * (depending on the argument's
	 * size relative to the driver's limits on <code>VARCHAR</code> values)
	 * when it sends it to the database.
	 * SQL驱动要在将这个值发送到数据库时转为SQL的<code>VARCHAR</code>或
	 * <code>LONGVARCHAR</code>类型.
	 *
	 * @param parameterIndex the first parameter is 1, the second is 2, ...
	 *                       <p>第一个参数是1, 第二个是2, ...
	 * @param x the parameter value
	 *          <p> 这个参数的值
	 * @throws EternaException     当相关配置出错时.
	 */
	void setString(int parameterIndex, String x) throws EternaException;

	/**
	 * Sets the designated parameter to the given Java <code>String</code> value.
	 * 将定义好的参数设为Java的<code>String</code>值.
	 * The driver converts this
	 * to an SQL <code>VARCHAR</code> or <code>LONGVARCHAR</code> value
	 * (depending on the argument's
	 * size relative to the driver's limits on <code>VARCHAR</code> values)
	 * when it sends it to the database.
	 * SQL驱动要在将这个值发送到数据库时转为SQL的<code>VARCHAR</code>或
	 * <code>LONGVARCHAR</code>类型.
	 *
	 * @param parameterName  the parameter name
	 *                       <p>这个参数的名称
	 * @param x the parameter value
	 *          <p> 这个参数的值
	 * @throws EternaException     当相关配置出错时.
	 */
	void setString(String parameterName, String x) throws EternaException;

	/**
	 * Sets the designated parameter to the given Java array of bytes.
	 * 将定义好的参数设为Java的<code>byte</code>数组.
	 * The driver converts
	 * this to an SQL <code>VARBINARY</code> or <code>LONGVARBINARY</code>
	 * (depending on the argument's size relative to the driver's limits on
	 * <code>VARBINARY</code> values) when it sends it to the database.
	 * SQL驱动要在将这个值发送到数据库时转为SQL的<code>VARBINARY</code>或
	 * <code>LONGVARBINARY</code>类型.
	 *
	 * @param parameterIndex the first parameter is 1, the second is 2, ...
	 *                       <p>第一个参数是1, 第二个是2, ...
	 * @param x the parameter value
	 *          <p> 这个参数的值
	 * @throws EternaException     当相关配置出错时.
	 */
	void setBytes(int parameterIndex, byte[] x) throws EternaException;

	/**
	 * Sets the designated parameter to the given Java array of bytes.
	 * 将定义好的参数设为Java的<code>byte</code>数组.
	 * The driver converts
	 * this to an SQL <code>VARBINARY</code> or <code>LONGVARBINARY</code>
	 * (depending on the argument's size relative to the driver's limits on
	 * <code>VARBINARY</code> values) when it sends it to the database.
	 * SQL驱动要在将这个值发送到数据库时转为SQL的<code>VARBINARY</code>或
	 * <code>LONGVARBINARY</code>类型.
	 *
	 * @param parameterName  the parameter name
	 *                       <p>这个参数的名称
	 * @param x the parameter value
	 *          <p> 这个参数的值
	 * @throws EternaException     当相关配置出错时.
	 */
	void setBytes(String parameterName, byte[] x) throws EternaException;

	/**
	 * Sets the designated parameter to the given <code>java.sql.Date</code> value.
	 * 将定义好的参数设为Java的<code>java.sql.Date</code>数组.
	 * The driver converts this
	 * to an SQL <code>DATE</code> value when it sends it to the database.
	 * SQL驱动要在将这个值发送到数据库时转为SQL的<code>DATE</code>类型.
	 *
	 * @param parameterIndex the first parameter is 1, the second is 2, ...
	 *                       <p>第一个参数是1, 第二个是2, ...
	 * @param x the parameter value
	 *          <p> 这个参数的值
	 * @throws EternaException     当相关配置出错时.
	 */
	void setDate(int parameterIndex, java.sql.Date x) throws EternaException;

	/**
	 * Sets the designated parameter to the given <code>java.sql.Date</code> value.
	 * 将定义好的参数设为Java的<code>java.sql.Date</code>数组.
	 * The driver converts this
	 * to an SQL <code>DATE</code> value when it sends it to the database.
	 * SQL驱动要在将这个值发送到数据库时转为SQL的<code>DATE</code>类型.
	 *
	 * @param parameterName  the parameter name
	 *                       <p>这个参数的名称
	 * @param x the parameter value
	 *          <p> 这个参数的值
	 * @throws EternaException     当相关配置出错时.
	 */
	void setDate(String parameterName, java.sql.Date x) throws EternaException;

	/**
	 * Sets the designated parameter to the given <code>java.sql.Time</code> value.
	 * 将定义好的参数设为Java的<code>java.sql.Time</code>数组.
	 * The driver converts this
	 * to an SQL <code>TIME</code> value when it sends it to the database.
	 * SQL驱动要在将这个值发送到数据库时转为SQL的<code>TIME</code>类型.
	 *
	 * @param parameterIndex the first parameter is 1, the second is 2, ...
	 *                       <p>第一个参数是1, 第二个是2, ...
	 * @param x the parameter value
	 *          <p> 这个参数的值
	 * @throws EternaException     当相关配置出错时.
	 */
	void setTime(int parameterIndex, java.sql.Time x) throws EternaException;

	/**
	 * Sets the designated parameter to the given <code>java.sql.Time</code> value.
	 * 将定义好的参数设为Java的<code>java.sql.Time</code>数组.
	 * The driver converts this
	 * to an SQL <code>TIME</code> value when it sends it to the database.
	 * SQL驱动要在将这个值发送到数据库时转为SQL的<code>TIME</code>类型.
	 *
	 * @param parameterName  the parameter name
	 *                       <p>这个参数的名称
	 * @param x the parameter value
	 *          <p> 这个参数的值
	 * @throws EternaException     当相关配置出错时.
	 */
	void setTime(String parameterName, java.sql.Time x) throws EternaException;

	/**
	 * Sets the designated parameter to the given <code>java.sql.Timestamp</code> value.
	 * 将定义好的参数设为Java的<code>java.sql.Timestamp</code>数组.
	 * The driver converts this
	 * to an SQL <code>TIMESTAMP</code> value when it sends it to the database.
	 * SQL驱动要在将这个值发送到数据库时转为SQL的<code>TIMESTAMP</code>类型.
	 *
	 * @param parameterIndex the first parameter is 1, the second is 2, ...
	 *                       <p>第一个参数是1, 第二个是2, ...
	 * @param x the parameter value
	 *          <p> 这个参数的值
	 * @throws EternaException     当相关配置出错时.
	 */
	void setTimestamp(int parameterIndex, java.sql.Timestamp x)
			throws EternaException;

	/**
	 * Sets the designated parameter to the given <code>java.sql.Timestamp</code> value.
	 * 将定义好的参数设为Java的<code>java.sql.Timestamp</code>数组.
	 * The driver converts this
	 * to an SQL <code>TIMESTAMP</code> value when it sends it to the database.
	 * SQL驱动要在将这个值发送到数据库时转为SQL的<code>TIMESTAMP</code>类型.
	 *
	 * @param parameterName  the parameter name
	 *                       <p>这个参数的名称
	 * @param x the parameter value
	 *          <p> 这个参数的值
	 * @throws EternaException     当相关配置出错时.
	 */
	void setTimestamp(String parameterName, java.sql.Timestamp x)
			throws EternaException;

	/*  *
	 * Sets the designated parameter to the given input stream, which will have
	 * the specified number of bytes.
	 * When a very large ASCII value is input to a <code>LONGVARCHAR</code>
	 * parameter, it may be more practical to send it via a
	 * <code>java.io.InputStream</code>. Data will be read from the stream
	 * as needed until end-of-file is reached.  The JDBC driver will
	 * do any necessary conversion from ASCII to the database char format.
	 *
	 * <P><B>Note:</B> This stream object can either be a standard
	 * Java stream object or your own subclass that implements the
	 * standard interface.
	 *
	 * @param parameterIndex the first parameter is 1, the second is 2, ...
	 * @param x the Java input stream that contains the ASCII parameter value
	 * @param length the number of bytes in the stream
	 * @exception java.sql.SQLException if a database access error occurs
	 * 未支持
	void setAsciiStream(int parameterIndex, java.io.InputStream x, int length)
			throws SQLException;
	*/

	/* *
	 * Sets the designated parameter to the given input stream, which
	 * will have the specified number of bytes. A Unicode character has
	 * two bytes, with the first byte being the high byte, and the second
	 * being the low byte.
	 *
	 * When a very large Unicode value is input to a <code>LONGVARCHAR</code>
	 * parameter, it may be more practical to send it via a
	 * <code>java.io.InputStream</code> object. The data will be read from the
	 * stream as needed until end-of-file is reached.  The JDBC driver will
	 * do any necessary conversion from Unicode to the database char format.
	 *
	 * <P><B>Note:</B> This stream object can either be a standard
	 * Java stream object or your own subclass that implements the
	 * standard interface.
	 *
	 * @param parameterIndex the first parameter is 1, the second is 2, ...
	 * @param x a <code>java.io.InputStream</code> object that contains the
	 *        Unicode parameter value as two-byte Unicode characters
	 * @param length the number of bytes in the stream
	 * @exception java.sql.SQLException if a database access error occurs
	 * @deprecated
	 * 未支持
	 *
	void setUnicodeStream(int parameterIndex, java.io.InputStream x,
			int length) throws SQLException;
	*/

	/**
	 * Sets the designated parameter to the given input stream, which will have
	 * the specified number of bytes.
	 * When a very large binary value is input to a <code>LONGVARBINARY</code>
	 * parameter, it may be more practical to send it via a
	 * <code>java.io.InputStream</code> object. The data will be read from the
	 * stream as needed until end-of-file is reached.
	 *
	 * <P><B>Note:</B> This stream object can either be a standard
	 * Java stream object or your own subclass that implements the
	 * standard interface.
	 *
	 * @param parameterIndex the first parameter is 1, the second is 2, ...
	 * @param x the java input stream which contains the binary parameter value
	 * @param length the number of bytes in the stream
	 * @throws EternaException     当相关配置出错时.
	 */
	void setBinaryStream(int parameterIndex, java.io.InputStream x, int length)
			throws EternaException;

	/**
	 * Sets the designated parameter to the given input stream, which will have
	 * the specified number of bytes.
	 * When a very large binary value is input to a <code>LONGVARBINARY</code>
	 * parameter, it may be more practical to send it via a
	 * <code>java.io.InputStream</code> object. The data will be read from the
	 * stream as needed until end-of-file is reached.
	 *
	 * <P><B>Note:</B> This stream object can either be a standard
	 * Java stream object or your own subclass that implements the
	 * standard interface.
	 *
	 * @param parameterName  这个参数的名称
	 * @param x the java input stream which contains the binary parameter value
	 * @param length the number of bytes in the stream
	 * @throws EternaException     当相关配置出错时.
	 */
	void setBinaryStream(String parameterName, java.io.InputStream x, int length)
			throws EternaException;

	/* *
	 * Clears the current parameter values immediately.
	 * <P>In general, parameter values remain in force for repeated use of a
	 * statement. Setting a parameter value automatically clears its
	 * previous value.  However, in some cases it is useful to immediately
	 * release the resources used by the current parameter values; this can
	 * be done by calling the method <code>clearParameters</code>.
	 *
	 * @exception java.sql.SQLException if a database access error occurs
	 * 未支持
	 *
	void clearParameters() throws SQLException;
	*/

	//----------------------------------------------------------------------
	// Advanced features:

	/* *
	 * <p>Sets the value of the designated parameter with the given object. The second
	 * argument must be an object type; for integral values, the
	 * <code>java.lang</code> equivalent objects should be used.
	 *
	 * <p>The given Java object will be converted to the given targetSqlType
	 * before being sent to the database.
	 *
	 * If the object has a custom mapping (is of a class implementing the
	 * interface <code>SQLData</code>),
	 * the JDBC driver should call the method <code>SQLData.writeSQL</code> to
	 * write it to the SQL data stream.
	 * If, on the other hand, the object is of a class implementing
	 * <code>Ref</code>, <code>Blob</code>, <code>Clob</code>, <code>Struct</code>,
	 * or <code>Array</code>, the driver should pass it to the database as a
	 * value of the corresponding SQL type.
	 *
	 * <p>Note that this method may be used to pass database-specific
	 * abstract data types.
	 *
	 * @param parameterIndex the first parameter is 1, the second is 2, ...
	 * @param x the object containing the input parameter value
	 * @param targetSqlType the SQL type (as defined in java.sql.Types) to be
	 * sent to the database. The scale argument may further qualify this type.
	 * @param scale for java.sql.Types.DECIMAL or java.sql.Types.NUMERIC types,
	 *          this is the number of digits after the decimal point.  For all other
	 *          types, this value will be ignored.
	 * @see java.sql.Types
	 * 未支持
	 *
	void setObject(int parameterIndex, Object x, int targetSqlType, int scale)
			throws SQLException;
	*/

	/* *
	 * Sets the value of the designated parameter with the given object.
	 * This method is like the method <code>setObject</code>
	 * above, except that it assumes a scale of zero.
	 *
	 * @param parameterIndex the first parameter is 1, the second is 2, ...
	 * @param x the object containing the input parameter value
	 * @param targetSqlType the SQL type (as defined in java.sql.Types) to be
	 *                      sent to the database
	 * @exception java.sql.SQLException if a database access error occurs
	 * 未支持
	 *
	void setObject(int parameterIndex, Object x, int targetSqlType)
			throws SQLException;
	*/

	/**
	 * <p>Sets the value of the designated parameter using the given object.
	 * The second parameter must be of type <code>Object</code>; therefore, the
	 * <code>java.lang</code> equivalent objects should be used for built-in types.
	 *
	 * <p>The JDBC specification specifies a standard mapping from
	 * Java <code>Object</code> types to SQL types.  The given argument
	 * will be converted to the corresponding SQL type before being
	 * sent to the database.
	 *
	 * <p>Note that this method may be used to pass datatabase-
	 * specific abstract data types, by using a driver-specific Java
	 * type.
	 *
	 * If the object is of a class implementing the interface <code>SQLData</code>,
	 * the JDBC driver should call the method <code>SQLData.writeSQL</code>
	 * to write it to the SQL data stream.
	 * If, on the other hand, the object is of a class implementing
	 * <code>Ref</code>, <code>Blob</code>, <code>Clob</code>, <code>Struct</code>,
	 * or <code>Array</code>, the driver should pass it to the database as a
	 * value of the corresponding SQL type.
	 * <P>
	 * This method throws an exception if there is an ambiguity, for example, if the
	 * object is of a class implementing more than one of the interfaces named above.
	 *
	 * @param parameterIndex the first parameter is 1, the second is 2, ..
	 *                       <p>第一个参数是1, 第二个是2, ....
	 * @param x the object containing the input parameter value
	 *          <p> 含有这个参数值的对象
	 * @throws EternaException     当相关配置出错时.
	 */
	void setObject(int parameterIndex, Object x) throws EternaException;

	/**
	 * <p>Sets the value of the designated parameter using the given object.
	 * The second parameter must be of type <code>Object</code>; therefore, the
	 * <code>java.lang</code> equivalent objects should be used for built-in types.
	 *
	 * <p>The JDBC specification specifies a standard mapping from
	 * Java <code>Object</code> types to SQL types.  The given argument
	 * will be converted to the corresponding SQL type before being
	 * sent to the database.
	 *
	 * <p>Note that this method may be used to pass datatabase-
	 * specific abstract data types, by using a driver-specific Java
	 * type.
	 *
	 * If the object is of a class implementing the interface <code>SQLData</code>,
	 * the JDBC driver should call the method <code>SQLData.writeSQL</code>
	 * to write it to the SQL data stream.
	 * If, on the other hand, the object is of a class implementing
	 * <code>Ref</code>, <code>Blob</code>, <code>Clob</code>, <code>Struct</code>,
	 * or <code>Array</code>, the driver should pass it to the database as a
	 * value of the corresponding SQL type.
	 * <P>
	 * This method throws an exception if there is an ambiguity, for example, if the
	 * object is of a class implementing more than one of the interfaces named above.
	 *
	 * @param parameterName  the parameter name
	 *                       <p>这个参数的名称
	 * @param x the parameter value
	 *          <p> 这个参数的值
	 * @throws EternaException     当相关配置出错时.
	 */
	void setObject(String parameterName, Object x) throws EternaException;

	//--------------------------JDBC 2.0-----------------------------

	/* *
	 * Adds a set of parameters to this <code>PreparedStatement</code>
	 * object's batch of commands.
	 *
	 * @exception SQLException if a database access error occurs
	 * @see java.sql.Statement#addBatch
	 * @since 1.2
	 * 未支持
	 *
	void addBatch() throws SQLException;
	*/

	/**
	 * Sets the designated parameter to the given <code>Reader</code>
	 * object, which is the given number of characters long.
	 * When a very large UNICODE value is input to a <code>LONGVARCHAR</code>
	 * parameter, it may be more practical to send it via a
	 * <code>java.io.Reader</code> object. The data will be read from the stream
	 * as needed until end-of-file is reached.  The JDBC driver will
	 * do any necessary conversion from UNICODE to the database char format.
	 *
	 * <P><B>Note:</B> This stream object can either be a standard
	 * Java stream object or your own subclass that implements the
	 * standard interface.
	 *
	 * @param parameterIndex the first parameter is 1, the second is 2, ...
	 * @param reader the <code>java.io.Reader</code> object that contains the
	 *        Unicode data
	 * @param length the number of characters in the stream
	 * @throws EternaException     当相关配置出错时.
	 * @since 1.2
	 */
	void setCharacterStream(int parameterIndex, java.io.Reader reader, int length)
			throws EternaException;

	/**
	 * Sets the designated parameter to the given <code>Reader</code>
	 * object, which is the given number of characters long.
	 * When a very large UNICODE value is input to a <code>LONGVARCHAR</code>
	 * parameter, it may be more practical to send it via a
	 * <code>java.io.Reader</code> object. The data will be read from the stream
	 * as needed until end-of-file is reached.  The JDBC driver will
	 * do any necessary conversion from UNICODE to the database char format.
	 *
	 * <P><B>Note:</B> This stream object can either be a standard
	 * Java stream object or your own subclass that implements the
	 * standard interface.
	 *
	 * @param parameterName  这个参数的名称
	 * @param reader the <code>java.io.Reader</code> object that contains the
	 *        Unicode data
	 * @param length the number of characters in the stream
	 * @throws EternaException     当相关配置出错时.
	 * @since 1.2
	 */
	void setCharacterStream(String parameterName, java.io.Reader reader, int length)
			throws EternaException;

	/* *
	 * Sets the designated parameter to the given
	 *  <code>REF(&lt;structured-type&gt;)</code> value.
	 * The driver converts this to an SQL <code>REF</code> value when it
	 * sends it to the database.
	 *
	 * @param i the first parameter is 1, the second is 2, ...
	 * @param x an SQL <code>REF</code> value
	 * @exception java.sql.SQLException if a database access error occurs
	 * @since 1.2
	 * 未支持
	 *
	void setRef(int i, Ref x) throws SQLException;
	*/

	/* *
	 * Sets the designated parameter to the given <code>Blob</code> object.
	 * The driver converts this to an SQL <code>BLOB</code> value when it
	 * sends it to the database.
	 *
	 * @param i the first parameter is 1, the second is 2, ...
	 * @param x a <code>Blob</code> object that maps an SQL <code>BLOB</code> value
	 * @exception java.sql.SQLException if a database access error occurs
	 * @since 1.2
	 * 未支持
	 *
	void setBlob(int i, Blob x) throws SQLException;
	*/

	/* *
	 * Sets the designated parameter to the given <code>Clob</code> object.
	 * The driver converts this to an SQL <code>CLOB</code> value when it
	 * sends it to the database.
	 *
	 * @param i the first parameter is 1, the second is 2, ...
	 * @param x a <code>Clob</code> object that maps an SQL <code>CLOB</code> value
	 * @exception java.sql.SQLException if a database access error occurs
	 * @since 1.2
	 * 未支持
	 *
	void setClob(int i, Clob x) throws SQLException;
	*/

	/* *
	 * Sets the designated parameter to the given <code>Array</code> object.
	 * The driver converts this to an SQL <code>ARRAY</code> value when it
	 * sends it to the database.
	 *
	 * @param i the first parameter is 1, the second is 2, ...
	 * @param x an <code>Array</code> object that maps an SQL <code>ARRAY</code> value
	 * @exception java.sql.SQLException if a database access error occurs
	 * @since 1.2
	 * 未支持
	 *
	void setArray(int i, Array x) throws SQLException;
	*/

	/* *
	 * Retrieves a <code>ResultSetMetaData</code> object that contains
	 * information about the columns of the <code>ResultSet</code> object
	 * that will be returned when this <code>PreparedStatement</code> object
	 * is executed.
	 * <P>
	 * Because a <code>PreparedStatement</code> object is precompiled, it is
	 * possible to know about the <code>ResultSet</code> object that it will
	 * return without having to execute it.  Consequently, it is possible
	 * to invoke the method <code>getMetaData</code> on a
	 * <code>PreparedStatement</code> object rather than waiting to execute
	 * it and then invoking the <code>ResultSet.getMetaData</code> method
	 * on the <code>ResultSet</code> object that is returned.
	 * <P>
	 * <B>NOTE:</B> Using this method may be expensive for some drivers due
	 * to the lack of underlying DBMS support.
	 *
	 * @return the description of a <code>ResultSet</code> object's columns or
	 *         <code>null</code> if the driver cannot return a
	 *         <code>ResultSetMetaData</code> object
	 * @exception java.sql.SQLException if a database access error occurs
	 * @since 1.2
	 * 不支持
	 *
	ResultSetMetaData getMetaData() throws SQLException;
	*/

	/* *
	 * Sets the designated parameter to the given <code>java.sql.Date</code> value,
	 * using the given <code>Calendar</code> object.  The driver uses
	 * the <code>Calendar</code> object to construct an SQL <code>DATE</code> value,
	 * which the driver then sends to the database.  With
	 * a <code>Calendar</code> object, the driver can calculate the date
	 * taking into account a custom timezone.  If no
	 * <code>Calendar</code> object is specified, the driver uses the default
	 * timezone, which is that of the virtual machine running the application.
	 *
	 * @param parameterIndex the first parameter is 1, the second is 2, ...
	 * @param x the parameter value
	 * @param cal the <code>Calendar</code> object the driver will use
	 *            to construct the date
	 * @exception java.sql.SQLException if a database access error occurs
	 * @since 1.2
	 * 未支持
	 *
	void setDate(int parameterIndex, java.sql.Date x, Calendar cal)
			throws SQLException;
	*/

	/* *
	 * Sets the designated parameter to the given <code>java.sql.Time</code> value,
	 * using the given <code>Calendar</code> object.  The driver uses
	 * the <code>Calendar</code> object to construct an SQL <code>TIME</code> value,
	 * which the driver then sends to the database.  With
	 * a <code>Calendar</code> object, the driver can calculate the time
	 * taking into account a custom timezone.  If no
	 * <code>Calendar</code> object is specified, the driver uses the default
	 * timezone, which is that of the virtual machine running the application.
	 *
	 * @param parameterIndex the first parameter is 1, the second is 2, ...
	 * @param x the parameter value
	 * @param cal the <code>Calendar</code> object the driver will use
	 *            to construct the time
	 * @exception java.sql.SQLException if a database access error occurs
	 * @since 1.2
	 * 未支持
	 *
	void setTime(int parameterIndex, java.sql.Time x, Calendar cal)
			throws SQLException;
	*/

	/* *
	 * Sets the designated parameter to the given <code>java.sql.Timestamp</code> value,
	 * using the given <code>Calendar</code> object.  The driver uses
	 * the <code>Calendar</code> object to construct an SQL <code>TIMESTAMP</code> value,
	 * which the driver then sends to the database.  With a
	 *  <code>Calendar</code> object, the driver can calculate the timestamp
	 * taking into account a custom timezone.  If no
	 * <code>Calendar</code> object is specified, the driver uses the default
	 * timezone, which is that of the virtual machine running the application.
	 *
	 * @param parameterIndex the first parameter is 1, the second is 2, ...
	 * @param x the parameter value
	 * @param cal the <code>Calendar</code> object the driver will use
	 *            to construct the timestamp
	 * @exception java.sql.SQLException if a database access error occurs
	 * @since 1.2
	 * 未支持
	 *
	void setTimestamp(int parameterIndex, java.sql.Timestamp x, Calendar cal)
			throws SQLException;
	*/

	/* *
	 * Sets the designated parameter to SQL <code>NULL</code>.
	 * This version of the method <code>setNull</code> should
	 * be used for user-defined types and REF type parameters.  Examples
	 * of user-defined types include: STRUCT, DISTINCT, JAVA_OBJECT, and
	 * named array types.
	 *
	 * <P><B>Note:</B> To be portable, applications must give the
	 * SQL type code and the fully-qualified SQL type name when specifying
	 * a NULL user-defined or REF parameter.  In the case of a user-defined type
	 * the name is the type name of the parameter itself.  For a REF
	 * parameter, the name is the type name of the referenced type.  If
	 * a JDBC driver does not need the type code or type name information,
	 * it may ignore it.
	 *
	 * Although it is intended for user-defined and Ref parameters,
	 * this method may be used to set a null parameter of any JDBC type.
	 * If the parameter does not have a user-defined or REF type, the given
	 * typeName is ignored.
	 *
	 *
	 * @param paramIndex the first parameter is 1, the second is 2, ...
	 * @param sqlType a value from <code>java.sql.Types</code>
	 * @param typeName the fully-qualified name of an SQL user-defined type;
	 *  ignored if the parameter is not a user-defined type or REF
	 * @exception java.sql.SQLException if a database access error occurs
	 * @since 1.2
	 * 未支持
	 *
	void setNull(int paramIndex, int sqlType, String typeName)
			throws SQLException;
	*/

	//------------------------- JDBC 3.0 -----------------------------------

	/* *
	 * Sets the designated parameter to the given <code>java.net.URL</code> value.
	 * The driver converts this to an SQL <code>DATALINK</code> value
	 * when it sends it to the database.
	 *
	 * @param parameterIndex the first parameter is 1, the second is 2, ...
	 * @param x the <code>java.net.URL</code> object to be set
	 * @exception java.sql.SQLException if a database access error occurs
	 * @since 1.4
	 * 未支持
	 *
	void setURL(int parameterIndex, java.net.URL x) throws SQLException;
	*/

	/* *
	 * Retrieves the number, types and properties of this
	 * <code>PreparedStatement</code> object's parameters.
	 *
	 * @return a <code>ParameterMetaData</code> object that contains information
	 *         about the number, types and properties of this
	 *         <code>PreparedStatement</code> object's parameters
	 * @exception java.sql.SQLException if a database access error occurs
	 * @see java.sql.ParameterMetaData
	 * @since 1.4
	 * 不支持
	 *
	ParameterMetaData getParameterMetaData() throws SQLException;
	*/
}