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

package self.micromagic.eterna.dao.impl;

import self.micromagic.eterna.dao.Entity;
import self.micromagic.eterna.dao.EntityItem;
import self.micromagic.eterna.security.PermissionSet;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.AttributeManager;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.TypeManager;

public class EntityItemGenerator extends AbstractGenerator
{
	public Object create()
	{
		return new EntityItemImpl(this.attributes, this.getName(),
				this.columnName == null ? this.getName() : this.columnName,
				this.type, this.caption);
	}

	public void setColumnName(String colName)
	{
		this.columnName = colName;
	}
	private String columnName;

	public void setType(String type)
	{
		this.type = type;
	}
	private String type;

	public void setCaption(String caption)
	{
		this.caption = caption;
	}
	private String caption;

	public void setPermission()
	{
		// TODO Auto-generated method stub
	}

}

class EntityItemImpl
		implements EntityItem
{
	public EntityItemImpl(AttributeManager attrs, String name, String colName,
			String type, String caption)
	{
		this.attrs = attrs;
		this.name = name;
		this.colName = colName;
		this.caption = caption;
		this.type = TypeManager.getTypeId(type);
	}
	private final AttributeManager attrs;

	public void initialize(Entity entity)
			throws EternaException
	{
		this.attrs.convertType(entity.getFactory(), "item");
		this.entity = entity;
	}
	private Entity entity;

	public Object getAttribute(String name)
	{
		return this.attrs.getAttribute(name);
	}

	public String[] getAttributeNames()
	{
		return this.attrs.getAttributeNames();
	}

	public Entity getEntity()
	{
		return this.entity;
	}

	public String getName()
	{
		return this.name;
	}
	private final String name;

	public String getColumnName()
	{
		return this.colName;
	}
	private final String colName;

	public int getType()
	{
		return this.type;
	}
	private final int type;

	public String getCaption()
	{
		return this.caption;
	}
	private final String caption;

	public PermissionSet getPermissionSet()
			throws EternaException
	{
		// TODO Auto-generated method stub
		return null;
	}

}
