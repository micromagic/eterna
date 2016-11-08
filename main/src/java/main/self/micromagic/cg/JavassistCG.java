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

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMember;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.Bytecode;
import javassist.compiler.CompileError;
import javassist.compiler.JvstCodeGen;
import javassist.compiler.Lex;
import javassist.compiler.MemberResolver;
import javassist.compiler.Parser;
import javassist.compiler.SymbolTable;
import javassist.compiler.ast.ASTList;
import javassist.compiler.ast.Declarator;
import javassist.compiler.ast.MethodDecl;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;
import self.micromagic.util.container.SynHashMap;

/**
 * 通过javassist对类进行编译的工具.
 *
 * @author micromagic@sina.com
 */
public class JavassistCG
		implements CG
{
	/**
	 * 配置文件中设置javassist重新构造实现类的类名.
	 */
	private static final String REBUILDER_CLASS_FLAG = "self.micromagic.compile.javassist.reBuilder";

	/**
	 * 用javassist作为编译类型时使用的名称.
	 */
	public static final String COMPILE_TYPE = "javassist";

	/**
	 * 使用javassist生成一个类.
	 */
	public Class createClass(ClassGenerator cg)
			throws NotFoundException, CannotCompileException, CompileError
	{
		return createClass0(cg);
	}

	private static synchronized Class createClass0(ClassGenerator cg)
			throws NotFoundException, CannotCompileException, CompileError
	{
		ClassLoader cl = cg.getClassLoader();
		MyClassPool pool = getClassPool(cl);
		Class[] classPaths = cg.getClassPaths();
		WeakClassPath[] cpArr = new WeakClassPath[classPaths.length];
		for (int i = 0; i < classPaths.length; i++)
		{
			cpArr[i] = new WeakClassPath(classPaths[i]);
			pool.appendClassPath(cpArr[i]);
		}
		try
		{
			String[] packages = cg.getPackages();
			for (int i = 0; i < packages.length; i++)
			{
				pool.importPackage(packages[i]);
			}
			pool.importPackage(ClassGenerator.getPackageString(cg.getClassName()));
			CtClass cc = pool.makeClass(cg.getClassName());
			Class[] interfaces = cg.getInterfaces();
			for (int i = 0; i < interfaces.length; i++)
			{
				cc.addInterface(pool.getClass(interfaces[i], true));
			}
			Class baseClass = cg.getSuperClass();
			if (baseClass != null)
			{
				cc.setSuperclass(pool.getClass(baseClass, true));
			}
			if (ClassGenerator.COMPILE_LOG_TYPE > COMPILE_LOG_TYPE_DEBUG)
			{
				log.info(cg.getClassName()
						+ (baseClass != null ? ":" + ClassGenerator.getClassName(baseClass) : ""));
			}
			String[] constructors = cg.getConstructors();
			for (int i = 0; i < constructors.length; i++)
			{
				if (ClassGenerator.COMPILE_LOG_TYPE > COMPILE_LOG_TYPE_DEBUG)
				{
					log.info(cg.getClassName() + ", constructor:" + constructors[i]);
				}
				pool.createConstructor(cc, constructors[i]);
				//cc.addConstructor(CtNewConstructor.make(constructors[i], cc));
			}
			String[] methods = cg.getMethods();
			for (int i = 0; i < methods.length; i++)
			{
				if (ClassGenerator.COMPILE_LOG_TYPE > COMPILE_LOG_TYPE_DEBUG)
				{
					log.info(cg.getClassName() + ", method:" + methods[i]);
				}
				pool.createMethod(cc, methods[i]);
				//cc.addMethod(CtNewMethod.make(methods[i], cc));
			}
			String[] fields = cg.getFields();
			for (int i = 0; i < fields.length; i++)
			{
				if (ClassGenerator.COMPILE_LOG_TYPE > COMPILE_LOG_TYPE_DEBUG)
				{
					log.info(cg.getClassName() + ", field:" + fields[i]);
				}
				cc.addField(CtField.make(fields[i], cc));
			}
			pool.dealDelayObj();
			Class c = cc.toClass(cl);
			return c;
		}
		finally
		{
			// 清理importPackages和classPaths
			pool.clearDelayObj();
			pool.clearImportedPackages();
			for (int i = 0; i < cpArr.length; i++)
			{
				pool.removeClassPath(cpArr[i]);
			}
		}
	}

	private static Map classPoolCache = new SynHashMap(8, SynHashMap.WEAK);

	private static ReBuilder reBuilder;

	/**
	 * 设置执行重新构造的实现类.
	 */
	static void setReBuilderClass(String className)
	{
		try
		{
			if (Utility.compareJavaVersion("1.6", 2) >= 0)
			{
				// 1.6以上的java才需要设置reBuilder
				reBuilder = (ReBuilder) Class.forName(className).newInstance();
			}
		}
		catch (Throwable ex)
		{
			if (ClassGenerator.COMPILE_LOG_TYPE > COMPILE_LOG_TYPE_ERROR)
			{
				log.error("Can't create [" + className + "].", ex);
			}
		}
	}

	static
	{
		try
		{
			Utility.addMethodPropertyManager(REBUILDER_CLASS_FLAG,
					JavassistCG.class, "setReBuilderClass");
		}
		catch (Exception ex) {}
	}


	/**
	 * 根据使用的ClassLoader获得一个ClassPool.
	 */
	private static MyClassPool getClassPool(ClassLoader cl)
	{
		MyClassPool pool = (MyClassPool) classPoolCache.get(cl);
		if (pool == null)
		{
			synchronized (classPoolCache)
			{
				pool = (MyClassPool) classPoolCache.get(cl);
				if (pool == null)
				{
					pool = new MyClassPool();
					pool.appendSystemPath();
					pool.appendClassPath(new LoaderClassPath(cl));
					classPoolCache.put(cl, pool);
				}
			}
		}
		return pool;
	}

	/**
	 * 重载ClassPool, 开放getCached方法, 并增加一些其他的操作.
	 */
	private static class MyClassPool
			extends ClassPool
	{
		MemberResolver resolver = new MemberResolver(this);

		/**
		 * 开放父类的getCached方法
		 */
		public CtClass getCached(String classname)
		{
			return super.getCached(classname);
		}

		/**
		 * 执行需要延迟处理的对象.
		 */
		void dealDelayObj()
				throws CompileError
		{
			Iterator itr = this.delayObjs.iterator();
			while (itr.hasNext())
			{
				DelayObj obj = (DelayObj) itr.next();
				if (obj.member instanceof CtMethod)
				{
					CtMethod method = (CtMethod) obj.member;
					MethodDecl md = (MethodDecl) obj.dec;
					obj.gen.setThisMethod(method);
					md.accept(obj.gen);
					method.getMethodInfo().setCodeAttribute(obj.b.toCodeAttribute());
				}
				else if (obj.member instanceof CtConstructor)
				{
					CtConstructor constructor = (CtConstructor) obj.member;
					MethodDecl md = (MethodDecl) obj.dec;
					md.accept(obj.gen);
					constructor.getMethodInfo().setCodeAttribute(obj.b.toCodeAttribute());
				}
				if (reBuilder != null)
				{
					reBuilder.reBuild(obj.member);
				}
			}
		}

		/**
		 * 清空需要延迟处理的对象.
		 */
		void clearDelayObj()
		{
			this.delayObjs.clear();
		}

		private final List delayObjs = new ArrayList();

		/**
		 * 根据方法的源代码在CtClass中创建一个初始化函数.
		 */
		CtConstructor createConstructor(CtClass cc, String src)
				throws CompileError, NotFoundException, CannotCompileException
		{
			return (CtConstructor) this.compileMethod(cc, src);
		}

		/**
		 * 根据方法的源代码在CtClass中创建一个方法.
		 */
		CtMethod createMethod(CtClass cc, String src)
				throws CompileError, NotFoundException, CannotCompileException
		{
			return (CtMethod) this.compileMethod(cc, src);
		}

		/**
		 * 编译方法, 包括方法及初始化函数.
		 */
		private CtMember compileMethod(CtClass cc, String src)
				throws CompileError, CannotCompileException, NotFoundException
		{
			Bytecode b = new Bytecode(cc.getClassFile2().getConstPool(), 0, 0);
			JvstCodeGen gen = new JvstCodeGen(b, cc, this);
			Parser p = new Parser(new Lex(src));
			SymbolTable stable = new SymbolTable();
			ASTList mem = p.parseMember1(stable);
			MethodDecl md = (MethodDecl) mem;
			int mod = MemberResolver.getModifiers(md.getModifiers());
			CtClass[] plist = gen.makeParamList(md);
			CtClass[] tlist = gen.makeThrowsList(md);
			gen.recordParams(plist, Modifier.isStatic(mod), "$", "$args", "$$", stable);
			md = p.parseMethod2(stable, md);
			if (md.isConstructor())
			{
				CtConstructor constructor = new CtConstructor(plist, cc);
				constructor.setModifiers(mod);
				this.delayObjs.add(new DelayObj(constructor, md, gen, b));
				constructor.setExceptionTypes(tlist);
				cc.addConstructor(constructor);
				return constructor;
			}
			else
			{
				Declarator r = md.getReturn();
				CtClass rtype = this.resolver.lookupClass(r);
				gen.recordType(rtype);
				gen.recordReturnType(rtype, "$r", null, stable);
				CtMethod method = new CtMethod(rtype, r.getVariable().get(), plist, cc);
				method.setModifiers(mod);
				if ((mod & Modifier.ABSTRACT) == 0)
				{
					this.delayObjs.add(new DelayObj(method, md, gen, b));
				}
				else
				{
					gen.setThisMethod(method);
					md.accept(gen);
				}
				method.setExceptionTypes(tlist);
				cc.addMethod(method);
				return method;
			}
		}

		/**
		 * 将一个定义(Declarator)列表转换成CtClass数组
		 */
		/*
		CtClass[] resolveClasses(ASTList list)
				throws CompileError
		{
			if (list == null)
			{
				return new CtClass[0];
			}
			CtClass[] cArr = new CtClass[list.length()];
			int i = 0;
			while (list != null)
			{
				cArr[i++] = this.resolver.lookupClass((Declarator) list.head());
				list = list.tail();
			}
			return cArr;
		}
		*/

		/**
		 * 获取一个CtClass.
		 * 先在缓存里查找, 如果未找到再根据类的文件流构造CtClass.
		 */
		CtClass getClass(Class c, boolean checkCache)
				throws NotFoundException
		{
			CtClass cc = null;
			if (checkCache)
			{
				cc = this.getCached(c.getName());
			}
			if (cc != null)
			{
				return cc;
			}
			String fName = "/" + c.getName().replace('.', '/') + ".class";
			try
			{
				InputStream in = c.getResourceAsStream(fName);
				if (in == null)
				{
					return this.get(c.getName());
				}
				else
				{
					return this.makeClass(in);
				}
			}
			catch (IOException ex)
			{
				return this.get(c.getName());
			}
		}

	}

	/**
	 * 需要延迟处理的对象.
	 */
	private static class DelayObj
	{
		public final CtBehavior member;
		public final ASTList dec;
		public final JvstCodeGen gen;
		public final Bytecode b;

		public DelayObj(CtBehavior member, ASTList dec, JvstCodeGen gen, Bytecode b)
		{
			this.member = member;
			this.dec = dec;
			this.gen = gen;
			this.b = b;
		}

	}

	private static class WeakClassPath
			implements ClassPath
	{
		/**
		 * 这里使用<code>WeakReference</code>来引用类, 这样就不会影响其正常的释放.
		 */
		private final WeakReference baseCL;
		private final String className;

		public WeakClassPath(Class c)
		{
			this.className = c.getName();
			this.baseCL = new WeakReference(c.getClassLoader());
		}

		public InputStream openClassfile(String classname)
		{
			ClassLoader cl = (ClassLoader) this.baseCL.get();
			if (cl == null)
			{
				if (ClassGenerator.COMPILE_LOG_TYPE > COMPILE_LOG_TYPE_ERROR)
				{
					log.warn(this);
				}
				return null;
			}
			String fName = classname.replace('.', '/') + ".class";
			InputStream is = cl.getResourceAsStream(fName);
			if (ClassGenerator.COMPILE_LOG_TYPE > COMPILE_LOG_TYPE_INFO)
			{
				if (log.isInfoEnabled())
				{
					StringAppender buf = StringTool.createStringAppender();
					buf.append(is == null ? "Not found " : "Open ").append("class file:")
							.append(fName).append(", base class:").append(this.className);
					log.info(buf);
				}
			}
			return is;
		}

		public URL find(String classname)
		{
			ClassLoader cl = (ClassLoader) this.baseCL.get();
			if (cl == null)
			{
				if (ClassGenerator.COMPILE_LOG_TYPE > COMPILE_LOG_TYPE_ERROR)
				{
					log.warn(this);
				}
				return null;
			}
			String fName = classname.replace('.', '/') + ".class";
			URL url = cl.getResource(fName);
			if (ClassGenerator.COMPILE_LOG_TYPE > COMPILE_LOG_TYPE_INFO)
			{
				if (log.isInfoEnabled())
				{
					StringAppender buf = StringTool.createStringAppender();
					buf.append(url == null ? "Not found " : "The ").append("res file:").append(fName);
					if (url != null)
					{
						buf.append(", locate:").append(url);
					}
					buf.append(", base class:").append(this.className);
					log.info(buf);
				}
			}
			return url;
		}

		public void close()
		{
		}

		public String toString()
		{
			ClassLoader cl = (ClassLoader) this.baseCL.get();
			if (cl == null)
			{
				return this.className + " released.";
			}
			return this.className + ".class";
		}

	}

	/**
	 * 执行重新构造的接口.
	 */
	public interface ReBuilder
	{
		void reBuild(CtBehavior member);

	}

}