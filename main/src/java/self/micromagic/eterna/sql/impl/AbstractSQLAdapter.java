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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.sql.PreparedStatementWrap;
import self.micromagic.eterna.sql.SQLAdapter;
import self.micromagic.eterna.sql.SQLAdapterGenerator;
import self.micromagic.eterna.sql.SQLParameter;
import self.micromagic.eterna.sql.SQLParameterGenerator;
import self.micromagic.eterna.sql.SQLParameterGroup;
import self.micromagic.eterna.sql.preparer.PreparedStatementWrapImpl;
import self.micromagic.eterna.sql.preparer.PreparerManager;
import self.micromagic.eterna.sql.preparer.ValuePreparer;
import self.micromagic.eterna.sql.preparer.ValuePreparerCreaterGenerator;
import self.micromagic.util.container.PreFetchIterator;

/**
 * @author micromagic@sina.com
 */
public abstract class AbstractSQLAdapter extends AbstractGenerator
		implements SQLAdapter, SQLAdapterGenerator
{
	private String preparedSQL;
	private SQLManager sqlManager;
	private PreparerManager preparerManager;
	private SQLParameterGroup paramGroup;

	private Map parameterNameMap;
	private SQLParameter[] parameterArray;
	protected ValuePreparerCreaterGenerator vpcg;

	protected boolean initialized;

	public EternaFactory getFactory()
	{
		return (EternaFactory) this.factory;
	}

	public void initialize(EternaFactory factory)
			throws EternaException
	{
		if (!this.initialized)
		{
			if (this.preparedSQL == null)
			{
				throw new EternaException( "Can't initialize without preparedSQL.");
			}
			this.initialized = true;

			this.vpcg = factory.getDefaultValuePreparerCreaterGenerator();
			List paramList = new ArrayList();
			if (this.paramGroup != null)
			{
				this.paramGroup.initialize(factory);
				Iterator itr = this.paramGroup.getParameterGeneratorIterator();
				int paramIndex = 1;
				while (itr.hasNext())
				{
					SQLParameterGenerator spg = (SQLParameterGenerator) itr.next();
					SQLParameter param = spg.createParameter(paramIndex++);
					param.initialize(this.getFactory());
					paramList.add(param);
				}
				this.paramGroup = null;
			}
			SQLParameter[] paramArray = new SQLParameter[paramList.size()];
			paramList.toArray(paramArray);
			this.parameterArray = paramArray;

			this.sqlManager = new SQLManager();
			String tmpSQL = this.sqlManager.frontParse(this.preparedSQL, paramArray);
			this.sqlManager.parse(tmpSQL);
			this.preparerManager = new PreparerManager(this, paramArray);
			this.sqlManager.initialize(this.getFactory());

			this.parameterNameMap = new HashMap();
			for (int i = 0; i < paramArray.length; i++)
			{
				SQLParameter param = paramArray[i];
				this.addParameterNameMap(param);
			}
			if (this.sqlManager.getParameterCount() > paramArray.length)
			{
				throw new EternaException(
						"Not all parameter has been bound in [" + this.getName() + "].");
			}
		}
	}

	public Object create()
			throws EternaException
	{
		return this.createSQLAdapter();
	}

	protected void copy(SQLAdapter copyObj)
	{
		AbstractSQLAdapter other = (AbstractSQLAdapter) copyObj;
		if (this.preparedSQL != null)
		{
			other.preparerManager = new PreparerManager(other, this.parameterArray);
			other.sqlManager = this.sqlManager.copy(true);
			other.preparedSQL = this.preparedSQL;
		}
		other.paramGroup = this.paramGroup;
		other.parameterNameMap = this.parameterNameMap;
		other.parameterArray = this.parameterArray;
		other.vpcg = this.vpcg;
		other.initialized = this.initialized;

		other.attributes = this.attributes;
		other.name = this.name;
		other.factory = this.factory;
	}

	public int getParameterCount()
			throws EternaException
	{
		if (this.sqlManager == null)
		{
			throw new EternaException("SQL not initialized.");
		}
		return this.sqlManager.getParameterCount();
	}

	public boolean hasActiveParam()
			throws EternaException
	{
		if (this.preparerManager == null)
		{
			throw new EternaException("SQL not initialized.");
		}
		return this.preparerManager.hasActiveParam();
	}

	public int getActiveParamCount()
			throws EternaException
	{
		if (this.preparerManager == null)
		{
			throw new EternaException("SQL not initialized.");
		}
		return this.preparerManager.getParamCount();
	}

	public int getSubSQLCount()
			throws EternaException
	{
		if (this.sqlManager == null)
		{
			throw new EternaException("SQL not initialized.");
		}
		return this.sqlManager.getSubPartCount();
	}

	public String getPreparedSQL()
			throws EternaException
	{
		if (this.sqlManager == null)
		{
			throw new EternaException("SQL not initialized.");
		}
		return this.sqlManager.getPreparedSQL();
	}

	/**
	 * 根据临时设置的子句获取预备SQL.
	 */
	String getTempPreparedSQL(int[] indexs, String[] subParts)
			throws EternaException
	{
		if (this.sqlManager == null)
		{
			throw new EternaException("SQL not initialized.");
		}
		return this.sqlManager.getTempPreparedSQL(indexs, subParts);
	}

	public void setPreparedSQL(String sql)
			throws EternaException
	{
		if (this.preparedSQL != null)
		{
			throw new EternaException("You can't set prepared sql twice.");
		}
		if (sql == null)
		{
			throw new NullPointerException();
		}
		this.preparedSQL = sql;
	}

	protected void clear()
			throws EternaException
	{
		this.preparedSQL = null;
		this.sqlManager = null;
		this.preparerManager = null;
		this.clearParameters();
	}

	public void setSubSQL(int index, String subPart)
			throws EternaException
	{
		this.setSubSQL(index, subPart, null);
	}

	public void setSubSQL(int index, String subPart, PreparerManager pm)
			throws EternaException
	{
		if (this.sqlManager == null)
		{
			throw new EternaException("SQL not initialized.");
		}
		int tempI = this.sqlManager.setSubPart(index - 1, subPart);
		this.preparerManager.inserPreparerManager(pm, tempI, index);
	}

	public PreparerManager getPreparerManager()
	{
		return this.preparerManager;
	}

	public boolean isDynamicParameter(int index)
			throws EternaException
	{
		if (this.sqlManager == null)
		{
			throw new EternaException("SQL not initialized.");
		}
		return this.sqlManager.isDynamicParameter(index - 1);
	}

	public boolean isDynamicParameter(String name)
			throws EternaException
	{
		return this.isDynamicParameter(this.getIndexByParameterName(name));
	}

	public void setIgnore(int parameterIndex)
			throws EternaException
	{
		if (this.sqlManager == null)
		{
			throw new EternaException("SQL not initialized.");
		}
		this.preparerManager.setIgnore(parameterIndex);
		this.sqlManager.setParamSetted(parameterIndex - 1, false);
	}

	public void setIgnore(String parameterName)
			throws EternaException
	{
		this.setIgnore(this.getIndexByParameterName(parameterName));
	}

	public void setValuePreparer(ValuePreparer preparer)
			throws EternaException
	{
		if (this.sqlManager == null)
		{
			throw new EternaException("SQL not initialized.");
		}
		this.preparerManager.setValuePreparer(preparer);
		preparer.setName(this.parameterArray[preparer.getRelativeIndex() - 1].getName());
		this.sqlManager.setParamSetted(preparer.getRelativeIndex() - 1, true);
	}

	public void prepareValues(PreparedStatement stmt)
			throws EternaException, SQLException
	{
		if (this.sqlManager == null)
		{
			throw new EternaException("SQL not initialized.");
		}
		this.preparerManager.prepareValues(new PreparedStatementWrapImpl(stmt));
	}

	public void prepareValues(PreparedStatementWrap stmtWrap)
			throws EternaException, SQLException
	{
		if (this.sqlManager == null)
		{
			throw new EternaException("SQL not initialized.");
		}
		this.preparerManager.prepareValues(stmtWrap);
	}

	public Iterator getParameterIterator()
			throws EternaException
	{
		return new PreFetchIterator(Arrays.asList(this.parameterArray).iterator(), false);
	}

	private void addParameterNameMap(SQLParameter param)
			throws EternaException
	{
		int index = param.getIndex();
		if (index < 1 || index > this.getParameterCount())
		{
			throw new EternaException(
					"Invalid parameter index:" + index + " at SQLAdapter "
					+ this.getName() + ".");
		}
		Object obj = this.parameterNameMap.put(param.getName(), param);
		if (obj != null)
		{
			throw new EternaException(
					"Duplicate parameter name:" + param.getName() + " at SQLAdapter "
					+ this.getName() + ".");
		}
		ParameterManager pm = this.sqlManager.getParameterManager(index - 1);
		pm.setParam(param);
	}

	public void clearParameters()
			throws EternaException
	{
		if (this.sqlManager != null)
		{
			int count = this.sqlManager.getParameterCount();
			for (int i = 0; i < count; i++)
			{
				ParameterManager pm = this.sqlManager.getParameterManager(i);
				pm.clearParam();
			}
			this.parameterNameMap = null;
		}
		this.paramGroup = null;
	}

	public void addParameter(SQLParameterGenerator paramGenerator)
			throws EternaException
	{
		if (this.initialized)
		{
			throw new EternaException(
					"Can't add parameter in initialized " + this.getType() + " [" + this.getName() + "].");
		}
		else if (this.paramGroup == null)
		{
			this.paramGroup = new SQLParameterGroupImpl();
		}
		this.paramGroup.addParameter(paramGenerator);
	}

	public void addParameterRef(String groupName, String ignoreList)
			throws EternaException
	{
		if (this.initialized)
		{
			throw new EternaException(
					"Can't add parameter ref in initialized " + this.getType() + " [" + this.getName() + "].");
		}
		else if (this.paramGroup == null)
		{
			this.paramGroup = new SQLParameterGroupImpl();
		}
		this.paramGroup.addParameterRef(groupName, ignoreList);
	}

	protected int getIndexByParameterName(String name)
			throws EternaException
	{
		return this.getParameter(name).getIndex();
	}

	public SQLParameter getParameter(String paramName)
			throws EternaException
	{
		SQLParameter p = (SQLParameter) this.parameterNameMap.get(paramName);
		if (p == null)
		{
			throw new EternaException("Invalid parameter name:[" + paramName + "].");
		}
		return p;
	}

}


