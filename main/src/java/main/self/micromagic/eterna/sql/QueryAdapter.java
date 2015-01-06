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
import java.sql.SQLException;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.security.Permission;
import self.micromagic.eterna.sql.impl.ResultReaders;
import self.micromagic.util.BooleanRef;

/**
 * @author micromagic@sina.com
 */
public interface QueryAdapter extends SQLAdapter
{
	/**
	 * 可设置查询前是否需要检查reader列表, 将结果中不存在的去除, 未设置的添加上. <p>
	 * 如果设为true表示需要检查, 设为false(默认值)表示不需要检查.
	 * 在query的attribute中设置, 仅对此quer有效.
	 * 在factory的attribute中设置, 将对此工厂中的所有未设置的query有效.
	 */
	String CHECK_READER_FLAG = "checkResultReader";

	/**
	 * 设置是否需要检查数据库名称的标签, 如果设为true(默认值)表示需要检查. <p>
	 * 对于一些数据库, 当设置了startRow和maxRows时, 会添加获取记录数限制的语句.
	 */
	String CHECK_DATABASE_NAME_FLAG = "checkDatabaseName";

	/**
	 * 复制所有的ResultReader. <p>
	 * 如果设为true, 则会在初始化时复制所有继承了ResultReaders.ObjectReader
	 * 的ResultReader, 并且将其checkIndex属性设为true.
	 * 默认值为false.
	 *
	 * @see ResultReaders.ObjectReader
	 * @see ResultReaders.ObjectReader#setCheckIndex
	 */
	String COPY_READERS_FLAG = "copyReaders";

	/**
	 * 可设置的其它ResultReaderManager的名称列表, 以逗号分割.
	 */
	String OTHER_READER_MANAGER_SET_FLAG = "otherReaderManagerSet";

	/**
	 * 自动计算总记录数, 通过循环ResultSet的next方法来计算.
	 */
	int TOTAL_COUNT_AUTO = -1;

	/**
	 * 不计算总记录数.
	 */
	int TOTAL_COUNT_NONE = -2;

	/**
	 * 通过自动生成的count语句, 获取总记录数.
	 */
	int TOTAL_COUNT_COUNT = -3;

	/**
	 * 如果查询时要根据权限来控制列的显示, 请在执行前设置权限类.
	 */
	void setPermission(Permission permission);

	/**
	 * 获得原始的查询语句.
	 * 当调用<code>getPreparedSQL</code>, 获取的SQL有可能是被转换过的,
	 * 如: 当设置了startRow和maxRows时, 会添加获取记录数限制的语句等.
	 *
	 * @see #getPreparedSQL
	 */
	String getPrimitiveQuerySQL() throws EternaException;

	/**
	 * 获得ResultReader的排序方式字符串.
	 */
	String getReaderOrder() throws EternaException;

	/**
	 * 获得当前查询对象的ResultReaderManager. <p>
	 * 该方法主要用于对ResultReaderManager进行一些设置, 如自定义列.
	 * 然后通过{@link #setReaderManager(ResultReaderManager)}方法将
	 * 改动作用到查询对象上.
	 */
	ResultReaderManager getReaderManager() throws EternaException;

	/**
	 * 设置该查询的ResultReaderManager, 所设置的ResultReaderManager的名称必须
	 * 与原来的名称相同, 或者在<code>otherReaderManagerSet</code>列表中.
	 */
	void setReaderManager(ResultReaderManager readerManager) throws EternaException;

	/**
	 * 是否可设置排序. <p>
	 * 如果在定义时设置了orderIndex这该query是可设置排序的.
	 */
	boolean canOrder() throws EternaException;

	/**
	 * 设置单列排序的列. <p>
	 * 排序的升降序为自动设置, 如前一次设置了该列, 初始为升序, 则再次
	 * 设置时就会变为降序. 如果前一次设置的不是该列, 则默认为升序.
	 */
	void setSingleOrder(String readerName) throws EternaException;

	/**
	 * 设置单列排序的列.
	 *
	 * @param orderType   0    表示自动选择顺序, 同{@link #setSingleOrder(String)}
	 *                    负数 表示使用降序
	 *                    正数 表示使用升序
	 */
	void setSingleOrder(String readerName, int orderType) throws EternaException;

	/**
	 * 获取单列排序的列名, 该名称为某个reader的名称. <p>
	 * 如果尚未设置单列排序, 则返回null.
	 *
	 * @param desc  表示是否为降序, true表示是降序
	 */
	String getSingleOrder(BooleanRef desc) throws EternaException;

