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

import java.sql.SQLException;

import org.dom4j.Element;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.EternaObject;
import self.micromagic.util.logging.TimeLogger;

/**
 * 数据操作对象的监听器.
 */
public interface DaoListener extends EternaObject
{
	/**
	 * 监听器的初始化操作.
	 *
	 * @return  是否已执行过初始化, 如果是第一次初始化则返回false, 反之返回true
	 */
	boolean initialize(EternaFactory factory) throws EternaException;

	/**
	 * 是否需要执行的结果信息.
	 */
	boolean needResult();

	/**
	 * 当数据操作执行之后将触发此方法.
	 *
	 * @param dao        发生数据操作的对象
	 * @param info       执行信息的xml节点
	 * @param result     执行结果的xml节点, 如果不需要结果则此参数为null
	 * @param usedTime   数据操作的执行耗时
	 * @param exception  数据操作中抛出的异常
	 * @param conn       执行数据操作所使用的连接对象, 一般为java.sql.Connection
	 *
	 * @see #needResult()
	 * @see TimeLogger#getPassTime(boolean)
	 * @see java.sql.Connection
	 */
	void executed(Dao dao, Element info, Element result, TimeLogger usedTime,
			Throwable exception, Object conn)
			throws EternaException, SQLException;

}
