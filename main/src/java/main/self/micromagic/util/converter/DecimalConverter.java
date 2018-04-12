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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;

import self.micromagic.cg.ClassGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.FormatTool;
import self.micromagic.util.container.RequestParameterMap;
import self.micromagic.util.ref.ObjectRef;
import self.micromagic.util.ref.StringRef;

public class DecimalConverter extends AbstractNumericalConverter
{
	private NumberFormat numberFormat;

	public void setNumberFormat(NumberFormat numberFormat)
	{
		this.numberFormat = numberFormat;
	}

	public int getConvertType(StringRef typeName)
	{
		if (typeName != null)
		{
			typeName.setString("Decimal");
		}
		return TypeManager.TYPE_DECIMAL;
	}

	public BigDecimal getResult(Object result)
			throws EternaException
	{
		try
		{
			return this.convertToDecimal(result);
		}
		catch (Exception ex)
		{
			throw getErrorTypeException(result, "Decimal");
		}
	}

	public BigDecimal convertToDecimal(Object value)
	{
		return this.convertToDecimal(value, this.numberFormat);
	}

	public BigDecimal convertToDecimal(Object value, NumberFormat format)
	{
		if (value == null || value instanceof BigDecimal)
		{
			return (BigDecimal) value;
		}
		if (value instanceof BigInteger)
		{
			return new BigDecimal((BigInteger) value);
		}
		if (value instanceof Number)
		{
			return new BigDecimal(((Number) value).doubleValue());
		}
		if (value instanceof String)
		{
			return this.convertToDecimal((String) value, format);
		}
		Object tmpObj = this.changeByPropertyEditor(value);
		if (tmpObj instanceof BigDecimal)
		{
			return (BigDecimal) tmpObj;
		}
		if (ClassGenerator.isArray(value.getClass()))
		{
			String str = RequestParameterMap.getFirstParam(value);
			return this.convertToDecimal(str, format);
		}
		if (value instanceof ObjectRef)
		{
			ObjectRef ref = (ObjectRef) value;
			if (ref.isNumber())
			{
				return new BigDecimal(ref.doubleValue());
			}
			else if (ref.isString())
			{
				return this.convertToDecimal(ref.toString(), format);
			}
			else
			{
				return this.convertToDecimal(ref.getObject(), format);
			}
		}
		throw new ClassCastException(getCastErrorMessage(value, "Decimal"));
	}

	public BigDecimal convertToDecimal(String value)
	{
		return this.convertToDecimal(value, this.numberFormat);
	}

	public BigDecimal convertToDecimal(String value, NumberFormat format)
	{
		if (value == null || (this.emptyToNull && value.length() == 0))
		{
			return null;
		}
		try
		{
			if (format == null)
			{
				Object tmpObj = this.changeByPropertyEditor(value);
				if (tmpObj instanceof BigDecimal)
				{
					return (BigDecimal) tmpObj;
				}
				return new BigDecimal(value);
			}
			else
			{
				return this.convertToDecimal(FormatTool.getThreadFormat(format).parse(value));
			}
		}
		catch (Exception ex) {}
		throw new ClassCastException(getCastErrorMessage(value, "Decimal"));
	}

	public Object convert(Object value)
	{
		if (value instanceof BigDecimal)
		{
			return value;
		}
		try
		{
			return this.convertToDecimal(value);
		}
		catch (Exception ex)
		{
			if (this.needThrow)
			{
				if (ex instanceof RuntimeException)
				{
					throw (RuntimeException) ex;
				}
				throw new ClassCastException(getCastErrorMessage(value, "Decimal"));
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
			return this.convertToDecimal(value);
		}
		catch (Exception ex)
		{
			if (this.needThrow)
			{
				if (ex instanceof RuntimeException)
				{
					throw (RuntimeException) ex;
				}
				throw new ClassCastException(getCastErrorMessage(value, "Decimal"));
			}
			else
			{
				return null;
			}
		}
	}

}
