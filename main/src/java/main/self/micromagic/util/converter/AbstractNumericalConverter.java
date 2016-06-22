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

package self.micromagic.util.converter;

import org.apache.commons.logging.Log;

import self.micromagic.util.Utility;

public class AbstractNumericalConverter extends ObjectConverter
{
	/**
	 * 转换相关的日志.
	 */
	protected static final Log log = Utility.createLog("eterna.converter");

	/**
	 * 配置中设置是否需要在转换成数值型时将空字符串作为null的标识.
	 */
	public static final String NUMERICAL_EMPTY_TO_NULL_FLAG
			= "eterna.converter.numerical.emptyToNull";

	/**
	 * 是否需要在转换成数值型时将空字符串作为null的全局配置.
	 */
	private static boolean NUMERICAL_EMPTY_TO_NULL;

	static
	{
		try
		{
			Utility.addFieldPropertyManager(NUMERICAL_EMPTY_TO_NULL_FLAG,
					AbstractNumericalConverter.class, "NUMERICAL_EMPTY_TO_NULL");
		}
		catch (Throwable ex)
		{
			log.warn("Error in init numerical empty to null.", ex);
		}
	}

	public AbstractNumericalConverter()
	{
		this.emptyToNull = NUMERICAL_EMPTY_TO_NULL;
	}

	protected boolean emptyToNull;

	public boolean isEmptyToNull()
	{
		return this.emptyToNull;
	}

	public void setEmptyToNull(boolean emptyToNull)
	{
		this.emptyToNull = emptyToNull;
	}

}
