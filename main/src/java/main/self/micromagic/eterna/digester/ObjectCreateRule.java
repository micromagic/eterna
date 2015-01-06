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

import java.lang.reflect.Constructor;

import org.xml.sax.Attributes;
import self.micromagic.util.Utility;
import self.micromagic.cg.ClassGenerator;

/**
 * 生成一个类的实例的初始化规则.
 * 被生成的类必须有一个无参的构造函数.
 *
 * @author micromagic@sina.com
 */
public class ObjectCreateRule extends MyRule
{
	public static final Class[] defaultConstructorParamClass = new Class[0];
	public static final Object[] defaultConstructorParam = new Object[0];

	protected String attributeName = null;
	protected String className = null;
	protected Class classType = null;

	/**
	 * @param className           要生成实例的类名, 如果配置中没有指定会将此作为默认值
	 * @param attributeName       配置中哪个属性名指定类名
	 * @param classType           实现的接口类或父类, 生成完后会进行类型检查
	 */
	public ObjectCreateRule(String className, String attributeName, Class classType)
	{
		this.className = className;
		this.attributeName = attributeName;
		this.classType = classType;
	}

	/**
	 * 获得要生成实例的类名.
	 *
	 * @param attributeName       配置中哪个属性名指定类名
	 * @param className           要生成实例的类名, 如果配置中没有指定会将此作为默认值
	 * @param attributes          当前xml元素的属性集
	 */
	public static String getClassName(String attributeName, String className, Attributes attributes)
	{
		String realClassName = className;
		if (attributeName != null)
		{
			String value = attributes.getValue(attributeName);
			if (value != null)
			{
				realClassName = value;
			}
		}
		return realClassName;
	}

	/**
	 * 根据指定的类名生成实例. <p>
	 * 在不能生成时会抛出异常.
	 *
	 * @param className    要生成实例的类名
	 */
	public static Object createObject(String className)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		return createObject(className, true);
	}

	/**
	 * 根据指定的类名生成实例. <p>
	 * 如果不是必须生成, 在不能生成时返回null.
	 * 如果必须生成, 在不能生成时会抛出异常.
	 *
	 * @param className    要生成实例的类名
	 * @param mustCreate   是否必须生成, 默认为true.
	 */
	public static Object createObject(String className, boolean mustCreate)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		try
		{
			Class theClass;
			if (className.startsWith("c|"))
			{
				theClass = (Class) FactoryManager.getCurrentInstance().getAttribute(className);
			}
			else
			{
				theClass = Class.forName(className);
			}
			Object instance;
			try
			{
				Constructor constructor = theClass.getDeclaredConstructor(
						defaultConstructorParamClass);
				if (!constructor.isAccessible())
				{
					constructor.setAccessible(true);
					instance = constructor.newInstance(defaultConstructorParam);
					constructor.setAccessible(false);
				}
				else
				{
					instance = theClass.newInstance();
				}
			}
			catch (Exception ex)
			{
				instance = theClass.newInstance();
			}
			return instance;
		}
		catch (ClassNotFoundException ex)
		{
			return ObjectCreateRule.createObject(
					className, Utility.getContextClassLoader(), mustCreate);
		}
	}

	/**
	 * 根据指定的类名生成实例. <p>
	 * 如果不是必须生成, 在不能生成时返回null.
	 * 如果必须生成, 在不能生成时会抛出异常.
	 *
	 * @param className    要生成实例的类名
	 * @param loader       载入类所使用的ClassLoader.
	 * @param mustCreate   是否必须生成, 默认为true.
	 */
	public static Object createObject(String className, ClassLoader loader, boolean mustCreate)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		try
		{
			Class theClass = Class.forName(className, true, loader);
			Object instance;
			try
			{
				Constructor constructor = theClass.getDeclaredConstructor(
						defaultConstructorParamClass);
				if (!constructor.isAccessible())
				{
					constructor.setAccessible(true);
					instance = constructor.newInstance(defaultConstructorParam);
					constructor.setAccessible(false);
				}
				else
				{
					instance = theClass.newInstance();
				}
			}
			catch (Exception ex)
			{
				instance = theClass.newInstance();
			}
			return instance;
		}
		catch (ClassNotFoundException ex)
		{
			if (mustCreate)
			{
				throw ex;
			}
			return null;
		}
	}

	/**
	 * 检查实例的类型是否符合指定的接口或类, 如果不符合则会抛出异常.
	 *
	 * @param classType                      需要符合的接口或类
	 * @param instance                       被检查的实例
	 * @throws InvalidAttributesException    类型不符合时抛出的异常.
	 */
	public static void checkType(Class classType, Object instance)
			throws InvalidAttributesException
	{
		if (classType != null && !classType.isInstance(instance))
		{
			throw new InvalidAttributesException("The class '"
					+ ClassGenerator.getClassName(instance.getClass())
					+ "' is not instance of " + ClassGenerator.getClassName(classType));
		}
	}

	public void myBegin(String namespace, String name, Attributes attributes)
			throws Exception
	{
		String realClassName = ObjectCreateRule.getClassName(
				this.attributeName, this.className, attributes);
		this.digester.getLogger().debug("New " + realClassName);
		Object instance = ObjectCreateRule.createObject(realClassName);
		ObjectCreateRule.checkType(this.classType, instance);
		this.digester.push(instance);
	}

	public void myEnd(String namespace, String name)
			throws Exception
	{
		Object top = this.digester.pop();
		this.digester.getLogger().debug("Pop " + ClassGenerator.getClassName(top.getClass()));
	}

}