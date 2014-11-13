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

package self.micromagic.eterna.sql.preparer;

import java.sql.SQLException;
import java.sql.Types;

import self.micromagic.eterna.sql.PreparedStatementWrap;
import self.micromagic.util.converter.BooleanConverter;
import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.util.StringTool;

class BooleanPreparer extends AbstractValuePreparer
{
	private boolean value;

	public BooleanPreparer(ValuePreparerCreater vpc, boolean value)
	{
		super(vpc);
		this.value = value;
	}

	public void setValueToStatement(int index, PreparedStatementWrap stmtWrap)
			throws SQLException
	{
		stmtWrap.setBoolean(this.getName(), index, this.value);
	}

	static class Creater extends AbstractCreater
	{
		BooleanConverter convert = new BooleanConverter();
		String[] trueValues = null;

		public Creater(ValuePreparerCreaterGenerator vpcg)
		{
			super(vpcg);
		}

		public void setFormat(String formatStr)
		{
			this.trueValues = StringTool.separateString(formatStr, ";", true);
		}

		public ValuePreparer createPreparer(Object value)
				throws ConfigurationException
		{
			if (value == null)
			{
				return this.vpcg.createNullPreparer(0, Types.BOOLEAN);
			}
			return new BooleanPreparer(this, this.convert.convertToBoolean(value, this.trueValues));
		}

		public ValuePreparer createPreparer(String value)
				throws ConfigurationException
		{
			if (value == null)
			{
				return this.vpcg.createNullPreparer(0, Types.BOOLEAN);
			}
			return new BooleanPreparer(this, this.convert.convertToBoolean(value, this.trueValues));
		}

		public ValuePreparer createPreparer(boolean value)
		{
			return new BooleanPreparer(this, value);
		}

	}

}