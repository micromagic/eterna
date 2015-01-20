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

import java.lang.reflect.Field;

import org.dom4j.Element;

import self.micromagic.util.IntegerRef;

/**
 * 将当前规则注入到原有规则的子规则列表中.
 */
public class InjectSub
		implements ElementProcessor
{
	public static final String __FLAG = "injectSub:";
	public static InjectSub getInstance()
	{
		return instance;
	}
	private static InjectSub instance = new InjectSub();

	public ElementProcessor parse(Digester digester, ParseRule rule,
			String config, IntegerRef position)
	{
		position.value += 1;
		while (config.charAt(position.value - 1) != ParseRule.BLOCK_END)
		{
			int tmpBegin = position.value;
			int tmpEnd = ParseRule.findItemEnd(config, position);
			String tmpStr = config.substring(tmpBegin, tmpEnd).trim();
			ParseRule tmpRule = digester.getRule(tmpStr);
			if (tmpRule == null)
			{
				throw new ParseException("Not found rule [" + tmpStr + "], config [" + config + "] for SubRuleCaller.");
			}
			this.doInject(tmpRule, rule);
			position.value = tmpEnd + 1;
		}
		return this;
	}

	private void doInject(ParseRule target, ParseRule current)
	{
		try
		{
			Field f = ParseRule.class.getDeclaredField("epList");
			f.setAccessible(true);
			ElementProcessor[] epArr = (ElementProcessor[]) f.get(target);
			f = SubRuleCaller.class.getDeclaredField("rules");
			f.setAccessible(true);
			for (int i = 0; i < epArr.length; i++)
			{
				if (epArr[i] instanceof SubRuleCaller)
				{
					ParseRule[] rules = (ParseRule[]) f.get(epArr[i]);
					ParseRule[] newRules = new ParseRule[rules.length + 1];
					System.arraycopy(rules, 0, newRules, 0, rules.length);
					newRules[newRules.length - 1] = current;
					f.set(epArr[i], newRules);
				}
			}
		}
		catch (RuntimeException ex)
		{
			throw ex;
		}
		catch (Exception ex)
		{
			throw new ParseException(ex);
		}
	}

	public boolean begin(Digester digester, Element element)
	{
		return true;
	}

	public void end(Digester digester, Element element)
	{
	}

}