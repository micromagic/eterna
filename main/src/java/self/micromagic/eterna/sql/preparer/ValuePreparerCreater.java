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

package self.micromagic.eterna.sql.preparer;

import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.EternaException;

/**
 * 值准备器创建者.
 */
public interface ValuePreparerCreater
{
	/**
	 * 设置默认使用的ValuePreparerCreater.
	 * 这是一个factory的arrtibute, 它的值是所定义的vpc的名称.
	 * 如果没有指定, factory会自动生成一个默认的.
	 */
	public static final String DEFAULT_VPC_ATTRIBUTE = "default.vpc.name";

	/**
	 * 在vpc或factory的属性中设置, 是否要将空字符串变为null, 默认值为true.
	 * 如果vpc中设置了值, 则忽略factory中的设置.
	 */
	public static final String EMPTY_STRING_TO_NULL = "sql.emptyStringToNull";

	/**
	 * 获取生成值准备器创建者的工厂.
	 */
	EternaFactory getFactory() throws EternaException;

	/**
	 * 获得一个配置的属性.
	 *
	 * @param name    属性的名称
	 * @return        属性的值
	 */
	Object getAttribute(String name) throws EternaException;

	/**
	 * 是否要将空字符串变为null.
	 */
	boolean isEmptyStringToNull();

	/**
	 * 根据一个Object类型的值创建一个值准备器.
	 *
	 * @param value    值
	 * @return      值准备器
	 */
	ValuePreparer createPreparer(Object value) throws EternaException;

	/**
	 * 根据一个String类型的值创建一个值准备器.
	 *
	 * @param value    值
	 * @return      值准备器
	 */
	ValuePreparer createPreparer(String value) throws EternaException;

}