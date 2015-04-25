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

package self.micromagic.eterna.dao.preparer;

import java.sql.Clob;
import java.sql.SQLException;

import self.micromagic.eterna.dao.PreparedStatementWrap;

class ClobCreater extends AbstractPreparerCreater
{
	public ClobCreater(String name)
	{
		super(name);
	}

	public ValuePreparer createPreparer(Object value)
	{
		return new ClobPreparer(this, (Clob) value);
	}

	public ValuePreparer createPreparer(String value)
	{
		throw new ClassCastException("Can't cast String to Clob.");
	}

}

class ClobPreparer extends AbstractValuePreparer
{
	private final Clob value;

	public ClobPreparer(PreparerCreater creater, Clob value)
	{
		super(creater);
		this.value = value;
	}

	public void setValueToStatement(int index, PreparedStatementWrap stmtWrap)
			throws SQLException
	{
		stmtWrap.setClob(this.getName(), index, this.value);
	}

}