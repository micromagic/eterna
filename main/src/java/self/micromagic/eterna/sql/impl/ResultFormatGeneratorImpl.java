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

package self.micromagic.eterna.sql.impl;

import java.text.DateFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.Locale;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.security.Permission;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.sql.ResultFormat;
import self.micromagic.eterna.sql.ResultFormatGenerator;
import self.micromagic.eterna.sql.ResultRow;
import self.micromagic.eterna.sql.ResultReader;
import self.micromagic.util.converter.BooleanConverter;
import self.micromagic.util.FormatTool;

public class ResultFormatGeneratorImpl extends AbstractGenerator
		implements ResultFormatGenerator
{
	private String formatType;
	private String formatPattern;

	public void setType(String type)
	{
		this.formatType = type;
	}

	public void setPattern(String pattern)
	{
		this.formatPattern = pattern;
	}

	public Object create()
			throws EternaException
	{
		return this.createFormat();
	}

	public ResultFormat createFormat()
			throws EternaException
	{
		Format format;
		if (this.formatType == null)
		{
			// 当没有指定类型时, 无法生成需要的格式化对象
			throw new EternaException(
					"The format's attribute [type] not give.");
		}
		if ("Number".equals(this.formatType))
		{
			if (this.formatPattern == null)
			{
				format = NumberFormat.getInstance();
			}
			else
			{
				format = new java.text.DecimalFormat(this.formatPattern);
			}
		}
		else if ("Date".equals(this.formatType))
		{
			if (this.formatPattern == null)
			{
				format = DateFormat.getInstance();
			}
			else if (this.formatPattern.startsWith("locale:"))
			{
				int index = this.formatPattern.indexOf(',');
				if (index == -1)
				{
					// 如果有地区设置，地区与日期模式之间必须用“,”分隔
					throw new EternaException(
							"Error format pattern:[" + this.formatPattern + "].");
				}
				String pattern = this.formatPattern.substring(index + 1);
				String localeStr = this.formatPattern.substring(7, index);
				index = localeStr.indexOf('_');
				Locale locale = index == -1 ? new Locale(localeStr)
						: new Locale(localeStr.substring(0, index), localeStr.substring(index + 1));
				format = new java.text.SimpleDateFormat(pattern, locale);
			}
			else
			{
				format = new java.text.SimpleDateFormat(this.formatPattern);
			}
		}
		else if ("boolean".equals(this.formatType))
		{
			return new BooleanFormat(this.formatPattern, this.name);
		}
		else
		{
			// 类型不明, 无法生成需要的格式化对象
			throw new EternaException(
					"Error format type [" + this.formatType + "].");
		}
		return new MyResultFormat(format, this.name);
	}

	private static class MyResultFormat
			implements ResultFormat
	{
		private Format format;
		private String name;

		public MyResultFormat(Format format, String name)
		{
			this.format = format;
			this.name = name;
		}

		public void initialize(EternaFactory factory) {}

		public String getName()
		{
			return this.name;
		}

		public Object format(Object obj, ResultRow row, ResultReader reader, Permission permission)
		{
			return obj == null ? "" : FormatTool.getThreadFormat(this.format).format(obj);
		}

		public boolean useEmptyString()
		{
			return true;
		}

	}

	private static class BooleanFormat
			implements ResultFormat
	{
		private static final BooleanConverter booleanConverter = new BooleanConverter();

		private String trueValue = "Yes";
		private String falseValue = "No";
		private String name;

		public BooleanFormat(String formatPattern, String name)
		{
			this.name = name;
			if (formatPattern == null)
			{
				return;
			}
			int index = formatPattern.indexOf(':');
			if (index != -1)
			{
				this.trueValue = formatPattern.substring(0, index);
				this.falseValue = formatPattern.substring(index + 1);
			}
		}

		public void initialize(EternaFactory factory) {}

		public String getName()
		{
			return this.name;
		}

		public Object format(Object obj, ResultRow row, ResultReader reader, Permission permission)
		{
			if (obj == null)
			{
				return "";
			}
			boolean v = booleanConverter.convertToBoolean(obj);
			return v ? this.trueValue : this.falseValue;
		}

		public boolean useEmptyString()
		{
			return true;
		}

	}

}