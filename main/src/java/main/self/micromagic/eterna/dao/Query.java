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
import java.sql.SQLException;

import self.micromagic.eterna.security.Permission;
import self.micromagic.eterna.share.EternaException;

/**
 * 数据库的查询对象.
 */
public interface Query extends Dao
{
	/**
	 * 可设置查询前是否需要检查reader列表, 将结果中不存在的去除, 未设置的添加上. <p>
	 * 如果设为true表示需要检查, 设为false(默认值)表示不需要检查.
	 * 在query的attribute中设置, 仅对此query有效.
	 * 在factory的attribute中设置, 将对此工厂中的所有未设置的query有效.
	 */
	String CHECK_READER_FLAG = "checkResultReader";

	/**
	 * 设置是否需要检查数据库名称的标签, 如果设为true(默认值)表示需要检查. <p>
	 * 对于一些数据库, 当设置了startRow和maxRows时, 会添加获取记录数限制的语句.
	 */
	String CHECK_DATABASE_NAME_FLAG = "checkDatabaseName";

	/**
	 * 自动计算总记录数, 通过循环ResultSet的next方法来计算.
	 */
	int TOTAL_COUNT_MODEL_AUTO = -1;

	/**
	 * 不计算总记录数.
	 */
	int TOTAL_COUNT_MODEL_NONE = -2;

	/**
	 * 通过自动生成的count语句, 获取总记录数.
	 */
	int TOTAL_COUNT_MODEL_COUNT = -3;

	/**
	 * 如果查询时要根据权限来控制列的显示, 请在执行前设置权限类.
	 */
	void setPermission(Permission permission);

	/**
	 * 获得原始的查询语句.
	 * 当调用<code>getPreparedScript</code>, 获取的SQL有可能是被转换过的,
	 * 如: 当设置了startRow和maxRows时, 会添加获取记录数限制的语句等.
	 *
	 * @see #getPreparedScript()
	 */
	String getPrimitiveQueryScript() throws EternaException;

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
	 * 如果在定义时设置了orderIndex, 则此query是可设置排序的.
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
	 * @param orderFlag   0    表示自动选择升降序, 同{@link #setSingleOrder(String)}
	 *                    -1 表示使用降序
	 *                    1 表示使用升序
	 */
	void setSingleOrder(String readerName, int orderFlag) throws EternaException;

	/**
	 * 获取排序的配置字符串, 每个排序配置间用","分隔
	 *
	 * @see #setMultipleOrder(String[])
	 */
	String getOrderConfig() throws EternaException;

	/**
	 * 设置多列排序. <p>
	 * 注: 多列排序设置的不只是reader的名称, 而是reader的名称再加上排序标记.
	 * 排序标记有:
	 * {@link ResultReaderManager#ORDER_FLAG_DESC}"-name" 表示此列降序
	 * {@link ResultReaderManager#ORDER_FLAG_ASC}"+name" 表示此列升序
	 *
	 * @param orderNames  带有排序标记的reader名称列表
	 */
	void setMultipleOrder(String[] orderNames) throws EternaException;

	/**
	 * 获取这个查询对象是否只能用forwardOnly模式来执行.
	 */
	boolean isForwardOnly() throws EternaException;

	/**
	 * 获取该查询对象是从第几条记录开始读取, 默认值为"1",
	 * 即从第一条记录.
	 */
	int getStartRow() throws EternaException, SQLException;

	/**
	 * 设置从第几条记录开始取值(从1开始).
	 *
	 * @param startRow   起始行号
	 */
	void setStartRow(int startRow) throws EternaException, SQLException;

	/**
	 * 获取该查询对象读取的最大记录数, 默认值为"-1", 表示
	 * 读取起始行之后所有的记录.
	 */
	int getMaxCount() throws EternaException, SQLException;

	/**
	 * 设置读取的最大记录数，-1表示读取起始行之后所有的记录.
	 *
	 * @param maxCount   读取的最大记录数
	 */
	void setMaxCount(int maxCount) throws EternaException, SQLException;

	/**
	 * 获取该查询对象计算总记录数的方式.
	 */
	int getTotalCountModel() throws EternaException;

	/**
	 * 设置该查询对象计算总记录数的方式. <p>
	 * 默认值为{@link #TOTAL_COUNT_MODEL_NONE}(-2)</code>.
	 *
	 * @param totalCount   总记录数.
	 *                     {@link #TOTAL_COUNT_MODEL_AUTO}(-1)将游标滚到最后获取总记录数
	 *                     {@link #TOTAL_COUNT_MODEL_NONE}(-2)不获取总记录数
	 *                     {@link #TOTAL_COUNT_MODEL_COUNT}(-3)通过执行一个统计查询获取总记录数
	 *                     0-N为直接设置总记录数
	 */
	void setTotalCountModel(int totalCount) throws EternaException;

	/**
	 * 设置该查询对象计算总记录数的方式. <p>
	 *
	 * @param totalCount  总记录数.
	 * @param info        相关信息, 只有在totalCount的值设为0-N时才有效.
	 *
	 * @see #setTotalCountModel(int)
	 * @see #TOTAL_COUNT_MODEL_AUTO
	 * @see #TOTAL_COUNT_MODEL_NONE
	 * @see #TOTAL_COUNT_MODEL_COUNT
	 */
	void setTotalCountModel(int totalCount, TotalCountInfo info) throws EternaException;

	/**
	 * 获取该查询对象计算总记录数的相关信息. <p>
	 * 只有在totalCount的值设为0-N时, 该值才有效.
	 *
	 * @see #setTotalCountModel(int, TotalCountInfo)
	 */
	TotalCountInfo getTotalCountInfo() throws EternaException;

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
	 * 获取前一次executeQuery或executeQueryHoldConnection方法的执行结果.
	 *
	 * @return  前一次的执行结果, 如果返回null表示未执行或执行出错
	 */
	ResultIterator getExecutedResult() throws EternaException;

	/**
	 * 总记录数的相关信息.
	 */
	static final class TotalCountInfo
	{
		/**
		 * 是否还有更多记录.
		 */
		public final boolean hasMoreRecord;

		/**
		 * 总记录数是否可用.
		 */
		public final boolean totalCountAvailable;

		public TotalCountInfo(boolean hasMoreRecord, boolean totalCountAvailable)
		{
			this.hasMoreRecord = hasMoreRecord;
			this.totalCountAvailable = totalCountAvailable;
		}

		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj instanceof TotalCountInfo)
			{
				TotalCountInfo other = (TotalCountInfo) obj;
				return this.hasMoreRecord == other.hasMoreRecord
						&& this.totalCountAvailable == other.totalCountAvailable;
			}
			return false;
		}

	}

}
