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

package self.micromagic.eterna.base;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import self.micromagic.eterna.security.PermissionSet;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;

/**
 * @author micromagic@sina.com
 */
public interface ResultReader
{
	/**
	 * 在arrtibute中设置输入框类型使用的名称.
	 */
	public static final String INPUT_TYPE_FLAG = "inputType";

	/**
	 * 初始化本reader对象, 系统会在初始化时调用此方法. <p>
	 * 该方法的主要作用是根据设置的format的名称来初始化format对象.
	 *
	 * @param factory  EternaFactory的实例, 可以从中获得format对象
	 */
	void initialize(EternaFactory factory) throws EternaException;

	/**
	 * 获取该reader的类型.
	 *
	 * @return  一个代表该reader类型的整数
	 * @see self.micromagic.eterna.share.TypeManager
	 */
	int getType() throws EternaException;

	/**
	 * 获取format的名称.
	 */
	String getFormatName() throws EternaException;

	/**
	 * 获取format对象.
	 */
	ResultFormat getFormat() throws EternaException;

	/**
	 * 获取本reader的名称.
	 */
	String getName() throws EternaException;

	/**
	 * 获得数据库的列列名, 用于生成在"ORDER BY"之后出现的列名.
	 * 在多个表时, 也可以是"[表名].[列名]"的形式.
	 */
	String getColumnName() throws EternaException;

	/**
	 * 读取数据时, 读取的列别名. <p>
	 * 该方法和{@link #getColumnIndex}方法只有一个有效, 当索引值有效
	 * 时，返回的列别名为null.
	 */
	String getAlias() throws EternaException;

	/**
	 * 读取数据时, 读取的列索引. <p>
	 * 该方法和{@link #getColumnName}方法只有一个有效, 当列名有效时,
	 * 返回的索引值为-1.
	 */
	int getColumnIndex() throws EternaException;

	/**
	 * 该ResultReader是否有效.
	 * 比如对于无权访问的列, 就会生成一个空的ResultReader来占位,
	 * 这个空的ResultReader的valid值就为false.
	 *
	 * 注: 如果不设置空的ResultReader占位的话, 用index访问时就会出错.
	 */
	boolean isValid() throws EternaException;

	/**
	 * 判断读取数据时, 是否是通过列别名来读取.
	 */
	boolean isUseAlias() throws EternaException;

	/**
	 * 判断读取数据时, 是否是通过索引值来读取.
	 */
	boolean isUseColumnIndex() throws EternaException;

	/**
	 * 获得一个attribute.
	 *
	 * @param name    要获得的attribute的名称
	 */
	Object getAttribute(String name) throws EternaException;

	/**
	 * 获得所有attribute的名称.
	 */
	String[] getAttributeNames() throws EternaException;

	/**
	 * 获取可读取该列的权限集合, 只有拥有集合中的任意1个权限就可以
	 * 读取该列. <p>
	 * 如果没有设置权限集合, 则返回null, 表示读取该列不需要权限.
	 */
	PermissionSet getPermissionSet() throws EternaException;

	/**
	 * 获取该列的标题.
	 */
	String getCaption() throws EternaException;

	/**
	 * 从<code>ResultSet</code>对象中读取数据, 并以相应的对象返回.
	 */
	Object readResult(ResultSet rs) throws SQLException;

	/**
	 * 从<code>CallableStatement</code>对象中读取数据, 并以相应的对象返回.
	 */
	Object readCall(CallableStatement call, int index) throws SQLException;

	/**
	 * 从<code>Object</code>对象中读取数据, 并以相应的对象返回.
	 */
	Object readObject(Object obj) throws EternaException;

}