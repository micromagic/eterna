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

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpSession;

/**
 * 管理session中的属性. <p>
 * 其主要作用就是将存入的对象包装成<code>Property</code>, 当需要序列化时,
 * 会检查被包装的对象是否可序列化, 如果是无法被序列化的对象, 则不会对其进
 * 行序列化, 在反序列化时将其作为null.
 *
 * @author micromagic@sina.com
 */
public class SessionCache
{
	private static SessionCache cache = new SessionCache();
	private static int globalVersion = 0;

	private SessionCache()
	{
	}

	/**
	 * 获得一个SessionCache的实例.
	 */
	public static SessionCache getInstance()
	{
		return cache;
	}

	/**
	 * 向session中设置属性.
	 * 如果是portlet, 可以通过此方法设置session的属性.
	 *
	 * @param saMap      被转换成map的session
	 * @param name       要设置的属性的名称
	 * @param property   要设置的属性值
	 * @see ValueContainerMap#createSessionAttributeMap(javax.servlet.http.HttpServletRequest)
	 */
	public void setProperty(Map saMap, String name, Object property)
	{
		saMap.put(name, new PropertyImpl(globalVersion, property));
	}

	/**
	 * 向session中设置属性.
	 *
	 * @param session    被操作的session对象
	 * @param name       要设置的属性的名称
	 * @param property   要设置的属性值
	 */
	public void setProperty(HttpSession session, String name, Object property)
	{
		session.setAttribute(name, new PropertyImpl(globalVersion, property));
	}

	/**
	 * 从session中获取属性值.
	 * 如果是portlet, 可以通过此方法获取session的属性.
	 *
	 * @param saMap      被转换成map的session
	 * @param name       要获取的属性的名称
	 * @see ValueContainerMap#createSessionAttributeMap(javax.servlet.http.HttpServletRequest)
	 */
	public Object getProperty(Map saMap, String name)
	{
		Object obj = saMap.get(name);
		if (obj != null && obj instanceof Property)
		{
			Property p = (Property) obj;
			if (p.getPropertyVersion() == globalVersion)
			{
				return ((Property) obj).getValue();
			}
			else
			{
				saMap.remove(name);
				return null;
			}
		}
		return obj;
	}

	/**
	 * 从session中获取属性值.
	 *
	 * @param session    被操作的session对象
	 * @param name       要获取的属性的名称
	 */
	public Object getProperty(HttpSession session, String name)
	{
		Object obj = session.getAttribute(name);
		if (obj != null && obj instanceof Property)
		{
			Property p = (Property) obj;
			if (p.getPropertyVersion() == globalVersion)
			{
				return ((Property) obj).getValue();
			}
			else
			{
				session.removeAttribute(name);
				return null;
			}
		}
		return obj;
	}

	/**
	 * 从session中移除一个属性.
	 * 如果是portlet, 可以通过此方法移除session的属性.
	 *
	 * @param saMap      被转换成map的session
	 * @param name       要移除的属性的名称
	 * @see ValueContainerMap#createSessionAttributeMap(javax.servlet.http.HttpServletRequest)
	 */
	public void removeProperty(Map saMap, String name)
	{
		saMap.remove(name);
	}

	/**
	 * 从session中移除一个属性.
	 *
	 * @param session    被操作的session对象
	 * @param name       要移除的属性的名称
	 */
	public void removeProperty(HttpSession session, String name)
	{
		session.removeAttribute(name);
	}

	/**
	 * 清空所有session中的属性值.
	 */
	public static void clearAllPropertys()
	{
		globalVersion++;
	}

	/**
	 * 通过SessionCache存放到session中的对象.
	 */
	public interface Property
	{
		/**
		 * 获取属性的原始值.
		 */
		Object getValue();

		/**
		 * 获取属性的版本号.
		 */
		int getPropertyVersion();

	}

	private static class PropertyImpl
			implements Property, Serializable
	{
		private final int propertyVersion;
		private transient Object value;

		public PropertyImpl(int propertyVersion, Object value)
		{
			this.propertyVersion = propertyVersion;
			this.value = value;
		}

		public int getPropertyVersion()
		{
			return this.propertyVersion;
		}

		public Object getValue()
		{
			return this.value;
		}

		private void writeObject(java.io.ObjectOutputStream s)
				throws IOException
		{
			s.defaultWriteObject();
			if (value != null && value instanceof Serializable)
			{
				Iterator itr = null;
				if (value instanceof Map)
				{
					itr = ((Map) value).values().iterator();
				}
				else if (value instanceof Collection)
				{
					itr = ((Collection) value).iterator();
				}
				if (itr != null)
				{
					while (itr.hasNext())
					{
						if (!(itr.next() instanceof Serializable))
						{
							s.writeBoolean(false);
							return;
						}
					}
				}
				s.writeBoolean(true);
				s.writeObject(this.value);
			}
			else
			{
				s.writeBoolean(false);
			}
		}

		private void readObject(java.io.ObjectInputStream s)
				throws IOException, ClassNotFoundException
		{
			s.defaultReadObject();
			boolean canSerialize = s.readBoolean();
			this.value = canSerialize ? s.readObject() : null;
		}

		private static final long serialVersionUID = 1L;

	}

}