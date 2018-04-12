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

import java.text.NumberFormat;

import self.micromagic.cg.ClassGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.FormatTool;
import self.micromagic.util.StringTool;
import self.micromagic.util.container.RequestParameterMap;
import self.micromagic.util.ref.ObjectRef;
import self.micromagic.util.ref.StringRef;

public class LongConverter extends AbstractNumericalConverter
{
	private static Long DEFAULT_VALUE = new Long(0L);

	private NumberFormat numberFormat;

	public void setNumberFormat(NumberFormat numberFormat)
	{
		this.numberFormat = numberFormat;
	}

	public int getConvertType(StringRef typeName)
	{
		if (typeName != null)
		{
			typeName.setString("long");
		}
		return TypeManager.TYPE_LONG;
	}

	public long getResult(Object result)
			throws EternaException
	{
		try
		{
			return this.convertToLong(result);
		}
		catch (Exception ex)
		{
			throw getErrorTypeException(result, "long");
		}
	}

	public long convertToLong(Object value)
	{
		return this.convertToLong(value, this.numberFormat);
	}

	public long convertToLong(Object value, NumberFormat format)
	{
		if (value == null)
		{
			return 0;
		}
		if (value instanceof Number)
		{
			return ((Number) value).longValue();
		}
		if (value instanceof String)
		{
			return this.convertToLong((String) value, format);
		}
		if (value instanceof java.util.Date)
		{
			return ((java.util.Date) value).getTime();
		}
		Object tmpObj = this.changeByPropertyEditor(value);
		if (tmpObj instanceof Long)
		{
			return ((Long) tmpObj).longValue();
		}
		if (ClassGenerator.isArray(value.getClass()))
		{
			String str = RequestParameterMap.getFirstParam(value);
			return this.convertToLong(str, format);
		}
		if (value instanceof ObjectRef)
		{
			ObjectRef ref = (ObjectRef) value;
			if (ref.isNumber())
			{
				return ref.longValue();
			}
			else if (ref.isString())
			{
				return this.convertToLong(ref.toString(), format);
			}
			else
			{
				return this.convertToLong(ref.getObject(), format);
			}
		}
		throw new ClassCastException(getCastErrorMessage(value, "long"));
	}

	public long convertToLong(String value)
	{
		return this.convertToLong(value, this.numberFormat);
	}

	public long convertToLong(String value, NumberFormat format)
	{
		if (value == null)
		{
			return 0;
		}
		try
		{
			if (format == null)
			{
				Object tmpObj = this.changeByPropertyEditor(value);
				if (tmpObj instanceof Long)
				{
					return ((Long) tmpObj).longValue();
				}
				return Long.parseLong(value);
			}
			else
			{
				return FormatTool.getThreadFormat(format).parse(value).longValue();
			}
		}
		catch (Exception ex) {}
		throw new ClassCastException(getCastErrorMessage(value, "long"));
	}

	public Object convert(Object value)
	{
		if (value instanceof Long)
		{
			return value;
		}
		if (this.emptyToNull && StringTool.isEmpty(value))
		{
			return null;
		}
		try
		{
			return new Long(this.convertToLong(value));
		}
		catch (Exception ex)
		{
			if (this.needThrow)
			{
				if (ex instanceof RuntimeException)
				{
					throw (RuntimeException) ex;
				}
				throw new ClassCastException(getCastErrorMessage(value, "long"));
			}
			else
			{
				return DEFAULT_VALUE;
			}
		}
	}

	public Object convert(String value)
	{
		if (this.emptyToNull && StringTool.isEmpty(value))
		{
			return null;
		}
		try
		{
			return new Long(this.convertToLong(value));
		}
		catch (Exception ex)
		{
			if (this.needThrow)
			{
				if (ex instanceof RuntimeException)
				{
					throw (RuntimeException) ex;
				}
				throw new ClassCastException(getCastErrorMessage(value, "long"));
			}
			else
			{
				return DEFAULT_VALUE;
			}
		}
	}

}
