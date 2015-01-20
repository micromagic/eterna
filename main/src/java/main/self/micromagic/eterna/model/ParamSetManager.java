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

package self.micromagic.eterna.model;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import self.micromagic.eterna.base.Base;
import self.micromagic.eterna.base.Parameter;
import self.micromagic.eterna.base.ResultIterator;
import self.micromagic.eterna.base.ResultRow;
import self.micromagic.eterna.base.preparer.CreaterManager;
import self.micromagic.eterna.base.preparer.PreparerManager;
import self.micromagic.eterna.base.preparer.ValuePreparer;
import self.micromagic.eterna.search.SearchManager;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;

public class ParamSetManager
{
	private final Base sql;
	private final Parameter[] params;
	private final boolean[] paramsSetted;
	private final Object[] paramsValues;
	private Map paramsCacheValues;

	public ParamSetManager(Base sql)
			throws EternaException
	{
		this.sql = sql;
		Iterator itr = sql.getParameterIterator();
		int count = sql.getParameterCount();
		this.params = new Parameter[count];
		this.paramsSetted = new boolean[count];
		this.paramsValues = new Object[count];
		for (int i = 0; i < count; i++)
		{
			Parameter param = (Parameter) itr.next();
			// 因为parameter的index从1开始，所以要减1
			int paramIndex = param.getIndex() - 1;
			this.params[paramIndex] = param;
			this.paramsSetted[paramIndex] = false;
			this.paramsValues[paramIndex] = null;
		}
	}

	/**
	 * 获取当前参数管理器所管理的SQLAdapter.
	 */
	public Base getSQLAdapter()
	{
		return this.sql;
	}

	private Object getValues(Name[] names)
	{
		if (this.paramsCacheValues == null)
		{
			return null;
		}
		return this.paramsCacheValues.get(names);
	}

	private void setValues(Name[] names, Object values)
	{
		if (this.paramsCacheValues == null)
		{
			this.paramsCacheValues = new HashMap();
		}
		this.paramsCacheValues.put(names, values);
	}

	public static void preparerValue(Base sql, Parameter param, String value)
			throws EternaException
	{
		try
		{
			sql.setValuePreparer(param.createValuePreparer(value));
		}
		catch (Exception ex)
		{
			if (ex instanceof EternaException)
			{
				throw (EternaException) ex;
			}
			doPreparerError(sql, param, value, ex);
		}
	}

	public static void preparerValue(Base sql, Parameter param, Object value)
			throws EternaException
	{
		try
		{
			sql.setValuePreparer(param.createValuePreparer(value));
		}
		catch (Exception ex)
		{
			if (ex instanceof EternaException)
			{
				throw (EternaException) ex;
			}
			doPreparerError(sql, param, value, ex);
		}
	}

	private static void doPreparerError(Base sql, Parameter param, Object value,
			Exception ex)
			throws EternaException
	{
		if (!"".equals(value))
		{
			// 如果因为是空字符串而产生的类型转换错误，则不记录警告日志.
			StringAppender str = StringTool.createStringAppender(64);
			str.append("SQL:[").append(sql.getName()).append("] ");
			str.append("param:[").append(param.getName()).append("] ");
			str.append("value:[").append(value).append("] preparer error.");
			AppData.log.warn(str, ex);
		}
		if (sql.isDynamicParameter(param.getIndex()))
		{
			sql.setIgnore(param.getIndex());
		}
		else
		{
			ValuePreparer p = CreaterManager.createNullPreparer(
					sql.getFactory(), param.getType());
			p.setRelativeIndex(param.getIndex());
			sql.setValuePreparer(p);
		}
	}


	public void setSubSQL(int index, String subSQL, PreparerManager pm)
			throws EternaException
	{
		this.sql.setSubSQL(index, subSQL, pm);
	}

	public void setSubSQL(int index, String subSQL)
			throws EternaException
	{
		this.sql.setSubSQL(index, subSQL);
	}

