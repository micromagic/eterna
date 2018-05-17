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
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;

import self.micromagic.cg.ClassGenerator;
import self.micromagic.eterna.digester2.ConfigResource;
import self.micromagic.eterna.digester2.ContainerManager;
import self.micromagic.eterna.digester2.FactoryContainerImpl;
import self.micromagic.eterna.share.FactoryContainer;
import self.micromagic.util.converter.BooleanConverter;
import self.micromagic.util.converter.ConverterFinder;
import self.micromagic.util.ref.StringRef;

/**
 * 供内部继承使用的配置属性的管理器.
 */
abstract class InternalPropertiesManager
{
	/**
	 * 默认的配置文件所在位置.
	 */
	static final String DEFAULT_PROP_LOCAL = "cp:/eterna.config";

	/**
	 * 存放父配置文件名的属性.
	 */
	public static final String PARENT_PROPERTIES = "_parent.properties";

	/**
	 * 存放子配置文件名的属性.
	 */
	public static final String CHILD_PROPERTIES = "_child.properties";

	/**
	 * 存放优先使用父配置的名称列表的属性.
	 */
	public static final String PARENT_FIRST_NAMES = "_parentFirst.propertyNames";

	/**
	 * 设置是否需要将defaultProperties的值作为默认值.
	 * 当本配置管理器中的值不存在时, 会再读取defaultProperties中的值. 如果
	 * 父配置管理器中设置为true, 那当前的值就会默认为false.
	 * 当未设置此属性时, 默认值为true.
	 * 注: defaultProperties中的值的修改不会触发当前配置管理器的事件, 只是
	 * 能够在读取时被取到.
	 */
	public static final String NEED_DEFAULT = "_need.default";

	/**
	 * 是否对成员对象使用弱引用.
	 * 如果需要使用则可将这个变量设为true, 否则默认值为false,
	 * 表示使用软引用.
	 */
	public static boolean weakRefMember = false;

	/**
	 * 动态属性名称的前缀: "${"
	 */
	private static final String DYNAMIC_PROPNAME_PREFIX = "${";

	/**
	 * 动态属性名称的后缀:: "}"
	 */
	private static final String DYNAMIC_PROPNAME_SUFFIX = "}";

	/**
	 * 日志的名称. <p>
	 * 这里需要在运行时再调用Utility对象, 以免在初始化的时候循环引用.
	 */
	private static final String LOGGER_NAME = "eterna.util";

	// 这里不先初始化日志对象, 在使用的时候初始化, 可避免循环初始化
	private static Log log;

	/**
	 * 全局默认的配置值.
	 */
	private static Properties defaultProperties = System.getProperties();

	/**
	 * 配置文件资源对象.
	 */
	private final ConfigResource propResource;

	/**
	 * 当前所读取的配置属性.
	 */
	private final Properties properties = new Properties();

	/**
	 * 属性变化监听者列表.
	 */
	private final List plList = new LinkedList();

	/**
	 * 配置属性的管理器中, 默认的属性变化监听者.
	 */
	private final DefaultPropertyListener defaultPL = new DefaultPropertyListener();

	/**
	 * 存放属性绑定时设置的默认值.
	 */
	protected Map defaultValues = new HashMap();

	/**
	 * 是否需要将系统的properties的值作为默认值.
	 */
	private boolean systemDefault;

	/**
	 * 父配置管理器.
	 */
	private final InternalPropertiesManager parent;

	/**
	 * 构造一个配置属性的管理器.
	 *
	 * @param resource    配置文件资源对象
	 * @param parent      当前配置管理器所继承的父配置管理器
	 * @param needLoad    是否需要在初始化完成之后立刻执行配置加载
	 */
	protected InternalPropertiesManager(ConfigResource resource,
			InternalPropertiesManager parent, boolean needLoad)
	{
		if (resource == null)
		{
			resource = createBaseResource(DEFAULT_PROP_LOCAL, null);
		}
		this.propResource = resource;
		this.addPropertyListener0(this.defaultPL);
		this.parent = parent;
		if (this.parent != null)
		{
			// 将监听器绑定到parent上
			this.parent.addPropertyListener0(new ParentPropertyListener(this));
		}
		if (needLoad)
		{
			this.reload(false, null);
		}
	}

	/**
	 * 设置全局默认的资源配置.
	 */
	public static void setDefaultProperties(Properties properties)
	{
		if (properties != null)
		{
			defaultProperties = properties;
		}
	}

	/**
	 * 创建一个基础配置资源对象.
	 */
	static ConfigResource createBaseResource(String propName, ClassLoader loader)
	{
		if (propName == null)
		{
			propName = DEFAULT_PROP_LOCAL;
		}
		if (propName.indexOf(':') == -1)
		{
			// 没有设置资源的协议, 默认为classpath中的资源
			propName = "cp:/".concat(propName);
		}
		FactoryContainerImpl tmp = new FactoryContainerImpl("<properties.manager>");
		if (loader != null)
		{
			tmp.setAttribute(FactoryContainer.CLASSLOADER_FLAG, loader);
		}
		return ContainerManager.createResource(propName, tmp);
	}

