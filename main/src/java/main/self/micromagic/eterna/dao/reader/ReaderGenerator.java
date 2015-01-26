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

package self.micromagic.eterna.dao.reader;

import self.micromagic.eterna.dao.ResultReader;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaException;

/**
 * reader对象的构造器.
 */
public class ReaderGenerator extends AbstractGenerator
{
	private String formatName;
	private String type;
	private String columnName;
	private String permissions;

	private String caption;

	private String alias;
	private int columnIndex = -1;
	protected boolean useIndexOrName;

	public void setColumnName(String columnName)
	{
		this.columnName = columnName;
	}

	public void setAlias(String alias)
			throws EternaException
	{
		if (this.columnIndex != -1)
		{
			throw new EternaException(
					"Can't set the attribute 'alias' when given the attribute 'colIndex'.");
		}
		this.alias = alias;
		this.useIndexOrName = false;
	}

	public void setColumnIndex(int columnIndex)
			throws EternaException
	{
		if (this.alias != null)
		{
			throw new EternaException(
					"Can't set the attribute 'colIndex' when given the attribute 'alias'.");
		}
		this.columnIndex = columnIndex;
		this.useIndexOrName = true;
	}

	public void setFormatName(String name)
	{
		this.formatName = name;
	}

	public void setPermissions(String permissions)
	{
		this.permissions = permissions;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public void setCaption(String caption)
	{
		this.caption = caption;
	}

	public Object create()
			throws EternaException
	{
		return this.createReader();
	}

	public ResultReader createReader()
			throws EternaException
	{
		this.type = this.type == null ? "Object" : this.type;
		ObjectReader reader = (ObjectReader) ReaderFactory.createReader(
				this.type, this.name);
		if (this.formatName != null)
		{
			reader.setFormatName(this.formatName);
		}
		if (this.columnName != null)
		{
			reader.setColumnName(this.columnName);
		}
		if (this.permissions != null)
		{
			reader.setPermission(this.permissions);
		}
		if (this.caption != null)
		{
			reader.setCaption(this.caption);
		}
		reader.setAttributes(this.attributes);
		if (this.useIndexOrName)
		{
			reader.setColumnIndex(this.columnIndex);
		}
		else
		{
			this.alias = this.alias == null ? this.name : this.alias;
			reader.setAlias(this.alias);
		}
		return reader;
	}

}