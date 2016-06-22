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

package self.micromagic.eterna.model;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.XMLWriter;

import self.micromagic.eterna.share.Tool;
import self.micromagic.util.FormatTool;
import self.micromagic.util.Utility;
import self.micromagic.util.container.RequestParameterMap;
import self.micromagic.util.container.ThreadCache;
import self.micromagic.util.logging.TimeLogger;

/**
 * @author micromagic@sina.com
 */
public class AppData
{
	public static final String APP_LOG_PROPERTY = "eterna.app.logType";
	public static final String CACHE_NAME = "eterna.model.APP_DATA";
	public static final Log log = Tool.log;

	public static final int POSITION_NONE = 0;
	public static final int POSITION_ALL = -1;
	public static final int POSITION_FILTER = 0x2;
	public static final int POSITION_SERVLET = 0x8;
	public static final int POSITION_PORTLET_ACTION = 0x10;
	public static final int POSITION_PORTLET_RENDER = 0x20;
	public static final int POSITION_SPECIAL = 0x100;
	public static final int POSITION_MODEL = 0x200;
	public static final int POSITION_OTHER1 = 0x1000;
	public static final int POSITION_OTHER2 = 0x2000;
	public static final int POSITION_OTHER3 = 0x4000;

	public static final String REQUEST_PARAMETER_MAP_NAME = "request-parameter";
	public static final int REQUEST_PARAMETER_MAP = 0;
	public static final String REQUEST_ATTRIBUTE_MAP_NAME = "request-attribute";
	public static final int REQUEST_ATTRIBUTE_MAP = 1;
	public static final String SESSION_ATTRIBUTE_MAP_NAME = "session-attribute";
	public static final int SESSION_ATTRIBUTE_MAP = 2;
	public static final String DATA_MAP_NAME = "data";
	public static final int DATA_MAP = 3;

	public static final String[] MAP_NAMES = {
		REQUEST_PARAMETER_MAP_NAME, REQUEST_ATTRIBUTE_MAP_NAME,
		SESSION_ATTRIBUTE_MAP_NAME, DATA_MAP_NAME
	};

	private static int APP_LOG_TYPE = 0;
	private static Document logDocument = null;
	private ArrayList nodeStack = null;

	/**
	 * 当前定义变量的缓存.
	 */
	public VarCache varCache;

	/**
	 * model作用域中存放的变量.
	 */
	public Object[] modelVars;
	/**
	 * 全局作用域中存放的变量.
	 */
	public Object[] globalVars;

	public int position;
	private String app_id = null;
	public String contextRoot = "";
	public String modelName;
	public ServletRequest request;
	public ServletResponse response;
	public FilterConfig filterConfig;
	public ServletConfig servletConfig;
	public RenderRequest renderRequest;
	public RenderResponse renderResponse;
	public ActionRequest actionRequest;
	public ActionResponse actionResponse;
	public PortletConfig portletConfig;
	public ModelExport export;
	private int logType = APP_LOG_TYPE;

	/**
	 * 标识AppData是否已执行了clearData方法.
	 */
	private boolean cleared;

	/**
	 * view中使用的数据集对象
	 */
	public final Map dataMap = new HashMap();

	public final Object[] caches = new Object[23];
	public final Map[] maps = new Map[4];
	public final ArrayList stack = new ArrayList();

	private final Map spcialMap = new HashMap(2);

	static
	{
		try
		{
			// 这里通过AppDataLogExecute类来监听
			// 因为在AppData中可能会有类不存在造成获取Method时发生错误
			Utility.addMethodPropertyManager(APP_LOG_PROPERTY, AppDataLogExecute.class,
					"setAppLogType");
		}
		catch (Exception ex)
		{
			log.error("Error in init app log type.", ex);
		}
	}

	public AppData()
	{
		this.maps[DATA_MAP] = this.dataMap;
	}

	/**
	 * 设置app运行日志记录方式.
	 */
	static void setAppLogType(int type)
	{
		APP_LOG_TYPE = type;
	}

	/**
	 * 获取app运行日志记录方式.
	 *
	 * @deprecated
	 * @see #getLogType()
	 * @see AppDataLogExecute#getAppLogType()
	 */
	public static int getAppLogType()
	{
		return APP_LOG_TYPE;
	}

	/**
	 * 获取当前app运行日志记录方式.
	 */
	public int getLogType()
	{
		return this.logType;
	}

	/**
	 * 打印app运行日志信息
	 */
	public static synchronized void printLog(Writer out, boolean clear)
			throws IOException
	{
		if (logDocument == null)
		{
			return;
		}
		XMLWriter writer = new XMLWriter(out);
		writer.write(logDocument);
		writer.flush();
		if (clear)
		{
			logDocument = null;
		}
	}