	public void setParam(int index, Object value)
			throws EternaException
	{
		// 因为parameter的index从1开始，所以要减1
		Parameter param = this.params[index - 1];
		preparerValue(this.sql, param, value);
		this.paramsSetted[index - 1] = true;
		this.paramsValues[index - 1] = value;
	}

	public void setIgnore(int index)
			throws EternaException
	{
		// 因为parameter的index从1开始，所以要减1
		Parameter param = this.params[index - 1];
		this.sql.setIgnore(param.getIndex());
		this.paramsSetted[index - 1] = true;
		this.paramsValues[index - 1] = null;
	}

	public Object getParamValue(int index)
			throws EternaException
	{
		return this.paramsValues[index - 1];
	}

	public boolean isParamSetted(int index)
			throws EternaException
	{
		return this.paramsSetted[index - 1];
	}

	public void setParam(String name, Object value)
			throws EternaException
	{
		Parameter param = this.sql.getParameter(name);
		preparerValue(this.sql, param, value);
		// 因为parameter的index从1开始，所以要减1
		int paramIndex = param.getIndex() - 1;
		this.paramsSetted[paramIndex] = true;
		this.paramsValues[paramIndex] = value;
	}

	public void setIgnore(String name)
			throws EternaException
	{
		Parameter param = this.sql.getParameter(name);
		if (this.sql.isDynamicParameter(param.getIndex()))
		{
			this.sql.setIgnore(param.getIndex());
		}
		else
		{
			ValuePreparer p = CreaterManager.createNullPreparer(
					sql.getFactory(), param.getType());
			p.setRelativeIndex(param.getIndex());
			sql.setValuePreparer(p);
		}
		// 因为parameter的index从1开始，所以要减1
		int paramIndex = param.getIndex() - 1;
		this.paramsSetted[paramIndex] = true;
		this.paramsValues[paramIndex] = null;
	}

	public Object getParamValue(String name)
			throws EternaException
	{
		Parameter param = this.sql.getParameter(name);
		return this.paramsValues[param.getIndex() - 1];
	}

	public boolean isParamSetted(String name)
			throws EternaException
	{
		Parameter param = this.sql.getParameter(name);
		return this.paramsSetted[param.getIndex() - 1];
	}

	/**
	 * 将所有的动态参数设为忽略非动态参数设为null. <p>
	 *
	 * @param settedFlag   设置完后是否要置上已设置标志
	 */
	public void setIgnores(boolean settedFlag)
			throws EternaException
	{
		for (int i = 0; i < this.params.length; i++)
		{
			if (!this.paramsSetted[i])
			{
				Parameter param = this.params[i];
				if (this.sql.isDynamicParameter(param.getIndex()))
				{
					this.sql.setIgnore(param.getIndex());
				}
				else
				{
					ValuePreparer p = CreaterManager.createNullPreparer(
							sql.getFactory(), param.getType());
					p.setRelativeIndex(param.getIndex());
					sql.setValuePreparer(p);
				}
				this.paramsSetted[i] = settedFlag;
				if (settedFlag)
				{
					this.paramsValues[i] = null;
				}
			}
		}
	}

	public void setParams(Map values)
			throws EternaException
	{
		for (int i = 0; i < this.params.length; i++)
		{
			if (!this.paramsSetted[i])
			{
				Parameter param = this.params[i];
				Object value = values.get(param.getName());
				if (value != null)
				{
					if (value instanceof String[])
					{
						String[] strs = (String[]) value;
						if (strs.length == 0)
						{
							continue;
						}
						preparerValue(this.sql, param, strs[0]);
					}
					else
					{
						preparerValue(this.sql, param, value);
					}
					this.paramsSetted[i] = true;
					this.paramsValues[i] = value;
				}
			}
		}
	}

