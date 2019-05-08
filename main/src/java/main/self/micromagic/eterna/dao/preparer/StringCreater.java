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

package self.micromagic.eterna.dao.preparer;

import java.sql.SQLException;
import java.sql.Types;

import self.micromagic.eterna.dao.PreparedStatementWrap;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.Tool;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;
import self.micromagic.util.converter.StringConverter;

class StringCreater extends AbstractPreparerCreater
{
	private static final StringConverter convert = new StringConverter();

	private String beginStr = "";
	private String endStr = "";
	private int appendLength = 0;
	private int caseType = 0;
	private boolean emptyToNull = true;

	public StringCreater(String name)
	{
		super(name);
	}

	public void setPattern(String pattern)
	{
		if (pattern.startsWith("#lower#"))
		{
			this.caseType = -1;
			pattern = pattern.substring(7);
		}
		else if (pattern.startsWith("#upper#"))
		{
			this.caseType = 1;
			pattern = pattern.substring(7);
		}
		int index = pattern.indexOf('$');
		if (index != -1)
		{
			this.beginStr = pattern.substring(0, index);
			this.endStr = pattern.substring(index + 1);
		}
		else
		{
			this.endStr = pattern;
		}
		this.appendLength = this.beginStr.length() + this.endStr.length();
	}

	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		if (super.initialize(factory))
		{
			return true;
		}
		Boolean b = Tool.checkEmptyToNull(
				(String) this.getAttribute(Tool.EMPTY_TO_NULL_FLAG), factory);
		if (b != null)
		{
			this.emptyToNull = b.booleanValue();
		}
		return false;
	}

	public Object convertValue(Object value)
	{
		return this.convertValue(convert.convertToString(value));
	}

	public Object convertValue(String value)
	{
		if (this.appendLength == 0)
		{
			if (value == null || (value.length() == 0 && this.emptyToNull))
			{
				return null;
			}
			if (this.caseType != 0)
			{
				value = this.caseType > 0 ? value.toUpperCase() : value.toLowerCase();
			}
			return value;
		}
		if (value == null)
		{
			value = "";
		}
		if (this.caseType != 0)
		{
			value = this.caseType > 0 ? value.toUpperCase() : value.toLowerCase();
		}
		StringAppender buf = StringTool.createStringAppender(value.length() + this.appendLength);
		buf.append(this.beginStr).append(value).append(this.endStr);
		return buf.toString();
	}

	public ValuePreparer createPreparer(Object value)
	{
		return this.createPreparer(convert.convertToString(value));
	}

	public ValuePreparer createPreparer(String value)
	{
		return new StringPreparer(this, (String) this.convertValue(value), Types.VARCHAR);
	}

}

class BigStringCreater extends StringCreater
{
	/**
	 * 系统日志时是否仅仅输出语句.
	 */
	private static int sqlType = Types.CLOB;

	static
	{
		try
		{
			Utility.addMethodPropertyManager("eterna.dao.bigString.sqlType",
					BigStringCreater.class, "setBigStringSqlType");
		}
		catch (Throwable ex)
		{
			Tool.log.warn("Error in init big string sql type.", ex);
		}
	}

	public BigStringCreater(String name)
	{
		super(name);
	}

	static void setBigStringSqlType(String type)
	{
		if (type == null)
		{
			sqlType = Types.CLOB;
		}
		else
		{
			int typeId = TypeManager.getTypeId(type);
			if (typeId != TypeManager.TYPE_NONE)
			{
				sqlType = TypeManager.getSQLType(typeId);
			}
			else
			{
				try
				{
					sqlType = Integer.parseInt(type);
				}
				catch (NumberFormatException ex)
				{
					sqlType = Types.CLOB;
				}
			}
		}
	}

	public ValuePreparer createPreparer(String value)
	{
		return new StringPreparer(this, (String) this.convertValue(value), sqlType);
	}

}

class StringPreparer extends AbstractValuePreparer
{
	private final String value;
	private final int sqlType;

	public StringPreparer(PreparerCreater creater, String value, int nullType)
	{
		super(creater);
		this.value = value;
		this.sqlType = nullType;
	}

	public void setValueToStatement(int index, PreparedStatementWrap stmtWrap)
			throws SQLException
	{
		if (this.value == null)
		{
			stmtWrap.setNull(this.getName(), index, this.sqlType);
		}
		else
		{
			stmtWrap.setString(this.getName(), index, this.value);
		}
	}

}
