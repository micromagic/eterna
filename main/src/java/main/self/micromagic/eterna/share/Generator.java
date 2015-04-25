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

/**
 * 对象的创建者.
 * 用于创建注册到工厂中的对象.
 */
public interface Generator
{
	/**
	 * 设置对象创建者所属的工厂.
	 */
	void setFactory(Factory factory) throws EternaException;

	/**
	 * 根据名称获取一个属性.
	 */
	Object getAttribute(String name) throws EternaException;

	/**
	 * 获取对象创建者中的属性名称列表.
	 * 如果没有任何属性则返回一个空的数组.
	 */
	String[] getAttributeNames() throws EternaException;

	/**
	 * 设置一个属性.
	 */
	Object setAttribute(String name, Object value) throws EternaException;

	/**
	 * 移除一个属性.
	 */
	Object removeAttribute(String name) throws EternaException;

	/**
	 * 设置对象创建者的名称.
	 */
	void setName(String name) throws EternaException;

	/**
	 * 获取对象创建者的名称.
	 */
	String getName() throws EternaException;

	/**
	 * 创建一个对象.
	 */
	Object create() throws EternaException;

}