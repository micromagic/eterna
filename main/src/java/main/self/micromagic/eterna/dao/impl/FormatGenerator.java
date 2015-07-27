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

package self.micromagic.eterna.dao.impl;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import self.micromagic.eterna.dao.ResultFormat;
import self.micromagic.eterna.dao.ResultReader;
import self.micromagic.eterna.dao.ResultRow;
import self.micromagic.eterna.security.Permission;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.Tool;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.FormatTool;
import self.micromagic.util.converter.BooleanConverter;
import self.micromagic.util.ref.StringRef;

public class FormatGenerator extends AbstractGenerator
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
		Format format;
		if (this.formatType == null)
		{
			// 当没有指定类型时, 无法生成需要的格式化对象
			throw new EternaException(
					"The format's attribute [type] not give.");
		}
		if (T_NUMBER.equals(this.formatType))
		{
			if (this.formatPattern == null)
			{
				format = NumberFormat.getInstance();
			}
			else
			{
				StringRef realPattern = new StringRef();
				Locale locale = Tool.parseLocal(this.formatPattern, realPattern);
				if (locale == null)
				{
					format = new DecimalFormat(this.formatPattern);
				}
				else
				{
					format = new DecimalFormat(realPattern.getString(),
							new DecimalFormatSymbols(locale));
				}
			}
		}
		else if (T_DATE.equals(this.formatType))
		{
			if (this.formatPattern == null)
			{
				format = DateFormat.getInstance();
			}
			else
			{
				StringRef realPattern = new StringRef();
				Locale locale = Tool.parseLocal(this.formatPattern, realPattern);
				if (locale == null)
				{
					format = new SimpleDateFormat(this.formatPattern);
				}
				else
				{
					format = new SimpleDateFormat(realPattern.getString(), locale);
				}
			}
		}
		else if (T_BOOLEAN.equals(this.formatType))
		{
			return new BooleanFormat(this.formatPattern, this.name);
		}
		else
		{
			// 类型不明, 无法生成需要的格式化对象
			throw new EternaException(
					"Error format type [" + this.formatType + "].");
		}
		return new StandardFormat(format, this.name);
	}

	/**
	 * 根据给出的类型及模式创建一个ResultFormat.
	 *
	 * @param type  类型的id
	 * @see TypeManager
	 */
	public static ResultFormat createFormat(int type, String pattern, ResultReader reader, EternaFactory factory)
	{
		synchronized (formatCache)
		{
			ResultFormat format = (ResultFormat) formatCache.get(pattern);
			if (format != null)
			{
				return format;
			}
			FormatGenerator fg = new FormatGenerator();
			fg.setName("_auto");
			fg.setPattern(pattern);
			if (TypeManager.isBoolean(type))
			{
				fg.setType(T_BOOLEAN);
			}
			else if (TypeManager.isNumber(type))
			{
				fg.setType(T_NUMBER);
			}
			else if (TypeManager.isDate(type))
			{
				fg.setType(T_DATE);
			}
			else
			{
				// 类型错误, 无法生成需要的格式化对象
				log.error("Error format type [" + TypeManager.getPureTypeName(type) + "] for pattern ["
						+ pattern + "] in factory [" + factory.getFactoryContainer().getId()
						+ "]'s reader [" + reader.getName() + "]");
				return null;
			}
			format = (ResultFormat) fg.create();
			formatCache.put(pattern, format);
			return format;
		}
	}
	private static Map formatCache = new HashMap();

	private static final String T_BOOLEAN = "boolean";
	private static final String T_DATE = "Date";
	private static final String T_NUMBER = "Number";

}

class StandardFormat
		implements ResultFormat
{
	private final Format format;
	private final String name;

	public StandardFormat(Format format, String name)
	{
		this.format = format;
		this.name = name;
	}

	public boolean initialize(EternaFactory factory)
	{
		return false;
	}

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

class BooleanFormat
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

	public boolean initialize(EternaFactory factory)
	{
		return false;
	}

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