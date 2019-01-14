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

import self.micromagic.cg.ClassGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.FormatTool;
import self.micromagic.util.container.RequestParameterMap;
import self.micromagic.util.ref.ObjectRef;
import self.micromagic.util.ref.StringRef;

public class UtilDateConverter extends AbstractNumericalConverter
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
			typeName.setString("UtilDate");
		}
		return TypeManager.TYPE_TIMPSTAMP;
	}

	public java.util.Date getResult(Object result)
			throws EternaException
	{
		try
		{
			return this.convertToDate(result);
		}
		catch (Exception ex)
		{
			throw getErrorTypeException(result, "UtilDate");
		}
	}

	public java.util.Date convertToDate(Object value)
	{
		return this.convertToDate(value, this.dateFormats);
	}

	public java.util.Date convertToDate(Object value, DateFormat format)
	{
		return this.convertToDate(value, new DateFormat[]{format});
	}

	public java.util.Date convertToDate(Object value, DateFormat[] formats)
	{
		if (this.isNull(value))
		{
			return null;
		}
		if (value instanceof java.util.Date)
		{
			return (java.util.Date) value;
		}
		if (value instanceof Number)
		{
			return new java.util.Date(((Number) value).longValue());
		}
		if (value instanceof String)
		{
			return this.convertToDate((String) value, formats);
		}
		Object tmpObj = this.changeByPropertyEditor(value);
		if (tmpObj instanceof java.util.Date)
		{
			return (java.util.Date) tmpObj;
		}
		if (ClassGenerator.isArray(value.getClass()))
		{
			String str = RequestParameterMap.getFirstParam(value);
			return this.convertToDate(str, formats);
		}
		if (value instanceof ObjectRef)
		{
			return this.convertToDate(((ObjectRef) value).getObject(), formats);
		}
		throw new ClassCastException(getCastErrorMessage(value, "UtilDate"));
	}

	public java.util.Date convertToDate(String value)
	{
		return this.convertToDate(value, this.dateFormats);
	}

	public java.util.Date convertToDate(String value, DateFormat format)
	{
		return this.convertToDate(value, new DateFormat[]{format});
	}

	public java.util.Date convertToDate(String value, DateFormat[] formats)
	{
		if (this.isNull(value))
		{
			return null;
		}
		try
		{
			if (formats == null)
			{
				Object tmpObj = this.changeByPropertyEditor(value);
				if (tmpObj instanceof java.util.Date)
				{
					return (java.util.Date) tmpObj;
				}
				try
				{
					return FormatTool.parseDatetime(value);
				}
				catch (ParseException ex)
				{
					return FormatTool.parseDate(value);
				}
			}
			else
			{
				for (int i = 0; i < formats.length; i++)
				{
					try
					{
						return FormatTool.getThreadFormat(formats[i]).parse(value);
					}
					catch (Throwable ex) {}
				}
			}
		}
		catch (ParseException ex) {}
		throw new ClassCastException(getCastErrorMessage(value, "UtilDate"));
	}

	public Object convert(Object value)
	{
		if (value instanceof java.util.Date)
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
				throw new ClassCastException(getCastErrorMessage(value, "UtilDate"));
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
				throw new ClassCastException(getCastErrorMessage(value, "UtilDate"));
			}
			else
			{
				return null;
			}
		}
	}

}
