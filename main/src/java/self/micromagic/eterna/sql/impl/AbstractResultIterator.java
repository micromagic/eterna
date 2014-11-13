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

import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;
import java.sql.SQLException;

import self.micromagic.eterna.sql.ResultIterator;
import self.micromagic.eterna.sql.ResultRow;
import self.micromagic.eterna.sql.ResultMetaData;
import self.micromagic.eterna.sql.ResultReaderManager;
import self.micromagic.eterna.sql.QueryAdapter;
import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.security.Permission;

/**
 * @author micromagic@sina.com
 */
public abstract class AbstractResultIterator
		implements ResultIterator
{
	protected List result;
	protected Iterator resultItr;
	protected List readerList;
	protected ResultReaderManager readerManager;
	protected List preFetchList;
	protected ResultRow currentRow;
	protected ResultMetaData metaData;
	protected QueryAdapter query;

	/**
	 * 当前的ResultIterator是否为最初构造的.
	 */
	protected boolean originItr;

	public AbstractResultIterator(List readerList)
	{
		this.readerList = readerList;
		this.originItr = true;
	}

	public AbstractResultIterator(ResultReaderManager readerManager, Permission permission)
			throws ConfigurationException
	{
		this.readerManager = readerManager;
		this.readerList = readerManager.getReaderList(permission);
		this.originItr = true;
	}

	protected AbstractResultIterator()
	{
	}

	public ResultMetaData getMetaData()
			throws SQLException, ConfigurationException
	{
		if (this.metaData == null)
		{
			this.metaData = new ResultMetaDataImpl(this.readerList, this.readerManager, this.query);
		}
		return this.metaData;
	}

	public boolean hasMoreRow()
	{
		if (this.preFetchList != null && this.preFetchList.size() > 0)
		{
			return true;
		}
		return this.resultItr.hasNext();
	}

	protected boolean hasMoreRow0()
	{
		return this.resultItr.hasNext();
	}

	public ResultRow preFetch()
	{
		return this.preFetch(1);
	}

	public ResultRow preFetch(int index)
	{
		if (this.preFetchList != null && this.preFetchList.size() >= index)
		{
			return (ResultRow) this.preFetchList.get(index - 1);
		}
		if (this.preFetchList == null)
		{
			this.preFetchList = new LinkedList();
		}
		for (int i = this.preFetchList.size(); i < index; i++)
		{
			if (this.hasMoreRow0())
			{
				this.preFetchList.add(this.nextRow0());
			}
			else
			{
				return null;
			}
		}
		return (ResultRow) this.preFetchList.get(index - 1);
	}

	public ResultRow getCurrentRow()
	{
		return this.currentRow;
	}

	public ResultRow nextRow()
	{
		if (this.preFetchList != null && this.preFetchList.size() > 0)
		{
			this.currentRow = (ResultRow) this.preFetchList.remove(0);
			return this.currentRow ;
		}
		this.currentRow = this.nextRow0();
		return this.currentRow;
	}

	protected ResultRow nextRow0()
	{
		ResultRow row = (ResultRow) this.resultItr.next();
		if (!this.originItr)
		{
			// 如果当前不是原始的ResultIterator, 需要调整返回的ResultRow
			if (row.getClass() == ResultRowImpl.class)
			{
				// 如果是ResultRowImpl(且不是子类), 则进行复制
				row = new ResultRowImpl((ResultRowImpl) row, this);
			}
			else
			{
				row = new ResultRowWrapper(this, row);
			}
		}
		return row;
	}

	public boolean beforeFirst()
	{
		this.resultItr = this.result.iterator();
		this.preFetchList = null;
		this.currentRow = null;
		return true;
	}

	public void close()
	{
	}

	protected void copy(ResultIterator copyObj)
			throws ConfigurationException
	{
		AbstractResultIterator other = (AbstractResultIterator) copyObj;
		other.result = this.result;
		other.resultItr = this.resultItr;
		other.readerList = this.readerList;
		other.readerManager = this.readerManager;
		other.preFetchList = this.preFetchList;
		other.currentRow = this.currentRow;
		other.metaData = this.metaData;
		other.query = this.query;
		other.originItr = false;
		other.beforeFirst();
	}

	public boolean hasNext()
	{
		return this.hasMoreRow();
	}

	public Object next()
	{
		return this.nextRow();
	}

	public void remove()
	{
		throw new UnsupportedOperationException();
	}

}