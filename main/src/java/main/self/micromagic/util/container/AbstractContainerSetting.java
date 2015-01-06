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

package self.micromagic.util.container;

import java.net.URLEncoder;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;

import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;

/**
 * 容器设置的抽象实现.
 */
class AbstractContainerSetting
{
	/**
	 * 进行URL编解码时使用的默认字符集
	 */
	protected static final String DEFAULT_CHARSET = Utility.getProperty(Utility.CHARSET_TAG, "UTF-8");

	/**
	 * 是否要对获取的value进行处理.
	 */
	protected boolean parseValue = true;

	/**
	 * 判断是否需要处理获取的value的名称后缀.
	 */
	protected String parseSuffix = "[]";

	/**
	 * 获取判断是否需要处理获取的value的名称后缀.
	 */
	public String getParseSuffix()
	{
		return this.parseSuffix;
	}

	/**
	 * 获取是否要对读取的value进行处理.
	 */
	public boolean isParseValue()
	{
		return this.parseValue;
	}

	/**
	 * 设置是否要对获取的value进行处理. <p>
	 * 如果设为<code>true</code>
	 * 当给的名称为普通的值, 则只获取字符串数据(如: 参数数组的第一个值或cookie中的vaule);
	 * 当给的名称是以特殊的后缀结尾(如：[]), 则以原始对象的形式返回(如: 字符串数组或cookie对象);
	 * 如果设为<code>false</code>, 则不对名称进行判断, 以原始对象的形式返回.
	 */
	public void setParseValue(boolean parseValue)
	{
		this.parseValue = parseValue;
	}

	/**
	 * 设置是否要对获取的value进行处理. <p>
	 * 如果设为<code>true</code>
	 * 当给的名称为普通的值, 则只获取字符串数据(如: 参数数组的第一个值或cookie中的vaule);
	 * 当给的名称是以特殊的后缀结尾(如：[]), 则以原始对象的形式返回(如: 字符串数组或cookie对象);
	 * 如果设为<code>false</code>, 则不对名称进行判断, 以原始对象的形式返回.
	 *
	 * @param parseValue  是否要对获取的value进行处理
	 * @param suffix      判断是否需要处理获取的value的名称后缀
	 */
	public void setParseValue(boolean parseValue, String suffix)
	{
		if (StringTool.isEmpty(suffix))
		{
			throw new NullPointerException("Suffix can't be null or empty.");
		}
		this.parseValue = parseValue;
		this.parseSuffix = suffix;
	}

	/**
	 * 对字符串以URL的格式进行编码.
	 */
	protected String encodeStr(String value, String charset)
	{
		if (value == null)
		{
			return null;
		}
		try
		{
			if (StringTool.isEmpty(charset))
			{
				charset = DEFAULT_CHARSET;
			}
			return URLEncoder.encode(value, charset);
		}
		catch (UnsupportedEncodingException ex)
		{
			throw new IllegalArgumentException("Error charset:[" + charset + "], " + ex);
		}
	}

	/**
	 * 对字符串以URL的格式进行解码.
	 */
	protected String decodeStr(String value, String charset)
	{
		if (value == null)
		{
			return null;
		}
		try
		{
			if (StringTool.isEmpty(charset))
			{
				charset = DEFAULT_CHARSET;
			}
			return URLDecoder.decode(value, charset);
		}
		catch (UnsupportedEncodingException ex)
		{
			throw new IllegalArgumentException("Error charset:[" + charset + "], " + ex);
		}
	}

}