	/**
	 * 判断当前的配置管理器是否将系统的properties的值作为默认值.
	 */
	public boolean isSystemDefault()
	{
		// 如果父配置为true, 那就为true, 否则获取当前配置中的值
		return (this.parent != null && this.parent.isSystemDefault()) || this.systemDefault;
	}

	/**
	 * (重新)载入配置.
	 */
	public void reload()
	{
		this.reload(true, null, null);
	}

	/**
	 * (重新)载入配置.
	 *
	 * @param msg  出参, 载入配置时的出错信息
	 */
	public void reload(StringRef msg)
	{
		this.reload(true, msg, null);
	}

	/**
	 * (重新)载入配置.
	 *
	 * @param reloadParent  是否需要重载父配置
	 * @param msg           出参, 载入配置时的出错信息
	 */
	public void reload(boolean reloadParent, StringRef msg)
	{
		this.reload(reloadParent, msg, null);
	}

	/**
	 * (重新)载入配置.
	 *
	 * @param reloadParent  是否需要重载父配置
	 * @param msg           出参, 载入配置时的出错信息
	 * @param preReadNames  需要预先读取的属性名列表
	 */
	void reload(boolean reloadParent, StringRef msg, String[] preReadNames)
	{
		String preMsg = "";
		if (this.parent != null && reloadParent)
		{
			this.parent.reload(msg);
			if (msg != null && msg.getString() != null)
			{
				preMsg = msg.getString() + "\n";
			}
		}
		try
		{
			this.systemDefault = true;
			Properties temp = this.loadProperties(preReadNames);
			if (temp == null)
			{
				if (msg != null)
				{
					msg.setString(preMsg + "The properties local [" + this.propResource.getConfig() + "] not exists.");
				}
				else if (!(DEFAULT_PROP_LOCAL.equals(this.propResource.getConfig())))
				{
					// 不是默认的配置才输出此信息
					System.err.println(FormatTool.getCurrentDatetimeString()
							+ ": The properties local [" + this.propResource.getConfig() + "] not exists.");
				}
				return;
			}
			temp.remove(CHILD_PROPERTIES);
			temp.remove(PARENT_PROPERTIES);
			String sd = (String) temp.remove(NEED_DEFAULT);
			if (sd != null)
			{
				this.systemDefault = BooleanConverter.toBoolean(sd);
			}
			this.checkParentFirst(temp);
			this.changeProperties(temp, null);
		}
		catch (Throwable ex)
		{
			System.err.println(FormatTool.getCurrentDatetimeString()
					+ ": Error when reload properties.");
			ex.printStackTrace(System.err);
			if (msg != null)
			{
				msg.setString(preMsg + "Reload properties error:" + ex.getMessage());
			}
		}
	}

	/**
	 * 检查并去除父配置优先的属性.
	 */
	private void checkParentFirst(Properties props)
	{
		String names = (String) props.remove(PARENT_FIRST_NAMES);
		if (this.parent == null || StringTool.isEmpty(names))
		{
			return;
		}
		String[] nameArr;
		try
		{
			nameArr = StringTool.separateString(names, ',', true, true);
		}
		catch (Exception ex)
		{
			getLog().error("Error in get parent first names.", ex);
			return;
		}
		for (int i = 0; i < nameArr.length; i++)
		{
			String name = nameArr[i];
			if (!StringTool.isEmpty(name) && this.parent.getProperty(name) != null)
			{
				props.remove(name);
			}
		}
	}

	/**
	 * 修改载入的属性.
	 */
	private synchronized void changeProperties(Properties newProps, String[] preReadNames)
	{
		if (preReadNames != null)
		{
			for (int i = 0; i < preReadNames.length; i++)
			{
				String value = newProps.getProperty(preReadNames[i]);
				if (value != null)
				{
					this.doSetProperty(preReadNames[i], value, null);
				}
			}
			return;
		}
		Iterator itr = newProps.entrySet().iterator();
		while (itr.hasNext())
		{
			Map.Entry entry = (Map.Entry) itr.next();
			this.doSetProperty((String) entry.getKey(), (String) entry.getValue(), null);
		}
		// 设置被删除的属性
		Enumeration e = this.properties.propertyNames();
		while (e.hasMoreElements())
		{
			String name = (String) e.nextElement();
			if (newProps.getProperty(name) == null)
			{
				this.doSetProperty(name, (String) this.defaultValues.get(name), null);
			}
		}
	}

