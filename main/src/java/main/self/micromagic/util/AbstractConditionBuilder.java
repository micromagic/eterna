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

import self.micromagic.eterna.search.ConditionBuilder;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;

/**
 * 一个抽象的ConditionBuilder的实现.
 */
public abstract class AbstractConditionBuilder extends AbstractGenerator
		implements ConditionBuilder
{
	public boolean initialize(EternaFactory factory)
	{
		if (this.factory == null)
		{
			this.factory = factory;
			return false;
		}
		return true;
	}

	public String getCaption()
	{
		return this.caption;
	}
	public void setCaption(String caption)
	{
		this.caption = caption;
	}
	protected String caption;

	public void setOperator(String operator)
	{
		this.operator = operator;
	}
	protected String operator;

	public Object create()
			throws EternaException
	{
		return this;
	}

}