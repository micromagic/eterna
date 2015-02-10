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

package self.micromagic.eterna.model.impl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.dom4j.Element;

import self.micromagic.eterna.dao.Dao;
import self.micromagic.eterna.dao.Query;
import self.micromagic.eterna.dao.ResultIterator;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.Execute;
import self.micromagic.eterna.model.Model;
import self.micromagic.eterna.model.ModelExport;
import self.micromagic.eterna.model.ParamSetManager;
import self.micromagic.eterna.model.QueryExecuteGenerator;
import self.micromagic.eterna.security.EmptyPermission;
import self.micromagic.eterna.security.User;
import self.micromagic.eterna.security.UserManager;
import self.micromagic.eterna.share.EternaException;

public class QueryExecute extends DaoExecute
		implements Execute, QueryExecuteGenerator
{
	private int start = 1;
	private int count = -1;
	private int countType = Query.TOTAL_COUNT_NONE;
	protected int queryAdapterIndex = -1;

	public void initialize(Model model)
			throws EternaException
	{
		if (this.initialized)
		{
			return;
		}
		super.initialize(model);
		this.queryAdapterIndex = this.factory.findObjectId(this.getName());
	}

	protected Dao getDao()
			throws EternaException
	{
		return this.queryAdapterIndex == -1 ? this.factory.createQuery(this.getName())
				: this.factory.createQuery(this.queryAdapterIndex);
	}

	public String getExecuteType()
	{
		return "query";
	}

	public void setStart(int start)
	{
		this.start = start;
	}

	public void setCount(int count)
	{
		this.count = count;
	}

	public void setCountType(String countType)
			throws EternaException
	{
		if ("auto".equals(countType))
		{
			this.countType = Query.TOTAL_COUNT_AUTO;
		}
		else if ("count".equals(countType))
		{
			this.countType = Query.TOTAL_COUNT_COUNT;
		}
		else if ("none".equals(countType))
		{
			this.countType = Query.TOTAL_COUNT_NONE;
		}
		else
		{
			throw new EternaException("Error count type:[" + countType + "].");
		}
	}

	public ModelExport execute(AppData data, Connection conn)
			throws EternaException, SQLException, IOException
	{
		boolean inCache = false;
		Query query = null;
		if (this.sqlCacheIndex != -1)
		{
			Object temp = data.caches[this.sqlCacheIndex];
			if (temp instanceof Query)
			{
				query = (Query) temp;
				if (!query.getName().equals(this.getName()))
				{
					query = null;
				}
				else
				{
					inCache = true;
				}
			}
		}
		if (query == null)
		{
			query = this.factory.createQuery(this.queryAdapterIndex);
			if (this.sqlCacheIndex != -1)
			{
				data.caches[this.sqlCacheIndex] = query;
			}
		}

		if (this.start != 1)
		{
			query.setStartRow(this.start);
		}
		if (this.count != -1)
		{
			query.setMaxCount(this.count);
		}
		query.setTotalCount(this.countType);
		UserManager um = this.factory.getUserManager();
		if (um != null)
		{
			User user = um.getUser(data);
			if (user != null)
			{
				query.setPermission(user.getPermission());
			}
			else
			{
				query.setPermission(EmptyPermission.getInstance());
			}
		}

		if (data.getLogType() > 0)
		{
			Element nowNode = data.getCurrentNode();
			nowNode.addAttribute("queryName", query.getName());
			String countTypeStr = "err";
			if (this.countType == Query.TOTAL_COUNT_AUTO)
			{
				countTypeStr = "auto";
			}
			else if (this.countType == Query.TOTAL_COUNT_COUNT)
			{
				countTypeStr = "count";
			}
			else if (this.countType == Query.TOTAL_COUNT_NONE)
			{
				countTypeStr = "none";
			}
			nowNode.addAttribute("countType", countTypeStr);
			if (this.start != 1)
			{
				nowNode.addAttribute("start", String.valueOf(this.start));
			}
			if (this.count != -1)
			{
				nowNode.addAttribute("count", String.valueOf(this.count));
			}
			if (this.sqlCacheIndex != -1)
			{
				nowNode.addAttribute("sqlCacheIndex", String.valueOf(this.sqlCacheIndex));
				if (inCache)
				{
					nowNode.addAttribute("inCache", "true");
				}
			}
			if (!this.doExecute)
			{
				nowNode.addAttribute("doExecute", String.valueOf(this.doExecute));
			}
		}
		ParamSetManager psm = new ParamSetManager(query);
		int count = this.setParams(data, psm, 0);
		if (this.doExecute)
		{
			if (count != 0)
			{
				ResultIterator ritr = query.executeQuery(conn);
				ResultIterator[] results = null;
				if (this.pushResult)
				{
					if (count > 1)
					{
						results = new ResultIterator[count];
						results[0] = ritr;
						data.push(results);
					}
					else
					{
						data.push(ritr);
					}
				}
				for (int i = 1; i < count; i++)
				{
					this.setParams(data, psm, i);
					ritr = query.executeQuery(conn);
					if (this.pushResult)
					{
						results[i] = ritr;
					}
				}
			}
			else if (this.pushResult)
			{
				data.push(new ResultIterator[0]);
			}
		}
		return null;
	}

}