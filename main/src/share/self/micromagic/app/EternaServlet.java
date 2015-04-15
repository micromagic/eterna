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
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import self.micromagic.eterna.digester.FactoryManager;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.ModelCaller;
import self.micromagic.eterna.model.ModelExport;
import self.micromagic.eterna.view.ViewAdapter;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.container.ValueContainerMap;
import self.micromagic.util.container.RequestParameterMap;

/**
 * 用于加载eterna配置的servlet.
 *
 * 可设置的参数如下:
 *
 * defaultModel       当没有指定model名称时, 调用此属性指定的默认的model
 *                    默认值为: ModelCaller.DEFAULT_MODEL_NAME
 *
 * initFiles          需要加载的eterna的配置文件列表
 *
 * parentFiles        需要加载的eterna父配置文件列表
 *
 * charset            使用的字符集, 默认值为: UTF-8
 *
 *
 * @see ModelCaller#DEFAULT_MODEL_NAME
 * @see FactoryManager#CONFIG_INIT_FILES
 * @see FactoryManager#CONFIG_INIT_PARENTFILES
 *
 * @author micromagic@sina.com
 */
public class EternaServlet extends HttpServlet
		implements WebApp
{
	protected FactoryManager.Instance factoryManager;
	protected String defaultModel = ModelCaller.DEFAULT_MODEL_NAME;
	private String charset = "UTF-8";
	protected boolean initFactoryManager = true;
	protected boolean checkMultipart;

	public void init(ServletConfig config)
			throws ServletException
	{
		super.init(config);
		if (this.initFactoryManager)
		{
			String initFiles = config.getInitParameter(FactoryManager.CONFIG_INIT_FILES);
			String parentFiles = config.getInitParameter(FactoryManager.CONFIG_INIT_PARENTFILES);
			this.factoryManager = FactoryManager.createClassFactoryManager(
					this.getClass(), this, initFiles,
					parentFiles == null ? null : new String[]{parentFiles}, false);
		}
		String tmp = config.getInitParameter(DEFAULT_MODEL_TAG);
		if (tmp != null)
		{
			this.defaultModel = config.getInitParameter(DEFAULT_MODEL_TAG);
		}

		String temp = config.getInitParameter("charset");
		if (temp != null)
		{
			this.charset = temp;
		}
		else
		{
			this.charset = CharsetFilter.getConfigCharset(this.charset);
		}

		this.checkMultipart = "true".equalsIgnoreCase(config.getInitParameter("checkMultipart"));
	}

	protected FactoryManager.Instance getFactoryManager()
			throws EternaException
	{
		return this.factoryManager;
	}

	public static String getServerRoot(HttpServletRequest request)
	{
		HttpSession session = request.getSession();
		String serverRoot = (String) session.getAttribute(SERVER_ROOT_TAG);
		if (serverRoot == null)
		{
			serverRoot = request.getScheme() + "://" + request.getServerName()
					+ ":" + request.getServerPort() + request.getContextPath();
			session.setAttribute(SERVER_ROOT_TAG, serverRoot);
		}
		return serverRoot;
	}

	/**
	 * 检查提交的请求是否为multipart.
	 */
	public static boolean isMultipartContent(HttpServletRequest request)
	{
		if (!"post".equals(request.getMethod().toLowerCase()))
		{
			return false;
		}
		String contentType = request.getContentType();
		if (contentType == null)
		{
			return false;
		}
		if (contentType.toLowerCase().startsWith("multipart/"))
		{
			return true;
		}
		return false;
	}

	/**
	 * 将请求的URL中的参数字符串解析成map对象.
	 */
	public static Map parseQueryString(String queryString, String charset)
			throws IOException
	{
		if (StringTool.isEmpty(queryString))
		{
			return Collections.EMPTY_MAP;
		}
		String[] items = StringTool.separateString(queryString, "&");
		Map params = new HashMap();
		for (int i = 0; i < items.length; i++)
		{
			String item = items[i];
			if (item.length() == 0)
			{
				continue;
			}
			int index = item.indexOf('=');
			String key, value;
			if (index == -1)
			{
				key = URLDecoder.decode(item, charset);
				value = "";
			}
			else
			{
				key = URLDecoder.decode(item.substring(0, index), charset);
				value = URLDecoder.decode(item.substring(index + 1), charset);
			}
			String[] values = (String[]) params.get(key);
			if (values == null)
			{
				params.put(key, new String[]{value});
			}
			else
			{
				String[] newValues = new String[values.length + 1];
				newValues[newValues.length - 1] = value;
				System.arraycopy(values, 0, newValues, 0, values.length);
				params.put(key, newValues);
			}
		}
		return params;
	}

	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		if (!this.charset.equals(request.getCharacterEncoding()))
		{
			request.setCharacterEncoding(this.charset);
		}
		response.setContentType("text/html;charset=" + this.charset);
		AppData data = AppData.getCurrentData();
		data.request = request;
		data.response = response;
		data.contextRoot = request.getContextPath();
		data.servletConfig = this.getServletConfig();
		data.position = AppData.POSITION_SERVLET;
		try
		{
			String queryStr = request.getQueryString();
			if (this.checkMultipart && isMultipartContent(request))
			{
				data.maps[AppData.REQUEST_PARAMETER_MAP]
						= RequestParameterMap.create(parseQueryString(queryStr, this.charset));
			}
			else
			{
				data.maps[AppData.REQUEST_PARAMETER_MAP] = RequestParameterMap.create(request);
			}
			data.maps[AppData.REQUEST_ATTRIBUTE_MAP] = ValueContainerMap.createRequestAttributeMap(request);
			data.maps[AppData.SESSION_ATTRIBUTE_MAP] = ValueContainerMap.createSessionAttributeMap(request);
			if (queryStr != null)
			{
				String modelNameTag = this.getFactoryManager().getEternaFactory().getModelNameTag();
				int index;
				int plusCount = 2;
				if (queryStr.length() > 0 && queryStr.charAt(0) != '?')
				{
					if (queryStr.startsWith(modelNameTag + "="))
					{
						index = 0;
						plusCount = 1;
					}
					else
					{
						index = -1;
					}
				}
				else
				{
					index = queryStr.indexOf("?" + modelNameTag + "=");
				}
				if (index == -1)
				{
					index = queryStr.indexOf("&" + modelNameTag + "=");
				}
				if (index != -1)
				{
					int endIndex = queryStr.indexOf('&', index + 1);
					if (endIndex != -1)
					{
						data.modelName = queryStr.substring(index + modelNameTag.length() + plusCount, endIndex);
					}
					else
					{
						data.modelName = queryStr.substring(index + modelNameTag.length() + plusCount);
					}
				}
			}

			request.setAttribute(ModelCaller.DEFAULT_MODEL_TAG, this.defaultModel);
			ModelExport export = this.getFactoryManager().getEternaFactory().getModelCaller().callModel(data);
			if (export != null)
			{
				data.export = export;
			}
		}
		catch (EternaException ex)
		{
			log.warn("Error in service.", ex);
		}
		catch (SQLException ex)
		{
			log.warn("SQL Error in service.", ex);
		}
		catch (Throwable ex)
		{
			log.error("Other Error in service.", ex);
		}
		finally
		{
			try
			{
				if (data.export != null)
				{
					this.doExport(data, request, response);
				}
			}
			catch (Throwable ex)
			{
				log.error("Error in doExport.", ex);
			}
			data.modelName = null;
			data.export = null;
			data.servletConfig = null;
			data.clearData();
		}
	}

	protected void doExport(AppData data, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, EternaException
	{
		if (data.export == null)
		{
			return;
		}
		ModelExport export = data.export;
		if (export.isRedirect())
		{
			if (export.getModelName() != null)
			{
				StringAppender url = StringTool.createStringAppender(512);
				url.append(request.getScheme()).append("://").append(request.getHeader("host"));
				url.append(request.getContextPath()).append(request.getServletPath());
				url.append('?').append(this.getFactoryManager().getEternaFactory().getModelNameTag());
				url.append('=').append(URLEncoder.encode(export.getModelName(), this.charset));
				url.append('&');
				url.append(this.getFactoryManager().getEternaFactory().getModelCaller()
						.prepareParam(data, this.charset));
				response.sendRedirect(url.toString());
			}
			else if (export.getPath() != null)
			{
				String path = export.getPath();
				StringAppender url = StringTool.createStringAppender(512);
				if (path.startsWith("/"))
				{
					url.append(request.getScheme()).append("://").append(request.getHeader("host"))
							.append(request.getContextPath());
				}
				url.append(path);
				if (path.indexOf('?') != -1)
				{
					url.append('&');
				}
				else
				{
					url.append('?');
				}
				url.append(this.getFactoryManager().getEternaFactory().getModelCaller()
						.prepareParam(data, this.charset));
				response.sendRedirect(url.toString());
			}
			return;
		}
		request.setAttribute(APPDATA_TAG, data);
		try
		{
			if (export.getViewName() != null)
			{
				ViewAdapter view = this.getFactoryManager().getEternaFactory().createViewAdapter(export.getViewName());
				request.setAttribute(VIEW_TAG, view);
			}
		}
		catch (EternaException ex)
		{
			log.warn("Error in doExport.", ex);
		}
		data.dataMap.put("servletPath", request.getServletPath());
		request.getRequestDispatcher(export.getPath()).include(request, response);
	}

	public void destroy()
	{
		super.destroy();
		try
		{
			if (this.getFactoryManager() != null)
			{
				this.getFactoryManager().destroy();
			}
		} catch (EternaException ex) {}
	}

}