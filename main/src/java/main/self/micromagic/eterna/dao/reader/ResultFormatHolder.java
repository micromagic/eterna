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

package self.micromagic.eterna.dao.reader;

import self.micromagic.eterna.dao.ResultFormat;
import self.micromagic.eterna.dao.ResultReader;
import self.micromagic.eterna.dao.ResultRow;
import self.micromagic.eterna.security.Permission;
import self.micromagic.eterna.share.EternaFactory;

/**
 * format的占位对象.
 *  在设置结果的格式化对象名称时使用.
 */
public class ResultFormatHolder
		implements ResultFormat
{
	private final String name;

	public ResultFormatHolder(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return this.name;
	}

	public String toString()
	{
		return this.getName();
	}

	public boolean initialize(EternaFactory factory)
	{
		return false;
	}

	public Object format(Object obj, ResultRow row,
			ResultReader reader, Permission permission)
	{
		return null;
	}

	public boolean useEmptyString()
	{
		return true;
	}

}
