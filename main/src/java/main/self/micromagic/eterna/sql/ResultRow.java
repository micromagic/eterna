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

import java.sql.SQLException;

import self.micromagic.eterna.share.EternaException;

public interface ResultRow
{
	/**
	 * 获得当前的ResultRow所在的ResultIterator.
	 */
	ResultIterator getResultIterator() throws SQLException, EternaException;

	/**
	 * 获取当前<code>ResultRow</code>在结果中的位置.
	 * 如果在第一条记录则返回1, 第二条记录则返回2...
	 */
	int getRowNum() throws SQLException, EternaException;

	/**
	 * 如果指定的列设置了format, 则以getFormated方式获取值.
	 * 如果未设置, 则以getObject方式获取值.
	 *
	 * @param columnIndex  参数的索引值, 第一个是1, 第二个是2, ....
	 * @return 格式化后的数据.
	 *
	 * @see #getFormated(int)
	 * @see #getObject(int)
	 * @exception SQLException 假如访问数据库时出错
	 */
	Object getSmartValue(int columnIndex) throws SQLException, EternaException;

	/**
	 * 如果指定的列设置了format, 则以getFormated方式获取值.
	 * 如果未设置, 则以getObject方式获取值.
	 *
	 * @param columnName  参数的名称.
	 * @param notThrow    设为<code>true<code>时, 当对应名称的reader不存在时
	 *                    不会抛出异常, 而只是返回null
	 * @return 格式化后的数据.
	 *
	 * @see #getFormated(String)
	 * @see #getObject(String)
	 * @exception SQLException 假如访问数据库时出错
	 */
	Object getSmartValue(String columnName, boolean notThrow) throws SQLException, EternaException;

	/**
	 * 在当前行<code>ResultRow</code>对象中取出格式化后指定的列.
	 *
	 * @param columnIndex  参数的索引值, 第一个是1, 第二个是2, ....
	 * @return 格式化后的数据.
	 *
	 * @exception SQLException 假如访问数据库时出错
	 */
	Object getFormated(int columnIndex) throws SQLException, EternaException;

	/**
	 * 在当前行<code>ResultRow</code>对象中取出格式化后指定的列.
	 *
	 * @param columnName  参数的名称.
	 * @return 格式化后的数据.
	 *
	 * @exception SQLException 假如访问数据库时出错
	 */
	Object getFormated(String columnName) throws SQLException, EternaException;

	/**
	 * Reports whether
	 * the last column read had a value of SQL <code>NULL</code>.
	 * 报告最后一次读取的列值是否为SQL的<code>NULL</code>.
	 * Note that you must first call one of the getter methods
	 * on a column to try to read its value and then call
	 * the method <code>wasNull</code> to see if the value read was
	 * SQL <code>NULL</code>.
	 * 注: 此方法是非线程安全的.
	 *
	 * @return <code>true</code> if the last column value read was SQL
	 *         <code>NULL</code> and <code>false</code> otherwise
	 *         当最后一次读取的列值是SQL的<code>NULL</code>时则返回
	 *         <code>true</code>否则返回<code>false</code>
	 * @exception SQLException if a database access error occurs
	 *                         <p>假如访问数据库时出错
	 */
	boolean wasNull() throws SQLException, EternaException;

