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

package self.micromagic.eterna.base.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import self.micromagic.eterna.base.Entity;
import self.micromagic.eterna.base.Query;
import self.micromagic.eterna.base.ResultMetaData;
import self.micromagic.eterna.base.ResultReader;
import self.micromagic.eterna.base.ResultReaderManager;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.util.Utility;

public class ResultMetaDataImpl
		implements ResultMetaData
{
	private String name;
	private final ResultReader[] readers;
	private final Query query;
	private final ResultReaderManager readerManager;
	private Map nameToIndexMap;
	private boolean colNameSensitive = true;

	public ResultMetaDataImpl(List readerList, ResultReaderManager readerManager, Query query)
			throws EternaException
	{
		this.query = query;
		int count = readerList.size();
		this.readers = new ResultReader[count];
		Iterator itr = readerList.iterator();
		for (int i = 0; i < this.readers.length; i++)
		{
			this.readers[i] = (ResultReader) itr.next();
		}
		this.readerManager = readerManager;
		if (readerManager == null || !readerManager.isLocked())
		{
			this.colNameSensitive = readerManager == null ? true : readerManager.isColNameSensitive();
			// 当readerManager为null或未锁定时, 需要构造自己的名称值对应表
			this.nameToIndexMap = new HashMap((int) (count * 1.5));
			for (int i = 0; i < this.readers.length; i++)
			{
				ResultReader r = this.readers[i];
				if (this.colNameSensitive)
				{
					this.nameToIndexMap.put(r.getName(), Utility.createInteger(i + 1));
				}
				else
				{
					this.nameToIndexMap.put(r.getName().toUpperCase(), Utility.createInteger(i + 1));
				}
			}
		}
		if (this.query != null)
		{
			this.name = "query [" + this.query.getName() + "]";
		}
		else if (this.readerManager != null)
		{
			this.name = "reader manager [" + this.readerManager.getName() + "]";
		}
		else
		{
			this.name = "unknow";
		}
	}

	/**
	 * 将一个实体对象转换成ResultReaderManager对象.
	 */
	public static ResultReaderManager enrity2ReaderManager(Entity entity)
	{
		return null;
	}

	public Query getQuery()
	{
		return this.query;
	}

	public ResultReaderManager getReaderManager()
	{
		return this.readerManager;
	}

	public String getName()
	{
		return this.name;
	}

	public int getColumnCount()
	{
		return this.readers.length;
	}

	public int getColumnWidth(int column)
			throws EternaException
	{
		return this.readers[column - 1].getWidth();
	}

	public String getColumnCaption(int column)
			throws EternaException
	{
		return this.readers[column - 1].getCaption();
	}

	public String getColumnName(int column)
			throws EternaException
	{
		return this.readers[column - 1].getName();
	}

	public ResultReader getColumnReader(int column)
	{
		return this.readers[column - 1];
	}

	public int findColumn(String columnName)
			throws EternaException
	{
		return this.findColumn(columnName, false);
	}

	public int findColumn(String columnName, boolean notThrow)
			throws EternaException
	{
		if (this.nameToIndexMap == null)
		{
			return this.readerManager.getIndexByName(columnName, notThrow);
		}
		else
		{
			Integer i = (Integer) this.nameToIndexMap.get(
					this.colNameSensitive ? columnName : columnName.toUpperCase());
			if (i == null)
			{
				if (notThrow)
				{
					return -1;
				}
				else
				{
					throw new EternaException(
							"Invalid column name:[" + columnName + "] at " + this.getName() + ".");
				}
			}
			return i.intValue();
		}
	}

}