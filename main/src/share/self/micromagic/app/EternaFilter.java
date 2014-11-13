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

package self.micromagic.app;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;

import self.micromagic.eterna.model.AppData;

/**
 * 用于生成AppData的过滤器.
 *
 * @author micromagic@sina.com
 */
public class EternaFilter
		implements Filter
{
	protected FilterConfig config;

	public void init(FilterConfig filterConfig)
	{
		this.config = filterConfig;
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
			throws IOException, ServletException
	{
		AppData data = AppData.getCurrentData();
		if (request instanceof HttpServletRequest)
		{
			data.contextRoot = ((HttpServletRequest) request).getContextPath();
		}

		if (filterChain != null)
		{
			String oldModelName = data.modelName;
			ServletRequest oldRequest = data.request;
			ServletResponse oldResponse = data.response;
			FilterConfig oldConfig = data.filterConfig;
			data.request = request;
			data.response = response;
			data.filterConfig = this.config;
			data.position = AppData.POSITION_FILTER;
			data.export = null;
			Throwable err = null;
			try
			{
				this.beginChain(data);
				filterChain.doFilter(data.request, data.response);
			}
			catch (Throwable ex)
			{
				err = ex;
				if (ex instanceof IOException)
				{
					throw (IOException) ex;
				}
				else if (ex instanceof ServletException)
				{
					throw (ServletException) ex;
				}
				else if (ex instanceof RuntimeException)
				{
					throw (RuntimeException) ex;
				}
				else if (ex instanceof Error)
				{
					throw (Error) ex;
				}
			}
			finally
			{
				try
				{
					this.endChain(data, err);
				}
				catch (Throwable ex) {}
				data.export = null;
				data.request = oldRequest;
				data.response = oldResponse;
				data.filterConfig = oldConfig;
				data.modelName = oldModelName;
				if (oldRequest == null && oldResponse == null)
				{
					data.clearData();
				}
			}
		}
	}

	/**
	 * 开始调用过滤链表时会调用此方法.
	 */
	protected void beginChain(AppData data)
			throws IOException, ServletException
	{
	}

	/**
	 * 过滤链表调用完成后会调用此方法.
	 *
	 * @deprecated
	 * @see #endChain(AppData, Throwable)
	 */
	protected void endChain(AppData data)
	{
	}

	/**
	 * 过滤链表调用完成后会调用此方法.
	 */
	protected void endChain(AppData data, Throwable err)
			throws IOException, ServletException
	{
		if (err != null)
		{
			WebApp.log.error("Error in EternaFilter.", err);
		}
		this.endChain(data);
	}

	public void destroy()
	{
	}

}