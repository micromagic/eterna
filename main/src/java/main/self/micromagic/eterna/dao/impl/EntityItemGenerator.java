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

package self.micromagic.eterna.dao.impl;

import java.util.Iterator;
import java.util.Set;

import self.micromagic.eterna.dao.Entity;
import self.micromagic.eterna.dao.EntityItem;
import self.micromagic.eterna.security.PermissionSet;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.Tool;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.StringTool;
import self.micromagic.util.converter.BooleanConverter;

public class EntityItemGenerator extends AbstractGenerator
		implements EntityItem
{
	protected boolean initialized;

	public void initialize(Entity entity)
			throws EternaException
	{
		if (this.initialized)
		{
			return;
		}
		this.initialized = true;
		EternaFactory factory = entity.getFactory();
		this.attributes.convertType(factory, "item");
		this.checkEmptyAttrs(factory);
		if (this.permissionConfig != null)
		{
			this.permissionSet = factory.createPermissionSet(this.permissionConfig);
		}
	}

	/**
	 * 根据配置的名称列表检查是否有值为空的属性并将其去除.
	 */
	protected void checkEmptyAttrs(EternaFactory factory)
	{
		Set names = Tool.getCheckEmptyAttrs(factory);
		if (names != null)
		{
			int count = names.size();
			Iterator itr = names.iterator();
			for (int i = 0; i < count; i++)
			{
				String name = (String) itr.next();
				Object tmp = this.attributes.getAttribute(name);
				if (tmp != null && StringTool.isEmpty(tmp))
				{
					this.attributes.removeAttribute(name);
				}
			}
		}
	}

	public String getColumnName()
	{
		return this.columnName != null ? this.columnName : this.getName();
	}

	public int getType()
	{
		return this.type;
	}
	protected int type;

	public String getCaption()
	{
		return this.caption;
	}

	public PermissionSet getPermissionSet()
			throws EternaException
	{
		return this.permissionSet;
	}
	protected PermissionSet permissionSet;

	public void merge(EntityItem other)
			throws EternaException
	{
		if (other == null || BooleanConverter.toBoolean(this.getAttribute(IGNORE_PARENT)))
		{
			return;
		}
		if (this.initialized)
		{
			throw new EternaException("Can't merge item [" + this.getName()
					+ "], because it has initialized.");
		}
		if (this.caption == null)
		{
			this.caption = other.getCaption();
		}
		if (this.type == TypeManager.TYPE_NONE)
		{
			this.type = other.getType();
		}
		if (this.columnName == null)
		{
			this.columnName = other.getColumnName();
		}
		if (this.permissionConfig == null)
		{
			this.permissionSet = other.getPermissionSet();
		}
		String[] names = other.getAttributeNames();
		for (int i = 0; i < names.length; i++)
		{
			if (!this.attributes.hasAttribute(names[i]))
			{
				this.attributes.setAttribute(names[i], other.getAttribute(names[i]));
			}
		}
	}

	public Object create()
	{
		this.type = TypeManager.getTypeId(this.typeName);
		return this;
	}

	public void setColumnName(String colName)
	{
		this.columnName = colName;
	}
	protected String columnName;

	public void setTypeName(String type)
	{
		this.typeName = type;
	}
	private String typeName;

	public void setCaption(String caption)
	{
		this.caption = caption;
	}
	private String caption;

	public void setPermission(String permission)
	{
		this.permissionConfig = permission;
	}
	protected String permissionConfig;

}
