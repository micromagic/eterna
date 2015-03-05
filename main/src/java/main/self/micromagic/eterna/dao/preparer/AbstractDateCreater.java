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

package self.micromagic.eterna.dao.preparer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import self.micromagic.eterna.share.AttributeManager;
import self.micromagic.eterna.share.Tool;
import self.micromagic.util.StringTool;
import self.micromagic.util.ref.StringRef;

abstract class AbstractDateCreater extends AbstractPreparerCreater
{
	public AbstractDateCreater(String name, String type)
	{
		super(name);
		this.typeName = type;
	}

	protected String typeName;
	protected DateFormat[] formats;
	protected Locale[] locales;
	protected TimeZone timeZone = null;

	public void setPattern(String pattern)
	{
		String[] fArr = StringTool.separateString(pattern, ";", true);
		this.formats = new DateFormat[fArr.length];
		this.locales = new Locale[fArr.length];
		for (int i = 0; i < fArr.length; i++)
		{
			StringRef realPattern = new StringRef();
			Locale locale = Tool.parseLocal(fArr[i], realPattern);
			if (locale == null)
			{
				this.formats[i] = new SimpleDateFormat(fArr[i]);
			}
			else
			{
				this.locales[i] = locale;
				this.formats[i] = new SimpleDateFormat(realPattern.getString(), locale);
			}
		}
	}

	protected void setAttributes(AttributeManager attributes)
	{
		super.setAttributes(attributes);
		String tStr = (String) attributes.getAttribute("timeZone");
		if (tStr != null)
		{
			this.timeZone = TimeZone.getTimeZone(tStr);
		}
	}

	public ValuePreparer createPreparer(Object value)
	{
		if (this.formats == null)
		{
			Calendar calendar = this.timeZone == null ? Calendar.getInstance()
					: Calendar.getInstance(this.timeZone);
			return this.createPreparer(value, null, calendar);
		}
		for (int i = 0; i < this.formats.length; i++)
		{
			try
			{
				Calendar calendar;
				if (this.locales[i] == null)
				{
					calendar = this.timeZone == null ? Calendar.getInstance()
							: Calendar.getInstance(this.timeZone);
				}
				else
				{
					calendar = this.timeZone == null ? Calendar.getInstance(this.locales[i])
							: Calendar.getInstance(this.timeZone, this.locales[i]);
				}
				return this.createPreparer(value, this.formats[i], calendar);
			}
			catch (Throwable ex) {}
		}
		throw new ClassCastException("Can't cast [" + value
				+ "](" + value.getClass() + ") to " + this.typeName + ".");
	}

	protected abstract ValuePreparer createPreparer(Object value, DateFormat format, Calendar calendar);

	public ValuePreparer createPreparer(String value)
	{
		if (this.formats == null)
		{
			Calendar calendar = this.timeZone == null ? Calendar.getInstance()
					: Calendar.getInstance(this.timeZone);
			return this.createPreparer(value, null, calendar);
		}
		for (int i = 0; i < this.formats.length; i++)
		{
			try
			{
				Calendar calendar;
				if (this.locales[i] == null)
				{
					calendar = this.timeZone == null ? Calendar.getInstance()
							: Calendar.getInstance(this.timeZone);
				}
				else
				{
					calendar = this.timeZone == null ? Calendar.getInstance(this.locales[i])
							: Calendar.getInstance(this.timeZone, this.locales[i]);
				}
				return this.createPreparer(value, this.formats[i], calendar);
			}
			catch (Throwable ex) {}
		}
		throw new ClassCastException("Can't cast [" + value
				+ "](" + value.getClass() + ") to " + this.typeName + ".");
	}

	protected abstract ValuePreparer createPreparer(String value, DateFormat format, Calendar calendar);

}
