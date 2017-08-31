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

package self.micromagic.dbvm.core;

import self.micromagic.dbvm.ConstantDef;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;

/**
 * 抽象的各类对象.
 */
public abstract class AbstractObject
		implements ConstantDef
{
	protected EternaFactory factory;
	protected String name;

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

	public String getName()
		throws EternaException
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

}