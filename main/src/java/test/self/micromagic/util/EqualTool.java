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

package self.micromagic.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 用于比较两个未实现equals方法的对象是否相同.
 */
public class EqualTool
{
	/**
	 * 检查两个对象是否相同.
	 */
	public static synchronized boolean checkEquals(Object obj1, Object obj2)
	{
		boolean firstCheck = !inCheckEquals;
		if (firstCheck)
		{
			inCheckEquals = true;
		}
		try
		{
			return checkEquals0(obj1, obj2);
		}
		finally
		{
			if (firstCheck)
			{
				inCheckEquals = false;
				checkedObjs.clear();
			}
		}
	}

	private static synchronized boolean checkEquals0(Object obj1, Object obj2)
	{
		if (obj1 == obj2)
		{
			return true;
		}
		if (obj1 == null)
		{
			// 因为前面比较过 obj1 == obj2, 所以obj2必定不为null
			return false;
		}
		if (obj2 == null)
		{
			return false;
		}
		if (hasChecked(obj1, obj2))
		{
			// 如果两个对象已经比较过, 如果为测试模式, 则返回true, 否则返回false
			return TEST_MODEL;
		}
		if (!TEST_MODEL)
		{
			// 如果不是测试模式, 则只通过equals方法进行比较
			return obj1.equals(obj2);
		}
		String className1 = obj1.getClass().getName();
		if (className1.startsWith("java."))
		{
			// 如果是java包下的类, 直接用equals方法进行比较
			return obj1.equals(obj2);
		}
		if (obj1 instanceof Map || obj1 instanceof Collection)
		{
			// 如果是容器对象, 直接用equals方法进行比较
			return obj1.equals(obj2);
		}
		Class c = obj1.getClass();
		if (c != obj2.getClass())
		{
			// 如果对应的类不同, 则两个对象不相同
			return false;
		}
		if (c.isArray())
		{
			int length1 = Array.getLength(obj1);
			int length2 = Array.getLength(obj2);
			if (length1 != length2)
			{
				return false;
			}
			for (int i = 0; i < length1; i++)
			{
				if (!checkEquals0(Array.get(obj1, i), Array.get(obj2, i)))
				{
					return false;
				}
			}
		}
		return checkByField(c, obj1, obj2);
	}

	private static boolean checkByField(Class c, Object obj1, Object obj2)
	{
		try
		{
			Field[] fields = c.getDeclaredFields();
			for (int i = 0; i < fields.length; i++)
			{
				Field field = fields[i];
				if (Modifier.isStatic(field.getModifiers()))
				{
					// 如果是静态成员, 则不用比较
					continue;
				}
				field.setAccessible(true);
				if (!checkEquals0(field.get(obj1), field.get(obj2)))
				{
					return false;
				}
			}
			Class superC = c.getSuperclass();
			if (superC != null && superC != Object.class)
			{
				return checkByField(superC, obj1, obj2);
			}
			return true;
		}
		catch (Throwable ex)
		{
			return false;
		}
	}

	/**
	 * 存放已比较过的对象.
	 */
	private static Set checkedObjs = new HashSet();

	/**
	 * 判断两个对象是否已经比较过.
	 */
	private static boolean hasChecked(Object obj1, Object obj2)
	{
		CheckedObject cb = new CheckedObject(obj1, obj2);
		return !checkedObjs.add(cb);
	}

	/**
	 * 当前是否在相等检查中.
	 */
	private static boolean inCheckEquals = false;

	/**
	 * 是否为测试模式.
	 */
	private static boolean TEST_MODEL = true;

	/**
	 * 设置是否为测试模式.
	 * 如果不是测试模式, 则只通过equals方法进行比较.
	 */
	public static synchronized void setTestModel(boolean model)
	{
		TEST_MODEL = model;
	}

	/**
	 * 获取是否为测试模式.
	 */
	public static boolean getTestModel()
	{
		return TEST_MODEL;
	}

	/**
	 * 已进行比较过的对象.
	 */
	private static class CheckedObject
	{
		private final int hashCode;
		private final Object obj1;
		private final Object obj2;

		public CheckedObject(Object obj1, Object obj2)
		{
			int h1 = obj1 == null ? 0 : System.identityHashCode(obj1);
			int h2 = obj2 == null ? 0 : System.identityHashCode(obj2);
			this.hashCode = h1 ^ h2;
			this.obj1 = obj1;
			this.obj2 = obj2;
		}

		public int hashCode()
		{
			return this.hashCode;
		}

		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj instanceof CheckedObject)
			{
				CheckedObject other = (CheckedObject) obj;
				if (this.obj1 == other.obj1)
				{
					return this.obj2 == other.obj2;
				}
			}
			return false;
		}

	}
}