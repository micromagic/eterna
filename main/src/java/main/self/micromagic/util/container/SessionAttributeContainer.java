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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.AppDataLogExecute;

/**
 * session中属性信息的容器.
 */
public class SessionAttributeContainer
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

	public Object[] getKeyValuePairs()
	{
		return null;
	}

	/**
	 * 检查session是否有效, 如果session失效了则清空并重新获取.
	 */
	public void checkValid()
	{
		if (this.session != null && this.request != null
				&& this.session != this.request.getSession(false))
		{
			this.session = null;
		}
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
