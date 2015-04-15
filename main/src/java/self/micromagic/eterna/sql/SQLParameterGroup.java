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

import java.util.Iterator;

import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.EternaException;

/**
 * 参数组, 多个参数的组合
 */
public interface SQLParameterGroup
{
	/**
	 * 在reader的arrtibute中设置VPC使用的名称.
	 */
	public static final String READER_VPC_FLAG = "vpcName";

	/**
	 * 在reader的arrtibute中设置columnName使用的名称.
	 */
	public static final String READER_COLNAME_FLAG = "columnName";

	/**
	 * ignoreList列表中, 加上此标记名表示忽略参数组中同名的参数
	 */
	public static final String IGNORE_SAME_NAME = "$ignoreSame";

	/**
	 * 初始化本SQLParameterGroup对象, 系统会在初始化时调用此方法. <p>
	 * 该方法的主要作用是初始化每个SQLParameter对象, 并根据父对象来组成自己
	 * 自己的reader列表.
	 *
	 * @param factory  EternaFactory的实例, 可以从中获得父对象
	 */
	void initialize(EternaFactory factory) throws EternaException;

	/**
	 * 设置本SQLParameterGroup的名称.
	 */
	void setName(String name) throws EternaException;

	/**
	 * 获取本SQLParameterGroup的名称.
	 */
	String getName() throws EternaException;

	/**
	 * 获取SQLParameterGenerator列表的迭代器.
	 */
	Iterator getParameterGeneratorIterator() throws EternaException;

	/**
	 * 添加一个参数. <p>
	 *
	 * @param paramGenerator     参数构造器.
	 * @throws EternaException     当相关配置出错时.
	 */
	void addParameter(SQLParameterGenerator paramGenerator) throws EternaException;

	/**
	 * 添加一个参数组. <p>
	 *
	 * @param groupName     参数组名称.
	 * @param ignoreList    忽略的参数列表.
	 * @throws EternaException     当相关配置出错时.
	 */
	void addParameterRef(String groupName, String ignoreList) throws EternaException;

}