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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;
import self.micromagic.util.ref.IntegerRef;

/**
 * 一个类的自动编译及生成工具.
 *
 * @author micromagic@sina.com
 */
public class ClassGenerator
{
	/**
	 * 已注册的类生成工具的缓存.
	 */
	private static final Map cgCache = new HashMap();

	/**
	 * 注册一个类生成工具.
	 *
	 * @param name   名称
	 * @param cg     类生成工具的实现
	 */
	public static void registerCG(String name, CG cg)
	{
		cgCache.put(name, cg);
	}

	private String className;
	private ClassLoader classLoader;
	private Class superClass;
	private final List interfaces = new ArrayList();
	private final Set importPackages = new HashSet();
	private final Map classPathCache = new HashMap();
	private final List fields = new ArrayList();
	private final List constructors = new ArrayList();
	private final List methods = new ArrayList();
	private String compileType;

	/**
	 * 获取本代码的类名.
	 */
	public String getClassName()
	{
		return this.className;
	}

	/**
	 * 设置本代码的类名.
	 */
	public void setClassName(String className)
	{
		if (!StringTool.isEmpty(className))
		{
			this.className = className;
		}
	}

	/**
	 * 获得生成类是使用的<code>ClassLoader</code>.
	 */
	public ClassLoader getClassLoader()
	{
		if (this.classLoader == null)
		{
			return this.getClass().getClassLoader();
		}
		return this.classLoader;
	}

	/**
	 * 设置生成类是使用的<code>ClassLoader</code>.
	 */
	public void setClassLoader(ClassLoader classLoader)
	{
		this.classLoader = classLoader;
	}

	/**
	 * 设置本代码需要继承的类.
	 */
	public void setSuperClass(Class superClass)
	{
		this.superClass = superClass;
	}

	/**
	 * 获得继承的类.
	 */
	public Class getSuperClass()
	{
		return superClass;
	}

	/**
	 * 添加本代码需要实现的接口.
	 */
	public void addInterface(Class anInterface)
	{
		if (anInterface != null)
		{
			this.interfaces.add(anInterface);
		}
	}

	/**
	 * 获得需要实现的接口列表.
	 */
	public Class[] getInterfaces()
	{
		return (Class[]) this.interfaces.toArray(new Class[this.interfaces.size()]);
	}

	/**
	 * 添加需要引用的包.
	 */
	public void importPackage(String packageName)
	{
		if (!StringTool.isEmpty(packageName))
		{
			this.importPackages.add(packageName);
		}
	}

	/**
	 * 获得需要引用的包列表.
	 */
	public String[] getPackages()
	{
		String[] arr = new String[this.importPackages.size()];
		return (String[]) this.importPackages.toArray(arr);
	}

	/**
	 * 添加一个读取其他类的路径工具.
	 */
	public void addClassPath(Class pathClass)
	{
		if (pathClass == null)
		{
			return;
		}
		ClassLoader cl = pathClass.getClassLoader();
		if (cl == null)
		{
			return;
		}
		this.classPathCache.put(pathClass, cl);
	}

	/**
	 * 获得需要读取其他类的路径工具列表.
	 */
	public Class[] getClassPaths()
	{
		Set tmpCheck = new HashSet();
		List result = new ArrayList();
		Iterator itr = this.classPathCache.entrySet().iterator();
		while (itr.hasNext())
		{
			Map.Entry entry = (Map.Entry) itr.next();
			ClassLoader cl = (ClassLoader) entry.getValue();
			if (tmpCheck.add(cl))
			{
				result.add(entry.getKey());
			}
		}
		return (Class[]) result.toArray(new Class[result.size()]);
	}

	/**
	 * 获得所有已设置的类列表.
	 * 但不包括系统类(ClassLoader == null)
	 */
	public Class[] getAllClasses()
	{
		Set result = new HashSet();
		Iterator itr = this.classPathCache.entrySet().iterator();
		while (itr.hasNext())
		{
			Map.Entry entry = (Map.Entry) itr.next();
			result.add(entry.getKey());
		}
		if (this.superClass != null && this.superClass.getClassLoader() != null)
		{
			result.add(this.superClass);
		}
		itr = this.interfaces.iterator();
		while (itr.hasNext())
		{
			Class c = (Class) itr.next();
			if (c.getClassLoader() != null)
			{
				result.add(c);
			}
		}
		return (Class[]) result.toArray(new Class[result.size()]);
	}

