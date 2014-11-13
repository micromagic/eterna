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

import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.share.Generator;

public interface ResultReaderGenerator extends Generator
{
	/**
	 * 设置reader的名称.
	 */
	void setName(String name) throws ConfigurationException;

	/**
	 * 设置reader读取数据时使用的列名. <p>
	 * 该方法和{@link #setColumnIndex(int)}方法只有一个有效, 当设置了使用
	 * 的列名后就不能再设置使用索引值.
	 */
	void setColumnName(String columnName) throws ConfigurationException;

	/**
	 * 设置reader读取数据时使用的索引值. <p>
	 * 该方法和{@link #setColumnName(String)}方法只有一个有效, 当设置了使用
	 * 的索引值后就不能再设置使用列名.
	 */
	void setColumnIndex(int columnIndex) throws ConfigurationException;

	/**
	 * 设置作为排序列, 即出现在"ORDER BY"之后的列名.
	 * 在多个表时, 也可以是"[表名].[列名]"的形式.
	 */
	void setOrderName(String orderName) throws ConfigurationException;

	/**
	 * 设置format的名称.
	 */
	void setFormatName(String name) throws ConfigurationException;

	/**
	 * 设置reader的类型.
	 *
	 * @param type   类型的名称
	 * @see self.micromagic.eterna.share.TypeManager
	 */
	void setType(String type) throws ConfigurationException;

	/**
	 * 设置可读取该列的权限集合, 只有拥有集合中的任意1个权限就可以
	 * 读取该列.
	 *
	 * @param permissions  权限的名称, 多个名称之间用","分割
	 */
	void setPermissions(String permissions) throws ConfigurationException;

	/**
	 * 设置reader的标题.
	 */
	void setCaption(String caption) throws ConfigurationException;

	/**
	 * 设置显示时的宽度.
	 */
	void setWidth(int width) throws ConfigurationException;

	/**
	 * 设置向html页面输出时是否要过滤特殊标签.
	 */
	void setHtmlFilter(boolean filter) throws ConfigurationException;

	/**
	 * 设置是否可见.
	 */
	void setVisible(boolean visible) throws ConfigurationException;

	/**
	 * 根据已有的设置, 创建一个reader对象.
	 */
	ResultReader createReader() throws ConfigurationException;

}