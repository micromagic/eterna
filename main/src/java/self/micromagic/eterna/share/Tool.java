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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import self.micromagic.cg.BeanTool;
import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;

/**
 * 框架中需要用到的一些公共方法.
 */
public class Tool
{
	/**
	 * 用于记录日志.
	 */
	public static final Log log = Utility.createLog("eterna.share");

	public static final String CAPTION_TRANSLATE_TAG = "caption.translate";
	public static final String CAPTION_TRANSLATE_MAP_TAG = "caption.translate.map";
	public static final String CAPTION_TRANSLATE_MAP_FACTORY_TAG = "caption.translate.map.factory";

	/**
	 * 根据标题翻译列表的配置进行翻译.
	 */
	public static String translateCaption(EternaFactory factory, String name)
			throws ConfigurationException
	{
		Map translateMap = (Map) factory.getAttribute(CAPTION_TRANSLATE_MAP_TAG);
		Object checkFactory = factory.getAttribute(CAPTION_TRANSLATE_MAP_FACTORY_TAG);
		if (translateMap == null || checkFactory != factory)
		{
			translateMap = getCaptionTranslateMap(factory);
			if (translateMap == null)
			{
				return null;
			}
		}
		return (String) translateMap.get(name);
	}

	/**
	 * 获得标题翻译用的map.
	 */
	public static synchronized Map getCaptionTranslateMap(EternaFactory factory)
			throws ConfigurationException
	{
		String translateStr = (String) factory.getAttribute(CAPTION_TRANSLATE_TAG);
		if (translateStr == null)
		{
			return null;
		}
		Map translateMap = (Map) factory.getAttribute(CAPTION_TRANSLATE_MAP_TAG);
		Object checkFactory = factory.getAttribute(CAPTION_TRANSLATE_MAP_FACTORY_TAG);
		if (translateMap == null || checkFactory != factory)
		{
			EternaFactory share = factory.getShareFactory();
			boolean needTranslate = true;
			if (share != null)
			{
				String shareStr = (String) share.getAttribute(CAPTION_TRANSLATE_TAG);
				if (shareStr != null)
				{
					if (shareStr == translateStr)
					{
						translateMap = getCaptionTranslateMap(share);
						needTranslate = false;
					}
					else
					{
						translateMap = new HashMap(getCaptionTranslateMap(share));
					}
				}
			}
			if (translateMap == null)
			{
				translateMap = new HashMap();
			}
			if (needTranslate)
			{
				String[] tmps = StringTool.separateString(
						Utility.resolveDynamicPropnames(translateStr), ";", true);
				for (int i = 0; i < tmps.length; i++)
				{
					int index = tmps[i].indexOf('=');
					if (index != -1)
					{
						translateMap.put(tmps[i].substring(0, index).trim(),
								tmps[i].substring(index + 1).trim());
					}
				}
				factory.setAttribute(CAPTION_TRANSLATE_MAP_TAG, Collections.unmodifiableMap(translateMap));
			}
			else
			{
				factory.setAttribute(CAPTION_TRANSLATE_MAP_TAG, translateMap);
			}
			factory.setAttribute(CAPTION_TRANSLATE_MAP_FACTORY_TAG, factory);
		}
		return translateMap;
	}

	/**
	 * 在factory中注册bean名称的属性名.
	 */
	public static final String BEAN_CLASS_NAMES = "bean.class.names";

	/**
	 * 注册作为bean的类, 多个类名之间用","或";"隔开.
	 */
	public static void registerBean(String classNames)
	{
		BeanTool.registerBean(classNames);
	}

	/**
	 * 判断所给出的类是否是bean.
	 */
	public static boolean isBean(Class c)
	{
		return BeanTool.checkBean(c);
	}

	/**
	 * 生成一个bean的属性输出类.
	 *
	 * @param beanClass           bean类
	 * @param interfaceClass      处理接口
	 * @param methodHead          方法头部
	 * @param beanParamName       bean参数的名称
	 * @param unitTemplate        单元代码模板
	 * @param primitiveTemplate   基本类型单元代码模板
	 * @param linkTemplate        两个类型单元之间的连接模板
	 * @param imports             要引入的包
	 * @return                    返回相应的处理类
	 */
	public static Object createBeanPrinter(Class beanClass, Class interfaceClass, String methodHead,
			String beanParamName, String unitTemplate, String primitiveTemplate, String linkTemplate,
			String[] imports)
	{
		return BeanTool.createBeanProcesser("Printer", beanClass, interfaceClass, methodHead, beanParamName,
				unitTemplate, primitiveTemplate, linkTemplate, imports, BeanTool.BEAN_PROCESSER_TYPE_R);
	}

	/**
	 * 调用一个对象的方法.
	 *
	 * @param object          被调用方法的对象, 如果此对象是个<code>Class</code>, 则
	 *                        给出的方法名必须是此类的静态方法
	 * @param methodName      方法的名称
	 * @param args            调用的参数
	 * @param parameterTypes  调用的参数类型
	 * @return  被调用的方法的返回结果
	 */
	public static Object invokeExactMethod(Object object, String methodName, Object[] args,
			Class[] parameterTypes)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
	{
		Class c;
		if (object instanceof Class)
		{
			c = (Class) object;
		}
		else
		{
			c = object.getClass();
		}
		Method method = c.getMethod(methodName, parameterTypes);
		return method.invoke(object, args);
	}

}