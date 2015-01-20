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

/**
 * 一个eterna对象.
 */
public interface EternaObject
{
	/**
	 * 执行初始化.
	 *
	 * @return  是否已初始化
	 *          false表示是第一次初始化, true表示已执行过初始化
	 */
	boolean initialize(EternaFactory factory) throws EternaException;

	/**
	 * 获取对象的名称.
	 */
	String getName() throws EternaException;

}