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

import java.util.Enumeration;

import javax.servlet.ServletContext;

/**
 * servlet context 中属性信息的容器.
 */
public class ApplicationAttributeContainer
		implements ValueContainer
{
	private final ServletContext context;

	public ApplicationAttributeContainer(ServletContext context)
	{
		this.context = context;
	}

	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o instanceof ApplicationAttributeContainer)
		{
			ApplicationAttributeContainer other = (ApplicationAttributeContainer) o;
			return this.context.equals(other.context);
		}
		return false;
	}

	public int hashCode()
	{
		return this.context.hashCode();
	}

	public Object getValue(Object key)
	{
		return this.context.getAttribute(key == null ? null : key.toString());
	}

	public boolean containsKey(Object key)
	{
		return this.getValue(key) != null;
	}

	public void setValue(Object key, Object value)
	{
		this.context.setAttribute(key == null ? null : key.toString(), value);
	}

	public void removeValue(Object key)
	{
		this.context.removeAttribute(key == null ? null : key.toString());
	}

	public Enumeration getKeys()
	{
		return this.context.getAttributeNames();
	}

	public Object[] getKeyValuePairs()
	{
		return null;
	}

}
