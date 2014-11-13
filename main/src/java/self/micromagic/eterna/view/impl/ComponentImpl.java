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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.view.Component;
import self.micromagic.eterna.view.ComponentGenerator;
import self.micromagic.eterna.view.StringCoder;
import self.micromagic.eterna.view.ViewAdapter;
import self.micromagic.eterna.view.ViewAdapterGenerator;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;
import self.micromagic.util.container.PreFetchIterator;

/**
 * @author micromagic@sina.com
 */
public class ComponentImpl extends AbstractGenerator
		implements Component, ComponentGenerator
{
	protected StringCoder stringCoder;
	protected EternaFactory eternaFactory;

	protected String type;
	protected Component parent;

	protected String componentParam;
	protected boolean ignoreGlobalParam;
	protected String beforeInit;
	protected String initScript;
	protected String attributes;

	protected List componentList = new LinkedList();
	protected List eventList = new LinkedList();

	private ViewAdapterGenerator.ModifiableViewRes viewRes = null;
	protected boolean initialized = false;

	public void initialize(EternaFactory factory, Component parent)
			throws ConfigurationException
	{
		if (this.initialized)
		{
			return;
		}
		this.eternaFactory = factory;
		this.initialized = true;
		this.stringCoder = factory.getStringCoder();
		this.parent = parent;

		Iterator subComponentItr = this.componentList.iterator();
		while (subComponentItr.hasNext())
		{
			Component sub = (Component) subComponentItr.next();
			sub.initialize(factory, this);
		}

		Iterator eventItr = this.eventList.iterator();
		while (eventItr.hasNext())
		{
			Event event = (Event) eventItr.next();
			event.initialize(this);
		}

		this.initAttributes(factory, this.attributes == null ? "" : this.attributes);
	}

	public void initAttributes(EternaFactory factory, String attributes)
			throws ConfigurationException
	{
		if (attributes == null || "".equals(attributes))
		{
			return;
		}
		String[] arr = StringTool.separateString(Utility.resolveDynamicPropnames(attributes), ";", true);
		for (int i = 0; i < arr.length; i++)
		{
			int index = arr[i].indexOf('=');
			if (index != -1)
			{
				this.setAttribute(arr[i].substring(0, index).trim(), arr[i].substring(index + 1).trim());
			}
		}
	}

	public void print(Writer out, AppData data, ViewAdapter view)
			throws IOException, ConfigurationException
	{
		out.write('{');
		this.printBody(out, data, view);
		this.printSpecialBody(out, data, view);
		out.write('}');
	}

	public void printBody(Writer out, AppData data, ViewAdapter view)
			throws IOException, ConfigurationException
	{
		out.write("name:\"");
		this.stringCoder.toJsonString(out, this.getName());
		out.write("\",type:\"");
		this.stringCoder.toJsonString(out, this.getType());
		out.write('"');

		if (this.isIgnoreGlobalParam())
		{
			out.write(",ignoreGlobal:1");
		}
		if (this.getComponentParam() != null)
		{
			out.write(',');
			out.write(this.getComponentParam());
		}

		boolean nextItr = false;
		Iterator eventItr = this.getEvents();
		if (eventItr.hasNext())
		{
			out.write(",events:[");
			while (eventItr.hasNext())
			{
				if (nextItr)
				{
					out.write(',');
				}
				else
				{
					nextItr = true;
				}
				Event e = (Event) eventItr.next();
				view.printEvent(out, data, e);
			}
			out.write(']');
		}

		nextItr = false;
		Iterator subComponentItr = this.getSubComponents();
		if (subComponentItr.hasNext())
		{
			out.write(",subs:[");
			while (subComponentItr.hasNext())
			{
				if (nextItr)
				{
					out.write(',');
				}
				else
				{
					nextItr = true;
				}
				Component sub = (Component) subComponentItr.next();
				sub.print(out, data, view);
			}
			out.write(']');
		}

		if (!StringTool.isEmpty(this.getInitScript()))
		{
			out.write(",init:\"");
			this.stringCoder.toJsonStringWithoutCheck(out, this.getInitScript());
			out.write('\"');
		}

		if (!StringTool.isEmpty(this.getBeforeInit()))
		{
			out.write(",beforeInit:\"");
			this.stringCoder.toJsonStringWithoutCheck(out, this.getBeforeInit());
			out.write('"');
		}
	}

	public void printSpecialBody(Writer out, AppData data, ViewAdapter view)
			throws IOException, ConfigurationException
	{
	}

	public String getType()
			throws ConfigurationException
	{
		return this.type;
	}

	public void setType(String type)
			throws ConfigurationException
	{
		this.type = type;
	}

	public Component getParent()
			throws ConfigurationException
	{
		return parent;
	}

	public Iterator getSubComponents()
			throws ConfigurationException
	{
		return new PreFetchIterator(this.componentList.iterator(), false);
	}

	public void addComponent(Component com)
			throws ConfigurationException
	{
		this.componentList.add(com);
	}

	public void deleteComponent(Component com)
			throws ConfigurationException
	{
		this.componentList.remove(com);
	}

	public void clearComponents()
			throws ConfigurationException
	{
		this.componentList.clear();
	}

	public Iterator getEvents()
			throws ConfigurationException
	{
		return new PreFetchIterator(this.eventList.iterator(), false);
	}

	public void addEvent(Event event)
			throws ConfigurationException
	{
		this.eventList.add(event);
	}

	public void deleteEvent(Event event)
			throws ConfigurationException
	{
		this.eventList.remove(event);
	}

	public void clearEvent()
			throws ConfigurationException
	{
		this.eventList.clear();
	}

	public boolean isIgnoreGlobalParam()
			throws ConfigurationException
	{
		return this.ignoreGlobalParam;
	}

	public void setIgnoreGlobalParam(boolean ignore)
			throws ConfigurationException
	{
		this.ignoreGlobalParam = ignore;
	}

	public String getComponentParam()
			throws ConfigurationException
	{
		return this.componentParam;
	}

	public void setComponentParam(String param)
			throws ConfigurationException
	{
		this.componentParam = param;
	}

	public String getBeforeInit()
			throws ConfigurationException
	{
		return this.beforeInit;
	}

	public void setBeforeInit(String condition)
			throws ConfigurationException
	{
		this.beforeInit = condition;
	}

	public String getInitScript()
			throws ConfigurationException
	{
		return this.initScript;
	}

	public void setInitScript(String body)
			throws ConfigurationException
	{
		this.initScript = body;
	}

	public void setAttributes(String attributes)
			throws ConfigurationException
	{
		this.attributes = attributes;
	}

	public EternaFactory getFactory()
	{
		return this.eternaFactory;
	}

	protected ViewAdapterGenerator.ModifiableViewRes getModifiableViewRes()
			throws ConfigurationException
	{
		if (this.viewRes == null)
		{
			this.viewRes = new ModifiableViewResImpl();
			this.beforeInit = ViewTool.dealScriptPart(
					this.viewRes, this.beforeInit, ViewTool.GRAMMER_TYPE_EXPRESSION, this.getFactory());
			this.initScript = ViewTool.dealScriptPart(
					this.viewRes, this.initScript, ViewTool.GRAMMER_TYPE_EXPRESSION, this.getFactory());
			this.componentParam = this.dealParamPart(this.componentParam, this.viewRes);

			Iterator eventItr = this.getEvents();
			while (eventItr.hasNext())
			{
				Event e = (Event) eventItr.next();
				this.viewRes.addAll(e.getViewRes());
			}

			Iterator subComponentItr = this.getSubComponents();
			while (subComponentItr.hasNext())
			{
				Component sub = (Component) subComponentItr.next();
				this.viewRes.addAll(sub.getViewRes());
			}
		}
		return this.viewRes;
	}

	/**
	 * 处理参数部分的脚本, 如: component-param, init-param.
	 */
	protected String dealParamPart(String param, ViewAdapterGenerator.ModifiableViewRes viewRes)
			throws ConfigurationException
	{
		// 为空或者没有代码都返回null
		if (param == null || param.trim().length() == 0)
		{
			return null;
		}
		return ViewTool.dealScriptPart(viewRes, param, ViewTool.GRAMMER_TYPE_JSON, this.getFactory());
	}

	public ViewAdapter.ViewRes getViewRes()
			throws ConfigurationException
	{
		return this.getModifiableViewRes();
	}

	public Component createComponent()
			throws ConfigurationException
	{
		return this;
	}

	public Object create()
			throws ConfigurationException
	{
		return this.createComponent();
	}

	public static class EventImpl extends AbstractGenerator
			implements Event, EventGenerator
	{
		private String param;
		private String scriptBody = "";

		protected Component component;
		protected ViewAdapterGenerator.ModifiableViewRes viewRes = null;

		public void initialize(Component component)
		{
			this.component = component;
		}

		public String getScriptParam()
			throws ConfigurationException
		{
			return this.param;
		}

		public void setScriptParam(String param)
			throws ConfigurationException
		{
			this.param = param;
		}

		public String getScriptBody()
			throws ConfigurationException
		{
			return this.scriptBody;
		}

		public void setScriptBody(String body)
			throws ConfigurationException
		{
			this.scriptBody = body;
		}

		public Component getComponent()
		{
			return this.component;
		}

		public ViewAdapter.ViewRes getViewRes()
				throws ConfigurationException
		{
			if (this.viewRes == null)
			{
				this.viewRes = new ModifiableViewResImpl();
				this.scriptBody = ViewTool.dealScriptPart(
						this.viewRes, this.scriptBody, ViewTool.GRAMMER_TYPE_EXPRESSION, this.getComponent().getFactory());
				this.param = ViewTool.dealScriptPart(
						this.viewRes, this.param, ViewTool.GRAMMER_TYPE_NONE, this.getComponent().getFactory());
			}
			return this.viewRes;
		}

		public Event createEvent()
			throws ConfigurationException
		{
			return this;
		}

		public Object create()
			throws ConfigurationException
		{
			return this.createEvent();
		}

	}

}