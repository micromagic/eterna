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

import org.dom4j.Element;

import self.micromagic.cg.BeanMap;
import self.micromagic.eterna.share.Generator;
import self.micromagic.eterna.share.Tool;
import self.micromagic.util.converter.BooleanConverter;
import self.micromagic.util.ref.IntegerRef;

/**
 * 调用堆栈中的对象的方法, 设置堆栈中的值.
 */
public class StackBinder
		implements ElementProcessor
{
	public static final String __FLAG = "stack:";
	public static StackBinder getInstance()
	{
		return instance;
	}
	private static StackBinder instance = new StackBinder();

	private String methodName;
	private int targetIndex;
	private int objIndex;
	private boolean needName;
	private AttrGetter attrGetter;
	private boolean needGenerate;
	private boolean execInBegin;

	public ElementProcessor parse(Digester digester, ParseRule rule,
			String config, IntegerRef position)
	{
		return parseConfig(config, position);
	}

	/**
	 * 解析配置信息, 生成StackBinder.
	 * 格式: stack:{mName,t:1,o:0,n:0,g:0}
	 * mName: 需要调用的方法名
	 * t: 方法所在对象在堆栈中的位置, 默认为1
	 * o: 参数对象在堆栈中的位置, 默认为0
	 * n: 是否需要获取对象的名称(对象必须实现Generator接口), 默认为false
	 * g: 是否需要调用对象的create方法(对象必须实现Generator接口), 默认为false
	 */
	public static StackBinder parseConfig(String config, IntegerRef position)
	{
		int mBegin = position.value += 1;
		int mEnd = ParseRule.findItemEnd(config, position);
		String mName = config.substring(mBegin, mEnd).trim();
		if (mName.length() == 0)
		{
			// 没有方法名不能解析为StackBinder
			throw new ParseException("Error config [" + config + "] for StackBinder.");
		}
		int tIndex = 1;
		int oIndex = 0;
		boolean needName = false;
		AttrGetter attrGetter = null;
		boolean needGenerate = false;
		boolean execInBegin = false;
		position.value = mEnd + 1;
		while (config.charAt(position.value - 1) != ParseRule.BLOCK_END)
		{
			int tmpBegin = position.value;
			int tmpEnd = ParseRule.findItemEnd(config, position);
			String tmpStr = config.substring(tmpBegin, tmpEnd).trim();
			if (tmpStr.startsWith("t:"))
			{
				tIndex = Integer.parseInt(tmpStr.substring(2).trim());
			}
			else if (tmpStr.startsWith("o:"))
			{
				oIndex = Integer.parseInt(tmpStr.substring(2).trim());
			}
			else if (tmpStr.startsWith("n:"))
			{
				needName = BooleanConverter.toBoolean(tmpStr.substring(2).trim());
			}
			else if (tmpStr.startsWith("g:"))
			{
				needGenerate = BooleanConverter.toBoolean(tmpStr.substring(2).trim());
			}
			else if (tmpStr.startsWith("begin:"))
			{
				execInBegin = BooleanConverter.toBoolean(tmpStr.substring(6).trim());
			}
			else if (tmpStr.startsWith("attrName:"))
			{
				needName = true;
				position.value += "attrName:".length();
				attrGetter = AttrBinder.parseGetter(config, position, null, "StackBinder");
				tmpEnd = position.value - 1;
			}
			position.value = tmpEnd + 1;
		}
		StackBinder stackBind = new StackBinder();
		stackBind.methodName = mName;
		stackBind.targetIndex = tIndex;
		stackBind.objIndex = oIndex;
		stackBind.needName = needName;
		stackBind.attrGetter = attrGetter;
		stackBind.needGenerate = needGenerate;
		stackBind.execInBegin = execInBegin;
		return stackBind;
	}

	public boolean begin(Digester digester, Element element)
	{
		if (this.execInBegin)
		{
			this.doBind(digester, element);
		}
		return true;
	}

	public void end(Digester digester, Element element)
	{
		if (!this.execInBegin)
		{
			this.doBind(digester, element);
		}
	}

	private void doBind(Digester digester, Element element)
	{
		Object obj = digester.peek(this.objIndex);
		if (obj instanceof BeanMap)
		{
			obj = ((BeanMap) obj).getBean();
		}
		Object target = digester.peek(this.targetIndex);
		if (target instanceof BeanMap)
		{
			target = ((BeanMap) target).getBean();
		}
		this.bind(target, obj, element);
	}

	public void bind(Object target, Object obj, Element element)
	{
		Object[] args;
		if (obj instanceof Generator)
		{
			((Generator) obj).setFactory(ContainerManager.getCurrentFactory());
		}
		if (this.needName)
		{
			args = new Object[2];
			if (this.attrGetter == null)
			{
				Generator g = (Generator) obj;
				args[0] = g.getName();
			}
			else
			{
				args[0] = this.attrGetter.get(element);
			}
			args[1] = this.needGenerate ? ((Generator) obj).create() : obj;
		}
		else
		{
			args = new Object[1];
			args[0] = this.needGenerate ? ((Generator) obj).create() : obj;
		}
		try
		{
			Tool.invokeExactMethod(target, this.methodName, args);
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