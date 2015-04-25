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

import java.util.Map;

/**
 * 定义在<code>ClassLoader</code>中, 存放缓存数据的类.
 * 这个类只是为了获取定义类二进制流使用, 通过反射定义到指定的
 * <code>ClassLoader</code>中.
 *
 * @author micromagic@sina.com
 */
public class ClassKeyCache$Caches
{
	/**
	 * 用于缓存数据的map.
	 * 在ClassKeyCache$CacheCellImpl1.getCache0中初始化
	 * new SynHashMap(8, SynHashMap.WEAK)
	 */
	public static Map caches;

}