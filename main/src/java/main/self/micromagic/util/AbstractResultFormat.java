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

package self.micromagic.util;

import self.micromagic.eterna.dao.ResultFormat;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;

/**
 * 一个抽象的ResultFormat的实现.
 */
public abstract class AbstractResultFormat extends AbstractGenerator
		implements ResultFormat
{
	protected EternaFactory factory;
	protected String type;
	protected String pattern;

	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		if (this.factory == null)
		{
			this.factory = factory;
			return false;
		}
		return true;
	}

	public boolean useEmptyString()
	{
		return true;
	}

	public Object create()
			throws EternaException
	{
		return this;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public void setPattern(String pattern)
	{
		this.pattern = pattern;
	}

}
