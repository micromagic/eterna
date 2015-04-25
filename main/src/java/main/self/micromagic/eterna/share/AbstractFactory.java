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


/**
 * 一个抽象的工厂.
 */
public abstract class AbstractFactory
		implements Factory
{
	protected AttributeManager attributes = new AttributeManager();

	protected String name;
	protected Factory shareFactory;
	protected FactoryContainer factoryContainer;

	/**
	 * 对工厂进行初始化.
	 *
	 * @param factoryContainer  工厂所属的容器
	 * @param shareFactory      获取共享对象的工厂
	 * @return  是否已初始化
	 *          false表示是第一次初始化, true表示已执行过初始化
	 */
	public boolean initialize(FactoryContainer factoryContainer, Factory shareFactory)
			throws EternaException
	{
		if (shareFactory == this)
		{
			throw new EternaException("The parent can't same this.");
		}
		this.factoryContainer = factoryContainer;
		this.shareFactory = shareFactory;
		return false;
	}

	/**
	 * 获取工厂的名称.
	 */
	public String getName()
			throws EternaException
	{
		return this.name;
	}

	/**
	 * 设置工厂的名称.
	 */
	public void setName(String name)
			throws EternaException
	{
		this.name = name;
	}

	/**
	 * 获取工厂所属的容器.
	 */
	public FactoryContainer getFactoryContainer()
			throws EternaException
	{
		return this.factoryContainer;
	}

	/**
	 * 根据名称获取一个属性.
	 */
	public Object getAttribute(String name)
			throws EternaException
	{
		return this.attributes.getAttribute(name);
	}

	/**
	 * 获取工厂中的属性名称列表.
	 * 如果没有任何属性则返回一个空的数组.
	 */
	public String[] getAttributeNames()
			throws EternaException
	{
		return this.attributes.getAttributeNames();
	}

	/**
	 * 设置一个属性.
	 */
	public Object setAttribute(String name, Object value)
			throws EternaException
	{
		return this.attributes.setAttribute(name, value);
	}

	/**
	 * 移除一个属性.
	 */
	public Object removeAttribute(String name)
			throws EternaException
	{
		return this.attributes.removeAttribute(name);
	}

	/**
	 * 判断工厂中是否拥有某个属性.
	 */
	protected boolean hasAttribute(String name)
			throws EternaException
	{
		return this.attributes.hasAttribute(name);
	}

	/**
	 * 销毁工厂及工厂中的对象.
	 */
	public void destroy()
	{
	}

}