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

import java.beans.PropertyEditor;

import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.StringRef;
import self.micromagic.util.ObjectRef;

public class ObjectConverter
		implements ValueConverter, Cloneable
{
	protected boolean needThrow = false;
	protected PropertyEditor propertyEditor;

	public PropertyEditor getPropertyEditor()
	{
		return this.propertyEditor;
	}

	public void setPropertyEditor(PropertyEditor propertyEditor)
	{
		this.propertyEditor = propertyEditor;
	}

	public boolean isNeedThrow()
	{
		return this.needThrow;
	}

	public void setNeedThrow(boolean need)
	{
		this.needThrow = need;
	}

	public int getConvertType(StringRef typeName)
	{
		if (typeName != null)
		{
			typeName.setString("Object");
		}
		return TypeManager.TYPE_OBJECT;
	}

	public Object convert(Object value)
	{
		if (value instanceof ObjectRef)
		{
			return this.convert(((ObjectRef) value).getObject());
		}
		return this.changeByPropertyEditor(value);
	}

	public Object convert(String value)
	{
		return this.changeByPropertyEditor(value);
	}

	public String convertToString(Object value)
	{
		return this.changeByPropertyEditorAsText(value);
	}

	public String convertToString(Object value, boolean changeNullToEmpty)
	{
		return value == null ? changeNullToEmpty ? "" : null
				: this.convertToString(value);
	}

	public Object changeByPropertyEditor(Object value)
	{
		if (this.propertyEditor == null)
		{
			return value;
		}
		try
		{
			synchronized (this.propertyEditor)
			{
				this.propertyEditor.setValue(value);
				return this.propertyEditor.getValue();
			}
		}
		catch (RuntimeException ex)
		{
			if (this.needThrow)
			{
				throw ex;
			}
			else
			{
				return value;
			}
		}
	}

	public Object changeByPropertyEditor(String value)
	{
		if (this.propertyEditor == null)
		{
			return value;
		}
		try
		{
			synchronized (this.propertyEditor)
			{
				this.propertyEditor.setAsText(value);
				return this.propertyEditor.getValue();
			}
		}
		catch (RuntimeException ex)
		{
			if (this.needThrow)
			{
				throw ex;
			}
			else
			{
				return value;
			}
		}
	}

	public String changeByPropertyEditorAsText(Object value)
	{
		if (this.propertyEditor == null)
		{
			return value == null ? null : value.toString();
		}
		try
		{
			synchronized (this.propertyEditor)
			{
				this.propertyEditor.setValue(value);
				return this.propertyEditor.getAsText();
			}
		}
		catch (RuntimeException ex)
		{
			if (this.needThrow)
			{
				throw ex;
			}
			else
			{
				return value == null ? null : value.toString();
			}
		}
	}

	public static String getCastErrorMessage(Object obj, String needType)
	{
		return "Can't cast [" + obj + "](" + obj.getClass() + ") to " + needType + ".";
	}

	public static ConfigurationException getErrorTypeException(Object obj, String needType)
	{
		return new ConfigurationException(getCastErrorMessage(obj, needType));
	}

	public ValueConverter copy()
	{
		try
		{
			return (ValueConverter) super.clone();
		}
		catch (Exception ex)
		{
			// assert false
			throw new Error();
		}
	}

}