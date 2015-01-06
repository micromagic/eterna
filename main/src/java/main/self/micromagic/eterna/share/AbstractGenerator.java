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

import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import self.micromagic.eterna.share.EternaException;

public abstract class AbstractGenerator
		implements Generator
{
	protected static final Log log = EternaFactoryImpl.log;

	protected AttributeManager attributes = new AttributeManager();

	protected String name;
	protected Factory factory;

	public Object getAttribute(String name)
	{
		return this.attributes.getAttribute(name);
	}

	public String[] getAttributeNames()
	{
		return this.attributes.getAttributeNames();
	}

	public Object setAttribute(String name, Object value)
	{
		return this.attributes.setAttribute(name, value);
	}

	public Object removeAttribute(String name)
	{
		return this.attributes.removeAttribute(name);
	}

	public void setFactory(Factory factory)
			throws EternaException
	{
		this.factory = factory;
	}

	public String getName()
			 throws EternaException
	{
		return this.name;
	}

	public void setName(String name)
			 throws EternaException
	{
		if (!this.checkName(name))
		{
			throw new EternaException("The name [" + name
					+ "] can't use (\",\", \";\", \"#\", \"$\", \"?\", \":\", \"/\","
					+ " \"{\", \"}\", \"[\", \"]\", \"(\", \")\", \"[space]\").");
		}
		this.name = name;
	}

	protected boolean checkName(String name)
	{
		if (name == null)
		{
			return true;
		}
		StringTokenizer st = new StringTokenizer(name, ",;#$?:/{}[]() \t\r\n", true);
		return st.countTokens() <= 1;
	}

	public void destroy()
	{

	}

}