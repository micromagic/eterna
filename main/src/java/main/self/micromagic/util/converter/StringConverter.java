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

import java.io.IOException;
import java.io.Reader;

import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.MemoryChars;
import self.micromagic.util.StringTool;
import self.micromagic.util.ref.ObjectRef;
import self.micromagic.util.ref.StringRef;

public class StringConverter extends ObjectConverter
{
	public static int MAX_BUFFER = 10240;

	public int getConvertType(StringRef typeName)
	{
		if (typeName != null)
		{
			typeName.setString("String");
		}
		return TypeManager.TYPE_STRING;
	}

	public Object convert(Object value)
	{
		if (value == null || value instanceof String)
		{
			return value;
		}
		if (value instanceof String[])
		{
			return StringTool.linkStringArr((String[]) value, ",");
		}
		if (value instanceof ObjectRef)
		{
			return this.convert(((ObjectRef) value).getObject());
		}
		if (value != null && value instanceof MemoryChars)
		{
			MemoryChars mc = ((MemoryChars) value);
			if (mc.getUsedSize() < MAX_BUFFER)
			{
				Reader reader = mc.getReader();
				char[] buf = new char[(int) mc.getUsedSize()];
				try
				{
					reader.read(buf);
				}
				catch (IOException ex) {}
				return new String(buf);
			}
		}
		return this.convertToString(value);
	}

}
