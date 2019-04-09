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

package self.micromagic.cg.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;

import self.micromagic.cg.BeanTool;
import self.micromagic.cg.CGException;
import self.micromagic.cg.ClassGenerator;
import self.micromagic.cg.ClassKeyCache;
import self.micromagic.util.ResManager;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;

/**
 * 动态代理类的生成工具.
 * 如生成MethodProxy等.
 */
public class ProxyTool
{
	private static int METHOD_PROXY_ID = 1;

	private ProxyTool()
	{
	}

	/**
	 * 获取基本类型的默认值.
	 */
	public static Object getPrimitiveDefaultValue(Class type)
	{
		return primitiveDefaults.get(type);
	}

	/**
	 * 创建一个方法调用的代理.
	 *
	 * @param method      需要创建方法调用代理的目标方法
	 * @return  方法调用的代理
	 */
	public static MethodProxy createMethodProxy(Method method)
			throws CGException
	{
		return createMethodProxy(method, true);
	}

	/**
	 * 创建一个方法调用的代理.
	 *
	 * @param method      需要创建方法调用代理的目标方法
	 * @param paramCheck  是否需要检查参数类型
	 * @return  方法调用的代理
	 */
	public static MethodProxy createMethodProxy(Method method, boolean paramCheck)
			throws CGException
	{
		Class c = method.getDeclaringClass();
		MethodProxy proxy = getCachedMethodProxy(c, method, paramCheck);
		if (proxy != null)
		{
			return proxy;
		}
		// 判断目标类是否在当前ClassLoader或子ClassLoader中
		boolean subClass = checkClassLoader(c.getClassLoader());
		if (subClass)
		{
			if (Modifier.isPrivate(method.getModifiers()))
			{
				throw new CGException("The method [" + method + "] is private.");
			}
			if (Modifier.isPrivate(c.getModifiers()))
			{
				throw new CGException("The method [" + method + "]'s declaring type [" + c + "] is private.");
			}
			Class[] params = method.getParameterTypes();
			for (int i = 0; i < params.length; i++)
			{
				if (Modifier.isPrivate(params[i].getModifiers()))
				{
					throw new CGException("The method [" + method + "]'s param(" + i + ") [" + c + "] is private.");
				}
			}
		}
		else
		{
			if (!Modifier.isPublic(method.getModifiers()))
			{
				throw new CGException("The method [" + method + "] isn't public.");
			}
			if (!Modifier.isPublic(c.getModifiers()))
			{
				throw new CGException("The method [" + method + "]'s declaring type [" + c + "] isn't public.");
			}
			Class[] params = method.getParameterTypes();
			for (int i = 0; i < params.length; i++)
			{
				if (!Modifier.isPublic(params[i].getModifiers()))
				{
					throw new CGException("The method [" + method + "]'s param(" + i + ") [" + c + "] isn't public.");
				}
			}
		}
		synchronized (methodProxyCache)
		{
			proxy = createMethodProxy0(c, method, paramCheck, !subClass);
		}
		return proxy;
	}
	private static MethodProxy createMethodProxy0(Class c, Method method, boolean paramCheck,
			boolean useThisClassLoader)
	{
		MethodProxy proxy = getCachedMethodProxy(c, method, paramCheck);
		if (proxy != null)
		{
			return proxy;
		}
		boolean staticMethod = Modifier.isStatic(method.getModifiers());
		Class[] params = method.getParameterTypes();

		ClassGenerator cg = new ClassGenerator();
		cg.setClassLoader(useThisClassLoader ? MethodProxy.class.getClassLoader() : c.getClassLoader());
		String namePrefix = c.getName();
		if (namePrefix.startsWith("java."))
		{
			namePrefix = "cg." + namePrefix;
		}
		cg.setClassName(namePrefix + "_$" + method.getName() + "$Proxy" + METHOD_PROXY_ID++);
		cg.addInterface(MethodProxy.class);
		cg.addClassPath(c);
		cg.addClassPath(MethodProxy.class);
		StringAppender buf = StringTool.createStringAppender(128);
		codeRes.printRes("methodProxy.invoke.declare", null, 0, buf).appendln().append('{').appendln();

		// 生成参数的转换
		String paramCode, paramCodePrimitive;
		Map paramCodeParams = new HashMap();
		if (paramCheck)
		{
			if (!staticMethod)
			{
				paramCodeParams.put("type", ClassGenerator.getClassName(c));
				codeRes.printRes("methodProxy.check.target", paramCodeParams, 1, buf).appendln();
			}
			if (params.length > 0)
			{
				paramCodeParams.put("paramCount", Integer.toString(params.length));
				codeRes.printRes("methodProxy.check.args", paramCodeParams, 1, buf).appendln();
			}
			paramCode = "methodProxy.param.cast.withCheck";
			paramCodePrimitive = "methodProxy.param.cast.primitive.withCheck";
		}
		else
		{
			paramCode = "methodProxy.param.cast.withDeclare";
			paramCodePrimitive = "methodProxy.param.cast.primitive.withDeclare";
		}
		for (int i = 0; i < params.length; i++)
		{
			Class type = params[i];
			paramCodeParams.put("type", ClassGenerator.getClassName(type));
			paramCodeParams.put("index", Integer.toString(i));
			if (type.isPrimitive())
			{
				if (paramCheck)
				{
					// 处理基本类型的检查代码
					String[] types = (String[]) primitiveCheckCache.get(type.getName());
					StringAppender checkBuf = StringTool.createStringAppender(80);
					for (int j = 0; j < types.length; j++)
					{
						if (j == 0)
						{
							paramCodeParams.put("elseKey", "");
						}
						else
						{
							paramCodeParams.put("elseKey", "else ");
						}
						paramCodeParams.put("wrapType", BeanTool.getPrimitiveWrapClassName(types[j]));
						paramCodeParams.put("tempType", types[j]);
						codeRes.printRes("methodProxy.param.cast.primitive.withCheck.doCheck", paramCodeParams, 0, checkBuf);
						if (j < types.length - 1)
						{
							checkBuf.appendln();
						}
					}
					paramCodeParams.put("primitiveTypeCheck", checkBuf.toString());
				}
				paramCodeParams.put("wrapType", BeanTool.getPrimitiveWrapClassName(type.getName()));
				codeRes.printRes(paramCodePrimitive, paramCodeParams, 1, buf).appendln();
			}
			else
			{
				codeRes.printRes(paramCode, paramCodeParams, 1, buf).appendln();
			}
		}

		// 生成调用及返回的代码
		Class rType = method.getReturnType();
		Map returnCodeParams = new HashMap();
		if (staticMethod)
		{
			returnCodeParams.put("target", ClassGenerator.getClassName(c));
		}
		else
		{
			returnCodeParams.put("target", "((" + ClassGenerator.getClassName(c) + ") target)");
		}
		returnCodeParams.put("method", method.getName());
		StringAppender paramsBuf = StringTool.createStringAppender(params.length * 8);
		for (int i = 0; i < params.length; i++)
		{
			if (i > 0)
			{
				paramsBuf.append(", ");
			}
			paramsBuf.append("param").append(i);
		}
		returnCodeParams.put("params", paramsBuf.toString());
		if (rType == void.class)
		{
			codeRes.printRes("methodProxy.doInvoke.void", returnCodeParams, 1, buf).appendln();
		}
		else if (rType.isPrimitive())
		{
			returnCodeParams.put("wrapType", BeanTool.getPrimitiveWrapClassName(rType.getName()));
			codeRes.printRes("methodProxy.doInvoke.primitive", returnCodeParams, 1, buf).appendln();
		}
		else
		{
			codeRes.printRes("methodProxy.doInvoke", returnCodeParams, 1, buf).appendln();
		}

		buf.append('}');
		cg.addMethod(buf.toString());
		try
		{
			Object obj = cg.createClass().newInstance();
			proxy = (MethodProxy) obj;
			putMethodProxy(c, method, paramCheck, proxy);
		}
		catch (Exception ex)
		{
			proxy = checkGeneratedClass(c, method, paramCheck);
			if (proxy == null)
			{
				if (ex instanceof RuntimeException)
				{
					throw (RuntimeException) ex;
				}
				throw new CGException(ex);
			}
		}
		return proxy;
	}

