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

package self.micromagic.eterna.dao.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import self.micromagic.eterna.dao.Dao;
import self.micromagic.eterna.dao.EntityRef;
import self.micromagic.eterna.dao.Parameter;
import self.micromagic.eterna.dao.ParameterGenerator;
import self.micromagic.eterna.dao.PreparedStatementWrap;
import self.micromagic.eterna.dao.preparer.PreparedStatementWrapImpl;
import self.micromagic.eterna.dao.preparer.PreparerManager;
import self.micromagic.eterna.dao.preparer.ValuePreparer;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.ObjectCreater;
import self.micromagic.util.container.PreFetchIterator;

/**
 * @author micromagic@sina.com
 */
public abstract class AbstractDao extends AbstractGenerator
		implements Dao, ObjectCreater
{
	private String preparedSQL;
	private DaoManager daoManager;
	private PreparerManager preparerManager;
	private ParameterGroup paramGroup;

	private Map parameterNameMap;
	private Parameter[] parameterArray;

	protected boolean initialized;

	public EternaFactory getFactory()
	{
		return (EternaFactory) this.factory;
	}

	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		if (!this.initialized)
		{
			if (this.preparedSQL == null)
			{
				throw new EternaException( "Can't initialize without preparedSQL.");
			}
			this.initialized = true;

			List paramList = new ArrayList();
			if (this.paramGroup != null)
			{
				this.paramGroup.initialize(factory);
				Iterator itr = this.paramGroup.getParameterGeneratorIterator();
				int paramIndex = 1;
				while (itr.hasNext())
				{
					ParameterGenerator spg = (ParameterGenerator) itr.next();
					Parameter param = spg.createParameter(paramIndex++);
					param.initialize(this.getFactory());
					paramList.add(param);
				}
				this.paramGroup = null;
			}
			Parameter[] paramArray = new Parameter[paramList.size()];
			paramList.toArray(paramArray);
			this.parameterArray = paramArray;

			this.initElse(factory);

			this.daoManager = new DaoManager();
			String tmpSQL = this.daoManager.frontParse(this.preparedSQL, this);
			this.daoManager.parse(tmpSQL);
			this.preparerManager = new PreparerManager(this, paramArray);
			this.daoManager.initialize(this);

			this.parameterNameMap = new HashMap();
			for (int i = 0; i < paramArray.length; i++)
			{
				Parameter param = paramArray[i];
				this.addParameterNameMap(param);
			}
			if (this.daoManager.getParameterCount() > paramArray.length)
			{
				throw new EternaException(
						"Not all parameter has been bound in [" + this.getName() + "].");
			}
			return false;
		}
		return true;
	}

	/**
	 * 对其他的属性进行初始化.
	 */
	protected abstract void initElse(EternaFactory factory) throws EternaException;

	public Class getObjectType()
	{
		return Dao.class;
	}

	public boolean isSingleton()
	{
		return false;
	}

	protected void copy(Dao copyObj)
	{
		AbstractDao other = (AbstractDao) copyObj;
		if (this.preparedSQL != null)
		{
			other.preparerManager = new PreparerManager(other, this.parameterArray);
			other.daoManager = this.daoManager.copy(true);
			other.preparedSQL = this.preparedSQL;
		}
		other.paramGroup = this.paramGroup;
		other.parameterNameMap = this.parameterNameMap;
		other.parameterArray = this.parameterArray;
		other.initialized = this.initialized;

		other.attributes = this.attributes;
		other.name = this.name;
		other.factory = this.factory;
	}

	public int getParameterCount()
			throws EternaException
	{
		if (this.daoManager == null)
		{
			throw new EternaException(this.getType() + " [" + this.getName()
					+ "] not initialized.");
		}
		return this.daoManager.getParameterCount();
	}

	public boolean hasActiveParam()
			throws EternaException
	{
		if (this.preparerManager == null)
		{
			throw new EternaException(this.getType() + " [" + this.getName()
					+ "] not initialized.");
		}
		return this.preparerManager.hasActiveParam();
	}

	public int getActiveParamCount()
			throws EternaException
	{
		if (this.preparerManager == null)
		{
			throw new EternaException(this.getType() + " [" + this.getName()
					+ "] not initialized.");
		}
		return this.preparerManager.getParamCount();
	}

	public int getSubSQLCount()
			throws EternaException
	{
		if (this.daoManager == null)
		{
			throw new EternaException(this.getType() + " [" + this.getName()
					+ "] not initialized.");
		}
		return this.daoManager.getSubPartCount();
	}

	public String getPreparedSQL()
			throws EternaException
	{
		if (this.daoManager == null)
		{
			throw new EternaException(this.getType() + " [" + this.getName()
					+ "] not initialized.");
		}
		return this.daoManager.getPreparedSQL();
	}

	/**
	 * 根据临时设置的子句获取预备SQL.
	 */
	String getTempPreparedSQL(int[] indexs, String[] subParts)
			throws EternaException
	{
		if (this.daoManager == null)
		{
			throw new EternaException(this.getType() + " [" + this.getName()
					+ "] not initialized.");
		}
		return this.daoManager.getTempPreparedSQL(indexs, subParts);
	}

	public void setPreparedSQL(String sql)
			throws EternaException
	{
		if (this.preparedSQL != null)
		{
			throw new EternaException("You can't set prepared sql twice at "
					+ this.getType() + " [" + this.getName() + "].");
		}
		if (sql == null)
		{
			throw new NullPointerException();
		}
		this.preparedSQL = sql;
	}

	public void setSubSQL(int index, String subPart)
			throws EternaException
	{
		this.setSubSQL(index, subPart, null);
	}

	public void setSubSQL(int index, String subPart, PreparerManager pm)
			throws EternaException
	{
		if (this.daoManager == null)
		{
			throw new EternaException(this.getType() + " [" + this.getName()
					+ "] not initialized.");
		}
		int tempI = this.daoManager.setSubPart(index - 1, subPart);
		this.preparerManager.inserPreparerManager(pm, tempI, index);
	}

	public PreparerManager getPreparerManager()
	{
		return this.preparerManager;
	}

	public boolean isParameterSetted(int index)
			throws EternaException
	{
		if (this.daoManager == null)
		{
			throw new EternaException(this.getType() + " [" + this.getName()
					+ "] not initialized.");
		}
		return this.daoManager.isParamSetted(index);
	}

	public boolean isParameterSetted(String name)
			throws EternaException
	{
		return this.isParameterSetted(this.getParameterIndex(name));
	}

	public boolean isDynamicParameter(int index)
			throws EternaException
	{
		if (this.daoManager == null)
		{
			throw new EternaException(this.getType() + " [" + this.getName()
					+ "] not initialized.");
		}
		return this.daoManager.isDynamicParameter(index - 1);
	}

	public boolean isDynamicParameter(String name)
			throws EternaException
	{
		return this.isDynamicParameter(this.getParameterIndex(name));
	}

	public void setIgnore(int parameterIndex)
			throws EternaException
	{
		if (this.daoManager == null)
		{
			throw new EternaException(this.getType() + " [" + this.getName()
					+ "] not initialized.");
		}
		this.preparerManager.setIgnore(parameterIndex);
		this.daoManager.setParamSetted(parameterIndex - 1, false);
	}

	public void setIgnore(String parameterName)
			throws EternaException
	{
		this.setIgnore(this.getParameterIndex(parameterName));
	}

	public void setValuePreparer(ValuePreparer preparer)
			throws EternaException
	{
		if (this.daoManager == null)
		{
			throw new EternaException(this.getType() + " [" + this.getName()
					+ "] not initialized.");
		}
		this.preparerManager.setValuePreparer(preparer);
		preparer.setName(this.parameterArray[preparer.getRelativeIndex() - 1].getName());
		this.daoManager.setParamSetted(preparer.getRelativeIndex() - 1, true);
	}

	public void setString(int parameterIndex, String x)
			throws EternaException
	{
		Parameter p = this.getParameter(parameterIndex);
		this.setValuePreparer(p.createValuePreparer(x));
	}

	public void setString(String parameterName, String x)
			throws EternaException
	{
		Parameter p = this.getParameter(parameterName);
		this.setValuePreparer(p.createValuePreparer(x));
	}

	public void setObject(int parameterIndex, Object x)
			throws EternaException
	{
		Parameter p = this.getParameter(parameterIndex);
		this.setValuePreparer(p.createValuePreparer(x));
	}

	public void setObject(String parameterName, Object x)
			throws EternaException
	{
		Parameter p = this.getParameter(parameterName);
		this.setValuePreparer(p.createValuePreparer(x));
	}

	public void prepareValues(PreparedStatement stmt)
			throws EternaException, SQLException
	{
		if (this.daoManager == null)
		{
			throw new EternaException(this.getType() + " [" + this.getName()
					+ "] not initialized.");
		}
		this.preparerManager.prepareValues(new PreparedStatementWrapImpl(stmt));
	}

	public void prepareValues(PreparedStatementWrap stmtWrap)
			throws EternaException, SQLException
	{
		if (this.daoManager == null)
		{
			throw new EternaException(this.getType() + " [" + this.getName()
					+ "] not initialized.");
		}
		this.preparerManager.prepareValues(stmtWrap);
	}

	public Iterator getParameterIterator()
			throws EternaException
	{
		return new PreFetchIterator(Arrays.asList(this.parameterArray).iterator(), false);
	}

	private void addParameterNameMap(Parameter param)
			throws EternaException
	{
		int index = param.getIndex();
		if (index < 1 || index > this.getParameterCount())
		{
			throw new EternaException("Invalid parameter index [" + index + "] at "
					+ this.getType() + "[" + this.getName() + "].");
		}
		Object obj = this.parameterNameMap.put(param.getName(), param);
		if (obj != null)
		{
			throw new EternaException("Duplicate parameter name [" + param.getName()
					+ "] at " + this.getType() + "[" + this.getName() + "].");
		}
		ParameterManager pm = this.daoManager.getParameterManager(index - 1);
		pm.setParam(param);
	}

	public void addParameter(ParameterGenerator paramGenerator)
			throws EternaException
	{
		if (this.paramGroup == null)
		{
			this.paramGroup = new ParameterGroup(this.getName(), this.getType());
		}
		this.paramGroup.addParameter(paramGenerator);
	}

	/**
	 * 添加一个实体的引用.
	 */
	public void addEntityRef(EntityRef ref)
			throws EternaException
	{
		if (this.paramGroup == null)
		{
			this.paramGroup = new ParameterGroup(this.getName(), this.getType());
		}
		this.paramGroup.addEntityRef(ref);
	}

	protected int getParameterIndex(String name)
			throws EternaException
	{
		return this.getParameter(name).getIndex();
	}

	public Parameter getParameter(int paramIndex)
			throws EternaException
	{
		if (paramIndex < 1 || paramIndex > this.parameterArray.length)
		{
			throw new EternaException("Invalid parameter index [" + paramIndex + "].");
		}
		return this.parameterArray[paramIndex - 1];
	}

	public Parameter getParameter(String paramName)
			throws EternaException
	{
		Parameter p = (Parameter) this.parameterNameMap.get(paramName);
		if (p == null)
		{
			throw new EternaException("Invalid parameter name [" + paramName + "].");
		}
		return p;
	}

	public void destroy()
	{
	}

}

