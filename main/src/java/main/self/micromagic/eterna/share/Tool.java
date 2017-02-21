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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import self.micromagic.cg.BeanMap;
import self.micromagic.cg.BeanTool;
import self.micromagic.eterna.digester2.ParseException;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;
import self.micromagic.util.converter.BooleanConverter;
import self.micromagic.util.converter.ConverterFinder;
import self.micromagic.util.converter.ValueConverter;
import self.micromagic.util.ref.StringRef;

/**
 * 框架中需要用到的一些公共方法.
 */
public class Tool
{
	/**
	 * 用于记录日志.
	 */
	public static final Log log = Utility.createLog("eterna.share");

	/**
	 * 标识后面是模式字符串的前缀.
	 */
	public static final String PATTERN_PREFIX = "pattern:";
	/**
	 * 在arrtibute中设置使用格式化模式的名称.
	 */
	public static final String PATTERN_FLAG = "pattern";

	/**
	 * 在arrtibute中设置是否显示的名称.
	 */
	public static final String VISIBLE_FLAG = "visible";
	/**
	 * 在arrtibute中设置是否要将空字符串变为null的名称.
	 */
	public static final String EMPTY_TO_NULL_FLAG = "emptyToNull";

	private Tool()
	{
	}

	/**
	 * 存放需要检查哪些属性值为空的属性名. <p>
	 * 目前只对item的属性生效.
	 */
	public static final String ATTR_CHECK_EMPTY_TAG = "attribute.check.empty";

	/**
	 * 获取需要检查属性值为空的属性名称集合.
	 */
	public static Set getCheckEmptyAttrs(EternaFactory factory)
	{
		Object checkObj = factory.getAttribute(ATTR_CHECK_EMPTY_TAG);
		if (checkObj == null || checkObj instanceof Set)
		{
			return (Set) checkObj;
		}
		String namesDef = (String) checkObj;
		Set baseSet = null;
		EternaFactory share = factory.getShareFactory();
		if (share != null)
		{
			baseSet = getCheckEmptyAttrs(share);
			// 如果没在当前工厂定义, 则再获取一次则会变成map
			checkObj = factory.getAttribute(ATTR_CHECK_EMPTY_TAG);
			if (checkObj instanceof Set)
			{
				return (Set) checkObj;
			}
		}
		Set namesSet = baseSet == null ? new HashSet() : new HashSet(baseSet);
		String[] arr = StringTool.separateString(
				Utility.resolveDynamicPropnames(namesDef), ",;", true);
		for (int i = 0; i < arr.length; i++)
		{
			int index = arr[i].indexOf('=');
			if (index != -1)
			{
				String name = arr[i].substring(0, index).trim();
				String flag = arr[i].substring(index + 1).trim();
				if (BooleanConverter.toBoolean(flag))
				{
					namesSet.add(name);
				}
				else
				{
					namesSet.remove(name);
				}
			}
			else
			{
				namesSet.add(arr[i]);
			}
		}
		factory.setAttribute(ATTR_CHECK_EMPTY_TAG, namesSet);
		return namesSet;
	}

	/**
	 * 存放翻译字典配置的属性名.
	 */
	public static final String CAPTION_TRANSLATE_TAG = "caption.translate";

	/**
	 * 根据标题翻译列表的配置进行翻译.
	 */
	public static String translateCaption(EternaFactory factory, String name)
			throws EternaException
	{
		Map translateMap = getCaptionTranslateMap(factory);
		return translateMap == null ? null : (String) translateMap.get(name);
	}

