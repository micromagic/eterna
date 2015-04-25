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

package self.micromagic.eterna.dao.reader;

import java.sql.CallableStatement;
import java.sql.ResultSet;

import self.micromagic.eterna.dao.ResultFormat;
import self.micromagic.eterna.dao.ResultReader;
import self.micromagic.eterna.security.PermissionSet;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.TypeManager;

/**
 * 一个标志为无效的ResultReader, 主要用于替换没有权限的ResultReader.
 */
public class InvalidReader
		implements ResultReader
{
	protected String name;

	public InvalidReader(String name)
	{
		this.name = name;
	}

	public void initialize(EternaFactory factory)
	{
	}

	public int getType()
	{
		return TypeManager.TYPE_NULL;
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

	public String getColumnName()
	{
		return this.name;
	}

	public String getAlias()
	{
		return this.name;
	}

	public boolean isUseAlias()
	{
		return true;
	}

	public int getColumnIndex()
	{
		return -1;
	}

	public boolean isValid()
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

	public String getCaption()
	{
		return null;
	}

	public Object getAttribute(String name)
	{
		return null;
	}

	public String[] getAttributeNames()
	{
		return null;
	}

	public Object readResult(ResultSet rs)
	{
		return null;
	}

	public Object readCall(CallableStatement call, int index)
	{
		return null;
	}

	public Object readObject(Object obj)
	{
		return null;
	}

}