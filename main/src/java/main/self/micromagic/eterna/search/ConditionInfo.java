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

import java.io.IOException;
import java.io.Writer;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.view.DataPrinter;

/**
 * 单个查询查询条件单元的信息.
 */
public final class ConditionInfo
		implements DataPrinter.BeanPrinter
{
	/**
	 * 条件的名称.
	 */
	public final String name;

	/**
	 * 条件所属条件组的名称.
	 */
	public final String group;

	/**
	 * 设置在该条件上的值.
	 */
	public final Object value;

	/**
	 * 构成该条件所要使用ConditionBuilder的名称.
	 */
	public final String builderName;

	/**
	 * 构成该条件所要使用的ConditionBuilder.
	 */
	public final ConditionBuilder builder;

	public ConditionInfo(String name, String group, Object value, ConditionBuilder builder)
	{
		this.name = name;
		this.group = group;
		this.value = value;
		this.builder = builder;
		this.builderName = builder.getName();
	}

	public ConditionInfo(String name, String group, Object value, String builderName)
	{
		this.name = name;
		this.group = group;
		this.value = value;
		this.builder = null;
		this.builderName = builderName;
	}

	public void print(DataPrinter p, Writer out, Object bean)
			throws IOException, EternaException
	{
		p.printObjectBegin(out);
		p.printPair(out, "group", this.group, true);
		p.printPair(out, "name", this.name, false);
		p.printPair(out, "value", this.value, false);
		p.printPair(out, "builder", this.builderName, false);
		p.printObjectEnd(out);
	}

}