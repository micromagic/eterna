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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletRequest;

import self.micromagic.util.Utility;

public class RequestParameterMap extends AbstractContainerSetting
		implements Map
{
	/**
	 * 配置是否需要对request中的数据取值进行解析.
	 */
	public static final String PARSE_PARAM_PROPERTY = "self.micromagic.parse.request.param";

	/**
	 * 是否需要对request中的数据取值进行解析. <p>
	 * 如果进行解析的话, 使用普通的名称(如: name)时, 只会取出字符串数组中的第一个;
	 * 使用特殊的名称(如: name[])时, 才会以数组的形式取出参数.
	 */
	private static boolean PARSE_PARAM = false;

	static
	{
		try
		{
			Utility.addFieldPropertyManager(PARSE_PARAM_PROPERTY, RequestParameterMap.class, "PARSE_PARAM");
		}
		catch (Throwable ex) {}
	}

	private Map paramMap;
	private final Map originParamMap;
	private boolean readOnly = true;
	private boolean selfMap = false;

	private RequestParameterMap(Map requestMap)
	{
		this.paramMap = requestMap;
		this.originParamMap = this.paramMap;
		this.parseValue = PARSE_PARAM;
	}

	private RequestParameterMap(Map requestMap, boolean readOnly)
	{
		this(requestMap);
		this.readOnly = readOnly;
	}

	private RequestParameterMap(ServletRequest request)
	{
		this.paramMap = request.getParameterMap();
		this.originParamMap = this.paramMap;
		this.parseValue = PARSE_PARAM;
	}

	private RequestParameterMap(ServletRequest request, boolean readOnly)
	{
		this(request);
		this.readOnly = readOnly;
	}

	/**
	 * 获取对象中的第一个字符串.
	 */
	public static String getFirstParam(Object obj)
	{
		if (obj == null)
		{
			return null;
		}
		if (obj instanceof String[])
		{
			String[] arr = (String[]) obj;
			return arr.length > 0 ? arr[0] : null;
		}
		if (obj instanceof String)
		{
			return (String) obj;
		}
		if (obj instanceof Object[])
		{
			Object[] arr = (Object[]) obj;
			return arr.length > 0 ? getFirstParam(arr[0]) : null;
		}
		if (obj instanceof Collection)
		{
			Collection c = (Collection) obj;
			return c.size() > 0 ? getFirstParam(c.iterator().next()) : null;
		}
		return obj.toString();
	}

	/**
	 * 通过request来构造一个request.parameter的map.
	 */
	public static RequestParameterMap create(ServletRequest request)
	{
		return request == null ? null : new RequestParameterMap(request);
	}

	/**
	 * 通过request来构造一个request.parameter的map.
	 *
	 * @param readOnly   是否为只读, 如果设为true, 表示不可以设置属性.
	 */
	public static RequestParameterMap create(ServletRequest request, boolean readOnly)
	{
		return request == null ? null : new RequestParameterMap(request, readOnly);
	}

	/**
	 * 通过map来构造一个request.parameter的map. <p>
	 * 一般在portlet或单元测试的环境中使用.
	 */
	public static RequestParameterMap create(Map requestMap)
	{
		return requestMap == null ? null : new RequestParameterMap(requestMap);
	}

	/**
	 * 通过map来构造一个request.parameter的map. <p>
	 * 一般在portlet或单元测试的环境中使用.
	 *
	 * @param readOnly   是否为只读, 如果设为true, 表示不可以设置属性.
	 */
	public static RequestParameterMap create(Map requestMap, boolean readOnly)
	{
		return requestMap == null ? null : new RequestParameterMap(requestMap, readOnly);
	}

	/**
	 * 获得原始的参数map.
	 */
	public Map getOriginParamMap()
	{
		return this.originParamMap;
	}

	/**
	 * 获取此参数map是否是只读的.
	 */
	public boolean isReadOnly()
	{
		return this.readOnly;
	}

	/**
	 * 获取参数数组中的第一个字符串.
	 */
	public String getFirstString(Object key)
	{
		return getFirstParam(this.paramMap.get(key));
	}

	/**
	 * 检查此参数map是否可编辑, 如果是可编辑的, 则通过原始的参数map来构造一个新的map.
	 */
	private boolean checkEdit()
	{
		if (!this.readOnly)
		{
			if (!this.selfMap)
			{
				this.paramMap = new HashMap(this.originParamMap);
				this.selfMap = true;
			}
			return true;
		}
		return false;
	}

	public Object get(Object key)
	{
		if (this.parseValue)
		{
			if (key == null)
			{
				return this.getFirstString(key);
			}
			String strKey = key.toString();
			if (strKey.endsWith(this.parseSuffix))
			{
				Object value = this.paramMap.get(strKey.substring(0, strKey.length() - this.parseSuffix.length()));
				if (value == null)
				{
					return null;
				}
				if (value instanceof String[])
				{
					return value;
				}
				if (value instanceof String)
				{
					return new String[]{(String) value};
				}
				if (value instanceof Object[])
				{
					// 如果是个对象数组这里将其变成Collection
					// 在下一个判断条件中处理
					value = Arrays.asList((Object[]) value);
				}
				if (value instanceof Collection)
				{
					Collection c = (Collection) value;
					String[] arr = new String[c.size()];
					Iterator itr = c.iterator();
					int index = 0;
					while (itr.hasNext())
					{
						Object obj = itr.next();
						arr[index++] = obj == null ? null : obj.toString();
					}
					return arr;
				}
				return new String[]{value.toString()};
			}
			else
			{
				return this.getFirstString(key);
			}
		}
		return this.paramMap.get(key);
	}

	public Object put(Object key, Object value)
	{
		this.checkEdit();
		return this.paramMap.put(key, value);
	}

	public Object remove(Object key)
	{
		this.checkEdit();
		return this.paramMap.remove(key);
	}

	public void putAll(Map t)
	{
		this.checkEdit();
		this.paramMap.putAll(t);
	}

	public void clear()
	{
		this.checkEdit();
		this.paramMap.clear();
	}

	public Set keySet()
	{
		return this.paramMap.keySet();
	}

	public Collection values()
	{
		if (this.parseValue)
		{
			return new RequestParameterMapEntrySet(this, true);
		}
		return this.paramMap.values();
	}

	public Set entrySet()
	{
		this.checkEdit();
		if (this.parseValue)
		{
			return new RequestParameterMapEntrySet(this, false);
		}
		return this.paramMap.entrySet();
	}

	/**
	 * 获取真正的EntrySet
	 */
	Set realEntrySet()
	{
		this.checkEdit();
		return this.paramMap.entrySet();
	}

	public boolean equals(Object obj)
	{
		return this.paramMap.equals(obj);
	}

	public int hashCode()
	{
		return this.paramMap.hashCode();
	}

	public int size()
	{
		return this.paramMap.size();
	}

	public boolean isEmpty()
	{
		return this.paramMap.isEmpty();
	}

	public boolean containsKey(Object key)
	{
		return this.paramMap.containsKey(key);
	}

	public boolean containsValue(Object value)
	{
		if (this.parseValue)
		{
			Set s = new RequestParameterMapEntrySet(this, true);
			return s.contains(value);
		}
		return this.paramMap.containsValue(value);
	}

}