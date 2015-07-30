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

import self.micromagic.eterna.security.PermissionSet;
import self.micromagic.eterna.share.EternaException;

/**
 * 实体中的元素对象.
 */
public interface EntityItem
{
	/**
	 * 在属性中设置是否忽略父元素中的属性.
	 */
	String IGNORE_PARENT = "ignoreParent";

	/**
	 * 初始化本元素对象, 系统会在初始化时调用此方法.
	 */
	void initialize(Entity entity) throws EternaException;

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
	 * 获取元素所属的实体.
	 */
	Entity getEntity() throws EternaException;

	/**
	 * 获取元素的名称.
	 */
	String getName() throws EternaException;

	/**
	 * 获取元素对应的列名.
	 */
	String getColumnName() throws EternaException;

	/**
	 * 获取元素的类型.
	 */
	int getType() throws EternaException;

	/**
	 * 获取元素的标题.
	 */
	String getCaption() throws EternaException;

	/**
	 * 获取元素的权限集合, 只有拥有集合中的任意一个权限就可以访问该元素. <p>
	 * 如果没有设置权限集合, 则返回null, 表示访问该元素不需要权限.
	 */
	PermissionSet getPermissionSet() throws EternaException;

	/**
	 * 将另一个元素的属性合并到当前元素中.
	 */
	void merge(EntityItem other) throws EternaException;

}