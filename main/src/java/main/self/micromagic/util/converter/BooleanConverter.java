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

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.container.RequestParameterMap;
import self.micromagic.util.ref.ObjectRef;
import self.micromagic.util.ref.StringRef;

public class BooleanConverter extends ObjectConverter
{
	/**
	 * 将一个对象转换成boolean.
	 */
	public static boolean toBoolean(Object obj)
	{
		return instance.convertToBoolean(obj);
	}
	private static final BooleanConverter instance = new BooleanConverter();

	private String[] trueValues;

	public void setTrueValues(String[] trueValues)
	{
		this.trueValues = trueValues;
	}

	public int getConvertType(StringRef typeName)
	{
		if (typeName != null)
		{
			typeName.setString("boolean");
		}
		return TypeManager.TYPE_BOOLEAN;
	}

	public boolean getResult(Object result)
			throws EternaException
	{
		try
		{
			return this.convertToBoolean(result);
		}
		catch (Exception ex)
		{
			throw getErrorTypeException(result, "boolean");
		}
	}

	public boolean convertToBoolean(Object value)
	{
		return this.convertToBoolean(value, this.trueValues);
	}

	public boolean convertToBoolean(Object value, String[] trueValues)
	{
		if (value == null)
		{
			return false;
		}
		if (value instanceof Boolean)
		{
			return ((Boolean) value).booleanValue();
		}
		if (value instanceof Number)
		{
			return ((Number) value).intValue() != 0;
		}
		if (value instanceof String)
		{
			return this.convertToBoolean((String) value, trueValues);
		}
		Object tmpObj = this.changeByPropertyEditor(value);
		if (tmpObj instanceof Boolean)
		{
			return ((Boolean) tmpObj).booleanValue();
		}
		if (value instanceof String[])
		{
			String str = RequestParameterMap.getFirstParam(value);
			return this.convertToBoolean(str, trueValues);
		}
		if (value instanceof ObjectRef)
		{
			ObjectRef ref = (ObjectRef) value;
			if (ref.isBoolean())
			{
				return ref.booleanValue();
			}
			else if (ref.isNumber())
			{
				return ref.intValue() != 0;
			}
			else if (ref.isString())
			{
				return this.convertToBoolean(ref.toString(), trueValues);
			}
			else
			{
				return this.convertToBoolean(ref.getObject(), trueValues);
			}
		}
		throw new ClassCastException(getCastErrorMessage(value, "boolean"));
	}

	public boolean convertToBoolean(String value)
	{
		return this.convertToBoolean(value, this.trueValues);
	}

	public boolean convertToBoolean(String value, String[] trueValues)
	{
		if (trueValues == null)
		{
			Object tmpObj = this.changeByPropertyEditor(value);
			if (tmpObj instanceof Boolean)
			{
				return ((Boolean) tmpObj).booleanValue();
			}
			return "true".equalsIgnoreCase(value) || "1".equals(value);
		}
		for (int i = 0; i < trueValues.length; i++)
		{
			if (trueValues[i].equalsIgnoreCase(value))
			{
				return true;
			}
		}
		return false;
	}

	public Object convert(Object value)
	{
		if (value instanceof Boolean)
		{
			return value;
		}
		try
		{
			return this.convertToBoolean(value) ? Boolean.TRUE : Boolean.FALSE;
		}
		catch (Exception ex)
		{
			if (this.needThrow)
			{
				if (ex instanceof RuntimeException)
				{
					throw (RuntimeException) ex;
				}
				throw new ClassCastException(getCastErrorMessage(value, "boolean"));
			}
			else
			{
				return Boolean.FALSE;
			}
		}
	}

	public Object convert(String value)
	{
		return this.convertToBoolean(value) ? Boolean.TRUE : Boolean.FALSE;
	}

}