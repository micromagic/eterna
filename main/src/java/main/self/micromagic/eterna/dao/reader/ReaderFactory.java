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

package self.micromagic.eterna.dao.reader;

import java.lang.reflect.Constructor;

import self.micromagic.cg.ClassGenerator;
import self.micromagic.eterna.dao.ResultReader;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.Tool;
import self.micromagic.eterna.share.TypeManager;

/**
 * reader对象的创建工厂.
 */
public class ReaderFactory
{
	private static Class[] readerTypes;
	static
	{
		readerTypes = new Class[TypeManager.TYPES_COUNT];
		readerTypes[TypeManager.TYPE_OBJECT] = ObjectReader.class;
		readerTypes[TypeManager.TYPE_STRING] = StringReader.class;
		readerTypes[TypeManager.TYPE_BOOLEAN] = BooleanReader.class;
		readerTypes[TypeManager.TYPE_BYTE] = ByteReader.class;
		readerTypes[TypeManager.TYPE_SHORT] = ShortReader.class;
		readerTypes[TypeManager.TYPE_INTEGER] = IntegerReader.class;
		readerTypes[TypeManager.TYPE_LONG] = LongReader.class;
		readerTypes[TypeManager.TYPE_FLOAT] = FloatReader.class;
		readerTypes[TypeManager.TYPE_DOUBLE] = DoubleReader.class;
		readerTypes[TypeManager.TYPE_BYTES] = BytesReader.class;
		readerTypes[TypeManager.TYPE_DATE] = DateReader.class;
		readerTypes[TypeManager.TYPE_TIME] = TimeReader.class;
		readerTypes[TypeManager.TYPE_TIMPSTAMP] = TimestampReader.class;
		readerTypes[TypeManager.TYPE_BIGSTRING] = BigStringReader.class;
		readerTypes[TypeManager.TYPE_STREAM] = StreamReader.class;
		readerTypes[TypeManager.TYPE_CHARS] = CharsReader.class;
		readerTypes[TypeManager.TYPE_BLOB] = BlobReader.class;
		readerTypes[TypeManager.TYPE_CLOB] = ClobReader.class;
		readerTypes[TypeManager.TYPE_NULL] = NullReader.class;
	}

	public static ResultReader createReader(String type, String name)
			throws EternaException
	{
		int pType = TypeManager.getPureType(TypeManager.getTypeId(type));
		Class c = readerTypes[pType];
		if (c == null)
		{
			throw new EternaException("Can't create ResultReader type [" + type + "].");
		}
		return ReaderFactory.createReader(c, name);
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