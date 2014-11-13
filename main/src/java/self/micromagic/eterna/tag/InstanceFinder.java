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

package self.micromagic.eterna.tag;

import self.micromagic.eterna.digester.FactoryManager;

/**
 * 工厂实例的查询者.
 *
 * @author micromagic@sina.com
 */
public interface InstanceFinder
{
	/**
	 * 根据给出的名称查找一个工厂的实例.
	 *
	 * @param name   将通过此名称查找工厂实例
	 * @return  查到的工厂实例, 或<code>null</code>没有查到
	 */
	FactoryManager.Instance findInstance(String name);

}