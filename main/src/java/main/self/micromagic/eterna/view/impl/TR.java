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

import java.util.Iterator;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.view.BaseManager;
import self.micromagic.eterna.view.Component;
import self.micromagic.eterna.view.ComponentGenerator;
import self.micromagic.eterna.view.TableForm;
import self.micromagic.eterna.view.TableList;
import self.micromagic.eterna.view.ViewAdapterGenerator;
import self.micromagic.eterna.view.ViewAdapter;
import self.micromagic.util.container.MultiIterator;

public class TR extends ComponentImpl
		implements Component, ComponentGenerator
{
	public static final String DEFAULT_TABLELIST_TR_ATTRIBUTE = "default.table-list.tr";
	public static final int TABLE_TYPE_FORM = 2;
	public static final int TABLE_TYPE_LIST = 1;

	protected int tableType = 0;
	protected Component baseComponent;
	protected String baseComponentName;

	private ViewAdapterGenerator.ModifiableViewRes viewRes = null;

	public TR()
	{
		this.type = "tr";
	}

	public TR(String name)
			throws EternaException
	{
		this();
		this.setName(name);
	}

	public void initialize(EternaFactory factory, Component parent)
			throws EternaException
	{
		if (this.initialized)
		{
			return;
		}
		super.initialize(factory, parent);
		String trName = this.baseComponentName != null ? this.baseComponentName :
				this.tableType != TABLE_TYPE_LIST ? null : (String) factory.getAttribute(DEFAULT_TABLELIST_TR_ATTRIBUTE);
		if (trName != null && trName.length() > 0)
		{
			this.baseComponent = factory.getTypicalComponent(trName);
			if (this.baseComponent == null)
			{
				log.warn("The Typical Component [" + trName + "] not found.");
			}
		}
		if (!this.isIgnoreGlobalParam() && this.baseComponent != null
				&& "tr".equalsIgnoreCase(this.baseComponent.getType()))
		{
			if (this.componentParam == null)
			{
				this.componentParam = this.baseComponent.getComponentParam();
			}
			if (this.beforeInit == null)
			{
				this.beforeInit = this.baseComponent.getBeforeInit();
			}
			else
			{
				String parentScript = this.baseComponent.getBeforeInit();
				this.beforeInit = ViewTool.addParentScript(this.beforeInit, parentScript);
			}
			if (this.initScript == null)
			{
				this.initScript = this.baseComponent.getInitScript();
			}
			else
			{
				String parentScript = this.baseComponent.getInitScript();
				this.initScript = ViewTool.addParentScript(this.initScript, parentScript);
			}
		}
		else
		{
			this.baseComponent = null;
			this.beforeInit = ViewTool.addParentScript(this.beforeInit, null);
			this.initScript = ViewTool.addParentScript(this.initScript, null);
		}

		if (this.tableType == TABLE_TYPE_LIST && this.beforeInit == null)
		{
			this.beforeInit = "checkResult=false;checkResult=(eg_temp.rowType==\"title\"||eg_temp.rowType==\"row\");";
		}
	}

	public void setName(String name)
			throws EternaException
	{
		super.setName(name);
		if (name == null)
		{
			return;
		}
		int preNameLength = 12;
		if (name.startsWith(TableList.TR_NAME_PERFIX))
		{
			this.tableType = TABLE_TYPE_LIST;
		}
		else if (name.startsWith(TableForm.TR_NAME_PERFIX))
		{
			this.tableType = TABLE_TYPE_FORM;
		}
		else
		{
			throw new EternaException("The name must start with [tableList_TR or tableForm_TR] for tr component.");
		}
		if (name.length() > preNameLength)
		{
			if (name.charAt(preNameLength) != '.')
			{
				throw new EternaException("If you want set plus base name, must start with \".\" for tr component.");
			}
			super.setName(name.substring(0, preNameLength));
			this.baseComponentName = name.substring(preNameLength + 1);
		}
	}

	public void setType(String type)
			throws EternaException
	{
		if (!"tr".equalsIgnoreCase(type))
		{
			throw new EternaException("The type must be [tr] for tr component.");
		}
	}

	public Iterator getSubComponents()
			throws EternaException
	{
		if (this.baseComponent != null && this.componentList.size() == 0)
		{
			return this.baseComponent.getSubComponents();
		}
		return super.getSubComponents();
	}

	public Iterator getEvents()
			throws EternaException
	{
		if (this.baseComponent == null)
		{
			return super.getEvents();
		}
		return new MultiIterator(this.baseComponent.getEvents(), super.getEvents());
	}

	protected ViewAdapterGenerator.ModifiableViewRes getModifiableViewRes()
			throws EternaException
	{
		if (this.viewRes == null)
		{
			this.viewRes = super.getModifiableViewRes();
			if (this.baseComponent != null)
			{
				this.viewRes.addAll(this.baseComponent.getViewRes());
			}
		}
		return this.viewRes;
	}

}