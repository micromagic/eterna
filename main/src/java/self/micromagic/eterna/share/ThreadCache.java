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

/**
 * @deprecated
 * @see self.micromagic.util.container.ThreadCache
 */
public class ThreadCache
{
	private self.micromagic.util.container.ThreadCache cache;

	private ThreadCache()
	{
		this.cache = self.micromagic.util.container.ThreadCache.getInstance();
	}

	/**
	 * 获得一个ThreadCache的实例.
	 */
	public static ThreadCache getInstance()
	{
		return new ThreadCache();
	}

	public void setProperty(String name, Object property)
	{
		this.cache.setProperty(name, property);
	}

	public Object getProperty(String name)
	{
		return this.cache.getProperty(name);
	}

	public void removeProperty(String name)
	{
		this.cache.removeProperty(name);
	}

	public void clearPropertys()
	{
		this.cache.clearPropertys();
	}

	public static void clearAllPropertys()
	{
		self.micromagic.util.container.ThreadCache.clearAllPropertys();
	}

}