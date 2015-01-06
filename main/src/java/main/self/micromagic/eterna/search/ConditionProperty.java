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

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.security.PermissionSet;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.sql.preparer.ValuePreparer;

/**
 * @author micromagic@sina.com
 */
public interface ConditionProperty
{
	void initialize(EternaFactory factory) throws EternaException;

	String getName() throws EternaException;

	String getColumnName() throws EternaException;

	String getColumnCaption() throws EternaException;

	/**
	 * 获取这个ConditionProperty对应列的数据类型.
	 */
	int getColumnType() throws EternaException;

	/**
	 * 获取这个ConditionProperty对应列的纯数据类型.
	 */
	int getColumnPureType() throws EternaException;

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

	String getConditionInputType() throws EternaException;

	String getDefaultValue() throws EternaException;

	String getAttribute(String name) throws EternaException;

	String[] getAttributeNames() throws EternaException;

	PermissionSet getPermissionSet() throws EternaException;

	String getConditionBuilderListName() throws EternaException;

	List getConditionBuilderList() throws EternaException;

	boolean isUseDefaultConditionBuilder() throws EternaException;

	ConditionBuilder getDefaultConditionBuilder() throws EternaException;

}