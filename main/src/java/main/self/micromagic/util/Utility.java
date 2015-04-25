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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.rmi.server.UID;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import self.micromagic.util.ref.StringRef;

/**
 * @author micromagic@sina.com
 */
public class Utility
{
	public static final int MEMORY_CACHE_SIZE_THRESHOLD = 1024 * 1024 * 8;

	public static final String CHARSET_TAG = "_charset";

	/**
	 * 配置文件名.
	 */
	public static final String PROPERTIES_NAME = PropertiesManager.PROPERTIES_NAME;

	/**
	 * 配置在处理文本的动态属性时, 是否要显示处理失败的信息
	 */
	public static final String SHOW_RDP_FAIL_PROPERTY = "show.rdp.fail";

	public static final Integer INTEGER_MINUS1 = new Integer(-1);
	public static final Integer INTEGER_0 = new Integer(0);
	public static final Integer INTEGER_1 = new Integer(1);
	public static final Integer INTEGER_2 = new Integer(2);
	public static final Integer INTEGER_3 = new Integer(3);
	public static final Integer INTEGER_4 = new Integer(4);
	public static final Integer INTEGER_5 = new Integer(5);
	public static final Integer INTEGER_6 = new Integer(6);
	public static final Integer INTEGER_7 = new Integer(7);
	public static final Integer INTEGER_8 = new Integer(8);
	public static final Integer INTEGER_9 = new Integer(9);
	public static final Integer INTEGER_10 = new Integer(10);
	public static final Integer INTEGER_11 = new Integer(11);
	public static final Integer INTEGER_12 = new Integer(12);
	public static final Integer INTEGER_13 = new Integer(13);
	public static final Integer INTEGER_14 = new Integer(14);
	public static final Integer INTEGER_15 = new Integer(15);

	public static final Integer[] INTEGER_ARRAY = new Integer[]{
		INTEGER_0, INTEGER_1, INTEGER_2, INTEGER_3, INTEGER_4,
		INTEGER_5, INTEGER_6, INTEGER_7, INTEGER_8, INTEGER_9,
		INTEGER_10, INTEGER_11, INTEGER_12, INTEGER_13,
		INTEGER_14, INTEGER_15
	};

	public static final String LINE_SEPARATOR;

	/**
	 * 复制数据时默认的缓存大小.
	 */
	private static final int DEFAULT_BUFSIZE = 512;

	/**
	 * 在处理文本的动态属性时, 是否要显示处理失败的信息
	 */
	static boolean SHOW_RDP_FAIL = false;

	private static PropertiesManager propertiesManager;
	private static DataSource dataSource;

	static
	{
		try
		{
			propertiesManager = new PropertiesManager();
		}
		catch (Throwable ex)
		{
			System.err.println(FormatTool.getCurrentDatetimeString()
					+ ": Error when init Utility.");
			ex.printStackTrace(System.err);
		}

		String nextLine = "\n";
		try
		{
			nextLine = System.getProperty("line.separator", "\n");
			Utility.addFieldPropertyManager(SHOW_RDP_FAIL_PROPERTY, Utility.class, "SHOW_RDP_FAIL");
		}
		catch (Throwable ex)
		{
			System.err.println(FormatTool.getCurrentDatetimeString()
					+ ": Error when init Utility.");
			ex.printStackTrace(System.err);
		}
		LINE_SEPARATOR = nextLine;
	}

	/**
	 * 重新载入配置
	 *
	 * @param msg   载入配置时的出错信息
	 */
	public static void reload(StringRef msg)
	{
		propertiesManager.reload(msg);
	}

	public static String getUID()
	{
		return new UID().toString().replace(':', '_').replace('-', '_');
	}

	public static Log createLog(String name)
	{
		if ("true".equalsIgnoreCase(propertiesManager.getProperty(USE_JDK_LOG_FLAG)))
		{
			return new Jdk14Factory().getInstance(name);
		}
		else
		{
			return LogFactory.getLog(name);
		}
	}
	public static final String USE_JDK_LOG_FLAG = "useJdkLog";

