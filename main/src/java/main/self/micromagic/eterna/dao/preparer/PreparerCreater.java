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

package self.micromagic.eterna.dao.preparer;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.EternaObject;

/**
 * 值准备器创建者.
 */
public interface PreparerCreater extends EternaObject
{
	/**
	 * 获取生成值准备器创建者的工厂.
	 */
	EternaFactory getFactory() throws EternaException;

	/**
	 * 获取值准备器创建者的名称.
	 */
	String getName();

	/**
	 * 设置preparer的模式.
	 */
	void setPattern(String pattern) throws EternaException;

	/**
	 * 获得一个配置的属性.
	 *
	 * @param name  属性的名称
	 * @return  属性的值
	 */
	Object getAttribute(String name) throws EternaException;

	/**
	 * 将一个Object类型的值转换成所需要的类型.
	 *
	 * @param value  值
	 * @return  转换后的值
	 */
	Object convertValue(Object value) throws EternaException;

	/**
	 * 将一个String类型的值转换成所需要的类型.
	 *
	 * @param value  值
	 * @return  转换后的值
	 */
	Object convertValue(String value) throws EternaException;

	/**
	 * 根据一个Object类型的值创建一个值准备器.
	 *
	 * @param value  值
	 * @return  值准备器
	 */
	ValuePreparer createPreparer(Object value) throws EternaException;

	/**
	 * 根据一个String类型的值创建一个值准备器.
	 *
	 * @param value  值
	 * @return  值准备器
	 */
	ValuePreparer createPreparer(String value) throws EternaException;

}
