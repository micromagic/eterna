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
 * 设置一个bean属性的工具.
 *
 * @author micromagic@sina.com
 */
public interface BeanPropertyWriter
{
	/**
	 * 获取此属性对应的成员.
	 */
	Member getMember();

	/**
	 * 设置一个属性的值.
	 *
	 * @param cd            属性描述类
	 * @param indexs        如果属性是个数组或Collection, 可通过此索引值来控制设置哪个值
	 * @param bean          属性所在的bean对象
	 * @param value         要设置的值
	 * @param prefix        当前的名称前缀
	 * @param beanMap       当前的BeanMap对象
	 * @param originObj     设置的值所在的原始对象, 可能是一个Map, 也可能是一个ResultRow,
	 *                      也可能是null(当原始对象不存在时)
	 * @param oldValue      该属性的原始值
	 * @return              成功设置了值的属性的个数
	 */
	int setBeanValue(CellDescriptor cd, int[] indexs, Object bean, Object value, String prefix,
			BeanMap beanMap, Object originObj, Object oldValue) throws Exception;

}