	/**
	 * 获得app日志中的当前活动节点.
	 * 如果未在app日志中没有活动的节点, 则返回null.
	 */
	public Element getCurrentNode()
	{
		if (this.logType == 0)
		{
			return null;
		}
		if (this.nodeStack == null || this.nodeStack.size() == 0)
		{
			return null;
		}
		return (Element) this.nodeStack.get(this.nodeStack.size() - 1);
	}

	/**
	 * 在当前节点下添加一条运行信息
	 */
	public void addAppMessage(String msg)
	{
		if (this.logType == 0)
		{
			// 如果日志处于关闭状态不做记录
			return;
		}
		this.addAppMessage(msg, null);
	}

	/**
	 * 在当前节点下添加一条运行信息
	 *
	 * @param msg   要添加的信息
	 * @param type	 信息的类型
	 */
	public void addAppMessage(String msg, String type)
	{
		if (this.logType == 0)
		{
			// 如果日志处于关闭状态不做记录
			return;
		}
		if (this.nodeStack == null || this.nodeStack.size() == 0)
		{
			// 节点堆栈为空不做记录
			return;
		}
		Element nowNode = (Element) this.nodeStack.get(this.nodeStack.size() - 1);
		Element msgNode = nowNode.addElement("message");
		if (type != null)
		{
			msgNode.addAttribute("type", type);
		}
		if (msg != null)
		{
			msgNode.setText(msg);
		}
		msgNode.addAttribute("time", FormatTool.formatDatetime(new Date(System.currentTimeMillis())));
	}

	/**
	 * 在app日志中记录一个节点起始标记
	 */
	public Element beginNode(String nodeType, String nodeName, String nodeDescription)
	{
		if (this.logType == 0)
		{
			// 如果日志处于关闭状态且节点堆栈为空的状态才返回null不做记录
			return null;
		}
		if (this.nodeStack == null)
		{
			this.nodeStack = new ArrayList();
		}
		Element node = DocumentHelper.createElement(nodeType);
		if (nodeName != null)
		{
			node.addAttribute("name", nodeName);
		}
		if (nodeDescription != null)
		{
			node.addAttribute("description", nodeDescription);
		}
		if (this.nodeStack.size() > 0)
		{
			Element parent = (Element) this.nodeStack.get(this.nodeStack.size() - 1);
			parent.add(node);
		}
		else
		{
			node.addAttribute("beginTime", FormatTool.formatDatetime(new Date(System.currentTimeMillis())));
			node.addAttribute("_time", Long.toString(TimeLogger.getTime()));
		}
		this.nodeStack.add(node);
		return node;
	}

	/**
	 * 在app日志中记录一个节点结束标记.
	 * @deprecated
	 * @see #endNode(Element, Throwable, ModelExport)
	 */
	public Element endNode(Throwable error, ModelExport export)
	{
		if (this.logType == 0)
		{
			// 如果日志处于关闭状态且节点堆栈为空的状态才返回null不做记录
			return null;
		}
		if (this.nodeStack == null || this.nodeStack.size() == 0)
		{
			log.error("You haven't begined a node in this app.");
			return null;
		}
		Element node = (Element) this.nodeStack.remove(this.nodeStack.size() - 1);
		if (error != null)
		{
			node.addAttribute("error", error.toString());
		}
		if (export != null)
		{
			node.addAttribute("export", export.getName());
		}
		if (this.nodeStack.size() == 0)
		{
			logNode(node);
		}
		return node;
	}

	/**
	 * 在app日志中记录一个节点结束标记.
	 *
	 * @param node    调用beginNode获得的Element对象
	 * @param error   执行过程中抛出的异常
	 * @param export  执行完成后需要转向的export
	 */
	public Element endNode(Element node, Throwable error, ModelExport export)
	{
		if (this.logType == 0)
		{
			// 如果日志处于关闭状态且节点堆栈为空的状态才返回null不做记录
			return null;
		}
		if (this.nodeStack == null || this.nodeStack.size() == 0)
		{
			log.error("You haven't begined a node in this app.");
			return null;
		}
		int nodePos = this.nodeStack.size() - 1;
		Element current = null;
		for (; nodePos >= 0; nodePos--)
		{
			current = (Element) this.nodeStack.get(nodePos);
			if (node == current)
			{
				break;
			}
		}
		if (node == current)
		{
			int index = this.nodeStack.size() - 1;
			for (; index >= nodePos; index--)
			{
				current = (Element) this.nodeStack.remove(index);
				if (node != current)
				{
					current.addAttribute("notEnd", "true");
				}
			}
			if (error != null)
			{
				node.addAttribute("error", error.toString());
			}
			if (export != null)
			{
				node.addAttribute("export", export.getName());
			}
			if (this.nodeStack.size() == 0)
			{
				logNode(node);
			}
			else
			{
				Attribute _time = node.attribute("_time");
				if (_time != null)
				{
					try
					{
						String beginTimeStr = _time.getValue();
						node.remove(_time);
						long beginTime = Long.parseLong(beginTimeStr);
						node.addAttribute("usedTime", TimeLogger.formatPassTime(TimeLogger.getTime() - beginTime));
					}
					catch (Exception ex) {}
				}
			}
		}
		else
		{
			log.error("Not found the node:" + node.asXML());
		}
		return node;
	}

