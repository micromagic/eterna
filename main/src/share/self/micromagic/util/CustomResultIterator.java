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

import java.util.LinkedList;
import java.sql.SQLException;

import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.security.Permission;
import self.micromagic.eterna.sql.ResultIterator;
import self.micromagic.eterna.sql.ResultReaderManager;
import self.micromagic.eterna.sql.ResultRow;
import self.micromagic.eterna.sql.impl.AbstractResultIterator;
import self.micromagic.eterna.sql.impl.ResultRowImpl;

public class CustomResultIterator extends AbstractResultIterator
		implements ResultIterator
{
	private int recordCount = 0;
	private Permission permission;
	private int rowNum;

	public CustomResultIterator(ResultReaderManager rrm, Permission permission)
			throws ConfigurationException
	{
		super(rrm, permission);
		this.permission = permission;
		this.result = new LinkedList();
	}

	private CustomResultIterator(Permission permission)
	{
		this.permission = permission;
	}

	public ResultRow createRow(Object[] values)
			throws ConfigurationException
	{
		if (values.length != this.readerManager.getReaderCount())
		{
			throw new ConfigurationException("The values count must same as the ResultReaderManager's readers count.");
		}
		try
		{
			this.rowNum++;
			ResultRow row = new ResultRowImpl(values, this, this.rowNum, this.permission);
			this.result.add(row);
			return row;
		}
		catch (SQLException ex)
		{
			throw new ConfigurationException(ex);
		}
	}

	public void finishCreateRow()
	{
		this.resultItr = this.result.iterator();
		this.recordCount = this.result.size();
	}

	public int getRealRecordCount()
	{
		return this.recordCount;
	}

	public int getRecordCount()
	{
		return this.recordCount;
	}

	public boolean isRealRecordCountAvailable()
	{
		return true;
	}

	public boolean isHasMoreRecord()
	{
		return false;
	}

	public ResultIterator copy()
			throws ConfigurationException
	{
		CustomResultIterator ritr = new CustomResultIterator(this.permission);
		this.copy(ritr);
		return ritr;
	}

	protected void copy(ResultIterator copyObj)
			throws ConfigurationException
	{
		super.copy(copyObj);
		CustomResultIterator ritr = (CustomResultIterator) copyObj;
		ritr.recordCount = this.recordCount;
	}

}