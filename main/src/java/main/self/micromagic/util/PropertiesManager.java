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
import java.net.URL;
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
import self.micromagic.util.converter.BooleanConverter;
import self.micromagic.util.converter.ConverterFinder;
import self.micromagic.util.ref.StringRef;

/**
 * 配置属性的管理器.
 *
 * @author micromagic@sina.com
 */
public class PropertiesManager
{
	/**
	 * 默认的配置文件名.
	 * 注: 配置文件都必须在classpath下.
	 */
	public static final String PROPERTIES_NAME = "eterna.config";

	/**
	 * 存放父配置文件名的属性.
	 */
	public static final String PARENT_PROPERTIES = "_parent.properties";

	/**
	 * 存放子配置文件名的属性.
	 */
	public static final String CHILD_PROPERTIES = "_child.properties";

	/**
	 * 设置是否需要将系统的properties的值作为默认值.
	 * 当本配置管理器中的值不存在时, 会再读取系统的properties中的值. 如果
	 * 父配置管理器中设置为true, 那当前的值就会默认为false.
	 * 当未设置此属性时, 默认值为true.
	 * 注: 系统的properties中的值的修改不会触发当前配置管理器的事件, 只是
	 * 能够在读取时被取到.
	 */
	public static final String SYSTEM_DEFAULT = "_system.default";

	/**
	 * 日志的名称.
	 */
	private static final String LOGGER_NAME = "eterna.util";

	/**
	 * 配置文件名.
	 * 注: 配置文件都必须在classpath下.
	 */
	private final String propName;

	/**
	 * 读取配置文件所使用的<code>ClassLoader</code>.
	 */
	private final ClassLoader classLoader;

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
	 * 是否需要将系统的properties的值作为默认值.
	 */
	private boolean systemDefault;

	/**
	 * 父配置管理器.
	 */
	private final PropertiesManager parent;

	/**
	 * 默认的构造函数.
	 * 默认的配置文件名及本类的<code>ClassLoader</code>.
	 */
	public PropertiesManager()
	{
		this(null, null);
	}

	/**
	 * 构造一个配置属性的管理器.
	 *
	 * @param propName      配置文件名, 必须在classpath下, 需给出的是classpath路径
	 *                      如: com/xxx.properties
	 * @param classLoader   读取配置文件所使用的<code>ClassLoader</code>
	 */
	public PropertiesManager(String propName, ClassLoader classLoader)
	{
		this(propName, classLoader, null);
	}

	/**
	 * 构造一个配置属性的管理器.
	 *
	 * @param propName      配置文件名, 必须在classpath下, 需给出的是classpath路径
	 *                      如: com/xxx.properties
	 * @param classLoader   读取配置文件所使用的<code>ClassLoader</code>
	 * @param parent        当前配置管理器所继承的父配置管理器
	 */
	public PropertiesManager(String propName, ClassLoader classLoader, PropertiesManager parent)
	{
		this.propName = propName == null ? PROPERTIES_NAME : propName;
		this.classLoader = classLoader == null ? this.getClass().getClassLoader() : classLoader;
		this.addPropertyListener(this.defaultPL);
		this.reload();
		// 初始化时不重载父配置管理器
		this.parent = parent;
		if (this.parent != null)
		{
			this.parent.addPropertyListener(new ParentPropertyListener(this));
		}
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
		this.reload(null);
	}

