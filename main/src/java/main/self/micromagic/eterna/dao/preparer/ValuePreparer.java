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

package self.micromagic.eterna.dao.preparer;

import java.sql.SQLException;

import self.micromagic.eterna.dao.PreparedStatementWrap;
import self.micromagic.eterna.share.EternaException;

/**
 * 值准备器.
 */
public interface ValuePreparer
{
	/**
	 * 获得这个值准备器的创建者.
	 */
	PreparerCreater getCreater() throws EternaException;

	/**
	 * 将参数设置到PreparedStatementWrap中.
	 *
	 * @param index      要设置的参数的索引值
	 * @param stmtWrap   PreparedStatement的外附类
	 */
	void setValueToStatement(int index, PreparedStatementWrap stmtWrap)
			throws SQLException;

	/**
	 * 设置此参数的名称.
	 */
	public void setName(String name);

	/**
	 * 获取此参数的名称.
	 */
	public String getName();

	/**
	 * 设置此参数的相对索引值, 此值需对应SQLAdapter对象中配置的参数索引值.
	 */
	public void setRelativeIndex(int index);

	/**
	 * 获取此参数的相对索引值, SQLAdapter对象会调用它, 将其设置到相应位置.
	 */
	public int getRelativeIndex();

}