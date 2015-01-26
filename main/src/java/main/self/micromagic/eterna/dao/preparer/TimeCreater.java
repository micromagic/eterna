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
import java.sql.Time;
import java.text.DateFormat;
import java.util.Calendar;

import self.micromagic.eterna.dao.PreparedStatementWrap;
import self.micromagic.util.converter.TimeConverter;

class TimeCreater extends AbstractDateCreater
{
	public TimeCreater(String name)
	{
		super(name, "Time");
	}
	private static final TimeConverter convert = new TimeConverter();

	public ValuePreparer createPreparer(Object value, DateFormat format, Calendar calendar)
	{
		return new TimePreparer(this, convert.convertToTime(value, format), calendar);
	}

	public ValuePreparer createPreparer(String value, DateFormat format, Calendar calendar)
	{
		return new TimePreparer(this, convert.convertToTime(value, format), calendar);
	}

}

class TimePreparer extends AbstractValuePreparer
{
	protected Time value;
	protected Calendar calendar;

	public TimePreparer(PreparerCreater creater, Time value)
	{
		this(creater, value, null);
	}

	public TimePreparer(PreparerCreater creater, Time value, Calendar calendar)
	{
		super(creater);
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

}