	/**
	 * 获得标题翻译用的map.
	 */
	public static synchronized Map getCaptionTranslateMap(EternaFactory factory)
			throws EternaException
	{
		Object checkObj = factory.getAttribute(CAPTION_TRANSLATE_TAG);
		if (checkObj == null || checkObj instanceof Map)
		{
			return (Map) checkObj;
		}
		String translateDef = (String) checkObj;
		Map baseMap = null;
		EternaFactory share = factory.getShareFactory();
		if (share != null)
		{
			baseMap = getCaptionTranslateMap(share);
			// 如果没在当前工厂定义, 则再获取一次则会变成map
			checkObj = factory.getAttribute(CAPTION_TRANSLATE_TAG);
			if (checkObj instanceof Map)
			{
				return (Map) checkObj;
			}
		}
		Map translateMap = baseMap == null ? new HashMap() : new HashMap(baseMap);
		String[] tmps = StringTool.separateString(
				Utility.resolveDynamicPropnames(translateDef), ";", true);
		for (int i = 0; i < tmps.length; i++)
		{
			int index = tmps[i].indexOf('=');
			if (index != -1)
			{
				translateMap.put(tmps[i].substring(0, index).trim(),
						tmps[i].substring(index + 1).trim());
			}
		}
		factory.setAttribute(CAPTION_TRANSLATE_TAG, translateMap);
		return translateMap;
	}

	/**
	 * 存放属性名与类型对应关系的属性名.
	 */
	public static final String ATTR_TYPE_DEF_FLAG = "attribute.type.def";
	/**
	 * 所有对象类型的标识.
	 */
	public static final String ALL_OBJ_TYPE = "$all";

	/**
	 * 实现类的前缀.
	 */
	public static final String CLASS_PREFIX = "class:";

	/**
	 * 根据定义表转换属性的类型.
	 *
	 * @param factory    当前的工厂
	 * @param objType    属性所在对象的类型
	 * @param attrName   属性的名称
	 * @param attrValue  属性的值
	 */
	public static Object transAttrType(EternaFactory factory, String objType,
			String attrName, Object attrValue)
	{
		Map typeMap = (Map) factory.getFactoryContainer().getAttribute(ATTR_TYPE_DEF_FLAG);
		if (typeMap == null)
		{
			typeMap = getAllAttrTypeDefMap0(factory);
		}
		ValueConverter converter = getValueConverter(typeMap, objType, attrName);
		if (converter != null)
		{
			return converter.convert(attrValue);
		}
		return attrValue;
	}
	private static ValueConverter getValueConverter(Map typeMap,
			String objType, String attrName)
	{
		if (typeMap == null)
		{
			return null;
		}
		ValueConverter converter = null;
		Map typeDef = (Map) typeMap.get(objType);
		if (typeDef != null)
		{
			converter = (ValueConverter) typeDef.get(attrName);
			if (converter == null)
			{
				Map allDef = (Map) typeMap.get(ALL_OBJ_TYPE);
				if (allDef != null)
				{
					converter = (ValueConverter) allDef.get(attrName);
				}
			}
		}
		else
		{
			Map allDef = (Map) typeMap.get(ALL_OBJ_TYPE);
			if (allDef != null)
			{
				converter = (ValueConverter) allDef.get(attrName);
			}
		}
		return converter;
	}

	/**
	 * 获取各个对象类型的属性类型定义.
	 */
	private static synchronized Map getAllAttrTypeDefMap0(EternaFactory factory)
	{
		Object checkObj = factory.getAttribute(ATTR_TYPE_DEF_FLAG);
		if (checkObj == null || checkObj instanceof Map)
		{
			// 为null或类型为map可直接返回
			return (Map) checkObj;
		}
		EternaFactory share = factory.getShareFactory();
		Map baseMap = null;
		if (share != null)
		{
			baseMap = getAllAttrTypeDefMap0(share);
			// 如果没在当前工厂定义, 则再获取一次则会变成map
			checkObj = factory.getAttribute(ATTR_TYPE_DEF_FLAG);
			if (checkObj instanceof Map)
			{
				return (Map) checkObj;
			}
		}
		Map typeMap = makeAllAttrTypeDefMap((String) checkObj, baseMap);
		factory.setAttribute(ATTR_TYPE_DEF_FLAG, typeMap);
		return typeMap;
	}

