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

package self.micromagic.eterna.digester2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import self.micromagic.eterna.dao.preparer.CreaterManager;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.Factory;
import self.micromagic.eterna.share.FactoryContainer;
import self.micromagic.util.IntegerRef;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringRef;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;
import self.micromagic.util.container.ThreadCache;

/**
 * FactoryContainer对象的管理者.
 */
public class ContainerManager
{
	/**
	 * 全局工厂容器的初始化配置的键值.
	 */
	public static final String INIT_CONFIG_FLAG = "eterna.initConfig";
	/**
	 * 全局工厂容器的初始化的父配置的前缀.
	 * 样例 <p>
	 * eterna.initParent1=...<p>
	 * eterna.initParent2=...<p>
	 */
	public static final String INIT_PARENT_PREFIX = "eterna.initParent";
	/**
	 * 全局初始化时是否要载入默认的配置.
	 * 默认值为true.
	 */
	public static final String LOAD_DEFAULT_FLAG = "eterna.init.loadDefault";
	/**
	 * 全局初始化时要载入的默认配置.
	 */
	public static final String DEFAULT_CONFIG_FLAG
			= "cp:self/micromagic/eterna/share/eterna_share.xml";//;cp:eterna_global.xml;";

	/**
	 * 根据容器id获取一个已注册的工厂容器.
	 */
	public static FactoryContainer getFactoryContainer(String id)
	{
		FactoryContainer fc = getFactoryContainer0(id);
		if (fc == null)
		{
			throw new EternaException("The FactoryContainer [" + id + "] not found.");
		}
		return fc;
	}
	private static FactoryContainer getFactoryContainer0(String id)
	{
		FactoryContainer fc = (FactoryContainer) fcCache.get(id);
		if (fc == null)
		{
			// 未获取到则在同步状态下再获取一次
			synchronized (fcCache)
			{
				fc = (FactoryContainer) fcCache.get(id);
			}
		}
		return fc;
	}

	/**
	 * 重新初始化所有已注册的工厂容器.
	 *
	 * @param msg  出参, 初始化时的出错信息
	 */
	public static void reInitAll(StringRef msg)
	{
		synchronized (fcCache)
		{
			Iterator itr = fcCache.values().iterator();
			while (itr.hasNext())
			{
				((FactoryContainer) itr.next()).reInit(msg);
			}
		}
	}

	/**
	 * 注册一个工厂容器.
	 * 注: 注册后会执行工厂容器的初始化方法.
	 */
	public static void registerFactoryContainer(FactoryContainer container)
	{
		registerFactoryContainer(container, false);
	}

	/**
	 * 注册一个工厂容器.
	 * 注: 注册后会执行工厂容器的初始化方法.
	 *
	 * @param overwrite  当已有同名的工厂容器存在时, 是否覆盖
	 * @return  是否覆盖了原来已注册的工厂容器
	 */
	public static boolean registerFactoryContainer(FactoryContainer container, boolean overwrite)
	{
		if (container == null)
		{
			return false;
		}
		String id = container.getId();
		boolean r;
		synchronized (fcCache)
		{
			if (!overwrite && fcCache.containsKey(id))
			{
				throw new EternaException("The FactoryContainer [" + id + "] has registerd.");
			}
			FactoryContainer old = (FactoryContainer) fcCache.put(id, container);
			// 这里是赋值兼判断
			if (r = old != null)
			{
				old.destroy();
			}
		}
		if (!container.isInitialized())
		{
			container.reInit();
		}
		return r;
	}
	private static final Map fcCache = new HashMap();

	/**
	 * 注销一个工厂容器.
	 *
	 * @param id  工厂容器的id
	 * @return  注销成功则返回true, 否则返回false
	 */
	public static boolean deregisterFactoryContainer(String id)
	{
		FactoryContainer fc;
		synchronized (fcCache)
		{
			fc = (FactoryContainer) fcCache.remove(id);
			if (fc != null)
			{
				if (GLOBAL_ID.equals(id))
				{
					// 如果是注销全局工厂容器, 则要将全局工厂容器设为null
					globalContainer = null;
				}
			}
		}
		if (fc != null)
		{
			fc.destroy();
			return true;
		}
		return false;
	}