	/**
	 * 载入配置信息并构造成Properties返回.
	 * 如果所指定的配置资源不存在, 则返回null.
	 */
	protected Properties loadProperties(String[] preReadNames)
			throws IOException
	{
		InputStream inStream = this.propResource.getAsStream();
		if (inStream == null)
		{
			return null;
		}
		Properties temp = new Properties();
		Utility.loadProperties(temp, inStream);
		if (preReadNames != null)
		{
			this.changeProperties(temp, preReadNames);
		}
		Map delayMap = new HashMap();
		this.loadChildProperties(temp, this.properties.getProperty(CHILD_PROPERTIES),
				this.propResource, delayMap, temp);
		this.loadParentProperties(temp, this.properties.getProperty(PARENT_PROPERTIES),
				this.propResource, delayMap, temp);
		if (!delayMap.isEmpty())
		{
			Iterator itr = delayMap.entrySet().iterator();
			while (itr.hasNext())
			{
				Map.Entry e = (Map.Entry) itr.next();
				Object[] values = (Object[]) e.getValue();
				if (Boolean.TRUE == values[0])
				{
					this.loadChildProperties((Properties) values[1], (String) e.getKey(),
							this.propResource, null, temp);
				}
				else
				{
					this.loadParentProperties((Properties) values[1], (String) e.getKey(),
							this.propResource, null, temp);
				}
			}
		}
		return temp;
	}

	/**
	 * 以集合的形式返回所有属性名称.
	 */
	public Set getPropertyNames()
	{
		Set result = new HashSet();
		this.fillPropertyNames(result);
		return result;
	}
	/**
	 * 将当前管理的属性名填入到(集合类型的)参数中.
	 *
	 * @param result  属性名需要放入的集合.
	 */
	protected void fillPropertyNames(Set result)
	{
		if (this.parent != null)
		{
			this.parent.fillPropertyNames(result);
		}
		Enumeration e = this.properties.propertyNames();
		while (e.hasMoreElements())
		{
			result.add(e.nextElement());
		}
	}

	/**
	 * 判断管理器中是否包含某个键值.
	 *
	 * @param key          需要判断的键值
	 * @param onlyCurrent  设为true表示仅判断当前管理器, 不包含父管理器中的键值
	 */
	public boolean contains(String key, boolean onlyCurrent)
	{
		if (onlyCurrent)
		{
			return this.properties.containsKey(key);
		}
		else
		{
			return this.getProperty(key) != null;
		}
	}

	/**
	 * 获取属性值.
	 *
	 * @param key  属性所在的键值
	 */
	public String getProperty(String key)
	{
		String value = this.properties.getProperty(key);
		if (value == null)
		{
			if (this.parent != null)
			{
				value = this.parent.getProperty(key);
				if (value == null && !this.parent.isSystemDefault() && this.systemDefault)
				{
					value = defaultProperties.getProperty(key);
				}
			}
			else if (this.systemDefault)
			{
				value = defaultProperties.getProperty(key);
			}
		}
		return value;
	}

	/**
	 * 获取属性值.
	 *
	 * @param key          属性所在的键值
	 * @param defaultValue 此键值下没属性时将返回此默认值
	 */
	public String getProperty(String key, String defaultValue)
	{
		String value = this.getProperty(key);
		return value == null ? defaultValue : value;
	}

	/**
	 * 获取对文本内容处理过的属性值.
	 *
	 * @param key          属性所在的键值
	 */
	public String getResolvedProperty(String key)
	{
		return this.resolveDynamicPropnames(this.getProperty(key));
	}

	/**
	 * 获取对文本内容处理过的属性值.
	 *
	 * @param key          属性所在的键值
	 * @param defaultValue 此键值下没属性时将返回此默认值
	 */
	public String getResolvedProperty(String key, String defaultValue)
	{
		return this.resolveDynamicPropnames(this.getProperty(key, defaultValue));
	}

	/**
	 * 设置属性值.
	 *
	 * @param key    属性所在的键值
	 * @param value  需要设置的值
	 */
	public void setProperty(String key, String value)
	{
		this.setProperty0(key, value, null);
	}

	/**
	 * 如果检测值与原始值相同才设置属性值.
	 *
	 * @return  true表示设置成功,
	 *          false则可能是原始值与检测值不同或原始值与设置的值相同
	 */
	public boolean setPropertyWithCheck(String key, String value, String check)
	{
		return this.setProperty0(key, value, new StringRef(check));
	}

	/**
	 * 移除属性值.
	 *
	 * @param key    属性所在的键值
	 */
	public void removeProperty(String key)
	{
		this.setProperty0(key, null, null);
	}

	/**
	 * 给内部调用设置属性的方法, 由于有写入判断, 需要加同步锁.
	 */
	private synchronized boolean setProperty0(String key, String value, StringRef check)
	{
		return this.doSetProperty(key, value, check);
	}

