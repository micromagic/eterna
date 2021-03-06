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
 * 扩展对象查找者.
 */
public interface ExtObjectFinder
{
	/**
	 * 根据指定的名称查找一个对象.
	 */
	Object findObject(String name);

	/**
	 * 根据指定的名称判断一个对象是否为单例. <p>
	 * 如果对象不存在则返回null.
	 */
	Boolean isSingleton(String name);

}