	/**
	 * 创建一个工厂容器.
	 *
	 * @param baseClass  基础类
	 */
	public static FactoryContainer createFactoryContainer(Class baseClass)
	{
		FactoryContainer fc;
		synchronized (fcCache)
		{
			String id = baseClass.getName();
			fc = getFactoryContainer0(id);
			if (fc != null)
			{
				if (baseClass != fc.getAttribute(BASE_CLASS_FLAG))
				{
					// 如果baseClass不同, 则注销原来的工厂容器
					fc = null;
					deregisterFactoryContainer(id);
				}
			}
			if (fc == null)
			{
				String config = "cp:" + id.replace('.', '/') + ".xml";
				ClassLoader loader = baseClass.getClassLoader();
				Map attrs = new HashMap();
				attrs.put(BASE_CLASS_FLAG, baseClass);
				fc = createFactoryContainer(id, config, null, null, attrs,
						loader, getGlobalContainer(), true);
			}
		}
		return fc;
	}
	/**
	 * FactoryContainer的属性中存放基础类的键值.
	 */
	private static final String BASE_CLASS_FLAG = "baseClass";

	/**
	 * 创建一个工厂容器.
	 *
	 * @param id
	 * @param config    初始化的配置
	 * @param loader    载入类及配置需要使用的classloader
	 */
	public static FactoryContainer createFactoryContainer(String id, String config, ClassLoader loader)
	{
		return createFactoryContainer(id, config, null, null, null, loader,
				getGlobalContainer(), true);
	}

	/**
	 * 创建一个工厂容器.
	 *
	 * @param id
	 * @param config    初始化的配置
	 * @param parents   初始化的父配置
	 * @param digester  配置文件的解析器
	 * @param attrs     需要添加的属性
	 * @param loader    载入类及配置需要使用的classloader
	 * @param share     共享的工厂容器
	 * @param register  是否需要注册到容器池中
	 */
	public static FactoryContainer createFactoryContainer(String id, String config, String[] parents,
			Digester digester, Map attrs, ClassLoader loader, FactoryContainer share, boolean register)
	{
		if (digester == null)
		{
			digester = Digester.getInstance();
		}
		FactoryContainerImpl fc = new FactoryContainerImpl();
		fc.setId(id);
		fc.setDigester(digester);
		if (attrs != null)
		{
			int count = attrs.size();
			Iterator itr = attrs.entrySet().iterator();
			for (int i = 0; i < count; i++)
			{
				Map.Entry e = (Map.Entry) itr.next();
				fc.setAttribute((String) e.getKey(), e.getValue());
			}
		}
		if (loader == null)
		{
			loader = Utility.getContextClassLoader();
		}
		fc.setClassLoader(loader);
		if (share != null)
		{
			fc.setShareContainer(share);
		}
		fc.setConfig(config, parents);
		if (register)
		{
			registerFactoryContainer(fc);
		}
		return fc;
	}

	/**
	 * 获取全局的工厂容器.
	 */
	public static FactoryContainer getGlobalContainer()
	{
		if (globalContainer != null)
		{
			return globalContainer;
		}
		List parentList = new ArrayList();
		if ("true".equalsIgnoreCase(Utility.getProperty(LOAD_DEFAULT_FLAG, "true")))
		{
			parentList.add(DEFAULT_CONFIG_FLAG);
		}
		for (int i = 1; true; i++)
		{
			String pStr = Utility.getProperty(INIT_PARENT_PREFIX + i);
			if (pStr == null)
			{
				// 已没有父配置则退出
				break;
			}
			parentList.add(pStr);
		}
		String config = Utility.getProperty(INIT_CONFIG_FLAG);
		int size = parentList.size();
		if (config == null)
		{
			if (size == 0)
			{
				Digester.log.warn("No global FactoryContainer config.");
				return null;
			}
			config = (String) parentList.remove(--size);
		}
		String[] parents = null;
		if (size > 0)
		{
			parents = new String[size];
			parentList.toArray(parents);
		}
		synchronized (fcCache)
		{
			if (globalContainer != null)
			{
				return globalContainer;
			}
			Map attrs = new HashMap();
			attrs.put(CreaterManager.ATTR_CREATER, new HashMap());
			globalContainer = createFactoryContainer(GLOBAL_ID, config, parents,
					null, attrs, ContainerManager.class.getClassLoader(), null, true);
		}
		return globalContainer;
	}
	/**
	 * 全局工厂容器.
	 */
	private static FactoryContainer globalContainer;
	/**
	 * 全局工厂容器的id.
	 */
	private static final String GLOBAL_ID = "eterna.global";

