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

import java.sql.SQLException;
import java.util.Iterator;

import self.micromagic.eterna.share.EternaException;

/**
 * @author micromagic@sina.com
 */
public interface ResultIterator extends Iterator
{
	/**
	 * 用于标志在保持连接的结果集中不要关闭连接.
	 */
	String DONT_CLOSE_CONNECTION = "ETERNA_DONT_CLOSE_CONNECTION";

	/**
	 * 获取这个<code>ResultIterator</code>的列数, 以及每列的名称 宽度
	 * 标题 reader对象.
	 */
	ResultMetaData getMetaData() throws SQLException, EternaException;

	/**
	 * 判断是否还有下一条的记录.
	 */
	boolean hasNextRow() throws SQLException, EternaException;

	/**
	 * 预取下一个<code>ResultRow</code>. <p>
	 * 这个操作并不会将游标指向下一个, 所以就算是调用多次, 也只是取到下一个.
	 * 如果没有下一个, 那返回null.
	 */
	ResultRow preFetch() throws SQLException, EternaException;

	/**
	 * 预取下面的某个<code>ResultRow</code>. <p>
	 * 这个操作并不会将游标移动, 所以就算是调用多次, 游标也是在原来位置.
	 * 如果剩余记录数没那么多, 那返回null.
	 *
	 * @param index    要预取之后的第几条记录, 1为第一条 2为第二条
	 */
	ResultRow preFetch(int index) throws SQLException, EternaException;

	/**
	 * 获取当前<code>ResultRow</code>.
	 * 如果未执行过 nextRow或next, 或者刚执行过beforeFirst, 那返回null.
	 */
	ResultRow getCurrentRow() throws SQLException, EternaException;

	/**
	 * 获取下一个<code>ResultRow</code>.
	 */
	ResultRow nextRow() throws SQLException, EternaException;

	/**
	 * 将游标移到第一行之前.
	 *
	 * @return <code>true</code> 假如游标移动成功
	 *         <code>false</code> 游标无法移动
	*/
	boolean beforeFirst() throws SQLException, EternaException;

	/**
	 * 关闭这个<code>ResultIterator</code>对象, 关闭的同时会关闭对应
	 * 的数据库连接.
	 */
	void close() throws SQLException, EternaException;

	/**
	 * 获取这个<code>ResultIterator</code>的副本.
	 * 生成副本的同时会调用beforeFirst方法, 将游标移到第一行之前.
	 *
	 * @return    生成的副本, 如果无法生成副本则返回<code>null</code>.
	 * @see #beforeFirst
	 */
	ResultIterator copy() throws EternaException;

	/**
	 * 取得查询的结果集中的总记录数. <p>
	 */
	int getTotalCount() throws SQLException, EternaException;

	/**
	 * 取得结果集中的记录数. <p>
	 * 如果获取的是所有记录，则返回的记录数与方法{@link #getTotalCount}
	 * 返回的记录数相同。反之，获取的记录数比总记录数小。
	 */
	int getCount() throws SQLException, EternaException;

	/**
	 * {@link #getTotalCount}中得到查询的结果集中的总记录数是否有效. <p>
	 * 当查询的计数方式为{@link Query#TOTAL_COUNT_MODEL_AUTO}或
	 * {@link Query#TOTAL_COUNT_MODEL_COUNT}时, 总记录数会是有效的.
	 * 如果总记录数无效，则{@link #getTotalCount}返回的个数并
	 * 不是实际的总记录数。
	 */
	boolean isTotalCountAvailable() throws SQLException, EternaException;

	/**
	 * 实际结果集中是否还有更多的记录. <p>
	 * 如果获取的不是所有的记录, 则可通过这个方法来判断实际的结果集中是否还有
	 * 更多的记录.
	 */
	boolean hasMoreRecord() throws SQLException, EternaException;

}