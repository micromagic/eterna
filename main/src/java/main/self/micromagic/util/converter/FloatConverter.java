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

import self.micromagic.eterna.share.TypeManager;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.util.container.RequestParameterMap;
import self.micromagic.util.ref.ObjectRef;
import self.micromagic.util.ref.StringRef;

public class FloatConverter extends ObjectConverter
{
	private static Float DEFAULT_VALUE = new Float(0.0F);

	public int getConvertType(StringRef typeName)
	{
		if (typeName != null)
		{
			typeName.setString("float");
		}
		return TypeManager.TYPE_FLOAT;
	}

	public float getResult(Object result)
			throws EternaException
	{
		try
		{
			return this.convertToFloat(result);
		}
		catch (Exception ex)
		{
			throw getErrorTypeException(result, "float");
		}
	}

	public float convertToFloat(Object value)
	{
		if (value == null)
		{
			return 0;
		}
		if (value instanceof Number)
		{
			return ((Number) value).floatValue();
		}
		if (value instanceof String)
		{
			return this.convertToFloat((String) value);
		}
		Object tmpObj = this.changeByPropertyEditor(value);
		if (tmpObj instanceof Float)
		{
			return ((Float) tmpObj).floatValue();
		}
		if (value instanceof String[])
		{
			String str = RequestParameterMap.getFirstParam(value);
			return this.convertToFloat(str);
		}
		if (value instanceof ObjectRef)
		{
			ObjectRef ref = (ObjectRef) value;
			if (ref.isNumber())
			{
				return (float) ref.doubleValue();
			}
			else if (ref.isString())
			{
				try
				{
					return Float.parseFloat(ref.toString());
				}
				catch (NumberFormatException ex) {}
			}
			else
			{
				return this.convertToFloat(ref.getObject());
			}
		}
		throw new ClassCastException(getCastErrorMessage(value, "float"));
	}

	public float convertToFloat(String value)
	{
		if (value == null)
		{
			return 0;
		}
		Object tmpObj = this.changeByPropertyEditor(value);
		if (tmpObj instanceof Float)
		{
			return ((Float) tmpObj).floatValue();
		}
		try
		{
			return Float.parseFloat(value);
		}
		catch (NumberFormatException ex) {}
		throw new ClassCastException(getCastErrorMessage(value, "float"));
	}

	public Object convert(Object value)
	{
		if (value instanceof Float)
		{
			return value;
		}
		try
		{
			return new Float(this.convertToFloat(value));
		}
		catch (Exception ex)
		{
			if (this.needThrow)
			{
				if (ex instanceof RuntimeException)
				{
					throw (RuntimeException) ex;
				}
				throw new ClassCastException(getCastErrorMessage(value, "float"));
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
			return new Float(this.convertToFloat(value));
		}
		catch (Exception ex)
		{
			if (this.needThrow)
			{
				if (ex instanceof RuntimeException)
				{
					throw (RuntimeException) ex;
				}
				throw new ClassCastException(getCastErrorMessage(value, "float"));
			}
			else
			{
				return DEFAULT_VALUE;
			}
		}
	}

}