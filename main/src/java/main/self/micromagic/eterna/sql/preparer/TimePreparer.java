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
import java.sql.Time;
import java.util.Calendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import self.micromagic.eterna.sql.PreparedStatementWrap;
import self.micromagic.util.converter.TimeConverter;

class TimePreparer extends AbstractValuePreparer
{
	protected Time value;
	protected Calendar calendar;

	public TimePreparer(ValuePreparerCreater vpc, Time value)
	{
		this(vpc, value, null);
	}

	public TimePreparer(ValuePreparerCreater vpc, Time value, Calendar calendar)
	{
		super(vpc);
		this.value = value;
		this.calendar = calendar;
	}

	public void setValueToStatement(int index, PreparedStatementWrap stmtWrap)
			throws SQLException
	{
		if (this.calendar == null)
		{
			stmtWrap.setTime(this.getName(), index, this.value);
		}
		else
		{
			stmtWrap.setTime(this.getName(), index, this.value, this.calendar);
		}
	}

	static class Creater extends AbstractCreater
	{
		TimeConverter convert = new TimeConverter();
		DateFormat format = null;

		public Creater(ValuePreparerCreaterGenerator vpcg)
		{
			super(vpcg);
		}

		public void setFormat(String formatStr)
		{
			this.format = new SimpleDateFormat(formatStr);
		}

		public ValuePreparer createPreparer(Object value)
		{
			return new TimePreparer(this, this.convert.convertToTime(value, this.format));
		}

		public ValuePreparer createPreparer(String value)
		{
			return new TimePreparer(this, this.convert.convertToTime(value, this.format));
		}

		public ValuePreparer createPreparer(Time value)
		{
			return new TimePreparer(this, value);
		}

		public ValuePreparer createPreparer(Time value, Calendar calendar)
		{
			return new TimePreparer(this, value, calendar);
		}

	}

}