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

package self.micromagic.util.container;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * request中请求头信息的容器.
 */
public class RequestHeaderContainer extends AbstractContainerSetting
		implements ValueContainer
{
	private final HttpServletRequest request;
	private HttpServletResponse response;

	/**
	 * 用于保存在response中设置的数据.
	 */
	private Map settedMap;

	/**
	 * 通过此构造函数创建的容器只能进行读取.
	 *
	 * @param request  请求头信息所在的request对象
	 */
	public RequestHeaderContainer(HttpServletRequest request)
	{
		if (request == null)
		{
			throw new NullPointerException("Request can't be null.");
		}
		this.request = request;
		this.parseValue = true;
	}

	/**
	 * 通过此构造函数创建的容器才能进行写入.
	 *
	 * @param request   请求头信息所在的request对象
	 * @param response  设置请求头信息所需要的response对象
	 */
	public RequestHeaderContainer(HttpServletRequest request, HttpServletResponse response)
	{
		this(request);
		this.response = response;
	}

	private String encodeStr(String value)
	{
		return this.encodeStr(value, this.response.getCharacterEncoding());
	}

	private String decodeStr(String value)
	{
		return this.decodeStr(value, this.request.getCharacterEncoding());
	}

	public Object getValue(Object key)
	{
		String name = key == null ? null : key.toString();
		if (this.parseValue)
		{
			if (name != null && name.endsWith(this.parseSuffix))
			{
				return this.getHeader(name.substring(0, name.length() - this.parseSuffix.length()), true);
			}
			else
			{
				return this.getHeader(name, false);
			}
		}
		else
		{
			return this.getHeader(name, true);
		}
	}

	/**
	 * 先从settedMap中获取再从request的header中获取.
	 */
	private Object getHeader(String name, boolean array)
	{
		Object value = this.settedMap == null ? null
				: this.settedMap.get(name == null ? null : name.toLowerCase());
		if (value == null)
		{
			name = this.encodeStr(name, this.request.getCharacterEncoding());
			if (array)
			{
				value = this.request.getHeaders(name);
			}
			else
			{
				value = this.decodeStr(this.request.getHeader(name));
			}
		}
		if (value != null)
		{
			if (array)
			{
				value = this.headers2Array(value);
			}
			else
			{
				value = RequestParameterMap.getFirstParam(value);
			}
		}
		return value;
	}

	/**
	 * 将头信息转换成字符串数组.
	 */
	private String[] headers2Array(Object value)
	{
		if (value instanceof String)
		{
			return new String[]{(String) value};
		}
		else if (value instanceof String[])
		{
			return (String[]) value;
		}
		Enumeration headers = (Enumeration) value;
		List result = new ArrayList();
		while (headers.hasMoreElements())
		{
			result.add(this.decodeStr((String) headers.nextElement()));
		}
		int size = result.size();
		return size == 0 ? null : (String[]) result.toArray(new String[size]);
	}

	public boolean containsKey(Object key)
	{
		if (this.getValue(key) != null)
		{
			return true;
		}
		if (this.settedMap != null)
		{
			return this.settedMap.containsKey(
					key == null ? null : key.toString().toLowerCase());
		}
		return false;
	}

	public void setValue(Object key, Object value)
	{
		if (this.response == null)
		{
			throw new UnsupportedOperationException();
		}
		if (value == null)
		{
			throw new NullPointerException("Value can't be null.");
		}
		String name = key == null ? null : key.toString();
		if (value instanceof String[])
		{
			String[] arr = (String[]) value;
			if (arr.length == 0)
			{
				throw new NullPointerException("Value can't be empty.");
			}
			else
			{
				String encodeName = this.encodeStr(name);
				this.response.setHeader(encodeName, this.encodeStr(arr[0]));
				for (int i = 1; i < arr.length; i++)
				{
					this.response.addHeader(encodeName, this.encodeStr(arr[i]));
				}
				this.setSettedMap(name, value);
			}
		}
		else
		{
			String str = value.toString();
			this.response.setHeader(this.encodeStr(name), this.encodeStr(str));
			this.setSettedMap(name, str);
		}
	}

	/**
	 * 保存在response中设置的数据.
	 */
	private void setSettedMap(String name, Object value)
	{
		if (this.settedMap == null)
		{
			this.settedMap = new HashMap();
		}
		this.settedMap.put(name.toLowerCase(), value);
	}

	public void removeValue(Object key)
	{
		throw new UnsupportedOperationException();
	}

	public Enumeration getKeys()
	{
		return this.request.getHeaderNames();
	}

}