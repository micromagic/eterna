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

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import self.micromagic.eterna.dao.ResultFormat;
import self.micromagic.eterna.dao.ResultReader;
import self.micromagic.eterna.security.PermissionSet;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;

/**
 * ResultReader对象的外覆类.
 */
public class ReaderWrapper
		implements ResultReader
{
	/**
	 * @param base     需要被外覆的ResultReader对象
	 * @param newName  外覆后的ResultReader需要使用的新名称
	 */
	public ReaderWrapper(ResultReader base, String newName)
	{
		this.base = base;
		this.name = newName == null ? base.getName() : newName;
	}
	private final ResultReader base;
	private final String name;

	/**
	 * 设置是否需要有format.
	 */
	public void setNeedFormat(boolean need)
	{
		this.needFormat = need;
	}
	private boolean needFormat = true;

	public void initialize(EternaFactory factory)
			throws EternaException
	{
		this.base.initialize(factory);
	}

	public int getType()
			throws EternaException
	{
		return this.base.getType();
	}

	public String getFormatName()
			throws EternaException
	{
		if (this.needFormat)
		{
			return this.base.getFormatName();
		}
		return null;
	}

	public ResultFormat getFormat()
			throws EternaException
	{
		if (this.needFormat)
		{
			return this.base.getFormat();
		}
		return null;
	}

	public String getName()
			throws EternaException
	{
		return this.name;
	}

	public String getColumnName()
			throws EternaException
	{
		return this.base.getColumnName();
	}

	public String getAlias()
			throws EternaException
	{
		return this.base.getAlias();
	}

	public int getColumnIndex()
			throws EternaException
	{
		return this.base.getColumnIndex();
	}

	public boolean isValid()
			throws EternaException
	{
		return this.base.isValid();
	}

	public boolean isUseAlias()
			throws EternaException
	{
		return this.base.isUseAlias();
	}

	public boolean isUseColumnIndex()
			throws EternaException
	{
		return this.base.isUseColumnIndex();
	}

	public Object getAttribute(String name)
			throws EternaException
	{
		return this.base.getAttribute(name);
	}

	public String[] getAttributeNames()
			throws EternaException
	{
		return this.base.getAttributeNames();
	}

	public PermissionSet getPermissionSet()
			throws EternaException
	{
		return this.base.getPermissionSet();
	}

	public String getCaption()
			throws EternaException
	{
		return this.base.getCaption();
	}

	public Object readResult(ResultSet rs)
			throws SQLException
	{
		return this.base.readResult(rs);
	}

	public Object readCall(CallableStatement call, int index)
			throws SQLException
	{
		return this.base.readCall(call, index);
	}

	public Object readObject(Object obj)
			throws EternaException
	{
		return this.base.readObject(obj);
	}

}
