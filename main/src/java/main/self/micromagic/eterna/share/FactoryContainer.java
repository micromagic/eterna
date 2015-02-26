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

package self.micromagic.eterna.share;

import self.micromagic.util.StringRef;

/**
 * 一个工厂实例的容器.
 */
public interface FactoryContainer
{
	/**
	 * 在attribute中存放ServletContext对象的键值.
	 */
	String SERVLET_FLAG = "eterna.servlet";

	/**
	 * 在attribute中存放ClassLoader对象的键值.
	 */
	String CLASSLOADER_FLAG = "eterna.classLoader";

	/**
	 * 在attribute中存放配置参数的键值.
	 */
	String CONFIG_PARAM_FLAG = "eterna.config.param";

	/**
	 * 在attribute中存放最小重新载入配置资源的时间间隔的键值.
	 *
	 */
	String RELOAD_TIME_FLAG = "eterna.reload.time";

	/**
	 * 在attribute中存放已被载入的资源的URI的集合的键值.
	 */
	String URIS_FLAG = "eterna.uris";

	/**
	 * 获得工厂容器实例的id.
	 */
	String getId();

	/**
	 * (重新)初始化工厂容器.
	 */
	void reInit();

	/**
	 * (重新)初始化工厂容器.
	 * @param msg  存放初始化的返回信息
	 */
	void reInit(StringRef msg);

	/**
	 * 判断当前工厂容器是否已完成初始化.
	 */
	boolean isInitialized();

	/**
	 * 设置自定义的属性. <p>
	 *
	 * @param name   属性的名称
	 * @param attr   属性值
	 */
	void setAttribute(String name, Object attr);

	/**
	 * 移除自定义的属性.
	 *
	 * @param name   属性的名称
	 */
	void removeAttribute(String name);

	/**
	 * 获取自定义的属性.
	 *
	 * @param name   属性的名称
	 * @return   属性值
	 */
	Object getAttribute(String name);

	/**
	 * 添加一个初始化监听者. <p>
	 *
	 * @param l  初始化监听者
	 * @see self.micromagic.eterna.share.InitializeListener
	 */
	void addInitializeListener(InitializeListener l);

	/**
	 * 获得容器中的工厂实例.
	 *
	 * @return   工厂实例
	 */
	Factory getFactory();

	/**
	 * 当此工厂实例的生命周期结束时, 会调用此方法.
	 */
	void destroy();

}