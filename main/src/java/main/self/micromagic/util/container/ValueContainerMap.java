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

import java.util.AbstractMap;
import java.util.Map;
import java.util.Enumeration;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.AppDataLogExecute;
import self.micromagic.eterna.sql.ResultRow;
import self.micromagic.eterna.sql.ResultMetaData;
import self.micromagic.eterna.share.EternaException;
import org.apache.commons.collections.iterators.IteratorEnumeration;

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

	/**
	 * @deprecated
	 * @see #createSessionAttributeMap(HttpServletRequest)
	 */
	public static ValueContainerMap createSessionAttributeMap(HttpSession session)
	{
		if (session == null)
		{
			return null;
		}
		ValueContainer vContainer = new SessionAttributeContainer(session);
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


	public static class ResultRowContainer
			implements ValueContainer
	{
		private ResultRow row;

		public ResultRowContainer(ResultRow row)
		{
			this.row = row;
		}

		public boolean equals(Object o)
		{
			if (this == o)
			{
				return true;
			}
			if (o instanceof ResultRowContainer)
			{
				ResultRowContainer other = (ResultRowContainer) o;
				return this.row.equals(other.row);
			}
			return false;
		}

		public int hashCode()
		{
			return this.row.hashCode();
		}

		public Object getValue(Object key)
		{
			try
			{
				return this.row.getSmartValue(key == null ? null : key.toString(), true);
			}
			catch (SQLException ex)
			{
				return null;
			}
			catch (EternaException ex)
			{
				return null;
			}
		}

		public boolean containsKey(Object key)
		{
			try
			{
				return this.row.findColumn(key == null ? null : key.toString(), true) != -1;
			}
			catch (SQLException ex)
			{
				return false;
			}
			catch (EternaException ex)
			{
				return false;
			}
		}

		public void setValue(Object key, Object value)
		{
			throw new UnsupportedOperationException();
		}

		public void removeValue(Object key)
		{
			throw new UnsupportedOperationException();
		}

		public Enumeration getKeys()
		{
			try
			{
				ResultMetaData rmd = this.row.getResultIterator().getMetaData();
				int count = rmd.getColumnCount();
				List names = new ArrayList(rmd.getColumnCount());
				for (int i = 0; i < count; i++)
				{
					names.add(rmd.getColumnReader(i + 1).getName());
				}
				return new IteratorEnumeration(names.iterator());
			}
			catch (SQLException ex)
			{
				return null;
			}
			catch (EternaException ex)
			{
				return null;
			}
		}

	}

	public static class RequestAttributeContainer
			implements ValueContainer
	{
		private ServletRequest request;

		public RequestAttributeContainer(ServletRequest request)
		{
			this.request = request;
		}

		public boolean equals(Object o)
		{
			if (this == o)
			{
				return true;
			}
			if (o instanceof RequestAttributeContainer)
			{
				RequestAttributeContainer other = (RequestAttributeContainer) o;
				return this.request.equals(other.request);
			}
			return false;
		}

		public int hashCode()
		{
			return this.request.hashCode();
		}

		public Object getValue(Object key)
		{
			return this.request.getAttribute(key == null ? null : key.toString());
		}

		public boolean containsKey(Object key)
		{
			return this.getValue(key) != null;
		}

		public void setValue(Object key, Object value)
		{
			this.request.setAttribute(key == null ? null : key.toString(), value);
		}

		public void removeValue(Object key)
		{
			this.request.removeAttribute(key == null ? null : key.toString());
		}

		public Enumeration getKeys()
		{
			return this.request.getAttributeNames();
		}

	}

	public static class SessionAttributeContainer
			implements ValueContainer
	{
		private HttpServletRequest request;
		private HttpSession session;

		public SessionAttributeContainer(HttpSession session)
		{
			this.session = session;
		}

		public SessionAttributeContainer(HttpServletRequest request)
		{
			this.request = request;
		}

		public boolean equals(Object o)
		{
			if (this == o)
			{
				return true;
			}
			if (o instanceof SessionAttributeContainer)
			{
				SessionAttributeContainer other = (SessionAttributeContainer) o;
				if (this.request != null && this.request == other.request)
				{
					return true;
				}
				return this.session == other.session;
			}
			return false;
		}

		public int hashCode()
		{
			if (!this.checkSession(false))
			{
				return 0;
			}
			return this.session.hashCode();
		}

		public Object getValue(Object key)
		{
			if (!this.checkSession(false))
			{
				return null;
			}
			return this.session.getAttribute(key == null ? null : key.toString());
		}

		public boolean containsKey(Object key)
		{
			return this.getValue(key) != null;
		}

		public void setValue(Object key, Object value)
		{
			if (this.checkSession(value != null))
			{
				this.session.setAttribute(key == null ? null : key.toString(), value);
			}
		}

		public void removeValue(Object key)
		{
			if (this.checkSession(false))
			{
				this.session.removeAttribute(key == null ? null : key.toString());
			}
		}

		public Enumeration getKeys()
		{
			if (!this.checkSession(false))
			{
				return UnmodifiableIterator.EMPTY_ENUMERATION;
			}
			return this.session.getAttributeNames();
		}

		private boolean checkSession(boolean create)
		{
			if (this.session != null)
			{
				return true;
			}
			if (this.request == null)
			{
				return false;
			}
			try
			{
				this.session = this.request.getSession(create);
			}
			catch (Exception ex)
			{
				// 创建session时可能会出错, 比如已经提交了应答之后
				if (AppDataLogExecute.getAppLogType() > 0)
				{
					AppData.log.warn("Error in create session.", ex);
				}
			}
			return this.session != null;
		}

	}

	public static class ApplicationAttributeContainer
			implements ValueContainer
	{
		private ServletContext context;

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

	}

}