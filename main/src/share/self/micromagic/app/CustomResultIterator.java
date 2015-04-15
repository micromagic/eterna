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

package self.micromagic.app;

import java.util.LinkedList;
import java.util.List;
import java.sql.SQLException;

import self.micromagic.eterna.security.Permission;
import self.micromagic.eterna.sql.ResultIterator;
import self.micromagic.eterna.sql.ResultReaderManager;
import self.micromagic.eterna.sql.ResultRow;
import self.micromagic.eterna.sql.impl.AbstractResultIterator;
import self.micromagic.eterna.sql.impl.ResultRowImpl;
import self.micromagic.eterna.share.EternaException;

/**
 * @deprecated
 * @see self.micromagic.util.CustomResultIterator
 */
public class CustomResultIterator extends AbstractResultIterator
		implements ResultIterator
{
	private int recordCount = 0;
	private Permission permission;
	private int rowNum;

	/**
	 * @deprecated
	 * @see #CustomResultIterator(ResultReaderManager, Permission)
	 */
	public CustomResultIterator(List readerList)
	{
		super(readerList);
		this.result = new LinkedList();
	}

	public CustomResultIterator(ResultReaderManager rrm, Permission permission)
			throws EternaException
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
			throws EternaException
	{
		return this.createRow(values, true);
	}

	public ResultRow createRow(Object[] values, boolean autoAdd)
			throws EternaException
	{
		if (this.readerManager == null)
		{
			throw new EternaException("Must use [CustomResultIterator(ResultReaderManager, Permission)] "
					+ "to constructor this object.");
		}
		try
		{
			this.rowNum++;
			ResultRow row = new ResultRowImpl(values, this, this.rowNum, this.permission);
			if (autoAdd)
			{
				this.result.add(row);
			}
			return row;
		}
		catch (SQLException ex)
		{
			throw new EternaException(ex);
		}
	}

	/**
	 * @deprecated
	 * @see #createRow(Object[])
	 */
	public void addRow(ResultRow row)
	{
		this.result.add(row);
	}

	public void addedOver()
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
			throws EternaException
	{
		CustomResultIterator ritr = new CustomResultIterator(this.permission);
		this.copy(ritr);
		return ritr;
	}

	protected void copy(ResultIterator copyObj)
			throws EternaException
	{
		super.copy(copyObj);
		CustomResultIterator ritr = (CustomResultIterator) copyObj;
		ritr.recordCount = this.recordCount;
	}

}