	private static synchronized void logNode(Element node)
	{
		node.addAttribute("endTime", FormatTool.formatDatetime(new Date(System.currentTimeMillis())));
		try
		{
			Attribute _time = node.attribute("_time");
			String beginTimeStr = _time.getValue();
			node.remove(_time);
			long beginTime = Long.parseLong(beginTimeStr);
			node.addAttribute("usedTime", TimeLogger.formatPassTime(TimeLogger.getTime() - beginTime));
		}
		catch (Exception ex) {}
		Element logs;
		if (logDocument == null)
		{
			logDocument = DocumentHelper.createDocument();
			Element root = logDocument.addElement("eterna");
			logs = root.addElement("logs");
		}
		else
		{
			logs = logDocument.getRootElement().element("logs");
		}

		if (logs.nodeCount() > 256)
		{
			// 当节点过多时, 清除最先添加的几个节点
			Iterator itr = logs.nodeIterator();
			try
			{
				for (int i = 0; i < 230; i++)
				{
					itr.next();
				}
				Element newLogs = DocumentHelper.createElement("logs");
				while (itr.hasNext())
				{
					newLogs.add((Node) itr.next());
				}
				Element root = logs.getParent();
				root.remove(logs);
				root.add(newLogs);
				logs = newLogs;
			}
			catch (Exception ex)
			{
				// 当去除节点出错时, 则清空日志
				log.warn("Remove app log error.", ex);
				logDocument = DocumentHelper.createDocument();
				Element root = logDocument.addElement("eterna");
				logs = root.addElement("logs");
			}
		}
		logs.add(node);
	}

	/**
	 * 获得当前线程中的AppData对象
	 */
	public static AppData getCurrentData()
	{
		ThreadCache cache = ThreadCache.getInstance();
		AppData data = (AppData) cache.getProperty(CACHE_NAME);
		if (data == null)
		{
			data = new AppData();
			cache.setProperty(CACHE_NAME, data);
		}
		else if (data.cleared)
		{
			data.cleared = false;
			data.logType = APP_LOG_TYPE;
		}
		return data;
	}

	public static String getRequestParameter(String name, Map rParamMap)
	{
		if (rParamMap == null)
		{
			return null;
		}
		Object obj = rParamMap.get(name);
		return RequestParameterMap.getFirstParam(obj);
	}

	public void clearData()
	{
		this.app_id = null;
		this.clearStack();
		this.dataMap.clear();
		this.maps[AppData.DATA_MAP] = this.dataMap;
		this.spcialMap.clear();
		this.maps[AppData.REQUEST_ATTRIBUTE_MAP] = null;
		this.maps[AppData.SESSION_ATTRIBUTE_MAP] = null;
		this.maps[AppData.REQUEST_PARAMETER_MAP] = null;
		for (int i = 0; i < this.caches.length; i++)
		{
			this.caches[i] = null;
		}
		this.modelVars = null;
		this.globalVars = null;
		// 恢复App日志状态并清空
		this.logType = APP_LOG_TYPE;
		if (this.nodeStack != null)
		{
			this.nodeStack.clear();
		}
		this.cleared = true;
	}

	public Map setSpcialDataMap(String specialName, Map map)
	{
		return (Map) this.spcialMap.put(specialName, map);
	}

	public Map getSpcialDataMap(String specialName)
	{
		return this.getSpcialDataMap(specialName, false);
	}

	public Map getSpcialDataMap(String specialName, boolean remove)
	{
		Object tmp = this.spcialMap.get(specialName);
		if (tmp == null)
		{
			return null;
		}
		if (remove)
		{
			this.spcialMap.remove(specialName);
		}
		if (tmp instanceof Map)
		{
			return (Map) tmp;
		}
		return null;
	}

	public Object addSpcialData(String specialName, String name, Object data)
	{
		Map tmp = (Map) this.spcialMap.get(specialName);
		if (tmp == null)
		{
			tmp = new HashMap();
			this.spcialMap.put(specialName, tmp);
		}
		return tmp.put(name, data);
	}

	public Object getSpcialData(String specialName, String name)
	{
		Map tmp = (Map) this.spcialMap.get(specialName);
		if (tmp == null)
		{
			return null;
		}
		return tmp.get(name);
	}

