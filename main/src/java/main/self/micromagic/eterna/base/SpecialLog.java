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

import java.sql.Connection;
import java.sql.SQLException;

import org.dom4j.Element;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.util.logging.TimeLogger;

/**
 * 用于记录特殊的SQL日志.
 */
public interface SpecialLog
{
	/**
	 * 初始化这个日志记录器.
	 */
	void initSpecialLog(EternaFactory factory) throws EternaException;

	/**
	 * 记录日志.
	 *
	 * @param base       发生日志的数据库操作对象
	 * @param xmlLog     已记录日志信息的xml节点
	 * @param usedTime   sql执行用时, 会根据jdk版本给出毫秒或纳秒, 请使用
	 *                   TimeLogger的formatPassTime方法格式化
	 * @param exception  出错时抛出的异常
	 * @param conn       执行<code>SQLAdapter</code>所使用的数据库连接
	 * @see TimeLogger#formatPassTime(long)
	 */
	void logSQL(Base base, Element xmlLog, long usedTime, Throwable exception, Connection conn)
			throws EternaException, SQLException;

}