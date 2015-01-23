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

package self.micromagic.eterna.share;

import java.util.List;

import self.micromagic.eterna.base.Entity;
import self.micromagic.eterna.base.Query;
import self.micromagic.eterna.base.ResultFormat;
import self.micromagic.eterna.base.SpecialLog;
import self.micromagic.eterna.base.Update;
import self.micromagic.eterna.base.preparer.PreparerCreater;
import self.micromagic.eterna.model.ModelAdapter;
import self.micromagic.eterna.model.ModelCaller;
import self.micromagic.eterna.model.ModelExport;
import self.micromagic.eterna.search.ConditionBuilder;
import self.micromagic.eterna.search.Search;
import self.micromagic.eterna.search.SearchAttributes;
import self.micromagic.eterna.search.SearchManager;
import self.micromagic.eterna.search.SearchManagerGenerator;
import self.micromagic.eterna.security.UserManager;
import self.micromagic.eterna.view.Component;
import self.micromagic.eterna.view.DataPrinter;
import self.micromagic.eterna.view.Function;
import self.micromagic.eterna.view.Resource;
import self.micromagic.eterna.view.StringCoder;
import self.micromagic.eterna.view.View;

/**
 * eterna框架的工厂.
 */
public interface EternaFactory extends Factory
{
	/**
	 * 获得本eterna工厂的共享工厂.
	 */
	EternaFactory getShareFactory() throws EternaException;

	/**
	 * 根据给出的编号创建对象.
	 */
	Object createObject(int id) throws EternaException;

	/**
	 * 查询已注册的对象的编号.
	 */
	int findObjectId(Object key) throws EternaException;

	/**
	 * 注销一个已注册的对象.
	 */
	void deregisterObject(Object key) throws EternaException;

	/**
	 * 获得一个UserManager对象.
	 *
	 * @return  如果UserManager对象未设置的话, 则返回null.
	 */
	UserManager getUserManager() throws EternaException;

	/**
	 * 设置一个UserManager对象.
	 */
	void setUserManager(UserManager um) throws EternaException;

	/**
	 * 获得一个DataSourceManager对象.
	 *
	 * @return  如果DataSourceManager对象未设置的话, 则返回null.
	 */
	DataSourceManager getDataSourceManager() throws EternaException;

	/**
	 * 设置一个UserManager对象.
	 */
	void setDataSourceManager(DataSourceManager dsm) throws EternaException;


	//----------------------------------  db --------------------------------------

	/**
	 * 获得日志记录器<code>SpecialLog</code>.
	 */
	SpecialLog getSpecialLog() throws EternaException;

	/**
	 * 设置日志记录器<code>SpecialLog</code>.
	 */
	void setSpecialLog(SpecialLog sl)throws EternaException;

	/**
	 * 获得一个常量的值.
	 *
	 * @param name       常量的名称.
	 */
	String getConstantValue(String name) throws EternaException;

	/**
	 * 添加一个常量的值.
	 *
	 * @param name       常量的名称.
	 * @param value      常量的值.
	 */
	void addConstantValue(String name, String value) throws EternaException;

	/**
	 * 获得一个实体对象.
	 *
	 * @param name       实体的名称.
	 */
	Entity getEntity(String name) throws EternaException;

	/**
	 * 获得一个ResultFormat类, 用于格式化查询的结果.
	 *
	 * @param name       format的名称.
	 */
	ResultFormat getFormat(String name) throws EternaException;

	/**
	 * 生成一个<code>Query</code>的实例.
	 *
	 * @param name       <code>Query</code>的名称.
	 */
	Query createQueryAdapter(String name) throws EternaException;

	/**
	 * 生成一个<code>Query</code>的实例.
	 *
	 * @param id         <code>Query</code>的id.
	 */
	Query createQueryAdapter(int id) throws EternaException;

	/**
	 * 生成一个<code>Update</code>的实例.
	 *
	 * @param name       <code>Update</code>的名称.
	 */
	Update createUpdateAdapter(String name) throws EternaException;

