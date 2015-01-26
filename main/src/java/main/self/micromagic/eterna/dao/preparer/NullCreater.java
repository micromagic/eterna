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

package self.micromagic.eterna.dao.preparer;

import java.sql.SQLException;

import self.micromagic.eterna.dao.PreparedStatementWrap;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;

class NullCreater implements PreparerCreater
{
	public NullCreater(String name)
	{
		this.name = name;
	}
	private final String name;

	public String getName()
	{
		return this.name;
	}

	public Object getAttribute(String name)
	{
		return null;
	}

	public boolean initialize(EternaFactory factory) throws EternaException
	{
		if (this.factory == null)
		{
			this.factory = factory;
			return false;
		}
		return true;
	}
	private EternaFactory factory;

	public EternaFactory getFactory() throws EternaException
	{
		return this.factory;
	}

	public ValuePreparer createPreparer(Object value)
	{
		return new NullPreparer(this, java.sql.Types.JAVA_OBJECT);
	}

	public ValuePreparer createPreparer(String value)
	{
		return new NullPreparer(this, java.sql.Types.VARCHAR);
	}

	/**
	 * @param type  sql的类型
	 * @see java.sql.Types
	 */
	public ValuePreparer createPreparer(int type)
	{
		return new NullPreparer(this, type);
	}

}

class NullPreparer extends AbstractValuePreparer
{
	private final int type;

	public NullPreparer(PreparerCreater creater, int type)
	{
		super(creater);
		this.type = type;
	}

	public void setValueToStatement(int index, PreparedStatementWrap stmtWrap) throws SQLException
	{
		stmtWrap.setNull(this.getName(), index, this.type);
	}

}
