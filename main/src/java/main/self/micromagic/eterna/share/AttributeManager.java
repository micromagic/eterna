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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import self.micromagic.util.StringTool;

public class AttributeManager
{
	protected Map attributes = null;

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

}