	/**
	 * 设置多列排序. <p>
	 * 注: 多列排序设置的不是reader的名称, 而是reader的名称再加上排序符.
	 * 如: 升序 readerName + 'A', 降序 readerName + 'D'
	 *
	 * @param orderNames   多列排序排序名称数组
	 */
	void setMultipleOrder(String[] orderNames) throws EternaException;

	/**
	 * 获取这个查询对象是否只能用forwardOnly模式来执行.
	 */
	boolean isForwardOnly() throws EternaException;

	/**
	 * 获取该查询对象是从第几条记录开始读取, 默认值为"1".
	 */
	int getStartRow() throws SQLException;

	/**
	 * 设置从第几条记录开始取值(从1开始计数).
	 *
	 * @param startRow   起始行号
	 */
	void setStartRow(int startRow) throws SQLException;

	/**
	 * 获取该查询对象读取的最大记录数, 默认值为"-1", 表示
	 * 取完为止.
	 */
	int getMaxRows() throws SQLException;

	/**
	 * 设置取出的最大记录数，-1表示取完为止.
	 *
	 * @param maxRows   取出的最大记录数
	 */
	void setMaxRows(int maxRows) throws SQLException;

	/**
	 * 获取该查询对象设置的总记录数.
	 */
	int getTotalCount() throws EternaException;

	/**
	 * 设置该查询对象的记录数. <p>
	 * 默认值为<code>TOTAL_COUNT_AUTO(-1)</code>.
	 *
	 * @param totalCount   总记录数.
	 *                     <code>TOTAL_COUNT_AUTO(-1)</code>, <code>TOTAL_COUNT_NONE(-2)</code>,
	 *                     <code>TOTAL_COUNT_COUNT(-3)</code>为特殊的设置. 0-N为直接设置总记录数.
	 *
	 * @see #TOTAL_COUNT_AUTO
	 * @see #TOTAL_COUNT_NONE
	 * @see #TOTAL_COUNT_COUNT
	 */
	void setTotalCount(int totalCount) throws EternaException;

	/**
	 * 设置该查询对象的记录数. <p>
	 *
	 * @param totalCount   总记录数.
	 * @param ext          扩展信息, 只有在totalCount的值设为0-N时才有效.
	 *
	 * @see #setTotalCount(int)
	 * @see #TOTAL_COUNT_AUTO
	 * @see #TOTAL_COUNT_NONE
	 * @see #TOTAL_COUNT_COUNT
	 */
	void setTotalCount(int totalCount, TotalCountExt ext) throws EternaException;

	/**
	 * 获取该查询对象设置的总记录数扩展信息. <p>
	 * 只有在totalCount的值设为0-N时, 该值才有效.
	 */
	TotalCountExt getTotalCountExt() throws EternaException;

	/**
	 * 获得查询的结果, 用于少量(100条左右)数据的查询.
	 * <p>实现者要保存结果, 在数据库连接<code>Connection</code>被关闭的时候,
	 * 也要能够读取数据.
	 *
	 * @param conn      数据库的连接
	 * @return          查询出来的结果集
	 * @throws EternaException   当相关配置出错时
	 * @throws SQLException    假如访问数据库时出错
	 */
	ResultIterator executeQuery(Connection conn)
			throws EternaException, SQLException;

	/**
	 * 获得查询的结果, 用于大量(500条以上)数据的查询.
	 * <p>实现者不需保存结果, 可以直接在使用<code>ResultSet</code>作为结果的载体.
	 * 当数据库连接<code>Connection</code>被关闭的时候, 也就无法读取数据.
	 *
	 * 注: 使用这种读取方式时, 将忽略startRow和maxRow的设置.
	 *
	 * @param conn      数据库的连接
	 * @return          查询出来的结果集
	 * @throws EternaException  当相关配置出错时
	 * @throws SQLException    假如访问数据库时出错
	 */
	ResultIterator executeQueryHoldConnection(Connection conn)
			throws EternaException, SQLException;

	/**
	 * 设置总记录数的扩展信息.
	 */
	static final class TotalCountExt
	{
		/**
		 * 是否还有更多记录.
		 */
		public final boolean hasMoreRecord;

		/**
		 * 总记录数是否可用.
		 */
		public final boolean realRecordCountAvailable;

		public TotalCountExt(boolean hasMoreRecord, boolean realRecordCountAvailable)
		{
			this.hasMoreRecord = hasMoreRecord;
			this.realRecordCountAvailable = realRecordCountAvailable;
		}

		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj instanceof TotalCountExt)
			{
				TotalCountExt other = (TotalCountExt) obj;
				return this.hasMoreRecord == other.hasMoreRecord
						&& this.realRecordCountAvailable == other.realRecordCountAvailable;
			}
			return false;
		}

	}

}