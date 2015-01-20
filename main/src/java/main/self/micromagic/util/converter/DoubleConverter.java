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

import java.text.NumberFormat;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.FormatTool;
import self.micromagic.util.ObjectRef;
import self.micromagic.util.StringRef;
import self.micromagic.util.container.RequestParameterMap;

public class DoubleConverter extends ObjectConverter
{
	private static Double DEFAULT_VALUE = new Double(0.0);

	private NumberFormat numberFormat;

	public void setNumberFormat(NumberFormat numberFormat)
	{
		this.numberFormat = numberFormat;
	}

	public int getConvertType(StringRef typeName)
	{
		if (typeName != null)
		{
			typeName.setString("double");
		}
		return TypeManager.TYPE_DOUBLE;
	}

	public double getResult(Object result)
			throws EternaException
	{
		try
		{
			return this.convertToDouble(result);
		}
		catch (Exception ex)
		{
			throw getErrorTypeException(result, "double");
		}
	}

	public double convertToDouble(Object value)
	{
		return this.convertToDouble(value, this.numberFormat);
	}

	public double convertToDouble(Object value, NumberFormat format)
	{
		if (value == null)
		{
			return 0;
		}
		if (value instanceof Number)
		{
			return ((Number) value).doubleValue();
		}
		if (value instanceof String)
		{
			return this.convertToDouble((String) value, format);
		}
		Object tmpObj = this.changeByPropertyEditor(value);
		if (tmpObj instanceof Double)
		{
			return ((Double) tmpObj).doubleValue();
		}
		if (value instanceof String[])
		{
			String str = RequestParameterMap.getFirstParam(value);
			return this.convertToDouble(str, format);
		}
		if (value instanceof ObjectRef)
		{
			ObjectRef ref = (ObjectRef) value;
			if (ref.isNumber())
			{
				return ref.doubleValue();
			}
			else if (ref.isString())
			{
				return this.convertToDouble(ref.toString(), format);
			}
			else
			{
				return this.convertToDouble(ref.getObject(), format);
			}
		}
		throw new ClassCastException(getCastErrorMessage(value, "double"));
	}

	public double convertToDouble(String value)
	{
		return this.convertToDouble(value, this.numberFormat);
	}

	public double convertToDouble(String value, NumberFormat format)
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
				if (tmpObj instanceof Double)
				{
					return ((Double) tmpObj).doubleValue();
				}
				return Double.parseDouble(value);
			}
			else
			{
				return FormatTool.getThreadFormat(format).parse(value).doubleValue();
			}
		}
		catch (Exception ex) {}
		throw new ClassCastException(getCastErrorMessage(value, "double"));
	}

	public Object convert(Object value)
	{
		if (value instanceof Double)
		{
			return value;
		}
		try
		{
			return new Double(this.convertToDouble(value));
		}
		catch (Exception ex)
		{
			if (this.needThrow)
			{
				if (ex instanceof RuntimeException)
				{
					throw (RuntimeException) ex;
				}
				throw new ClassCastException(getCastErrorMessage(value, "double"));
			}
			else
			{
				return DEFAULT_VALUE;
			}
		}
	}

	public Object convert(String value)
	{
		try
		{
			return new Double(this.convertToDouble(value));
		}
		catch (Exception ex)
		{
			if (this.needThrow)
			{
				if (ex instanceof RuntimeException)
				{
					throw (RuntimeException) ex;
				}
				throw new ClassCastException(getCastErrorMessage(value, "double"));
			}
			else
			{
				return DEFAULT_VALUE;
			}
		}
	}

}