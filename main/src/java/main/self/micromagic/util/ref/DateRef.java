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
package self.micromagic.util.ref;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import self.micromagic.util.FormatTool;

public class DateRef extends ObjectRef
		implements Serializable
{
	private static DateFormat format = DateFormat.getDateTimeInstance();

	public DateRef()
	{
		super(new Date());
	}

	public DateRef(Date date)
	{
		super(date);
	}

	public static Date getDateValue(Object obj)
	{
		if (obj == null)
		{
			return null;
		}
		else if (obj instanceof Date)
		{
			return (Date) obj;
		}
		else if (obj instanceof String)
		{
			try
			{
				return FormatTool.getThreadFormat(DateRef.format).parse((String) obj);
			}
			catch (ParseException ex) {}
		}
		return null;
	}

	public void setObject(Object obj)
	{
		super.setObject(DateRef.getDateValue(obj));
	}

	public void setDate(Date date)
	{
		this.setObject(date);
	}

	public Date getDate()
	{
		return (Date) this.getObject();
	}

	private static final long serialVersionUID = 1L;

}