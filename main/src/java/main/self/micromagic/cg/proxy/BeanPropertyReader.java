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

package self.micromagic.cg.proxy;

import java.lang.reflect.Member;

import self.micromagic.cg.BeanMap;
import self.micromagic.cg.CellDescriptor;

/**
 * 读取一个bean属性的工具.
 *
 * @author micromagic@sina.com
 */
public interface BeanPropertyReader
{
	/**
	 * 获取此属性对应的成员.
	 */
	Member getMember();

	/**
	 * 读取一个属性的值.
	 *
	 * @param cd            属性描述类
	 * @param indexs        如果属性是个数组或Collection, 可通过此索引值来控制读取哪个值
	 * @param bean          属性所在的bean对象
	 * @param prefix        当前的名称前缀
	 * @param beanMap       当前的BeanMap对象
	 * @return              对应的属性的值
	 */
	Object getBeanValue(CellDescriptor cd, int[] indexs, Object bean, String prefix,
			BeanMap beanMap) throws Exception;

}