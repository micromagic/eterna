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

package self.micromagic.eterna.dao.reader;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import self.micromagic.cg.ClassGenerator;
import self.micromagic.eterna.dao.ResultReader;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.Tool;

/**
 * @author micromagic@sina.com
 */
public class ReaderManager
{
	private static Map typeClassMap = new HashMap();
	static
	{
		typeClassMap.put("Object", ObjectReader.class);
		typeClassMap.put("String", StringReader.class);
		typeClassMap.put("boolean", BooleanReader.class);
		typeClassMap.put("byte", ByteReader.class);
		typeClassMap.put("short", ShortReader.class);
		typeClassMap.put("int", IntegerReader.class);
		typeClassMap.put("long", LongReader.class);
		typeClassMap.put("float", FloatReader.class);
		typeClassMap.put("double", DoubleReader.class);
		typeClassMap.put("Bytes", BytesReader.class);
		typeClassMap.put("Date", DateReader.class);
		typeClassMap.put("Time", TimeReader.class);
		typeClassMap.put("Datetime", TimestampReader.class);
		typeClassMap.put("Timestamp", TimestampReader.class);
		typeClassMap.put("BigString", BigStringReader.class);
		typeClassMap.put("Stream", StreamReader.class);
		typeClassMap.put("Chars", CharsReader.class);
		typeClassMap.put("Blob", BlobReader.class);
		typeClassMap.put("Clob", ClobReader.class);
		typeClassMap.put("null", NullReader.class);
	}

	public static ResultReader createReader(String type, String name)
			throws EternaException
	{
		Class c = (Class) ReaderManager.typeClassMap.get(type);
		if (c == null)
		{
			throw new EternaException("Can't create ResultReader type [" + type + "].");
		}
		return ReaderManager.createReader(c, name);
	}

	public static ResultReader createReader(Class type, String name)
			throws EternaException
	{
		if (type == null)
		{
			throw new NullPointerException();
		}
		if (!ResultReader.class.isAssignableFrom(type))
		{
			throw new EternaException(ClassGenerator.getClassName(type)
					+ " is not instance of " + ClassGenerator.getClassName(ResultReader.class));
		}
		try
		{
			Constructor ct = type.getConstructor(new Class[]{String.class});
			return (ResultReader) ct.newInstance(new Object[]{name});
		}
		catch (Exception ex)
		{
			Tool.log.warn("createReader [" + name + "]", ex);
			throw new EternaException("Can't create ResultReader class ["
					+ ClassGenerator.getClassName(type) + "].");
		}
	}

}