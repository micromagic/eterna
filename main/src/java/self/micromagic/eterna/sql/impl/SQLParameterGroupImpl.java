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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.eterna.sql.ResultReader;
import self.micromagic.eterna.sql.ResultReaderManager;
import self.micromagic.eterna.sql.SQLParameterGenerator;
import self.micromagic.eterna.sql.SQLParameterGroup;
import self.micromagic.util.StringTool;
import self.micromagic.util.container.PreFetchIterator;

/**
 * @author micromagic@sina.com
 */
public class SQLParameterGroupImpl
		implements SQLParameterGroup
{
	private boolean initialized;

	private String name;
	private List tmpParamList = new LinkedList();
	private Set tmpParamSet = new HashSet();
	private List paramGeneratorList = new LinkedList();

	public void initialize(EternaFactory factory)
			throws ConfigurationException
	{
		if (!this.initialized)
		{
			this.initialized = true;

			Iterator itr = this.tmpParamList.iterator();
			while (itr.hasNext())
			{
				Object tmp = itr.next();
				if (tmp instanceof SQLParameterGenerator)
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
						group = new ResultReaderMamagerGroup(factory.getReaderManager(refName));
					}
					else
					{
						if (refName.startsWith("group:"))
						{
							refName = refName.substring(6);
						}
						group = factory.getParameterGroup(refName);
					}
					if (group == null)
					{
						throw new ConfigurationException("The SQLParameterGroup [" + arr[0] + "] not found.");
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
						SQLParameterGenerator spg = (SQLParameterGenerator) tmpItr.next();
						if (!ignoreSet.contains(spg.getName()))
						{
							if (this.tmpParamSet.contains(spg.getName()))
							{
								if (!ignoreSet.contains(IGNORE_SAME_NAME))
								{
									throw new ConfigurationException(
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

	public void addParameter(SQLParameterGenerator paramGenerator)
			throws ConfigurationException
	{
		if (this.initialized)
		{
			throw new ConfigurationException("You can't invoke addParameter after initialized.");
		}
		if (this.tmpParamSet.contains(paramGenerator.getName()))
		{
			throw new ConfigurationException(
					"Duplicate [SQLParameter] name:" + paramGenerator.getName() + ".");
		}
		this.tmpParamList.add(paramGenerator);
		this.tmpParamSet.add(paramGenerator.getName());
	}

	public void addParameterRef(String groupName, String ignoreList)
			throws ConfigurationException
	{
		if (this.initialized)
		{
			throw new ConfigurationException("You can't invoke addParameterRef after initialized.");
		}
		this.tmpParamList.add(new String[]{groupName, ignoreList});
	}

	/**
	 * 用于将一个ResultReaderManager转换成SQLParameterGroup
	 */
	static class ResultReaderMamagerGroup
			implements SQLParameterGroup
	{
		private ResultReaderManager readerManager;
		private List groupList = null;

		public ResultReaderMamagerGroup(ResultReaderManager readerManager)
		{
			this.readerManager = readerManager;
		}

		public void initialize(EternaFactory factory) {}

		public void setName(String name) {}

		public String getName()
				throws ConfigurationException
		{
			return this.readerManager.getName();
		}

		public Iterator getParameterGeneratorIterator()
				throws ConfigurationException
		{
			if (this.groupList == null)
			{
				this.groupList = new LinkedList();
				Iterator itr = this.readerManager.getReaderList().iterator();
				while (itr.hasNext())
				{
					ResultReader reader = (ResultReader) itr.next();
					SQLParameterGeneratorImpl spg = new SQLParameterGeneratorImpl();
					spg.setFactory(this.readerManager.getFactory());
					spg.setName(reader.getName());
					String colName = (String) reader.getAttribute(READER_COLNAME_FLAG);
					if (colName != null)
					{
						spg.setColumnName(colName);
					}
					else
					{
						colName = reader.getColumnName();
						if (colName != null)
						{
							spg.setColumnName(colName);
						}
					}
					spg.setParamType(TypeManager.getTypeName(reader.getType()));
					String vpcName = (String) reader.getAttribute(READER_VPC_FLAG);
					if (vpcName != null)
					{
						spg.setParamVPC(vpcName);
					}
					this.groupList.add(spg);
				}
			}
			return new PreFetchIterator(this.groupList.iterator(), false);
		}

		public void addParameter(SQLParameterGenerator paramGenerator) {}

		public void addParameterRef(String groupName, String ignoreList) {}

	}

}