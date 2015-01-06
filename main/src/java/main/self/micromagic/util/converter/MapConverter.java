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

package self.micromagic.util.converter;

import java.util.Map;
import java.util.HashMap;

import self.micromagic.util.StringTool;
import self.micromagic.util.ObjectRef;
import self.micromagic.cg.BeanTool;
import self.micromagic.cg.BeanMap;

public class MapConverter extends ObjectConverter
{
	private String itemDelimiter = ";\n";
	private char kvDelimiter = '=';
	private boolean beanToMap = true;

	/**
	 * 获取字符串中每个元素的分割符.
	 */
	public String getItemDelimiter()
	{
		return this.itemDelimiter;
	}

	/**
	 * 设置字符串中每个元素的分割符.
	 */
	public void setItemDelimiter(String itemDelimiter)
	{
		this.itemDelimiter = itemDelimiter;
	}

	/**
	 * 获取字符串中键和值的分割符.
	 */
	public char getKvDelimiter()
	{
		return this.kvDelimiter;
	}

	/**
	 * 设置字符串中键和值的分割符.
	 */
	public void setKvDelimiter(char kvDelimiter)
	{
		this.kvDelimiter = kvDelimiter;
	}

	/**
	 * 当对象为bean时, 转换时使用的beanToMap的值.
	 *
	 * @see BeanMap#setBean2Map(boolean)
	 */
	public boolean isBeanToMap()
	{
		return this.beanToMap;
	}

	/**
	 * 设置当对象为bean时, 转换时使用的beanToMap的值.
	 *
	 * @see BeanMap#setBean2Map(boolean)
	 */
	public void setBeanToMap(boolean beanToMap)
	{
		this.beanToMap = beanToMap;
	}

	public Map convertToMap(Object value)
	{
		if (value == null)
		{
			return null;
		}
		if (value instanceof Map)
		{
			return (Map) value;
		}
		if (value instanceof String)
		{
			return this.convertToMap((String) value);
		}
		Object tmpObj = this.changeByPropertyEditor(value);
		if (tmpObj instanceof Map)
		{
			return (Map) tmpObj;
		}
		if (value instanceof Object[])
		{
			Object[] arr = (Object[]) value;
			Map result = new HashMap();
			for (int i = 0; i < arr.length; i += 2)
			{
				if (i + 1 < arr.length)
				{
					result.put(arr[i], arr[i + 1]);
				}
				else
				{
					result.put(arr[i], null);
				}
			}
		}
		if (value instanceof ObjectRef)
		{
			return this.convertToMap(((ObjectRef) value).getObject());
		}
		if (BeanTool.checkBean(value.getClass()))
		{
			BeanMap map = BeanTool.getBeanMap(value);
			map.setBean2Map(this.beanToMap);
			return map;
		}
		throw new ClassCastException(getCastErrorMessage(value, "Map"));
	}

	public Map convertToMap(String value)
	{
		if (value == null)
		{
			return null;
		}
		Object tmpObj = this.changeByPropertyEditor(value);
		if (tmpObj instanceof Map)
		{
			return (Map) tmpObj;
		}
		return StringTool.string2Map(value, this.itemDelimiter, this.kvDelimiter,
				true, false, null, null);
	}

	public Object convert(Object value)
	{
		if (value instanceof Map)
		{
			return value;
		}
		try
		{
			return this.convertToMap(value);
		}
		catch (Exception ex)
		{
			if (this.needThrow)
			{
				if (ex instanceof RuntimeException)
				{
					throw (RuntimeException) ex;
				}
				throw new ClassCastException(getCastErrorMessage(value, "Map"));
			}
			else
			{
				return null;
			}
		}
	}

	public Object convert(String value)
	{
		try
		{
			return this.convertToMap(value);
		}
		catch (Exception ex)
		{
			if (this.needThrow)
			{
				if (ex instanceof RuntimeException)
				{
					throw (RuntimeException) ex;
				}
				throw new ClassCastException(getCastErrorMessage(value, "Map"));
			}
			else
			{
				return null;
			}
		}
	}

}