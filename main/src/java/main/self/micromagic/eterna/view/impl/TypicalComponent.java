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

package self.micromagic.eterna.view.impl;

import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.EternaCreater;
import self.micromagic.eterna.view.Component;

/**
 * 一个Typical控件的创建者.
 */
public class TypicalComponent extends AbstractGenerator
		implements EternaCreater
{
	protected EternaFactory eternaFactory;
	protected Component component;

	public Class getObjectType()
	{
		return this.component.getClass();
	}

	public boolean isSingleton()
	{
		return true;
	}

	public EternaFactory getFactory()
	{
		return this.eternaFactory;
	}

	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		if (this.eternaFactory != null)
		{
			return true;
		}
		this.eternaFactory = factory;
		if (this.component == null)
		{
			throw new EternaException("No component.");
		}
		this.component.initialize(factory, null);
		return false;
	}

	public void addComponent(Component com)
			throws EternaException
	{
		if (this.component != null)
		{
			throw new EternaException("The component has setted in typical [" + this.getName() + "].");
		}
		this.component = com;
	}

	public Object create()
			throws EternaException
	{
		return this.component;
	}

	public void destroy()
	{
	}



}