	//======================================================================
	// Methods for accessing results by column index
	//======================================================================

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>String</code> in the Java programming language.
	 * 在当前行<code>ResultRow</code>对象中以<code>String</code>的形式
	 * 取出指定列的值.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 *                    <p>第一个参数是1, 第二个是2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
	 *         <p>这个列的值; 如果这个值是SQL的<code>NULL</code>, 则返回值
	 * 是<code>null</code>
	 * @exception SQLException if a database access error occurs
	 *                         <p>假如访问数据库时出错
	 */
	String getString(int columnIndex) throws SQLException, EternaException;

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>boolean</code> in the Java programming language.
	 * 在当前行<code>ResultRow</code>对象中以<code>boolean</code>的形式
	 * 取出指定列的值.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 *                    <p>第一个参数是1, 第二个是2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>false</code>
	 *         <p>这个列的值; 如果这个值是SQL的<code>NULL</code>, 则返回值
	 * 是<code>false</code>
	 * @exception SQLException if a database access error occurs
	 *                         <p>假如访问数据库时出错
	 */
	boolean getBoolean(int columnIndex) throws SQLException, EternaException;

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>byte</code> in the Java programming language.
	 * 在当前行<code>ResultRow</code>对象中以<code>byte</code>的形式
	 * 取出指定列的值.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 *                    <p>第一个参数是1, 第二个是2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 *         <p>这个列的值; 如果这个值是SQL的<code>NULL</code>, 则返回值
	 * 是<code>0</code>
	 * @exception SQLException if a database access error occurs
	 *                         <p>假如访问数据库时出错
	 */
	byte getByte(int columnIndex) throws SQLException, EternaException;

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>short</code> in the Java programming language.
	 * 在当前行<code>ResultRow</code>对象中以<code>short</code>的形式
	 * 取出指定列的值.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 *                    <p>第一个参数是1, 第二个是2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 *         <p>这个列的值; 如果这个值是SQL的<code>NULL</code>, 则返回值
	 * 是<code>0</code>
	 * @exception SQLException if a database access error occurs
	 *                         <p>假如访问数据库时出错
	 */
	short getShort(int columnIndex) throws SQLException, EternaException;

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * an <code>int</code> in the Java programming language.
	 * 在当前行<code>ResultRow</code>对象中以<code>int</code>的形式
	 * 取出指定列的值.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 *                    <p>第一个参数是1, 第二个是2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 *         <p>这个列的值; 如果这个值是SQL的<code>NULL</code>, 则返回值
	 * 是<code>0</code>
	 * @exception SQLException if a database access error occurs
	 *                         <p>假如访问数据库时出错
	 */
	int getInt(int columnIndex) throws SQLException, EternaException;

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>long</code> in the Java programming language.
	 * 在当前行<code>ResultRow</code>对象中以<code>long</code>的形式
	 * 取出指定列的值.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 *                    <p>第一个参数是1, 第二个是2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 *         <p>这个列的值; 如果这个值是SQL的<code>NULL</code>, 则返回值
	 * 是<code>0</code>
	 * @exception SQLException if a database access error occurs
	 *                         <p>假如访问数据库时出错
	 */
	long getLong(int columnIndex) throws SQLException, EternaException;

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>float</code> in the Java programming language.
	 * 在当前行<code>ResultRow</code>对象中以<code>float</code>的形式
	 * 取出指定列的值.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 *                    <p>第一个参数是1, 第二个是2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 *         <p>这个列的值; 如果这个值是SQL的<code>NULL</code>, 则返回值
	 * 是<code>0</code>
	 * @exception SQLException if a database access error occurs
	 *                         <p>假如访问数据库时出错
	 */
	float getFloat(int columnIndex) throws SQLException, EternaException;

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>double</code> in the Java programming language.
	 * 在当前行<code>ResultRow</code>对象中以<code>double</code>的形式
	 * 取出指定列的值.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 *                    <p>第一个参数是1, 第二个是2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 *         <p>这个列的值; 如果这个值是SQL的<code>NULL</code>, 则返回值
	 * 是<code>0</code>
	 * @exception SQLException if a database access error occurs
	 *                         <p>假如访问数据库时出错
	 */
	double getDouble(int columnIndex) throws SQLException, EternaException;

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>byte</code> array in the Java programming language.
	 * The bytes represent the raw values returned by the driver.
	 * 在当前行<code>ResultRow</code>对象中以<code>byte</code>数组的形式
	 * 取出指定列的值.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 *                    <p>第一个参数是1, 第二个是2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
	 *         <p>这个列的值; 如果这个值是SQL的<code>NULL</code>, 则返回值
	 * 是<code>null</code>
	 * @exception SQLException if a database access error occurs
	 *                         <p>假如访问数据库时出错
	 */
	byte[] getBytes(int columnIndex) throws SQLException, EternaException;

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>java.sql.Date</code> object in the Java programming language.
	 * 在当前行<code>ResultRow</code>对象中以<code>java.sql.Date</code>的形式
	 * 取出指定列的值.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 *                    <p>第一个参数是1, 第二个是2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
	 *         <p>这个列的值; 如果这个值是SQL的<code>NULL</code>, 则返回值
	 * 是<code>null</code>
	 * @exception SQLException if a database access error occurs
	 *                         <p>假如访问数据库时出错
	 */
	java.sql.Date getDate(int columnIndex) throws SQLException, EternaException;

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>java.sql.Time</code> object in the Java programming language.
	 * 在当前行<code>ResultRow</code>对象中以<code>java.sql.Time</code>的形式
	 * 取出指定列的值.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 *                    <p>第一个参数是1, 第二个是2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
	 *         <p>这个列的值; 如果这个值是SQL的<code>NULL</code>, 则返回值
	 * 是<code>null</code>
	 * @exception SQLException if a database access error occurs
	 *                         <p>假如访问数据库时出错
	 */
	java.sql.Time getTime(int columnIndex) throws SQLException, EternaException;

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>java.sql.Timestamp</code> object in the Java programming language.
	 * 在当前行<code>ResultRow</code>对象中以<code>java.sql.Timestamp</code>的形式
	 * 取出指定列的值.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 *                    <p>第一个参数是1, 第二个是2, ...
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
	 *         <p>这个列的值; 如果这个值是SQL的<code>NULL</code>, 则返回值
	 * 是<code>null</code>
	 * @exception SQLException if a database access error occurs
	 *                         <p>假如访问数据库时出错
	 */
	java.sql.Timestamp getTimestamp(int columnIndex) throws SQLException, EternaException;

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a binary stream of
	 * uninterpreted bytes. The value can then be read in chunks from the
	 * stream. This method is particularly
	 * suitable for retrieving large <code>LONGVARBINARY</code> values.
	 *
	 * <P><B>Note:</B> All the data in the returned stream must be
	 * read prior to getting the value of any other column. The next
	 * call to a getter method implicitly closes the stream.  Also, a
	 * stream may return <code>0</code> when the method
	 * <code>InputStream.available</code>
	 * is called whether there is data available or not.
	 *
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @return a Java input stream that delivers the database column value
	 *         as a stream of uninterpreted bytes;
	 *         if the value is SQL <code>NULL</code>, the value returned is
	 *         <code>null</code>
	 * @exception SQLException if a database access error occurs
	 *
	 */
	java.io.InputStream getBinaryStream(int columnIndex) throws SQLException, EternaException;

