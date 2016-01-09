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

package self.micromagic.eterna.digester2;

import self.micromagic.util.converter.ObjectConverter;

public class MyAttrConverter extends ObjectConverter
{
	public Object convert(Object value)
	{
		if (value instanceof MyType)
		{
			return value;
		}
		try
		{
			return this.convert((String) value);
		}
		catch (RuntimeException ex)
		{
			System.out.println(ex);
			throw ex;
		}
	}

	public Object convert(String value)
	{
		return new MyType(value);
	}

}

class MyType
{
	public String table;
	public String type;

	public MyType(String config)
	{
		int index = config.indexOf('/');
		if (index == -1)
		{
			this.table = config;
		}
		else
		{
			this.table = config.substring(0, index);
			this.type = config.substring(index + 1);
		}
	}

}