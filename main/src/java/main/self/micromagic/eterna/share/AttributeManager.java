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

package self.micromagic.eterna.share;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import self.micromagic.util.StringTool;

public class AttributeManager
{
	protected Map attributes = null;

	/**
	 * 对属性的类型进行转换.
	 *
	 * @param factory  属性所在的工厂
	 * @param objType  属性所在对象的类型
	 */
	public void convertType(EternaFactory factory, String objType)
	{
		if (this.attributes == null)
		{
			return;
		}
		Iterator itr = this.attributes.entrySet().iterator();
		while (itr.hasNext())
		{
			Map.Entry e = (Map.Entry) itr.next();
			e.setValue(Tool.transAttrType(factory, objType,
					(String) e.getKey(), e.getValue()));
		}
	}

	public Object getAttribute(String name)
	{
		if (this.attributes == null)
		{
			return null;
		}
		return this.attributes.get(name);
	}

	public String[] getAttributeNames()
	{
		if (this.attributes == null)
		{
			return StringTool.EMPTY_STRING_ARRAY;
		}
		Set keys = this.attributes.keySet();
		return (String[]) keys.toArray(new String[keys.size()]);
	}

	public Set attributeEntrySet()
	{
		if (this.attributes == null)
		{
			return Collections.EMPTY_SET;
		}
		return this.attributes.entrySet();
	}

	public Object setAttribute(String name, Object value)
	{
		if (this.attributes == null)
		{
			synchronized (this)
			{
				if (this.attributes == null)
				{
					this.attributes = new HashMap();
				}
			}
		}
		return this.attributes.put(name, value);
	}

	public Object removeAttribute(String name)
	{
		if (this.attributes == null)
		{
			return null;
		}
		return this.attributes.remove(name);
	}

	public boolean hasAttribute(String name)
	{
		if (this.attributes == null)
		{
			return false;
		}
		return this.attributes.containsKey(name);
	}

	public int size()
	{
		if (this.attributes == null)
		{
			return 0;
		}
		return this.attributes.size();
	}

}