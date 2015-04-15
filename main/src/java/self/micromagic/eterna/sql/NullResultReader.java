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

package self.micromagic.eterna.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.CallableStatement;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.security.PermissionSet;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.TypeManager;

/**
 * 一个空的ResultReader, 主要用于替换没有权限的ResultReader.
 */
public class NullResultReader
		implements ResultReader
{
	protected String name;

	public NullResultReader(String name)
	{
		this.name = name;
	}

	public void initialize(EternaFactory factory)
	{
	}

	public int getType()
	{
		return TypeManager.TYPE_IGNORE;
	}

	public boolean isIgnore()
	{
		return true;
	}

	public ResultFormat getFormat()
	{
		return null;
	}

	public String getFormatName()
	{
		return null;
	}

	public String getName()
	{
		return this.name;
	}

	public String getOrderName()
	{
		return this.name;
	}

	public String getColumnName()
	{
		return this.name;
	}

	public boolean isUseColumnName()
	{
		return true;
	}

	public int getColumnIndex()
	{
		return -1;
	}

	public boolean needHtmlFilter() throws EternaException
	{
		return false;
	}

	public boolean isValid() throws EternaException
	{
		return false;
	}

	public boolean isUseColumnIndex()
	{
		return false;
	}

	public PermissionSet getPermissionSet()
	{
		return null;
	}

	public String getCaption() throws EternaException
	{
		return null;
	}

	public String getFilledCaption() throws EternaException
	{
		return null;
	}

	public int getWidth() throws EternaException
	{
		return 0;
	}

	public boolean isVisible() throws EternaException
	{
		return false;
	}

	public Object getAttribute(String name) throws EternaException
	{
		return null;
	}

	public String[] getAttributeNames() throws EternaException
	{
		return null;
	}

	public Object readResult(ResultSet rs)
			throws SQLException
	{
		return null;
	}

	public Object readCall(CallableStatement call, int index)
	{
		return null;
	}

	public Object readObject(Object obj) throws EternaException
	{
		return null;
	}

}