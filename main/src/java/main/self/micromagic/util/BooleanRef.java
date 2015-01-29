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
package self.micromagic.util;

import java.io.Serializable;

import self.micromagic.util.converter.BooleanConverter;

public class BooleanRef extends ObjectRef
		implements Serializable
{
	public boolean value;

	public BooleanRef()
	{
		this.value = false;
	}

	public BooleanRef(boolean value)
	{
		this.value = value;
	}

	public BooleanRef(Boolean value)
	{
		this.value = value.booleanValue();
	}

	public boolean isBoolean()
	{
		return true;
	}

	public boolean booleanValue()
	{
		return this.value;
	}

	public static boolean getBooleanValue(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		if (obj instanceof Boolean)
		{
			return ((Boolean) obj).booleanValue();
		}
		if (obj instanceof Number)
		{
			return ((Number) obj).intValue() != 0;
		}
		if (obj instanceof String)
		{
			return (new BooleanConverter()).convertToBoolean((String) obj);
		}
		return false;
	}

	public void setObject(Object obj)
	{
		this.value = BooleanRef.getBooleanValue(obj);
	}

	public Object getObject()
	{
		return this.value ? Boolean.TRUE : Boolean.FALSE;
	}

	public void setBoolean(boolean value)
	{
		this.value = value;
	}

	public boolean getBoolean()
	{
		return this.value;
	}

	public String toString()
	{
		return String.valueOf(this.value);
	}

	public boolean equals(Object other)
	{
		int result = this.shareEqual(other, BooleanRef.class);
		if (result != MORE_EQUAL)
		{
			return result == TRUE_EQUAL;
		}
		return this.value == ((BooleanRef) other).value;
	}

	private static final long serialVersionUID = 1L;

}