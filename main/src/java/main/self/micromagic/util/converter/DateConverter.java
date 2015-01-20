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

package self.micromagic.util.converter;

import java.text.DateFormat;
import java.text.ParseException;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.FormatTool;
import self.micromagic.util.ObjectRef;
import self.micromagic.util.StringRef;
import self.micromagic.util.container.RequestParameterMap;

public class DateConverter extends ObjectConverter
{
	private DateFormat dateFormat;

	public void setDateFormat(DateFormat dateFormat)
	{
		this.dateFormat = dateFormat;
	}

	public int getConvertType(StringRef typeName)
	{
		if (typeName != null)
		{
			typeName.setString("Date");
		}
		return TypeManager.TYPE_DATE;
	}

	public java.sql.Date getResult(Object result)
			throws EternaException
	{
		try
		{
			return this.convertToDate(result);
		}
		catch (Exception ex)
		{
			throw getErrorTypeException(result, "Date");
		}
	}

	public java.sql.Date convertToDate(Object value)
	{
		return this.convertToDate(value, this.dateFormat);
	}

	public java.sql.Date convertToDate(Object value, DateFormat format)
	{
		if (value == null)
		{
			return null;
		}
		if (value instanceof java.sql.Date)
		{
			return (java.sql.Date) value;
		}
		if (value instanceof java.util.Date)
		{
			return new java.sql.Date(((java.util.Date) value).getTime());
		}
		if (value instanceof Number)
		{
			return new java.sql.Date(((Number) value).longValue());
		}
		if (value instanceof String)
		{
			return this.convertToDate((String) value, format);
		}
		Object tmpObj = this.changeByPropertyEditor(value);
		if (tmpObj instanceof java.sql.Date)
		{
			return (java.sql.Date) tmpObj;
		}
		if (value instanceof String[])
		{
			String str = RequestParameterMap.getFirstParam(value);
			return this.convertToDate(str, format);
		}
		if (value instanceof ObjectRef)
		{
			return this.convertToDate(((ObjectRef) value).getObject(), format);
		}
		throw new ClassCastException(getCastErrorMessage(value, "Date"));
	}

	public java.sql.Date convertToDate(String value)
	{
		return this.convertToDate(value, this.dateFormat);
	}

	public java.sql.Date convertToDate(String value, DateFormat format)
	{
		if (value == null)
		{
			return null;
		}
		try
		{
			if (format == null)
			{
				Object tmpObj = this.changeByPropertyEditor(value);
				if (tmpObj instanceof java.sql.Date)
				{
					return (java.sql.Date) tmpObj;
				}
				return new java.sql.Date(FormatTool.parseDate(value).getTime());
			}
			else
			{
				return new java.sql.Date(FormatTool.getThreadFormat(format).parse(value).getTime());
			}
		}
		catch (ParseException ex) {}
		throw new ClassCastException(getCastErrorMessage(value, "Date"));
	}

	public Object convert(Object value)
	{
		if (value instanceof java.sql.Date)
		{
			return value;
		}
		try
		{
			return this.convertToDate(value);
		}
		catch (Exception ex)
		{
			if (this.needThrow)
			{
				if (ex instanceof RuntimeException)
				{
					throw (RuntimeException) ex;
				}
				throw new ClassCastException(getCastErrorMessage(value, "Date"));
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
			return this.convertToDate(value);
		}
		catch (Exception ex)
		{
			if (this.needThrow)
			{
				if (ex instanceof RuntimeException)
				{
					throw (RuntimeException) ex;
				}
				throw new ClassCastException(getCastErrorMessage(value, "Date"));
			}
			else
			{
				return null;
			}
		}
	}

}