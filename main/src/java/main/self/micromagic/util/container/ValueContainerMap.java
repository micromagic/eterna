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

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import self.micromagic.eterna.dao.ResultRow;

/**
 * 注: 使用时需注意, 如果没有通过ValueContainerMap而是直接对
 * Attribute设置或删除值的话, 那再访问ValueContainerMap的
 * values和keySet的话会造成数据不一致
 */
public class ValueContainerMap extends AbstractMap
		implements Map
{
	private ValueContainerMapEntrySet vEntrySet;
	private ValueContainer vContainer;

	/**
	 * 在执行put和remove时, 是否要读取原始值.
	 * <code>true</code>为需要读取原始值.
	 */
	private boolean loadOldValue = false;

	private ValueContainerMap(ValueContainer vContainer)
	{
		this.vContainer = vContainer;
		this.vEntrySet = new ValueContainerMapEntrySet(this, this.vContainer);
	}

	public static ValueContainerMap create(ValueContainer vContainer)
	{
		if (vContainer == null)
		{
			return null;
		}
		return new ValueContainerMap(vContainer);
	}

	public static ValueContainerMap createResultRowMap(ResultRow row)
	{
		if (row == null)
		{
			return null;
		}
		ValueContainer vContainer = new ResultRowContainer(row);
		return new ValueContainerMap(vContainer);
	}


	public static ValueContainerMap createRequestAttributeMap(ServletRequest request)
	{
		if (request == null)
		{
			return null;
		}
		ValueContainer vContainer = new RequestAttributeContainer(request);
		return new ValueContainerMap(vContainer);
	}

	public static ValueContainerMap createSessionAttributeMap(HttpServletRequest request)
	{
		if (request == null)
		{
			return null;
		}
		ValueContainer vContainer = new SessionAttributeContainer(request);
		return new ValueContainerMap(vContainer);
	}

	public static ValueContainerMap createApplicationAttributeMap(ServletContext context)
	{
		if (context == null)
		{
			return null;
		}
		ValueContainer vContainer = new ApplicationAttributeContainer(context);
		return new ValueContainerMap(vContainer);
	}

	/**
	 * 在执行put和remove时, 是否要读取原始值.
	 */
	public boolean isLoadOldValue()
	{
		return loadOldValue;
	}

	/**
	 * 设置在执行put和remove时, 是否要读取原始值.
	 */
	public void setLoadOldValue(boolean loadOldValue)
	{
		this.loadOldValue = loadOldValue;
	}

	/**
	 * 是否需要保存entry列表.
	 */
	public boolean isKeepEntry()
	{
		return this.vEntrySet.isKeepEntry();
	}

	/**
	 * 设置是否需要保存entry列表.
	 * 如果不保存, 将在每次使用时生成新的entry列表.
	 * 默认值为: true.
	 */
	public void setKeepEntry(boolean keepEntry)
	{
		this.vEntrySet.setKeepEntry(keepEntry);
	}

	/**
	 * 切换当前的数据容器.
	 * 当数据容器发生改变时, 可以通过此方法切换成新的数据容器.
	 */
	public void changeContainer(ValueContainer vContainer)
	{
		if (vContainer != null && vContainer != this.vContainer)
		{
			this.vContainer = vContainer;
			this.vEntrySet = new ValueContainerMapEntrySet(this, this.vContainer);
		}
	}

	public boolean containsKey(Object key)
	{
		return this.vEntrySet.containsKey(key);
	}

	public Object get(Object key)
	{
		return this.vContainer.getValue(key);
	}

	public Object put(Object key, Object value)
	{
		return this.vEntrySet.addValue(key, value);
	}

	public Object remove(Object key)
	{
		return this.vEntrySet.removeValue(key);
	}

	public Set entrySet()
	{
		return this.vEntrySet;
	}

}
