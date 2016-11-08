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

package self.micromagic.eterna.search;

import java.sql.Connection;
import java.sql.SQLException;

import self.micromagic.eterna.dao.ResultReaderManager;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;

/**
 * 数据搜索对象.
 */
public interface Search
{
	String SESSION_SEARCH_MANAGER = "ETERNA_SESSION_SEARCH_MANAGER";
	String SESSION_SEARCH_QUERY = "ETERNA_SESSION_SEARCH_QUERY";

	/**
	 * 在item的arrtibute中设置输入类型的名称.
	 */
	String INPUT_TYPE_FLAG = "inputType";

	/**
	 * 在item的arrtibute中设置查询列名的名称.
	 */
	String SEARCH_COLUMN_FLAG = "searchColumn";

	/**
	 * 在item的arrtibute中设置默认条件构造器的名称.
	 */
	String BUILDER_FLAG = "builder";
	/**
	 * 在item的arrtibute中设置默认条件构造器的名称.
	 */
	String DEFAULT_BUILDER_FLAG = "defaultBuilder";

	/**
	 * 在item的arrtibute中设置条件构造列表的名称.
	 */
	String BUILDER_LIST_FLAG = "builderList";

	/**
	 * 在factory或search的属性中设置是否要将query对象及条件对象保存在会话中.
	 */
	String SESSION_STORE_FLAG = "search.sessionStore";

	/**
	 * 在factory的属性中设置默认每页行数的属性名称. <p>
	 * 可在配置中按如下方法设置:
	 * <factory>
	 *    <attributes>
	 *       <attribute name="pageSize" value="10"/>
	 *    </attributes>
	 * </factory>
	 */
	String PAGE_SIZE_ATTRIBUTE = "search.pageSize";

	/**
	 * 用于设置显示列的配置. <p>
	 * 在调用前按如下方法设置:
	 * request.setAttribute(Search.COLUMN_SETTING, String[]);
	 *
	 * @see ResultReaderManager#setReaderList(String[])
	 */
	String COLUMN_SETTING = "search.column.settion";

	/**
	 * 在请求的属性中设置查询的参数的名称.
	 *
	 * @see SearchParam
	 */
	String ATTR_SEARCH_PARAM = "ETERNA_ATTR_SEARCH_PARAM";

	/**
	 * 用于标志是否要读取所有的记录. <p>
	 * 由于查询模块中设置了分页功能, 如果需要读取所有记录的话, 则可
	 * 在调用前按如下方法设置:
	 * request.setAttribute(Search.READ_ALL_ROW, "1");
	 * 此外, 如果需要把已设置的标志去除, 可以使用如下方法:
	 * request.removeAttribute(SearchManager.READ_ALL_ROW);
	 */
	String READ_ALL_ROW = "ETERNA_READ_ALL_ROW";

	/**
	 * 用于标志读取的记录数. <p>
	 * 由于查询模块中设置了分页功能, 在配置中设置了读取的记录数, 如果需要
	 * 设置读取的起始记录和读取的记录数，则可在调用前按如下方法设置:
	 * request.setAttribute(Search.READ_ROW_START_AND_COUNT, new StartAndCount(start, count));
	 * 此外, 如果需要把已设置的标志去除, 可以使用如下方法:
	 * request.removeAttribute(SearchManager.READ_ROW_START_AND_COUNT);
	 */
	String READ_ROW_START_AND_COUNT = "ETERNA_READ_ROW_START_AND_COUNT";

	/**
	 * 用于标志是否要读取所有的记录. <p>
	 * 由于查询模块中设置了分页功能, 所以读取的数据内容会现取出来. 但是, 对于
	 * 大量的数据的话, 采用现取出来的方式会占用大量的内存, 所以可以采用保持
	 * 连接的方式, 需要设置的话可在调用前按如下方法设置:
	 * request.setAttribute(Search.HOLD_CONNECTION, "1");
	 * 此外, 如果需要把已设置的标志去除, 可以使用如下方法:
	 * request.removeAttribute(SearchManager.HOLD_CONNECTION);
	 */
	String HOLD_CONNECTION = "ETERNA_HODE_CONNECTION";

	/**
	 * 不需要使用query对象时, 在query属性中设置的值.
	 */
	String NONE_QUERY_NAME = "$none";

	String getName() throws EternaException;

	EternaFactory getFactory() throws EternaException;

	Object getAttribute(String name) throws EternaException;

	String[] getAttributeNames() throws EternaException;

	String getOtherSearchManagerName() throws EternaException;

	/**
	 * 获取其它辅助设置条件及参数的search, 用于分布式条件查询的时候使用.
	 */
	Search[] getOtherSearchs() throws EternaException;

	/**
	 * 是否是特殊的条件, 需要重新构造条件子语句..
	 */
	boolean isSpecialCondition() throws EternaException;

	/**
	 * 判断是否需要在条件外面带上括号"(", ")".
	 */
	boolean isNeedWrap() throws EternaException;

	/**
	 * 获得ColumnSetting的类型, 用于区分读取哪个ColumnSetting.
	 */
	String getColumnSettingType() throws EternaException;

	/**
	 * 获得设置的ColumnSetting, SearchAdapter将用它来设置查询的列.
	 */
	ColumnSetting getColumnSetting() throws EternaException;

	/**
	 * 获得绑定的参数设置器<code>ParameterSetting</code>.
	 *
	 * @return  如果未绑定则返回null, 如果已绑定则返回参数设置器
	 */
	ParameterSetting getParameterSetting() throws EternaException;

	String getConditionPropertyOrder() throws EternaException;

	int getConditionPropertyCount() throws EternaException;

	ConditionProperty getConditionProperty(int colId) throws EternaException;

	ConditionProperty getConditionProperty(String name) throws EternaException;

	int getConditionIndex() throws EternaException;

	int getPageSize() throws EternaException;

	String getSearchManagerName() throws EternaException;

	/**
	 * 获得一个SearchManager.
	 *
	 * @param data   数据, 里面包含了request的parameter, request的attribute,
	 *               session的attritute
	 */
	SearchManager getSearchManager(AppData data) throws EternaException;

	/**
	 * 执行查询, 并获得结果.
	 *
	 * @param data   数据, 里面包含了request的parameter, request的attribute,
	 *               session的attritute
	 * @param conn   数据库连接
	 */
	SearchResult doSearch(AppData data, Connection conn) throws EternaException, SQLException;

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