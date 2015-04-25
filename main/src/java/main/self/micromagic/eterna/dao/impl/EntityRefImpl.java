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

package self.micromagic.eterna.dao.impl;

import self.micromagic.eterna.dao.EntityRef;

/**
 * 对一个实体对象的引用.
 */
public class EntityRefImpl
		implements EntityRef
{
	public String getEntityName()
	{
		return this.entityName;
	}
	private String entityName;
	public void setEntityName(String name)
	{
		this.entityName = name;
	}

	public String getInclude()
	{
		return this.include;
	}
	private String include;
	public void setInclude(String include)
	{
		this.include = include;
	}

	public String getExclude()
	{
		return this.exclude;
	}
	private String exclude;
	public void setExclude(String exclude)
	{
		this.exclude = exclude;
	}

	public boolean isIgnoreSame()
	{
		return this.ignoreSame;
	}
	private boolean ignoreSame;
	public void setIgnoreSame(boolean ignoreSame)
	{
		this.ignoreSame = ignoreSame;
	}

	public String getTableAlias()
	{
		return this.tableAlias;
	}
	private String tableAlias;
	public void setTableAlias(String tableAlias)
	{
		this.tableAlias = tableAlias;
	}

}