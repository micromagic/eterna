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
import java.sql.Types;

import self.micromagic.eterna.dao.PreparedStatementWrap;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.util.converter.IntegerConverter;

class IntegerCreater extends AbstractNumberCreater
{
	public IntegerCreater(String name)
	{
		super(name);
	}
	private static final IntegerConverter convert = new IntegerConverter();

	public ValuePreparer createPreparer(Object value)
			throws EternaException
	{
		if (value == null)
		{
			return this.createNull(Types.INTEGER);
		}
		return new IntegerPreparer(this, convert.convertToInt(value, this.format));
	}

	public ValuePreparer createPreparer(String value)
			throws EternaException
	{
		if (value == null)
		{
			return this.createNull(Types.INTEGER);
		}
		return new IntegerPreparer(this, convert.convertToInt(value, this.format));
	}

}

class IntegerPreparer extends AbstractValuePreparer
{
	private final int value;

	public IntegerPreparer(PreparerCreater creater, int value)
	{
		super(creater);
		this.value = value;
	}

	public void setValueToStatement(int index, PreparedStatementWrap stmtWrap)
			throws SQLException
	{
		stmtWrap.setInt(this.getName(), index, this.value);
	}

}