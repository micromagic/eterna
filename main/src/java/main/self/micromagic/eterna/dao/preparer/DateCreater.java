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

import java.sql.Date;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Calendar;

import self.micromagic.eterna.dao.PreparedStatementWrap;
import self.micromagic.util.converter.DateConverter;

class DateCreater extends AbstractDateCreater
{
	private static final DateConverter convert = new DateConverter();

	public DateCreater(String name)
	{
		super(name, "Date");
	}

	public Object convertValue(Object value, DateFormat format)
	{
		return convert.convertToDate(value, format);
	}

	protected Object convertValue(String value, DateFormat format)
	{
		return convert.convertToDate(value, format);
	}

	public ValuePreparer createPreparer(Object value, DateFormat format, Calendar calendar)
	{
		return new DatePreparer(this, convert.convertToDate(value, format), calendar);
	}

	public ValuePreparer createPreparer(String value, DateFormat format, Calendar calendar)
	{
		return new DatePreparer(this, convert.convertToDate(value, format), calendar);
	}

}

class DatePreparer extends AbstractValuePreparer
{
	protected Date value;
	protected Calendar calendar;

	public DatePreparer(PreparerCreater creater, Date value)
	{
		this(creater, value, null);
	}

	public DatePreparer(PreparerCreater creater, Date value, Calendar calendar)
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
			stmtWrap.setDate(this.getName(), index, this.value);
		}
		else
		{
			stmtWrap.setDate(this.getName(), index, this.value, this.calendar);
		}
	}

}
