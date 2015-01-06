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

package self.micromagic.eterna.share;

import java.util.Map;

import javax.servlet.http.HttpSession;

/**
 * @deprecated
 * @see self.micromagic.util.container.SessionCache
 */
public class SessionCache
{
	private self.micromagic.util.container.SessionCache cache;

	public SessionCache()
	{
		this.cache = self.micromagic.util.container.SessionCache.getInstance();
	}

	/**
	 * 获得一个SessionCache的实例.
	 */
	public static SessionCache getInstance()
	{
		return new SessionCache();
	}

	public void setProperty(Map saMap, String name, Object property)
	{
		this.cache.setProperty(saMap, name, property);
	}

	public void setProperty(HttpSession session, String name, Object property)
	{
		this.cache.setProperty(session, name, property);
	}

	public Object getProperty(Map saMap, String name)
	{
		return this.cache.getProperty(saMap, name);
	}

	public Object getProperty(HttpSession session, String name)
	{
		return this.cache.getProperty(session, name);
	}

	public void removeProperty(Map saMap, String name)
	{
		this.cache.removeProperty(saMap, name);
	}

	public void removeProperty(HttpSession session, String name)
	{
		this.cache.removeProperty(session, name);
	}

}