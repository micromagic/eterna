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

import java.util.AbstractSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import self.micromagic.util.Utility;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;

class ValueContainerMapEntrySet extends AbstractSet
		implements Set
{
	private ValueContainerMap vcm;
	private ValueContainer vContainer;
	private Map entryMap = null;
	private boolean keepEntry = true;

	public ValueContainerMapEntrySet(ValueContainerMap vcm, ValueContainer vContainer)
	{
		if (vContainer == null)
		{
			throw new NullPointerException();
		}
		this.vcm = vcm;
		this.vContainer = vContainer;
	}

	private Map initEntryMap()
	{
		Map result = this.entryMap;
		if (result != null)
		{
			return result;
		}
		result = new HashMap();
		MapEntry entry;
		Enumeration e = this.vContainer.getKeys();
		while (e.hasMoreElements())
		{
			entry = new MapEntry(e.nextElement());
			result.put(entry.getKey(), entry);
		}
		if (this.keepEntry)
		{
			this.entryMap = result;
		}
		return result;
	}

	/**
	 * 判断entry列表是否被初始化了.
	 */
	public boolean isEntryInitialized()
	{
		return this.entryMap != null;
	}

	/**
	 * 是否需要保存entry列表.
	 */
	public boolean isKeepEntry()
	{
		return this.keepEntry;
	}

	/**
	 * 设置是否需要保存entry列表.
	 * 如果不保存, 将在每次使用时生成新的entry列表.
	 */
	public void setKeepEntry(boolean keepEntry)
	{
		this.keepEntry = keepEntry;
	}

	/**
	 * 判断是否包含指定的键值.
	 */
	public boolean containsKey(Object key)
	{
		Map tmpMap = null;
		if ((tmpMap = this.entryMap) == null)
		{
			return this.vcm.containsKey(key);
		}
		else
		{
			return tmpMap.containsKey(key);
		}
	}

	public int size()
	{
		return this.initEntryMap().size();
	}

	public boolean contains(Object o)
	{
		if (o != null && (o instanceof Map.Entry))
		{
			Map.Entry entry = (Map.Entry) o;
			Map tmpMap = null;
			if ((tmpMap = this.entryMap) != null)
			{
				Object value = tmpMap.get(entry.getKey());
				if (value != null)
				{
					return Utility.objectEquals(
							((Map.Entry) value).getValue(), entry.getValue());
				}
			}
			else
			{
				Object value = this.vContainer.getValue(entry.getKey());
				if (value != null)
				{
					return value.equals(entry.getValue());
				}
				else if (entry.getValue() == null)
				{
					return this.vContainer.containsKey(entry.getKey());
				}
			}
		}
		return false;
	}

	public Iterator iterator()
	{
		return new MapEntrySetIterator(this.initEntryMap().values().iterator());
	}

	public boolean add(Object o)
	{
		if (o != null && (o instanceof Map.Entry))
		{
			Map.Entry entry = (Map.Entry) o;
			this.vContainer.setValue(entry.getKey(), entry.getValue());
			return this.addEntry(entry) == null;
		}
		return false;
	}

	protected Object addEntry(Map.Entry entry)
	{
		Map tmpMap;
		if ((tmpMap = this.entryMap) != null)
		{
			return tmpMap.put(entry.getKey(), entry);
		}
		return null;
	}

	public boolean remove(Object o)
	{
		if (o != null && (o instanceof Map.Entry))
		{
			Map.Entry entry = (Map.Entry) o;
			this.vContainer.removeValue(entry.getKey());
			return this.removeEntry(entry) != null;
		}
		return false;
	}

	protected Object removeEntry(Map.Entry entry)
	{
		Map tmpMap = null;
		if ((tmpMap = this.entryMap) != null)
		{
			return tmpMap.remove(entry.getKey());
		}
		return entry;
	}

	private void removeByIterator(Map.Entry entry)
	{
		this.vContainer.removeValue(entry.getKey());
	}

	public Object addValue(Object key, Object value)
	{
		Object oldValue = null;
		if (this.vcm.isLoadOldValue())
		{
			oldValue = this.vContainer.getValue(key);
		}
		if (this.isEntryInitialized())
		{
			MapEntry entry = new MapEntry(key, value);
			this.add(entry);
		}
		else
		{
			this.vContainer.setValue(key, value);
		}
		return oldValue;
	}

	public Object removeValue(Object key)
	{
		Object oldValue = null;
		if (this.vcm.isLoadOldValue())
		{
			oldValue = this.vContainer.getValue(key);
		}
		if (this.isEntryInitialized())
		{
			Map.Entry entry = new MapEntry(key);
			this.remove(entry);
		}
		else
		{
			this.vContainer.removeValue(key);
		}
		return oldValue;
	}

	private class MapEntrySetIterator
			implements Iterator
	{
		private Iterator itr;
		private MapEntry current = null;

		public MapEntrySetIterator(Iterator itr)
		{
			this.itr = itr;
		}

		public boolean hasNext()
		{
			return this.itr.hasNext();
		}

		public Object next()
		{
			this.current = (MapEntry) this.itr.next();
			return this.current;
		}

		public void remove()
		{
			if (this.current == null)
			{
				throw new IllegalStateException();
			}
			ValueContainerMapEntrySet.this.removeByIterator(this.current);
			this.current = null;
			this.itr.remove();
		}

	}

	private class MapEntry
			implements Map.Entry
	{
		private Object key;
		private Object value = null;

		private String toStringValue = null;

		public MapEntry(Object key)
		{
			this.key = key;
		}

		public MapEntry(Object key, Object value)
		{
			this(key);
			this.value = value;
		}

		public Object getKey()
		{
			return this.key;
		}

		public Object getValue()
		{
			if (this.value == null)
			{
				this.value = ValueContainerMapEntrySet.this.vContainer.getValue(this.key);
			}
			return this.value;
		}

		public Object setValue(Object value)
		{
			Object oldValue = this.getValue();
			this.value = value;
			ValueContainerMapEntrySet.this.vContainer.setValue(this.key, value);
			return oldValue;
		}

		public int hashCode()
		{
			Object key = this.getKey();
			Object value = this.getValue();
			return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
		}

		public boolean equals(Object obj)
		{
			if (obj instanceof Map.Entry)
			{
				Object otherKey = ((Map.Entry) obj).getKey();
				Object otherValue = ((Map.Entry) obj).getValue();
				return Utility.objectEquals(this.key, otherKey)
						&& Utility.objectEquals(this.value, otherValue);
			}
			return false;
		}

		public String toString()
		{
			if (this.toStringValue == null)
			{
				StringAppender buf = StringTool.createStringAppender(32);
				buf.append("Entry[key:").append(this.key)
						.append(",value:").append(this.value).append(']');
				this.toStringValue = buf.toString();
			}
			return this.toStringValue;
		}

	}

}