	/**
	 * 判断所给出的类是否为自动生成的类.
	 * 即无法找到类定义文件.
	 */
	public static boolean isGeneratedClass(Class c)
	{
		String name = c.getName();
		return c.getResource("/".concat(name.replace('.', '/')).concat(".class")) == null;
	}

	/**
	 * 检查是否为自动生成的类.
	 * 如果是则生成反射的方法代理, 并将其添加到缓存中.
	 */
	private static MethodProxy checkGeneratedClass(Class c, Method method, boolean paramCheck)
	{
		MethodProxy proxy = null;
		if (isGeneratedClass(c))
		{
			// 类文件不存在, 说明是通过其他工具自动生成的类
			proxy = new ReflectMethodProxy(method);
			putMethodProxy(c, method, paramCheck, proxy);
		}
		return proxy;
	}

	/**
	 * 检查目标类的ClassLoader是否在当前的ClassLoader之下或相同.
	 */
	private static boolean checkClassLoader(ClassLoader cl)
	{
		if (cl == null)
		{
			return false;
		}
		ClassLoader thisCL = MethodProxy.class.getClassLoader();
		do
		{
			if (cl == thisCL)
			{
				return true;
			}
			cl = cl.getParent();
		} while (cl != null);
		return false;
	}

	/**
	 * 获取缓存的方法调用代理.
	 */
	private static MethodProxy getCachedMethodProxy(Class c, Method method, boolean paramCheck)
	{
		Map methodCache = (Map) methodProxyCache.getProperty(c);
		if (methodCache == null)
		{
			return null;
		}
		MethodProxyKey key = new MethodProxyKey(method.getName(), paramCheck, method.getParameterTypes());
		return (MethodProxy) methodCache.get(key);
	}