	/**
	 * 添加一个属性代码.
	 */
	public void addField(String field)
	{
		if (!StringTool.isEmpty(field))
		{
			this.fields.add(field);
		}
	}

	/**
	 * 获得属性代码列表.
	 */
	public String[] getFields()
	{
		if (this.getClassName() == null)
		{
			return (String[]) this.fields.toArray(new String[this.fields.size()]);
		}
		else
		{
			String[] arr = new String[this.fields.size()];
			this.fields.toArray(arr);
			Map map = new HashMap(2);
			map.put(CG.THIS_NAME, getConstructorName(this.getClassName()));
			for (int i = 0; i < arr.length; i++)
			{
				arr[i] = Utility.resolveDynamicPropnames(arr[i], map, true);
			}
			return arr;
		}
	}

	/**
	 * 添加一个构造方法代码.
	 * 代码中构造函数的名称可以用"${thisName}"代替.
	 * @see CG#THIS_NAME
	 */
	public void addConstructor(String constructor)
	{
		if (!StringTool.isEmpty(constructor))
		{
			this.constructors.add(constructor);
		}
	}

	/**
	 * 获得构造方法代码列表.
	 */
	public String[] getConstructors()
	{
		if (this.getClassName() == null)
		{
			throw new IllegalArgumentException("The class name hasn't bean setted .");
		}
		String[] arr = new String[this.constructors.size()];
		this.constructors.toArray(arr);
		Map map = new HashMap(2);
		map.put(CG.THIS_NAME, getConstructorName(this.getClassName()));
		for (int i = 0; i < arr.length; i++)
		{
			arr[i] = Utility.resolveDynamicPropnames(arr[i], map, true);
		}
		return arr;
	}

	/**
	 * 添加一个方法代码.
	 */
	public void addMethod(String methodCode)
	{
		if (!StringTool.isEmpty(methodCode))
		{
			this.methods.add(methodCode);
		}
	}

	/**
	 * 获得方法代码列表.
	 */
	public String[] getMethods()
	{
		if (this.getClassName() == null)
		{
			return (String[]) this.methods.toArray(new String[this.methods.size()]);
		}
		else
		{
			String[] arr = new String[this.methods.size()];
			this.methods.toArray(arr);
			Map map = new HashMap(2);
			map.put(CG.THIS_NAME, getConstructorName(this.getClassName()));
			for (int i = 0; i < arr.length; i++)
			{
				arr[i] = Utility.resolveDynamicPropnames(arr[i], map, true);
			}
			return arr;
		}
	}

	/**
	 * 获得对本代码的编译方式.
	 */
	public String getCompileType()
	{
		if (this.compileType == null)
		{
			return Utility.getProperty(CG.COMPILE_TYPE_PROPERTY, "javassist");
		}
		return this.compileType;
	}

	/**
	 * 设置对本代码的编译方式.
	 */
	public void setCompileType(String compileType)
	{
		this.compileType = compileType;
	}

	/**
	 * 根据设置的代码生成一个类.
	 */
	public Class createClass()
	{
		if (this.getClassName() == null)
		{
			throw new IllegalArgumentException("The class name hasn't bean setted .");
		}
		String type = this.getCompileType();
		try
		{
			CG cg = (CG) cgCache.get(type.toLowerCase());
			if (cg != null)
			{
				return cg.createClass(this);
			}
		}
		catch (RuntimeException ex)
		{
			throw ex;
		}
		catch (Exception ex)
		{
			throw new CGException(ex);
		}
		throw new IllegalArgumentException("Error compile type:" + type + ".");
	}

	/**
	 * 用于生成类的流水号, 防止类名重复.
	 */
	private static volatile int CLASS_GENERATOR_ID = 1;

