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
 * 工厂容器的初始化监听器. <p>
 * 假如你想在工厂容器初始化完后, 初始化你的类, 请实现此接口.
 * 可通工厂容器的addInitializeListener注册此监听器.
 *
 * @see FactoryContainer#addInitializeListener(InitializeListener)
 */
public interface InitializeListener
{
	/**
	 * 当一个工厂容器初始化完成后, 或重新初始化后, 会调用此方法.
	 *
	 * @param factoryContainer  初始化完成的工厂容器
	 */
	void afterInitialize(FactoryContainer factoryContainer) throws EternaException;

}