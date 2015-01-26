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

import org.apache.commons.logging.Log;

import self.micromagic.eterna.share.Tool;

public abstract class AbstractValuePreparer
		implements ValuePreparer
{
	protected static final Log log = Tool.log;

	protected PreparerCreater creater;
	protected int index;
	protected String name;

	public AbstractValuePreparer(PreparerCreater creater)
	{
		this.creater = creater;
	}

	public PreparerCreater getCreater()
	{
		return this.creater;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return this.name;
	}

	public void setRelativeIndex(int index)
	{
		this.index = index;
	}

	public int getRelativeIndex()
	{
		return this.index;
	}

}