	/**
	 * 获取当前的FactoryContainer.
	 */
	public static FactoryContainer getCurrentContainer()
	{
		return (FactoryContainer) ThreadCache.getInstance().getProperty(THREAD_COONTAINER_KEY);
	}
	/**
	 * 设置当前的FactoryContainer.
	 */
	public static void setCurrentContainer(FactoryContainer container)
	{
		setCurrentContainer(container, true);
	}
	/**
	 * 设置当前的FactoryContainer.
	 *
	 * @param withFactory  是否需要同时设置当前的工厂
	 */
	public static void setCurrentContainer(FactoryContainer container, boolean withFactory)
	{
		ThreadCache.getInstance().setProperty(THREAD_COONTAINER_KEY, container);
		if (container != null && withFactory)
		{
			Factory f = container.getFactory();
			if (f instanceof EternaFactory)
			{
				setCurrentFactory(f);
			}
		}
	}
	/**
	 * 在线程中存储当前FactoryContainer的键值.
	 */
	private static final String THREAD_COONTAINER_KEY = "eterna.current.container";

	/**
	 * 获取当前的EternaFactory.
	 */
	public static Factory getCurrentFactory()
	{
		return (Factory) ThreadCache.getInstance().getProperty(THREAD_FACTORY_KEY);
	}
	/**
	 * 设置当前的EternaFactory.
	 */
	public static void setCurrentFactory(Factory factory)
	{
		ThreadCache.getInstance().setProperty(THREAD_FACTORY_KEY, factory);
	}
	/**
	 * 在线程中存储当前EternaFactory的键值.
	 */
	private static final String THREAD_FACTORY_KEY = "eterna.current.factory";

	/**
	 * 获取当前的ConfigResource.
	 */
	public static ConfigResource getCurrentResource()
	{
		return (ConfigResource) ThreadCache.getInstance().getProperty(THREAD_RESOURCE_KEY);
	}
	/**
	 * 设置当前的ConfigResource.
	 */
	public static void setCurrentResource(ConfigResource res)
	{
		ThreadCache.getInstance().setProperty(THREAD_RESOURCE_KEY, res);
	}
	/**
	 * 在线程中存储当前ConfigResource的键值.
	 */
	private static final String THREAD_RESOURCE_KEY = "eterna.current.resource";

	/**
	 * 初始化父配置的level等级, 0为基本配置, 1为第一级, 2为第二级 ...
	 */
	public static int getSuperInitLevel()
	{
		IntegerRef level = (IntegerRef) ThreadCache.getInstance().getProperty(
				THREAD_INIT_LEVEL_KEY);
		return level != null ? level.value : 0;
	}
	/**
	 * 设置初始化父配置的level等级.
	 */
	static void setSuperInitLevel(int l)
	{
		ThreadCache cache = ThreadCache.getInstance();
		IntegerRef level = (IntegerRef) cache.getProperty(THREAD_INIT_LEVEL_KEY);
		if (level != null)
		{
			level.value = l;
		}
		else
		{
			cache.setProperty(THREAD_INIT_LEVEL_KEY, new IntegerRef(l));
		}
	}
	/**
	 * 在线程中存储当前初始化父配置的level等级的键值.
	 */
	private static final String THREAD_INIT_LEVEL_KEY = "eterna.init.level";

