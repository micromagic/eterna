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

package self.micromagic.eterna.search.impl;

import java.util.HashMap;
import java.util.Map;

import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.search.ConditionBuilder;
import self.micromagic.eterna.search.ConditionBuilderGenerator;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;

/**
 * @author micromagic@sina.com
 */
public class ConditionBuilderGeneratorImpl extends AbstractGenerator
		implements ConditionBuilderGenerator
{
	// 这里再定义一边, 防止ConditionBuilder接口中定义的被修改
	private static final String[] OPERATOR_NAMES = {
		"isNull", "notNull", "checkNull",
		"equal", "notEqual", "large", "below", "notLarge", "notBelow",
		"beginWith", "endWith", "include", "match"
	};

	private static Map builderMap = new HashMap();

	private String caption;
	private String operator;

	static
	{
		int index = 0;
		builderMap.put(OPERATOR_NAMES[index++], new ConditionBuilderImpl("IS NULL", -1));
		builderMap.put(OPERATOR_NAMES[index++], new ConditionBuilderImpl("IS NOT NULL", -1));
		builderMap.put(OPERATOR_NAMES[index++],
				new ConditionBuilderImpl(ConditionBuilderImpl.CHECK_OPT_TAG, -1));

		builderMap.put(OPERATOR_NAMES[index++], new ConditionBuilderImpl("=", 0));
		builderMap.put(OPERATOR_NAMES[index++], new ConditionBuilderImpl("<>", 0));
		builderMap.put(OPERATOR_NAMES[index++], new ConditionBuilderImpl(">", 0));
		builderMap.put(OPERATOR_NAMES[index++], new ConditionBuilderImpl("<", 0));
		builderMap.put(OPERATOR_NAMES[index++], new ConditionBuilderImpl("<=", 0));
		builderMap.put(OPERATOR_NAMES[index++], new ConditionBuilderImpl(">=", 0));
		builderMap.put(OPERATOR_NAMES[index++],
				new ConditionBuilderImpl(ConditionBuilderImpl.LIKE_OPT_TAG, 1));
		builderMap.put(OPERATOR_NAMES[index++],
				new ConditionBuilderImpl(ConditionBuilderImpl.LIKE_OPT_TAG, 2));
		builderMap.put(OPERATOR_NAMES[index++],
				new ConditionBuilderImpl(ConditionBuilderImpl.LIKE_OPT_TAG, 3));
		builderMap.put(OPERATOR_NAMES[index++],
				new ConditionBuilderImpl(ConditionBuilderImpl.LIKE_OPT_TAG, 0));
	}

	public void setCaption(String caption)
	{
		this.caption = caption;
	}

	public void setOperator(String operator)
	{
		this.operator = operator;
	}

	public Object create()
			throws ConfigurationException
	{
		return this.createConditionBuilder();
	}

	public ConditionBuilder createConditionBuilder()
			throws ConfigurationException
	{
		ConditionBuilderImpl cb = (ConditionBuilderImpl) builderMap.get(this.operator);
		if (cb == null)
		{
			cb = new ConditionBuilderImpl("=", 0);
		}
		else
		{
			cb = cb.copy();
		}
		cb.name = this.name;
		cb.caption = this.caption == null ? this.name : this.caption;
		return cb;
	}

	/**
	 * 处理匹配查询的字符串中需要转义的字符. <p>
	 * 如果没有需要转义的字符串, 会直接返回原字符串, 因此可以
	 * 用<code>newStr == oldStr</code>判断是否有处理.
	 */
	public static String dealEscapeString(String str)
	{
		if (str == null)
		{
			return null;
		}
		StringAppender temp = null;
		int modifyCount = 0;
		for (int i = 0; i < str.length(); i++)
		{
			char c = str.charAt(i);
			String appendStr = null;
			if (c == '%' || c == '_' || c == '\\')
			{
				appendStr = "\\" + c;
				modifyCount++;
			}
			if (modifyCount == 1)
			{
				temp = StringTool.createStringAppender(str.length() + 16)
						.append(str.substring(0, i));
				//这里将modifyCount的个数增加, 防止下一次调用使他继续进入这个初始化
				modifyCount++;
			}
			if (modifyCount > 0)
			{
				if (appendStr == null)
				{
					temp.append(c);
				}
				else
				{
					temp.append(appendStr);
				}
			}
		}
		return temp == null ? str : temp.toString();
	}

}