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

package self.micromagic.eterna.search;

import java.io.Reader;
import java.io.Writer;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.security.Permission;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.sql.ResultIterator;
import self.micromagic.eterna.view.DataPrinter;

/**
 * @author micromagic@sina.com
 */
public interface SearchAdapter
{
	String SESSION_SEARCH_MANAGER = "ETERNA_SESSION_SEARCH_MANAGER";
	String SESSION_SEARCH_QUERY = "ETERNA_SESSION_SEARCH_QUERY";

	/**
	 * 设置单列排序的参数名后缀. <p>
	 * 参数格式为: [searchName].order
	 * 值为需要排序的reader名称.
	 */
	String SINGLE_ORDER_SUFIX = ".order";

	/**
	 * 设置单列排序的类型. <p>
	 * 参数格式为: [searchName].orderType
	 * 值可以分别为 0 自动切换升序及降序, 1 升序, -1 降序
	 */
	String SINGLE_ORDER_TYPE = ".orderType";

	/**
	 * 设置默认每页行数的属性名称. <p>
	 * 可在配置中按如下方法设置:
	 * <search>
	 *    <attributes>
	 *       <attribute name="SearchAdapter.Attribute.pageSize" value="10"/>
	 *    </attributes>
	 * </search>
	 */
	String PAGE_SIZE_ATTRIBUTE = "SearchAdapter.Attribute.pageSize";

	/**
	 * 用于标志是否要强制读取列设置. <p>
	 * 由于列设置的信息会做缓存, 所以需要强制读取列设置信息的话, 则可
	 * 在调用前按如下方法设置:
	 * request.setAttribute(SearchAdapter.FORCE_LOAD_COLUMN_SETTING, "1");
	 * 此外, 如果需要把已设置的标志去除, 可以使用如下方法:
	 * request.removeAttribute(SearchManager.FORCE_LOAD_COLUMN_SETTING);
	 *
	 * 注: 使用此标志强制读取的列设置不会被缓存, 也就是说下次强制读取表示未
	 *     设置的话, 读取的列设置仍然是前一次缓存的值
	 */
	String FORCE_LOAD_COLUMN_SETTING = "ETERNA_FORCE_LOAD_COLUMN_SETTING";

	/**
	 * 用于标志是否要读取所有的记录. <p>
	 * 由于查询模块中设置了分页功能, 如果需要读取所有记录的话, 则可
	 * 在调用前按如下方法设置:
	 * request.setAttribute(SearchAdapter.READ_ALL_ROW, "1");
	 * 此外, 如果需要把已设置的标志去除, 可以使用如下方法:
	 * request.removeAttribute(SearchManager.READ_ALL_ROW);
	 */
	String READ_ALL_ROW = "ETERNA_READ_ALL_ROW";

	/**
	 * 用于标志读取的记录数. <p>
	 * 由于查询模块中设置了分页功能, 在配置中设置了读取的记录数, 如果需要
	 * 设置读取的起始记录和读取的记录数，则可在调用前按如下方法设置:
	 * request.setAttribute(SearchAdapter.READ_ROW_START_AND_COUNT, new StartAndCount(start, count));
	 * 此外, 如果需要把已设置的标志去除, 可以使用如下方法:
	 * request.removeAttribute(SearchManager.READ_ROW_START_AND_COUNT);
	 */
	String READ_ROW_START_AND_COUNT = "ETERNA_READ_ROW_START_AND_COUNT";

	/**
	 * 用于标志是否要读取所有的记录. <p>
	 * 由于查询模块中设置了分页功能, 所以读取的数据内容会现取出来. 但是, 对于
	 * 大量的数据的话, 采用现取出来的方式会占用大量的内存, 所以可以采用保持
	 * 连接的方式, 需要设置的话可在调用前按如下方法设置:
	 * request.setAttribute(SearchAdapter.HOLD_CONNECTION, "1");
	 * 此外, 如果需要把已设置的标志去除, 可以使用如下方法:
	 * request.removeAttribute(SearchManager.HOLD_CONNECTION);
	 */
	String HOLD_CONNECTION = "ETERNA_HODE_CONNECTION";

	String getName() throws ConfigurationException;

	EternaFactory getFactory() throws ConfigurationException;

	Object getAttribute(String name) throws ConfigurationException;

	String[] getAttributeNames() throws ConfigurationException;

	String getOtherSearchManagerName() throws ConfigurationException;

	/**
	 * 获取其它辅助设置条件及参数的search, 用于分布式条件查询的时候使用.
	 */
	SearchAdapter[] getOtherSearchs() throws ConfigurationException;

	String getConditionPropertyOrderWithOther() throws ConfigurationException;

	/**
	 * 获得关于这个查询的相关条件说明的XML文档.
	 */
	Reader getConditionDocument(Permission permission) throws ConfigurationException;

	/**
	 * 是否是特殊的条件, 需要重新构造条件子语句..
	 */
	boolean isSpecialCondition() throws ConfigurationException;

