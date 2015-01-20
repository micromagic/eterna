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

package self.micromagic.eterna.search;

import java.sql.Connection;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.base.Query;
import self.micromagic.eterna.model.AppData;

/**
 * @author micromagic@sina.com
 */
public interface ParameterSetting
{
	/**
	 * 初始化, 该方法会在所属的search初始化时被调用.
	 */
	void initParameterSetting(Search search) throws EternaException;

	/**
	 * 设置用于查询的QueryAdapter的参数.
	 *
	 * @param first     表示是否为第一次执行参数设置, 如果不是第一次, 则可根据情况,
	 *                  或重新设置参数, 或返回什么都不做使用前一次的设置.
	 * @param data      数据, 里面包含了request的parameter, request的attribute,
	 *                  session的attritute
	 */
	void setParameter(Query query, Search search, boolean first,
			AppData data, Connection conn)
			throws EternaException;

}