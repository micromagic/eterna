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

import self.micromagic.eterna.dao.preparer.ValuePreparer;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;

/**
 * 一个条件对象.
 */
public class Condition
{
	public final String sqlPart;
	public final ValuePreparer[] preparers;

	public Condition(String sqlPart)
	{
		this(sqlPart, null);
	}

	public Condition(String sqlPart, ValuePreparer[] preparers)
	{
		this.sqlPart = sqlPart;
		this.preparers = preparers == null ? ConditionBuilder.EMPTY_PREPARERS : preparers;
	}

	public String toString()
	{
		if (this.strBuf == null)
		{
			int count = this.sqlPart.length() + 39;
			StringAppender buf = StringTool.createStringAppender(count);
			buf.append("Condition[sqlPart:(").append(this.sqlPart);
			buf.append("),preparerCount:").append(this.preparers.length).append(']');
			this.strBuf = buf.toString();
		}
		return this.strBuf;
	}
	private String strBuf = null;

}