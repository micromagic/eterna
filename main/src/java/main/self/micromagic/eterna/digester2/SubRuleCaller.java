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

package self.micromagic.eterna.digester2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import self.micromagic.util.IntegerRef;

/**
 * 调用子规则.
 */
public class SubRuleCaller
		implements ElementProcessor
{
	static final String __FLAG = "sub:";
	public static SubRuleCaller getInstance()
	{
		return instance;
	}
	private static SubRuleCaller instance = new SubRuleCaller();

	private ParseRule[] rules;

	/**
	 * 解析配置信息.
	 * 格式: sub:{name1,name2}
	 */
	public ElementProcessor parse(Digester digester, String config, IntegerRef position)
	{
		position.value += 1;
		List rules = new ArrayList();
		while (config.charAt(position.value - 1) != ParseRule.BLOCK_END)
		{
			int tmpBegin = position.value;
			int tmpEnd = ParseRule.findItemEnd(config, position);
			String tmpStr = config.substring(tmpBegin, tmpEnd).trim();
			ParseRule rule = digester.getRule(tmpStr);
			if (rule == null)
			{
				throw new ParseException("Not found rule [" + tmpStr + "], config [" + config + "] for SubRuleCaller.");
			}
			rules.add(rule);
			position.value = tmpEnd + 1;
		}
		SubRuleCaller sub = new SubRuleCaller();
		int size = rules.size();
		sub.rules = new ParseRule[size];
		rules.toArray(sub.rules);
		return sub;
	}

	public boolean begin(Digester digester, Element element)
	{
		Iterator itr = element.elementIterator();
		while (itr.hasNext())
		{
			digester.doRules((Element) itr.next(), this.rules);
		}
		return true;
	}

	public void end(Digester digester, Element element)
	{
	}

}