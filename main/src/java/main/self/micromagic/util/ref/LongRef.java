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

public class LongRef extends ObjectRef
		implements Serializable
{
	public long value;

	public LongRef()
	{
		this.value = 0L;
	}

	public LongRef(long value)
	{
		this.value = value;
	}

	public LongRef(Long value)
	{
		this.value = value.intValue();
	}

	public boolean isNumber()
	{
		return true;
	}

	public int intValue()
	{
		return (int) this.value;
	}

	public long longValue()
	{
		return this.value;
	}

	public double doubleValue()
	{
		return this.value;
	}

	public static long getLongValue(Object obj)
	{
		if (obj == null)
		{
			return 0L;
		}
		else if (obj instanceof Number)
		{
			return ((Number) obj).longValue();
		}
		else if (obj instanceof String)
		{
			try
			{
				return Long.parseLong((String) obj);
			}
			catch (NumberFormatException e) {}
		}
		return 0L;
	}

	public void setObject(Object obj)
	{
		this.value = LongRef.getLongValue(obj);
		super.setObject(null);
	}

	public Object getObject()
	{
		Object obj = super.getObject();
		if (obj == null || ((Long) obj).longValue() != this.value)
		{
			obj = new Long(this.value);
			super.setObject(obj);
		}
		return obj;
	}

	public void setLong(long value)
	{
		this.value = value;
		super.setObject(null);
	}

	public long getLong()
	{
		return this.value;
	}

	public String toString()
	{
		return Long.toString(this.value);
	}

	public boolean equals(Object other)
	{
		int result = this.shareEqual(other, LongRef.class);
		if (result != MORE_EQUAL)
		{
			return result == TRUE_EQUAL;
		}
		return this.value == ((LongRef) other).value;
	}

	private static final long serialVersionUID = 1L;

}