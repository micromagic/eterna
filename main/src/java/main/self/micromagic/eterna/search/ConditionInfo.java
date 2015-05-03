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
import java.io.Serializable;
import java.io.Writer;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.view.DataPrinter;
import self.micromagic.util.StringTool;

/**
 * 单个查询查询条件单元的信息.
 */
public final class ConditionInfo
		implements DataPrinter.BeanPrinter, Serializable
{
	/**
	 * 条件的名称.
	 */
	public final String name;

	/**
	 * 与前一个条件连接所使用的操作符.
	 */
	public final String linkOpt;

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
	private transient ConditionBuilder builder;

	public ConditionInfo(String name, String linkOpt, Object value, ConditionBuilder builder)
	{
		this.name = name;
		this.linkOpt = this.parseLinkOpt(linkOpt);
		this.value = value;
		this.builder = builder;
		this.builderName = builder.getName();
	}

	public ConditionInfo(String name, String linkOpt, Object value, String builderName)
	{
		this.name = name;
		this.linkOpt = this.parseLinkOpt(linkOpt);
		this.value = value;
		this.builder = null;
		this.builderName = builderName;
	}

	/**
	 * 获取条件构造器.
	 */
	public ConditionBuilder getConditionBuilder(EternaFactory factory)
	{
		ConditionBuilder tmp = this.builder;
		if (tmp == null && !StringTool.isEmpty(this.builderName))
		{
			tmp = factory.getConditionBuilder(this.builderName);
			this.builder = tmp;
		}
		return tmp;
	}

	private String parseLinkOpt(String linkOpt)
	{
		if (StringTool.isEmpty(linkOpt))
		{
			return "AND";
		}
		linkOpt = linkOpt.trim();
		if ("and".equalsIgnoreCase(linkOpt))
		{
			return "AND";
		}
		if ("or".equalsIgnoreCase(linkOpt))
		{
			return "OR";
		}
		if ("not".equalsIgnoreCase(linkOpt))
		{
			return "AND NOT";
		}
		String[] arr = StringTool.separateString(linkOpt, " ");
		if (arr.length == 2)
		{
			if ("not".equalsIgnoreCase(arr[1]))
			{
				if ("and".equalsIgnoreCase(arr[0]))
				{
					return "AND NOT";
				}
				if ("or".equalsIgnoreCase(linkOpt))
				{
					return "OR NOT";
				}
			}
		}
		throw new EternaException("Error link opt [" + linkOpt + "].");
	}

	public void print(DataPrinter p, Writer out, Object bean)
			throws IOException, EternaException
	{
		p.printObjectBegin(out);
		p.printPair(out, "linkOpt", this.linkOpt, true);
		p.printPair(out, "name", this.name, false);
		p.printPair(out, "value", this.value, false);
		p.printPair(out, "builder", this.builderName, false);
		p.printObjectEnd(out);
	}

	private static final long serialVersionUID = -3784038182140520505L;

}