	/**
	 * 将方法调用代理放入缓存.
	 */
	private static void putMethodProxy(Class c, Method method, boolean paramCheck, MethodProxy proxy)
	{
		Map methodCache = (Map) methodProxyCache.getProperty(c);
		if (methodCache == null)
		{
			methodCache = new HashMap();
			methodProxyCache.setProperty(c, methodCache);
		}
		MethodProxyKey key = new MethodProxyKey(method.getName(), paramCheck, method.getParameterTypes());
		methodCache.put(key, proxy);
	}

	/**
	 * 存放方法调用代理的缓存.
	 */
	private static ClassKeyCache methodProxyCache = ClassKeyCache.getInstance();

	/**
	 * 用于记录日志.
	 */
	static final Log log = Utility.createLog("eterna.dc");

	/**
	 * 基本类型需要检查的类型的对应表.
	 */
	static final Map primitiveCheckCache = new HashMap();

	/**
	 * 基本类型需要检查的类型的对应表.
	 */
	static final Map primitiveDefaults = new HashMap();

	/**
	 * 代码段资源.
	 */
	static ResManager codeRes = new ResManager();

	/**
	 * 初始化代码资源及各种类型对应的转换器.
	 */
	static
	{
		try
		{
			codeRes.load(ProxyTool.class.getResourceAsStream("ProxyTool.res"));
		}
		catch (Exception ex)
		{
			log.error("Error in get code res.", ex);
		}
		primitiveCheckCache.put("boolean", new String[]{"boolean"});
		primitiveCheckCache.put("char", new String[]{"char"});
		primitiveCheckCache.put("byte", new String[]{"byte"});
		primitiveCheckCache.put("short", new String[]{"short", "byte"});
		primitiveCheckCache.put("int", new String[]{"int", "char", "short", "byte"});
		primitiveCheckCache.put("long", new String[]{"long", "int", "char", "short", "byte"});
		primitiveCheckCache.put("float", new String[]{"float", "int", "long", "char", "short", "byte"});
		primitiveCheckCache.put("double", new String[]{"double", "float", "int", "long", "char", "short", "byte"});

		primitiveDefaults.put(boolean.class, Boolean.FALSE);
		primitiveDefaults.put(char.class, new Character((char) 0));
		primitiveDefaults.put(byte.class, new Byte((byte) 0));
		primitiveDefaults.put(short.class, new Short((short) 0));
		primitiveDefaults.put(int.class, Utility.INTEGER_0);
		primitiveDefaults.put(long.class, new Long(0L));
		primitiveDefaults.put(float.class, new Float(0.0f));
		primitiveDefaults.put(double.class, new Double(0.0));
	}

}


/**
 * 方法调用代理存放的键值.
 */
class MethodProxyKey
{
	private final String methodName;
	private final boolean paramCheck;
	private final Class[] params;
	private int hash = -1;

	public MethodProxyKey(String methodName, boolean paramCheck, Class[] params)
	{
		this.methodName = methodName;
		this.paramCheck = paramCheck;
		this.params = params;
	}

	public boolean equals(Object obj)
	{
		if (obj instanceof MethodProxyKey)
		{
			MethodProxyKey key = (MethodProxyKey) obj;
			if (this.methodName.equals(key.methodName) && this.paramCheck == key.paramCheck)
			{
				Class[] params1 = this.params;
				Class[] params2 = key.params;
				if (params1.length == params2.length)
				{
					for (int i = 0; i < params1.length; i++)
					{
						if (params1[i] != params2[i])
						{
							return false;
						}
					}
					return true;
				}
			}
		}
		return false;
	}

	public int hashCode()
	{
		if (this.hash == -1)
		{
			this.hash = ("method:".concat(this.methodName).hashCode()) ^ (this.params.length << 12)
					^ (this.paramCheck ? 1231 : 1237);
		}
		return this.hash;
	}

}
