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

import java.sql.SQLException;
import java.sql.Types;

import self.micromagic.eterna.dao.PreparedStatementWrap;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.util.StringTool;
import self.micromagic.util.converter.BooleanConverter;

class BooleanCreater extends AbstractPreparerCreater
{
	private static final BooleanConverter convert = new BooleanConverter();
	private String[] trueValues = null;

	public BooleanCreater(String name)
	{
		super(name);
	}

	public void setPattern(String pattern)
	{
		this.trueValues = StringTool.separateString(pattern, ";", true);
	}

	public Object convertValue(Object value)
	{
		if (convert.isNull(value))
		{
			return null;
		}
		return convert.convertToBoolean(value, this.trueValues) ? Boolean.TRUE : Boolean.FALSE;
	}

	public Object convertValue(String value)
	{
		if (convert.isNull(value))
		{
			return null;
		}
		return convert.convertToBoolean(value, this.trueValues) ? Boolean.TRUE : Boolean.FALSE;
	}

	public ValuePreparer createPreparer(Object value)
			throws EternaException
	{
		if (convert.isNull(value))
		{
			return this.createNull(Types.BOOLEAN);
		}
		return new BooleanPreparer(this, convert.convertToBoolean(value, this.trueValues));
	}

	public ValuePreparer createPreparer(String value)
			throws EternaException
	{
		if (convert.isNull(value))
		{
			return this.createNull(Types.BOOLEAN);
		}
		return new BooleanPreparer(this, convert.convertToBoolean(value, this.trueValues));
	}

}

class BooleanPreparer extends AbstractValuePreparer
{
	private final boolean value;

	public BooleanPreparer(boolean value)
	{
		this(null, value);
	}

	public BooleanPreparer(PreparerCreater creater, boolean value)
	{
		super(creater);
		this.value = value;
	}

	public void setValueToStatement(int index, PreparedStatementWrap stmtWrap)
			throws SQLException
	{
		stmtWrap.setBoolean(this.getName(), index, this.value);
	}

}
