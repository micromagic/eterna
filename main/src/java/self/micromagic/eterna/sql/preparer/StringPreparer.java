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

package self.micromagic.eterna.sql.preparer;

import java.sql.SQLException;

import self.micromagic.eterna.sql.PreparedStatementWrap;
import self.micromagic.util.converter.StringConverter;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;

class StringPreparer extends AbstractValuePreparer
{
	private String value;

	public StringPreparer(ValuePreparerCreater vpc, String value)
	{
		super(vpc);
		this.value = value;
	}

	public void setValueToStatement(int index, PreparedStatementWrap stmtWrap)
			throws SQLException
	{
		String tmpStr = this.value;
		if (this.value != null && this.value.length() == 0 && this.vpc.isEmptyStringToNull())
		{
			tmpStr = null;
		}
		stmtWrap.setString(this.getName(), index, tmpStr);
	}

	static class Creater extends AbstractCreater
	{
		StringConverter convert = new StringConverter();
		String beginStr = "";
		String endStr = "";
		int appendLength = 0;
		int caseType = 0;

		public Creater(ValuePreparerCreaterGenerator vpcg)
		{
			super(vpcg);
		}

		public void setFormat(String formatStr)
		{
			if (formatStr.startsWith("#lower#"))
			{
				this.caseType = -1;
				formatStr = formatStr.substring(7);
			}
			else if (formatStr.startsWith("#upper#"))
			{
				this.caseType = 1;
				formatStr = formatStr.substring(7);
			}
			int index = formatStr.indexOf('$');
			if (index != -1)
			{
				this.beginStr = formatStr.substring(0, index);
				this.endStr = formatStr.substring(index + 1);
			}
			else
			{
				this.endStr = formatStr;
			}
			this.appendLength = this.beginStr.length() + this.endStr.length();
		}

		public ValuePreparer createPreparer(Object value)
		{
			return this.createPreparer(this.convert.convertToString(value));
		}

		public ValuePreparer createPreparer(String value)
		{
			if (this.appendLength == 0)
			{
				if (this.caseType != 0 && value != null)
				{
					value = this.caseType > 0 ? value.toUpperCase() : value.toLowerCase();
				}
				return new StringPreparer(this, value);
			}
			if (value == null) value = "";
			if (this.caseType != 0)
			{
				value = this.caseType > 0 ? value.toUpperCase() : value.toLowerCase();
			}
			StringAppender buf = StringTool.createStringAppender(value.length() + this.appendLength);
			buf.append(this.beginStr).append(value).append(this.endStr);
			return new StringPreparer(this, buf.toString());
		}

	}

}