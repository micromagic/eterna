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
 * 判断给出的类名是否为bean的检查器.
 *
 * @author micromagic@sina.com
 */
public interface BeanChecker
{
	/**
	 * 检查结果，是.
	 */
	public static final int CHECK_RESULT_YES = 1;

	/**
	 * 检查结果，否.
	 */
	public static final int CHECK_RESULT_NO = -1;

	/**
	 * 检查结果，不明.
	 */
	public static final int CHECK_RESULT_UNKNOW = 0;


	public int check(Class beanClass);

}