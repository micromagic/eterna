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

package self.micromagic.cg;

import java.beans.Introspector;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ReferenceMap;

/**
 * 描述bean的一个方法的信息类.
 *
 * @author micromagic@sina.com
 */
public class BeanMethodInfo
{
	/**
	 * 方法对应属性的名称.
	 */
	public final String name;

	/**
	 * 属性的类型.
	 */
	public final Class type;

	/**
	 * 对属性操作的方法.
	 */
	public final Method method;

	/**
	 * 是否为读取的方法.
	 * <code>true</code>为读取, <code>false</code>为设置.
	 */
	public final boolean doGet;

	/**
	 * 带有索引值的类型.
	 */
	public final Class indexedType;

	/**
	 * 带有索引值的属性操作的方法.
	 */
	public final Method indexedMethod;

	/**
	 * 一般的构造函数.
	 */
	private BeanMethodInfo(String name, Method method, Class type, boolean doGet,
			Method indexedMethod, Class indexedType)
	{
		this.name = name;
		this.method = method;
		this.type = type;
		this.doGet = doGet;
		this.indexedMethod = indexedMethod;
		this.indexedType = indexedType;
	}

	/**
	 * 将两个<code>BeanMethodInfo</code>合并的构造函数.
	 */
	private BeanMethodInfo(BeanMethodInfo info1, BeanMethodInfo info2)
	{
		this.name = info1.name;
		this.type = info1.type == null ? info2.type : info1.type;
		this.doGet = info1.doGet;
		if (isBool(this.type) && this.doGet)
		{
			if (info1.method == null)
			{
				this.method = info2.method;
			}
			else if (info2.method == null)
			{
				this.method = info1.method;
			}
			// 如果两个方法都存在, 则取is开头的方法
			else if (info1.method.getName().startsWith(IS_PREFIX))
			{
				this.method = info1.method;
			}
			else
			{
				this.method = info2.method;
			}
		}
		else
		{
			this.method = info1.method == null ? info2.method : info1.method;
		}
		// 带有索引值的方法不会有is开头的
		this.indexedType = info1.indexedType == null ? info2.indexedType : info1.indexedType;
		this.indexedMethod = info1.indexedMethod == null ? info2.indexedMethod : info1.indexedMethod;
	}

	/**
	 * bean相关的方法列表的缓存.
	 */
	private static ReferenceMap beanMethodsCache = new ReferenceMap(ReferenceMap.WEAK, ReferenceMap.SOFT);

	/**
	 * 获取和这个bean相关的方法.
	 *
	 * @param beanClass   bean类
	 * @return      bean相关的方法列表
	 */
	public static synchronized BeanMethodInfo[] getBeanMethods(Class beanClass)
	{
		BeanMethodInfo[] result = (BeanMethodInfo[]) beanMethodsCache.get(beanClass);
		if (result != null)
		{
			return result;
		}

		Method[] methodList = beanClass.getMethods();
		Map tmpMethodsCache = new LinkedHashMap();
		for (int i = 0; i < methodList.length; i++)
		{
			Method method = methodList[i];
			if (Modifier.isStatic(method.getModifiers()))
			{
				// 忽略静态方法
				continue;
			}

			String name = method.getName();
			Class argTypes[] = method.getParameterTypes();
			Class resultType = method.getReturnType();
			int argCount = argTypes.length;
			if (argCount == 0)
			{
				if (name.startsWith(GET_PREFIX) && !name.equals(GET_CLASS))
				{
					// 一般的get方法
					addBeanMethod(tmpMethodsCache, Introspector.decapitalize(name.substring(3)),
							method, resultType, true, false);
				}
				else if (isBool(resultType) && name.startsWith(IS_PREFIX))
				{
					// boolean类型的get方法
					addBeanMethod(tmpMethodsCache, Introspector.decapitalize(name.substring(2)),
							method, resultType, true, false);
				}
			}
			else if (argCount == 1)
			{
				if (argTypes[0] == int.class && name.startsWith(GET_PREFIX))
				{
					// 带索引值的get方法
					addBeanMethod(tmpMethodsCache, Introspector.decapitalize(name.substring(3)),
							method, resultType, true, true);
				}
				else if (resultType == void.class && name.startsWith(SET_PREFIX))
				{
					// 一般的set方法
					addBeanMethod(tmpMethodsCache, Introspector.decapitalize(name.substring(3)),
							method, argTypes[0], false, false);
				}
			}
			else if (argCount == 2)
			{
				if (argTypes[0] == int.class && name.startsWith(SET_PREFIX))
				{
					// 带索引值的set方法
					addBeanMethod(tmpMethodsCache, Introspector.decapitalize(name.substring(3)),
							method, argTypes[1], false, true);
				}
			}
		}

		result = arrangeMethods(tmpMethodsCache);
		beanMethodsCache.put(beanClass, result);
		return result;
	}

