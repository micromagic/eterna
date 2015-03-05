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
import java.util.List;

import org.dom4j.Element;

import self.micromagic.cg.BeanMap;
import self.micromagic.eterna.share.Tool;
import self.micromagic.util.ref.IntegerRef;

/**
 * 通过方法调用的设置来绑定配置与对象的属性.
 */
public class MethodBinder
		implements ElementProcessor
{
	public static final String __FLAG = "method:";
	public static MethodBinder getInstance()
	{
		return instance;
	}
	private static MethodBinder instance = new MethodBinder();

	private String methodName;
	private AttrGetter[] attrs;

	public ElementProcessor parse(Digester digester, ParseRule rule,
			String config, IntegerRef position)
	{
		return parseConfig(config, position);
	}

	/**
	 * 解析配置信息, 生成MethodBinder.
	 * 格式: method:{mName,name2,$body}
	 */
	public static MethodBinder parseConfig(String config, IntegerRef position)
	{
		int mBegin = position.value += 1;
		int mEnd = ParseRule.findItemEnd(config, position);
		String mName = config.substring(mBegin, mEnd).trim();
		if (mName.length() == 0)
		{
			// 没有方法名不能解析为MethodBinder
			throw new ParseException("Error config [" + config + "] for MethodBinder.");
		}
		List attrs = new ArrayList();
		position.value = mEnd + 1;
		while (config.charAt(position.value - 1) != ParseRule.BLOCK_END)
		{
			AttrGetter ag = AttrBinder.parseGetter(config, position, null);
			attrs.add(ag);
		}
		MethodBinder methodBind = new MethodBinder();
		methodBind.methodName = mName;
		int size = attrs.size();
		methodBind.attrs = new AttrGetter[size];
		attrs.toArray(methodBind.attrs);
		return methodBind;
	}

	public boolean begin(Digester digester, Element element)
	{
		Object obj = digester.peek(0);
		if (obj instanceof BeanMap)
		{
			obj = ((BeanMap) obj).getBean();
		}
		this.bind(obj, element);
		return true;
	}

	public void end(Digester digester, Element element)
	{
	}

	public void bind(Object obj, Element element)
	{
		Object[] args = new Object[this.attrs.length];
		for (int i = 0; i < args.length; i++)
		{
			args[i] = this.attrs[i].get(element);
		}
		try
		{
			Tool.invokeExactMethod(obj, this.methodName, args);
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