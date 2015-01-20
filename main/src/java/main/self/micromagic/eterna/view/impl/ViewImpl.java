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

package self.micromagic.eterna.view.impl;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.ObjectCreater;
import self.micromagic.eterna.view.Component;
import self.micromagic.eterna.view.DataPrinter;
import self.micromagic.eterna.view.Function;
import self.micromagic.eterna.view.ModifiableViewRes;
import self.micromagic.eterna.view.Resource;
import self.micromagic.eterna.view.StringCoder;
import self.micromagic.eterna.view.View;
import self.micromagic.util.MemoryChars;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;
import self.micromagic.util.container.PreFetchIterator;

/**
 * @author micromagic@sina.com
 */
public class ViewImpl extends AbstractGenerator
		implements View, ObjectCreater
{
	private final List componentList = new LinkedList();
	private StringCoder stringCoder;
	private String viewGlobalSetting = "";
	private int debug = 0;
	private String width;
	private String height;
	protected String beforeInit;
	protected String initScript;
	protected String dynamicViewRes;
	protected String defaultDataType = DATA_TYPE_WEB;
	protected String dataPrinterName = DEFAULT_DATA_PRINTER_NAME;
	protected DataPrinter dataPrinter;

	private final ModifiableViewRes viewRes = new ModifiableViewResImpl();

	protected boolean initialized = false;

	private MemoryChars mc = null;

	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		if (!this.initialized)
		{
			this.initialized = true;

			this.dynamicViewRes = ViewTool.dealScriptPart(
					this.viewRes, this.dynamicViewRes, ViewTool.GRAMMER_TYPE_NONE, factory);
			this.dataPrinter = factory.getDataPrinter(this.dataPrinterName);
			if (this.dataPrinter == null)
			{
				throw new EternaException("Not found DataPrinter [" + this.dataPrinterName
						+ "] in view [" + this.getName() + "].");
			}

			Iterator componentItr = this.getComponents();
			while (componentItr.hasNext())
			{
				Component com = (Component) componentItr.next();
				com.initialize(factory, null);
				this.viewRes.addAll(com.getViewRes());
			}
			this.beforeInit = ViewTool.dealScriptPart(
					this.viewRes, this.beforeInit, ViewTool.GRAMMER_TYPE_EXPRESSION, factory);
			this.initScript = ViewTool.dealScriptPart(
					this.viewRes, this.initScript, ViewTool.GRAMMER_TYPE_EXPRESSION, factory);

			this.stringCoder = factory.getStringCoder();
			this.viewGlobalSetting = this.getFactory().getViewGlobalSetting();
			this.viewGlobalSetting = ViewTool.dealScriptPart(
					this.viewRes, this.viewGlobalSetting, ViewTool.GRAMMER_TYPE_JSON, factory);

			// 这里需要新建一个set, 因为执行过程中会改变原来的数据集
			Set typicalSet = new HashSet(this.viewRes.getTypicalComponentNames());
			Set dealedSet = new HashSet();
			int setSize = 0;
			while (typicalSet.size() > setSize)
			{
				setSize = typicalSet.size();
				Iterator typicals = typicalSet.iterator();
				while (typicals.hasNext())
				{
					String name = (String) typicals.next();
					if (dealedSet.contains(name))
					{
						continue;
					}
					dealedSet.add(name);
					Component com = this.getFactory().getTypicalComponent(name);
					if (com != null)
					{
						this.viewRes.addAll(com.getViewRes());
					}
					else
					{
						log.error("Not found typical component [" + name + "] in view ["
							+ this.getName() + "]");
					}
				}
				typicalSet = new HashSet(this.viewRes.getTypicalComponentNames());
			}

			return false;
		}
		return true;
	}

	public void setDynamicViewRes(String res)
	{
		this.dynamicViewRes = res;
	}

	public View.ViewRes getViewRes()
	{
		return this.viewRes;
	}

	public void setDataPrinterName(String dpName)
	{
		this.dataPrinterName = dpName;
	}

	public DataPrinter getDataPrinter()
	{
		return this.dataPrinter;
	}

	public void setDefaultDataType(String type)
	{
		this.defaultDataType = type;
	}

	public String getDefaultDataType()
	{
		return this.defaultDataType;
	}

	public String getDataType(AppData data)
	{
		String dt = data.getRequestParameter(DATA_TYPE);
		return dt == null ? this.defaultDataType : dt;
	}

	public int getDebug()
	{
		return this.debug;
	}

	public void setDebug(int debug)
	{
		this.debug = debug;
	}

	public String getWidth()
	{
		return this.width;
	}

	public void setWidth(String width)
	{
		this.width = width;
	}

	public String getHeight()
	{
		return this.height;
	}

	public void setHeight(String height)
	{
		this.height = height;
	}

	public String getBeforeInit()
			throws EternaException
	{
		return this.beforeInit;
	}

	public void setBeforeInit(String condition)
			throws EternaException
	{
		this.beforeInit = condition;
	}

	public String getInitScript()
			throws EternaException
	{
		return this.initScript;
	}

	public void setInitScript(String body)
			throws EternaException
	{
		this.initScript = body;
	}

	public Iterator getComponents()
	{
		return new PreFetchIterator(this.componentList.iterator(), false);
	}

	public void addComponent(Component com)
	{
		this.componentList.add(com);
	}

	public void deleteComponent(Component com)
	{
		this.componentList.remove(com);
	}

	public void clearComponents()
	{
		this.componentList.clear();
	}

	public EternaFactory getFactory()
	{
		return (EternaFactory) this.factory;
	}

	private static final String DEBUG_EVENT_CODE = ",fn:function(event,webObj,objConfig){"
			+ "try{"
			+ "return event.data.eventConfig._fn.call(this,event,webObj,objConfig);"
			+ "}"
			+ "catch(ex){"
			+ "if(eterna_debug >= ED_FN_CALLED){"
			+ "eterna_fn_stack.push(new Array(event.data.eventConfig.type,event.data.eventConfig._fn,\"event.data\",event.data));"
			+ "_eterna.printException(ex);"
			+ "eterna_fn_stack.pop();"
			+ "}"
			+ "throw ex;"
			+ "}"
			+ "},_fn:function(event,webObj,objConfig){";

	public void printEvent(Writer out, AppData data, Component.Event event)
			throws IOException, EternaException
	{
		out.write("{type:\"");
		this.stringCoder.toJsonString(out, event.getName());
		out.write('"');

		if (!StringTool.isEmpty(event.getScriptParam()))
		{
			out.write(",param:\"");
			this.stringCoder.toJsonStringWithoutCheck(out, event.getScriptParam());
			out.write('"');
		}

		String eventBegin = "var configData=objConfig;";
		if (this.getDebug() >= ETERNA_VIEW_DEBUG_BASE)
		{
			out.write(DEBUG_EVENT_CODE);
			out.write(eventBegin);
			out.write(event.getScriptBody());
			out.write("}}");
		}
		else
		{
			out.write(",fn:function(event,webObj,objConfig){");
			out.write(eventBegin);
			out.write(event.getScriptBody());
			out.write("}}");
		}
	}

	public void printFunction(Writer out, AppData data, String key, Function fn)
			throws IOException, EternaException
	{
		String param = fn.getParam();
		if (StringTool.isEmpty(param))
		{
			param = "";
		}
		else
		{
			param.trim();
		}

		if (this.getDebug() >= ETERNA_VIEW_DEBUG_BASE)
		{
			out.write("\"$ef_");
			this.stringCoder.toJsonString(out, key);
			out.write("\":function(");
			out.write(param);
			out.write("){");
			out.write(fn.getBody());
			out.write("},\"");
			this.stringCoder.toJsonString(out, key);
			out.write("\":function(");
			out.write(param);
			out.write("){try{return $E.F[\"$ef_");
			this.stringCoder.toJsonString(out, key);
			out.write("\"].call(this");
			if (param.length() > 0)
			{
				out.write(',');
				out.write(param);
			}
			out.write(");}catch(ex){if(eterna_debug >= ED_FN_CALLED){eterna_fn_stack.push(new Array(\"");
			this.stringCoder.toJsonString(out, key);
			out.write("\",$E.F[\"$ef_");
			this.stringCoder.toJsonString(out, key);
			out.write("\"]");
			String[] params = StringTool.separateString(param, ",", true);
			for (int i = 0; i < params.length; i++)
			{
				out.write(",\"");
				out.write(params[i]);
				out.write("\",");
				out.write(params[i]);
			}
			out.write("));_eterna.printException(ex);eterna_fn_stack.pop();}throw ex;}}");
		}
		else
		{
			out.write('"');
			this.stringCoder.toJsonString(out, key);
			out.write("\":function(");
			out.write(param);
			out.write("){");
			out.write(fn.getBody());
			out.write('}');
		}
	}

	protected void printResource(Writer out, String name, Resource resource)
			throws IOException, EternaException
	{
		out.write('"');
		this.stringCoder.toJsonString(out, name);
		out.write("\":function(){var resArray=");
		this.dataPrinter.printIterator(out, resource.getParsedRessource());
		out.write(";return eterna_getResourceValue(resArray,arguments);}");
	}

	public void printView(Writer out, AppData data)
			throws IOException, EternaException
	{
		this.printView(out, data, null);
	}

	public void printView(Writer out, AppData data, Map cache)
			throws IOException, EternaException
	{
		String dataType = this.getDataType(data);
		boolean webData = true;
		boolean restData = false;
		if (DATA_TYPE_DATA.equals(dataType))
		{
			webData = false;
		}
		else if (DATA_TYPE_REST.equals(dataType))
		{
			webData = false;
			restData = true;
		}

		out.write('{');

		if (webData)
		{
			out.write("G:{");
			out.write(this.getFactory().getViewGlobalSetting());
			out.write("},\n");
		}

		if (!restData)
		{
			out.write("D:{root:\"");
			this.stringCoder.toJsonString(out, data.contextRoot);
			out.write("\",modelNameTag:\"");
			this.stringCoder.toJsonString(out, this.getFactory().getModelNameTag());
			out.write('"');
			if (data.modelName != null)
			{
				out.write(",modelName:\"");
				this.stringCoder.toJsonStringWithoutCheck(out, data.modelName);
				out.write('"');
			}
		}
		this.dataPrinter.printData(out, data.dataMap, !restData);
		if (!restData)
		{
			out.write('}');
		}

		if (webData)
		{
			if (cache != null && cache.size() > 0)
			{
				out.write(",\ncache:");
				this.dataPrinter.printMap(out, cache);
			}

			data.addSpcialData(VIEW_CACHE, DYNAMIC_VIEW, null);

			if (this.mc == null || this.getDebug() >= ETERNA_VIEW_DEBUG_BASE)
			{
				synchronized (this)
				{
					if (this.mc == null || this.getDebug() >= ETERNA_VIEW_DEBUG_BASE)
					{
						Writer oldOut = out;
						if (this.getDebug() < ETERNA_VIEW_DEBUG_BASE)
						{
							this.mc = new MemoryChars(1, 128);
							out = mc.getWriter();
						}

						Iterator tmpTypicals = this.viewRes.getTypicalComponentNames().iterator();
						while (tmpTypicals.hasNext())
						{
							String name = (String) tmpTypicals.next();
							Component com = this.getFactory().getTypicalComponent(name);
							if (com != null)
							{
								data.addSpcialData(View.TYPICAL_COMPONENTS_MAP, name, com);
							}
						}

						out.write(",\nV:[");
						boolean hasComponent = false;
						Iterator itr = this.getComponents();
						while (itr.hasNext())
						{
							if (hasComponent)
							{
								out.write(',');
							}
							else
							{
								hasComponent = true;
							}
							Component com = (Component) itr.next();
							com.print(out, data, this);
						}
						out.write(']');

						if (this.initScript != null)
						{
							out.write(",\ninit:\"");
							this.stringCoder.toJsonStringWithoutCheck(out, this.initScript);
							out.write('"');
						}

						if (this.beforeInit != null)
						{
							out.write(",beforeInit:\"");
							this.stringCoder.toJsonStringWithoutCheck(out, this.beforeInit);
							out.write('"');
						}

						Map typical = data.getSpcialDataMap(TYPICAL_COMPONENTS_MAP, true);
						data.setSpcialDataMap(USED_TYPICAL_COMPONENTS, typical);
						if (typical != null)
						{
							out.write(",\nT:{");
							this.printTypical(out, data, typical, null);
							out.write('}');
						}

						Map fnMap = (Map) data.getSpcialData(VIEW_CACHE, DYNAMIC_FUNCTIONS);
						if (fnMap != null)
						{
							ViewTool.putAllFunction(fnMap, this.viewRes.getFunctionMap());
						}
						else
						{
							fnMap = this.viewRes.getFunctionMap();
						}
						Iterator entrys = fnMap.entrySet().iterator();
						if (fnMap.size() > 0)
						{
							out.write(",\nF:{");
							boolean hasFunction = false;
							while (entrys.hasNext())
							{
								Map.Entry entry = (Map.Entry) entrys.next();
								String key = (String) entry.getKey();
								Function fn = (Function) entry.getValue();
								if (fn != null)
								{
									if (hasFunction)
									{
										out.write(',');
									}
									else
									{
										hasFunction = true;
									}
									this.printFunction(out, data, key, fn);
								}
							}
							out.write('}');
						}

						Set resourceSet = (Set) data.getSpcialData(VIEW_CACHE, DYNAMIC_RESOURCE_NAMES);
						if (resourceSet != null)
						{
							resourceSet.addAll(this.viewRes.getResourceNames());
						}
						else
						{
							resourceSet = this.viewRes.getResourceNames();
						}
						if (resourceSet.size() > 0)
						{
							Iterator resources = resourceSet.iterator();
							out.write(",\nR:{");
							boolean hasResource = false;
							while (resources.hasNext())
							{
								String name = (String) resources.next();
								Resource resource = this.getFactory().getResource(name);
								if (resource != null)
								{
									if (hasResource)
									{
										out.write(',');
									}
									else
									{
										hasResource = true;
									}
									this.printResource(out, name, resource);
								}
								else
								{
									log.error("Not found the resource:[" + name + "].");
								}
							}
							out.write('}');
						}

						out = oldOut;
					}
				}
			}

			if (this.getDebug() < ETERNA_VIEW_DEBUG_BASE)
			{
				Utility.copyChars(this.mc.getReader(), out);
			}

			// 如果是动态视图, 则不能缓存
			if ("1".equals(data.getSpcialData(VIEW_CACHE, DYNAMIC_VIEW)))
			{
				this.mc = null;
			}
		}

		out.write('}');
	}

	/**
	 * 生成typical控件.
	 *
	 * @param typicalMap   当前要生成的typical控件
	 * @param allMap       所有生成的typical控件
	 */
	private void printTypical(Writer out, AppData data, Map typicalMap, Map allMap)
			throws IOException, EternaException
	{
		Map typical = typicalMap;
		if (allMap == null)
		{
			allMap = typical;
		}
		else
		{
			// allMap 不为空，表示是递归进来的，所以要加个","
			out.write(',');
		}

		boolean first = true;
		Iterator itr = typical.entrySet().iterator();
		while (itr.hasNext())
		{
			Map.Entry entry = (Map.Entry) itr.next();
			String key = (String) entry.getKey();
			Component com = (Component) entry.getValue();
			if (first)
			{
				first = false;
				out.write('"');
			}
			else
			{
				out.write(",\"");
			}
			this.stringCoder.toJsonString(out, key);
			out.write("\":");
			com.print(out, data, this);
		}

		Map newTypical = data.getSpcialDataMap(TYPICAL_COMPONENTS_MAP, true);
		data.setSpcialDataMap(USED_TYPICAL_COMPONENTS, allMap);
		if (newTypical != null)
		{
			itr = newTypical.entrySet().iterator();
			while (itr.hasNext())
			{
				Map.Entry entry = (Map.Entry) itr.next();
				if (allMap.containsKey(entry.getKey()))
				{
					itr.remove();
				}
				else
				{
					allMap.put(entry.getKey(), entry.getValue());
				}
			}
			if (newTypical.size() > 0)
			{
				this.printTypical(out, data, newTypical, allMap);
			}
		}
	}

	public Object create()
	{
		return this;
	}

	public Class getObjectType()
	{
		return ViewImpl.class;
	}

	public boolean isSingleton()
	{
		return true;
	}

	public void destroy()
	{
	}

}