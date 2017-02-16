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

import self.micromagic.eterna.dao.preparer.CreaterManager;
import self.micromagic.eterna.dao.preparer.PreparerCreater;
import self.micromagic.eterna.dao.preparer.ValuePreparer;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;

/**
 * 一个抽象的PreparerCreater的实现.
 */
public abstract class AbstractPreparerCreater extends AbstractGenerator
		implements PreparerCreater
{
	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		if (this.factory == null)
		{
			this.factory = factory;
			this.baseCreater = CreaterManager.createPreparerCreater(
					this.type, null, factory);
			return false;
		}
		return true;
	}
	private EternaFactory factory;
	private PreparerCreater baseCreater;

	public Object create()
			throws EternaException
	{
		return this;
	}

	public EternaFactory getFactory()
			throws EternaException
	{
		return this.factory;
	}

	public void setType(String type)
	{
		this.type = type;
	}
	protected String type;

	public void setPattern(String pattern)
	{
		this.pattern = pattern;
	}
	protected String pattern;

	public ValuePreparer createPreparer(Object value)
			throws EternaException
	{
		Object v = this.convertValue(value);
		return this.baseCreater.createPreparer(v);
	}

	public ValuePreparer createPreparer(String value)
			throws EternaException
	{
		Object v = this.convertValue(value);
		return this.baseCreater.createPreparer(v);
	}

}
