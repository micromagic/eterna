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

import self.micromagic.eterna.digester.FactoryManager;

/**
 * Eterna的初始化配置. <p>
 * 假如你想在Eterna初始化完后, 初始化你的类, 请实现此接口, 并在你的类中加上
 * 下面这些方法.
 * 然后在配置文件中的self.micromagic.eterna.digester.initClasses
 * 属性中加入你的类名(包括类路径), 类之间用";"分割.
 * 如果你使用的是base class来初始化, 则只需base class实现此接口, 而不需要在
 * 配置文件中添加定义.
 *
 * 需要定义的方法如下:
 * private static void afterEternaInitialize(FactoryContainer factoryContainer)
 *
 * 如果初始化完成的通知需要发送到类的实例, 则不要将此方法定义成静态的, 如:
 * private void afterEternaInitialize(FactoryContainer factoryContainer)
 *
 * @see FactoryManager.Instance#createClassFactoryManager(Class)
 * @see FactoryManager.Instance#addInitializedListener(Object)
 *
 *
 * 如果你使用的是base class来初始化, 并需要自动重载的功能.
 *
 * 需要定义的方法如下:
 * private static long autoReloadTime()
 * 返回值为检查重载的间隔毫秒数, 至少要大于200.
 */
public interface EternaInitialize
{

}