	private static final char OBJ_TYPE_SPLIT = '/';
	private static Map makeAllAttrTypeDefMap(String defStr, Map share)
	{
		Map result = new HashMap();
		Map tmpMap = StringTool.string2Map(defStr, ";", '=');
		Iterator itr = tmpMap.entrySet().iterator();
		while (itr.hasNext())
		{
			Map.Entry e = (Map.Entry) itr.next();
			String name = (String) e.getKey();
			int index = name.indexOf(OBJ_TYPE_SPLIT);
			String objType, attrName;
			if (index == -1)
			{
				objType = ALL_OBJ_TYPE;
				attrName = name;
			}
			else
			{
				objType = name.substring(0, index);
				attrName = name.substring(index + 1);
			}
			ValueConverter converter;
			String typeDefStr = (String) e.getValue();
			if (typeDefStr.startsWith(CLASS_PREFIX))
			{
				try
				{
					String className = typeDefStr.substring(CLASS_PREFIX.length());
					converter = (ValueConverter) Class.forName(className).newInstance();
				}
				catch (Exception ex)
				{
					throw new EternaException(ex);
				}
			}
			else
			{
				int typeId = TypeManager.getPureType(TypeManager.getTypeId(typeDefStr));
				converter = TypeManager.getConverter(typeId);
			}
			Map defMap = (Map) result.get(objType);
			if (defMap == null)
			{
				if (share != null && share.containsKey(objType))
				{
					// 如果共享工厂中存在对象类型, 则要复制过来
					defMap = new HashMap((Map) share.get(objType));
				}
				else
				{
					defMap = new HashMap();
				}
				result.put(objType, defMap);
			}
			defMap.put(attrName, converter);
		}
		if (share != null)
		{
			// 如果存在共享工厂的类型定义表, 则要将未定义的对象类型添加进来
			itr = share.entrySet().iterator();
			while (itr.hasNext())
			{
				Map.Entry e = (Map.Entry) itr.next();
				if (!result.containsKey(e.getKey()))
				{
					result.put(e.getKey(), e.getValue());
				}
			}
		}
		return result;
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
			throws NoSuchMethodException, IllegalAccessException
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
		try
		{
			method.setAccessible(true);
			return method.invoke(object, args);
		}
		catch (InvocationTargetException ex)
		{
			throw transInvocationTargetException(ex);
		}
	}

