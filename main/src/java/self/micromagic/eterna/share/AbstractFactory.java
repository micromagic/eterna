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

package self.micromagic.eterna.share;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.digester.FactoryManager;

public abstract class AbstractFactory
		implements Factory
{
	protected AttributeManager attributes = new AttributeManager();

	protected String name;
	protected Factory shareFactory;
	protected FactoryManager.Instance instance;

	public void initialize(FactoryManager.Instance instance, Factory shareFactory)
			throws EternaException
	{
		if (shareFactory == this)
		{
			throw new EternaException("The parent can't same this.");
		}
		this.instance = instance;
		this.shareFactory = shareFactory;
	}

	public String getName()
			throws EternaException
	{
		return this.name;
	}

	public void setName(String name)
			throws EternaException
	{
		this.name = name;
	}

	public FactoryManager.Instance getFactoryContainer()
			throws EternaException
	{
		return this.instance;
	}

	public Object getAttribute(String name)
			throws EternaException
	{
		return this.attributes.getAttribute(name);
	}

	public String[] getAttributeNames()
			throws EternaException
	{
		return this.attributes.getAttributeNames();
	}

	public Object setAttribute(String name, Object value)
			throws EternaException
	{
		return this.attributes.setAttribute(name, value);
	}

	public Object removeAttribute(String name)
			throws EternaException
	{
		return this.attributes.removeAttribute(name);
	}

	public boolean hasAttribute(String name)
			throws EternaException
	{
		return this.attributes.hasAttribute(name);
	}

	public void destroy()
	{
	}

}