	/**
	 * 从当前线程的上下文环境中获取ClassLoader, 如果不存在则给出Utility类
	 * 所属的ClassLoader.
	 */
	public static ClassLoader getContextClassLoader()
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		return cl != null ? cl : Utility.class.getClassLoader();
	}

	public static Integer createInteger(int i)
	{
		return i >= 0 && i <= 15 ? INTEGER_ARRAY[i] : new Integer(i);
	}

	/**
	 * 获取工具类中的属性管理器.
	 */
	public static PropertiesManager getPropertiesManager()
	{
		return propertiesManager;
	}

	public static String getProperty(String key)
	{
		return propertiesManager.getProperty(key);
	}

	public static String getProperty(String key, String defaultValue)
	{
		return propertiesManager.getProperty(key, defaultValue);
	}

	public static void setProperty(String key, String value)
	{
		propertiesManager.setProperty(key, value);
	}

	public static void removeProperty(String key)
	{
		propertiesManager.removeProperty(key);
	}

	/**
	 * 判断两个对象是否相同. <p>
	 * 如:
	 * null == null
	 * obj != null
	 * null != obj
	 * obj1 != obj2
	 * obj1 == obj1
	 */
	public static boolean objectEquals(Object obj1, Object obj2)
	{
		return obj1 == obj2 || (obj1 != null && obj2 != null && obj1.equals(obj2));
	}

	/**
	 * 添加一个配置监控者, 当配置的值改变时, 它会自动更新指定类的静态属性成员, 该属性
	 * 的类型可以是: <code>String</code>, <code>int</code>或<code>boolean</code>.
	 *
	 * @param key            配置的键值
	 * @param theClass       被修改的属性所在的类
	 * @param fieldName      需要被修改的静态属性名称
	 */
	public static void addFieldPropertyManager(String key, Class theClass, String fieldName)
			throws NoSuchFieldException
	{
		propertiesManager.addFieldPropertyManager(key, theClass, fieldName, null);
	}

	/**
	 * 添加一个配置监控者, 当配置的值改变时, 它会自动更新指定类的静态属性成员, 该属性
	 * 的类型可以是: <code>String</code>, <code>int</code>或<code>boolean</code>.
	 *
	 * @param key            配置的键值
	 * @param theClass       被修改的属性所在的类
	 * @param fieldName      需要被修改的静态属性名称
	 * @param defaultValue   当配置中不存在指定的键值时使用的默认值
	 */
	public static void addFieldPropertyManager(String key, Class theClass, String fieldName,
			String defaultValue)
			throws NoSuchFieldException
	{
		propertiesManager.addFieldPropertyManager(key, theClass, fieldName, defaultValue);
	}

	/**
	 * 移除一个配置监控者.
	 *
	 * @param key            配置的键值
	 * @param theClass       被修改的属性所在的类
	 * @param fieldName      需要被修改的静态属性名称
	 */
	public static void removeFieldPropertyManager(String key, Class theClass, String fieldName)
			throws NoSuchFieldException
	{
		propertiesManager.removeFieldPropertyManager(key, theClass, fieldName);
	}

	/**
	 * 添加一个配置监控者, 当配置的值改变时, 它会自动调用指定类的静态方法,
	 * 此方法必须是只有一个<code>String</code>类型的参数.
	 *
	 * @param key            配置的键值
	 * @param theClass       被调用的方法所在的类
	 * @param methodName     需要被调用的静态方法名称
	 */
	public static void addMethodPropertyManager(String key, Class theClass, String methodName)
			throws NoSuchMethodException
	{
		propertiesManager.addMethodPropertyManager(key, theClass, methodName, null);
	}

	/**
	 * 添加一个配置监控者, 当配置的值改变时, 它会自动调用指定类的静态方法,
	 * 此方法必须是只有一个<code>String</code>类型的参数.
	 *
	 * @param key            配置的键值
	 * @param theClass       被调用的方法所在的类
	 * @param methodName     需要被调用的静态方法名称
	 * @param defaultValue   当配置中不存在指定的键值时使用的默认值
	 */
	public static void addMethodPropertyManager(String key, Class theClass, String methodName,
			String defaultValue)
			throws NoSuchMethodException
	{
		propertiesManager.addMethodPropertyManager(key, theClass, methodName, defaultValue);
	}

	/**
	 * 移除一个配置监控者.
	 *
	 * @param key            配置的键值
	 * @param theClass       被调用的方法所在的类
	 * @param methodName     需要被调用的静态方法名称
	 */
	public static void removeMethodPropertyManager(String key, Class theClass, String methodName)
			throws NoSuchMethodException
	{
		propertiesManager.removeMethodPropertyManager(key, theClass, methodName);
	}

	/**
	 * 添加一个配置变更的监听者.
	 */
	public static synchronized void addPropertyListener(PropertiesManager.PropertyListener l)
	{
		propertiesManager.addPropertyListener(l);
	}

	/**
	 * 移除一个配置变更的监听者.
	 */
	public static synchronized void removePropertyListener(PropertiesManager.PropertyListener l)
	{
		propertiesManager.addPropertyListener(l);
	}

	/**
	 * 将in中的值全部复制到out中, 但是不关闭in和out.
	 */
	public static void copyStream(InputStream in, OutputStream out)
			throws IOException
	{
		copyStream(in, out, new byte[DEFAULT_BUFSIZE]);
	}

	/**
	 * 将in中的值部分复制到out中(复制limit个字节), 但是不关闭in和out.
	 *
	 * @param limit   复制的字节个数, 如果为-1, 则表示没有限制
	 *
	 * @return   实际复制的字节数, 如果参数limit设置为-1, 则不会计算实际复制的个数,
	 *           返回值为-1
	 */
	public static int copyStream(int limit, InputStream in, OutputStream out)
			throws IOException
	{
		return copyStream(limit, in, out, new byte[DEFAULT_BUFSIZE]);
	}

	/**
	 * 将in中的值全部复制到out中, 但是不关闭in和out.
	 *
	 * @param bufSize  复制时使用的缓存的大小
	 */
	public static void copyStream(InputStream in, OutputStream out, int bufSize)
			throws IOException
	{
		bufSize = bufSize <= 0 ? DEFAULT_BUFSIZE : bufSize;
		copyStream(in, out, new byte[bufSize]);
	}

	/**
	 * 将in中的值全部复制到out中, 但是不关闭in和out.
	 *
	 * @param buf  复制时使用的缓存
	 */
	public static void copyStream(InputStream in, OutputStream out, byte[] buf)
			throws IOException
	{
		int readCount;
		while ((readCount = in.read(buf)) >= 0)
		{
			out.write(buf, 0, readCount);
		}
	}

	/**
	 * 将in中的值部分复制到out中(复制limit个字节), 但是不关闭in和out.
	 *
	 * @param limit     复制的字节个数, 如果为-1, 则表示没有限制
	 * @param bufSize   复制时使用的缓存的大小
	 *
	 * @return   实际复制的字节数, 如果参数limit设置为-1, 则不会计算实际复制的个数,
	 *           返回值为-1
	 */
	public static int copyStream(int limit, InputStream in, OutputStream out, int bufSize)
			throws IOException
	{
		if (limit == -1)
		{
			copyStream(in, out, bufSize);
			return -1;
		}
		if (limit < 0)
		{
			throw new IllegalArgumentException("Error limit:" + limit);
		}
		if (limit == 0)
		{
			return 0;
		}
		bufSize = bufSize <= 0 ? DEFAULT_BUFSIZE : bufSize;
		byte[] buf = new byte[limit > bufSize ? bufSize : limit];
		return copyStream(limit, in, out, buf);
	}

	/**
	 * 将in中的值部分复制到out中(复制limit个字节), 但是不关闭in和out.
	 *
	 * @param limit     复制的字节个数, 如果为-1, 则表示没有限制
	 * @param buf       复制时使用的缓存
	 *
	 * @return   实际复制的字节数, 如果参数limit设置为-1, 则不会计算实际复制的个数,
	 *           返回值为-1
	 */
	public static int copyStream(int limit, InputStream in, OutputStream out, byte[] buf)
			throws IOException
	{
		if (limit == -1)
		{
			copyStream(in, out, buf);
			return -1;
		}
		if (limit < 0)
		{
			throw new IllegalArgumentException("Error limit:" + limit);
		}
		int allCount = 0;
		int left = limit;
		int rCount;
		while (left > 0 && (rCount = in.read(buf, 0, buf.length > left ? left : buf.length)) >= 0)
		{
			out.write(buf, 0, rCount);
			allCount += rCount;
			left -= rCount;
		}
		return allCount;
	}

	/**
	 * 将in中的值全部复制到out中, 但是不关闭in和out.
	 */
	public static void copyChars(Reader in, Writer out)
			throws IOException
	{
		copyChars(in, out, new char[DEFAULT_BUFSIZE]);
	}

	/**
	 * 将in中的值部分复制到out中(复制limit个字节), 但是不关闭in和out.
	 *
	 * @param limit   复制的字节个数, 如果为-1, 则表示没有限制
	 *
	 * @return   实际复制的字节数, 如果参数limit设置为-1, 则不会计算实际复制的个数,
	 *           返回值为-1
	 */
	public static int copyChars(int limit, Reader in, Writer out)
			throws IOException
	{
		return copyChars(limit, in, out, new char[DEFAULT_BUFSIZE]);
	}

	/**
	 * 将in中的值全部复制到out中, 但是不关闭in和out.
	 *
	 * @param bufSize  复制时使用的缓存的大小
	 */
	public static void copyChars(Reader in, Writer out, int bufSize)
			throws IOException
	{
		bufSize = bufSize <= 0 ? DEFAULT_BUFSIZE : bufSize;
		copyChars(in, out, new char[bufSize]);
	}

	/**
	 * 将in中的值全部复制到out中, 但是不关闭in和out.
	 *
	 * @param buf  复制时使用的缓存
	 */
	public static void copyChars(Reader in, Writer out, char[] buf)
			throws IOException
	{
		int readCount;
		while ((readCount = in.read(buf)) >= 0)
		{
			out.write(buf, 0, readCount);
		}
	}

	/**
	 * 将in中的值部分复制到out中(复制limit个字节), 但是不关闭in和out.
	 *
	 * @param limit     复制的字节个数, 如果为-1, 则表示没有限制
	 * @param bufSize   复制时使用的缓存的大小
	 *
	 * @return   实际复制的字节数, 如果参数limit设置为-1, 则不会计算实际复制的个数,
	 *           返回值为-1
	 */
	public static int copyChars(int limit, Reader in, Writer out, int bufSize)
			throws IOException
	{
		if (limit == -1)
		{
			copyChars(in, out, bufSize);
			return -1;
		}
		if (limit < 0)
		{
			throw new IllegalArgumentException("Error limit:" + limit);
		}
		if (limit == 0)
		{
			return 0;
		}
		bufSize = bufSize <= 0 ? DEFAULT_BUFSIZE : bufSize;
		char[] buf = new char[limit > bufSize ? bufSize : limit];
		return copyChars(limit, in, out, buf);
	}

	/**
	 * 将in中的值部分复制到out中(复制limit个字节), 但是不关闭in和out.
	 *
	 * @param limit     复制的字节个数, 如果为-1, 则表示没有限制
	 * @param buf       复制时使用的缓存
	 *
	 * @return   实际复制的字节数, 如果参数limit设置为-1, 则不会计算实际复制的个数,
	 *           返回值为-1
	 */
	public static int copyChars(int limit, Reader in, Writer out, char[] buf)
			throws IOException
	{
		if (limit == -1)
		{
			copyChars(in, out, buf);
			return -1;
		}
		if (limit < 0)
		{
			throw new IllegalArgumentException("Error limit:" + limit);
		}
		int allCount = 0;
		int left = limit;
		int rCount;
		while (left > 0 && (rCount = in.read(buf, 0, buf.length > left ? left : buf.length)) >= 0)
		{
			out.write(buf, 0, rCount);
			allCount += rCount;
			left -= rCount;
		}
		return allCount;
	}

	public static DataSource getDataSource()
	{
		DataSource tmpDS = dataSource;
		if (tmpDS != null)
		{
			return tmpDS;
		}
		synchronized (Utility.class)
		{
			tmpDS = dataSource;
			if (tmpDS != null)
			{
				return tmpDS;
			}
			System.out.println("Start creat datasource.");

			String className = getProperty("dataSource.className");
			if (className != null && className.length() > 0)
			{
				System.out.println("Creat datasource:" + className + ".");
			}
			else
			{
				className = "org.apache.struts.legacy.GenericDataSource";
				System.out.println("Creat default datasource:" + className + ".");
			}

			try
			{
				Class c = Class.forName(className);
				tmpDS = (DataSource) c.newInstance();
				setDataSourceProperties(tmpDS, propertiesManager);
			}
			catch (Exception ex)
			{
				System.out.println("Error! Creat datasource:" + className + " message:" + ex.getMessage());
			}
			return dataSource = tmpDS;
		}
	}

	private static final String[] DATASOURCE_PROPERTIES = {
		"description", "String", "driverClass", "String", "maxCount", "int", "minCount", "int",
		"url", "String", "user", "String", "password", "String", "autoCommit", "boolean"
	};
	/**
	 * 单个String类型的参数.
	 */
	static Class[] STR_PARAM = {String.class};

	private static void setDataSourceProperties(DataSource dataSource, PropertiesManager prop)
			throws Exception
	{
		Class c = dataSource.getClass();
		for (int i = 0; i < DATASOURCE_PROPERTIES.length; i += 2)
		{
			String name = DATASOURCE_PROPERTIES[i];
			String type = DATASOURCE_PROPERTIES[i + 1];
			String value = prop.getProperty("dataSource." + name);
			if (value != null)
			{
				value = resolveDynamicPropnames(value);
				String fName = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
				Class[] params = STR_PARAM;
				if ("int".equals(type))
				{
					params = new Class[]{int.class};
				}
				else if ("boolean".equals(type))
				{
					params = new Class[]{boolean.class};
				}
				try
				{
					Method m = c.getDeclaredMethod(fName, params);
					Object v = value;
					if ("int".equals(type))
					{
						v = new Integer(value);
					}
					else if ("boolean".equals(type))
					{
						v = new Boolean(value);
					}
					m.invoke(dataSource, new Object[]{v});
				}
				catch (Throwable ex)
				{
					System.out.println("Invoke method:[" + fName + "] error, in datasource:"
							+ dataSource.getClass().getName() + ".");
				}
			}
		}
	}

	/**
	 * 处理文本中"${...}"的动态属性, 将他们替换成配置文件
	 * (eterna.config 或 System.property)中的对应值.
	 *
	 * @param text      要处理的文本
	 * @return 处理完的文本
	 * @see PropertiesManager#resolveDynamicPropnames(String)
	 */
	public static String resolveDynamicPropnames(String text)
	{
		return propertiesManager.resolveDynamicPropnames(text, null, false);
	}

	/**
	 * 处理文本中"${...}"的动态属性, 将他们替换成配置文件
	 * (bindRes 或 eterna.config 或 System.property)中的对应值.
	 *
	 * @param text      要处理的文本
	 * @param bindRes   绑定的资源, 会先在bindRes寻找对应的值
	 * @return 处理完的文本
	 * @see PropertiesManager#resolveDynamicPropnames(String, Map)
	 */
	public static String resolveDynamicPropnames(String text, Map bindRes)
	{
		return propertiesManager.resolveDynamicPropnames(text, bindRes, false);
	}

	/**
	 * 处理文本中"${...}"的动态属性, 将他们替换成配置文件
	 * (bindRes 或 eterna.config 或 System.property)中的对应值.
	 *
	 * @param text      要处理的文本
	 * @param bindRes   绑定的资源, 会先在bindRes寻找对应的值
	 * @param onlyRes   设置为<code>true</code>时, 只对绑定的资源进行处理, 设置为
	 *                  <code>false</code>时, 如果绑定的资源中不存在对应的值会再到
	 *                  eterna.config 或 System.property中寻找
	 * @return 处理完的文本
	 * @see PropertiesManager#resolveDynamicPropnames(String, Map, boolean)
	 */
	public static String resolveDynamicPropnames(String text, Map bindRes, boolean onlyRes)
	{
		return propertiesManager.resolveDynamicPropnames(text, bindRes, onlyRes);
	}

}