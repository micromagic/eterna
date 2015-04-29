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

package self.micromagic.eterna.digester2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import self.micromagic.eterna.dao.preparer.CreaterManager;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.Factory;
import self.micromagic.eterna.share.FactoryContainer;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;
import self.micromagic.util.container.ThreadCache;
import self.micromagic.util.ref.IntegerRef;
import self.micromagic.util.ref.StringRef;

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
	private static final Map fcCache = new LinkedHashMap();

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
			// 这里需要先生成, 再注册及初始化
			// 可防止初始化时再调用getGlobalContainer时重复注册
			globalContainer = createFactoryContainer(GLOBAL_ID, config, parents,
					null, attrs, ContainerManager.class.getClassLoader(), null, false);
			registerFactoryContainer(globalContainer);
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
	 * 检查给出的URI定义是否已被载入过.
	 */
	public static boolean checkResourceURI(String uri)
	{
		FactoryContainer fc = getCurrentContainer();
		if (fc == null)
		{
			return false;
		}
		synchronized (fc)
		{
			Object obj = fc.getAttribute(FactoryContainer.URIS_FLAG);
			if (obj == null)
			{
				obj = new HashMap();
				fc.setAttribute(FactoryContainer.URIS_FLAG, obj);
			}
			boolean exists = false;
			if (obj instanceof Map)
			{
				if (!(exists = ((Map) obj).containsKey(uri)))
				{
					((Map) obj).put(uri, Boolean.TRUE);
				}
			}
			else if (obj instanceof Set)
			{
				if (!(exists = ((Set) obj).contains(uri)))
				{
					((Set) obj).add(uri);
				}
			}
			return exists;
		}
	}

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
	 * 获取下一个序列号.
	 */
	public static int getNextSerial()
	{
		ThreadCache cache = ThreadCache.getInstance();
		IntegerRef serial = (IntegerRef) cache.getProperty(THREAD_SERIAL_KEY);
		if (serial == null)
		{
			serial = new IntegerRef(1);
			cache.setProperty(THREAD_SERIAL_KEY, serial);
		}
		return serial.value++;
	}
	/**
	 * 重置序列号.
	 */
	public static void resetSerial()
	{
		ThreadCache.getInstance().removeProperty(THREAD_SERIAL_KEY);
	}
	/**
	 * 在线程中存储当序列号的键值.
	 */
	private static final String THREAD_SERIAL_KEY = "eterna.serial";

	/**
	 * 添加一个在工厂容器重新初始化时需要清除的属性名.
	 */
	public static synchronized void addContainerAttributeClearName(String name)
	{
		// 复制后添加, 不影响读取
		List tmp = new ArrayList(containerAttributeClearNames);
		tmp.add(name);
		containerAttributeClearNames = tmp;
	}
	private static List containerAttributeClearNames = new ArrayList();

	/**
	 * 从工厂容器中清除需要清除的属性.
	 */
	public static void clearSettedAttribute(FactoryContainer container)
	{
		List tmp = containerAttributeClearNames;
		Iterator itr = tmp.iterator();
		while (itr.hasNext())
		{
			container.removeAttribute((String) itr.next());
		}
	}

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
		if (index == -1 || index <= 1)
		{
			// 没有":"或只有一个字符, 如: d:时作为文件资源
			flag = "file";
		}
		else
		{
			flag = config.substring(0, index);
		}
		ConfigResource res = (ConfigResource) crCache.get(flag);
		if (res == null)
		{
			//throw new EternaException("Can't create ConfigResource for flag [" + flag + "].");
			return (new UrlResource()).create(config, container);
		}
		return res.create(config, container);
	}

	/**
	 * 根据配置字符串创建一个配置资源对象.
	 *
	 * @param config     资源的配置
	 */
	public static ConfigResource createResource(String config)
	{
		return createResource(config, getGlobalContainer());
	}

	/**
	 * 根据配置字符串创建一个classpath中的配置资源对象.
	 *
	 * @param config     资源的配置
	 * @param loader     用于获取classpath的ClassLoader
	 */
	public static ConfigResource createClassPathResource(String config, ClassLoader loader)
	{
		if (config.indexOf(':') == -1)
		{
			config = "cp:".concat(config);
		}
		return (new ClassPathResource()).create(config, loader);
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
