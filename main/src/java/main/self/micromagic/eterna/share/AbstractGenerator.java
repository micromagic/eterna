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

package self.micromagic.eterna.share;

import java.util.StringTokenizer;

import org.apache.commons.logging.Log;

/**
 * 一个抽象的对象创建者.
 */
public abstract class AbstractGenerator
		implements Generator
{
	/**
	 * 日志.
	 */
	protected static final Log log = EternaFactoryImpl.log;

	protected transient AttributeManager attributes = new AttributeManager();

	protected String name;
	protected transient Factory factory;

	/**
	 * 根据名称获取一个属性.
	 */
	public Object getAttribute(String name)
	{
		return this.attributes.getAttribute(name);
	}

	/**
	 * 获取对象创建者中的属性名称列表.
	 * 如果没有任何属性则返回一个空的数组.
	 */
	public String[] getAttributeNames()
	{
		return this.attributes.getAttributeNames();
	}

	/**
	 * 设置一个属性.
	 */
	public Object setAttribute(String name, Object value)
	{
		return this.attributes.setAttribute(name, value);
	}

	/**
	 * 移除一个属性.
	 */
	public Object removeAttribute(String name)
	{
		return this.attributes.removeAttribute(name);
	}

	/**
	 * 设置对象创建者所属的工厂.
	 */
	public void setFactory(Factory factory)
			throws EternaException
	{
		this.factory = factory;
	}

	/**
	 * 获取对象创建者的名称.
	 */
	public String getName()
			 throws EternaException
	{
		return this.name;
	}

	/**
	 * 设置对象创建者的名称.
	 */
	public void setName(String name)
			 throws EternaException
	{
		if (!this.checkName(name))
		{
			throw new EternaException("The name [" + name
					+ "] can't use (\",\", \";\", \"#\", \"$\", \"?\", \":\", \"/\", "
					+ "\"{\", \"}\", \"[\", \"]\", \"(\", \")\", \"\"\", \"\'\""
					+ "\"[space]\").");
		}
		this.name = name;
	}

	/**
	 * 检查名称是否合法.
	 */
	protected boolean checkName(String name)
	{
		if (name == null)
		{
			return true;
		}
		StringTokenizer st = new StringTokenizer(name, ",;#$?:/{}[]()\"\' \t\r\n", true);
		return st.countTokens() <= 1;
	}

}