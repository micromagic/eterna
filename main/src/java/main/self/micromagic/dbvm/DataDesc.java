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

package self.micromagic.dbvm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;

import self.micromagic.eterna.dao.Dao;
import self.micromagic.eterna.dao.Parameter;
import self.micromagic.eterna.dao.PreparedStatementWrap;
import self.micromagic.eterna.dao.Update;
import self.micromagic.eterna.dao.impl.DaoManager;
import self.micromagic.eterna.dao.preparer.PreparerManager;
import self.micromagic.eterna.dao.preparer.ValuePreparer;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.EternaObject;
import self.micromagic.util.StringTool;
import self.micromagic.util.container.UnmodifiableIterator;


/**
 * 数据库操作定义的描述.
 */
public class DataDesc
		implements OptDesc, EternaObject, ConstantDef
{
	/**
	 * 数据操作的脚本.
	 */
	public String script;

	public void exec(Connection conn)
			throws SQLException
	{
		Update u = this.factory.createUpdate(COMMON_EXEC);
		u.setSubScript(1, this.script);
		u.execute(conn);
	}

	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		if (this.factory == null)
		{
			this.factory = factory;
			// 处理执行脚本中定义的常量
			DaoManager m = new DaoManager();
			m.parse(this.script);
			m.initialize(new DataDao(this));
			this.script = m.getPreparedSQL();
			return false;
		}
		return true;
	}
	private EternaFactory factory;

	public EternaFactory getFactory()
	{
		return this.factory;
	}

	public String getName()
			throws EternaException
	{
		return this.name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	private String name;

}

class DataDao
		implements Dao
{
	public DataDao(DataDesc dataDesc)
	{
		this.name = dataDesc.script;
		this.dataDesc = dataDesc;
	}
	private final String name;
	private final DataDesc dataDesc;

	public String getName()
			throws EternaException
	{
		return this.name;
	}

	public String getType()
			throws EternaException
	{
		return "data";
	}

	public Object getAttribute(String name)
	{
		return null;
	}

	public String[] getAttributeNames()
	{
		return StringTool.EMPTY_STRING_ARRAY;
	}

	public int getLogType()
	{
		return DAO_LOG_TYPE_NONE;
	}

	public EternaFactory getFactory()
	{
		return this.dataDesc.getFactory();
	}

	public int getParameterCount()
	{
		return 0;
	}

	public int getActiveParamCount()
	{
		return 0;
	}

	public boolean hasActiveParam()
	{
		return false;
	}

	public Parameter getParameter(String paramName)
	{
		return null;
	}

	public Parameter getParameter(int paramIndex)
			throws EternaException
	{
		return null;
	}

	public Iterator getParameterIterator()
			throws EternaException
	{
		return UnmodifiableIterator.EMPTY_ITERATOR;
	}

	public void execute(Connection conn)
	{
		throw new UnsupportedOperationException();
	}

	public int getSubScriptCount()
	{
		return 0;
	}

	public String getPreparedScript()
	{
		return this.dataDesc.script;
	}

	public void setSubScript(int index, String subPart)
	{
	}

	public void setSubScript(int index, String subPart, PreparerManager pm)
	{
	}

	public PreparerManager getPreparerManager()
	{
		return null;
	}

	public boolean isDynamicParameter(int index)
	{
		return false;
	}

	public boolean isDynamicParameter(String name)
	{
		return false;
	}

	public boolean isParameterSetted(int index)
	{
		return false;
	}

	public boolean isParameterSetted(String name)
	{
		return false;
	}

	public void setValuePreparer(ValuePreparer preparer)
	{
	}

	public void prepareValues(PreparedStatement stmt)
	{
	}

	public void prepareValues(PreparedStatementWrap stmtWrap)
	{
	}

	public void setIgnore(int parameterIndex)
	{
	}

	public void setIgnore(String parameterName)
	{
	}

	public void setString(int parameterIndex, String x)
	{
	}

	public void setString(String parameterName, String x)
	{
	}

	public void setObject(int parameterIndex, Object x)
	{
	}

	public void setObject(String parameterName, Object x)
	{
	}

}
