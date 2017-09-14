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

package self.micromagic.eterna.search.impl;

import java.util.List;

import self.micromagic.eterna.dao.impl.ScriptParser;
import self.micromagic.eterna.dao.preparer.CreaterManager;
import self.micromagic.eterna.dao.preparer.PreparerCreater;
import self.micromagic.eterna.dao.preparer.ValuePreparer;
import self.micromagic.eterna.search.ConditionBuilder;
import self.micromagic.eterna.search.ConditionProperty;
import self.micromagic.eterna.security.PermissionSet;
import self.micromagic.eterna.share.AttributeManager;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.Tool;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.StringTool;

/**
 * 条件配置信息的实现类.
 */
class ConditionPropertyImpl
		implements ConditionProperty
{
	String name;
	String columnName;
	String columnCaption = null;
	boolean ignore = false;
	int columnType;
	String prepareName;
	private PreparerCreater prepare;
	boolean visible = true;
	String inputType;
	Object defaultObj;
	String permissionConfig;
	private PermissionSet permissionSet = null;
	AttributeManager attributes;

	String listName;
	private List builderList;
	private String builderName;
	private ConditionBuilder defaultBuilder;
	boolean useDefaultConditionBuilder = false;

	public void initialize(EternaFactory factory)
			throws EternaException
	{
		if (this.listName != null)
		{
			this.builderList = factory.getConditionBuilderList(this.listName);
		}
		if (this.builderName != null)
		{
			this.defaultBuilder = factory.getConditionBuilder(this.builderName);
		}
		else if (this.builderList != null && !this.builderList.isEmpty())
		{
			this.defaultBuilder = (ConditionBuilder) this.builderList.iterator().next();
		}

		if (!StringTool.isEmpty(this.permissionConfig))
		{
			this.permissionSet = factory.createPermissionSet(this.permissionConfig);
		}
		this.prepare = CreaterManager.createPreparerCreater(
				this.columnType, this.prepareName, factory);

		this.columnName = ScriptParser.checkNameWithKey(this.columnName);
		if (this.columnCaption == null)
		{
			this.columnCaption = Tool.translateCaption(factory, this.getName());
		}
	}

	public String getName()
	{
		return this.name;
	}

	public String getColumnName()
	{
		return this.columnName;
	}

	public String getColumnCaption()
	{
		return this.columnCaption;
	}

	public String getColumnTypeName()
	{
		return TypeManager.getTypeName(this.columnType);
	}

	public int getColumnType()
	{
		return this.columnType;
	}

	public ValuePreparer createValuePreparer(String value)
			throws EternaException
	{
		return this.prepare.createPreparer(value);
	}

	public ValuePreparer createValuePreparer(Object value)
			throws EternaException
	{
		return this.prepare.createPreparer(value);
	}

	public boolean isIgnore()
	{
		return this.ignore;
	}

	public boolean isVisible()
	{
		return this.visible;
	}

	public String getConditionInputType()
	{
		return this.inputType;
	}

	public Object getDefaultValue()
	{
		return this.defaultObj;
	}

	public Object getAttribute(String name)
	{
		return this.attributes.getAttribute(name);
	}

	public String[] getAttributeNames()
	{
		return this.attributes.getAttributeNames();
	}

	public PermissionSet getPermissionSet()
	{
		return this.permissionSet;
	}

	public String getConditionBuilderListName()
	{
		return this.listName;
	}

	public boolean isUseDefaultConditionBuilder()
	{
		return this.useDefaultConditionBuilder;
	}

	public void setDefaultConditionBuilderName(String name)
	{
		this.builderName = name;
	}

	public ConditionBuilder getDefaultConditionBuilder()
	{
		return this.defaultBuilder;
	}

	public List getConditionBuilderList()
	{
		return this.builderList;
	}

}