	/**
	 * 执行设置属性, 给setProperty0和changeProperties调用, 不需要加锁
	 */
	private boolean doSetProperty(String key, String value, StringRef check)
	{
		String oldValue = this.properties.getProperty(key);
		if (isValueEquals(oldValue, value))
		{
			// 新的值和原值相等, 则返回未设置
			return false;
		}
		boolean needFire = true;
		boolean parentGetted = false;
		if (oldValue == null && this.parent != null)
		{
			// 如果存在父配置且原始值为null, 则需要读取父配置中的原始值
			oldValue = this.parent.getProperty(key);
			parentGetted = true;
			if (isValueEquals(oldValue, value))
			{
				// 新的值和父配置的原值相等, 则需要设置(这样不会受父配置的变化而影响), 但不触发更新事件
				needFire = false;
			}
		}
		if (check != null && (!needFire || !isValueEquals(oldValue, check.getString())))
		{
			// 有原始值检查, 原始值不同或不触发更新事件(新的值和父配置的原值相等), 则不进行设置
			return false;
		}
		String newValue = value;
		if (value == null)
		{
			this.properties.remove(key);
			if (!parentGetted && this.parent != null)
			{
				// 如果存在父配置, 则需要将值恢复到父配置的值
				newValue = this.parent.getProperty(key);
				if (isValueEquals(oldValue, newValue))
				{
					// 如果是删除配置且原始的值和父配置的原值相等, 则不触发更新事件
					needFire = false;
				}
			}
		}
		else
		{
			this.properties.setProperty(key, value);
		}
		if (needFire)
		{
			this.firePropertyChanged(key, oldValue, newValue);
		}
		// 如果触发了更新事件表示更新了值, 否则虽然设置了但值未更新, 需要返回未设置
		return needFire;
	}

	/**
	 * 判断两个值是否相同.
	 */
	static boolean isValueEquals(String value1, String value2)
	{
		return value1 == value2 || (value1 != null && value1.equals(value2));
	}

	/**
	 * 触发属性发生变化的事件.
	 */
	void firePropertyChanged(String key, String oldValue, String newValue)
	{
		Iterator itr = this.plList.iterator();
		while (itr.hasNext())
		{
			if (!((InternalPropertyListener) itr.next()).propertyChanged(key, oldValue, newValue))
			{
				// 返回false, 表示需要删除此Listener
				itr.remove();
			}
		}
	}

	/**
	 * 配置监控者添加完后, 判断并处理是否要将配置中的值设置到目标中.
	 */
	private void dealChangeProperty(String key, String defaultValue, PropertyManager pm)
	{
		String temp = this.getProperty(key);
		if (temp == null && defaultValue != null)
		{
			if (this.setProperty0(key, defaultValue, new StringRef(null)))
			{
				// 如果默认值设置成功, 则添加到默认值map中
				this.defaultValues.put(key, defaultValue);
			}
			else
			{
				// 如果默认值设置失败, 说明值被更新了, 需要重新获取值
				temp = this.getProperty(key);
			}
		}
		try
		{
			if (temp != null)
			{
				// 如果存在要设置的值, 则要将值设置到被监控的属性中
				pm.changeProperty(temp);
			}
		}
		catch (Throwable ex)
		{
			getLog().warn("Error when change property.", ex);
		}
	}