	//======================================================================
	// Methods for accessing results by column name
	//======================================================================

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>String</code> in the Java programming language.
	 * 在当前行<code>ResultRow</code>对象中以<code>String</code>的形式
	 * 取出指定列的值.
	 *
	 * @param columnName the SQL name of the column
	 *                   <p>这个列的SQL名称
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
	 *         <p>这个列的值; 如果这个值是SQL的<code>NULL</code>, 则返回值
	 * 是<code>null</code>
	 * @exception SQLException if a database access error occurs
	 *                         <p>假如访问数据库时出错
	 */
	String getString(String columnName) throws SQLException, EternaException;

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>boolean</code> in the Java programming language.
	 * 在当前行<code>ResultRow</code>对象中以<code>boolean</code>的形式
	 * 取出指定列的值.
	 *
	 * @param columnName the SQL name of the column
	 *                   <p>这个列的SQL名称
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>false</code>
	 *         <p>这个列的值; 如果这个值是SQL的<code>NULL</code>, 则返回值
	 * 是<code>false</code>
	 * @exception SQLException if a database access error occurs
	 *                         <p>假如访问数据库时出错
	 */
	boolean getBoolean(String columnName) throws SQLException, EternaException;

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>byte</code> in the Java programming language.
	 * 在当前行<code>ResultRow</code>对象中以<code>byte</code>的形式
	 * 取出指定列的值.
	 *
	 * @param columnName the SQL name of the column
	 *                   <p>这个列的SQL名称
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 *         <p>这个列的值; 如果这个值是SQL的<code>NULL</code>, 则返回值
	 * 是<code>0</code>
	 * @exception SQLException if a database access error occurs
	 *                         <p>假如访问数据库时出错
	 */
	byte getByte(String columnName) throws SQLException, EternaException;

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>short</code> in the Java programming language.
	 * 在当前行<code>ResultRow</code>对象中以<code>short</code>的形式
	 * 取出指定列的值.
	 *
	 * @param columnName the SQL name of the column
	 *                   <p>这个列的SQL名称
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 *         <p>这个列的值; 如果这个值是SQL的<code>NULL</code>, 则返回值
	 * 是<code>0</code>
	 * @exception SQLException if a database access error occurs
	 *                         <p>假如访问数据库时出错
	 */
	short getShort(String columnName) throws SQLException, EternaException;

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * an <code>int</code> in the Java programming language.
	 * 在当前行<code>ResultRow</code>对象中以<code>int</code>的形式
	 * 取出指定列的值.
	 *
	 * @param columnName the SQL name of the column
	 *                   <p>这个列的SQL名称
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 *         <p>这个列的值; 如果这个值是SQL的<code>NULL</code>, 则返回值
	 * 是<code>0</code>
	 * @exception SQLException if a database access error occurs
	 *                         <p>假如访问数据库时出错
	 */
	int getInt(String columnName) throws SQLException, EternaException;

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>long</code> in the Java programming language.
	 * 在当前行<code>ResultRow</code>对象中以<code>long</code>的形式
	 * 取出指定列的值.
	 *
	 * @param columnName the SQL name of the column
	 *                   <p>这个列的SQL名称
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 *         <p>这个列的值; 如果这个值是SQL的<code>NULL</code>, 则返回值
	 * 是<code>0</code>
	 * @exception SQLException if a database access error occurs
	 *                         <p>假如访问数据库时出错
	 */
	long getLong(String columnName) throws SQLException, EternaException;

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>float</code> in the Java programming language.
	 * 在当前行<code>ResultRow</code>对象中以<code>float</code>的形式
	 * 取出指定列的值.
	 *
	 * @param columnName the SQL name of the column
	 *                   <p>这个列的SQL名称
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 *         <p>这个列的值; 如果这个值是SQL的<code>NULL</code>, 则返回值
	 * 是<code>0</code>
	 * @exception SQLException if a database access error occurs
	 *                         <p>假如访问数据库时出错
	 */
	float getFloat(String columnName) throws SQLException, EternaException;

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>double</code> in the Java programming language.
	 * 在当前行<code>ResultRow</code>对象中以<code>double</code>的形式
	 * 取出指定列的值.
	 *
	 * @param columnName the SQL name of the column
	 *                   <p>这个列的SQL名称
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>0</code>
	 *         <p>这个列的值; 如果这个值是SQL的<code>NULL</code>, 则返回值
	 * 是<code>0</code>
	 * @exception SQLException if a database access error occurs
	 *                         <p>假如访问数据库时出错
	 */
	double getDouble(String columnName) throws SQLException, EternaException;

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>byte</code> array in the Java programming language.
	 * The bytes represent the raw values returned by the driver.
	 * 在当前行<code>ResultRow</code>对象中以<code>byte</code>的数组形式
	 * 取出指定列的值.
	 *
	 * @param columnName the SQL name of the column
	 *                   <p>这个列的SQL名称
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
	 *         <p>这个列的值; 如果这个值是SQL的<code>NULL</code>, 则返回值
	 * 是<code>null</code>
	 * @exception SQLException if a database access error occurs
	 *                         <p>假如访问数据库时出错
	 */
	byte[] getBytes(String columnName) throws SQLException, EternaException;

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>java.sql.Date</code> object in the Java programming language.
	 * 在当前行<code>ResultRow</code>对象中以<code>java.sql.Date</code>的形式
	 * 取出指定列的值.
	 *
	 * @param columnName the SQL name of the column
	 *                   <p>这个列的SQL名称
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
	 *         <p>这个列的值; 如果这个值是SQL的<code>NULL</code>, 则返回值
	 * 是<code>null</code>
	 * @exception SQLException if a database access error occurs
	 *                         <p>假如访问数据库时出错
	 */
	java.sql.Date getDate(String columnName) throws SQLException, EternaException;

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>java.sql.Time</code> object in the Java programming language.
	 * 在当前行<code>ResultRow</code>对象中以<code>java.sql.Time</code>的形式
	 * 取出指定列的值.
	 *
	 * @param columnName the SQL name of the column
	 *                   <p>这个列的SQL名称
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
	 *         <p>这个列的值; 如果这个值是SQL的<code>NULL</code>, 则返回值
	 * 是<code>null</code>
	 * @exception SQLException if a database access error occurs
	 *                         <p>假如访问数据库时出错
	 */
	java.sql.Time getTime(String columnName) throws SQLException, EternaException;

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as
	 * a <code>java.sql.Timestamp</code> object.
	 * 在当前行<code>ResultRow</code>对象中以<code>java.sql.Timestamp</code>的形式
	 * 取出指定列的值.
	 *
	 * @param columnName the SQL name of the column
	 *                   <p>这个列的SQL名称
	 * @return the column value; if the value is SQL <code>NULL</code>, the
	 * value returned is <code>null</code>
	 *         <p>这个列的值; 如果这个值是SQL的<code>NULL</code>, 则返回值
	 * 是<code>null</code>
	 * @exception SQLException if a database access error occurs
	 *                         <p>假如访问数据库时出错
	 */
	java.sql.Timestamp getTimestamp(String columnName) throws SQLException, EternaException;

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a stream of uninterpreted
	 * <code>byte</code>s.
	 * The value can then be read in chunks from the
	 * stream. This method is particularly
	 * suitable for retrieving large <code>LONGVARBINARY</code>
	 * values.
	 *
	 * <P><B>Note:</B> All the data in the returned stream must be
	 * read prior to getting the value of any other column. The next
	 * call to a getter method implicitly closes the stream. Also, a
	 * stream may return <code>0</code> when the method <code>available</code>
	 * is called whether there is data available or not.
	 *
	 * @param columnName the SQL name of the column
	 * @return a Java input stream that delivers the database column value
	 * as a stream of uninterpreted bytes;
	 * if the value is SQL <code>NULL</code>, the result is <code>null</code>
	 * @exception SQLException if a database access error occurs
	 */
	java.io.InputStream getBinaryStream(String columnName) throws SQLException, EternaException;


