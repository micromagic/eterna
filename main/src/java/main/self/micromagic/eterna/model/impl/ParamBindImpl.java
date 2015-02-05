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

import java.sql.SQLException;
import java.util.Map;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.dao.Dao;
import self.micromagic.eterna.dao.Parameter;
import self.micromagic.eterna.dao.ResultIterator;
import self.micromagic.eterna.dao.ResultRow;
import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.DataHandler;
import self.micromagic.eterna.model.Execute;
import self.micromagic.eterna.model.Model;
import self.micromagic.eterna.model.ParamBind;
import self.micromagic.eterna.model.ParamBindGenerator;
import self.micromagic.eterna.model.ParamSetManager;
import self.micromagic.eterna.search.SearchResult;
import self.micromagic.eterna.search.Search;
import self.micromagic.eterna.search.SearchManager;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.util.StringTool;
import self.micromagic.util.container.RequestParameterMap;

/**
 * @author micromagic@sina.com
 */
public class ParamBindImpl extends AbstractGenerator
		implements ParamBind, ParamBindGenerator
{
	/**
	 * 当subSQL为true, 同时没有设置names属性时, 用此默认值.
	 */
	private static final ParamSetManager.Name[] DEFAULT_SUBSQL_NAMES
			= new ParamSetManager.Name[]{new ParamSetManager.Name(null, 1)};

	protected ParamSetManager.Name[] names = null;
	protected DataHandler srcHandler = new DataHandler("src", false, true);

	protected boolean loop = false;
	protected boolean subSQL = false;

	public void initialize(Model model, Execute execute)
			throws EternaException
	{
		if (this.loop && this.names == null)
		{
			log.info("Because not give the attribute names, so set loop = false.");
			this.loop = false;
		}
		if (this.subSQL)
		{
			if (this.names == null || this.names.length == 0)
			{
				log.warn("Because not give the sub index at attribute names, so set names = 1."
						+ " model:" + model.getName() + ", execute:" + execute.getExecuteType()
						+ "#" + execute.getName() + ".");
				this.names = DEFAULT_SUBSQL_NAMES;
			}
			else
			{
				for (int i = 0; i < this.names.length; i++)
				{
					try
					{
						this.names[i] = new ParamSetManager.Name(
								this.names[i], Integer.parseInt(this.names[i].daoName));
					}
					catch (Exception ex)
					{
						throw new EternaException("When set sub SQL, names must be number. but the "
								+ (i + 1) + " name is [" + this.names[i].daoName +  "].");
					}
				}
			}
		}
		else
		{
			if (this.names != null && execute instanceof DaoExecute)
			{
				try
				{
					Dao sql = ((DaoExecute) execute).getDao();
					for (int i = 0; i < this.names.length; i++)
					{
						Parameter param = sql.getParameter(this.names[i].daoName);
						this.names[i] = new ParamSetManager.Name(this.names[i], param.getIndex());
					}
				}
				catch (EternaException ex)
				{
					log.error("Error in parse name index.", ex);
				}
			}
		}
	}

	public boolean isLoop()
	{
		return this.loop;
	}

	public void setLoop(boolean loop)
	{
		this.loop = loop;
	}

	public boolean isSubSQL()
	{
		return this.subSQL;
	}

	public void setSubSQL(boolean subSQL)
	{
		this.subSQL = subSQL;
	}

	public void setSrc(String theSrc)
			throws EternaException
	{
		this.srcHandler.setConfig(theSrc);
	}

	public void setNames(String names)
	{
		if (names == null || names.length() == 0)
		{
			this.names = null;
		}
		String[] tmps = StringTool.separateString(names, ",", true);
		this.names = new ParamSetManager.Name[tmps.length];
		for (int i = 0; i < tmps.length; i++)
		{
			String tmp = tmps[i];
			int index = tmp.indexOf(':');
			if (index == -1)
			{
				if (tmp.endsWith("[]"))
				{
					// 如果名称是以[]结尾, 则说明是以数组方式获取值
					// sqlName只取"[]"前面的部分
					this.names[i] = new ParamSetManager.Name(tmp, tmp.substring(0, tmp.length() - 2));
				}
				else
				{
					this.names[i] = new ParamSetManager.Name(tmp, tmp);
				}
			}
			else
			{
				this.names[i] = new ParamSetManager.Name(tmp.substring(0, index),
						tmp.substring(index + 1));
			}
		}
	}

	public int setParam(AppData data, ParamSetManager psm, int loopIndex)
			throws EternaException, SQLException
	{
		int loopCount = -1;
		Object tempValue = null;
		if (loopIndex == 0)
		{
			tempValue = this.srcHandler.getData(data, false);
		}

		if (this.subSQL)
		{
			if (tempValue == null)
			{
				for (int i = 0; i < this.names.length; i++)
				{
					psm.setSubSQL(this.names[i].daoIndex, "");
				}
			}
			else if (tempValue instanceof String)
			{
				for (int i = 0; i < this.names.length; i++)
				{
					psm.setSubSQL(this.names[i].daoIndex, (String) tempValue);
				}
			}
			else if (tempValue instanceof SearchManager)
			{
				SearchManager sm = (SearchManager) tempValue;
				for (int i = 0; i < this.names.length; i++)
				{
					psm.setSubSQL(this.names[i].daoIndex, sm.getConditionPart(), sm.getPreparerManager());
				}
			}
			else if (tempValue instanceof Search)
			{
				Search sa = (Search) tempValue;
				SearchManager sm = sa.getSearchManager(data);
				for (int i = 0; i < this.names.length; i++)
				{
					psm.setSubSQL(this.names[i].daoIndex, sm.getSpecialConditionPart(sa),
							sm.getSpecialPreparerManager(sa));
				}
			}
			else if (tempValue instanceof Map)
			{
				Map subs = (Map) tempValue;
				for (int i = 0; i < this.names.length; i++)
				{
					String v = RequestParameterMap.getFirstParam(subs.get(this.names[i].srcName));
					if (v == null)
					{
						psm.setSubSQL(this.names[i].daoIndex, "");
					}
					else
					{
						psm.setSubSQL(this.names[i].daoIndex, v);
					}
				}
			}
			else
			{
				throw new EternaException("Error src type:" + tempValue.getClass() + ".");
			}
		}
		else
		{
			if (loopIndex == 0 && tempValue == null)
			{
				log.warn("Not found the src:" + this.srcHandler.getConfig() + ".");
			}
			if (this.names != null)
			{
				if (this.loop)
				{
					if (tempValue == null)
					{
						loopCount = psm.setParams(this.names, loopIndex);
					}
					else if (tempValue instanceof Map)
					{
						loopCount = psm.setParams((Map) tempValue, this.names, loopIndex);
					}
					else if (tempValue instanceof ResultIterator)
					{
						ResultIterator ritr = (ResultIterator) tempValue;
						loopCount = psm.setParams(ritr, this.names, loopIndex);
					}
					else if (tempValue instanceof SearchResult)
					{
						ResultIterator ritr = ((SearchResult) tempValue).queryResult;
						loopCount = psm.setParams(ritr, this.names, loopIndex);
					}
					else
					{
						throw new EternaException("Error src type:" + tempValue.getClass() + ".");
					}
				}
				else
				{
					if (tempValue == null)
					{
						for (int i = 0; i < this.names.length; i++)
						{
							psm.setParam(this.names[i].daoName, tempValue);
						}
					}
					else if (tempValue instanceof Map)
					{
						psm.setParams((Map) tempValue, this.names);
					}
					else if (tempValue instanceof ResultRow)
					{
						psm.setParams((ResultRow) tempValue, this.names);
					}
					else if (tempValue instanceof SearchManager)
					{
						psm.setParams((SearchManager) tempValue, this.names);
					}
					else if (tempValue instanceof ResultIterator)
					{
						ResultIterator ritr = (ResultIterator) tempValue;
						if (ritr.hasNextRow())
						{
							psm.setParams(ritr.nextRow(), this.names);
						}
					}
					else if (tempValue instanceof SearchResult)
					{
						ResultIterator ritr = ((SearchResult) tempValue).queryResult;
						if (ritr.hasNextRow())
						{
							psm.setParams(ritr.nextRow(), this.names);
						}
					}
					else if (tempValue instanceof Object[])
					{
						Object[] objs = (Object[]) tempValue;
						if (objs.length == this.names.length)
						{
							for (int i = 0; i < objs.length; i++)
							{
								psm.setParam(this.names[i].daoName, objs[i]);
							}
						}
						else if (objs.length == 1)
						{
							for (int i = 0; i < this.names.length; i++)
							{
								psm.setParam(this.names[i].daoName, objs[0]);
							}
						}
						else
						{
							throw new EternaException("Error src length:" + objs.length + ", require:" + this.names.length + ".");
						}
					}
					else
					{
						for (int i = 0; i < this.names.length; i++)
						{
							psm.setParam(this.names[i].daoName, tempValue);
						}
					}
				}
			}
			else
			{
				if (tempValue == null)
				{
					throw new EternaException("Error src type:[null].");
				}
				else if (tempValue instanceof Map)
				{
					psm.setParams((Map) tempValue);
				}
				else if (tempValue instanceof ResultRow)
				{
					psm.setParams((ResultRow) tempValue);
				}
				else if (tempValue instanceof SearchManager)
				{
					psm.setParams((SearchManager) tempValue);
				}
				else if (tempValue instanceof ResultIterator)
				{
					ResultIterator ritr = (ResultIterator) tempValue;
					if (ritr.hasNextRow())
					{
						psm.setParams(ritr.nextRow());
					}
				}
				else if (tempValue instanceof SearchResult)
				{
					ResultIterator ritr = ((SearchResult) tempValue).queryResult;
					if (ritr.hasNextRow())
					{
						psm.setParams(ritr.nextRow());
					}
				}
				else
				{
					throw new EternaException("Error src type:" + tempValue.getClass() + ".");
				}
			}
		}
		return loopCount;
	}

	public Object create()
	{
		return this.createParamBind();
	}

	public ParamBind createParamBind()
	{
		return this;
	}

}

