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

package self.micromagic.util.container;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.collections.iterators.IteratorEnumeration;

import self.micromagic.eterna.dao.ModifiableResultRow;
import self.micromagic.eterna.dao.ResultMetaData;
import self.micromagic.eterna.dao.ResultRow;

/**
 * 结果行的容器.
 */
public class ResultRowContainer
		implements ValueContainer
{
	private final ResultRow row;

	public ResultRowContainer(ResultRow row)
	{
		this.row = row;
	}

	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o instanceof ResultRowContainer)
		{
			ResultRowContainer other = (ResultRowContainer) o;
			return this.row.equals(other.row);
		}
		return false;
	}

	public int hashCode()
	{
		return this.row.hashCode();
	}

	public Object getValue(Object key)
	{
		try
		{
			return this.row.getSmartValue(key == null ? null : key.toString(), true);
		}
		catch (SQLException ex)
		{
			return null;
		}
	}

	public boolean containsKey(Object key)
	{
		try
		{
			return this.row.findColumn(key == null ? null : key.toString(), true) != -1;
		}
		catch (SQLException ex)
		{
			return false;
		}
	}

	public void setValue(Object key, Object value)
	{
		if (this.row instanceof ModifiableResultRow)
		{
			((ModifiableResultRow) this.row).setValue(
					key == null ? null : key.toString(), value);
		}
		else
		{
			throw new UnsupportedOperationException();
		}
	}

	public void removeValue(Object key)
	{
		throw new UnsupportedOperationException();
	}

	public Enumeration getKeys()
	{
		try
		{
			ResultMetaData rmd = this.row.getResultIterator().getMetaData();
			int count = rmd.getColumnCount();
			List names = new ArrayList(rmd.getColumnCount());
			for (int i = 1; i <= count; i++)
			{
				names.add(rmd.getColumnReader(i).getName());
			}
			return new IteratorEnumeration(names.iterator());
		}
		catch (SQLException ex)
		{
			return null;
		}
	}

	public Object[] getKeyValuePairs()
	{
		try
		{
			ResultMetaData rmd = this.row.getResultIterator().getMetaData();
			int count = rmd.getColumnCount();
			Object[] arr = new Object[count * 2];
			for (int i = 0; i < count; i++)
			{
				arr[i * 2] = rmd.getColumnReader(i + 1).getName();
				arr[i * 2 + 1] = this.row.getSmartValue(i + 1);
			}
			return arr;
		}
		catch (SQLException ex)
		{
			return null;
		}
	}

}
