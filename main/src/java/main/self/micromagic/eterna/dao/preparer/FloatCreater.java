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
import self.micromagic.util.converter.DoubleConverter;

class FloatCreater extends AbstractNumberCreater
{
	private static final DoubleConverter convert = new DoubleConverter();

	public FloatCreater(String name)
	{
		super(name);
	}

	public Object convertValue(Object value)
	{
		if (convert.isNull(value))
		{
			return null;
		}
		return new Float((float) convert.convertToDouble(value, this.format));
	}

	public Object convertValue(String value)
	{
		if (convert.isNull(value))
		{
			return null;
		}
		return new Float((float) convert.convertToDouble(value, this.format));
	}

	public ValuePreparer createPreparer(Object value)
			throws EternaException
	{
		if (convert.isNull(value))
		{
			return this.createNull(Types.FLOAT);
		}
		return new FloatPreparer(this, (float) convert.convertToDouble(value, this.format));
	}

	public ValuePreparer createPreparer(String value)
			throws EternaException
	{
		if (convert.isNull(value))
		{
			return this.createNull(Types.FLOAT);
		}
		return new FloatPreparer(this, (float) convert.convertToDouble(value, this.format));
	}

}

class FloatPreparer extends AbstractValuePreparer
{
	private final float value;

	public FloatPreparer(PreparerCreater creater, float value)
	{
		super(creater);
		this.value = value;
	}

	public void setValueToStatement(int index, PreparedStatementWrap stmtWrap)
			throws SQLException
	{
		stmtWrap.setFloat(this.getName(), index, this.value);
	}

}
