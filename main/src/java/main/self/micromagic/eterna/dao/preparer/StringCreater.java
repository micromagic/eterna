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

import self.micromagic.eterna.dao.PreparedStatementWrap;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.Tool;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.converter.StringConverter;

class StringCreater extends AbstractPreparerCreater
{
	public StringCreater(String name)
	{
		super(name);
	}
	private static final StringConverter convert = new StringConverter();
	String beginStr = "";
	String endStr = "";
	int appendLength = 0;
	int caseType = 0;
	boolean emptyToNull = true;

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
		return new StringPreparer(this, (String) this.convertValue(value));
	}

}

class StringPreparer extends AbstractValuePreparer
{
	private final String value;

	public StringPreparer(PreparerCreater creater, String value)
	{
		super(creater);
		this.value = value;
	}

	public void setValueToStatement(int index, PreparedStatementWrap stmtWrap)
			throws SQLException
	{
		stmtWrap.setString(this.getName(), index, this.value);
	}

}
