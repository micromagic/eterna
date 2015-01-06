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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.lang.reflect.Constructor;

import org.dom4j.Element;
import org.dom4j.Document;
import self.micromagic.util.ResManager;
import self.micromagic.util.StringTool;

/**
 * 文档的解析工具.
 */
public class Digester
{
	public static Digester getInstance()
	{
		return instance;
	}
	private static Digester instance = new Digester();
	private static void initRules()
			throws IOException
	{
		ResManager rm = new ResManager();
		rm.load(Digester.class.getResourceAsStream("rules.res"));
		Properties rConfig = new Properties();
		rConfig.load(Digester.class.getResourceAsStream("config.properties"));
		instance.initRules(rm, rConfig);
	}
	static
	{
		try
		{
			initRules();
		}
		catch (Exception ex)
		{
			ContainerManager.log.error("Init error.", ex);
		}
	}

	private void initRules(ResManager resManager, Map resConfig)
	{
		ArrayList tmpRuleAndConfig = new ArrayList();
		Class[] cParamTypes = new Class[]{Digester.class};
		Object[] cParams = new Object[]{this};
		Class dRuleClass = ParseRule.class;
		Iterator itr = resManager.getResNames();
		while (itr.hasNext())
		{
			String name = (String) itr.next();
			String config = resManager.getRes(name, resConfig, 0);
			int index = config.indexOf('\n');
			if (index == -1)
			{
				throw new ParseException("Error rule [" + name + "], config [" + config + "].");
			}
			String firstLine = config.substring(0, index).trim();
         int endFlag = firstLine.indexOf(';');
			Class rClass;
			String pattern;
			if (endFlag == -1)
			{
				rClass = dRuleClass;
				pattern = firstLine;
			}
			else
			{
				pattern = firstLine.substring(0, endFlag).trim();
				String cName = firstLine.substring(endFlag + 1).trim();
				if (!StringTool.isEmpty(cName))
				{
					try
					{
						rClass = ParseRule.getClass(cName, Thread.currentThread().getContextClassLoader());
					}
					catch (ClassNotFoundException ex)
					{
						throw new ParseException(ex);
					}
				}
				else
				{
					rClass = dRuleClass;
				}
			}
			try
			{
				Constructor constructor = rClass.getConstructor(cParamTypes);
				ParseRule rule = (ParseRule) constructor.newInstance(cParams);
				rule.setPattern(pattern);
				this.addRule(name, rule);
				tmpRuleAndConfig.add(rule);
				tmpRuleAndConfig.add(config.substring(index + 1));
			}
			catch (Exception ex)
			{
				throw new ParseException(ex);
			}
		}

		// 在所有的规则都注册完后在执行规则的初始化
		int count = tmpRuleAndConfig.size();
		for (int i = 0; i < count; i += 2)
		{
			ParseRule rule = (ParseRule) tmpRuleAndConfig.get(i);
			String config = (String) tmpRuleAndConfig.get(i + 1);
			rule.init(config);
		}
	}

	/**
	 * 对一个文档进行解析.
	 *
	 * @param doc   需要解析的文档
	 * @param rule  进行解析的根规则
	 */
	public void parse(Document doc, String rule)
	{
		ParseRule r = this.getRule(rule);
		if (r == null)
		{
			throw new ParseException("Not found rule [" + rule + "].");
		}
		r.doRule(doc.getRootElement());
	}

	/**
	 * 顺序执行一组解析规则.
	 *
	 * @param rules  需要执行的解析规则列表.
	 */
	public void doRules(Element element, ParseRule[] rules)
	{
      for (int i = 0; i < rules.length; i++)
		{
			if (rules[i].match(element))
			{
				if (!rules[i].doRule(element))
				{
					// 遇到返回值为false的终止后面的执行
					return;
				}
			}
		}
	}

	/**
	 * 添加一个规则.
	 */
	private void addRule(String name, ParseRule rule)
	{
      if (this.roleMap.put(name, rule) != null)
		{
			throw new ParseException("Same rule name [" + name + "].");
		}
	}

	/**
	 * 根据规则名称获取一个规则.
	 */
	public ParseRule getRule(String name)
	{
		return (ParseRule) this.roleMap.get(name);
	}
	private Map roleMap = new HashMap();

	/**
	 * 获取解析配置中的属性.
	 */
	public String getProperty(String name)
	{
		return null;
	}

	/**
	 * 向堆栈中压入一个对象.
	 */
	public void push(Object obj)
	{
		this.stack.add(obj);
	}
	/**
	 * 根据索引, 从堆栈中获取一个对象.
	 */
	public Object peek(int index)
	{
		if (index < this.stack.size())
		{
			return this.stack.get(this.stack.size() - 1 - index);
		}
		return null;
	}
	/**
	 * 从堆栈中弹出一个对象.
	 */
	public Object pop()
	{
		if (this.stack.size() > 0)
		{
			return this.stack.remove(this.stack.size() - 1);
		}
		return null;
	}
	private List stack = new ArrayList();

}