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

package self.micromagic.eterna.digester;

import org.xml.sax.Attributes;
import self.micromagic.eterna.share.Factory;
import self.micromagic.cg.ClassGenerator;

/**
 * 注册一个工厂的初始化规则.
 *
 * @author micromagic@sina.com
 */
public class FactoryRegisterRule extends ObjectCreateRule
{
	protected String registerName = null;

	/**
	 * @param className        工厂的实现类, 如果配置中没有指定会将此作为默认值
	 * @param attributeName    配置中哪个属性名指定工厂的实现类
	 * @param classType        工厂实现的接口类
	 * @param registerName     注册此工厂的分类名
	 */
	public FactoryRegisterRule(String className, String attributeName, Class classType,
			String registerName)
	{
		super(className, attributeName, classType);
		this.registerName = registerName;
	}

	/**
	 * @param className        工厂的实现类, 如果配置中没有指定会将此作为默认值
	 * @param attributeName    配置中哪个属性名指定工厂的实现类
	 * @param classType        工厂实现的接口类
	 */
	public FactoryRegisterRule(String className, String attributeName, Class classType)
	{
		this(className, attributeName, classType, null);
	}

	public void myBegin(String namespace, String name, Attributes attributes)
			throws Exception
	{
		String realClassName = ObjectCreateRule.getClassName(
				this.attributeName, this.className, attributes);
		this.digester.getLogger().debug("New " + realClassName);

		Factory instance = null;
		boolean register = false;
		if (this.registerName != null)
		{
			try
			{
				instance = FactoryManager.getFactory(this.registerName, realClassName);
			}
			catch (Exception ex) {}
		}
		if (instance == null)
		{
			register = true;
			instance = (Factory) ObjectCreateRule.createObject(realClassName);
		}
		if (this.classType != null && !this.classType.isInstance(instance))
		{
			throw new InvalidAttributesException(realClassName + " is not instance of "
					+ ClassGenerator.getClassName(this.classType));
		}
		this.digester.push(instance);

		if (this.registerName != null && register)
		{
			FactoryManager.addFactory(this.registerName, instance);
		}
	}

}