	/**
	 * 判断所给的类型是否为布尔型.
	 */
	private static boolean isBool(Class type)
	{
		return type == boolean.class || type == Boolean.class;
	}

	/**
	 * 整理获取的bean方法.
	 */
	private static BeanMethodInfo[] arrangeMethods(Map methodsCache)
	{
		List result = new ArrayList();
		List list;
		Iterator itr = methodsCache.values().iterator();
		while (itr.hasNext())
		{
			list = (List) itr.next();

			// 每个bean方法名称最多有5个方法, is get set getI setI, 最坏情况
			// 就是这5个方法的类型都不一样, 没一组方法合并起来最多是两种.
			// is/get 和 set
			BeanMethodInfo[][] infos = new BeanMethodInfo[5][2];

			// 将所有的方法按类型分类, 整理到上面的数组中
			for (int i = 0; i < list.size(); i++)
			{
				BeanMethodInfo info = (BeanMethodInfo) list.get(i);
				for (int x = 0; x < infos.length; x++)
				{
					if (infos[x][0] != null)
					{
						BeanMethodInfo tmp = infos[x][0];
						if (checkType(tmp, info))
						{
							if (tmp.doGet == info.doGet)
							{
								infos[x][0] = new BeanMethodInfo(tmp, info);
							}
							else if (infos[x][1] == null)
							{
								infos[x][1] = info;
							}
							else
							{
								infos[x][1] = new BeanMethodInfo(infos[x][1], info);;
							}
							break;
						}
					}
					else
					{
						infos[x][0] = info;
						break;
					}
				}
			}

			int rWeight = 0;
			BeanMethodInfo[] r = null;

			// 计算每一组整理出来的权重, 取最高的
			// 权重规则为:
			// 有get                   +1
			// 有set和get              +1
			// 有基本方法              +1
			for (int i = 0; i < infos.length; i++)
			{
				if (infos[i][0] == null)
				{
					// 没有方法信息了则退出
					break;
				}
				int weitht = 0;
				if (infos[i][1] != null)
				{
					// 有set和get
					weitht++;
					// 有get  (两个方法都存在必定有get)
					weitht++;
					if (infos[i][1].method != null)
					{
						// 有基本方法
						weitht++;
					}
				}
				else if (infos[i][0].doGet)
				{
					// 有get
					weitht++;
				}
				if (infos[i][0].method != null)
				{
					// 有基本方法
					weitht++;
				}

				if (weitht > rWeight)
				{
					// 权重比原来的高则替换原来的
					r = infos[i];
					rWeight = weitht;
				}
			}

			if (r != null)
			{
				for (int i = 0; i < r.length; i++)
				{
					if (r[i] != null)
					{
						result.add(r[i]);
					}
				}
			}
		}
		return (BeanMethodInfo[]) result.toArray(new BeanMethodInfo[result.size()]);
	}

	/**
	 * 检查两个方法信息的类型是否一致.
	 */
	private static boolean checkType(BeanMethodInfo info1, BeanMethodInfo info2)
	{
		if (info1.type != null && info2.type != null)
		{
			// 如果基本类型都不为null, 则必须相同
			return info1.type == info2.type;
		}
		if (info1.indexedType != null && info2.indexedType != null)
		{
			// 如果带索引的类型都不为null, 则必须相同
			return info1.indexedType == info2.indexedType;
		}
		return checkIndexedType(info1.type, info2.indexedType)
				|| checkIndexedType(info2.type, info1.indexedType);
	}

	/**
	 * 检查带索引的类型和基本类型是否是一致的.
	 */
	private static boolean checkIndexedType(Class type, Class indexedType)
	{
		if (type == null || indexedType == null)
		{
			// 任何一个为null, 则不可能一致
			return false;
		}
		if (type == indexedType)
		{
			// 如果两个类型相同则一致
			return true;
		}
		if (type.isArray() && type.getComponentType() == indexedType)
		{
			// 如果基本类型为数组, 且数组元素类型和带索引的类型相同则一致
			return true;
		}
		if (Collection.class.isAssignableFrom(type))
		{
			// 如果基本类型实现了Collection, 则认为是一致的
			return true;
		}
		// 剩余情况都为不一致
		return false;
	}

	/**
	 * 向临时方法缓存中添加一个方法.
	 */
	private static void addBeanMethod(Map methodsCache, String name, Method method, Class type,
			boolean doGet, boolean withIndex)
	{
		BeanMethodInfo bmi;
		if (withIndex)
		{
			bmi = new BeanMethodInfo(name, null, null, doGet, method, type);
		}
		else
		{
			bmi = new BeanMethodInfo(name, method, type, doGet, null, null);
		}
		List l = (List) methodsCache.get(name);
		if (l == null)
		{
			l = new ArrayList();
			methodsCache.put(name, l);
		}
		l.add(bmi);
	}

	private static final String GET_CLASS = "getClass";
	private static final String GET_PREFIX = "get";
	private static final String SET_PREFIX = "set";
	private static final String IS_PREFIX = "is";

}