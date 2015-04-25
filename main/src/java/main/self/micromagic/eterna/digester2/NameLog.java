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

package self.micromagic.eterna.digester2;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

import self.micromagic.util.container.ThreadCache;
import self.micromagic.util.ref.IntegerRef;

/**
 * 进行处理到的节点日志记录.
 */
public class NameLog
		implements ElementProcessor
{
	public static final String __FLAG = "log:";
	public static NameLog getInstance()
	{
		return instance;
	}
	private static NameLog instance = new NameLog();

	public ElementProcessor parse(Digester digester, ParseRule rule,
			String config, IntegerRef position)
	{
		int nBegin = position.value += 1;
		int nEnd = ParseRule.findItemEnd(config, position);
		String aName = config.substring(nBegin, nEnd).trim();
		if (config.charAt(nEnd) != ParseRule.BLOCK_END)
		{
			// 没有块结束符
			throw new ParseException("Error config [" + config + "] for NameLog.");
		}
		position.value = nEnd + 1;
		NameLog r = new NameLog();
		r.attrName = aName.length() > 0 ? aName : null;
		r.onlyElement = SPECIAL_FLAG.equals(aName);
		return r;
	}
	private boolean onlyElement;
	private String attrName;

	/**
	 * 标记仅仅记录元素节点等特殊的标签.
	 */
	private static final String SPECIAL_FLAG = "$";
	/**
	 * 线程中记录元素节点的堆栈.
	 */
	private static final String ELEMENT_STACK_FLAG = "eterna.digester2.element.stack";

	public boolean begin(Digester digester, Element element)
	{
		String objFlag = null;
		if (this.onlyElement)
		{
			ContextInfo ci = ParseException.getContextInfo();
			if (ci.element != null)
			{
				ThreadCache tc = ThreadCache.getInstance();
				List stack = (List) tc.getProperty(ELEMENT_STACK_FLAG);
				if (stack == null)
				{
					stack = new ArrayList();
					tc.setProperty(ELEMENT_STACK_FLAG, stack);
				}
				stack.add(ci.objName);
				stack.add(ci.element);
			}
		}
		else
		{
			objFlag = element.getName();
			if (this.attrName != null)
			{
				if (this.attrName.startsWith(SPECIAL_FLAG))
				{
					String tmp = this.attrName.substring(1);
					if ("$parent".equals(tmp))
					{
						objFlag = element.getParent().getName() + "'s " + objFlag;
					}
					else
					{
						objFlag = tmp;
					}
				}
				else
				{
					objFlag = objFlag + ":" + element.attributeValue(this.attrName);
				}
			}
		}
		ParseException.setContextInfo(objFlag, element);
		return true;
	}

	public void end(Digester digester, Element element)
	{
		if (this.onlyElement)
		{
			List stack = (List) ThreadCache.getInstance().getProperty(ELEMENT_STACK_FLAG);
			if (stack != null && stack.size() > 0)
			{
				Element e = (Element) stack.remove(stack.size() - 1);
				String objFlag = (String) stack.remove(stack.size() - 1);
				ParseException.setContextInfo(objFlag, e);
			}
		}
	}

}