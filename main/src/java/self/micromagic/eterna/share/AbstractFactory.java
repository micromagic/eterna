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

import self.micromagic.eterna.digester.FactoryManager;
import self.micromagic.eterna.digester.ConfigurationException;

public abstract class AbstractFactory
		implements Factory
{
	protected AttributeManager attributes = new AttributeManager();

	protected String name;
	protected Factory shareFactory;
	protected FactoryManager.Instance factoryManager;

	public void initialize(FactoryManager.Instance factoryManager, Factory shareFactory)
			throws ConfigurationException
	{
		if (shareFactory == this)
		{
			throw new ConfigurationException("The parent can't same this.");
		}
		this.factoryManager = factoryManager;
		this.shareFactory = shareFactory;
	}

	public String getName()
			throws ConfigurationException
	{
		return this.name;
	}

	public void setName(String name)
			throws ConfigurationException
	{
		this.name = name;
	}

	public FactoryManager.Instance getFactoryManager()
			throws ConfigurationException
	{
		return this.factoryManager;
	}

	public Object getAttribute(String name)
			throws ConfigurationException
	{
		return this.attributes.getAttribute(name);
	}

	public String[] getAttributeNames()
			throws ConfigurationException
	{
		return this.attributes.getAttributeNames();
	}

	public Object setAttribute(String name, Object value)
			throws ConfigurationException
	{
		return this.attributes.setAttribute(name, value);
	}

	public Object removeAttribute(String name)
			throws ConfigurationException
	{
		return this.attributes.removeAttribute(name);
	}

	public boolean hasAttribute(String name)
			throws ConfigurationException
	{
		return this.attributes.hasAttribute(name);
	}

	public void destroy()
	{
	}

}