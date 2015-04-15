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
import java.text.NumberFormat;
import java.text.DecimalFormat;

import self.micromagic.eterna.sql.PreparedStatementWrap;
import self.micromagic.util.converter.LongConverter;
import self.micromagic.eterna.share.EternaException;

class LongPreparer extends AbstractValuePreparer
{
	private long value;

	public LongPreparer(ValuePreparerCreater vpc, long value)
	{
		super(vpc);
		this.value = value;
	}

	public void setValueToStatement(int index, PreparedStatementWrap stmtWrap)
			throws SQLException
	{
		stmtWrap.setLong(this.getName(), index, this.value);
	}

	static class Creater extends AbstractCreater
	{
		LongConverter convert = new LongConverter();
		NumberFormat format = null;

		public Creater(ValuePreparerCreaterGenerator vpcg)
		{
			super(vpcg);
		}

		public void setFormat(String formatStr)
		{
			this.format = new DecimalFormat(formatStr);
		}

		public ValuePreparer createPreparer(Object value)
				throws EternaException
		{
			if (value == null)
			{
				return this.vpcg.createNullPreparer(0, Types.BIGINT);
			}
			return new LongPreparer(this, this.convert.convertToLong(value, this.format));
		}

		public ValuePreparer createPreparer(String value)
				throws EternaException
		{
			if (value == null)
			{
				return this.vpcg.createNullPreparer(0, Types.BIGINT);
			}
			return new LongPreparer(this, this.convert.convertToLong(value, this.format));
		}

		public ValuePreparer createPreparer(long value)
		{
			return new LongPreparer(this, value);
		}

	}

}