	/**
	 * 根据配置字符串创建一个配置资源对象.
	 *
	 * @param config     资源的配置
	 * @param container  需要获取配置对象的FactoryContainer
	 */
	public static ConfigResource createResource(String config, FactoryContainer container)
	{
		if (StringTool.isEmpty(config))
		{
			throw new EternaException("The config can't be empty");
		}
		int index = config.indexOf(':');
		String flag;
		if (index == -1 || index == 1)
		{
			// 没有":"获取只有一个字符, 如: d:时作为文件资源
			flag = "file";
		}
		else
		{
			flag = config.substring(0, index);
		}
		ConfigResource res = (ConfigResource) crCache.get(flag);
		if (res == null)
		{
			throw new EternaException("Can't create ConfigResource for flag [" + flag + "].");
		}
		return res.create(config, container);
	}

	/**
	 * 注册一个配置资源对象.
	 *
	 * @param flag  配置资源对象的前缀标识
	 * @param res   配置资源对象
	 */
	public static void registerConfigResource(String flag, ConfigResource res)
	{
		if (StringTool.isEmpty(flag) || flag.length() == 1)
		{
			throw new EternaException(
					"The ConfigResource flag can't be empty or one character.");
		}
		synchronized (crCache)
		{
			if (crCache.containsKey(flag))
			{
				throw new EternaException("ConfigResource [" + flag + "] has registered.");
			}
			crCache.put(flag, res);
		}
	}
	private static Map crCache = new HashMap();
	static
	{
		ClassPathResource cpr = new ClassPathResource();
		registerConfigResource("cp", cpr);
		registerConfigResource("classpath", cpr);
		registerConfigResource("web", new WebResource());
		registerConfigResource("file", new FileResource());
	}

}


/**
 * 抽象的资源.
 */
class AbstractResource
{
	protected String config;
	protected FactoryContainer container;
	protected File resFile;

	/**
	 * 初始化各个变量.
	 */
	protected void init(String config, FactoryContainer container, URL url)
	{
		this.config = config;
		this.container = container;
		if (url != null)
		{
			if ("file".equals(url.getProtocol()))
			{
				File f = new File(url.getFile());
				if (f.isFile())
				{
					this.resFile = f;
				}
			}
		}
	}

	public long getLastModified()
	{
		if (this.resFile != null)
		{
			return this.resFile.lastModified();
		}
		return -1L;
	}

	/**
	 * 检查给出的路径是否包含前缀标识.
	 * 如果有则创建一个ConfigResource, 否则返回null.
	 */
	protected ConfigResource checkFlag(String path)
	{
		int index = path.indexOf(':');
		if (index != -1)
		{
			return ContainerManager.createResource(path, this.container);
		}
		return null;
	}


	/**
	 * 对一个路径进行解析, 将路径中的各个部分分解成数组返回.
	 *
	 * @param path    需要解析的路径
	 * @param pCount  出参, 路径需要向上递归的层数, 如: ../../test
	 *                如果是绝对路径则值为-1
	 * @param err     出参, 如果路径有错误, 可从这里获取出错信息
	 * @return  如果解析成功的话则返回解析完的数组, 否则返回null
	 */
	protected static String[] parsePath(String path, IntegerRef pCount)
	{
		if (StringTool.isEmpty(path) || StringTool.isEmpty(path = path.trim()))
		{
			return StringTool.EMPTY_STRING_ARRAY;
		}
		boolean absolutePath = path.charAt(0) == '/';
		String[] pArr = StringTool.separateString(path, "/", true);
		int upCount = 0;
		int currentIndex = 0;
		for (int i = 0; i < pArr.length; i++)
		{
			String unit = pArr[i];
			if (unit.length() == 0)
			{
				// 多个分隔符如"//", 作为一个处理
			}
			if ("..".equals(unit))
			{
				// 如果为".."目录需要向前一层
				if (currentIndex > 0)
				{
					currentIndex--;
				}
				else
				{
					upCount++;
				}
			}
			else if (!".".equals(unit))
			{
				// 如果不为"."目录需要向后一层
				pArr[currentIndex++] = unit;
			}
		}
		if (pCount != null)
		{
			pCount.value = absolutePath ? -1 : upCount;
		}
		String[] result;
		if (currentIndex < pArr.length)
		{
			// 如果实际的目录数小于已切分的单元, 则要重新构造数组
			result = new String[currentIndex];
			System.arraycopy(pArr, 0, result, 0, currentIndex);
		}
		else
		{
			result = pArr;
		}
		return result;
	}

