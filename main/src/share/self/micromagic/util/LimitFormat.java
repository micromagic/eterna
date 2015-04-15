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

package self.micromagic.util;

import self.micromagic.eterna.security.Permission;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.sql.ResultFormat;
import self.micromagic.eterna.sql.ResultFormatGenerator;
import self.micromagic.eterna.sql.ResultRow;
import self.micromagic.eterna.sql.ResultReader;

public class LimitFormat extends AbstractGenerator
		implements ResultFormat, ResultFormatGenerator
{
	private int limit = 5;

	public void initialize(EternaFactory factory)
			throws EternaException
	{
	}

	public Object format(Object obj, ResultRow row, ResultReader reader, Permission permission)
			throws EternaException
	{
		String temp = obj == null ? "" : obj.toString();
		return Utils.formatLength(temp, this.limit);
	}

	public boolean useEmptyString()
	{
		return true;
	}

	public Object create()
			throws EternaException
	{
		return this.createFormat();
	}

	public void setType(String type)
	{
	}

	public void setPattern(String pattern)
	{
		try
		{
			this.limit = Integer.parseInt(pattern);
		}
		catch (NumberFormatException ex) {}
	}

	public ResultFormat createFormat()
	{
		return this;
	}

}