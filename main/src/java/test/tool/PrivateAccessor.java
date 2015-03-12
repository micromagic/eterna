/*
 * Copyright 2014 xinjunli (micromagic@sina.com).
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

package tool;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * 类的私有方法及私有属性的访问工具. <p>
 * 当然用这个工具来访问那些非私有的成员也是可以的.
 */
public class PrivateAccessor
{
	/**
	 * 对一个值进行强制类型转换. <p>
	 * 如下面两个方法: <p>
	 * void test(String str)<p>
	 * void test(Object obj)<p>
	 * 如果你直接使用test(null)来调用编译时就会出错, 这时候需要通过强制
	 * 类型转换指定一个类型, 如: test((String) null).
	 * 这个方法就相当于对一个值进行强制类型转换.
	 * 使用样例: <p>
	 * invoke(obj, "methodName", cast(String.class, null));
	 *
	 * @param type   需要转换成的类型
	 * @param value  需要进行类型转换的值
	 */
	public static Object cast(Class type, Object value)
	{
		return new CastValue(type, value);
	}

	/**
	 * 调用一个类的构造函数来创建对象.
	 *
	 * @param c       需要构造的类
	 * @param params  构造的参数
	 */
	public static Object create(Class c, Object[] params)
			throws Exception
	{
		if (c == null)
		{
			throw new IllegalArgumentException("Param c is null!");
		}
		Constructor[] cArr = c.getDeclaredConstructors();
		int paramCount = params == null ? 0 : params.length;
		Constructor constructor = null;
		int match = Integer.MAX_VALUE;
		for (int i = 0; i < cArr.length; i++)
		{
			Class[] types = cArr[i].getParameterTypes();
			if (types.length == paramCount)
			{
				int tmp = checkTypes(types, params);
				if (tmp != -1 && tmp <= match)
				{
					constructor = cArr[i];
					match = tmp;
				}
			}
		}
		if (constructor == null)
		{
			throw new NoSuchMethodException(c.getName() + ".<init>()");
		}
		// 由于类的成员对象都是复制后返回的, 所以这里修改了Accessible属性
		// 不会对其它地方产生影响(后面的Filed、Method也同样如此)
		constructor.setAccessible(true);
		try
		{
			return constructor.newInstance(changeParams(params));
		}
		catch (InvocationTargetException ex)
		{
			Throwable target = ex.getTargetException();
			if (target instanceof Exception)
			{
				throw (Exception) target;
			}
			else if (target instanceof Error)
			{
				throw (Error) target;
			}
			else
			{
				throw ex;
			}
		}
	}

	/**
	 * 调用一个对象或类的(静态)方法.
	 *
	 * @param obj     方法所在的对象, 如果是静态方法则可直接给出其class
	 * @param method  需要调用的方法名称
	 * @param params  方法的参数
	 */
	public static Object invoke(Object obj, String method,
			Object[] params)
			throws Exception
	{
		return invoke(obj, null, method, params);
	}

	/**
	 * 调用一个对象或类的(静态)方法. <p>
	 * 增加了一个class类型的参数用于指定方法在哪个类中, 如在A中有个私有
	 * 方法set, B继承了A也有个私有方法set, 那如果直接给出B的实例那只能
	 * 调用到B中的方法set, 如果需要调用A中的方法set, 那就需要在这里type
	 * 参数中将class指定为A.
	 *
	 * @param obj     方法所在的对象, 如果是静态方法则可直接给出其class
	 * @param type    方法所在的class
	 * @param method  需要调用的方法名称
	 * @param params  方法的参数
	 */
	public static Object invoke(Object obj, Class type,
			String method, Object[] params)
			throws Exception
	{
		if (obj == null)
		{
			throw new IllegalArgumentException("Param obj is null!");
		}
		if (method == null)
		{
			throw new IllegalArgumentException("Param method is null!");
		}
		Class c;
		if ((c = type) == null)
		{
			if (obj instanceof Class)
			{
				c = (Class) obj;
			}
			else
			{
				c = obj.getClass();
			}
		}
		MethodContainer mc = getMethod(c, method, obj instanceof Class, params);
		if (mc == null)
		{
			throw new NoSuchMethodException(c.getName() + "." + method + "()");
		}
		Method m = mc.method;
		m.setAccessible(true);
		try
		{
			return m.invoke(obj, changeParams(params));
		}
		catch (InvocationTargetException ex)
		{
			Throwable target = ex.getTargetException();
			if (target instanceof Exception)
			{
				throw (Exception) target;
			}
			else if (target instanceof Error)
			{
				throw (Error) target;
			}
			else
			{
				throw ex;
			}
		}
	}

