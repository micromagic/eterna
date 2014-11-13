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

package self.micromagic.dc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;

import self.micromagic.cg.ClassGenerator;
import self.micromagic.cg.ClassKeyCache;
import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.digester.FactoryManager;
import self.micromagic.eterna.share.Factory;
import self.micromagic.eterna.share.Generator;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;
import self.micromagic.util.container.SynHashMap;
import self.micromagic.util.container.ThreadCache;

/**
 * 动态类的生成工具.
 *
 * @author micromagic@sina.com
 */
public class CodeClassTool
{
	/**
	 * 在线程缓存中存储的错误信息列表的名称, 类型为List.
	 */
	public static final String CODE_ERRORS_FLAG = "codeErrors";

	/**
	 * 生成的代码的编号序列.
	 */
	private static volatile int CODE_ID = 1;

	/**
	 * 生成的代码的缓存. <p>
	 * 以baseClass为主键进行缓存, 值为一个map, 为继承这个类的代码集.
	 */
	private static ClassKeyCache codeCache = ClassKeyCache.getInstance();

	/**
	 * 根据动态代码生成一个类.
	 *
	 * @param baseClass       生成的类所继承的类
	 * @param interfaceClass  生成的类所实现的接口
	 * @param methodHead      要生成的方法的头部, 包括方法名 参数列表 抛出的异常
	 * @param bodyCode        方法的代码
	 * @param imports         需要引用的类路径列表
	 * @return                生成出来的类
	 */
	public static synchronized Class createJavaCodeClass(Class baseClass, Class interfaceClass,
			String methodHead, String bodyCode, String[] imports)
			throws Exception
	{
		if (baseClass == null)
		{
			baseClass = CodeClassTool.class;
		}
		Map cache;
		cache = (Map) codeCache.getProperty(baseClass);
		if (cache == null)
		{
			cache = new HashMap();
			codeCache.setProperty(baseClass, cache);
		}
		CodeKey key = new CodeKey(methodHead, bodyCode);
		Class codeClass = (Class) cache.get(key);
		if (codeClass != null)
		{
			return codeClass;
		}

		ClassGenerator cg = new ClassGenerator();
		Iterator itr = pathClassCache.entrySet().iterator();
		while (itr.hasNext())
		{
			Map.Entry entry = (Map.Entry) itr.next();
			cg.addClassPath((Class) entry.getKey());
		}
		cg.addClassPath(baseClass);
		cg.addClassPath(interfaceClass);
		cg.addClassPath(CodeClassTool.class);
		String nameSuffix = "$$EDC_" + (CODE_ID++);
		cg.setClassName("dc." + baseClass.getName() + nameSuffix);
		cg.addInterface(interfaceClass);
		cg.setSuperClass(baseClass);
		cg.importPackage("self.micromagic.util");
		cg.importPackage("self.micromagic.eterna.sql");
		cg.importPackage("self.micromagic.eterna.model");
		cg.importPackage("self.micromagic.eterna.search");
		cg.importPackage("self.micromagic.eterna.security");
		cg.importPackage("java.util");
		cg.importPackage("java.sql");
		if (imports != null)
		{
			for (int i = 0; i < imports.length; i++)
			{
				cg.importPackage(imports[i]);
			}
		}
		cg.setClassLoader(baseClass.getClassLoader());
		StringAppender tmpCode = StringTool.createStringAppender(bodyCode.length() + 32);
		tmpCode.append(methodHead).appendln().append('{').appendln();
		tmpCode.append(bodyCode);
		tmpCode.appendln().append('}');
		cg.addMethod(tmpCode.toString());
		if (DC_COMPILE_TYPE != null)
		{
			cg.setCompileType(DC_COMPILE_TYPE);
		}
		codeClass = cg.createClass();
		cache.put(key, codeClass);
		return codeClass;
	}

	/**
	 * 路径类的缓存, 主键为路径类, 值为此路径类的ClassLoader.
	 */
	private static Map pathClassCache = new SynHashMap(8, SynHashMap.WEAK);