	/**
	 * 生成一个<code>Update</code>的实例.
	 *
	 * @param id         <code>Update</code>的id.
	 */
	Update createUpdateAdapter(int id) throws EternaException;

	/**
	 * 生成一个<code>PreparerCreater</code>的实例.
	 *
	 * @param name       PreparerCreater的名称.
	 */
	PreparerCreater getPrepare(String name)
			throws EternaException;


	//----------------------------------  search  --------------------------------------

	/**
	 * 在工厂的属性中设置查询条件的配置的键值.
	 */
	String SEARCH_ATTRIBUTES_FLAG = "search.attributes";

	/**
	 * 获得一个ConditionBuilder类, 用于构成一个查询条件.
	 *
	 * @param name       ConditionBuilder的名称.
	 */
	ConditionBuilder getConditionBuilder(String name) throws EternaException;

	/**
	 * 获得一个ConditionBuilder的列表.
	 * 在ConditionProperty中会用的该列表, 用于确定该条件的可选操作的范围.
	 *
	 * @param name       ConditionBuilder列表的名称.
	 */
	List getConditionBuilderList(String name) throws EternaException;

	/**
	 * 生成一个<code>Search</code>的实例.
	 *
	 * @param id         <code>Search</code>的名称.
	 */
	Search createSearchAdapter(String name) throws EternaException;

	/**
	 * 生成一个<code>Search</code>的实例.
	 *
	 * @param id         <code>Search</code>的id.
	 */
	Search createSearchAdapter(int id) throws EternaException;

	void registerSearchManager(SearchManagerGenerator generator)
			throws EternaException;

	SearchManager createSearchManager() throws EternaException;

	SearchAttributes getSearchAttributes()
			throws EternaException;


	//----------------------------------  model  --------------------------------------

	/**
	 * 在factory的属性中设置存放model名称的标签的属性名称.
	 */
	String MODEL_NAME_TAG_FLAG = "model.name.tag";

	String getModelNameTag() throws EternaException;

	ModelCaller getModelCaller() throws EternaException;

	void setModelCaller(ModelCaller mc)throws EternaException;

	/**
	 * 获取一个model的export对象.
	 */
	ModelExport getModelExport(String exportName) throws EternaException;

	/**
	 * 生成一个<code>Model</code>的实例.
	 *
	 * @param id         <code>Model</code>的名称.
	 */
	ModelAdapter createModelAdapter(String name) throws EternaException;

	/**
	 * 生成一个<code>Model</code>的实例.
	 *
	 * @param id         <code>Model</code>的id.
	 */
	ModelAdapter createModelAdapter(int id) throws EternaException;

	//----------------------------------  view  --------------------------------------

	/**
	 * 在factory的属性中设置视图全局配置的属性名称.
	 */
	String VIEW_GLOBAL_SETTING_FLAG = "view.global.setting";

	String getViewGlobalSetting() throws EternaException;

	/**
	 * 获得一个数据集输出器.
	 *
	 * @param name       数据集输出器的名称.
	 */
	DataPrinter getDataPrinter(String name) throws EternaException;

	/**
	 * 获取一个界面中的函数对象.
	 */
	Function getFunction(String name) throws EternaException;

	/**
	 * 获取一个可在界面使用的控件对象.
	 */
	Component getTypicalComponent(String name) throws EternaException;

	StringCoder getStringCoder() throws EternaException;

	void setStringCoder(StringCoder sc)throws EternaException;

	/**
	 * 生成一个<code>View</code>的实例.
	 *
	 * @param id         <code>View</code>的名称.
	 */
	View createViewAdapter(String name) throws EternaException;

	/**
	 * 生成一个<code>View</code>的实例.
	 *
	 * @param id         <code>View</code>的id.
	 */
	View createViewAdapter(int id) throws EternaException;

	/**
	 * 获取一个可在界面或后端程序中使用的资源对象.
	 */
	Resource getResource(String name) throws EternaException;

}