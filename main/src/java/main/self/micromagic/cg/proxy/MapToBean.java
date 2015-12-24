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

import java.util.Map;

/**
 * 将map中的属性设置到bean中的工具.
 *
 * @author micromagic@sina.com
 */
public interface MapToBean
{
	/**
	 * 将map中的数据设置到bean的属性中.
	 *
	 * @param bean     被设置属性的beean
	 * @param values   值所在的map
	 * @param prefix   获取值所用的名称的前缀
	 *                 如：
	 *                 prefix = "" 时使用values.get("name")
	 *                 prefix = "sub." 时使用values.get("sub.name")
	 * @return     成功设置了的属性个数
	 */
	public int setBeanValues(Object bean, Map values, String prefix) throws Exception;

}