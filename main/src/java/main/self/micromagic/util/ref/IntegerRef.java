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

package self.micromagic.util.ref;

import java.io.Serializable;

public class IntegerRef extends ObjectRef
		implements Serializable
{
	public int value;

	public IntegerRef()
	{
		this.value = 0;
	}

	public IntegerRef(int value)
	{
		this.value = value;
	}

	public IntegerRef(Integer value)
	{
		this.value = value.intValue();
	}

	public boolean isNumber()
	{
		return true;
	}

	public int intValue()
	{
		return this.value;
	}

	public long longValue()
	{
		return this.value;
	}

	public double doubleValue()
	{
		return this.value;
	}

	public static int getIntegerValue(Object obj)
	{
		if (obj == null)
		{
			return 0;
		}
		else if (obj instanceof Number)
		{
			return ((Number) obj).intValue();
		}
		else if (obj instanceof String)
		{
			try
			{
				return Integer.parseInt((String) obj);
			}
			catch (NumberFormatException e) {}
		}
		return 0;
	}

	public void setObject(Object obj)
	{
		this.value = IntegerRef.getIntegerValue(obj);
		super.setObject(null);
	}

	public Object getObject()
	{
		Object obj = super.getObject();
		if (obj == null || ((Integer) obj).intValue() != this.value)
		{
			obj = new Integer(this.value);
			super.setObject(obj);
		}
		return obj;
	}

	public void setInt(int value)
	{
		this.value = value;
		super.setObject(null);
	}

	public int getInt()
	{
		return this.value;
	}

	public String toString()
	{
		return Integer.toString(this.value);
	}

	public boolean equals(Object other)
	{
		int result = this.shareEqual(other, IntegerRef.class);
		if (result != ObjectRef.MORE_EQUAL)
		{
			return result == ObjectRef.TRUE_EQUAL;
		}
		return this.value == ((IntegerRef) other).value;
	}

	private static final long serialVersionUID = 1L;

}