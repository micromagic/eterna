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

import java.util.List;

import self.micromagic.eterna.dao.preparer.ValuePreparer;
import self.micromagic.eterna.security.PermissionSet;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;

/**
 * 条件配置信息.
 */
public interface ConditionProperty
{
	/**
	 * 标识当前默认值使用环境变量的前缀.
	 */
	String DEFAULT_ENV_PREFIX = "env:";

	/**
	 * 初始化.
	 */
	void initialize(EternaFactory factory) throws EternaException;

	/**
	 * 获取条件配置信息的名称.
	 */
	String getName() throws EternaException;

	/**
	 * 获取条件相关的列名.
	 */
	String getColumnName() throws EternaException;

	/**
	 * 获取列的标题.
	 */
	String getColumnCaption() throws EternaException;

	/**
	 * 获取这个ConditionProperty对应列的数据类型.
	 */
	int getColumnType() throws EternaException;

	/**
	 * 获取这个ConditionProperty对应列的数据类型名称.
	 */
	String getColumnTypeName() throws EternaException;

	/**
	 * 通过String类型的数据构成一个ValuePreparer.
	 */
	ValuePreparer createValuePreparer(String value) throws EternaException;

	/**
	 * 通过Object类型的数据构成一个ValuePreparer.
	 */
	ValuePreparer createValuePreparer(Object value) throws EternaException;

	/**
	 * 是否ColumnType的类型为TYPE_IGNORE.
	 * 如果为true则表示忽略此Property, 可以将其删除, 这样可以在Property
	 * 继承时去掉父对象中不需要的Property.
	 */
	boolean isIgnore() throws EternaException;

	/**
	 * 该ConditionProperty是否可见.
	 */
	boolean isVisible() throws EternaException;

	/**
	 * 获取条件的输入类型.
	 */
	String getConditionInputType() throws EternaException;

	/**
	 * 获取默认值.
	 */
	Object getDefaultValue() throws EternaException;

	/**
	 * 根据名称获取属性.
	 */
	Object getAttribute(String name) throws EternaException;

	/**
	 * 获取所有属性的名称.
	 */
	String[] getAttributeNames() throws EternaException;

	/**
	 * 获取权限集合.
	 */
	PermissionSet getPermissionSet() throws EternaException;

	/**
	 * 获取条件构造器列表的名称.
	 */
	String getConditionBuilderListName() throws EternaException;

	/**
	 * 获取条件构造器列表.
	 */
	List getConditionBuilderList() throws EternaException;

	/**
	 * 是否强制使用默认的条件构造器.
	 */
	boolean isUseDefaultConditionBuilder() throws EternaException;

	/**
	 * 获取默认的条件构造器.
	 */
	ConditionBuilder getDefaultConditionBuilder() throws EternaException;

}
