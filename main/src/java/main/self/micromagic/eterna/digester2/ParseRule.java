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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.util.StringTool;
import self.micromagic.util.converter.BooleanConverter;
import self.micromagic.util.ref.IntegerRef;

/**
 * 对文档的一个解析规则.
 */
public class ParseRule
{
	public ParseRule(Digester digester)
	{
		this.digester = digester;
	}
	protected Digester digester;

	/**
	 * 获取解析规则所在的解析器.
	 */
	public Digester getDigester()
	{
		return this.digester;
	}

	/**
	 * 对规则进行初始化.
	 */
	public void init(String config)
	{
		List epList = new ArrayList(6);
		IntegerRef position = new IntegerRef(0);
		int begin;
		while ((begin = findBeginFlag(config, position)) != -1)
		{
			String flag = config.substring(position.value, begin).trim();
			ElementProcessor ep = this.findProcessor(flag);
			if (ep == null)
			{
				throw new ParseException("Error flag [" + flag + "] in config:" + config);
			}
			position.value = begin;
			epList.add(ep.parse(this.digester, this, config, position));
		}
		this.epList = new ElementProcessor[epList.size()];
		epList.toArray(this.epList);
	}
	private ElementProcessor[] epList;

	/**
	 * 根据给出的标签查找对应的元素节点处理器.
	 */
	protected ElementProcessor findProcessor(String flag)
	{
		return (ElementProcessor) epCache.get(flag);
	}

	/**
	 * 判断当前节点是否与规则匹配.
	 */
	public boolean match(Element element)
	{
		//String msg = ((EternaElement) element).getLineNumber() + ":" + element.getName() + ","
		//		+ element.getNamespacePrefix() + ", " + this.pattern;
		//System.out.println(msg);
		if (element.getName().equals(this.nodeName))
		{
			if (this.nsPrefix == null)
			{
				return true;
			}
			return this.nsPrefix.equals(element.getNamespacePrefix());
		}
		return false;
	}

	/**
	 * 获取当前规则的匹配模式.
	 */
	public String getPattern()
	{
		return this.pattern;
	}

	/**
	 * 设置当前规则的匹配模式.
	 */
	public void setPattern(String pattern)
	{
		this.nodeName = this.pattern = pattern;
		if (pattern != null)
		{
			int index = pattern.indexOf(':');
			if (index != -1)
			{
				this.nodeName = pattern.substring(index + 1);
				this.nsPrefix = pattern.substring(0, index);
				if ("*".equals(this.nsPrefix))
				{
					// "*"表示匹配任意命名空间.
					this.nsPrefix = null;
				}
			}
		}
	}
	private String pattern;
	private String nodeName;
	private String nsPrefix = "";

	/**
	 * 执行解析规则.
	 *
	 * @return  是否需要继续执行后面的规则
	 */
	public boolean doRule(Element element)
	{
		boolean result = true;
		int index = 0;
		for (; index < this.epList.length; index++)
		{
			if (!this.epList[index].begin(this.digester, element))
			{
				// 遇到返回值为false中断执行
				result = false;
				index++; // 这里是跳出循环, 为了后面处理的一致性将值增1
				break;
			}
		}
		for (index--; index >= 0; index--)
		{
			// 对执行过begin的处理执行end
			this.epList[index].end(this.digester, element);
		}
		return result;
	}

	/**
	 * 解析配置中的参数信息.
	 * 格式: (m=mustExists)
	 */
	public static Map parseParam(String config, IntegerRef position)
	{
		int count = config.length();
		int paramBegin = -1;
		int beginPos = position.value;
		for (int i = beginPos; i < count; i++)
		{
			if (config.charAt(i) == '(')
			{
				paramBegin = i + 1;
				break;
			}
			else if (config.charAt(i) > ' ')
			{
				break;
			}
		}
		if (paramBegin != -1)
		{
			int paramEnd = config.indexOf(')', paramBegin);
			if (paramEnd == -1)
			{
				throw new ParseException("Error param [" + config + "] start:" + beginPos + ".");
			}
			position.value = paramEnd + 1;
			return StringTool.string2Map(config.substring(paramBegin, paramEnd),
					",", '=', true, false, null, null);
		}
		return null;
	}

	/**
	 * 寻找起始标记.
	 *
	 * @throws ParseException  起始标记未找到
	 */
	public static int findBeginFlag(String config, IntegerRef position)
	{
		int count = config.length();
		int begin = position.value;
		for (int i = begin; i < count; i++)
		{
			char c = config.charAt(i);
			if (c == BLOCK_BEGIN)
			{
				return i;
			}
		}
		if (config.substring(begin).trim().length() == 0)
		{
			return -1;
		}
		throw new ParseException("Error config [" + config + "].");
	}

	/**
	 * 寻找元素结束标记.
	 *
	 * @throws ParseException  元素结束标记未找到
	 */
	public static int findItemEnd(String config, IntegerRef position)
	{
		int count = config.length();
		for (int i = position.value; i < count; i++)
		{
			char c = config.charAt(i);
			for (int j = 0; j < endFlags.length; j++)
			{
				if (c == endFlags[j])
				{
					return i;
				}
			}
		}
		throw new ParseException("Error config [" + config + "].");
	}

	/**
	 * 单元块的起始标记.
	 */
	public static final char BLOCK_BEGIN = '{';
	/**
	 * 单元块的结束标记.
	 */
	public static final char BLOCK_END = '}';

	private static char[] endFlags = {',', BLOCK_END};

	static BooleanConverter booleanConverter = new BooleanConverter();

	/**
	 * 注册一个元素节点的处理器.
	 *
	 * @param flag  元素节点的处理器标识
	 * @param ep    元素节点的处理器对象, 用于解析配置信息
	 */
	public static void registerElementProcessor(String flag, ElementProcessor ep)
	{
		synchronized (epCache)
		{
			if (epCache.containsKey(flag))
			{
				throw new EternaException("ElementProcessor [" + flag + "] has registered.");
			}
			epCache.put(flag, ep);
		}
	}
	private static Map epCache = new HashMap();

	static
	{
		// 注册现有的元素节点的处理器
		registerElementProcessor(ObjectCreater.__FLAG, ObjectCreater.getInstance());
		registerElementProcessor(AttrBinder.__FLAG, AttrBinder.getInstance());
		registerElementProcessor(MethodBinder.__FLAG, MethodBinder.getInstance());
		registerElementProcessor(MapBinder.__FLAG, MapBinder.getInstance());
		registerElementProcessor(StackBinder.__FLAG, StackBinder.getInstance());
		registerElementProcessor(SubRuleCaller.__FLAG, SubRuleCaller.getInstance());
		registerElementProcessor(SameCheck.__FLAG, SameCheck.getInstance());
		registerElementProcessor(NameLog.__FLAG, NameLog.getInstance());
		registerElementProcessor(InjectSub.__FLAG, InjectSub.getInstance());
	}

}