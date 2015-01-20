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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import self.micromagic.eterna.base.ParameterGenerator;
import self.micromagic.eterna.base.ResultReader;
import self.micromagic.eterna.base.ResultReaderManager;
import self.micromagic.eterna.base.SQLParameterGroup;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.StringTool;
import self.micromagic.util.container.PreFetchIterator;

/**
 * @author micromagic@sina.com
 */
public class ParameterGroupImpl
		implements SQLParameterGroup
{
	private boolean initialized;

	private String name;
	private List tmpParamList = new LinkedList();
	private Set tmpParamSet = new HashSet();
	private final List paramGeneratorList = new LinkedList();

	public void initialize(EternaFactory factory)
			throws EternaException
	{
		if (!this.initialized)
		{
			this.initialized = true;

			Iterator itr = this.tmpParamList.iterator();
			while (itr.hasNext())
			{
				Object tmp = itr.next();
				if (tmp instanceof ParameterGenerator)
				{
					this.paramGeneratorList.add(tmp);
				}
				else
				{
					String[] arr = (String[]) tmp;
					String refName = arr[0];
					SQLParameterGroup group;
					if (refName.startsWith("reader:"))
					{
						refName = refName.substring(7);
						group = null;//new ResultReaderMamagerGroup(factory.getReaderManager(refName));
					}
					else
					{
						if (refName.startsWith("group:"))
						{
							refName = refName.substring(6);
						}
						group = null;//factory.getParameterGroup(refName);
					}
					if (group == null)
					{
						throw new EternaException("The SQLParameterGroup [" + arr[0] + "] not found.");
					}
					Set ignoreSet;
					if (arr[1] == null)
					{
						ignoreSet = new HashSet(2);
					}
					else
					{
						String[] ignores = StringTool.separateString(arr[1], ",;", true);
						ignoreSet = new HashSet(Arrays.asList(ignores));
					}
					Iterator tmpItr = group.getParameterGeneratorIterator();
					while (tmpItr.hasNext())
					{
						ParameterGenerator spg = (ParameterGenerator) tmpItr.next();
						if (!ignoreSet.contains(spg.getName()))
						{
							if (this.tmpParamSet.contains(spg.getName()))
							{
								if (!ignoreSet.contains(IGNORE_SAME_NAME))
								{
									throw new EternaException(
											"Duplicate [SQLParameter] name:" + spg.getName() + ".");
								}
							}
							else
							{
								this.paramGeneratorList.add(spg);
								this.tmpParamSet.add(spg.getName());
							}
						}
					}
				}
			}

			this.tmpParamList = null;
			this.tmpParamSet = null;
		}
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return this.name;
	}

	public Iterator getParameterGeneratorIterator()
	{
		return new PreFetchIterator(this.paramGeneratorList.iterator(), false);
	}

	public void addParameter(ParameterGenerator paramGenerator)
			throws EternaException
	{
		if (this.initialized)
		{
			throw new EternaException("You can't invoke addParameter after initialized.");
		}
		if (this.tmpParamSet.contains(paramGenerator.getName()))
		{
			throw new EternaException(
					"Duplicate [SQLParameter] name:" + paramGenerator.getName() + ".");
		}
		this.tmpParamList.add(paramGenerator);
		this.tmpParamSet.add(paramGenerator.getName());
	}

	public void addParameterRef(String groupName, String ignoreList)
			throws EternaException
	{
		if (this.initialized)
		{
			throw new EternaException("You can't invoke addParameterRef after initialized.");
		}
		this.tmpParamList.add(new String[]{groupName, ignoreList});
	}

	/**
	 * 用于将一个ResultReaderManager转换成SQLParameterGroup
	 */
	static class ResultReaderMamagerGroup
			implements SQLParameterGroup
	{
		private final ResultReaderManager readerManager;
		private List groupList = null;

		public ResultReaderMamagerGroup(ResultReaderManager readerManager)
		{
			this.readerManager = readerManager;
		}

		public void initialize(EternaFactory factory) {}

		public void setName(String name) {}

		public String getName()
				throws EternaException
		{
			return this.readerManager.getName();
		}

		public Iterator getParameterGeneratorIterator()
				throws EternaException
		{
			if (this.groupList == null)
			{
				this.groupList = new LinkedList();
				Iterator itr = this.readerManager.getReaderList().iterator();
				while (itr.hasNext())
				{
					ResultReader reader = (ResultReader) itr.next();
					ParameterGeneratorImpl pg = new ParameterGeneratorImpl();
					pg.setFactory(this.readerManager.getFactory());
					pg.setName(reader.getName());
					String colName = (String) reader.getAttribute(READER_COLNAME_FLAG);
					if (colName != null)
					{
						pg.setColumnName(colName);
					}
					else
					{
						colName = reader.getColumnName();
						if (colName != null)
						{
							pg.setColumnName(colName);
						}
					}
					pg.setParamType(TypeManager.getTypeName(reader.getType()));
					String prepareName = (String) reader.getAttribute(READER_VPC_FLAG);
					if (prepareName != null)
					{
						pg.setPrepareName(prepareName);
					}
					this.groupList.add(pg);
				}
			}
			return new PreFetchIterator(this.groupList.iterator(), false);
		}

		public void addParameter(ParameterGenerator paramGenerator) {}

		public void addParameterRef(String groupName, String ignoreList) {}

	}

}