	public int setParams(Map values, Name[] names, int index)
			throws EternaException
	{
		Object[][] arrays;
		if (index == 0)
		{
			arrays = new Object[names.length][];
			this.setValues(names, arrays);
		}
		else
		{
			arrays = (Object[][]) this.getValues(names);
		}

		int loopCount = -1;
		for (int i = 0; i < names.length; i++)
		{
			Parameter param = names[i].sqlIndex == -1 ?
					this.sql.getParameter(names[i].sqlName) : this.params[names[i].sqlIndex - 1];
			Object[] array = null;
			if (index == 0)
			{
				Object value = values.get(names[i].srcName);
				if (value != null)
				{
					if (value instanceof Object[])
					{
						array = (Object[]) value;
						arrays[i] = array;

						if (loopCount == -1)
						{
							loopCount = array.length;
						}
						if (loopCount != array.length)
						{
							throw new EternaException("The param count not same, "
									+ loopCount + " and " + array.length + ".");
						}
					}
				}
			}
			else
			{
				array = arrays[i];
			}
			if (array != null)
			{
				loopCount = array.length;
				preparerValue(this.sql, param, array[index]);
				int paramIndex = param.getIndex() - 1;
				this.paramsSetted[paramIndex] = true;
				this.paramsValues[paramIndex] = array[index];
			}
			else
			{
				this.dealNull(param);
			}
		}
		return loopCount;
	}

	public void setParams(Map values, Name[] names)
			throws EternaException
	{
		for (int i = 0; i < names.length; i++)
		{
			Parameter param = names[i].sqlIndex == -1 ?
					this.sql.getParameter(names[i].sqlName) : this.params[names[i].sqlIndex - 1];
			Object value = values.get(names[i].srcName);
			if (value != null)
			{
				if (value instanceof String[])
				{
					String[] strs = (String[]) value;
					if (strs.length == 0)
					{
						continue;
					}
					preparerValue(this.sql, param, strs[0]);
				}
				else
				{
					preparerValue(this.sql, param, value);
				}
				int paramIndex = param.getIndex() - 1;
				this.paramsSetted[paramIndex] = true;
				this.paramsValues[paramIndex] = value;
			}
			else
			{
				this.dealNull(param);
			}
		}
	}

	public void setParams(ResultRow values)
			throws EternaException, SQLException
	{
		for (int i = 0; i < this.params.length; i++)
		{
			if (!this.paramsSetted[i])
			{
				Parameter param = this.params[i];
				int colIndex = -1;
				try
				{
					colIndex = values.findColumn(param.getName(), true);
				}
				catch (SQLException ex) {}
				catch (EternaException ex) {}
				if (colIndex != -1)
				{
					Object value = values.getObject(colIndex);
					preparerValue(this.sql, param, value);
					this.paramsSetted[i] = true;
					this.paramsValues[i] = value;
				}
			}
		}
	}

	public int setParams(ResultIterator values, Name[] names, int index)
			throws EternaException, SQLException
	{
		ResultIterator ritr;
		if (index == 0)
		{
			ritr = values;
			this.setValues(names, ritr);
		}
		else
		{
			ritr = (ResultIterator) this.getValues(names);
		}

		int loopCount = ritr.getRecordCount();
		if (loopCount > 0)
		{
			ResultRow row = ritr.nextRow();
			for (int i = 0; i < names.length; i++)
			{
				Parameter param = names[i].sqlIndex == -1 ?
						this.sql.getParameter(names[i].sqlName) : this.params[names[i].sqlIndex - 1];
				int colIndex = -1;
				try
				{
					colIndex = row.findColumn(names[i].srcName, true);
				}
				catch (SQLException ex) {}
				catch (EternaException ex) {}
				if (colIndex != -1)
				{
					Object value = row.getObject(colIndex);
					preparerValue(this.sql, param, value);
					int paramIndex = param.getIndex() - 1;
					this.paramsSetted[paramIndex] = true;
					this.paramsValues[paramIndex] = value;
				}
				else
				{
					this.dealNull(param);
				}
			}
		}
		else
		{
			loopCount = -1;
		}
		return loopCount;
	}

