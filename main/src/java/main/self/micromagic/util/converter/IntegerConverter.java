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
import self.micromagic.util.Utility;
import self.micromagic.util.container.RequestParameterMap;
import self.micromagic.util.ref.ObjectRef;
import self.micromagic.util.ref.StringRef;

public class IntegerConverter extends AbstractNumericalConverter
{
	/**
	 * 将一个对象转换成int.
	 */
	public static int toInt(Object obj)
	{
		return instance.convertToInt(obj);
	}
	private static final IntegerConverter instance = new IntegerConverter();

	private static Integer DEFAULT_VALUE = Utility.INTEGER_0;

	private NumberFormat numberFormat;

	public void setNumberFormat(NumberFormat numberFormat)
	{
		this.numberFormat = numberFormat;
	}

	public int getConvertType(StringRef typeName)
	{
		if (typeName != null)
		{
			typeName.setString("int");
		}
		return TypeManager.TYPE_INTEGER;
	}

	public int getResult(Object result)
			throws EternaException
	{
		try
		{
			return this.convertToInt(result);
		}
		catch (Exception ex)
		{
			throw getErrorTypeException(result, "int");
		}
	}

	public int convertToInt(Object value)
	{
		return this.convertToInt(value, this.numberFormat);
	}

	public int convertToInt(Object value, NumberFormat format)
	{
		if (this.isNull(value))
		{
			return 0;
		}
		if (value instanceof Number)
		{
			return ((Number) value).intValue();
		}
		if (value instanceof String)
		{
			return this.convertToInt((String) value, format);
		}
		Object tmpObj = this.changeByPropertyEditor(value);
		if (tmpObj instanceof Integer)
		{
			return ((Integer) tmpObj).intValue();
		}
		if (ClassGenerator.isArray(value.getClass()))
		{
			String str = RequestParameterMap.getFirstParam(value);
			return this.convertToInt(str, format);
		}
		if (value instanceof ObjectRef)
		{
			ObjectRef ref = (ObjectRef) value;
			if (ref.isNumber())
			{
				return ref.intValue();
			}
			else if (ref.isString())
			{
				return this.convertToInt(ref.toString(), format);
			}
			else
			{
				return this.convertToInt(ref.getObject(), format);
			}
		}
		throw new ClassCastException(getCastErrorMessage(value, "int"));
	}

	public int convertToInt(String value)
	{
		return this.convertToInt(value, this.numberFormat);
	}

	public int convertToInt(String value, NumberFormat format)
	{
		if (this.isNull(value))
		{
			return 0;
		}
		try
		{
			if (format == null)
			{
				Object tmpObj = this.changeByPropertyEditor(value);
				if (tmpObj instanceof Integer)
				{
					return ((Integer) tmpObj).intValue();
				}
				return Integer.parseInt(value);
			}
			else
			{
				return FormatTool.getThreadFormat(format).parse(value).intValue();
			}
		}
		catch (Exception ex) {}
		throw new ClassCastException(getCastErrorMessage(value, "int"));
	}

	public Object convert(Object value)
	{
		if (this.isNull(value))
		{
			return null;
		}
		if (value instanceof Integer)
		{
			return value;
		}
		try
		{
			return new Integer(this.convertToInt(value));
		}
		catch (Exception ex)
		{
			if (this.needThrow)
			{
				if (ex instanceof RuntimeException)
				{
					throw (RuntimeException) ex;
				}
				throw new ClassCastException(getCastErrorMessage(value, "int"));
			}
			else
			{
				return DEFAULT_VALUE;
			}
		}
	}

	public Object convert(String value)
	{
		if (this.isNull(value))
		{
			return null;
		}
		try
		{
			return new Integer(this.convertToInt(value));
		}
		catch (Exception ex)
		{
			if (this.needThrow)
			{
				if (ex instanceof RuntimeException)
				{
					throw (RuntimeException) ex;
				}
				throw new ClassCastException(getCastErrorMessage(value, "int"));
			}
			else
			{
				return DEFAULT_VALUE;
			}
		}
	}

}