	//=====================================================================
	// Advanced features:
	//=====================================================================

	/**
	 * 在当前行<code>ResultRow</code>对象中以<code>Object</code>的形式
	 * 取出指定列的值.
	 *
	 * @param columnIndex 该列对应reader的索引值, 第一个是1, 第二个是2, ...
	 * @return  持有该列值的<code>java.lang.Object</code>
	 * @exception SQLException  假如访问数据库时出错
	 */
	Object getObject(int columnIndex) throws SQLException, EternaException;

	/**
	 * 在当前行<code>ResultRow</code>对象中以<code>Object</code>的形式
	 * 取出指定列的值.
	 *
	 * @param columnName  这个列对应reader的名称
	 * @return  持有该列值的<code>java.lang.Object</code>
	 * @exception SQLException   假如访问数据库时出错
	 */
	Object getObject(String columnName) throws SQLException, EternaException;

	/**
	 * 在当前行<code>ResultRow</code>对象中以<code>Object</code>的形式
	 * 取出指定列的值.
	 *
	 * @param columnName  这个列对应reader的名称
	 * @param notThrow    设为<code>true<code>时, 当对应名称的reader不存在时
	 *                    不会抛出异常, 而只是返回null
	 * @return  持有该列值的<code>java.lang.Object</code>,
	 *          或null(当对应名称的reader不存在时)
	 * @exception SQLException   假如访问数据库时出错
	 */
	Object getObject(String columnName, boolean notThrow) throws SQLException, EternaException;

