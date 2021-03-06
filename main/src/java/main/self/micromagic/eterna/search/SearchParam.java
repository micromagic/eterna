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

package self.micromagic.eterna.search;

import java.util.Map;

/**
 * 查询的参数对象.
 */
public class SearchParam
{
	/**
	 * 查询的条件.
	 */
	public Map condition;

	/**
	 * 特殊格式的条件.
	 * 数组中每个元素可以是一个ConditionInfo对象或一个数组.
	 */
	public Object[] conditionStruct;

	/**
	 * 是否需要忽略空字符串.
	 */
	public boolean skipEmpty;

	/**
	 * 查询的方式.
	 *
	 * @see SearchAttributes#queryTypeTag
	 */
	public String queryType;

	/**
	 * 当前的页数.
	 */
	public int pageNum;

	/**
	 * 分页的大小.
	 */
	public int pageSize;

	/**
	 * 是否读取所有行.
	 * 当设置为true时, 将忽略pageNum, pageSize属性.
	 */
	public boolean allRow;

}