	/**
	 * 判断是否需要在条件外面带上括号"(", ")".
	 */
	boolean isNeedWrap() throws ConfigurationException;

	/**
	 * 获得ColumnSetting的类型, 用于区分读取哪个ColumnSetting.
	 */
	String getColumnSettingType() throws ConfigurationException;

	/**
	 * 获得设置的ColumnSetting, SearchAdapter将用它来设置查询的列.
	 */
	ColumnSetting getColumnSetting() throws ConfigurationException;

	/**
	 * 获得绑定的参数设置器<code>ParameterSetting</code>.
	 *
	 * @return  如果未绑定则返回null, 如果已绑定则返回参数设置器
	 */
	ParameterSetting getParameterSetting() throws ConfigurationException;

	String getParentConditionPropretyName() throws ConfigurationException;

	String getConditionPropertyOrder() throws ConfigurationException;

	int getConditionPropertyCount() throws ConfigurationException;

	ConditionProperty getConditionProperty(int colId) throws ConfigurationException;

	ConditionProperty getConditionProperty(String name) throws ConfigurationException;

	int getConditionIndex() throws ConfigurationException;

	int getPageSize() throws ConfigurationException;

	String getSearchManagerName() throws ConfigurationException;

	/**
	 * 获得一个SearchManager.
	 *
	 * @param data   数据, 里面包含了request的parameter, request的attribute,
	 *               session的attritute
	 */
	SearchManager getSearchManager(AppData data) throws ConfigurationException;

	/**
	 * 执行查询, 并获得结果.
	 *
	 * @param data   数据, 里面包含了request的parameter, request的attribute,
	 *               session的attritute
	 * @param conn   数据库连接
	 */
	Result doSearch(AppData data, Connection conn) throws ConfigurationException, SQLException;

	/**
	 * 搜索的结果.
	 */
	static final class Result
			implements DataPrinter.BeanPrinter
	{
		/**
		 * 分页的尺寸.
		 */
		public final int pageSize;

		/**
		 * 当前的页数.
		 * 从0开始, 第一页为0 第二页为1 ...
		 */
		public final int pageNum;

		/**
		 * 使用的search对象的名称.
		 */
		public final String searchName;

		/**
		 * 使用的query对象的名称.
		 */
		public final String queryName;

		/**
		 * 搜索的结果集.
		 */
		public final ResultIterator queryResult;

		/**
		 * 搜索统计的结果集.
		 */
		public final ResultIterator searchCount;

		/**
		 * 设置的单列排序的reader名称.
		 */
		public final String singleOrderName;

		/**
		 * 单列排序是否为降序.
		 */
		public final boolean singleOrderDesc;

		public Result(String searchName, String queryName, ResultIterator queryResult, ResultIterator searchCount,
				int pageSize, int pageNum, String singleOrderName, boolean singleOrderDesc)
		{
			this.pageSize = pageSize;
			this.pageNum = pageNum;
			this.searchName = searchName;
			this.queryName = queryName;
			this.queryResult = queryResult;
			this.searchCount = searchCount;
			this.singleOrderName = singleOrderName;
			this.singleOrderDesc = singleOrderDesc;
		}

		public Result(Result old, String queryName, ResultIterator queryResult)
		{
			this.pageSize = old.pageSize;
			this.pageNum = old.pageNum;
			this.searchName = old.searchName;
			this.queryName = queryName;
			this.queryResult = queryResult;
			this.searchCount = old.searchCount;
			this.singleOrderName = old.singleOrderName;
			this.singleOrderDesc = old.singleOrderDesc;
		}

		public void print(DataPrinter p, Writer out, Object bean)
				throws IOException, ConfigurationException
		{
			try
			{
				p.printObjectBegin(out);
				p.printResultIterator(out, this.queryResult);
				p.printPairWithoutCheck(out, "pageNum", this.pageNum, false);
				p.printPairWithoutCheck(out, "pageSize", this.pageSize, false);
				p.printPairWithoutCheck(out, "searchName", this.searchName, false);
				if (this.queryResult.isRealRecordCountAvailable())
				{
					p.printPairWithoutCheck(out, "totalCount", this.queryResult.getRealRecordCount(), false);
				}
				if (this.singleOrderName != null)
				{
					p.printPairWithoutCheck(out, "orderName", this.singleOrderName, false);
					p.printPairWithoutCheck(out, "orderDesc", this.singleOrderDesc ? 1 : 0, false);
				}
				p.printPairWithoutCheck(out, "hasNextPage", this.queryResult.isHasMoreRecord() ? 1 : 0, false);
				p.printObjectEnd(out);
			}
			catch (SQLException ex)
			{
				throw new ConfigurationException(ex);
			}
		}

	}

	/**
	 * 查询的起始值及获取的记录数.
	 */
	static final class StartAndCount
	{
		/**
		 * 查询的起始值.
		 */
		public final int start;

		/**
		 * 获取的记录数.
		 */
		public final int count;

		public StartAndCount(int start, int count)
		{
			this.start = start;
			this.count = count;
		}

	}

}