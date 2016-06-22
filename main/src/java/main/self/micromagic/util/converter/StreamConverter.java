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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.fileupload.FileItem;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.MemoryStream;
import self.micromagic.util.StringTool;
import self.micromagic.util.ref.ObjectRef;
import self.micromagic.util.ref.StringRef;

public class StreamConverter extends ObjectConverter
{
	public int getConvertType(StringRef typeName)
	{
		if (typeName != null)
		{
			typeName.setString("Stream");
		}
		return TypeManager.TYPE_STREAM;
	}

	public InputStream getResult(Object result)
			throws EternaException
	{
		try
		{
			return this.convertToStream(result);
		}
		catch (Exception ex)
		{
			throw getErrorTypeException(result, "Stream");
		}
	}

	public InputStream convertToStream(Object value)
	{
		return this.convertToStream(value, null);
	}

	public InputStream convertToStream(Object value, String charset)
	{
		if (value == null)
		{
			return null;
		}
		if (value instanceof InputStream)
		{
			return (InputStream) value;
		}
		if (value instanceof MemoryStream)
		{
			return ((MemoryStream) value).getInputStream();
		}
		if (value instanceof FileItem)
		{
			try
			{
				return ((FileItem) value).getInputStream();
			}
			catch (IOException ex)
			{
				throw new RuntimeException(ex);
			}
		}
		if (value instanceof byte[])
		{
			return new ByteArrayInputStream((byte[]) value);
		}
		if (value instanceof String)
		{
			return this.convertToStream((String) value, charset);
		}
		Object tmpObj = this.changeByPropertyEditor(value);
		if (tmpObj instanceof InputStream)
		{
			return (InputStream) tmpObj;
		}
		if (value instanceof String[])
		{
			String str = StringTool.linkStringArr((String[]) value, ",");
			return this.convertToStream(str, charset);
		}
		if (value instanceof ObjectRef)
		{
			return this.convertToStream(((ObjectRef) value).getObject(), charset);
		}
		throw new ClassCastException(getCastErrorMessage(value, "Stream"));
	}

	public InputStream convertToStream(String value)
	{
		return this.convertToStream(value, null);
	}

	public InputStream convertToStream(String value, String charset)
	{
		if (value == null)
		{
			return null;
		}
		try
		{
			if (charset == null)
			{
				Object tmpObj = this.changeByPropertyEditor(value);
				if (tmpObj instanceof InputStream)
				{
					return (InputStream) tmpObj;
				}
				return new ByteArrayInputStream(value.getBytes("8859_1"));
			}
			else
			{
				return new ByteArrayInputStream(value.getBytes(charset));
			}
		}
		catch (UnsupportedEncodingException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	public Object convert(Object value)
	{
		if (value instanceof InputStream)
		{
			return value;
		}
		try
		{
			return this.convertToStream(value);
		}
		catch (Exception ex)
		{
			if (this.needThrow)
			{
				if (ex instanceof RuntimeException)
				{
					throw (RuntimeException) ex;
				}
				throw new ClassCastException(getCastErrorMessage(value, "Stream"));
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
			return this.convertToStream(value);
		}
		catch (Exception ex)
		{
			if (this.needThrow)
			{
				if (ex instanceof RuntimeException)
				{
					throw (RuntimeException) ex;
				}
				throw new ClassCastException(getCastErrorMessage(value, "Stream"));
			}
			else
			{
				return null;
			}
		}
	}

}
