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

package self.micromagic.util.converter;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;

import self.micromagic.cg.ClassGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.FormatTool;
import self.micromagic.util.container.RequestParameterMap;
import self.micromagic.util.ref.ObjectRef;
import self.micromagic.util.ref.StringRef;

public class CalendarConverter extends AbstractNumericalConverter
{
	private DateFormat[] dateFormats;

	public void setDateFormat(DateFormat dateFormat)
	{
		this.dateFormats = new DateFormat[]{dateFormat};
	}

	public void setDateFormats(DateFormat[] dateFormats)
	{
		this.dateFormats = dateFormats;
	}

	public int getConvertType(StringRef typeName)
	{
		if (typeName != null)
		{
			typeName.setString("Calendar");
		}
		return TypeManager.TYPE_TIMPSTAMP;
	}

	public Calendar getResult(Object result)
			throws EternaException
	{
		try
		{
			return this.convertToCalendar(result);
		}
		catch (Exception ex)
		{
			throw getErrorTypeException(result, "Calendar");
		}
	}

	public Calendar convertToCalendar(Object value)
	{
		return this.convertToCalendar(value, this.dateFormats);
	}

	public Calendar convertToCalendar(Object value, DateFormat format)
	{
		return this.convertToCalendar(value, new DateFormat[]{format});
	}

	public Calendar convertToCalendar(Object value, DateFormat[] formats)
	{
		if (value == null || value instanceof Calendar)
		{
			return (Calendar) value;
		}
		if (value instanceof java.util.Date)
		{
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(((java.util.Date) value).getTime());
			return c;
		}
		if (value instanceof Number)
		{
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(((Number) value).longValue());
			return c;
		}
		if (value instanceof String)
		{
			return this.convertToCalendar((String) value, formats);
		}
		Object tmpObj = this.changeByPropertyEditor(value);
		if (tmpObj instanceof Calendar)
		{
			return (Calendar) tmpObj;
		}
		if (ClassGenerator.isArray(value.getClass()))
		{
			String str = RequestParameterMap.getFirstParam(value);
			return this.convertToCalendar(str, formats);
		}
		if (value instanceof ObjectRef)
		{
			return this.convertToCalendar(((ObjectRef) value).getObject(), formats);
		}
		throw new ClassCastException(getCastErrorMessage(value, "Calendar"));
	}

	public Calendar convertToCalendar(String value)
	{
		return this.convertToCalendar(value, this.dateFormats);
	}

	public Calendar convertToCalendar(String value, DateFormat format)
	{
		return this.convertToCalendar(value, new DateFormat[]{format});
	}

	public Calendar convertToCalendar(String value, DateFormat[] formats)
	{
		if (value == null || (this.emptyToNull && value.length() == 0))
		{
			return null;
		}
		try
		{
			if (formats == null)
			{
				Object tmpObj = this.changeByPropertyEditor(value);
				if (tmpObj instanceof Calendar)
				{
					return (Calendar) tmpObj;
				}
				Calendar c = Calendar.getInstance();
				try
				{
					c.setTimeInMillis(FormatTool.parseDatetime(value).getTime());
				}
				catch (ParseException ex)
				{
					c.setTimeInMillis(FormatTool.parseDate(value).getTime());
				}
				return c;
			}
			else
			{
				Calendar c = Calendar.getInstance();
				for (int i = 0; i < formats.length; i++)
				{
					try
					{
						c.setTimeInMillis(FormatTool.getThreadFormat(formats[i]).parse(value).getTime());
						return c;
					}
					catch (Throwable ex) {}
				}
			}
		}
		catch (ParseException ex) {}
		throw new ClassCastException(getCastErrorMessage(value, "Calendar"));
	}

	public Object convert(Object value)
	{
		if (value instanceof Calendar)
		{
			return value;
		}
		try
		{
			return this.convertToCalendar(value);
		}
		catch (Exception ex)
		{
			if (this.needThrow)
			{
				if (ex instanceof RuntimeException)
				{
					throw (RuntimeException) ex;
				}
				throw new ClassCastException(getCastErrorMessage(value, "Calendar"));
			}
			else
			{
				return null;
			}
		}
	}

	public Object convert(String value)
	{
		try
		{
			return this.convertToCalendar(value);
		}
		catch (Exception ex)
		{
			if (this.needThrow)
			{
				if (ex instanceof RuntimeException)
				{
					throw (RuntimeException) ex;
				}
				throw new ClassCastException(getCastErrorMessage(value, "Calendar"));
			}
			else
			{
				return null;
			}
		}
	}

}
