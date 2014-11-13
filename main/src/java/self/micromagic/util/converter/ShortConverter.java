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

import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.ObjectRef;
import self.micromagic.util.StringRef;
import self.micromagic.util.container.RequestParameterMap;

public class ShortConverter extends ObjectConverter
{
	private static Short DEFAULT_VALUE = new Short((short) 0);

	public int getConvertType(StringRef typeName)
	{
		if (typeName != null)
		{
			typeName.setString("short");
		}
		return TypeManager.TYPE_SHORT;
	}

	public short getResult(Object result)
			throws ConfigurationException
	{
		try
		{
			return this.convertToShort(result);
		}
		catch (Exception ex)
		{
			throw getErrorTypeException(result, "short");
		}
	}

	public short convertToShort(Object value)
	{
		if (value == null)
		{
			return 0;
		}
		if (value instanceof Number)
		{
			return ((Number) value).shortValue();
		}
		if (value instanceof String)
		{
			return this.convertToShort((String) value);
		}
		Object tmpObj = this.changeByPropertyEditor(value);
		if (tmpObj instanceof Short)
		{
			return ((Short) tmpObj).shortValue();
		}
		if (value instanceof String[])
		{
			String str = RequestParameterMap.getFirstParam(value);
			return this.convertToShort(str);
		}
		if (value instanceof ObjectRef)
		{
			ObjectRef ref = (ObjectRef) value;
			if (ref.isNumber())
			{
				return (short) ref.longValue();
			}
			else if (ref.isString())
			{
				try
				{
					return Short.parseShort(ref.toString());
				}
				catch (NumberFormatException ex) {}
			}
			else
			{
				return this.convertToShort(ref.getObject());
			}
		}
		throw new ClassCastException(getCastErrorMessage(value, "short"));
	}

	public short convertToShort(String value)
	{
		if (value == null)
		{
			return 0;
		}
		Object tmpObj = this.changeByPropertyEditor(value);
		if (tmpObj instanceof Short)
		{
			return ((Short) tmpObj).shortValue();
		}
		try
		{
			return Short.parseShort(value);
		}
		catch (NumberFormatException ex) {}
		throw new ClassCastException(getCastErrorMessage(value, "short"));
	}

	public Object convert(Object value)
	{
		if (value instanceof Short)
		{
			return value;
		}
		try
		{
			return new Short(this.convertToShort(value));
		}
		catch (Exception ex)
		{
			if (this.needThrow)
			{
				if (ex instanceof RuntimeException)
				{
					throw (RuntimeException) ex;
				}
				throw new ClassCastException(getCastErrorMessage(value, "short"));
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
			return new Short(this.convertToShort(value));
		}
		catch (Exception ex)
		{
			if (this.needThrow)
			{
				if (ex instanceof RuntimeException)
				{
					throw (RuntimeException) ex;
				}
				throw new ClassCastException(getCastErrorMessage(value, "short"));
			}
			else
			{
				return DEFAULT_VALUE;
			}
		}
	}
}