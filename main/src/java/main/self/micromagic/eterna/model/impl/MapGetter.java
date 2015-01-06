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

import self.micromagic.eterna.model.AppData;
import self.micromagic.util.container.ValueContainerMap;
import self.micromagic.util.container.RequestHeaderContainer;
import self.micromagic.util.container.CookieContainer;

/**
 * 用于获取AppData中各类map的工具.
 */
public interface MapGetter
{
	Map getMap(AppData data);

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
	private int mapIndex;
	public BaseMapGetter(int mapIndex)
	{
		this.mapIndex = mapIndex;
	}

	public Map getMap(AppData data)
	{
		return data.maps[this.mapIndex];
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
		Map r = (Map) data.getSpcialData(SPECIAL_MAP_FLAG, "header");
		if (r == null)
		{
			if (data.getHttpServletRequest() == null)
			{
				r = new HashMap();
			}
			else
			{
				r = ValueContainerMap.create(
						new RequestHeaderContainer(data.getHttpServletRequest(), data.getHttpServletResponse()));
			}
			data.addSpcialData(SPECIAL_MAP_FLAG, "header", r);
		}
		return r;
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
		Map r = (Map) data.getSpcialData(SPECIAL_MAP_FLAG, "cookie");
		if (r == null)
		{
			if (data.getHttpServletRequest() == null)
			{
				r = new HashMap();
			}
			else
			{
				r = ValueContainerMap.create(
						new CookieContainer(data.getHttpServletRequest(), data.getHttpServletResponse()));
			}
			data.addSpcialData(SPECIAL_MAP_FLAG, "cookie", r);
		}
		return r;
	}

}