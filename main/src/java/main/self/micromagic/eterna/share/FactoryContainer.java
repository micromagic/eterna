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
	 * 获得工厂实例的id.
	 */
	String getId();

	/**
	 * 获得工厂容器的初始化配置.
	 */
	String getInitConfig();

	/**
	 * (重新)初始化工厂
	 */
	void reInit();

	/**
	 * (重新)初始化工厂
	 * @param msg  存放初始化的返回信息
	 */
	void reInit(StringRef msg);

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
	 * 此对象必须实现<code>self.micromagic.eterna.share.EternaInitialize</code>接口,
	 * 还必须定义afterEternaInitialize(FactoryContainer)方法, 在初始化完毕后
	 * 会调用此方法.
	 *
	 * @param obj    初始化监听者
	 * @see self.micromagic.eterna.share.EternaInitialize
	 */
	void addInitializedListener(Object obj);

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