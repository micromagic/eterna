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

package self.micromagic.eterna.tag;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.digester.FactoryManager;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.ModelExport;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.view.ViewAdapter;
import self.micromagic.util.container.RequestParameterMap;
import self.micromagic.util.container.ThreadCache;
import self.micromagic.util.container.ValueContainerMap;

/**
 * 在JSP中, 可通过此标签在页面中定义一个eterna对象.
 *
 * @author micromagic@sina.com
 */
public class EternaDefine extends InitBaseTag
{
	/**
	 * 默认的空界面的名称.
	 */
	public static final String EMPTY_VIEW_FLAG = "empty.view";

	/**
	 * 在上下文环境中获得工厂实例的名称前缀.
	 */
	public static final String CONTEXT_FMI_PREFIX = "$context.";


	private String name;
	private String instanceName;
	private String modelName;
	private String param;
	private String viewName;
	private String data;

	public int doStartTag()
			throws JspException
	{
		AppData oldData = AppData.getCurrentData();
		try
		{
			ServletRequest req = this.pageContext.getRequest();
			HttpServletRequest request = null;
			if (req instanceof HttpServletRequest)
			{
				request = (HttpServletRequest) req;
			}
			AppData nowData = new AppData();
			ThreadCache.getInstance().setProperty(AppData.CACHE_NAME, nowData);
			nowData.position = AppData.POSITION_SERVLET;
			nowData.response = this.pageContext.getResponse();
			nowData.request = req;
			nowData.servletConfig = this.pageContext.getServletConfig();
			nowData.maps[AppData.REQUEST_ATTRIBUTE_MAP] = ValueContainerMap.createRequestAttributeMap(req);
			Map paramMap = null;
			if (this.param != null)
			{
				Object obj = this.pageContext.findAttribute(this.param);
				if (obj == null)
				{
					DefaultFinder.log.warn("Not found param:[" + this.param + "].");
				}
				else
				{
					if (obj instanceof Map)
					{
						paramMap = (Map) obj;
					}
					else
					{
						DefaultFinder.log.warn("Error param type:[" + obj.getClass() + "].");
					}
				}
			}
			if (paramMap == null)
			{
				nowData.maps[AppData.REQUEST_PARAMETER_MAP] = RequestParameterMap.create(request);
			}
			else
			{
				nowData.maps[AppData.REQUEST_PARAMETER_MAP] = RequestParameterMap.create(paramMap);
			}
			if (request != null)
			{
				nowData.contextRoot = request.getContextPath();
				nowData.dataMap.put("servletPath", request.getServletPath());
				nowData.maps[AppData.SESSION_ATTRIBUTE_MAP] = ValueContainerMap.createSessionAttributeMap(request);
			}
			else
			{
				nowData.maps[AppData.SESSION_ATTRIBUTE_MAP] = new HashMap();
			}


			EternaFactory f = this.getEternaFactory();
			String tmpViewName = this.viewName;
			if (this.modelName != null)
			{
				nowData.modelName = this.modelName;
				ModelExport export = f.getModelCaller().callModel(nowData);
				while (export != null && export.getModelName() != null)
				{
					nowData.modelName = export.getModelName();
					export = f.getModelCaller().callModel(nowData);
					nowData.export = export;
				}
				if (export != null && tmpViewName == null)
				{
					tmpViewName = export.getViewName();
				}
			}
			if (tmpViewName == null)
			{
				tmpViewName = EMPTY_VIEW_FLAG;
			}
			if (this.data != null)
			{
				Object obj = this.pageContext.findAttribute(this.data);
				if (obj == null)
				{
					DefaultFinder.log.warn("Not found data:[" + this.data + "].");
				}
				else
				{
					if (obj instanceof Map)
					{
						nowData.dataMap.putAll((Map) obj);
					}
					else
					{
						DefaultFinder.log.warn("Error data type:[" + obj.getClass() + "].");
					}
				}
			}
			ViewAdapter view = f.createViewAdapter(tmpViewName);
			JspWriter out = this.pageContext.getOut();
			String dataType = view.getDataType(nowData);
			if (ViewAdapter.DATA_TYPE_WEB.equals(dataType))
			{
				out.println("<script type=\"text/javascript\">");
				out.println("(function() {");
				out.print("var $E = ");
				view.printView(out, nowData, this.getCacheMap(view));
				out.println(';');
				out.println("var eternaData = $E;");
				out.println("var eterna_debug = " + view.getDebug() + ";");
				out.println("var _eterna = new Eterna(eternaData, eterna_debug, null);");
				if (this.isUseAJAX())
				{
					out.println("_eterna.cache.useAJAX = true;");
				}
				if (this.getParentElement() != null)
				{
					out.println("jQuery(_eterna.reInit);");
				}
				out.println("window." + this.name + " = _eterna;");
				out.println("})();");
				out.println("</script>");
				return EVAL_BODY_INCLUDE;
			}
			else
			{
				view.printView(out, nowData, this.getCacheMap(view));
				if (ViewAdapter.DATA_TYPE_ALL.equals(dataType))
				{
					out.println(ViewAdapter.JSON_SPLIT_FLAG);
					return EVAL_BODY_INCLUDE;
				}
			}
		}
		catch (EternaException ex)
		{
			DefaultFinder.log.warn("Error in def.", ex);
		}
		catch (SQLException ex)
		{
			DefaultFinder.log.warn("SQL Error in def.", ex);
		}
		catch (Throwable ex)
		{
			DefaultFinder.log.error("Other Error in def.", ex);
		}
		finally
		{
			ThreadCache.getInstance().setProperty(AppData.CACHE_NAME, oldData);
		}
		return SKIP_BODY;
	}

	private EternaFactory getEternaFactory()
			throws EternaException
	{
		if (this.instanceName != null)
		{
			if (this.instanceName.startsWith(CONTEXT_FMI_PREFIX))
			{
				FactoryManager.Instance instance = (FactoryManager.Instance) this.pageContext.findAttribute(
						this.instanceName.substring(CONTEXT_FMI_PREFIX.length()));
				if (instance != null)
				{
					return instance.getEternaFactory();
				}
				else
				{
					DefaultFinder.log.error("Not found factory [" + this.instanceName + "] in context.");
				}
			}
			else
			{
				FactoryManager.Instance instance = DefaultFinder.finder.findInstance(this.instanceName);
				if (instance != null)
				{
					return instance.getEternaFactory();
				}
				else
				{
					DefaultFinder.log.error("Not found factory [" + this.instanceName + "].");
				}
			}
		}
		return FactoryManager.getEternaFactory();
	}

	public void release()
	{
		this.name = null;
		this.instanceName = null;
		this.modelName = null;
		this.param = null;
		this.viewName = null;
		this.data = null;
		super.release();
	}

	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getInstanceName()
	{
		return this.instanceName;
	}

	public void setInstanceName(String instanceName)
	{
		this.instanceName = instanceName;
	}

	public String getModelName()
	{
		return this.modelName;
	}

	public void setModelName(String modelName)
	{
		this.modelName = modelName;
	}

	public String getParam()
	{
		return this.param;
	}

	public void setParam(String param)
	{
		this.param = param;
	}

	public String getViewName()
	{
		return this.viewName;
	}

	public void setViewName(String viewName)
	{
		this.viewName = viewName;
	}

	public String getData()
	{
		return this.data;
	}

	public void setData(String data)
	{
		this.data = data;
	}

}