	/**
	 * (重新)载入配置.
	 *
	 * @param msg   出参, 载入配置时的出错信息
	 */
	public void reload(StringRef msg)
	{
		String preMsg = "";
		if (this.parent != null)
		{
			this.parent.reload(msg);
			if (msg != null && msg.getString() != null)
			{
				preMsg = msg.getString() + "\n";
			}
		}
		try
		{
			Properties temp = this.loadProperties();
			if (temp == null)
			{
				if (msg != null)
				{
					msg.setString(preMsg + "The properties name:[" + this.propName + "] not found in class loader.");
				}
				else if (!PROPERTIES_NAME.equals(this.propName))
				{
					// 不是默认的名称才输出此信息
					System.err.println(FormatTool.getCurrentDatetimeString()
							+ ": The properties name:[" + this.propName + "] not found in class loader.");
				}
				return;
			}
			temp.remove(CHILD_PROPERTIES);
			temp.remove(PARENT_PROPERTIES);
			String sd = (String) temp.remove(SYSTEM_DEFAULT);
			if (sd != null)
			{
				BooleanConverter converter = new BooleanConverter();
				this.systemDefault = converter.convertToBoolean(sd);
			}
			else
			{
				this.systemDefault = true;
			}
			synchronized (this)
			{
				Iterator itr = temp.entrySet().iterator();
				while (itr.hasNext())
				{
					Map.Entry entry = (Map.Entry) itr.next();
					this.setProperty0((String) entry.getKey(), (String) entry.getValue());
				}
				// 设置被删除的属性
				Enumeration e = this.properties.propertyNames();
				while (e.hasMoreElements())
				{
					String name = (String) e.nextElement();
					if (temp.getProperty(name) == null)
					{
						this.setProperty0(name, (String) this.defaultValues.get(name));
					}
				}
			}
			// 由于上面已经设置了属性 所以不用对properties赋值;
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
	 * 存放属性绑定时设置的默认值.
	 */
	protected Map defaultValues = new HashMap();

	/**
	 * 载入配置信息并构造成Properties返回.
	 * 如果所指定的配置资源不存在, 则返回null.
	 */
	protected Properties loadProperties()
			throws IOException
	{
		URL url = this.classLoader.getResource(this.propName);
		if (url == null)
		{
			return null;
		}
		Properties temp = new Properties();
		InputStream inStream = url.openStream();
		temp.load(inStream);
		inStream.close();
		this.loadChildProperties(temp, this.properties.getProperty(CHILD_PROPERTIES));
		this.loadParentProperties(temp, this.properties.getProperty(PARENT_PROPERTIES));
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
			return this.properties.contains(key);
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
					value = System.getProperty(key);
				}
			}
			else if (this.systemDefault)
			{
				value = System.getProperty(key);
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
	public synchronized void setProperty(String key, String value)
	{
		this.setProperty0(key, value);
	}
	/**
	 * 给内部调用设置属性的方法, 不加同步锁.
	 */
	private void setProperty0(String key, String value)
	{
		String oldValue = this.properties.getProperty(key);
		// 判断新的值和原值是否相等
		if (oldValue != null)
		{
			if (oldValue.equals(value))
			{
				return;
			}
		}
		else if (value == null)
		{
			return;
		}
		if (oldValue == null && this.parent != null)
		{
			// 如果存在父配置且原始值为null, 则需要读取父配置中的原始值
			oldValue = this.parent.getProperty(key);
		}

		if (value == null)
		{
			this.properties.remove(key);
			if (this.parent != null)
			{
				// 如果存在父配置, 则需要将值恢复到父配置的值
				value = this.parent.getProperty(key);
			}
		}
		else
		{
			this.properties.setProperty(key, value);
		}
		this.firePropertyChanged(key, oldValue, value);
	}

	/**
	 * 移除属性值.
	 *
	 * @param key    属性所在的键值
	 */
	public void removeProperty(String key)
	{
		this.setProperty(key, null);
	}

	/**
	 * 触发属性发生变化的事件.
	 */
	private void firePropertyChanged(String key, String oldValue, String newValue)
	{
		Iterator itr = this.plList.iterator();
		while (itr.hasNext())
		{
			if (!((PropertyListener) itr.next()).propertyChanged(key, oldValue, newValue))
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
		boolean setted = false;
		if (temp == null && defaultValue != null)
		{
			temp = defaultValue;
			synchronized (this)
			{
				this.defaultValues.put(key, defaultValue);
				this.setProperty0(key, defaultValue);
			}
			setted = true;
		}
		try
		{
			if (temp != null && !setted)
			{
				// 如果存在要设置的值, 且未设置过值, 则要将值设置到被监控的属性中
				pm.changeProperty(temp);
			}
		}
		catch (Throwable ex)
		{
			Log log = Utility.createLog(LOGGER_NAME);
			log.warn("Error when change property.", ex);
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
	public synchronized void addPropertyListener(PropertyListener l)
	{
		if (!this.plList.contains(l))
		{
			this.plList.add(l);
		}
	}

	/**
	 * 移除一个配置变化的监听者.
	 */
	public synchronized void removePropertyListener(PropertyListener l)
	{
		this.plList.remove(l);
	}

	/**
	 * 载入父配置
	 */
	private void loadChildProperties(Properties props, String nowName)
			throws IOException
	{
		String cName = nowName != null ? nowName : props.getProperty(CHILD_PROPERTIES);
		if (cName == null)
		{
			return;
		}
		URL url = this.classLoader.getResource(cName);
		if (url == null)
		{
			return;
		}
		InputStream is = url.openStream();
		if (is != null)
		{
			Properties tmpProps = new Properties();
			tmpProps.load(is);
			is.close();
			this.loadChildProperties(tmpProps, null);
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
	private void loadParentProperties(Properties props, String nowName)
			throws IOException
	{
		String pName = nowName != null ? nowName : props.getProperty(PARENT_PROPERTIES);
		if (pName == null)
		{
			return;
		}
		URL url = this.classLoader.getResource(pName);
		if (url == null)
		{
			return;
		}
		InputStream is = url.openStream();
		if (is != null)
		{
			Properties tmpProps = new Properties();
			tmpProps.load(is);
			is.close();
			this.loadParentProperties(tmpProps, null);
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

	public String toString()
	{
		return this.properties.toString();
	}


	/**
	 * 动态属性名称的前缀: "${"
	 */
	private static final String DYNAMIC_PROPNAME_PREFIX = "${";
	/**
	 * 动态属性名称的后缀:: "}"
	 */
	private static final String DYNAMIC_PROPNAME_SUFFIX = "}";

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
		if (text == null)
		{
			return text;
		}
		int startIndex = text.indexOf(DYNAMIC_PROPNAME_PREFIX);
		if (startIndex == -1)
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
					String pValue = null;
					if (bindRes != null)
					{
						Object obj = bindRes.get(dName);
						if (obj != null)
						{
							pValue = String.valueOf(obj);
						}
					}
					if (!onlyRes)
					{
						if (pValue == null)
						{
							// 如果bindRes为null或其中不存在需要的值, 则到当前的属性管理器中查找
							pValue = this.getProperty(dName);
						}
					}
					if (pValue != null)
					{
						result.append(resolveDynamicPropnames(pValue, bindRes));
					}
					else
					{
						result.append(tempStr.substring(startIndex, endIndex + 1));
						if (Utility.SHOW_RDP_FAIL)
						{
							Utility.createLog(LOGGER_NAME).warn("Could not resolve dynamic name '"
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
						Utility.createLog(LOGGER_NAME).warn(msg, ex);
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
		result.append(tempStr);

		return result.toString();
	}

	/**
	 * 配置变化的监听者.
	 */
	public interface PropertyListener extends EventListener
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
		public boolean propertyChanged(String key, String oldValue, String newValue);

	}


	/**
	 * 用于监听父配置变化的监听者.
	 */
	private static class ParentPropertyListener
			implements PropertyListener
	{
		/**
		 * 当前的配置属性管理器.
		 */
		private final WeakReference nowPM;

		public ParentPropertyListener(PropertiesManager pm)
		{
			this.nowPM = new WeakReference(pm);
		}

		public boolean propertyChanged(String key, String oldValue, String newValue)
		{
			PropertiesManager tmp = (PropertiesManager) this.nowPM.get();
			if (tmp == null)
			{
				return false;
			}
			if (tmp.properties.getProperty(key) == null)
			{
				// 如果当前的配置属性管理器中没有此键值, 则说明是需要使用父配置属性管理器中
				// 的值, 当父配置中的属性变化时, 需要触发事件
				tmp.firePropertyChanged(key, oldValue, newValue);
			}
			return true;
		}

	}


	/**
	 * 默认的配置变化监听者.
	 */
	private static class DefaultPropertyListener
			implements PropertyListener
	{
		private final Map propertyMap = new HashMap();

		public synchronized void addPropertyManager(String key, PropertyManager pm)
		{
			PropertyManager[] pms = (PropertyManager[]) this.propertyMap.get(key);
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
			this.propertyMap.put(key, pms);
		}

		public synchronized void removePropertyManager(String key, PropertyManager pm)
		{
			PropertyManager[] pms = (PropertyManager[]) this.propertyMap.get(key);
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
						this.propertyMap.remove(key);
						return;
					}
					PropertyManager[] newPms = new PropertyManager[pms.length - 1];
					System.arraycopy(pms, 0, newPms, 0, i);
					System.arraycopy(pms, i + 1, newPms, i, pms.length - i - 1);
					pms = newPms;
					this.propertyMap.put(key, pms);
					return;
				}
			}
		}

		public boolean propertyChanged(String key, String oldValue, String newValue)
		{
			// 判断新的值和原值是否相等
			if (oldValue != null)
			{
				if (oldValue.equals(newValue))
				{
					return true;
				}
			}
			else if (newValue == null)
			{
				return true;
			}

			PropertyManager[] pms = (PropertyManager[]) this.propertyMap.get(key);
			if (pms == null)
			{
				return true;
			}

			for (int i = 0; i < pms.length; i++)
			{
				try
				{
					pms[i].changeProperty(newValue);
				}
				catch (Throwable ex)
				{
					Log log = Utility.createLog(LOGGER_NAME);
					log.warn("Error when change property.", ex);
				}
			}
			return true;
		}

	}

	/**
	 * 是否对成员对象使用弱引用.
	 * 如果需要使用则可将这个变量设为true, 否则默认值为false,
	 * 表示使用软引用.
	 */
 	public static boolean weakRefMember = false;

	/**
	 * 单个属性的管理器, 给默认的配置变化监听者使用.
	 */
	private static class PropertyManager
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

		private Member getOptMember()
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
			if (weakRefMember)
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
							Utility.createLog(LOGGER_NAME).warn(msg, ex);
						}
						return;
					}
				}
				if (!theField.isAccessible())
				{
					theField.setAccessible(true);
					theField.set(null, objValue);
					theField.setAccessible(false);
				}
				else
				{
					theField.set(null, objValue);
				}
			}
			else
			{
				Method theMethod = (Method) member;
				if (!theMethod.isAccessible())
				{
					theMethod.setAccessible(true);
					theMethod.invoke(null, new Object[]{value});
					theMethod.setAccessible(false);
				}
				else
				{
					theMethod.invoke(null, new Object[]{value});
				}
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

	private static class BaseClassRef extends WeakReference
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

}