	/**
	 * 将一个路径单元合并成一个路径.
	 *
	 * @param rootArr  根路径单元
	 * @param pCount   路径需要向上递归的层数
	 * @param unitArr  需要合并成路径的路径单元
	 * @return  合并后的路径
	 */
	protected static String mergePath(String[] rootArr, int pCount,
			String[] unitArr)
	{
		StringAppender buf = StringTool.createStringAppender(32);
		if (pCount >= 0 && rootArr != null && rootArr.length > 0)
		{
			int count = rootArr.length - pCount;
			for (int i = 0; i < count; i++)
			{
				buf.append('/').append(rootArr[i]);
			}
		}
		for (int i = 0; i < unitArr.length; i++)
		{
			buf.append('/').append(unitArr[i]);
		}
		return buf.length() > 0 ? buf.toString() : "/";
	}

	/**
	 * 去除路径起始部分的分隔符"/";
	 */
	protected static String trimBeginSplit(String path)
	{
		if (StringTool.isEmpty(path))
		{
			return path;
		}
		int count = path.length();
		int beginIndex = 0;
		for (int i = 0; i < count; i++)
		{
			if (path.charAt(i) != '/')
			{
				beginIndex = i;
				break;
			}
			else
			{
				beginIndex = i + 1;
			}
		}
		return beginIndex == 0 ? path : path.substring(beginIndex);
	}

}

/**
 * classpath中的资源.
 */
class ClassPathResource extends AbstractResource
		implements ConfigResource
{
	public ConfigResource create(String config, FactoryContainer container)
	{
		int index = config.indexOf(':');
		ClassPathResource res = new ClassPathResource();
		res.path = trimBeginSplit(config.substring(index + 1).trim());
		ClassLoader loader = (ClassLoader) container.getAttribute(
				FactoryContainer.CLASSLOADER_FLAG);
		if (loader == null)
		{
			loader = this.getClass().getClassLoader();
		}
		res.loader = loader;
		res.url = loader.getResource(res.path);
		res.init(config, container, res.url);
		return res;
	}

	private URL url;
	private String path;
	private ClassLoader loader;

	public String getURI()
	{
		if (this.url != null)
		{
			return this.url.toString();
		}
		return null;
	}

	public InputStream getAsStream()
	{
		return this.loader.getResourceAsStream(this.path);
	}

	public ConfigResource getResource(String path)
	{
		ConfigResource res = this.checkFlag(path);
		if (res != null)
		{
			return res;
		}
		IntegerRef pCount = new IntegerRef();
		String[] pArr = parsePath(path, pCount);
		if (pCount.value == -1)
		{
			return this.create(path.substring(1), this.container);
		}
		String mPath = mergePath(parsePath(this.path, null), pCount.value + 1, pArr);
		return this.create(mPath.substring(1), this.container);
	}

}

/**
 * 文件系统中的资源.
 */
