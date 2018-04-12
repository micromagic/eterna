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

import java.util.Locale;

import self.micromagic.cg.ClassGenerator;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.StringTool;
import self.micromagic.util.container.RequestParameterMap;
import self.micromagic.util.ref.ObjectRef;
import self.micromagic.util.ref.StringRef;

public class LocaleConverter extends ObjectConverter
{
	public int getConvertType(StringRef typeName)
	{
		if (typeName != null)
		{
			typeName.setString("Locale");
		}
		return TypeManager.TYPE_OBJECT;
	}

	public Locale convertToLocale(Object value)
	{
		if (value == null || value instanceof Locale)
		{
			return (Locale) value;
		}
		if (value instanceof String)
		{
			return this.convertToLocale((String) value);
		}
		Object tmpObj = this.changeByPropertyEditor(value);
		if (tmpObj instanceof Locale)
		{
			return (Locale) tmpObj;
		}
		if (ClassGenerator.isArray(value.getClass()))
		{
			String str = RequestParameterMap.getFirstParam(value);
			return this.convertToLocale(str);
		}
		if (value instanceof ObjectRef)
		{
			return this.convertToLocale(((ObjectRef) value).getObject());
		}
		throw new ClassCastException(getCastErrorMessage(value, "Locale"));
	}

	public Locale convertToLocale(String value)
	{
		if (StringTool.isEmpty(value))
		{
			return null;
		}
		Object tmpObj = this.changeByPropertyEditor(value);
		if (tmpObj instanceof Locale)
		{
			return (Locale) tmpObj;
		}
		int idx = value.indexOf('_');
		if (idx == -1)
		{
			return new Locale(value);
		}
		else
		{
			int idx2 = value.indexOf('_', idx + 1);
			if (idx2 == -1)
			{
				String language = value.substring(0, idx);
				String country = value.substring(idx + 1);
				return new Locale(language, country);
			}
			else
			{
				String language = value.substring(0, idx);
				String country = value.substring(idx + 1, idx2);
				String variant = value.substring(idx2 + 1);
				return new Locale(language, country, variant);
			}
		}
	}

	public Object convert(Object value)
	{
		if (value instanceof Locale)
		{
			return value;
		}
		try
		{
			return this.convertToLocale(value);
		}
		catch (Exception ex)
		{
			if (this.needThrow)
			{
				if (ex instanceof RuntimeException)
				{
					throw (RuntimeException) ex;
				}
				throw new ClassCastException(getCastErrorMessage(value, "Locale"));
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
			return this.convertToLocale(value);
		}
		catch (Exception ex)
		{
			if (this.needThrow)
			{
				if (ex instanceof RuntimeException)
				{
					throw (RuntimeException) ex;
				}
				throw new ClassCastException(getCastErrorMessage(value, "Locale"));
			}
			else
			{
				return null;
			}
		}
	}

}