	/**
	 * 注册一个用于读取类信息的路径类. <p>
	 * 可以通过这个路径类来获取代码中使用到的类文件.
	 *
	 * @param pathClass   读取类信息的路径类
	 */
	public static void registerPathClass(Class pathClass)
	{
		if (pathClass != null)
		{
			pathClassCache.put(pathClass, Boolean.TRUE);
		}
	}

	/**
	 * 获取需要动态生成的代码.
	 *
	 * @param g              动态代码所在的构造器
	 * @param factory        构造器所在Factory的实例
	 * @param codeFlag       构造器中存放代码的属性名称
	 * @param attrCondeFlag  构造器中存放引用的代码的属性名称
	 * @return 代码字符串
	 */
	public static String getCode(Generator g, Factory factory, String codeFlag, String attrCondeFlag)
			throws ConfigurationException
	{
		String code = (String) g.getAttribute(codeFlag);
		String attrCode = (String) g.getAttribute(attrCondeFlag);
		if (code == null && attrCode == null)
		{
			throw new ConfigurationException("Not found the [" + codeFlag + "] or ["
					+ attrCondeFlag + "] attribute.");
		}
		if (code == null)
		{
			code = (String) factory.getAttribute(attrCode);
			if (code == null)
			{
				throw new ConfigurationException("Not found the [" + attrCode + "] in factory attribute.");
			}
		}
		Map paramMap = new HashMap();
		String[] names = g.getAttributeNames();
		for (int i = 0; i < names.length; i++)
		{
			String name = names[i];
			if (codeFlag.equals(name) || attrCondeFlag.equals(name))
			{
				continue;
			}
			paramMap.put(name, g.getAttribute(name));
		}
		if (paramMap.size() == 0)
		{
			paramMap = null;
		}
		if (paramMap != null)
		{
			code = Utility.resolveDynamicPropnames(code, paramMap);
		}
		else
		{
			code = Utility.resolveDynamicPropnames(code);
		}
		return code;
	}

	/**
	 * 记录动态生成类时的出错信息.
	 *
	 * @param code         需要动态编译的代码
	 * @param position     代码所在的位置信息
	 * @param error        出错的异常信息
	 */
	public static void logCodeError(String code, String position, Exception error)
	{
		String msg = "Error in compile java code at " + position + ". the code is:\n" + code;
		FactoryManager.log.error(msg, error);
		Object obj = ThreadCache.getInstance().getProperty(CODE_ERRORS_FLAG);
		if (obj instanceof List)
		{
			((List) obj).add(new CodeErrorInfo(code, position, error));
		}
	}

	/**
	 * 设置动态代码生成时, 对代码编译的类型.
	 */
	public static final String COMPILE_TYPE_PROPERTY = "self.micromagic.dc.compile.type";

	/**
	 * 动态代码生成时, 对代码编译的类型.
	 */
	static String DC_COMPILE_TYPE = null;

	static
	{
		try
		{
			Utility.addFieldPropertyManager(COMPILE_TYPE_PROPERTY, CodeClassTool.class, "DC_COMPILE_TYPE");
		}
		catch (Throwable ex) {}
	}

	private static class CodeKey
	{
		private byte[] compressedCode;
		private int strCodeHash;

		public CodeKey(String methodHead, String bodyCode)
				throws IOException
		{
			String code = methodHead + "#" + bodyCode;
			this.strCodeHash = code.hashCode();
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream(128);
			DeflaterOutputStream out = new DeflaterOutputStream(byteOut);
			byte[] buf = code.getBytes("UTF-8");
			out.write(buf);
			out.close();
			this.compressedCode = byteOut.toByteArray();
		}

		public int hashCode()
		{
			return this.strCodeHash;
		}

		public boolean equals(Object obj)
		{
			if (obj instanceof CodeKey)
			{
				CodeKey other = (CodeKey) obj;
				if (other.compressedCode.length == this.compressedCode.length
						&& other.strCodeHash == this.strCodeHash)
				{
					for (int i = 0; i < this.compressedCode.length; i++)
					{
						if (other.compressedCode[i] != this.compressedCode[i])
						{
							return false;
						}
					}
					return true;
				}
			}
			return false;
		}

	}

}