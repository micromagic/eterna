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

import java.sql.Connection;
import java.sql.SQLException;
import java.io.IOException;
import java.util.Map;

import self.micromagic.eterna.model.SearchExecuteGenerator;
import self.micromagic.eterna.model.Execute;
import self.micromagic.eterna.model.ModelAdapter;
import self.micromagic.eterna.model.ModelExport;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.search.SearchResult;
import self.micromagic.eterna.search.Search;
import self.micromagic.eterna.search.SearchManager;
import self.micromagic.eterna.share.EternaFactory;
import org.dom4j.Element;


public class SearchExecute extends AbstractExecute
		implements Execute, SearchExecuteGenerator
{
	private static final DataHandler DEFAULT_QUERY_RESULT = new DataHandler("queryResult", true, false);
	private static final DataHandler DEFAULT_SEARCH_MANAGER = new DataHandler("searchManager", true, false);
	private static final DataHandler DEFAULT_SEARCH_COUNT = new DataHandler("searchCount", true, false);

	static
	{
		try
		{
			DEFAULT_QUERY_RESULT.setConfig("data:queryResult");
			DEFAULT_SEARCH_MANAGER.setConfig("data:searchManager");
			DEFAULT_SEARCH_COUNT.setConfig("data:searchCount");
		}
		catch (EternaException ex)
		{
			log.error("Error in init SearchExecute DataHandler.", ex);
		}
	}

	private String searchNameTag = "searchName";
	private String searchName = null;

	private int searchCacheIdnex = -1;
	private DataHandler queryResult = DEFAULT_QUERY_RESULT;
	private DataHandler searchManager = DEFAULT_SEARCH_MANAGER;
	private DataHandler searchCount = DEFAULT_SEARCH_COUNT;

	private boolean saveCondition = true;
	private boolean forceSetParam = false;
	private int start = -1;
	private int count = -1;

	private boolean holdConnection = false;
	protected boolean doExecute = true;

	public void initialize(ModelAdapter model)
			throws EternaException
	{
		if (this.initialized)
		{
			return;
		}
		super.initialize(model);
	}

	public String getName()
	{
		return this.searchName == null ? this.searchNameTag : this.searchName;
	}

	public String getExecuteType()
			throws EternaException
	{
		return "search";
	}

	public void setSearchNameTag(String tag)
	{
		this.searchNameTag = tag;
	}

	public void setSearchName(String name)
	{
		this.searchName = name;
	}

	public void setCache(int cacheIndex)
	{
		this.searchCacheIdnex = cacheIndex;
	}

	public void setQueryResultName(String config)
			throws EternaException
	{
		if (config == null || config.length() == 0)
		{
			this.queryResult = null;
			return;
		}
		if (config.indexOf(':') == -1)
		{
			// 如果配置格式中没有":", 说明是用旧的配置格式, 前面补上"data:"
			config = "data:" + config;
		}
		this.queryResult = new DataHandler("queryResult", true, false);
		this.queryResult.setConfig(config);
	}

	public void setSearchManagerName(String config)
			throws EternaException
	{
		if (config == null || config.length() == 0)
		{
			this.searchManager = null;
			return;
		}
		if (config.indexOf(':') == -1)
		{
			// 如果配置格式中没有":", 说明是用旧的配置格式, 前面补上"data:"
			config = "data:" + config;
		}
		this.searchManager = new DataHandler("searchManager", true, false);
		this.searchManager.setConfig(config);
	}

	public void setSearchCountName(String config)
			throws EternaException
	{
		if (config == null || config.length() == 0)
		{
			this.searchCount = null;
			return;
		}
		if (config.indexOf(':') == -1)
		{
			// 如果配置格式中没有":", 说明是用旧的配置格式, 前面补上"data:"
			config = "data:" + config;
		}
		this.searchCount = new DataHandler("searchCount", true, false);
		this.searchCount.setConfig(config);
	}

	public void setSaveCondition(boolean saveCondition)
	{
		this.saveCondition = saveCondition;
	}

	public void setForceSetParam(boolean forceSetParam)
	{
		this.forceSetParam = forceSetParam;
	}

	public void setStart(int start)
	{
		this.start = start;
	}

	public void setCount(int count)
	{
		this.count = count;
	}

	public void setHoldConnection(boolean hold)
	{
		this.holdConnection = hold;
	}

	public void setDoExecute(boolean execute)
	{
		this.doExecute = execute;
	}

	public ModelExport execute(AppData data, Connection conn)
			throws EternaException, SQLException, IOException
	{
		String searchName = this.searchName != null ?
				this.searchName : data.getRequestParameter(this.searchNameTag);
		if (searchName == null)
		{
			searchName = (String) data.getRequestAttributeMap().get(this.searchNameTag);
		}
		EternaFactory f = this.getModelAdapter().getFactory();
		if (searchName != null)
		{
			if (data.getLogType() > 0)
			{
				Element nowNode = data.getCurrentNode();
				nowNode.addAttribute("searchName", searchName);
				if (this.start != 1)
				{
					nowNode.addAttribute("start", String.valueOf(this.start));
				}
				if (this.count != -1)
				{
					nowNode.addAttribute("count", String.valueOf(this.count));
				}
				if (this.searchCacheIdnex != -1)
				{
					nowNode.addAttribute("searchCacheIdnex", String.valueOf(this.searchCacheIdnex));
				}
				if (this.forceSetParam)
				{
					nowNode.addAttribute("forceSetParam", String.valueOf(this.forceSetParam));
				}
				if (this.holdConnection)
				{
					nowNode.addAttribute("holdConnection", String.valueOf(this.holdConnection));
				}
				if (!this.doExecute)
				{
					nowNode.addAttribute("doExecute", String.valueOf(this.doExecute));
				}
			}
			Search search = f.createSearchAdapter(searchName);
			if (this.searchCacheIdnex != -1)
			{
				data.caches[this.searchCacheIdnex] = search;
			}
			Map raMap = data.getRequestAttributeMap();
			if (this.forceSetParam)
			{
				raMap.put(SearchManager.FORCE_DEAL_CONDITION, "1");
			}
			if (this.holdConnection)
			{
				if (this.doExecute)
				{
					raMap.put(Search.READ_ALL_ROW, "1");
					raMap.put(Search.HOLD_CONNECTION, "1");
					SearchResult sr = search.doSearch(data, conn);
					if (this.queryResult != null)
					{
						this.queryResult.setData(data, sr);
					}
				}
			}
			else
			{
				if (this.start != -1)
				{
					raMap.put(Search.READ_ROW_START_AND_COUNT,
							new Search.StartAndCount(this.start, this.count));
				}
				if (this.saveCondition)
				{
					raMap.put(SearchManager.SAVE_CONDITION, "1");
					SearchManager sm = search.getSearchManager(data);
					if (this.searchManager != null)
					{
						this.searchManager.setData(data, sm);
					}
				}
				else
				{
					raMap.remove(SearchManager.SAVE_CONDITION);
				}
				if (this.doExecute)
				{
					SearchResult sr = search.doSearch(data, conn);
					if (this.queryResult != null)
					{
						this.queryResult.setData(data, sr);
					}
					if (sr.searchCount != null && this.searchCount != null)
					{
						this.searchCount.setData(data, sr.searchCount);
					}
				}
				if (this.start != -1)
				{
					raMap.remove(Search.READ_ROW_START_AND_COUNT);
				}
			}
			if (this.forceSetParam)
			{
				raMap.remove(SearchManager.FORCE_DEAL_CONDITION);
			}
		}
		data.dataMap.put(SEARCH_MANAGER_ATTRIBUTES, f.getSearchAttributes());
		return null;
	}

}