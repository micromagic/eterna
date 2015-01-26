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

package self.micromagic.eterna.dao;

import self.micromagic.eterna.security.Permission;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.EternaObject;

public interface ResultFormat extends EternaObject
{
	/**
	 * 初始化format.
	 */
	boolean initialize(EternaFactory factory) throws EternaException;

	/**
	 * 获取这个format的名称.
	 */
	String getName() throws EternaException;

	/**
	 * 对一个对象进行格式化输出. <p>
	 * 注: 格式化的结果不能返回<code>null</code>, 如果无法格式化, 请抛出异常.
	 *
	 * @param obj         要进行格式化输出的对象
	 * @param row         当前格式化对象所在的行对象
	 * @param reader      当前格式化的reader对象
	 * @param permission  相关的权限信息
	 * @return   格式化后的字符串
	 * @throws EternaException     当相关配置出错或无法格式化时.
	 */
	Object format(Object obj, ResultRow row, ResultReader reader, Permission permission)
			throws EternaException;

	/**
	 * 当格式化结果为null时, 是否使用空字符串代替.
	 */
	boolean useEmptyString() throws EternaException;

}