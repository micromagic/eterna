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

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;

/**
 * 对一个实体对象的引用.
 */
public interface EntityRef
{
	/**
	 * 获取引用的实体对象名称.
	 */
	String getEntityName() throws EternaException;

	/**
	 * 获取轻易的实体对象.
	 *
	 * @param factory  获取实体对象所需要的工厂.
	 */
	Entity getEntity(EternaFactory factory) throws EternaException;

	/**
	 * 获取需要包含的实体中的元素列表.
	 */
	String getInclude() throws EternaException;

	/**
	 * 获取需要排除的实体中的元素列表.
	 */
	String getExclude() throws EternaException;

	/**
	 * 是否忽略已有的同名元素.
	 */
	boolean isIgnoreSame() throws EternaException;

	/**
	 * 实体对应数据库的表别名.
	 */
	String getTableAlias() throws EternaException;

}