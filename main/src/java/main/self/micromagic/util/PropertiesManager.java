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

import self.micromagic.eterna.digester2.ConfigResource;

/**
 * 配置属性的管理器.
 */
public class PropertiesManager extends InternalPropertiesManager
{
	/**
	 * 默认的构造函数.
	 * 默认的配置文件名及本类的<code>ClassLoader</code>.
	 *
	 * @param needLoad  是否需要在初始化完成之后立刻执行配置加载
	 */
	PropertiesManager(boolean needLoad)
	{
		super(createBaseResource(DEFAULT_PROP_LOCAL, null), null, needLoad);
	}

	/**
	 * 构造一个配置属性的管理器.
	 *
	 * @param propLocal     配置文件所在的位置
	 * @param classLoader   读取配置文件所使用的<code>ClassLoader</code>
	 */
	public PropertiesManager(String propLocal, ClassLoader classLoader)
	{
		super(createBaseResource(propLocal, classLoader), null, true);
	}

	/**
	 * 构造一个配置属性的管理器.
	 *
	 * @param propLocal     配置文件所在的位置
	 * @param classLoader   读取配置文件所使用的<code>ClassLoader</code>
	 * @param parent        当前配置管理器所继承的父配置管理器
	 */
	public PropertiesManager(String propLocal, ClassLoader classLoader, PropertiesManager parent)
	{
		super(createBaseResource(propLocal, classLoader), parent, true);
	}

	/**
	 * 构造一个配置属性的管理器.
	 *
	 * @param resource    配置文件资源对象
	 * @param parent      当前配置管理器所继承的父配置管理器
	 */
	public PropertiesManager(ConfigResource resource, PropertiesManager parent)
	{
		super(resource, parent, true);
	}

	/**
	 * 构造一个配置属性的管理器.
	 *
	 * @param resource    配置文件资源对象
	 * @param parent      当前配置管理器所继承的父配置管理器
	 * @param needLoad    是否需要在初始化完成之后立刻执行配置加载
	 */
	protected PropertiesManager(ConfigResource resource, PropertiesManager parent, boolean needLoad)
	{
		super(resource, parent, needLoad);
	}

	/**
	 * 添加一个配置变化的监听者.
	 */
	public void addPropertyListener(PropertyListener l)
	{
		super.addPropertyListener0(l);
	}

	/**
	 * 移除一个配置变化的监听者.
	 */
	public void removePropertyListener(PropertyListener l)
	{
		super.removePropertyListener0(l);
	}

	/**
	 * 配置变化的监听者.
	 */
	public interface PropertyListener extends InternalPropertyListener
	{
	}

}
