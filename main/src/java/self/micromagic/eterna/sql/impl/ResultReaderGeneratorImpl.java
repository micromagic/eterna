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

package self.micromagic.eterna.sql.impl;

import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.sql.ResultReader;
import self.micromagic.eterna.sql.ResultReaderGenerator;

public class ResultReaderGeneratorImpl extends AbstractGenerator
		implements ResultReaderGenerator
{
	private String formatName;
	private String type;
	private String orderName;
	private String permissions;
	private boolean htmlFilter = true;
	private boolean visible = true;

	private int width = -1;
	private String caption;

	private String columnName;
	private int columnIndex = -1;
	protected boolean useIndexOrName;

	public void setOrderName(String orderName)
	{
		this.orderName = orderName;
	}

	public void setColumnName(String columnName)
			throws ConfigurationException
	{
		if (this.columnIndex != -1)
		{
			throw new ConfigurationException(
					"Can't set the attribute 'colName' when given the attribute 'colIndex'.");
		}
		this.columnName = columnName;
		this.useIndexOrName = false;
	}

	public void setColumnIndex(int columnIndex)
			throws ConfigurationException
	{
		if (this.columnName != null)
		{
			throw new ConfigurationException(
					"Can't set the attribute 'colIndex' when given the attribute 'colName'.");
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

	public void setHtmlFilter(boolean filter)
	{
		this.htmlFilter = filter;
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public void setWidth(int width)
	{
		this.width = width;
	}

	public void setCaption(String caption)
	{
		this.caption = caption;
	}

	public Object create()
			throws ConfigurationException
	{
		return this.createReader();
	}

	public ResultReader createReader()
			throws ConfigurationException
	{
		this.type = this.type == null ? "Object" : this.type;
		ResultReader tmpReader = ResultReaders.createReader(this.type, this.name);
		if (!(tmpReader instanceof ResultReaders.ObjectReader))
		{
			return tmpReader;
		}
		ResultReaders.ObjectReader reader = (ResultReaders.ObjectReader) tmpReader;
		if (this.formatName != null)
		{
			reader.setFormatName(this.formatName);
		}
		if (this.orderName != null)
		{
			reader.setOrderName(this.orderName);
		}
		if (this.permissions != null)
		{
			reader.setPermission(this.permissions);
		}
		if (this.caption != null)
		{
			reader.setCaption(this.caption);
		}
		reader.setWidth(this.width);
		reader.setHtmlFilter(this.htmlFilter);
		reader.setVisible(this.visible);
		reader.setAttributes(this.attributes);
		if (this.useIndexOrName)
		{
			reader.setColumnIndex(this.columnIndex);
		}
		else
		{
			this.columnName = this.columnName == null ? this.name : this.columnName;
			reader.setColumnName(this.columnName);
		}
		return reader;
	}

}