	public void setParams(ResultRow values, Name[] names)
			throws EternaException, SQLException
	{
		for (int i = 0; i < names.length; i++)
		{
			Parameter param = names[i].sqlIndex == -1 ?
					this.sql.getParameter(names[i].sqlName) : this.params[names[i].sqlIndex - 1];
			int colIndex = -1;
			try
			{
				colIndex = values.findColumn(names[i].srcName, true);
			}
			catch (SQLException ex) {}
			catch (EternaException ex) {}
			if (colIndex != -1)
			{
				Object value = values.getObject(colIndex);
				preparerValue(this.sql, param, value);
				int paramIndex = param.getIndex() - 1;
				this.paramsSetted[paramIndex] = true;
				this.paramsValues[paramIndex] = value;
			}
			else
			{
				this.dealNull(param);
			}
		}
	}

	public void setParams(SearchManager searchManager)
			throws EternaException, SQLException
	{
		for (int i = 0; i < this.params.length; i++)
		{
			if (!this.paramsSetted[i])
			{
				Parameter param = this.params[i];
				SearchManager.ConditionInfo con = searchManager.getCondition(param.getName());
				if (con != null)
				{
					preparerValue(this.sql, param, con.value);
					this.paramsSetted[i] = true;
					this.paramsValues[i] = con.value;
				}
			}
		}
	}

	public void setParams(SearchManager searchManager, Name[] names)
			throws EternaException, SQLException
	{
		for (int i = 0; i < names.length; i++)
		{
			Parameter param = names[i].sqlIndex == -1 ?
					this.sql.getParameter(names[i].sqlName) : this.params[names[i].sqlIndex - 1];
			SearchManager.ConditionInfo con = searchManager.getCondition(names[i].srcName);
			if (con != null)
			{
				preparerValue(this.sql, param, con.value);
				this.paramsSetted[i] = true;
				this.paramsValues[i] = con.value;
			}
			else
			{
				this.dealNull(param);
			}
		}
	}

	private void dealNull(Parameter param)
			throws EternaException
	{
		if (this.sql.isDynamicParameter(param.getIndex()))
		{
			this.sql.setIgnore(param.getIndex());
		}
		else
		{
			ValuePreparer p = CreaterManager.createNullPreparer(
					sql.getFactory(), param.getType());
			p.setRelativeIndex(param.getIndex());
			sql.setValuePreparer(p);
		}
		int paramIndex = param.getIndex() - 1;
		this.paramsSetted[paramIndex] = true;
		this.paramsValues[paramIndex] = null;
	}

	public int setParams(Name[] names, int index)
			throws EternaException, SQLException
	{
		if (index < 1)
		{
			throw new EternaException("In this method:setParams(String[], int), the second parameter mustn't below than 1.");
		}
		Object obj = this.getValues(names);
		if (obj != null)
		{
			if (obj instanceof Object[][])
			{
				return this.setParams((Map) null, names, index);
			}
			if (obj instanceof ResultIterator)
			{
				return this.setParams((ResultIterator) null, names, index);
			}
		}
		throw new EternaException("Not found cached values.");
	}

	public static class Name
	{
		public final String srcName;
		public final String sqlName;
		public final int sqlIndex;

		public Name(String srcName, String sqlName)
		{
			this.sqlIndex = -1;
			this.srcName = StringTool.intern(srcName);
			if (srcName == sqlName)
			{
				// 如果两个值相同, 第二个值就不做处理了
				this.sqlName = this.srcName;
			}
			else
			{
				this.sqlName = StringTool.intern(sqlName);
			}
		}

		public Name(Name other, int sqlIndex)
		{
			this.sqlIndex = sqlIndex;
			if (other != null)
			{
				this.srcName = other.srcName;
				this.sqlName = other.sqlName;
			}
			else
			{
				this.srcName = null;
				this.sqlName = null;
			}
		}

	}

}