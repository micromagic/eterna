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

public interface ModifiableResultRow extends ResultRow
{
	/**
	 * 判断当前行<code>ResultRow</code>对象是否被修改过. <p>
	 * 只有修改值才会返回true, 修改行号或格式化后的值不会改变是否修改的标识.
	 */
	boolean isModified() throws EternaException;

	/**
	 * 设置当前<code>ResultRow</code>在结果中的位置.
	 */
	void setRowNum(int num) throws EternaException;

	/**
	 * 设置当前行<code>ResultRow</code>对象中指定列的值.
	 *
	 * @param columnIndex  第一列数是1, 第二列是2, ...
	 * @param v            要设置的值
	 */
	void setValue(int columnIndex, Object v) throws EternaException;

	/**
	 * 设置当前行<code>ResultRow</code>对象中指定列的值.
	 *
	 * @param columnName  列的名称
	 * @param v           要设置的值
	 */
	void setValue(String columnName, Object v) throws EternaException;

	/**
	 * 设置当前行<code>ResultRow</code>对象中指定列的格式化后的值.
	 *
	 * @param columnIndex  第一列数是1, 第二列是2, ...
	 * @param v            要设置的格式化后的值
	 */
	void setFormated(int columnIndex, Object v) throws EternaException;

	/**
	 * 设置当前行<code>ResultRow</code>对象中指定列的格式化后的值.
	 *
	 * @param columnName  列的名称
	 * @param v           要设置的格式化后的值
	 */
	void setFormated(String columnName, Object v) throws EternaException;

}