	public String getAppId()
	{
		if (this.app_id == null)
		{
			this.app_id = Long.toString(System.currentTimeMillis(), 32).toUpperCase() + "_"
					+ Integer.toString(System.identityHashCode(this), 32).toUpperCase();
		}
		return this.app_id;
	}

	/**
	 * 获得request中的参数
	 */
	public String getRequestParameter(String name)
	{
		return getRequestParameter(name, this.getRequestParameterMap());
	}

	/**
	 * 获得request中参数的map对象
	 */
	public Map getRequestParameterMap()
	{
		return this.maps[REQUEST_PARAMETER_MAP];
	}

	/**
	 * 获得request中attribute的map对象
	 */
	public Map getRequestAttributeMap()
	{
		return this.maps[REQUEST_ATTRIBUTE_MAP];
	}

	/**
	 * 获得session中attribute的map对象
	 */
	public Map getSessionAttributeMap()
	{
		return this.maps[SESSION_ATTRIBUTE_MAP];
	}

	public HttpServletRequest getHttpServletRequest()
	{
		if (this.request instanceof HttpServletRequest)
		{
			return (HttpServletRequest) this.request;
		}
		return null;
	}

	/**
	 * 获取客户端的IP地址. <p>
	 * 如果前端有HTTP服务器转发的话, 可以设置<code>agentNames</code>代理名称列表,
	 * 来获取真正的客户端IP.
	 *
	 * @param agentNames  代理名称列表, 如果没有可以设为<code>null</code>
	 *	                   格式为: 头信息中的名称[:多个ip的分割符及顺序]
	 *                    顺序的值设为A表示取第一个ip, 设为D表示去最后个ip
	 * @return  客户端的IP地址
	 */
	public String getRemoteAddr(String[] agentNames)
	{
		HttpServletRequest request = this.getHttpServletRequest();
		if (request == null)
		{
			return null;
		}
		if (agentNames != null)
		{
			for (int i = 0; i < agentNames.length; i++)
			{
				String agentName = agentNames[i];
				// 解析代理的名称中是否有分隔符及ip顺序的设置
				int flag = agentName.indexOf(':');
				char split = ';';
				boolean desc = false;
				if (flag != -1 && flag == agentName.length() - 3)
				{
					split = agentName.charAt(flag + 1);
					desc = agentName.charAt(flag + 2) == 'D';
					agentName = agentName.substring(0, flag);
				}
				String ip = request.getHeader(agentName);
				if (ip != null)
				{
					if (desc)
					{
						int index = ip.lastIndexOf(split);
						return index == -1 ? ip : ip.substring(index + 1).trim();
					}
					else
					{
						int index = ip.indexOf(split);
						return index == -1 ? ip : ip.substring(0, index).trim();
					}
				}
			}
		}
		return request.getRemoteAddr();
	}

	public HttpServletResponse getHttpServletResponse()
	{
		if (this.request instanceof HttpServletRequest)
		{
			return (HttpServletResponse) this.response;
		}
		return null;
	}

	public ServletContext getServletContext()
	{
		if (this.servletConfig != null)
		{
			return this.servletConfig.getServletContext();
		}
		if (this.filterConfig != null)
		{
			return this.filterConfig.getServletContext();
		}
		return null;
	}

	public PortletContext getPortletContext()
	{
		if (this.portletConfig != null)
		{
			return this.portletConfig.getPortletContext();
		}
		return null;
	}

	/**
	 * 获得servlet或portlet的初始化参数
	 */
	public String getInitParameter(String name)
	{
		if (this.portletConfig != null)
		{
			return this.portletConfig.getInitParameter(name);
		}
		if (this.servletConfig != null)
		{
			return this.servletConfig.getInitParameter(name);
		}
		if (this.filterConfig != null)
		{
			return this.filterConfig.getInitParameter(name);
		}
		return null;
	}

	/**
	 * 获得response中的OutputStream对象
	 */
	public OutputStream getOutputStream()
			throws IOException
	{
		if (this.renderResponse != null)
		{
			return this.renderResponse.getPortletOutputStream();
		}
		if (this.response != null)
		{
			return this.response.getOutputStream();
		}
		return null;
	}

	public void clearStack()
	{
		this.stack.clear();
	}

	public Object pop()
	{
		if (this.stack.size() > 0)
		{
			return this.stack.remove(this.stack.size() - 1);
		}
		return null;
	}

	public void push(Object obj)
	{
		this.stack.add(obj);
	}

	public Object peek(int index)
	{
		if (index < this.stack.size())
		{
			return this.stack.get(this.stack.size() - 1 - index);
		}
		return null;
	}

}