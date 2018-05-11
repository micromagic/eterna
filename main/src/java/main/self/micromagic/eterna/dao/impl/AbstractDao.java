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

import java.sql.Connection;
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
import self.micromagic.eterna.digester2.ParseException;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaCreater;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.util.Utility;
import self.micromagic.util.container.PreFetchIterator;
import self.micromagic.util.converter.BooleanConverter;

/**
 * 抽象的数据库操作对象.
 */
public abstract class AbstractDao extends AbstractGenerator
		implements Dao, EternaCreater
{
	private String preparedScript;
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
		if (this.initialized)
		{
			return true;
		}
		this.initialized = true;
		if (this.preparedScript == null)
		{
			this.preparedScript = "";
			log.error(this.getType() + " [" + this.getName()
					+ "]'s prepared-sql hasn't setted.");
		}
		this.attributes.convertType(factory, this.getType());

		List paramList = new ArrayList();
		List pgList = new ArrayList();
		if (this.paramGroup != null)
		{
			this.paramGroup.initialize(factory);
			Iterator itr = this.paramGroup.getParameterGeneratorIterator();
			int paramIndex = 1;
			while (itr.hasNext())
			{
				ParameterGenerator pg = (ParameterGenerator) itr.next();
				pgList.add(pg);
				paramList.add(pg.createParameter(paramIndex++));
			}
			this.paramGroup = null;
		}
		Parameter[] paramArray = new Parameter[paramList.size()];
		paramList.toArray(paramArray);
		this.parameterArray = paramArray;

		this.initElse(factory);

		boolean bindWithName = this.getBooleanAttr(PARAM_BIND_WITH_NAME_FLAG, false);
		this.daoManager = new DaoManager(bindWithName);
		String tmpScript = this.preChange(this.preparedScript);
		tmpScript = this.daoManager.preParse(tmpScript, this);
		this.daoManager.parse(tmpScript);
		this.daoManager.initialize(this);
		this.parameterNameMap = new HashMap();
		for (int i = 0; i < paramArray.length; i++)
		{
			Parameter param = paramArray[i];
			ParseException.setContextInfo(null, null, param.getName());
			if (bindWithName)
			{
				int[] positions = this.getParameterPositions(param.getName());
				if (positions.length > 1 || positions[0] != param.getIndex())
				{
					param = ((ParameterGenerator) pgList.get(i)).createParameter(
							param.getIndex(), positions);
					paramArray[i] = param;
				}
			}
			param.initialize(this.getFactory());
			this.addParameterNameMap(param, bindWithName);
		}
		ParseException.setContextInfo(null, null, "");
		if (bindWithName)
		{
			for (int i = 0; i < this.daoManager.getParameterCount(); i++)
			{
				ParameterManager pm = this.daoManager.getParameterManager(i);
				if (!this.parameterNameMap.containsKey(pm.getParamName()))
				{
					throw new ParseException("Used parameter bind with name, "
						+ "the parameter [" + pm.getParamName() + "] at position ["
						+ (i + 1) + "] hasn't binded.");
				}
			}
		}
		else if (this.daoManager.getParameterCount() != paramArray.length)
		{
			throw new ParseException("There are " + paramArray.length
					+ " parameter(s), " + "but the script [" + tmpScript + "] need "
					+ this.daoManager.getParameterCount() + " parameter(s).");
		}

		this.preparerManager = new PreparerManager(
				this, this.daoManager.getParameterCount(), paramArray);
		this.checkParamPermission(tmpScript);
		return false;
	}

	/**
	 * 预先对需要处理的数据操作脚本进行转换.
	 */
	protected String preChange(String script)
	{
		return script;
	}

	/**
	 * 检查设置了权限的参数是否为动态参数.
	 */
	protected void checkParamPermission(String script)
	{
		for (int i = 0; i < this.parameterArray.length; i++)
		{
			Parameter p = this.parameterArray[i];
			if (p.getPermissionSet() != null && !this.daoManager.isDynamicParameter(i))
			{
				String msg = this.getType() + " [" + this.getName() + "]'s parameter ["
						+ p.getName() + "] isn't dynamic, can't set permission.";
				throw new EternaException(msg);
			}
		}
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

	private int[] getParameterPositions(String name)
	{
		List positions = new ArrayList();
		for (int i = 0; i < this.daoManager.getParameterCount(); i++)
		{
			ParameterManager pm = this.daoManager.getParameterManager(i);
			if (name.equals(pm.getParamName()))
			{
				positions.add(Utility.createInteger(i + 1));
			}
		}
		if (positions.isEmpty())
		{
			throw new ParseException("Used parameter bind with name, "
				+ "not found parameter [" + name + "] in script.");
		}
		int[] result = new int[positions.size()];
		Iterator itr = positions.iterator();
		for (int i = 0; i < result.length; i++)
		{
			result[i] = ((Integer) itr.next()).intValue();
		}
		return result;
	}

	protected void copy(Dao copyObj)
	{
		AbstractDao other = (AbstractDao) copyObj;
		if (this.preparedScript != null)
		{
			other.preparerManager = new PreparerManager(
					other, this.daoManager.getParameterCount(), this.parameterArray);
			other.daoManager = this.daoManager.copy(true);
			other.preparedScript = this.preparedScript;
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
		return this.parameterArray.length;
	}

	public int getRealParameterCount() throws EternaException
	{
		this.checkInitialized();
		return this.daoManager.getParameterCount();
	}

	public boolean hasActiveParam()
			throws EternaException
	{
		this.checkInitialized();
		return this.preparerManager.hasActiveParam();
	}

	public int getActiveParamCount()
			throws EternaException
	{
		this.checkInitialized();
		return this.preparerManager.getParamCount();
	}

	public int getSubScriptCount()
			throws EternaException
	{
		this.checkInitialized();
		return this.daoManager.getSubPartCount();
	}

	/**
	 * 设置执行的数据库连接.
	 */
	protected void setExecuteConnection(Connection conn)
			throws SQLException
	{
		if (this.daoManager != null)
		{
			this.daoManager.setExecuteConnection(conn);
		}
	}

	public String getPreparedScript()
			throws EternaException
	{
		this.checkInitialized();
		return this.daoManager.getPreparedScript();
	}

	/**
	 * 根据临时设置的子句获取预备SQL.
	 */
	String getTempPreparedScript(int[] indexs, String[] subParts)
			throws EternaException
	{
		this.checkInitialized();
		return this.daoManager.getTempPreparedScript(indexs, subParts);
	}

	public void setPreparedScript(String script)
			throws EternaException
	{
		if (this.preparedScript != null)
		{
			throw new EternaException("You can't set prepared sql twice at "
					+ this.getType() + " [" + this.getName() + "].");
		}
		if (script == null)
		{
			throw new NullPointerException();
		}
		this.preparedScript = script;
	}

	public void setSubScript(int index, String subPart)
			throws EternaException
	{
		this.setSubScript(index, subPart, null);
	}

	public void setSubScript(int index, String subPart, PreparerManager pm)
			throws EternaException
	{
		this.checkInitialized();
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
		return this.isParameterSetted(this.getParameter(index));
	}

	public boolean isParameterSetted(String name)
			throws EternaException
	{
		return this.isParameterSetted(this.getParameter(name));
	}

	private boolean isParameterSetted(Parameter param)
			throws EternaException
	{
		this.checkInitialized();
		boolean setted = false;
		if (param.isMultiple())
		{
			int[] arr = param.listValuePreparerIndex();
			for (int i = 0; i < arr.length; i++)
			{
				if (!this.daoManager.isParamSetted(arr[i] - 1))
				{
					return false;
				}
			}
			setted = true;
		}
		else if (this.daoManager.isParamSetted(param.getValuePreparerIndex() - 1))
		{
			setted = true;
		}
		return setted;
	}

	public boolean isDynamicParameter(int index)
			throws EternaException
	{
		return this.isDynamicParameter(this.getParameter(index));
	}

	public boolean isDynamicParameter(String name)
			throws EternaException
	{
		return this.isDynamicParameter(this.getParameter(name));
	}

	public boolean isDynamicParameter(Parameter param)
			throws EternaException
	{
		this.checkInitialized();
		boolean dynamic = false;
		if (param.isMultiple())
		{
			int[] arr = param.listValuePreparerIndex();
			for (int i = 0; i < arr.length; i++)
			{
				if (!this.daoManager.isDynamicParameter(arr[i] - 1))
				{
					return false;
				}
			}
			dynamic = true;
		}
		else if (this.daoManager.isDynamicParameter(param.getValuePreparerIndex() - 1))
		{
			dynamic = true;
		}
		return dynamic;
	}

	public void setIgnore(int parameterIndex)
			throws EternaException
	{
		this.setIgnore(this.getParameter(parameterIndex));
	}

	public void setIgnore(String parameterName)
			throws EternaException
	{
		this.setIgnore(this.getParameter(parameterName));
	}

	private void setIgnore(Parameter param)
			throws EternaException
	{
		this.checkInitialized();
		if (param.isMultiple())
		{
			int[] arr = param.listValuePreparerIndex();
			for (int i = 0; i < arr.length; i++)
			{
				this.preparerManager.setIgnore(arr[i]);
				this.daoManager.setParamSetted(arr[i] - 1, false);
			}
		}
		else
		{
			this.preparerManager.setIgnore(param.getValuePreparerIndex());
			this.daoManager.setParamSetted(param.getValuePreparerIndex() - 1, false);
		}
	}

	private void setParamValue(ValuePreparer[] preparers, Parameter param)
			throws EternaException
	{
		this.checkInitialized();
		for (int i = 0; i < preparers.length; i++)
		{
			this.preparerManager.setValuePreparer(preparers[i]);
			this.daoManager.setParamSetted(preparers[i].getRelativeIndex() - 1, true);
		}
	}

	private void setParamValue(ValuePreparer preparer, Parameter param)
			throws EternaException
	{
		this.checkInitialized();
		this.preparerManager.setValuePreparer(preparer);
		this.daoManager.setParamSetted(preparer.getRelativeIndex() - 1, true);
	}

	public void setString(int parameterIndex, String x)
			throws EternaException
	{
		this.setString(x, this.getParameter(parameterIndex));
	}

	public void setString(String parameterName, String x)
			throws EternaException
	{
		this.setString(x, this.getParameter(parameterName));
	}

	private void setString(Object x, Parameter param)
			throws EternaException
	{
		if (param.isMultiple())
		{
			this.setParamValue(param.listValuePreparer(x), param);
		}
		else
		{
			this.setParamValue(param.createValuePreparer(x), param);
		}
	}

	public void setObject(int parameterIndex, Object x)
			throws EternaException
	{
		this.setObject(x, this.getParameter(parameterIndex));
	}

	public void setObject(String parameterName, Object x)
			throws EternaException
	{
		this.setObject(x, this.getParameter(parameterName));
	}

	private void setObject(Object x, Parameter param)
			throws EternaException
	{
		if (param.isMultiple())
		{
			this.setParamValue(param.listValuePreparer(x), param);
		}
		else
		{
			this.setParamValue(param.createValuePreparer(x), param);
		}
	}

	public void prepareValues(PreparedStatement stmt)
			throws EternaException, SQLException
	{
		this.checkInitialized();
		this.preparerManager.prepareValues(new PreparedStatementWrapImpl(stmt));
	}

	public void prepareValues(PreparedStatementWrap stmtWrap)
			throws EternaException, SQLException
	{
		this.checkInitialized();
		this.preparerManager.prepareValues(stmtWrap);
	}

	public Iterator getParameterIterator()
			throws EternaException
	{
		return new PreFetchIterator(Arrays.asList(this.parameterArray).iterator(), false);
	}

	private void addParameterNameMap(Parameter param, boolean paramBindWithName)
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
		if (!paramBindWithName)
		{
			ParameterManager pm = this.daoManager.getParameterManager(index - 1);
			pm.setParam(param);
		}
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

	private void checkInitialized()
	{
		if (this.daoManager == null)
		{
			throw new EternaException(this.getType() + " [" + this.getName()
					+ "] not initialized.");
		}
	}

	/**
	 * 从当前数据操作对象的属性中获取布尔值, 如果当前操作对象中不存在指定名称的属性,
	 * 则从所属的工厂属性中获取, 如果也不存在则使用默认值.
	 */
	protected boolean getBooleanAttr(String name, boolean defaultValue)
	{
		Object boolObj = this.getAttribute(name);
		if (boolObj == null)
		{
			boolObj = this.getFactory().getAttribute(name);
		}
		return boolObj == null ? defaultValue : BooleanConverter.toBoolean(boolObj);
	}

	public void destroy()
	{
		this.daoManager = null;
		this.preparerManager = null;
		this.parameterArray = null;
		this.parameterNameMap = null;
	}

}
