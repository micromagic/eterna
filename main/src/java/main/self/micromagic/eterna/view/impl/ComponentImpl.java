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

import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.view.Component;
import self.micromagic.eterna.view.ComponentGenerator;
import self.micromagic.eterna.view.Event;
import self.micromagic.eterna.view.ModifiableViewRes;
import self.micromagic.eterna.view.StringCoder;
import self.micromagic.eterna.view.View;
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
	private String _attributes;

	protected List componentList = new LinkedList();
	protected List eventList = new LinkedList();

	private ModifiableViewRes viewRes = null;
	protected boolean initialized = false;

	public void initialize(EternaFactory factory, Component parent)
			throws EternaException
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

		this.initAttributes(factory, this._attributes == null ? "" : this._attributes);
	}

	public void initAttributes(EternaFactory factory, String attributes)
			throws EternaException
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

	public void print(Writer out, AppData data, View view)
			throws IOException, EternaException
	{
		out.write('{');
		this.printBody(out, data, view);
		this.printSpecialBody(out, data, view);
		out.write('}');
	}

	public void printBody(Writer out, AppData data, View view)
			throws IOException, EternaException
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

	public void printSpecialBody(Writer out, AppData data, View view)
			throws IOException, EternaException
	{
	}

	public String getType()
			throws EternaException
	{
		return this.type;
	}

	public void setType(String type)
			throws EternaException
	{
		this.type = type;
	}

	public Component getParent()
			throws EternaException
	{
		return parent;
	}

	public Iterator getSubComponents()
			throws EternaException
	{
		return new PreFetchIterator(this.componentList.iterator(), false);
	}

	public void addComponent(Component com)
			throws EternaException
	{
		this.componentList.add(com);
	}

	public void deleteComponent(Component com)
			throws EternaException
	{
		this.componentList.remove(com);
	}

	public void clearComponents()
			throws EternaException
	{
		this.componentList.clear();
	}

	public Iterator getEvents()
			throws EternaException
	{
		return new PreFetchIterator(this.eventList.iterator(), false);
	}

	public void addEvent(Event event)
			throws EternaException
	{
		this.eventList.add(event);
	}

	public void deleteEvent(Event event)
			throws EternaException
	{
		this.eventList.remove(event);
	}

	public void clearEvent()
			throws EternaException
	{
		this.eventList.clear();
	}

	public boolean isIgnoreGlobalParam()
			throws EternaException
	{
		return this.ignoreGlobalParam;
	}

	public void setIgnoreGlobalParam(boolean ignore)
			throws EternaException
	{
		this.ignoreGlobalParam = ignore;
	}

	public String getComponentParam()
			throws EternaException
	{
		return this.componentParam;
	}

	public void setComponentParam(String param)
			throws EternaException
	{
		this.componentParam = param;
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

	public void setAttributes(String attributes)
			throws EternaException
	{
		this._attributes = attributes;
	}

	public EternaFactory getFactory()
	{
		return this.eternaFactory;
	}

	protected ModifiableViewRes getModifiableViewRes()
			throws EternaException
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
	protected String dealParamPart(String param, ModifiableViewRes viewRes)
			throws EternaException
	{
		// 为空或者没有代码都返回null
		if (param == null || param.trim().length() == 0)
		{
			return null;
		}
		return ViewTool.dealScriptPart(viewRes, param, ViewTool.GRAMMER_TYPE_JSON, this.getFactory());
	}

	public View.ViewRes getViewRes()
			throws EternaException
	{
		return this.getModifiableViewRes();
	}

	public Component createComponent()
			throws EternaException
	{
		return this;
	}

	public Object create()
			throws EternaException
	{
		return this.createComponent();
	}

	public static class EventImpl extends AbstractGenerator
			implements Event, EventGenerator
	{
		private String param;
		private String scriptBody = "";

		protected Component component;
		protected ModifiableViewRes viewRes = null;

		public void initialize(Component component)
		{
			this.component = component;
		}

		public String getScriptParam()
			throws EternaException
		{
			return this.param;
		}

		public void setScriptParam(String param)
			throws EternaException
		{
			this.param = param;
		}

		public String getScriptBody()
			throws EternaException
		{
			return this.scriptBody;
		}

		public void setScriptBody(String body)
			throws EternaException
		{
			this.scriptBody = body;
		}

		public Component getComponent()
		{
			return this.component;
		}

		public View.ViewRes getViewRes()
				throws EternaException
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
			throws EternaException
		{
			return this;
		}

		public Object create()
			throws EternaException
		{
			return this.createEvent();
		}

	}

}