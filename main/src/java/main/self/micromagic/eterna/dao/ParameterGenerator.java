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
import self.micromagic.eterna.share.Generator;

/**
 * 参数对象的构造器.
 */
public interface ParameterGenerator extends Generator
{
	/**
	 * 设置要构造的Parameter的名称.
	 */
	void setName(String name) throws EternaException;

	/**
	 * 设置对应的列名.
	 */
	void setColumnName(String name) throws EternaException;

	/**
	 * 设置需要的权限.
	 */
	void setPermission(String permission) throws EternaException;

	/**
	 * 设置要构造的Parameter的类型.
	 */
	void setParamType(String type) throws EternaException;

	/**
	 * 设置对应的数据准备生成器的名称.
	 */
	void setPrepareName(String name) throws EternaException;

	/**
	 * 构造一个Parameter.
	 */
	Parameter createParameter(int paramIndex) throws EternaException;

	/**
	 * 构造一个Parameter.
	 *
	 * @param preparers  对应参数准备器的位置列表
	 */
	Parameter createParameter(int paramIndex, int[] preparers) throws EternaException;

}
