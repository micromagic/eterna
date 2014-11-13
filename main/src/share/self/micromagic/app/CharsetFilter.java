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
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import self.micromagic.util.Utility;

/**
 * 设置编码格式及内容类型的过滤器.
 *
 * 可设置的参数如下:
 *
 * charset            使用的字符集, 默认值为: UTF-8
 *
 * contentType        输出的内容类型, 默认值为: text/html
 *
 * forceSet           当已经设置过编码格式时, 是否还要继续设置
 *                    默认值为: false
 *
 */
public class CharsetFilter
		implements Filter, WebApp
{
	private String charset = "UTF-8";
	private String contentType = "text/html";
	private boolean forceSet = false;

	public void init(FilterConfig filterConfig)
			throws ServletException
	{
		String temp = filterConfig.getInitParameter("charset");
		if (temp != null)
		{
			this.charset = temp;
		}
		else
		{
			this.charset = getConfigCharset(this.charset);
		}
		temp = filterConfig.getInitParameter("contentType");
		if (temp != null)
		{
			this.contentType = temp;
		}
		temp = filterConfig.getInitParameter("forceSet");
		if (temp != null)
		{
			this.forceSet = temp.equalsIgnoreCase("true");
		}
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException
	{
		if (this.forceSet || request.getCharacterEncoding() == null)
		{
			request.setCharacterEncoding(this.charset);
			if (this.contentType.startsWith("text/"))
			{
				response.setContentType(this.contentType + ";charset=" + this.charset);
			}
			else
			{
				response.setContentType(this.contentType);
			}
		}
		chain.doFilter(request, response);
	}

	public void destroy()
	{
	}

	/**
	 * 从配置中获取编码格式.
	 *
	 * @param defaultValue  当配置中不存在编码格式设置时, 使用此默认值
	 */
	public static String getConfigCharset(String defaultValue)
	{
		String charset = Utility.getProperty(Utility.CHARSET_TAG);
		return charset == null ? defaultValue : charset;
	}

}