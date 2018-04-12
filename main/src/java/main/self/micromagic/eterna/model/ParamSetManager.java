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

package self.micromagic.eterna.model;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import self.micromagic.cg.ClassGenerator;
import self.micromagic.eterna.dao.Dao;
import self.micromagic.eterna.dao.Parameter;
import self.micromagic.eterna.dao.ResultIterator;
import self.micromagic.eterna.dao.ResultRow;
import self.micromagic.eterna.dao.preparer.PreparerManager;
import self.micromagic.eterna.search.ConditionInfo;
import self.micromagic.eterna.search.SearchManager;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;

/**
 * 数据库操作的参数设置管理者.
 */
public class ParamSetManager
{
	private final Dao dao;
	private final int paramCount;
	private Map paramsCacheValues;

	public ParamSetManager(Dao dao)
			throws EternaException
	{
		this.dao = dao;
		this.paramCount = dao.getParameterCount();
	}

	/**
	 * 获取当前参数管理器所管理的Dao.
	 */
	public Dao getDao()
	{
		return this.dao;
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

	public static void preparerValue(Dao dao, Parameter param, String value)
			throws EternaException
	{
		try
		{
			dao.setString(param.getIndex(), value);
		}
		catch (Exception ex)
		{
			if (ex instanceof EternaException)
			{
				throw (EternaException) ex;
			}
			doPreparerError(dao, param, value, ex);
		}
	}

	public static void preparerValue(Dao dao, Parameter param, Object value)
			throws EternaException
	{
		try
		{
			dao.setObject(param.getIndex(), value);
		}
		catch (Exception ex)
		{
			if (ex instanceof EternaException)
			{
				throw (EternaException) ex;
			}
			doPreparerError(dao, param, value, ex);
		}
	}

	private static void doPreparerError(Dao dao, Parameter param,
			Object value, Exception err)
			throws EternaException
	{
		if (!"".equals(value))
		{
			// 如果因为是空字符串而产生的类型转换错误，则不记录警告日志.
			StringAppender str = StringTool.createStringAppender(64);
			str.append("SQL:[").append(dao.getName()).append("] ");
			str.append("param:[").append(param.getName()).append("] ");
			str.append("value:[").append(value).append("] preparer error.");
			AppData.log.warn(str, err);
		}
		if (dao.isDynamicParameter(param.getIndex()))
		{
			dao.setIgnore(param.getIndex());
		}
		else
		{
			dao.setObject(param.getIndex(), null);
		}
	}


	public void setSubSQL(int index, String subSQL, PreparerManager pm)
			throws EternaException
	{
		this.dao.setSubScript(index, subSQL, pm);
	}

	public void setSubSQL(int index, String subSQL)
			throws EternaException
	{
		this.dao.setSubScript(index, subSQL);
	}

	public void setParam(int index, Object value)
			throws EternaException
	{
		Parameter param = this.dao.getParameter(index);
		preparerValue(this.dao, param, value);
	}

	public void setIgnore(int index)
			throws EternaException
	{
		Parameter param = this.dao.getParameter(index);
		if (this.dao.isDynamicParameter(index))
		{
			this.dao.setIgnore(index);
		}
		else
		{
			this.dao.setObject(param.getIndex(), null);
		}
	}

	public void setParam(String name, Object value)
			throws EternaException
	{
		Parameter param = this.dao.getParameter(name);
		preparerValue(this.dao, param, value);
	}

	public void setIgnore(String name)
			throws EternaException
	{
		Parameter param = this.dao.getParameter(name);
		this.setIgnore(param.getIndex());
	}

	/**
	 * 将所有的动态参数设为忽略非动态参数设为null.
	 */
	public void setIgnores()
			throws EternaException
	{
		for (int i = 1; i <= this.paramCount; i++)
		{
			if (!this.dao.isParameterSetted(i))
			{
				this.setIgnore(i);
			}
		}
	}

	public void setParams(Map values)
			throws EternaException
	{
		for (int i = 1; i <= this.paramCount; i++)
		{
			if (!this.dao.isParameterSetted(i))
			{
				Parameter param = this.dao.getParameter(i);
				Object value = values.get(param.getName());
				if (value != null)
				{
					if (value instanceof String[])
					{
						String[] strs = (String[]) value;
						if (strs.length > 0)
						{
							preparerValue(this.dao, param, strs[0]);
						}
					}
					else
					{
						preparerValue(this.dao, param, value);
					}
				}
			}
		}
	}

	public int setParams(Map values, Name[] names, int index)
			throws EternaException
	{
		Object[][] arrays;
		boolean needInitArray = false;;
		if (index == 0)
		{
			arrays = new Object[names.length][];
			this.setValues(names, arrays);
			needInitArray = true;
		}
		else
		{
			arrays = (Object[][]) this.getValues(names);
			if (arrays == null)
			{
				arrays = new Object[names.length][];
				this.setValues(names, arrays);
				needInitArray = true;
			}
		}

		int loopCount = -1;
		for (int i = 0; i < names.length; i++)
		{
			Parameter param = names[i].daoIndex == -1 ? this.dao.getParameter(names[i].daoName)
					: this.dao.getParameter(names[i].daoIndex);
			Object[] array = null;
			if (needInitArray)
			{
				Object value = values.get(names[i].srcName);
				if (value != null)
				{
					if (ClassGenerator.isArray(value.getClass()))
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
				preparerValue(this.dao, param, array[index]);
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
			Parameter param = names[i].daoIndex == -1 ? this.dao.getParameter(names[i].daoName)
					: this.dao.getParameter(names[i].daoIndex);
			Object value = values.get(names[i].srcName);
			if (value != null)
			{
				if (value instanceof String[])
				{
					String[] strs = (String[]) value;
					if (strs.length > 0)
					{
						preparerValue(this.dao, param, strs[0]);
					}
				}
				else
				{
					preparerValue(this.dao, param, value);
				}
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
		for (int i = 1; i <= this.paramCount; i++)
		{
			if (!this.dao.isParameterSetted(i))
			{
				Parameter param = this.dao.getParameter(i);
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
					preparerValue(this.dao, param, value);
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
			if (ritr == null)
			{
				ritr = values;
				this.setValues(names, ritr);
			}
		}

		int loopCount = ritr.getCount();
		if (loopCount > 0)
		{
			ResultRow row = ritr.nextRow();
			for (int i = 0; i < names.length; i++)
			{
				Parameter param = names[i].daoIndex == -1 ? this.dao.getParameter(names[i].daoName)
						: this.dao.getParameter(names[i].daoIndex);
				int colIndex = -1;
				try
				{
					colIndex = row.findColumn(names[i].srcName, true);
				}
				catch (SQLException ex) {}
				if (colIndex != -1)
				{
					Object value = row.getObject(colIndex);
					preparerValue(this.dao, param, value);
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
			Parameter param = names[i].daoIndex == -1 ? this.dao.getParameter(names[i].daoName)
					: this.dao.getParameter(names[i].daoIndex);
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
				preparerValue(this.dao, param, value);
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
		for (int i = 1; i < this.paramCount; i++)
		{
			if (!this.dao.isParameterSetted(i))
			{
				Parameter param = this.dao.getParameter(i);
				ConditionInfo con = searchManager.getCondition(param.getName());
				if (con != null)
				{
					preparerValue(this.dao, param, con.value);
				}
			}
		}
	}

	public void setParams(SearchManager searchManager, Name[] names)
			throws EternaException, SQLException
	{
		for (int i = 0; i < names.length; i++)
		{
			Parameter param = names[i].daoIndex == -1 ? this.dao.getParameter(names[i].daoName)
					: this.dao.getParameter(names[i].daoIndex);
			ConditionInfo con = searchManager.getCondition(names[i].srcName);
			if (con != null)
			{
				preparerValue(this.dao, param, con.value);
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
		if (this.dao.isDynamicParameter(param.getIndex()))
		{
			this.dao.setIgnore(param.getIndex());
		}
		else
		{
			this.dao.setObject(param.getIndex(), null);
		}
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
		public final String daoName;
		public final int daoIndex;

		public Name(String srcName, String daoName)
		{
			this.daoIndex = -1;
			this.srcName = StringTool.intern(srcName);
			if (srcName == daoName)
			{
				// 如果两个值相同, 第二个值就不做处理了
				this.daoName = this.srcName;
			}
			else
			{
				this.daoName = StringTool.intern(daoName);
			}
		}

		public Name(String srcName, String daoName, int daoIndex)
		{
			this.daoIndex = -1;
			this.srcName = StringTool.intern(srcName);
			if (srcName == daoName)
			{
				// 如果两个值相同, 第二个值就不做处理了
				this.daoName = this.srcName;
			}
			else
			{
				this.daoName = StringTool.intern(daoName);
			}
		}

		public Name(Name other, int daoIndex)
		{
			this.daoIndex = daoIndex;
			if (other != null)
			{
				this.srcName = other.srcName;
				this.daoName = other.daoName;
			}
			else
			{
				this.srcName = null;
				this.daoName = null;
			}
		}

	}

}
