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

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.AdapterGenerator;

/**
 * @author micromagic@sina.com
 */
public interface SearchAdapterGenerator extends AdapterGenerator
{
	public static final String NONE_QUERY_NAME = "$none";

	void setName(String name) throws EternaException;

	String getName() throws EternaException;

	SearchAdapter createSearchAdapter() throws EternaException;

	void setQueryName(String queryName) throws EternaException;

	/**
	 * 获得本search使用的query的名称.
	 */
	String getQueryName() throws EternaException;

	/**
	 * 设置一个页面可以显示的记录条数.
	 */
	void setPageSize(int pageSize);

	/**
	 * 设置是否是特殊的条件, 需要重新构造的条件子语句.
	 */
	void setSpecialCondition(boolean special) throws EternaException;

	/**
	 * 设置计算总记录数的方式. <p>
	 * 分别为auto, count, none. 默认值为: auto.
	 * 另外, 还可以按search:[searchName],[readerName]的格式设置用于计算总记录数的search.
	 */
	void setCountType(String countType) throws EternaException;

	/**
	 * 设置是否需要在条件外面带上括号"(", ")".
	 */
	void setNeedWrap(boolean needWrap) throws EternaException;

	/**
	 * 设置在Session中存放SearchManager的名称.
	 */
	void setSearchManagerName(String name) throws EternaException;

	void setConditionIndex(int index) throws EternaException;

	void setOtherSearchManagerName(String otherName) throws EternaException;

	void setConditionPropertyOrderWithOther(String order) throws EternaException;

	void setParentConditionPropretyName(String parentName) throws EternaException;

	void setConditionPropertyOrder(String order) throws EternaException;

	void clearConditionPropertys() throws EternaException;

	void addConditionProperty(ConditionProperty cp) throws EternaException;

	/**
	 * 设置一个ColumnSetting的类型, 用于区分读取哪个ColumnSetting.
	 */
	void setColumnSettingType(String type) throws EternaException;

	/**
	 * 设置一个ColumnSetting, SearchAdapter将用它来设置查询的列.
	 */
	void setColumnSetting(ColumnSetting setting) throws EternaException;

	/**
	 * 设置一个ParameterSetting, SearchAdapter将用它来设置查询参数.
	 */
	void setParameterSetting(ParameterSetting setting)  throws EternaException;

}