	/**
	 * 创建一个类生成工具.
	 *
	 * @param baseClass       生成的类所使用的基础类,
	 *                        会使用此类的ClassLoader来载入新生成的类
	 * @param interfaceClass  需要实现的接口
	 * @param imports         需要引用的包列表
	 * @return  <code>ClassGenerator</code>的实例.
	 */
	public static ClassGenerator createClassGenerator(Class baseClass, Class interfaceClass,
			String[] imports)
	{
		return createClassGenerator(null, baseClass, null, interfaceClass, imports);
	}

	/**
	 * 创建一个类生成工具. <p>
	 * 新的类名为：eterna.[baseClass]$suffix$$EBP_[序列号]
	 *
	 * @param suffix          生成类名的后缀
	 * @param baseClass       生成的类所使用的基础类,
	 *                        会使用此类的ClassLoader来载入新生成的类
	 * @param interfaceClass  需要实现的接口
	 * @param imports         需要引用的包列表
	 * @return  <code>ClassGenerator</code>的实例.
	 */
	public static ClassGenerator createClassGenerator(String suffix, Class baseClass, Class interfaceClass,
			String[] imports)
	{
		return createClassGenerator(suffix, baseClass, null, interfaceClass, imports);
	}

	/**
	 * 创建一个类生成工具. <p>
	 * 新的类名为：eterna.[baseClass]$suffix$$EBP_[序列号]
	 *
	 * @param suffix          生成类名的后缀
	 * @param baseClass       生成的类所使用的基础类,
	 *                        会使用此类的ClassLoader来载入新生成的类
	 * @param superClass      需要继承的类
	 * @param interfaceClass  需要实现的接口
	 * @param imports         需要引用的包列表
	 * @return  <code>ClassGenerator</code>的实例.
	 */
	public static ClassGenerator createClassGenerator(String suffix, Class baseClass, Class superClass,
			Class interfaceClass, String[] imports)
	{
		ClassGenerator cg = new ClassGenerator();
		if (superClass != null)
		{
			cg.setSuperClass(superClass);
		}
		cg.addClassPath(baseClass);
		cg.addClassPath(interfaceClass);
		cg.addClassPath(ClassGenerator.class);
		String tmpSuffix;
		synchronized (ClassGenerator.class)
		{
			if (StringTool.isEmpty(suffix))
			{
				tmpSuffix = "$$ECG_" + (CLASS_GENERATOR_ID++);
			}
			else
			{
				tmpSuffix = "$" + suffix + "$$ECG_" + (CLASS_GENERATOR_ID++);
			}
		}
		if (isArray(baseClass))
		{
			throw new CGException("The base class can't be an array.");
		}
		else
		{
			cg.setClassName("cg." + baseClass.getName() + tmpSuffix);
		}
		cg.addInterface(interfaceClass);
		if (imports != null)
		{
			for (int i = 0; i < imports.length; i++)
			{
				if (!StringTool.isEmpty(imports[i]))
				{
					cg.importPackage(imports[i]);
				}
			}
		}
		return cg;
	}

	/**
	 * 获取给定类的包路径字符串.
	 */
	public static String getPackageString(Class c)
	{
		String cName = c.getName();
		int lastIndex = cName.lastIndexOf('.');
		if (lastIndex == -1)
		{
			return "";
		}
		return cName.substring(0, lastIndex);
	}

	/**
	 * 获取给定类名的包路径字符串.
	 */
	public static String getPackageString(String className)
	{
		if (className == null)
		{
			return "";
		}
		int lastIndex = className.lastIndexOf('.');
		if (lastIndex == -1)
		{
			return "";
		}
		return className.substring(0, lastIndex);
	}

	/**
	 * 获取给定类名的构造函数名称.
	 */
	public static String getConstructorName(String className)
	{
		int lastIndex = className.lastIndexOf('.');
		if (lastIndex == -1)
		{
			return className;
		}
		return className.substring(lastIndex + 1);
	}