class FileResource extends AbstractResource
		implements ConfigResource
{
	public ConfigResource create(String config, FactoryContainer container)
	{
		int index = config.indexOf(':');
		try
		{
			File file;
			if (index != -1 && "file".equalsIgnoreCase(config.substring(0, index)))
			{
				URL url = new URL(config);
				file = new File(url.getFile());
			}
			else
			{
				boolean winSys = File.separatorChar != '/';
				if (winSys)
				{
					config = config.replace(File.separatorChar, '/');
				}
				IntegerRef pCount = new IntegerRef();
				String tmpPath = mergePath(null, 0, parsePath(config, pCount));
				if (pCount.value != -1 || index != -1)
				{
					// 不是根路径或带有盘符去除起始部分的"/"
					tmpPath = trimBeginSplit(tmpPath);
				}
				if (winSys)
				{
					tmpPath = tmpPath.replace('/', File.separatorChar);
				}
				if (pCount.value != 0)
				{
					// 为根路径或相对父路径, 需要向上获取当前路径的父路径
					File cFile = (new File("")).getAbsoluteFile();
					file = new File(getParent(cFile, pCount.value), tmpPath);
				}
				else
				{
					file = new File(tmpPath);
				}
			}
			file = file.getAbsoluteFile();
			FileResource res = new FileResource();
			res.resFile = file;
			res.init(config, container, null);
			return res;
		}
		catch (IOException ex)
		{
			throw new EternaException(ex);
		}
	}

	public String getURI()
	{
		if (this.resFile.isFile())
		{
			return this.resFile.toString();
		}
		return null;
	}

	public InputStream getAsStream()
	{
		try
		{
			if (this.resFile.isFile())
			{
				return new FileInputStream(this.resFile);
			}
			return null;
		}
		catch (IOException ex)
		{
			throw new EternaException(ex);
		}
	}

	public ConfigResource getResource(String path)
	{
		ConfigResource res = this.checkFlag(path);
		if (res != null)
		{
			return res;
		}
		IntegerRef pCount = new IntegerRef();
		String[] pArr = parsePath(path, pCount);
		File pFile = getParent(this.resFile.getParentFile(), pCount.value);
		File tFile = this.makeFileByUnit(pFile, pArr);
		return this.create(tFile.getPath(), this.container);
	}

	private File makeFileByUnit(File rootPath, String[] units)
	{
		String tmpPath = trimBeginSplit(mergePath(null, 0, units));
		if (File.separatorChar != '/')
		{
			tmpPath = tmpPath.replace('/', File.separatorChar);
		}
		return new File(rootPath, tmpPath);
	}

	/**
	 * 获取父路径.
	 *
	 * @param pCount  父路径的层数, -1表示获取根路径
	 */
	private static File getParent(File file, int pCount)
	{
		File result = file;
		for (int i = 0; i < pCount || pCount == -1; i++)
		{
			File p = result.getParentFile();
			if (p == null)
			{
				// 已到根路径
				return result;
			}
			result = p;
		}
		return result;
	}

}


/**
 * web中的资源.
 */
class WebResource extends AbstractResource
		implements ConfigResource
{
	public ConfigResource create(String config, FactoryContainer container)
	{
		int index = config.indexOf(':');
		WebResource res = new WebResource();
		res.path = config.substring(index + 1);
		if (res.path.charAt(0) != '/')
		{
			throw new EternaException("Error path [" + res.path + "] for web.");
		}
		res.context = (javax.servlet.ServletContext) container.getAttribute(
				FactoryContainer.SERVLET_FLAG);
		if (res.context == null)
		{
			String msg = "Not found servlet with key \""
					+ FactoryContainer.SERVLET_FLAG+ "\" in FactoryContainer ["
					+ container.getId() + "].";
			throw new EternaException(msg);
		}
		try
		{
			res.url = res.context.getResource(res.path);
		}
		catch (IOException ex)
		{
			throw new EternaException(ex);
		}
		res.init(config, container, res.url);
		return res;
	}

	private URL url;
	private String path;
	private javax.servlet.ServletContext context;

	public String getURI()
	{
		if (this.url != null)
		{
			return this.url.toString();
		}
		return null;
	}

	public InputStream getAsStream()
	{
		return this.context.getResourceAsStream(this.path);
	}

	public ConfigResource getResource(String path)
	{
		ConfigResource res = this.checkFlag(path);
		if (res != null)
		{
			return res;
		}
		IntegerRef pCount = new IntegerRef();
		String[] pArr = parsePath(path, pCount);
		if (pCount.value == -1)
		{
			return this.create(path, this.container);
		}
		String mPath = mergePath(parsePath(this.path, null), pCount.value + 1, pArr);
		return this.create(mPath, this.container);
	}

}