	private static final Object[] EMPTY_ARR = new Object[0];
	/**
	 * 对参数中CastValue类型的值进行转换.
	 */
	private static Object[] changeParams(Object[] params)
	{
		if (params == null || params.length == 0)
		{
			return EMPTY_ARR;
		}
		Object[] args = params;
		for (int i = 0; i < params.length; i++)
		{
			if (params[i] instanceof CastValue)
			{
				if (args == params)
				{
					args = new Object[params.length];
					System.arraycopy(params, 0, args, 0, params.length);
				}
				args[i] = ((CastValue) params[i]).originValue;
			}
		}
		return args;
	}

	/**
	 * 在给出了指定类的情况下, 获取需要的方法.
	 */
	private static MethodContainer getMethod(Class c, String method, boolean needStatic,
			Object[] params)
			throws Exception
	{
		Method[] mArr = c.getDeclaredMethods();
		int paramCount = params == null ? 0 : params.length;
		Method m = null;
		int match = Integer.MAX_VALUE;
		for (int i = 0; i < mArr.length; i++)
		{
			Class[] types = mArr[i].getParameterTypes();
			if (method.equals(mArr[i].getName()) && types.length == paramCount)
			{
				if (needStatic && !Modifier.isStatic(mArr[i].getModifiers()))
				{
					continue;
				}
				int tmp = checkTypes(types, params);
				if (tmp != -1 && tmp <= match)
				{
					m = mArr[i];
					match = tmp;
				}
			}
		}
		if (m != null && match == 0)
		{
			return new MethodContainer(m, match);
		}
		Class superClass = c.getSuperclass();
		if (superClass != null && superClass != Object.class)
		{
			MethodContainer mc = getMethod(superClass, method, needStatic, params);
			if (mc != null && mc.match < match)
			{
				return mc;
			}
		}
		return m != null ? new MethodContainer(m, match) : null;
	}

	/**
	 * 检查参数列表与给出的参数类型是否一致.
	 *
	 * @return  -1 表示不一致
	 *           0 表示完全一致
	 *          >1 表示匹配度, 数字越大匹配度越低
	 */
	private static int checkTypes(Class[] types, Object[] objs)
	{
		if (types.length == 0)
		{
			return 0;
		}
		int match = 0;
		nextType:
		for (int i = 0; i < types.length; i++)
		{
			if (objs[i] != null)
			{
				Class t = objs[i].getClass() ;
				if (t == CastValue.class)
				{
					t = ((CastValue) objs[i]).type;
				}
				if (t != types[i])
				{
					if (types[i].isAssignableFrom(t))
					{
						// 匹配的是父类, 需要降低匹配度
						match += getInheritLevel(t, types[i], 1);
						continue nextType;
					}
					if (types[i].isPrimitive())
					{
						Class[] tArr = (Class[]) wrapperIndex.get(t);
						if (tArr == null)
						{
							// 不是外覆类, 无法和基本类型匹配
							return -1;
						}
						for (int j = 0; j < tArr.length; j++)
						{
							if (tArr[j] == types[i])
							{
								match += j;
								continue nextType;
							}
						}
						// 此类型不匹配
						return -1;
					}
					else
					{
						// 如果不是基本类型, 则不匹配
						return -1;
					}
				}
			}
			else if (types[i].isPrimitive())
			{
				// 对于基本类型, 给出的参数不能为null
				return -1;
			}
		}
		return match;
	}