	/**
	 * 获取数组的元素类型.
	 *
	 * @param arrayClass  数组类
	 * @param levelRef    返回数组的维度
	 */
	public static Class getArrayElementType(Class arrayClass, IntegerRef levelRef)
	{
		if (arrayClass == null || !isArray(arrayClass))
		{
			return null;
		}
		int level = 0;
		Class tmpClass = arrayClass;
		while (isArray(tmpClass))
		{
			tmpClass = tmpClass.getComponentType();
			level++;
		}
		if (levelRef != null)
		{
			levelRef.value = level;
		}
		return tmpClass;
	}

	/**
	 * 获得一个类的类名.
	 * 此方法会根据不同的JDK版本调用不同的方法.
	 * 如：1.5 以上的需要使用getCanonicalName.
	 */
	public static String getClassName(Class c)
	{
		if (isArray(c))
		{
			IntegerRef level = new IntegerRef();
			Class type = getArrayElementType(c, level);
			String nameStr = nameAccessor.getName(type);
			StringAppender arrDef = StringTool.createStringAppender(nameStr.length() + level.value * 2);
			arrDef.append(nameStr);
			arrDef.append(getArrayDefine(level.value));
			return arrDef.toString();
		}
		return nameAccessor.getName(c);
	}

	/**
	 * 判断给出的类型是否为一个数组.
	 */
	public static boolean isArray(Class c)
	{
		return c != null && c.getName().charAt(0) == '[';
	}

	/**
	 * 获取数组的定义部分, 如: 一维数组[], 二维数组[][] ...
	 *
	 * @param arrayLevel  数组的维度.
	 */
	public static String getArrayDefine(int arrayLevel)
	{
		StringAppender arrVL = StringTool.createStringAppender();
		for (int i = 0; i < arrayLevel; i++)
		{
			arrVL.append("[]");
		}
		return arrVL.toString();
	}

	static int COMPILE_LOG_TYPE = 1;

	/**
	 * 初始化一个类名的访问者.
	 */
	private static NameAccessor nameAccessor;

	static
	{
		try
		{
			Class c = Class.forName("self.micromagic.cg.ClassGenerator$ClassCanonicalNameAccessor");
			nameAccessor = (NameAccessor) c.newInstance();
		}
		catch (Throwable ex)
		{
			// 如果出现异常, 这可能是jdk版本小于1.5, 使用getName方法来获取类名
			nameAccessor = new ClassNameAccessor();
			if (!(ex instanceof UnsupportedClassVersionError || ex instanceof LinkageError
					|| ex instanceof ClassNotFoundException
					|| ex.getCause() instanceof UnsupportedClassVersionError))
			{
				// 当不是版本相关的异常时, 才记录日志
				if (COMPILE_LOG_TYPE > CG.COMPILE_LOG_TYPE_ERROR)
				{
					CG.log.error("init name accessor error.", ex);
				}
			}
		}
		try
		{
			Utility.addFieldPropertyManager(CG.COMPILE_LOG_PROPERTY, ClassGenerator.class, "COMPILE_LOG_TYPE");
			if (COMPILE_LOG_TYPE > CG.COMPILE_LOG_TYPE_INFO)
			{
				CG.log.info("map entry name:" + nameAccessor.getName(Map.Entry.class)
						+ ", accessor class:" + nameAccessor.getClass());
			}
		}
		catch (Throwable ex) {}
		try
		{
			registerCG("ant", new AntCG());
		}
		catch (Throwable ex)
		{
			if (COMPILE_LOG_TYPE > CG.COMPILE_LOG_TYPE_INFO)
			{
				CG.log.error("AntCG init error.", ex);
			}
			else if (COMPILE_LOG_TYPE > CG.COMPILE_LOG_TYPE_ERROR)
			{
				if (!(ex instanceof NoClassDefFoundError))
				{
					CG.log.warn("AntCG init error, message is [" + ex + "].");
				}
			}
		}
		try
		{
			registerCG("javassist", new JavassistCG());
		}
		catch (Throwable ex)
		{
			if (COMPILE_LOG_TYPE > CG.COMPILE_LOG_TYPE_ERROR)
			{
				CG.log.error("JavassistCG init error.", ex);
			}
		}
	}

	interface NameAccessor
	{
		String getName(Class c);

	}

	static class ClassNameAccessor
			implements NameAccessor
	{
		public String getName(Class c)
		{
			return c.getName();
		}

	}

}