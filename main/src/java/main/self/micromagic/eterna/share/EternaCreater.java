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

package self.micromagic.eterna.share;

/**
 * 对象构造器.
 * 注册到工厂中用于创建对象.
 */
public interface EternaCreater extends Generator
{
	/**
	 * 执行初始化.
	 *
	 * @param factory  对象构造器所属的工厂
	 * @return  是否已初始化
	 *          false表示是第一次初始化, true表示已执行过初始化
	 */
	boolean initialize(EternaFactory factory) throws EternaException;

	/**
	 * 获取所创建的对象的类型.
	 */
	Class getObjectType();

	/**
	 * 所创建的对象是否为单例.
	 */
	boolean isSingleton();

	/**
	 * 获取对象构造器所属的工厂.
	 */
	EternaFactory getFactory();

	/**
	 * 销毁对象构造器.
	 */
	void destroy();

}