	/**
	 * 当父类为Object时需要降低的匹配度等级.
	 */
	private static final int ML = 5;
	/**
	 * 获取继承关系的层级.
	 */
	private static int getInheritLevel(Class c, Class type, int nowLevel)
	{
		Class p = c.getSuperclass();
		int minLevel;
		if (p == type)
		{
			if (type == Object.class)
			{
				minLevel = nowLevel + ML;
			}
			else
			{
				return nowLevel;
			}
		}
		else
		{
			minLevel = Integer.MAX_VALUE;
			if (p != null)
			{
				minLevel = getInheritLevel(p, type, nowLevel + 1);
			}
		}
		Class[] iArr = c.getInterfaces();
		for (int i = 0; i < iArr.length; i++)
		{
			int tmp;
			if (iArr[i] == type)
			{
				// 接口的匹配度需要降一级
				tmp = nowLevel + 1;
			}
			else
			{
				tmp = getInheritLevel(iArr[i], type, nowLevel + 1);
			}
			if (tmp < minLevel)
			{
				minLevel = tmp;
			}
		}
		return minLevel;
	}

	/**
	 * 获取一个对象或类的(静态)属性的值.
	 *
	 * @param obj    属性值所在的对象, 如果是静态属性则可直接给出其class
	 * @param field  需要获取的属性名称
	 */
	public static Object get(Object obj, String field)
			throws Exception
	{
		return get(obj, null, field);
	}

	/**
	 * 获取一个对象或类的(静态)属性的值. <p>
	 * 增加了一个class类型的参数用于指定属性在哪个类中, 如在A中有个私有属性i,
	 * B继承了A也有个私有属性i, 那如果直接给出B的实例那只能获取到B中的属性i,
	 * 如果需要获取A中的属性i, 那就需要在这里type参数中将class指定为A.
	 *
	 * @param obj    属性值所在的对象, 如果是静态属性则可直接给出其class
	 * @param type   属性所在的class
	 * @param field  需要获取的属性名称
	 */
	public static Object get(Object obj, Class type, String field)
			throws Exception
	{
		if (obj == null)
		{
			throw new IllegalArgumentException("Param obj is null!");
		}
		if (field == null)
		{
			throw new IllegalArgumentException("Param field is null!");
		}
		Class c;
		if ((c = type) == null)
		{
			if (obj instanceof Class)
			{
				c = (Class) obj;
			}
			else
			{
				c = obj.getClass();
			}
		}
		Field f = getField(c, field, obj instanceof Class);
		f.setAccessible(true);
		return f.get(obj);
	}

	/**
	 * 设置一个对象或类的(静态)属性的值.
	 *
	 * @param obj    属性值所在的对象, 如果是静态属性则可直接给出其class
	 * @param field  需要设置的属性名称
	 * @param value  需要设置的值
	 */
	public static void set(Object obj, String field, Object value)
			throws Exception
	{
		set(obj, null, field, value);
	}

	/**
	 * 设置一个对象或类的(静态)属性的值. <p>
	 * 增加了一个class类型的参数用于指定属性在哪个类中, 如在A中有个私有属性i,
	 * B继承了A也有个私有属性i, 那如果直接给出B的实例那只能设置B中的属性i, 如果
	 * 需要设置A中的属性i, 那就需要在这里type参数中将class指定为A.
	 *
	 * @param obj    属性值所在的对象, 如果是静态属性则可直接给出其class
	 * @param type   属性所在的class
	 * @param field  需要设置的属性名称
	 * @param value  需要设置的值
	 */
	public static void set(Object obj, Class type, String field,
			Object value)
			throws Exception
	{
		if (obj == null)
		{
			throw new IllegalArgumentException("Param obj is null!");
		}
		if (field == null)
		{
			throw new IllegalArgumentException("Param field is null!");
		}
		Class c;
		if ((c = type) == null)
		{
			if (obj instanceof Class)
			{
				c = (Class) obj;
			}
			else
			{
				c = obj.getClass();
			}
		}
		Field f = getField(c, field, obj instanceof Class);
		f.setAccessible(true);
		f.set(obj, value);
	}

