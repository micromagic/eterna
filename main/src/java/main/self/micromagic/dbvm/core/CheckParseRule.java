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

package self.micromagic.dbvm.core;

import org.dom4j.Element;

import self.micromagic.cg.BeanTool;
import self.micromagic.eterna.digester2.Digester;
import self.micromagic.eterna.digester2.ParseRule;

/**
 * 检测处理的解析规则.
 */
public class CheckParseRule extends ParseRule
{
	public CheckParseRule(Digester digester)
	{
		super(digester);
	}

	public boolean doRule(Element element)
	{
		CheckHandler check = new CheckHandler();
		CheckHandler.initCurrentCheck(check);
		try
		{
			this.digester.push(this);
			this.digester.push(BeanTool.getBeanMap(check));
			return super.doRule(element);
		}
		finally
		{
			// 已通过popStack方法将堆栈弹出, 这里不需要在pop
			CheckHandler.removeCurrentCheck();
		}
	}

	/**
	 * 将已压入的堆栈弹出, 这样不会影响内部节点registerObject的调用.
	 */
	public void popStack(CheckHandler checkHandler)
	{
		this.digester.pop();
		this.digester.pop();
	}

}
