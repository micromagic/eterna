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

package self.micromagic.cg;

/**
 * 数组类型的转换器.
 *
 * @author micromagic@sina.com
 */
public interface ArrayConverter
{
	/**
	 * 处理数组类型的转换.
	 *
	 * @param array      需要被转换的数组
	 * @param destArr    目标数组对象
	 * @param converter  类型转换器, 可以是BeanMap或ValueConverter
	 * @param needThrow  当给出的数组不是基本类型数组时, 是否要抛出异常
	 * @return  转换后的数组
	 */
	Object convertArray(Object array, Object destArr, Object converter, boolean needThrow) throws Exception;

	/**
	 * 处理数组类型的转换.
	 *
	 * @param array      需要被转换的数组
	 * @param converter  类型转换器, 可以是BeanMap或ValueConverter
	 * @param needThrow  当给出的数组不是基本类型数组时, 是否要抛出异常
	 * @return  转换后的数组
	 */
	Object convertArray(Object array, Object converter, boolean needThrow) throws Exception;

}