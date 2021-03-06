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

import java.util.Iterator;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.EternaObject;

/**
 * 一个实体对象.
 */
public interface Entity extends EternaObject
{
	/**
	 * 实体的类型名称.
	 */
	String TYPE = "Entity";

	/**
	 * 初始化本实体对象, 系统会在初始化时调用此方法.
	 */
	boolean initialize(EternaFactory factory) throws EternaException;

	/**
	 * 获取本实体对象的名称.
	 */
	String getName() throws EternaException;

	/**
	 * 获取生成本实体对象的工厂.
	 */
	EternaFactory getFactory() throws EternaException;

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
	 * 获得实体对象元素的排序配置.
	 */
	String getOrder() throws EternaException;

	/**
	 * 获得实体对象中的元素总个数.
	 */
	int getItemCount() throws EternaException;

	/**
	 * 通过名称获取一个元素.
	 */
	EntityItem getItem(String name) throws EternaException;

	/**
	 * 通过索引值获取一个元素.
	 * 第一个元素是0, 第二个是1 ...
	 */
	EntityItem getItem(int index) throws EternaException;

	/**
	 * 以迭代器的方式获取元素的列表.
	 */
	Iterator getItemIterator() throws EternaException;

}