	/**
	 * 调用一个对象的方法.
	 *
	 * @param object          被调用方法的对象, 如果此对象是个<code>Class</code>, 则
	 *                        给出的方法名必须是此类的静态方法
	 * @param methodName      方法的名称
	 * @param args            调用的参数
	 * @return  被调用的方法的返回结果
	 */
	public static Object invokeExactMethod(Object object, String methodName, Object[] args)
			throws NoSuchMethodException, IllegalAccessException
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
		Method[] mArr = c.getMethods();
		int paramCount = args == null ? 0 : args.length;
		Method method = null;
		for (int i = 0; i < mArr.length; i++)
		{
			if (methodName.equals(mArr[i].getName()) && mArr[i].getParameterTypes().length == paramCount)
			{
				if (method != null)
				{
					throw new NoSuchMethodException("Has too many method:[" + methodName
							+ "] in class:[" + c.getName() + "]");
				}
				method = mArr[i];
			}
		}
		if (method == null)
		{
			throw new NoSuchMethodException("Not found method:[" + methodName
					+ "] in class:[" + c.getName() + "]");
		}
		Class[] pTypes = method.getParameterTypes();
		for (int i = 0; i < paramCount; i++)
		{
			if (args[i] != null && !pTypes[i].isInstance(args[i]))
			{
				args[i] = ConverterFinder.findConverter(pTypes[i]).convert(args[i]);
			}
		}
		try
		{
			method.setAccessible(true);
			return method.invoke(object, args);
		}
		catch (InvocationTargetException ex)
		{
			throw transInvocationTargetException(ex);
		}
	}

	/**
	 * 获取InvocationTargetException的目标异常并转换为RuntimeException.
	 */
	public static RuntimeException transInvocationTargetException(InvocationTargetException ex)
	{
		Throwable t = ex.getTargetException();
		if (t instanceof RuntimeException)
		{
			return (RuntimeException) t;
		}
		return new EternaException(t != null ? t : ex);
	}

	/**
	 * 解析模式字符串中的所在地区信息.
	 * 如: locale:zn_CN,yyyy-MM-dd
	 *
	 * @param pattern      需要解析的模式字符串
	 * @param realPattern  去除地区信息部分的模式字符串
	 * @return  地区信息对象, 如果为null表示模式字符串中没有地区信息
	 */
	public static Locale parseLocal(String pattern, StringRef realPattern)
	{
		if (pattern == null)
		{
			return null;
		}
		String checkStr = "locale:";
		Locale locale = null;
		if (pattern.startsWith(checkStr))
		{
			int index = pattern.indexOf(',');
			if (index == -1)
			{
				// 如果有地区设置，地区与日期模式之间必须用","分隔
				throw new EternaException(
						"Error format pattern with locale [" + pattern + "].");
			}
			String localeStr = pattern.substring(7, index);
			pattern = pattern.substring(index + 1);
			index = localeStr.indexOf('_');
			if (index == -1)
			{
				locale = new Locale(localeStr);
			}
			else
			{
				String language = localeStr.substring(0, index);
				int tmpI = localeStr.indexOf('_', index + 1);
				if (tmpI == -1)
				{
					locale = new Locale(language, localeStr.substring(index + 1));
				}
				else
				{
					locale = new Locale(language, localeStr.substring(index + 1, tmpI),
							localeStr.substring(tmpI + 1));
				}
			}
		}
		if (realPattern != null)
		{
			realPattern.setString(pattern);
		}
		return locale;
	}


	/**
	 * 根据指定的类名创建对象.
	 */
	public static Object createObject(String className)
			throws EternaException, ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		Class c = getClass(className, Thread.currentThread().getContextClassLoader());
		return c.newInstance();
	}

	/**
	 * 根据指定的类名获取class.
	 */
	public static Class getClass(String className, ClassLoader loader)
			throws EternaException, ClassNotFoundException
	{
		try
		{
			return Class.forName(className);
		}
		catch (ClassNotFoundException ex)
		{
			if (loader == null)
			{
				throw ex;
			}
			return Class.forName(className, true, loader);
		}
	}

	/**
	 * 根据指定的类名生成对其处理的BeanMap. <p>
	 * 如果不是必须生成, 在不能生成时返回null.
	 * 如果必须生成, 在不能生成时会抛出异常.
	 *
	 * @param className    要生成实例的类名
	 * @param loader       载入类所使用的ClassLoader.
	 * @param mustCreate   是否必须生成, 默认为true.
	 */
	public static BeanMap createBeanMap(String className, ClassLoader loader, boolean mustCreate)
			throws EternaException
	{
		try
		{
			Class theClass = getClass(className, loader);
			BeanMap beanMap = BeanTool.getBeanMap(theClass, null);
			if (beanMap.createBean() != null)
			{
				beanMap.setReadBeforeModify(false);
				beanMap.setThrowException(true);
				return beanMap;
			}
			if (mustCreate)
			{
				throw new ParseException("Can't create [" + className + "].");
			}
		}
		catch (ClassNotFoundException ex)
		{
			if (mustCreate)
			{
				throw new ParseException(ex);
			}
		}
		return null;
	}

	/**
	 * 检查并获取emptyToNull的配置值.
	 */
	public static Boolean checkEmptyToNull(String baseValue, Factory factory)
	{
		String tStr = baseValue;
		if (tStr == null && factory != null)
		{
			tStr = (String) factory.getAttribute(Tool.EMPTY_TO_NULL_FLAG);
		}
		if (tStr != null)
		{
			return BooleanConverter.toBoolean(tStr) ? Boolean.TRUE : Boolean.FALSE;
		}
		return null;
	}

}
