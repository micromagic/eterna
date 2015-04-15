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
import java.sql.SQLException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import self.micromagic.eterna.digester.FactoryManager;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.ModelCaller;
import self.micromagic.eterna.model.ModelExport;
import self.micromagic.eterna.view.ViewAdapter;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.util.container.RequestParameterMap;

/**
 * 用于加载eterna配置的portlet.
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
 *
 * @see ModelCaller#DEFAULT_MODEL_NAME
 * @see FactoryManager#CONFIG_INIT_FILES
 * @see FactoryManager#CONFIG_INIT_PARENTFILES
 *
 * @author micromagic@sina.com
 */
public class EternaPortlet extends GenericPortlet
		implements WebApp
{
	public static final String NEXT_MODEL_TAG = "nextModel";

	protected FactoryManager.Instance factoryManager = null;
	protected String defaultModel = "index";
	protected boolean initFactoryManager = true;

	public void init(PortletConfig config)
			throws PortletException
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
	}

	protected FactoryManager.Instance getFactoryManager()
			throws EternaException
	{
		return this.factoryManager;
	}

	public static String getServerRoot(PortletRequest request)
	{
		PortletSession session = request.getPortletSession();
		String serverRoot = (String) session.getAttribute(SERVER_ROOT_TAG);
		if (serverRoot == null)
		{
			serverRoot = request.getScheme() + "://" + request.getServerName()
					+ ":" + request.getServerPort() + request.getContextPath();
			session.setAttribute(SERVER_ROOT_TAG, serverRoot);
		}
		return serverRoot;
	}

	public void processAction(ActionRequest request, ActionResponse response)
			throws PortletException, IOException
	{
		AppData data = AppData.getCurrentData();
		data.contextRoot = request.getContextPath();
		data.actionRequest = request;
		data.actionResponse = response;
		data.portletConfig = this.getPortletConfig();
		data.position = AppData.POSITION_PORTLET_ACTION;

		if (this.defaultModel != null)
		{
			request.setAttribute(ModelCaller.DEFAULT_MODEL_TAG, this.defaultModel);
		}
		else
		{
			request.setAttribute(ModelCaller.DEFAULT_MODEL_TAG, ModelCaller.DEFAULT_MODEL_NAME);
		}
		try
		{
			data.maps[AppData.REQUEST_PARAMETER_MAP] = RequestParameterMap.create(request.getParameterMap());
			data.maps[AppData.REQUEST_ATTRIBUTE_MAP] = PortletValueMap.createRequestAttributeMap(request);
			data.maps[AppData.SESSION_ATTRIBUTE_MAP] = PortletValueMap.createSessionAttributeMap(
					request, PortletSession.APPLICATION_SCOPE);
			data.export = this.getFactoryManager().getEternaFactory().getModelCaller().callModel(data);
			String nextModel = (String) request.getAttribute(NEXT_MODEL_TAG);
			if (nextModel != null)
			{
				data.modelName = nextModel;
			}
		}
		catch (EternaException ex)
		{
			log.warn("Error in processAction.", ex);
		}
		catch (SQLException ex)
		{
			log.warn("SQL Error in processAction.", ex);
		}
		catch (Throwable ex)
		{
			log.error("Other Error in processAction.", ex);
		}
	}

	public void render(RenderRequest request, RenderResponse response)
			throws PortletException, IOException
	{
		response.setTitle(this.getTitle(request));
		AppData data = AppData.getCurrentData();
		try
		{
			data.contextRoot = request.getContextPath();
			data.renderRequest = request;
			data.renderResponse = response;
			data.portletConfig = this.getPortletConfig();
			data.position = AppData.POSITION_PORTLET_RENDER;
			if (data.export == null)
			{
				if (this.defaultModel != null)
				{
					request.setAttribute(ModelCaller.DEFAULT_MODEL_TAG, this.defaultModel);
				}
				else
				{
					request.setAttribute(ModelCaller.DEFAULT_MODEL_TAG, ModelCaller.DEFAULT_MODEL_NAME);
				}
				try
				{
					data.maps[AppData.REQUEST_PARAMETER_MAP] = RequestParameterMap.create(request.getParameterMap());
					data.maps[AppData.REQUEST_ATTRIBUTE_MAP] = PortletValueMap.createRequestAttributeMap(request);
					data.maps[AppData.SESSION_ATTRIBUTE_MAP] = PortletValueMap.createSessionAttributeMap(
							request, PortletSession.APPLICATION_SCOPE);
					ModelExport export = this.getFactoryManager().getEternaFactory().getModelCaller().callModel(data);
					if (export != null)
					{
						data.export = export;
					}
				}
				catch (EternaException ex)
				{
					log.warn("Error in render.", ex);
				}
				catch (SQLException ex)
				{
					log.warn("SQL Error in render.", ex);
				}
				catch (Throwable ex)
				{
					log.error("Other Error in render.", ex);
				}
			}
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
			data.portletConfig = null;
			data.clearData();
		}
	}

	protected void doExport(AppData data, RenderRequest request, RenderResponse response)
			throws PortletException, IOException
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
				// portlet 中无法重定向, 所以Export是model时, 通过render方式再调用这个model
				data.modelName = export.getModelName();
				data.export = null;
				this.render(request, response);
				return;
			}
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
		data.dataMap.put("actionURL", response.createActionURL().toString());
		data.dataMap.put("renderURL", response.createRenderURL().toString());
		data.dataMap.put("portletRoot", response.encodeURL(""));
		this.getPortletContext().getRequestDispatcher(export.getPath()).include(request, response);
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