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

package self.micromagic.eterna.search;

import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.sql.preparer.ValuePreparer;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;

/**
 * SQL语句条件的生成器. <p>
 * {@link self.micromagic.eterna.search.ConditionBuilder.Condition}将通过该生成器来生成SQL条件。
 *
 * @author  micromagic@sina.com
 */
public interface ConditionBuilder
{
	public static final String[] OPERATOR_NAMES = {
		"isNull", "notNull", "checkNull",
		"equal", "notEqual", "large", "below", "notLarge", "notBelow",
		"beginWith", "endWith", "include", "match"
	};

	public static ValuePreparer[] EMPTY_PREPARERS = new ValuePreparer[0];

	void initialize(EternaFactory factory) throws ConfigurationException;

	public String getName() throws ConfigurationException;

	public String getCaption() throws ConfigurationException;

	/**
	 * 生成一个SQL条件.
	 *
	 * @param colName  要生成的条件的名称.
	 * @param value    要生成的条件的值.
	 * @param cp       与此条件生成器的相对应的ConditionProperty.
	 * @return         所生成的条件, 及相关参数.
	 */
	public Condition buildeCondition(String colName, String value, ConditionProperty cp)
			throws ConfigurationException;

	public static class Condition
	{
		public final String sqlPart;
		public final ValuePreparer[] preparers;

		private String toStrBuf = null;

		public Condition(String sqlPart)
		{
			this(sqlPart, null);
		}

		public Condition(String sqlPart, ValuePreparer[] preparers)
		{
			this.sqlPart = sqlPart;
			this.preparers = preparers == null ? EMPTY_PREPARERS : preparers;
		}

		public String toString()
		{
			if (this.toStrBuf == null)
			{
				int count = this.sqlPart.length() + 39;
				StringAppender buf = StringTool.createStringAppender(count);
				buf.append("Condition[sqlPart:(").append(this.sqlPart);
				buf.append("),preparerCount:").append(this.preparers.length).append(']');
				this.toStrBuf = buf.toString();
			}
			return this.toStrBuf;
		}

	}

}