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

package self.micromagic.eterna.model.impl;

import java.util.Map;
import java.util.HashMap;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.model.AppData;

/**
 * 数据处理者.
 */
public class DataHandler
{
	/**
	 * 读取数据的配置.
	 */
	private String config;


	/**
	 * AppData中的map的获取工具.
	 */
	private MapGetter mapGetter;

	/**
	 * 读取/设置map中数据的名称.
	 */
	private String mapDataName;

	/**
	 * 从哪个cache中读取/设置数据.
	 */
	private int cacheIndex = -1;

	/**
	 * 是否从堆栈中读取数据.
	 */
	private boolean fromStack = false;

	/**
	 * 以peek方式获取堆栈中的值.
	 */
	private int peekIndex = -1;

	/**
	 * 读取的常量数据的值.
	 */
	private String constValue = null;

	private boolean needMapDataName = true;
	private boolean readOnly = true;
	private String caption = "config";

	/**
	 * @param caption           显示错误信息是使用的标题
	 * @param needMapDataName   当读取map数据时, 是否需要给出数据的名称
	 *                          当readOnly的值为false时, 此值会被强制设为true
	 * @param readOnly          是否为只读方式
	 */
	public DataHandler(String caption, boolean needMapDataName, boolean readOnly)
	{
		this.caption = caption;
		this.readOnly = readOnly;
		this.needMapDataName = !readOnly || needMapDataName;
	}

	/**
	 * 清空已有的配置
	 */
	private void clearConfig()
	{
		this.config = null;
		this.mapGetter = null;
		this.mapDataName = null;
		this.cacheIndex = -1;
		this.fromStack = false;
		this.peekIndex = -1;
	}

	/**
	 * 获取处理配置.
	 */
	public String getConfig()
	{
		return this.config;
	}

	/**
	 * 设置处理配置.
	 */
	public void setConfig(String config)
			throws EternaException
	{
		this.clearConfig();
		this.config = config;
		int index = config.indexOf(':');
		String mainName = config;
		String subName = null;
		if (index != -1)
		{
			subName = config.substring(index + 1);
			mainName = config.substring(0, index);
		}

		Object tObj = mapNameIndex.get(mainName);
		if (tObj != null)
		{
			this.mapGetter = (MapGetter) tObj;
		}
		if (this.mapGetter != null)
		{
			if (subName != null)
			{
				this.mapDataName = subName;
				return;
			}
			if (!this.needMapDataName)
			{
				// 子名称未设置并且不是必需时, 则退出不用抛出异常.
				return;
			}
		}
		else if ("cache".equals(mainName))
		{
			this.cacheIndex = 0;
			if (subName != null)
			{
				try
				{
					this.cacheIndex = Integer.parseInt(subName);
					return;
				}
				catch (NumberFormatException ex) {}
			}
			else
			{
				return;
			}
		}
		if (this.readOnly)
		{
			if ("stack".equals(mainName))
			{
				this.fromStack = true;
				if ("pop".equals(subName) || subName == null)
				{
					return;
				}
				if (subName != null && subName.startsWith("peek"))
				{
					this.peekIndex = 0;
					if (subName.length() > 4)
					{
						if (subName.charAt(4) == '-')
						{
							try
							{
								this.peekIndex = Integer.parseInt(subName.substring(5));
								return;
							}
							catch (NumberFormatException ex) {}
						}
					}
					else
					{
						return;
					}
				}
			}
			else if ("value".equals(mainName))
			{
				if (subName != null)
				{
					this.constValue = subName;
					return;
				}
			}
		}

		throw new EternaException("Error " + this.caption + " [" + config + "].");
	}

	/**
	 * 根据处理配置读取数据.
	 *
	 * @param data       AppData对象, 可从中获取数据
	 * @param remove     获取数据后, 是否将源头的数据移除
	 */
	public Object getData(AppData data, boolean remove)
			throws EternaException
	{
		Object value = null;
		if (this.constValue != null)
		{
			value = this.constValue;
		}
		else if (this.mapGetter != null)
		{
			Map tmpMap = this.mapGetter.getMap(data);
			if (this.mapDataName != null)
			{
				value = tmpMap.get(this.mapDataName);
				if (remove)
				{
					tmpMap.remove(this.mapDataName);
				}
			}
			else
			{
				value = tmpMap;
			}
		}
		else if (this.cacheIndex != -1)
		{
			value = data.caches[this.cacheIndex];
			if (remove)
			{
				data.caches[this.cacheIndex] = null;
			}
		}
		else if (this.fromStack)
		{
			if (this.peekIndex != -1)
			{
				value = data.peek(this.peekIndex);
			}
			else
			{
				value = data.pop();
			}
		}
		return value;
	}

	/**
	 * 根据处理配置设置数据
	 */
	public void setData(AppData data, Object value)
			throws EternaException
	{
		if (this.readOnly)
		{
			throw new EternaException("The [" + this.caption + "] is read only, can't be setted.");
		}
		if (this.mapGetter != null)
		{
			Map tmpMap = this.mapGetter.getMap(data);
			if (value == null)
			{
				tmpMap.remove(this.mapDataName);
			}
			else
			{
				tmpMap.put(this.mapDataName, value);
			}
		}
		else if (this.cacheIndex != -1)
		{
			data.caches[this.cacheIndex] = value;
		}
	}

	/**
	 * 存放名称与AppData中map索引值的对应表.
	 */
	private static final Map mapNameIndex = new HashMap();
	static
	{
      mapNameIndex.put(AppData.REQUEST_PARAMETER_MAP_NAME, new BaseMapGetter(AppData.REQUEST_PARAMETER_MAP));
      mapNameIndex.put(AppData.REQUEST_ATTRIBUTE_MAP_NAME, new BaseMapGetter(AppData.REQUEST_ATTRIBUTE_MAP));
      mapNameIndex.put(AppData.SESSION_ATTRIBUTE_MAP_NAME, new BaseMapGetter(AppData.SESSION_ATTRIBUTE_MAP));
      mapNameIndex.put(AppData.DATA_MAP_NAME, new BaseMapGetter(AppData.DATA_MAP));
      mapNameIndex.put("param", new BaseMapGetter(AppData.REQUEST_PARAMETER_MAP));
      mapNameIndex.put("attr", new BaseMapGetter(AppData.REQUEST_ATTRIBUTE_MAP));
      mapNameIndex.put("session", new BaseMapGetter(AppData.SESSION_ATTRIBUTE_MAP));
      mapNameIndex.put("RP", new BaseMapGetter(AppData.REQUEST_PARAMETER_MAP));
      mapNameIndex.put("RA", new BaseMapGetter(AppData.REQUEST_ATTRIBUTE_MAP));
      mapNameIndex.put("SA", new BaseMapGetter(AppData.SESSION_ATTRIBUTE_MAP));
      mapNameIndex.put("header", new HeaderGetter());
      mapNameIndex.put("cookie", new CookieGetter());
	}

}