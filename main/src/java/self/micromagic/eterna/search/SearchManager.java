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

import java.util.List;
import java.io.Writer;
import java.io.IOException;

import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.sql.preparer.PreparerManager;
import self.micromagic.eterna.view.DataPrinter;

public interface SearchManager
{
	/**
	 * 用于标志是否要强制清空所有的条件. <p>
	 * 由于是否要清空或重置条件是根据request中的[queryTypeTag]的
	 * 值来确定的, 如果需要强制清空, 则可在调用前按如下方法设置:
	 * request.setAttribute(SearchManager.FORCE_CLEAR_CONDITION, "1");
	 * 此外, 如果需要把已设置的标志去除, 可以使用如下方法:
	 * request.removeAttribute(SearchManager.FORCE_CLEAR_CONDITION);
	 */
	static final String FORCE_CLEAR_CONDITION = "ETERNA_FORCE_CLEAR_CONDITION";

	/**
	 * 用于标志是否要强制处理request中的条件. <p>
	 * 由于为了效率, 处理request前会现判断此次的request是否与前
	 * 一次相同, 相同的话则会不作处理. 如果需要强制处理request,
	 * 则可在调用前按如下方法设置:
	 * request.setAttribute(SearchManager.FORCE_DEAL_CONDITION, "1");
	 * 此外, 如果需要把已设置的标志去除, 可以使用如下方法:
	 * request.removeAttribute(SearchManager.FORCE_DEAL_CONDITION);
	 */
	static final String FORCE_DEAL_CONDITION = "ETERNA_FORCE_DEAL_CONDITION";

	/**
	 * 用于标志是否要将所有的Condition保存下来, 以便使用. <p>
	 * 由于保存Condition需要不少开销, 所以默认情况是不保存.
	 * 如果需要保存Condition, 则可在调用前按如下方法设置:
	 * request.setAttribute(SearchManager.SAVE_CONDITION, "1");
	 * 此外, 如果需要把已设置的标志去除, 可以使用如下方法:
	 * request.removeAttribute(SearchManager.SAVE_CONDITION);
	 */
	static final String SAVE_CONDITION = "ETERNA_SAVE_CONDITION";

	/**
	 * 标识是否是使用数据集中的指定值来作为条件的默认值.
	 */
	static final String DATA_DEFAULT_VALUE_PREFIX = "$data.";

	/**
	 * 分页时单页可显示的最大记录数.
	 */
	static final int MAX_PAGE_SIZE = 1024;

	/**
	 * 默认的查询相关的配置属性.
	 */
	static final Attributes DEFAULT_PROPERTIES = new Attributes(
			null, null, null, null, null, null);

	/**
	 * 获得当前所在的页号.
	 */
	int getPageNum();

	/**
	 * 获得当前页面显示的行数.
	 */
	int getPageSize(int defaultSize);

	/**
	 * 是否存在查询标志.
	 */
	boolean hasQueryType(AppData data) throws ConfigurationException;

	/**
	 * 获得当前的条件版本, 每更新一次条件版本自动增1, 起始版本号为1.
	 */
	int getConditionVersion() throws ConfigurationException;

	/**
	 * 根据request中的信息, 设置条件和页号.
	 */
	void setPageNumAndCondition(AppData data, SearchAdapter search)
			throws ConfigurationException;

	/**
	 * 获取整理出来的PreparerManager.
	 */
	PreparerManager getPreparerManager();

	/**
	 * 获取整理出来的PreparerManager子集.
	 */
	PreparerManager getSpecialPreparerManager(SearchAdapter search)
			throws ConfigurationException;

	/**
	 * 获取整理出来做为条件的sql子句.
	 */
	String getConditionPart();

	/**
	 * 获取整理出来做为条件的sql子句.
	 *
	 * @param needWrap   是否需要在条件外面带上括号"(", ")".
	 */
	String getConditionPart(boolean needWrap);

	/**
	 * 获取整理出来做为条件的sql子句的子集.
	 */
	String getSpecialConditionPart(SearchAdapter search) throws ConfigurationException;

	/**
	 * 获取整理出来做为条件的sql子句的子集.
	 *
	 * @param needWrap   是否需要在条件外面带上括号"(", ")".
	 */
	String getSpecialConditionPart(SearchAdapter search, boolean needWrap) throws ConfigurationException;

	/**
	 * 获取本SearchManager的配置属性.
	 */
	Attributes getAttributes();

	/**
	 * 设置本SearchManager的配置属性.
	 */
	void setAttributes(Attributes attributes);

	/**
	 * 根据条件的名称获取某个构造好的条件.
	 * 注: 这里只获取该名称下的第一个条件.
	 */
	Condition getCondition(String name);

	/**
	 * 根据条件的名称获取该名称下所有构造好的条件.
	 */
	List getConditions(String name);

	/**
	 * 获取所有构造好的条件.
	 */
	List getConditions();

	/**
	 * 保存的查询条件单元.
	 */
	static final class Condition
	{
		/**
		 * 条件的名词
		 */
		public final String name;

		/**
		 * 条件所属条件组的名称
		 */
		public final String group;

		/**
		 * 设置在该条件上的值
		 */
		public final String value;

		/**
		 * 构成该条件所要使用的ConditionBuilder
		 */
		public final ConditionBuilder builder;

		public Condition(String name, String group, String value, ConditionBuilder builder)
		{
			this.name = name;
			this.group = group;
			this.value = value;
			this.builder = builder;
		}

	}

	/**
	 * 查询相关的配置属性.
	 */
	static final class Attributes
			implements DataPrinter.BeanPrinter
	{
		/**
		 * 存储页号的控件名称
		 */
		public final String pageNumTag;

		/**
		 * 存储每页显示个数的控件名称
		 */
		public final String pageSizeTag;

		/**
		 * 存储XML格式查询条件的控件名称
		 */
		public final String querySettingTag;

		/**
		 * 存储查询方式的控件名称
		 */
		public final String queryTypeTag;

		/**
		 * 清除条件的查询方式
		 */
		public final String queryTypeClear;

		/**
		 * 设置条件的查询方式
		 */
		public final String queryTypeReset;

		public Attributes(String pageNumTag, String pageSizeTag, String querySettingTag,
				String queryTypeTag, String queryTypeClear, String queryTypeReset)
		{
			this.pageNumTag = pageNumTag == null ? "pageNum" : pageNumTag;
			this.pageSizeTag = pageSizeTag == null ? "pageSize" : pageSizeTag;
			this.querySettingTag = querySettingTag == null ? "querySetting" : querySettingTag;
			this.queryTypeTag = queryTypeTag == null ? "queryType" : queryTypeTag;
			this.queryTypeClear = queryTypeClear == null ? "clear" : queryTypeClear;
			this.queryTypeReset = queryTypeReset == null ? "set" : queryTypeReset;
		}

		public void print(DataPrinter p, Writer out, Object bean)
				throws IOException, ConfigurationException
		{
			p.printObjectBegin(out);
			p.printPairWithoutCheck(out, "pageNumTag", this.pageNumTag, true);
			p.printPairWithoutCheck(out, "pageSizeTag", this.pageSizeTag, false);
			p.printPairWithoutCheck(out, "querySettingTag", this.querySettingTag, false);
			p.printPairWithoutCheck(out, "queryTypeClear", this.queryTypeClear, false);
			p.printPairWithoutCheck(out, "queryTypeReset", this.queryTypeReset, false);
			p.printPairWithoutCheck(out, "queryTypeTag", this.queryTypeTag, false);
			p.printObjectEnd(out);
		}

	}

}