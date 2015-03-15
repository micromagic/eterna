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

import self.micromagic.eterna.share.EternaException;

public interface ModifiableResultRow extends ResultRow
{
	/**
	 * 在当前行<code>ResultRow</code>对象中指定列的值.
	 *
	 * @param columnIndex  第一列数是1, 第二列是2, ...
	 * @param v            要设置的值
	 */
	public void setValue(int columnIndex, Object v) throws EternaException;

	/**
	 * 在当前行<code>ResultRow</code>对象中指定列的值.
	 *
	 * @param columnName  列的名称
	 * @param v           要设置的值
	 */
	public void setValue(String columnName, Object v) throws EternaException;

}
