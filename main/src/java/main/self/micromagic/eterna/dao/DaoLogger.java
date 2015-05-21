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

import org.dom4j.Element;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaObject;
import self.micromagic.util.logging.TimeLogger;

/**
 * 用于记录特殊的SQL日志.
 */
public interface DaoLogger extends EternaObject
{
	/**
	 * 记录日志.
	 *
	 * @param base       发生日志的数据库操作对象
	 * @param node       记录了相关信息的xml节点
	 * @param usedTime   数据操作执行用时, 请使用formatPassTime方法格式化后的时间
	 * @param error      出错时抛出的异常
	 * @param conn       执行数据操作执所使用的数据库连接
	 * @see TimeLogger#formatPassTime(boolean)
	 */
	void log(Dao base, Element node, TimeLogger usedTime, Throwable error, Connection conn)
			throws EternaException, SQLException;

}