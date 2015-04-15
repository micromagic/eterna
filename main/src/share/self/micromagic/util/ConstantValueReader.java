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

package self.micromagic.util;

import java.sql.ResultSet;
import java.sql.CallableStatement;

import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.eterna.share.Tool;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.sql.ResultReaderGenerator;
import self.micromagic.eterna.sql.ResultReader;
import self.micromagic.eterna.sql.ResultFormat;
import self.micromagic.eterna.security.PermissionSet;

public class ConstantValueReader extends AbstractGenerator
		implements ResultReader, ResultReaderGenerator
{
	private int width = -1;
	private String caption = null;

	protected boolean htmlFilter = true;
	protected boolean visible = true;

	protected PermissionSet permissionSet = null;
	protected String theValue = "";

	public Object create()
			throws EternaException
	{
		return this.createReader();
	}

	public ResultReader createReader()
	{
		return this;
	}

	public void initialize(EternaFactory factory)
			throws EternaException
	{
		this.theValue = (String) this.getAttribute("value");
		if (this.permissionSet != null)
		{
			this.permissionSet.initialize(factory);
		}
		if (this.caption == null)
		{
			this.caption = Tool.translateCaption(factory, this.getName());
		}
	}

	public int getType()
	{
		return TypeManager.TYPE_STRING;
	}

	public boolean isIgnore()
	{
		return false;
	}

	public String getFormatName()
	{
		return null;
	}

	public ResultFormat getFormat()
	{
		return null;
	}

	public String getOrderName()
	{
		return null;
	}

	public String getColumnName()
	{
		return this.name;
	}

	public int getColumnIndex()
	{
		return -1;
	}

	public void setHtmlFilter(boolean filter)
	{
		this.htmlFilter = filter;
	}

	public boolean needHtmlFilter()
	{
		return this.htmlFilter;
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

	public boolean isVisible()
	{
		return this.visible;
	}

	public boolean isValid()
	{
		return true;
	}

	public boolean isUseColumnName()
	{
		return true;
	}

	public boolean isUseColumnIndex()
	{
		return false;
	}

	public void setPermissions(String permissions)
	{
		if (permissions == null || permissions.trim().length() == 0)
		{
			return;
		}
		this.permissionSet = new PermissionSet(
				StringTool.separateString(permissions, ",", true));
	}

	public PermissionSet getPermissionSet()
	{
		return this.permissionSet;
	}

	public void setCaption(String caption)
	{
		this.caption = caption;
	}

	public String getFilledCaption()
	{
		if (this.caption == null)
		{
			return this.isUseColumnIndex() ?
					"col_" + this.getColumnIndex() : this.getColumnName();
		}
		return this.caption;
	}

	public String getCaption()
	{
		return this.caption;
	}

	public void setWidth(int width)
	{
		this.width = width;
	}

	public int getWidth()
	{
		return this.width;
	}

	public Object readResult(ResultSet rs)
	{
		return this.theValue;
	}

	public Object readCall(CallableStatement call, int index)
	{
		return this.theValue;
	}

	public Object readObject(Object obj)
	{
		return this.theValue;
	}

	public void setColumnName(String columnName) {}

	public void setColumnIndex(int columnIndex) {}

	public void setOrderName(String orderName) {}

	public void setFormatName(String name) {}

	public void setType(String type) {}

}