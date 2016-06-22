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

import java.io.Reader;
import java.io.StringReader;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.MemoryChars;
import self.micromagic.util.StringTool;
import self.micromagic.util.ref.ObjectRef;
import self.micromagic.util.ref.StringRef;

public class ReaderConverter extends ObjectConverter
{
	public int getConvertType(StringRef typeName)
	{
		if (typeName != null)
		{
			typeName.setString("Reader");
		}
		return TypeManager.TYPE_CHARS;
	}

	public Reader getResult(Object result)
			throws EternaException
	{
		try
		{
			return this.convertToReader(result);
		}
		catch (Exception ex)
		{
			throw getErrorTypeException(result, "Reader");
		}
	}

	public Reader convertToReader(Object value)
	{
		if (value == null)
		{
			return null;
		}
		if (value instanceof Reader)
		{
			return (Reader) value;
		}
		if (value instanceof MemoryChars)
		{
			return ((MemoryChars) value).getReader();
		}
		if (value instanceof char[])
		{
			return this.convertToReader(new String((char[]) value));
		}
		if (value instanceof String)
		{
			return this.convertToReader((String) value);
		}
		Object tmpObj = this.changeByPropertyEditor(value);
		if (tmpObj instanceof Reader)
		{
			return (Reader) tmpObj;
		}
		if (value instanceof String[])
		{
			String str = StringTool.linkStringArr((String[]) value, ",");
			return this.convertToReader(str);
		}
		if (value instanceof ObjectRef)
		{
			return this.convertToReader(((ObjectRef) value).getObject());
		}
		throw new ClassCastException(getCastErrorMessage(value, "Reader"));
	}

	public Reader convertToReader(String value)
	{
		if (value == null)
		{
			return null;
		}
		return new StringReader(value);
	}

	public Object convert(Object value)
	{
		if (value instanceof Reader)
		{
			return value;
		}
		try
		{
			return this.convertToReader(value);
		}
		catch (Exception ex)
		{
			if (this.needThrow)
			{
				if (ex instanceof RuntimeException)
				{
					throw (RuntimeException) ex;
				}
				throw new ClassCastException(getCastErrorMessage(value, "Reader"));
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
			return this.convertToReader(value);
		}
		catch (Exception ex)
		{
			if (this.needThrow)
			{
				if (ex instanceof RuntimeException)
				{
					throw (RuntimeException) ex;
				}
				throw new ClassCastException(getCastErrorMessage(value, "Reader"));
			}
			else
			{
				return null;
			}
		}
	}

}
