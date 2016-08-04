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

package self.micromagic.eterna.share;

import java.util.List;

import self.micromagic.eterna.dao.DaoLogger;
import self.micromagic.eterna.dao.Entity;
import self.micromagic.eterna.dao.Query;
import self.micromagic.eterna.dao.ResultFormat;
import self.micromagic.eterna.dao.Update;
import self.micromagic.eterna.dao.preparer.PreparerCreater;
import self.micromagic.eterna.dao.preparer.ValuePreparer;
import self.micromagic.eterna.model.Model;
import self.micromagic.eterna.model.ModelCaller;
import self.micromagic.eterna.model.ModelExport;
import self.micromagic.eterna.search.ConditionBuilder;
import self.micromagic.eterna.search.Search;
import self.micromagic.eterna.search.SearchAttributes;
import self.micromagic.eterna.search.SearchManager;
import self.micromagic.eterna.security.PermissionSet;
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
	 * 在工厂的对象中设置权限集合创建器的名称.
	 */
	String PERMISSION_SET_GENERATOR_NAME = "permissionSet.generator";

	/**
	 * 获得本eterna工厂的共享工厂.
	 */
	EternaFactory getShareFactory() throws EternaException;

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
	 * 创建一个权限集合对象.
	 *
	 * @param permission  权限配置字符串
	 */
	PermissionSet createPermissionSet(String permission) throws EternaException;

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


	//----------------------------------  dao --------------------------------------

	/**
	 * 可设置数据操作日志的最大数目.
	 */
	int MAX_DAO_LOGGER_COUNT = 10;

	/**
	 * 获得日志记录器<code>DaoLogger</code>.
	 *
	 * @param index  日志记录器对象所在的索引值
	 */
	DaoLogger getDaoLogger(int index) throws EternaException;

	/**
	 * 获得日志记录器对象所在的索引值.
	 */
	int getDaoLoggerIndex(String name) throws EternaException;

	/**
	 * 获得日志记录器对象的个数.
	 */
	int getDaoLoggerCount() throws EternaException;

	/**
	 * 获得一个常量的值.
	 *
	 * @param name       常量的名称.
	 */
	String getConstantValue(String name) throws EternaException;

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
	Query createQuery(String name) throws EternaException;

	/**
	 * 生成一个<code>Query</code>的实例.
	 *
	 * @param id         <code>Query</code>的id.
	 */
	Query createQuery(int id) throws EternaException;

	/**
	 * 生成一个<code>Update</code>的实例.
	 *
	 * @param name       <code>Update</code>的名称.
	 */
	Update createUpdate(String name) throws EternaException;

	/**
	 * 生成一个<code>Update</code>的实例.
	 *
	 * @param id         <code>Update</code>的id.
	 */
	Update createUpdate(int id) throws EternaException;

	/**
	 * 生成一个<code>PreparerCreater</code>的实例.
	 *
	 * @param name       PreparerCreater的名称.
	 */
	PreparerCreater getPrepare(String name) throws EternaException;

	/**
	 * 根据指定的类型及给出的值创建一个ValuePreparer.
	 *
	 * @param type   值的类型
	 * @param value  需要设置的值
	 */
	ValuePreparer createValuePreparer(int type, Object value)
			throws EternaException;

	//----------------------------------  search  --------------------------------------

	/**
	 * 在工厂的属性中设置查询条件的配置的键值.
	 */
	String SEARCH_ATTRIBUTES_FLAG = "search.attributes";

	/**
	 * 在工厂的对象中设置搜索管理者创建器的名称.
	 */
	String SEARCH_MANAGER_GENERATOR_NAME = "searchManager.generator";

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
	Search createSearch(String name) throws EternaException;

	/**
	 * 生成一个<code>Search</code>的实例.
	 *
	 * @param id         <code>Search</code>的id.
	 */
	Search createSearch(int id) throws EternaException;

	SearchManager createSearchManager() throws EternaException;

	SearchAttributes getSearchAttributes()
			throws EternaException;


	//----------------------------------  model  --------------------------------------

	/**
	 * 在工厂的对象中设置模块调用者的名称.
	 */
	String MODEL_CALLER_NAME = "model.caller";

	String getModelNameTag() throws EternaException;

	ModelCaller getModelCaller() throws EternaException;

	/**
	 * 获取一个model的export对象.
	 */
	ModelExport getModelExport(String exportName) throws EternaException;

	/**
	 * 生成一个<code>Model</code>的实例.
	 *
	 * @param id         <code>Model</code>的名称.
	 */
	Model createModel(String name) throws EternaException;

	/**
	 * 生成一个<code>Model</code>的实例.
	 *
	 * @param id         <code>Model</code>的id.
	 */
	Model createModel(int id) throws EternaException;

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
	 * 获得一个字符串格式转换的编码器.
	 *
	 * @param name       字符串格式转换编码器的名称.
	 */
	StringCoder getStringCoder(String name) throws EternaException;

	/**
	 * 获取一个界面中的函数对象.
	 */
	Function getFunction(String name) throws EternaException;

	/**
	 * 获取一个可在界面使用的控件对象.
	 */
	Component getTypicalComponent(String name) throws EternaException;

	/**
	 * 生成一个<code>View</code>的实例.
	 *
	 * @param id         <code>View</code>的名称.
	 */
	View createView(String name) throws EternaException;

	/**
	 * 生成一个<code>View</code>的实例.
	 *
	 * @param id         <code>View</code>的id.
	 */
	View createView(int id) throws EternaException;

	/**
	 * 获取一个可在界面或后端程序中使用的资源对象.
	 */
	Resource getResource(String name) throws EternaException;

}