	/**
	 * 在给出了指定类的情况下, 获取需要的属性.
	 */
	private static Field getField(Class c, String field, boolean needStatic)
			throws Exception
	{
		try
		{
			Field f = c.getDeclaredField(field);
			if (needStatic && !Modifier.isStatic(f.getModifiers()))
			{
				throw new NoSuchFieldException("Field " + c.getName() + "."
						+ f.getName() + " is't static.");
			}
			return f;
		}
		catch (Exception ex)
		{
			Class superClass = c.getSuperclass();
			if (superClass != null && superClass != Object.class)
			{
				return getField(superClass, field, needStatic);
			}
			throw ex;
		}
	}

	// 外覆类与基本类型互转的索引表
	private static Map wrapperIndex = new HashMap();
	static
	{
		wrapperIndex.put(Boolean.class, new Class[]{boolean.class});
		wrapperIndex.put(boolean.class, Boolean.class);
		wrapperIndex.put(Character.class, new Class[]{char.class, int.class, long.class, double.class, float.class});
		wrapperIndex.put(char.class, Character.class);
		wrapperIndex.put(Byte.class, new Class[]{byte.class, short.class, int.class, long.class, double.class, float.class});
		wrapperIndex.put(byte.class, Byte.class);
		wrapperIndex.put(Short.class, new Class[]{short.class, int.class, long.class, double.class, float.class});
		wrapperIndex.put(short.class, Short.class);
		wrapperIndex.put(Integer.class, new Class[]{int.class, long.class, double.class, float.class});
		wrapperIndex.put(int.class, Integer.class);
		wrapperIndex.put(Long.class, new Class[]{long.class, double.class, float.class});
		wrapperIndex.put(long.class, Long.class);
		wrapperIndex.put(Float.class, new Class[]{float.class, double.class});
		wrapperIndex.put(float.class, Float.class);
		wrapperIndex.put(Double.class, new Class[]{double.class});
		wrapperIndex.put(double.class, Double.class);
	}

	private static final class CastValue
	{
		private final Class type;
		private final Object originValue;
		public CastValue(Class type, Object value)
		{
			if (type == null)
			{
				throw new NullPointerException("type is null.");
			}
			boolean pType = type.isPrimitive();
			this.type = pType ? (Class) wrapperIndex.get(type) : type;
			if (value != null && !this.type.isInstance(value))
			{
				boolean mismatch = true;
				if (pType && type != boolean.class
						&& (value instanceof Number || value instanceof Character))
				{
					if (type == char.class)
					{
						// 如果type为char那value一定是Number类型
						value = new Character((char) ((Number) value).intValue());
						mismatch = false;
					}
					else
					{
						// 如果type不是char那一定是要转成Number中的某个类型
						if (value instanceof Character)
						{
							value = new Integer(((Character) value).charValue());
						}
						try
						{
							Method m = Number.class.getMethod(
									type.getName() + "Value", new Class[0]);
							value = m.invoke(value, EMPTY_ARR);
							mismatch = false;
						}
						catch (Exception ex) {}
					}
				}
				if (mismatch)
				{
					throw new IllegalArgumentException(value.getClass().getName()
							+ " can't cast to " + type.getName() + ".");
				}
			}
			this.originValue = value;
		}

	}

}

class MethodContainer
{
	Method method;
	int match;
	public MethodContainer(Method method, int match)
	{
		this.match = match;
		this.method = method;
	}

}
