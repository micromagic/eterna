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

package self.micromagic.eterna.model;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import self.micromagic.util.container.CookieContainer;
import self.micromagic.util.container.RequestHeaderContainer;
import self.micromagic.util.container.ValueContainerMap;

/**
 * 用于获取AppData中各类map的工具.
 */
public interface MapGetter
{
	/**
	 * 获取对应的map对象.
	 */
	Map getMap(AppData data);

	/**
	 * 是否需要初始化.
	 * 即在初始化时, 是否需要生成一个新的对象.
	 */
	boolean needInit();

	/**
	 * 初始化并生成一个新的对象.
	 * 如果不需要初始化, 则返回对象本身.
	 *
	 * @see #needInit()
	 */
	MapGetter init();

	/**
	 * 在AppData中存放特殊的map的标签.
	 */
	String SPECIAL_MAP_FLAG = "appData.special.map";

}

/**
 * 用于获取基本的param, attr, session, data这4个map.
 */
class BaseMapGetter
		implements MapGetter
{
	private final int mapIndex;
	public BaseMapGetter(int mapIndex)
	{
		this.mapIndex = mapIndex;
	}

	public Map getMap(AppData data)
	{
		return data.maps[this.mapIndex];
	}

	public boolean needInit()
	{
		return false;
	}

	public MapGetter init()
	{
		return this;
	}

}

/**
 * 用于获取header.
 */
class HeaderGetter
		implements MapGetter
{
	public Map getMap(AppData data)
	{
		Map r = data.maps[AppData.HEADER_MAP];
		if (r == null)
		{
			HttpServletRequest req = data.getHttpServletRequest();
			if (req == null)
			{
				r = new HashMap();
			}
			else
			{
				RequestHeaderContainer c = new RequestHeaderContainer(
						req, data.getHttpServletResponse());
				r = ValueContainerMap.create(c);
			}
			data.maps[AppData.HEADER_MAP] = r;
		}
		return r;
	}

	public boolean needInit()
	{
		return false;
	}

	public MapGetter init()
	{
		return this;
	}

}

/**
 * 用于获取cookie.
 */
class CookieGetter
		implements MapGetter
{
	public Map getMap(AppData data)
	{
		Map r = data.maps[AppData.COOKIE_MAP];
		if (r == null)
		{
			HttpServletRequest req = data.getHttpServletRequest();
			if (req == null)
			{
				r = new HashMap();
			}
			else
			{
				CookieContainer c = new CookieContainer(
						req, data.getHttpServletResponse());
				r = ValueContainerMap.create(c);
			}
			data.maps[AppData.COOKIE_MAP] = r;
		}
		return r;
	}

	public boolean needInit()
	{
		return false;
	}

	public MapGetter init()
	{
		return this;
	}

}