	/**
	 * 添加一个配置监控者, 当配置的值改变时, 它会自动更新指定类的静态属性成员, 该属性
	 * 的类型可以是: <code>String</code>, <code>int</code>或<code>boolean</code>.
	 *
	 * @param key            配置的键值
	 * @param theClass       被修改的属性所在的类
	 * @param fieldName      需要被修改的静态属性名称
	 */
	public void addFieldPropertyManager(String key, Class theClass, String fieldName)
			throws NoSuchFieldException
	{
		this.addFieldPropertyManager(key, theClass, fieldName, null);
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
	public void addFieldPropertyManager(String key, Class theClass, String fieldName,
			String defaultValue)
			throws NoSuchFieldException
	{
		PropertyManager pm = new PropertyManager(key, theClass,
				theClass.getDeclaredField(fieldName), this.defaultPL);
		this.defaultPL.addPropertyManager(key, pm);
		this.dealChangeProperty(key, defaultValue, pm);
	}

	/**
	 * 移除一个配置监控者.
	 *
	 * @param key            配置的键值
	 * @param theClass       被修改的属性所在的类
	 * @param fieldName      需要被修改的静态属性名称
	 */
	public void removeFieldPropertyManager(String key, Class theClass, String fieldName)
			throws NoSuchFieldException
	{
		PropertyManager pm = new PropertyManager(key, theClass,
				theClass.getDeclaredField(fieldName), this.defaultPL);
		this.defaultPL.removePropertyManager(key, pm);
	}

	/**
	 * 添加一个配置监控者, 当配置的值改变时, 它会自动调用指定类的静态方法,
	 * 此方法必须是只有一个<code>String</code>类型的参数.
	 *
	 * @param key            配置的键值
	 * @param theClass       被调用的方法所在的类
	 * @param methodName     需要被调用的静态方法名称
	 */
	public void addMethodPropertyManager(String key, Class theClass, String methodName)
			throws NoSuchMethodException
	{
		this.addMethodPropertyManager(key, theClass, methodName, null);
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
	public void addMethodPropertyManager(String key, Class theClass, String methodName,
			String defaultValue)
			throws NoSuchMethodException
	{
		PropertyManager pm = new PropertyManager(key, theClass,
				theClass.getDeclaredMethod(methodName, Utility.STR_PARAM), this.defaultPL);
		this.defaultPL.addPropertyManager(key, pm);
		this.dealChangeProperty(key, defaultValue, pm);
	}

	/**
	 * 移除一个配置监控者.
	 *
	 * @param key            配置的键值
	 * @param theClass       被调用的方法所在的类
	 * @param methodName     需要被调用的静态方法名称
	 */
	public void removeMethodPropertyManager(String key, Class theClass, String methodName)
			throws NoSuchMethodException
	{
		PropertyManager pm = new PropertyManager(key, theClass,
				theClass.getDeclaredMethod(methodName, Utility.STR_PARAM), this.defaultPL);
		this.defaultPL.removePropertyManager(key, pm);
	}

	/**
	 * 添加一个配置变化的监听者.
	 */
	synchronized void addPropertyListener0(InternalPropertyListener l)
	{
		if (!this.plList.contains(l))
		{
			this.plList.add(l);
		}
	}

	/**
	 * 移除一个配置变化的监听者.
	 */
	synchronized void removePropertyListener0(InternalPropertyListener l)
	{
		this.plList.remove(l);
	}

	/**
	 * 载入子配置
	 */
	private void loadChildProperties(Properties props, String nowName, ConfigResource baseRes,
			Map delayMap, Properties allProps)
			throws IOException
	{
		String cName = nowName != null ? nowName : props.getProperty(CHILD_PROPERTIES);
		if (cName == null)
		{
			return;
		}
		ConfigResource res = this.checkDynamicRes(cName, allProps, true, baseRes, delayMap, props);
		if (res == null)
		{
			return;
		}
		InputStream stream = res.getAsStream();
		if (stream != null)
		{
			Properties tmpProps = new Properties();
			Utility.loadProperties(tmpProps, stream);
			this.loadChildProperties(tmpProps, null, res, delayMap, allProps);
			Iterator itr = tmpProps.entrySet().iterator();
			while (itr.hasNext())
			{
				Map.Entry entry = (Map.Entry) itr.next();
				props.put(entry.getKey(), entry.getValue());
			}
		}
	}

	/**
	 * 载入父配置
	 */
	private void loadParentProperties(Properties props, String nowName, ConfigResource baseRes,
			Map delayMap, Properties allProps)
			throws IOException
	{
		String pName = nowName != null ? nowName : props.getProperty(PARENT_PROPERTIES);
		if (pName == null)
		{
			return;
		}
		ConfigResource res = this.checkDynamicRes(pName, allProps, false, baseRes, delayMap, props);
		if (res == null)
		{
			return;
		}
		InputStream stream = res.getAsStream();
		if (stream != null)
		{
			Properties tmpProps = new Properties();
			Utility.loadProperties(tmpProps, stream);
			this.loadParentProperties(tmpProps, null, res, delayMap, allProps);
			Iterator itr = tmpProps.entrySet().iterator();
			while (itr.hasNext())
			{
				Map.Entry entry = (Map.Entry) itr.next();
				if (!props.containsKey(entry.getKey()))
				{
					props.put(entry.getKey(), entry.getValue());
				}
			}
		}
	}

	/**
	 * 检查需要载入的资源配置中是否有未解析的动态参数, 如果有需要延迟加载.
	 */
	private ConfigResource checkDynamicRes(String resName, Properties allProps, boolean child,
			ConfigResource baseRes, Map delayMap, Properties nowProps)
	{
		String newRes = this.resolveDynamicPropnames(resName, allProps);
		if (newRes.indexOf(DYNAMIC_PROPNAME_PREFIX) != -1)
		{
			// 存在动态参数标识, 需要延迟载入
			if (delayMap != null)
			{
				delayMap.put(resName, new Object[]{child ? Boolean.TRUE : Boolean.FALSE, nowProps});
			}
			return null;
		}
		return baseRes.getResource(newRes);
	}

	public String toString()
	{
		return this.properties.toString();
	}

	/**
	 * 处理文本中"${...}"的动态属性, 将他们替换成配置文件
	 * (本配置对象 或 System.property)中的对应值.
	 *
	 * @param text      要处理的文本
	 * @return 处理完的文本
	 */
	public String resolveDynamicPropnames(String text)
	{
		return this.resolveDynamicPropnames(text, null, false);
	}

	/**
	 * 处理文本中"${...}"的动态属性, 将他们替换成配置文件
	 * (bindRes 或 本配置对象 或 System.property)中的对应值.
	 *
	 * @param text      要处理的文本
	 * @param bindRes   绑定的资源, 会先在bindRes寻找对应的值
	 * @return 处理完的文本
	 *
	 */
	public String resolveDynamicPropnames(String text, Map bindRes)
	{
		return this.resolveDynamicPropnames(text, bindRes, false);
	}

	/**
	 * 处理文本中"${...}"的动态属性, 将他们替换成配置文件
	 * (bindRes 或 本配置对象 或 System.property)中的对应值.
	 *
	 * @param text      要处理的文本
	 * @param bindRes   绑定的资源, 会先在bindRes寻找对应的值
	 * @param onlyRes   设置为<code>true</code>时, 只对绑定的资源进行处理, 设置为
	 *                  <code>false</code>时, 如果绑定的资源中不存在对应的值会再到
	 *                  本配置对象中寻找
	 * @return 处理完的文本
	 *
	 */
	public String resolveDynamicPropnames(String text, Map bindRes, boolean onlyRes)
	{
		int startIndex;
		if (text == null || (startIndex = text.indexOf(DYNAMIC_PROPNAME_PREFIX)) == -1)
		{
			return text;
		}

		String tempStr = text;
		StringAppender result = StringTool.createStringAppender(text.length() + 32);
		int prefixLength = DYNAMIC_PROPNAME_PREFIX.length();
		while (startIndex != -1)
		{
			result.append(tempStr.substring(0, startIndex));
			int endIndex = tempStr.indexOf(DYNAMIC_PROPNAME_SUFFIX, startIndex + prefixLength);
			if (endIndex != -1)
			{
				String dName = tempStr.substring(startIndex + prefixLength, endIndex);
				try
				{
					String defValue = null;
					int defIndex = dName.indexOf(':');
					if (defIndex != -1)
					{
						defValue = dName.substring(defIndex + 1);
						dName = dName.substring(0, defIndex);
					}
					String pValue = null;
					if (bindRes != null)
					{
						Object obj = bindRes.get(dName);
						if (obj != null)
						{
							pValue = String.valueOf(obj);
						}
					}
					if (!onlyRes && pValue == null)
					{
						// 如果bindRes为null或其中不存在需要的值, 则到当前的属性管理器中查找
						pValue = this.getProperty(dName);
					}
					if (pValue == null && defValue != null)
					{
						// 如果存在默认值, 则设置默认值
						pValue = defValue;
					}
					if (pValue != null)
					{
						result.append(this.resolveDynamicPropnames(pValue, bindRes, onlyRes));
					}
					else
					{
						result.append(tempStr.substring(startIndex, endIndex + 1));
						if (Utility.SHOW_RDP_FAIL)
						{
							getLog().warn("Could not resolve dynamic name '"
									+ dName + "' in [" + text + "] as config property.");
						}
					}
				}
				catch (Throwable ex)
				{
					if (Utility.SHOW_RDP_FAIL)
					{
						String msg = "Could not resolve dynamic name '" + dName
								+ "' in [" + text + "] as config property.";
						getLog().warn(msg, ex);
					}
				}
				tempStr = tempStr.substring(endIndex + DYNAMIC_PROPNAME_SUFFIX.length());
				startIndex = tempStr.indexOf(DYNAMIC_PROPNAME_PREFIX);
			}
			else
			{
				tempStr = tempStr.substring(startIndex);
				startIndex = -1;
			}
		}
		return result.append(tempStr).toString();
	}

	/**
	 * 获取所有绑定的PropertyManager.
	 */
	Map.Entry[] getAllPropertyManagers()
	{
		return this.defaultPL.getAllPropertyManagers();
	}

	/**
	 * 获取日志对象.
	 */
	static Log getLog()
	{
		Log tmp = log;
		if (tmp == null)
		{
			log = tmp = Utility.createLog(LOGGER_NAME);
		}
		return tmp;
	}

}

/**
 * 内部使用的配置变化监听者.
 */
interface InternalPropertyListener extends EventListener
{
	/**
	 * 当某个配置值发生了改变时, 会调用此方法.
	 *
	 * @param key       发生改变的配置的键值
	 * @param oldValue  改变前配置的原始值
	 * @param newValue  改变后配置的值
	 * @return  返回true表示需要继续保留此Listener, 返回false表示
	 *          需要删除此Listener
	 */
	boolean propertyChanged(String key, String oldValue, String newValue);

}

/**
 * 用于监听父配置变化的监听者.
 */
class ParentPropertyListener
		implements InternalPropertyListener
{
	/**
	 * 当前的配置属性管理器.
	 */
	private final WeakReference nowPM;

	public ParentPropertyListener(InternalPropertiesManager pm)
	{
		this.nowPM = new WeakReference(pm);
	}

	public boolean propertyChanged(String key, String oldValue, String newValue)
	{
		InternalPropertiesManager tmp = (InternalPropertiesManager) this.nowPM.get();
		if (tmp == null)
		{
			return false;
		}
		if (!tmp.contains(key, true))
		{
			// 如果当前的配置属性管理器中没有此键值, 则说明是需要使用父配置属性管理器中
			// 的值, 当父配置中的属性变化时, 需要触发事件
			synchronized (tmp)
			{
				tmp.firePropertyChanged(key, oldValue, newValue);
			}
		}
		return true;
	}

}

/**
 * 默认的配置变化监听者.
 */
class DefaultPropertyListener
		implements InternalPropertyListener
{
	private final Map managerMap = new HashMap();

	synchronized Map.Entry[] getAllPropertyManagers()
	{
		Set entrySet = this.managerMap.entrySet();
		return (Map.Entry[]) entrySet.toArray(new Map.Entry[entrySet.size()]);
	}

	public synchronized void addPropertyManager(String key, PropertyManager pm)
	{
		PropertyManager[] pms = (PropertyManager[]) this.managerMap.get(key);
		if (pms == null)
		{
			pms = new PropertyManager[]{pm};
		}
		else
		{
			for (int i = 0; i < pms.length; i++)
			{
				if (pms[i].equals(pm))
				{
					return;
				}
			}
			PropertyManager[] newPms = new PropertyManager[pms.length + 1];
			System.arraycopy(pms, 0, newPms, 0, pms.length);
			newPms[pms.length] = pm;
			pms = newPms;
		}
		this.managerMap.put(key, pms);
	}

	public synchronized void removePropertyManager(String key, PropertyManager pm)
	{
		PropertyManager[] pms = (PropertyManager[]) this.managerMap.get(key);
		if (pms == null)
		{
			return;
		}

		for (int i = 0; i < pms.length; i++)
		{
			//System.out.println(pms.length + ":" + pm);
			if (pms[i].equals(pm))
			{
				if (pms.length == 1)
				{
					this.managerMap.remove(key);
					return;
				}
				PropertyManager[] newPms = new PropertyManager[pms.length - 1];
				System.arraycopy(pms, 0, newPms, 0, i);
				System.arraycopy(pms, i + 1, newPms, i, pms.length - i - 1);
				pms = newPms;
				this.managerMap.put(key, pms);
				return;
			}
		}
	}

	public boolean propertyChanged(String key, String oldValue, String newValue)
	{
		PropertyManager[] pms;
		synchronized (this)
		{
			pms = (PropertyManager[]) this.managerMap.get(key);
		}
		if (pms != null)
		{
			for (int i = 0; i < pms.length; i++)
			{
				try
				{
					pms[i].changeProperty(newValue);
				}
				catch (Throwable ex)
				{
					InternalPropertiesManager.getLog().warn("Error when change property.", ex);
				}
			}
		}
		return true;
	}

}

/**
 * 单个属性的管理器, 给默认的配置变化监听者使用.
 */
class PropertyManager
{
	/**
	 * 用于清楚weak方式的引用队列.
	 */
	private final ReferenceQueue queue = new ReferenceQueue();

	/**
	 * 对应属性的键值.
	 */
	private final String key;

	/**
	 * 这里使用<code>WeakReference</code>来引用对应的类, 并在其释放时删除本属性管理者.
	 */
	private final WeakReference baseClass;

	/**
	 * 这里使用<code>WeakReference</code>来引用对应的成员, 这样不会影响类的正常释放.
	 */
	private Reference optMember;

	/**
	 * 要操作的成员名称.
	 */
	private final String optMemberName;

	/**
	 * 标识是否是属性成员, <code>true</code>表示属性成员, <code>false</code>表示方法成员.
	 */
	private final boolean fieldMember;

	/**
	 * 该配置管理器所在的listener.
	 */
	private final DefaultPropertyListener listener;

	private PropertyManager(String key, boolean fieldMember, Class baseClass, Member optMember,
			DefaultPropertyListener listener)
	{
		expunge();
		if (key == null)
		{
			throw new IllegalArgumentException("The property key can't be null.");
		}
		this.listener = listener;
		this.key = key;
		this.fieldMember = fieldMember;
		this.baseClass = new BaseClassRef(this, baseClass, this.queue);
		this.optMember = makeMemberRef(optMember);
		this.optMemberName = optMember.getName();
		if (!Modifier.isStatic(optMember.getModifiers()))
		{
			throw new IllegalArgumentException("The opt member must be static.");
		}
	}

	/**
	 * 构造一个触发方法调用的配置管理器.
	 */
	PropertyManager(String key, Class theClass, Method theMethod, DefaultPropertyListener listener)
	{
		this(key, false, theClass, theMethod, listener);
	}

	/**
	 * 构造一个触发属性值修改的配置管理器.
	 */
	PropertyManager(String key, Class theClass, Field theField, DefaultPropertyListener listener)
	{
		this(key, true, theClass, theField, listener);
		if (Modifier.isFinal(theField.getModifiers()))
		{
			throw new IllegalArgumentException("The field can't be final.");
		}
	}

	Class getBaseClass()
	{
		return (Class) this.baseClass.get();
	}

	boolean isFieldMember()
	{
		return this.fieldMember;
	}

	Member getOptMember()
			throws NoSuchFieldException, NoSuchMethodException
	{
		Member m = (Member) this.optMember.get();
		if (m != null)
		{
			return m;
		}
		Class c = (Class) this.baseClass.get();
		if (c == null)
		{
			return null;
		}
		if (this.fieldMember)
		{
			m = c.getDeclaredField(this.optMemberName);
		}
		else
		{
			m = c.getDeclaredMethod(this.optMemberName, Utility.STR_PARAM);
		}
		this.optMember = makeMemberRef(m);
		return m;
	}

	private static Reference makeMemberRef(Member m)
	{
		if (InternalPropertiesManager.weakRefMember)
		{
			return new WeakReference(m);
		}
		else
		{
			return new SoftReference(m);
		}
	}

	public void changeProperty(String value)
			throws Exception
	{
		expunge();
		Member member = this.getOptMember();
		// 如果操作的成员为null, 则不执行变更.
		if (member == null)
		{
			return;
		}
		if (this.fieldMember)
		{
			Object objValue = value;
			Field theField = (Field) member;
			if (theField.getType() != String.class)
			{
				try
				{
					objValue = ConverterFinder.findConverter(theField.getType(), false).convert(value);
				}
				catch (Throwable ex)
				{
					Class theClass = (Class) this.baseClass.get();
					if (theClass != null)
					{
						String typeName = ClassGenerator.getClassName(theField.getType());
						String msg = "Type convert error for value:[" + value + "] to [" + typeName
								+ "] in class:[" + ClassGenerator.getClassName(theClass)
								+ "] field:[" + this.optMemberName + "].";
						InternalPropertiesManager.getLog().warn(msg, ex);
					}
					return;
				}
			}
			if (!theField.isAccessible())
			{
				theField.setAccessible(true);
			}
			theField.set(null, objValue);
		}
		else
		{
			Method theMethod = (Method) member;
			if (!theMethod.isAccessible())
			{
				theMethod.setAccessible(true);
			}
			theMethod.invoke(null, new Object[]{value});
		}
	}

	/**
	 * 清除过期的属性管理者.
	 */
	private void expunge()
	{
		BaseClassRef bcr = (BaseClassRef) this.queue.poll();
		while (bcr != null)
		{
			PropertyManager pm = bcr.getPropertyManager();
			this.listener.removePropertyManager(pm.key, pm);
			bcr = (BaseClassRef) this.queue.poll();
		}
	}

	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj instanceof PropertyManager)
		{
			PropertyManager pm = (PropertyManager) obj;
			if (!this.key.equals(pm.key))
			{
				return false;
			}
			if (!Utility.objectEquals(this.baseClass.get(), pm.baseClass.get()))
			{
				return false;
			}
			if (this.fieldMember != pm.fieldMember)
			{
				return false;
			}
			if (!Utility.objectEquals(this.optMemberName, pm.optMemberName))
			{
				return false;
			}
			return true;
		}
		return false;
	}

	public String toString()
	{
		StringAppender temp = StringTool.createStringAppender(128);
		Class baseClass = (Class) this.baseClass.get();
		temp.append("PropertyManager[class:").append(
				baseClass == null ? "<released>" : ClassGenerator.getClassName(baseClass));
		Member member = (Member) this.optMember.get();
		if (this.fieldMember)
		{
			temp.append(" field:(");
		}
		else
		{
			temp.append(" method:(");
		}
		temp.append(member == null ? "<released>" : member.getName()).append(')').append(']');
		return temp.toString();
	}

}

class BaseClassRef extends WeakReference
{
	private final PropertyManager pm;

	public BaseClassRef(PropertyManager pm, Object baseClass, ReferenceQueue q)
	{
		super(baseClass, q);
		this.pm = pm;
	}

	public PropertyManager getPropertyManager()
	{
		return this.pm;
	}

}
