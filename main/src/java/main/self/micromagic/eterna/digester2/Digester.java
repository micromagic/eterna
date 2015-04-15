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
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import self.micromagic.eterna.digester2.dom.EternaDocumentFactory;
import self.micromagic.eterna.digester2.dom.EternaSAXReader;
import self.micromagic.eterna.share.Tool;
import self.micromagic.util.ResManager;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;
import self.micromagic.util.ref.StringRef;

/**
 * 文档的解析工具.
 */
public class Digester
{
	/**
	 * 在classes目录中存放扩展规则的文件名.
	 */
	public static final String RULES_EXT_FILE = "rules_ext.res";
	/**
	 * 在classes目录中存放自定义配置项的文件名.
	 */
	public static final String RULES_CINFIG_FILE = "rules_config.properties";

	/**
	 * 日志.
	 */
	static final Log log = Utility.createLog("eterna.digester2");

	/**
	 * 构造一个Digester.
	 *
	 * @param resManager  存放规则的资源管理器
	 * @param resConfig   规则定义中需要使用的配置参数
	 */
	public Digester(ResManager resManager, Map resConfig)
	{
		this.initRules(resManager, resConfig);
	}

	/**
	 * 无参的构造函数用于生成默认的Digester.
	 */
	private Digester()
	{
	}
	static Digester getInstance()
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
		InputStream in = Digester.class.getResourceAsStream("/" + RULES_CINFIG_FILE);
		if (in != null)
		{
			// 如果有自定义的配置, 也将其载入
			rConfig.load(in);
		}
		instance.initRules(rm, rConfig);
		in = Digester.class.getResourceAsStream("/" + RULES_EXT_FILE);
		if (in != null)
		{
			// 载入扩展规则.
			rm = new ResManager();
			rm.load(in);
			instance.initRules(rm, rConfig);
		}
	}
	static
	{
		try
		{
			initRules();
		}
		catch (Exception ex)
		{
			log.error("Init error.", ex);
		}
	}

	private void initRules(ResManager resManager, Map resConfig)
	{
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
						rClass = Tool.getClass(cName, Thread.currentThread().getContextClassLoader());
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
				this.willInitRules.put(rule,
						new StringRef(config.substring(index + 1)));
			}
			catch (Exception ex)
			{
				throw new ParseException(ex);
			}
		}

		// 在所有的规则都注册完后在执行规则的初始化
		int count = this.willInitRules.size();
		itr = this.willInitRules.entrySet().iterator();
		this.inInit = true;
		for (int i = 0; i < count; i++)
		{
			Map.Entry e = (Map.Entry) itr.next();
			StringRef config = (StringRef) e.getValue();
			String tConfig;
			if ((tConfig = config.getString()) != null)
			{
				config.setString(null);
				ParseRule rule = (ParseRule) e.getKey();
				rule.init(tConfig);
			}
		}
		this.willInitRules.clear();
		this.inInit = false;
	}

	private boolean inInit;
	/**
	 * 记录已执行过初始化的规则.
	 */
	private final Map willInitRules = new IdentityHashMap();

	/**
	 * 对一个xml数据流进行解析.
	 *
	 * @param in  xml数据流
	 * @throws DocumentException
	 */
	void parse(InputStream in)
			throws DocumentException
	{
		EternaSAXReader reader = new EternaSAXReader(new EternaDocumentFactory());
		Document doc = reader.read(in);
		this.parse(doc);
	}

	/**
	 * 对一个xml数据流进行解析.
	 *
	 * @param in  xml数据流
	 * @throws DocumentException
	 */
	void parse(Reader in)
			throws DocumentException
	{
		EternaSAXReader reader = new EternaSAXReader(new EternaDocumentFactory());
		Document doc = reader.read(in);
		this.parse(doc);
	}

	/**
	 * 对一个文档进行解析.
	 *
	 * @param doc   需要解析的文档
	 */
	public void parse(Document doc)
	{
		Element root = doc.getRootElement();
		String rName = root.getName();
		ParseRule r = this.getRule(rName);
		if (r == null)
		{
			throw new ParseException("Not found rule [" + rName + "].");
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
		if (this.ruleMap.put(name, rule) != null)
		{
			throw new ParseException("Same rule name [" + name + "].");
		}
	}

	/**
	 * 根据规则名称获取一个规则.
	 */
	public ParseRule getRule(String name)
	{
		ParseRule r = (ParseRule) this.ruleMap.get(name);
		if (this.inInit)
		{
			// 如果在初始化过程中获取规则, 需要判断是否已初始化
			StringRef config = (StringRef) this.willInitRules.get(r);
			String tConfig;
			if (config != null && (tConfig = config.getString()) != null)
			{
				config.setString(null);
				r.init(tConfig);
			}
		}
		return r;
	}
	private final Map ruleMap = new HashMap();

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
	private final List stack = new ArrayList();

}