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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.ReferenceMap;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.types.Path;

import self.micromagic.util.ResManager;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;

/**
 * 通过ant对类进行编译的工具.
 *
 * @author micromagic@sina.com
 */
public class AntCG
		implements CG
{
	/**
	 * 用ant作为编译类型时使用的名称.
	 */
	public static final String COMPILE_TYPE = "ant";

	/**
	 * 配置文件中对ant相关属性进行配置的前缀.
	 */
	private static final String ANT_TOOL_CONFIG_PREFIX = "self.micromagic.compile.ant.";

	/**
	 * 配置文件中对ant产生的相关文件是否要放到一个通过UID生成的路径中.
	 * 一般来说, 使用ant进行编译是在测试环境中, 只会运行在一个jvm中, 但如果需要运行多个jvm
	 * 而编译路径设置的一样, 那就会发生冲突, 可以通过这个设置将各个jvm的编译的路径设置到
	 * 通过UID设成的唯一目录下.
	 * 默认值为: false.
	 */
	public static final String ANT_TOOL_UID_PATH_PROPERTY = "self.micromagic.compile.ant.uidPath";

	/**
	 * 使用ant生成一个类.
	 */
	public Class createClass(ClassGenerator cg)
			throws IOException, ClassNotFoundException
	{
		return createClass0(cg);
	}

	private static synchronized Class createClass0(ClassGenerator cg)
			throws IOException, ClassNotFoundException
	{
		File destPath = new File(getDestPath());
		Project p = new Project();
		p.setName("cg.ant");
		CompileLogger cl = new CompileLogger();
		p.addBuildListener(cl);
		MyJavac javac = new MyJavac();
		javac.setProject(p);
		javac.setDebug(getDebug());
		javac.setSourcepath(new Path(p, getSrcPath()));
		javac.setCompiler(getCompiler());
		setClassPath(p, javac, cg);
		javac.setDestdir(destPath);
		javac.setSrcFile(createSrcFile(cg));
		javac.setEncoding(getEncoding());
		try
		{
			javac.compile();
			CompileClassLoader ccl = getClassLoader(destPath, cg.getClassLoader());
			ccl.addMessage(cg.getClassName(), cl.toString());
			return ccl.findClass(cg.getClassName());
		}
		catch (Exception ex)
		{
			throw new ClassNotFoundException("message:" + cl, ex);
		}
	}

	private static File createSrcFile(ClassGenerator cg)
			throws IOException
	{
		String srcPath = getSrcPath();
		String destPath = getDestPath();
		String className = cg.getClassName();
		int index = className.lastIndexOf('.');
		String tmpPath = null;
		String cName = className;
		String pName = null;
		if (index != -1)
		{
			pName = className.substring(0, index);
			tmpPath = pName.replace('.', File.separatorChar);
			cName = className.substring(index + 1);
		}
		File srcDir = tmpPath == null ? new File(srcPath) : new File(srcPath, tmpPath);
		if (!srcDir.exists())
		{
			srcDir.mkdirs();
		}
		File destDir = tmpPath == null ? new File(destPath) : new File(destPath, tmpPath);
		if (!destDir.exists())
		{
			destDir.mkdirs();
		}
		StringAppender out = StringTool.createStringAppender(256);
		out.appendln();
		if (pName != null)
		{
			out.append("package ").append(pName).append(';').appendln().appendln();
		}
		String[] packages = cg.getPackages();
		for (int i = 0; i < packages.length; i++)
		{
			out.append("import ").append(packages[i]).append(".*;").appendln();
		}
		out.appendln().append("public class ").append(cName);
		Class baseClass = cg.getSuperClass();
		if (baseClass != null)
		{
			out.append(" extends ").append(ClassGenerator.getClassName(baseClass));
		}
		out.appendln();
		Class[] interfaces = cg.getInterfaces();
		for (int i = 0; i < interfaces.length; i++)
		{
			if (i == 0)
			{
				out.append("      ").append("implements ");
			}
			else
			{
				out.append(", ");
			}
			out.append(ClassGenerator.getClassName(interfaces[i]));
		}
		if (interfaces.length > 0)
		{
			out.appendln();
		}
		out.append('{').appendln();
		String[] fields = cg.getFields();
		for (int i = 0; i < fields.length; i++)
		{
			out.append(ResManager.indentCode(fields[i], 1)).appendln().appendln();
		}
		String[] constructors = cg.getConstructors();
		for (int i = 0; i < constructors.length; i++)
		{
			out.append(ResManager.indentCode(constructors[i], 1)).appendln().appendln();
		}
		String[] methods = cg.getMethods();
		for (int i = 0; i < methods.length; i++)
		{
			out.append(ResManager.indentCode(methods[i], 1)).appendln().appendln();
		}
		out.append('}');
		if (ClassGenerator.COMPILE_LOG_TYPE > COMPILE_LOG_TYPE_DEBUG)
		{
			log.info(out.toString());
		}
		File srcFile = new File(srcDir, cName + ".java");
		FileOutputStream fos = new FileOutputStream(srcFile);
		fos.write(out.toString().getBytes(getEncoding()));
		fos.close();
		return srcFile;
	}

	/**
	 * 设置需要的classpath.
	 */
	public static void setClassPath(Project p, Javac javac, ClassGenerator cg)
	{
		Set paths = new HashSet();
		parserClassPath(cg.getClassLoader(), paths);
		Class[] arr = cg.getClassPaths();
		for (int i = 0; i < arr.length; i++)
		{
			parserClassPath(arr[i].getClassLoader(), paths);
		}
		Iterator itr = paths.iterator();
		while (itr.hasNext())
		{
			String path = (String) itr.next();
			javac.setClasspath(new Path(p, path));
			if (ClassGenerator.COMPILE_LOG_TYPE > COMPILE_LOG_TYPE_DEBUG)
			{
				log.info("Added classpath:" + path);
			}
		}
	}

	/**
	 * 解析</code>ClassLoader</code>中的路径, 放到结果集合中.
	 */
	private static void parserClassPath(ClassLoader cl, Set result)
	{
		if (cl == null)
		{
			return;
		}
		if (cl instanceof URLClassLoader)
		{
			URLClassLoader ucl = (URLClassLoader) cl;
			URL[] urls = ucl.getURLs();
			for (int i = 0; i < urls.length; i++)
			{
				URL url = urls[i];
				if ("file".equals(url.getProtocol()))
				{
					result.add(url.getFile());
				}
				else
				{
					result.add(url.toString());
				}
			}
		}
		parserClassPath(cl.getParent(), result);
	}

	/**
	 * 获得文件的编码格式.
	 */
	public static String getEncoding()
	{
		String encoding = Utility.getProperty(ANT_TOOL_CONFIG_PREFIX + "encoding");
		if (encoding == null)
		{
			encoding = System.getProperty("file.encoding");
		}
		return encoding;
	}

	/**
	 * 获得是否需要编译debug信息.
	 */
	public static boolean getDebug()
	{
		String debug = Utility.getProperty(ANT_TOOL_CONFIG_PREFIX + "debug");
		if (debug == null)
		{
			return true;
		}
		return "true".equalsIgnoreCase(debug);
	}

	/**
	 * 获得源文件的路径.
	 */
	public static String getSrcPath()
	{
		String srcPath = Utility.getProperty(ANT_TOOL_CONFIG_PREFIX + "srcPath");
		if (srcPath == null)
		{
			srcPath = getTempPath();
		}
		return resolvePath(srcPath);
	}

	/**
	 * 获得编译文件的输出路径.
	 */
	public static String getDestPath()
	{
		String destPath = Utility.getProperty(ANT_TOOL_CONFIG_PREFIX + "destPath");
		if (destPath == null)
		{
			destPath = getTempPath();
		}
		return resolvePath(destPath);
	}

	/**
	 * 获取系统临时目录路径.
	 */
	private static String getTempPath()
	{
		return (new File(System.getProperty("java.io.tmpdir"), "eternaCG")).getPath();
	}

	/**
	 * 根据ANT_TOOL_UID_PATH_PROPERTY设置的值来判断是否需要加上唯一的路径.
	 *
	 * @see #ANT_TOOL_UID_PATH_PROPERTY
	 */
	private static String resolvePath(String path)
	{
		String addUID = Utility.getProperty(ANT_TOOL_UID_PATH_PROPERTY);
		if (addUID == null)
		{
			return path;
		}
		if ("true".equalsIgnoreCase(addUID))
		{
			return (new File(path, uidPath)).getPath();
		}
		else
		{
			return path;
		}
	}
	private static String uidPath = Utility.getUID();

	/**
	 * 获得使用的编译器名称.
	 */
	public static String getCompiler()
	{
		String compiler = Utility.getProperty(ANT_TOOL_CONFIG_PREFIX + "compiler");
		if (compiler == null)
		{
			compiler = "extJavac";
		}
		return compiler;
	}

	/**
	 * <code>CompileClassLoader</code>的缓存, 主键为编译的输出目录+parent
	 */
	private static Map cclCache = new ReferenceMap(ReferenceMap.HARD, ReferenceMap.WEAK);

	/**
	 * 从缓存中获取<code>CompileClassLoader</code>, 如果没有则创建一个.
	 */
	private static synchronized CompileClassLoader getClassLoader(
			File basePath, ClassLoader parent)
	{
		CCL_KEY key = new CCL_KEY(basePath, parent);
		CompileClassLoader ccl = (CompileClassLoader) cclCache.get(key);
		if (ccl == null)
		{
			ccl = new CompileClassLoader(parent, basePath);
			cclCache.put(key, ccl);
		}
		return ccl;
	}

	private static class MyJavac extends Javac
	{
		public void setSrcFile(File file)
		{
			this.compileList = new File[]{file};
		}

		public void compile()
		{
			super.compile();
		}

	}

	/**
	 * <code>CompileClassLoader</code>缓存的主键类
	 */
	private static class CCL_KEY
	{
		private final File basePath;
		private int hashCode;

		/**
		 * 这里使用<code>WeakReference</code>来引用父ClassLoader, 这样就不会影响其正常的释放.
		 */
		private final WeakReference parent;

		public CCL_KEY(File basePath, ClassLoader parent)
		{
			this.basePath = basePath;
			this.parent = new WeakReference(parent);
			this.hashCode = basePath == null ? 0 : basePath.hashCode();
			this.hashCode ^= parent == null ? 0 : parent.hashCode();
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
			if (obj instanceof CCL_KEY)
			{
				CCL_KEY other = (CCL_KEY) obj;
				return Utility.objectEquals(this.basePath, other.basePath)
						&& Utility.objectEquals(this.parent.get(), other.parent.get());
			}
			return false;
		}

	}

	private static class CompileClassLoader extends ClassLoader
	{
		private final File basePath;
		private final Map msgCache = new HashMap();
		private Method defineMethod;

		public CompileClassLoader(ClassLoader parent, File basePath)
		{
			super(parent);
			this.basePath = basePath;
			try
			{
				Class cl = Class.forName("java.lang.ClassLoader");
				Class[] paramTypes = {String.class, byte[].class, int.class, int.class};
				this.defineMethod = cl.getDeclaredMethod("defineClass", paramTypes);
				this.defineMethod.setAccessible(true);
			}
			catch (Throwable ex)
			{
				this.defineMethod = null;
			}
		}

		public void addMessage(String className, String msg)
		{
			this.msgCache.put(className, msg);
		}

		protected Class findClass(String name)
				throws ClassNotFoundException
		{
			try
			{
				File f = new File(this.basePath, name.replace('.', File.separatorChar) + ".class");
				if (f.isFile())
				{
					FileInputStream fis = new FileInputStream(f);
					byte[] buf = new byte[(int) f.length()];
					fis.read(buf);
					Class c = null;
					if (this.defineMethod != null)
					{
						try
						{
							Object[] args = {name, buf, new Integer(0), new Integer(buf.length)};
							c = (Class) this.defineMethod.invoke(this.getParent(), args);
						}
						catch (Throwable ex)
						{
							c = this.defineClass(name, buf, 0, buf.length);
						}
					}
					else
					{
						c = this.defineClass(name, buf, 0, buf.length);
					}
					// 类载入成功, 可以将缓存的消息清除.
					this.msgCache.remove(name);
					return c;
				}
				else
				{
					Class c = super.findClass(name);
					if (c == null)
					{
						throw new ClassNotFoundException("name:" + name + ", file:" + f
								+ ", message:" + this.msgCache.get(name));
					}
					return c;
				}
			}
			catch (Exception ex)
			{
				throw new ClassNotFoundException("message:" + this.msgCache.get(name), ex);
			}
		}

		public URL findResource(String name)
		{
			File f = new File(this.basePath, name.replace('.', File.separatorChar) + ".class");
			if (f.isFile())
			{
				try
				{
					return f.toURL();
				}
				catch (MalformedURLException ex) {}
			}
			return super.getResource(name);
		}

		protected Enumeration findResources(String name)
				throws IOException
		{
			File f = new File(this.basePath, name.replace('.', File.separatorChar) + ".class");
			if (f.isFile())
			{
				return Collections.enumeration(Arrays.asList(new URL[]{f.toURL()}));
			}
			return super.findResources(name);
		}

	}

	private static class CompileLogger
			implements BuildLogger
	{
		private final StringAppender out = StringTool.createStringAppender();

		public synchronized void messageLogged(BuildEvent event)
		{
			Throwable ex = event.getException();
			int level = event.getPriority();
			if (level <= Project.MSG_WARN || ex != null)
			{
				if (level == Project.MSG_ERR || ex != null)
				{
					this.out.append("Error:").appendln();
				}
				else
				{
					this.out.append("Warn:").appendln();
				}
				this.out.append(event.getMessage());
				if (ex != null)
				{
					this.out.appendln().append("Exception:").appendln();
					StringTool.appendStackTrace(ex, this.out);
				}
				this.out.appendln();
			}
		}

		public void buildStarted(BuildEvent event)
		{
			this.messageLogged(event);
		}

		public void buildFinished(BuildEvent event)
		{
			this.messageLogged(event);
		}

		public void targetStarted(BuildEvent event)
		{
			this.messageLogged(event);
		}

		public void targetFinished(BuildEvent event)
		{
			this.messageLogged(event);
		}

		public void taskStarted(BuildEvent event)
		{
			this.messageLogged(event);
		}

		public void taskFinished(BuildEvent event)
		{
			this.messageLogged(event);
		}

		public String toString()
		{
			return this.out.toString();
		}

		public void setMessageOutputLevel(int level)
		{
		}

		public void setEmacsMode(boolean emacsMode)
		{
		}

		public void setOutputPrintStream(PrintStream output)
		{
		}

		public void setErrorPrintStream(PrintStream err)
		{
		}

	}

}