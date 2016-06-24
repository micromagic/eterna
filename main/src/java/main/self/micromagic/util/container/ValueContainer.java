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

package self.micromagic.util.container;

import java.util.Enumeration;

/**
 * 一个数据容器, 可通过这个容器方便得构造一个map对象.
 *
 * @see ValueContainerMap
 */
public interface ValueContainer
{
	/**
	 * Get the value at the key.
	 */
	Object getValue(Object key);

	/**
	 * Check this container contains the key.
	 */
	boolean containsKey(Object key);

	/**
	 * Set the value to the key.
	 */
	void setValue(Object key, Object value);

	/**
	 * Remove the value at the key.
	 */
	void removeValue(Object key);

	/**
	 * Get the keys enumeration.
	 */
	Enumeration getKeys();

}
