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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import self.micromagic.util.StringTool;

/**
 * 将当前规则注入到原有规则列表中.
 */
public class InjectRule extends ParseRule
{
	public InjectRule(Digester digester)
	{
		super(digester);
	}

	public void init(String config)
	{
		super.init(config);
		String[] rules = StringTool.separateString(this.getPattern(), ",", true);
		for (int i = 0; i < rules.length; i++)
		{
			ParseRule tmpRule = this.digester.getRule(rules[i]);
			this.doInject(tmpRule);
		}
	}

	private void doInject(ParseRule target)
	{
		try
		{
			Field f = ParseRule.class.getDeclaredField("epList");
			f.setAccessible(true);
			ElementProcessor[] epArrOld = (ElementProcessor[]) f.get(target);
			ElementProcessor[] epArrCurr = (ElementProcessor[]) f.get(this);
			ElementProcessor[] epArrNew = new ElementProcessor[epArrOld.length + epArrCurr.length];
			List tmp = new LinkedList();
			boolean currAdded = false;
			for (int i = 0; i < epArrOld.length; i++)
			{
				if (!currAdded && epArrOld[i] instanceof StackBinder)
				{
					// 如果遇到StackBinder, 将curr添加到其之前
					currAdded = true;
					tmp.addAll(Arrays.asList(epArrCurr));
				}
				tmp.add(epArrOld[i]);
			}
			if (!currAdded)
			{
				tmp.addAll(Arrays.asList(epArrCurr));
			}
			tmp.toArray(epArrNew);
			f.set(target, epArrNew);
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

}