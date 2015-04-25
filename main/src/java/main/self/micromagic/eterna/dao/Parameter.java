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

import self.micromagic.eterna.dao.preparer.ValuePreparer;
import self.micromagic.eterna.security.PermissionSet;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;

public interface Parameter
{
	/**
	 * 初始化本SQLParameter.
	 */
	void initialize(EternaFactory factory) throws EternaException;

	/**
	 * 获取这个SQLParameter的名称.
	 */
	String getName() throws EternaException;

	/**
	 * 获取对应的列名.
	 */
	String getColumnName() throws EternaException;

	/**
	 * 获取这个SQLParameter的数据类型.
	 */
	int getType() throws EternaException;

	/**
	 * 获取这个SQLParameter的纯数据类型.
	 */
	int getPureType() throws EternaException;

	/**
	 * 获取这个SQLParameter的参数索引值.
	 */
	int getIndex() throws EternaException;

	/**
	 * 获取这个SQLParameter的数据类型名称.
	 */
	String getTypeName() throws EternaException;

	/**
	 * 获取需要的权限集合.
	 */
	PermissionSet getPermissionSet();

	/**
	 * 根据名称获取一个属性.
	 */
	Object getAttribute(String name) throws EternaException;

	/**
	 * 获取对象中的属性名称列表.
	 * 如果没有任何属性则返回一个空的数组.
	 */
	String[] getAttributeNames() throws EternaException;

	/**
	 * 通过String类型的数据构成一个ValuePreparer.
	 */
	ValuePreparer createValuePreparer(String value) throws EternaException;

	/**
	 * 通过Object类型的数据构成一个ValuePreparer.
	 */
	ValuePreparer createValuePreparer(Object value) throws EternaException;

}