	//----------------------------------------------------------------

	/**
	 * 在<code>ResultRow</code>中将列名映射为列的索引值.
	 *
	 * @param columnName  该列的名称
	 * @return    所给列名的索引值
	 * @exception SQLException  假如<code>ResultRow</code>对象中不存在该列名或
	 *                          访问数据库时发生错误
	 */
	int findColumn(String columnName) throws SQLException, EternaException;

	/**
	 * 在<code>ResultRow</code>中将列名映射为列的索引值.
	 *
	 * @param columnName  该列的名称
	 * @param notThrow    设为<code>true<code>时, 当对应名称的reader不存在时
	 *                    不会抛出异常, 而只是返回-1
	 * @return    所给列名的索引值, 或-1(当不存在该列名时)
	 * @exception SQLException  假如<code>ResultRow</code>对象中不存在该列名或
	 *                          访问数据库时发生错误
	 */
	int findColumn(String columnName, boolean notThrow) throws SQLException, EternaException;


	//--------------------------JDBC 2.0-----------------------------------

	//---------------------------------------------------------------------
	// Getters and Setters
	//---------------------------------------------------------------------

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a
	 * <code>java.io.Reader</code> object.
	 * @return a <code>java.io.Reader</code> object that contains the column
	 * value; if the value is SQL <code>NULL</code>, the value returned is
	 * <code>null</code> in the Java programming language.
	 * @param columnIndex the first column is 1, the second is 2, ...
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	java.io.Reader getCharacterStream(int columnIndex) throws SQLException, EternaException;

	/**
	 * Retrieves the value of the designated column in the current row
	 * of this <code>ResultSet</code> object as a
	 * <code>java.io.Reader</code> object.
	 *
	 * @param columnName the name of the column
	 * @return a <code>java.io.Reader</code> object that contains the column
	 * value; if the value is SQL <code>NULL</code>, the value returned is
	 * <code>null</code> in the Java programming language
	 * @exception SQLException if a database access error occurs
	 * @since 1.2
	 */
	java.io.Reader getCharacterStream(String columnName) throws SQLException, EternaException;

}