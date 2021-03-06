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

import java.sql.Connection;
import java.sql.SQLException;

import self.micromagic.eterna.share.EternaException;

/**
 * 数据库的更新对象.
 */
public interface Update extends Dao
{
	/**
	 * 执行本数据库更新对象.
	 *
	 * @return    更新的记录数.
	 */
	int executeUpdate(Connection conn) throws EternaException, SQLException;

	/**
	 * 获取前一次executeUpdate方法的执行结果.
	 *
	 * @return  前一次的执行结果, 如果返回-1表示未执行或执行的是execute方法
	 */
	int getExecutedResult() throws EternaException;

}