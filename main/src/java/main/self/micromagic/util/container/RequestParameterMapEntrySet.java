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

package self.micromagic.util.container;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;

import self.micromagic.util.Utility;
import self.micromagic.util.StringTool;
import self.micromagic.util.StringAppender;

class RequestParameterMapEntrySet extends AbstractSet
{
	private RequestParameterMap rpMap;
	private boolean onlyValue;

	public RequestParameterMapEntrySet(RequestParameterMap rpMap, boolean onlyValue)
	{
		this.rpMap = rpMap;
		this.onlyValue = onlyValue;
	}

	public int size()
	{
		return this.rpMap.size();
	}

	public boolean contains(Object o)
	{
		if (this.onlyValue)
		{
			super.contains(o);
		}
		if (o != null && (o instanceof Map.Entry))
		{
			Map.Entry entry = (Map.Entry) o;
			Object value = this.rpMap.get(entry.getKey());
			return Utility.objectEquals(value, entry.getValue());
		}
		return false;
	}

	public Iterator iterator()
	{
		return new MapEntrySetIterator(this.rpMap.realEntrySet().iterator(), this.onlyValue);
	}

	public boolean add(Object o)
	{
		if (this.onlyValue)
		{
			throw new UnsupportedOperationException();
		}
		Map.Entry entry = (Map.Entry) o;
		return this.rpMap.put(entry.getKey(), entry.getValue()) == null;
	}

	private class MapEntrySetIterator
			implements Iterator
	{
		private Iterator itr;
		private boolean onlyValue;

		public MapEntrySetIterator(Iterator itr, boolean onlyValue)
		{
			this.itr = itr;
			this.onlyValue = onlyValue;
		}

		public boolean hasNext()
		{
			return this.itr.hasNext();
		}

		public Object next()
		{
			if (this.onlyValue)
			{
				return RequestParameterMap.getFirstParam(
						((Map.Entry) this.itr.next()).getValue());
			}
			return new MapEntry((Map.Entry) this.itr.next());
		}

		public void remove()
		{
			this.itr.remove();
		}

	}

	private class MapEntry
			implements Map.Entry
	{
		private Map.Entry entry;
		private String toStringValue;

		public MapEntry(Map.Entry entry)
		{
			this.entry = entry;
		}

		public Object getKey()
		{
			return this.entry.getKey();
		}

		public Object getValue()
		{
			return RequestParameterMap.getFirstParam(this.entry.getValue());
		}

		public Object setValue(Object value)
		{
			Object oldValue = this.getValue();
			this.entry.setValue(value);
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
				return Utility.objectEquals(this.getKey(), otherKey)
						&& Utility.objectEquals(this.getValue(), otherValue);
			}
			return false;
		}

		public String toString()
		{
			if (this.toStringValue == null)
			{
				StringAppender buf = StringTool.createStringAppender(32);
				buf.append("Entry[key:").append(this.getKey())
						.append(",value:").append(this.getValue()).append(']');
				this.toStringValue = buf.toString();
			}
			return this.toStringValue;
		}

	}

}