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

package self.micromagic.eterna.sql;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.Generator;

public interface ResultFormatGenerator extends Generator
{
	/**
	 * 设置这个format的名称.
	 */
	void setName(String name) throws EternaException;

	/**
	 * 获取这个format的名称.
	 */
	String getName() throws EternaException;

	/**
	 * 设置这个format要格式化的对象的类型.
	 */
	void setType(String type) throws EternaException;

	/**
	 * 设置格式化输出的模板.
	 * 可以设置在pattern属性中, 也可以设置在pattern子节点的body中.
	 * 如果两个都设置, 那取pattern属性中的设置.
	 */
	void setPattern(String pattern) throws EternaException;

	/**
	 * 创建一个<code>ResultFormat</code>的实例. <p>
	 *
	 * @return <code>ResultFormat</code>的实例.
	 * @throws EternaException     当相关配置出错时.
	 */
	ResultFormat createFormat() throws EternaException;

}