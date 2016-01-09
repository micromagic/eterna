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

package self.micromagic.eterna.view.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.Tool;
import self.micromagic.eterna.view.Component;
import self.micromagic.eterna.view.Function;
import self.micromagic.eterna.view.ModifiableViewRes;
import self.micromagic.eterna.view.Replacement;
import self.micromagic.eterna.view.StringCoder;
import self.micromagic.eterna.view.View;
import self.micromagic.grammer.GrammerElement;
import self.micromagic.grammer.GrammerManager;
import self.micromagic.grammer.ParserData;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;

/**
 * 视图模块中需要用到的一些公共方法.
 *
 * @author micromagic@sina.com
 */
public class ViewTool
{
	public static final Log log = Tool.log;

	public static final int GRAMMER_TYPE_NONE = 0;
	public static final int GRAMMER_TYPE_JSON = 1;
	public static final int GRAMMER_TYPE_EXPRESSION = 2;

	private static final String[] PLUS_NAMES = {
		"ef", "$E.F", "eternaFunction", "$E.F",
		"typical", "$E.T", "res", "$E.R",
		"data", "$E.D", "dataV", "$E.D", "tmpData", "D", "tmpDataV", "D",
		"efV", "$E.F", "typicalV", "$E.T", "resV", "$E.R",
		"global", "$E.G", "globalV", "$E.G", "caption", "$"
	};
	private static final int PLUS_GRAMMER_CELL_COUNT = 5;

	public static final String TYPICAL_NAME = "$typical";
	public static final String TYPICAL_SAME_AS_NAME = "$sameAsName";

	private static GrammerManager grammerManager;

	private static volatile int eternaId = 1;

	/**
	 * 配置中设置初始化时是否需要对界面脚本语言进行语法检查的键值.
	 */
	public static final String CHECK_GRAMMER_FLAG = "eterna.view.checkGrammer";
	private static boolean checkGrammer = true;

	static
	{
		try
		{
			grammerManager = new GrammerManager();
			grammerManager.init(ViewTool.class.getClassLoader().getResource(
					"self/micromagic/eterna/view/grammer.xml").openStream());
			Utility.addFieldPropertyManager(CHECK_GRAMMER_FLAG, ViewTool.class, "checkGrammer");
		}
		catch (Exception ex)
		{
			log.error("Error in init ViewTool.", ex);
		}
	}

	/**
	 * 初始化时是否需要对界面脚本语言进行语法检查.
	 */
	public static boolean isCheckGrammer()
	{
		return checkGrammer;
	}

	public static synchronized int createEternaId()
	{
		return 0xffffff & eternaId++;
	}

	public static String addParentScript(String script, String parentScript)
	{
		if (script == null)
		{
			return parentScript;
		}
		if (parentScript == null)
		{
			parentScript = "";
		}
		int index = script.indexOf(Replacement.PARENT_SCRIPT);
		if (index == -1)
		{
			return script;
		}
		StringAppender buf = StringTool.createStringAppender(script.length() + 64);
		while (index != -1)
		{
			buf.append(script.substring(0, index));
			buf.append(parentScript);
			script = script.substring(index + Replacement.PARENT_SCRIPT.length());
			index = script.indexOf(Replacement.PARENT_SCRIPT);
		}
		buf.append(script);
		// 这里的返回内容不用做任何处理, 因为在后面的dealScriptPart方法中还会做处理
		return buf.toString();
	}

	public static String createTypicalComponentName(AppData data, Component com)
			throws EternaException
	{
		boolean hasOld = false;
		// 先从typical列表和used列表中查找对应名称的控件
		Component oldCom = queryTypicalComponent(data, com.getName());
		if (oldCom == com)
		{
			return com.getName();
		}
		else if (oldCom != null)
		{
			hasOld = true;
		}
		// 如果存在同名的控件, 则放入列表的名称要加上后缀
		String idName;
		if (hasOld)
		{
			idName = com.getName() + "_T" + Integer.toString(System.identityHashCode(com), 32);
			Component tmpCom = queryTypicalComponent(data, idName);
			int idIndex = 0;
			String tmpName = idName;
			while (tmpCom != com)
			{
				if (tmpCom == null)
				{
					data.addSpcialData(View.TYPICAL_COMPONENTS_MAP, idName, com);
					break;
				}
				// 某些存储地址比较大的情况下, identityHashCode可能会重复, 所以再添加顺序编号
				idIndex++;
				idName = tmpName + "_" + idIndex;
				tmpCom = queryTypicalComponent(data, idName);
			}
		}
		else
		{
			idName = com.getName();
			data.addSpcialData(View.TYPICAL_COMPONENTS_MAP, idName, com);
		}
		return idName;
	}

	private static Component queryTypicalComponent(AppData data, String name)
	{
		Component tmp = (Component) data.getSpcialData(View.TYPICAL_COMPONENTS_MAP, name);
		if (tmp == null)
		{
			tmp = (Component) data.getSpcialData(View.USED_TYPICAL_COMPONENTS, name);
		}
		return tmp;
	}

	/**
	 * 添加一个动态资源的名称
	 */
	public static void addDynamicResourceName(String name)
	{
		if (name != null)
		{
			AppData data = AppData.getCurrentData();
			Set resourceNames = (Set) data.getSpcialData(View.VIEW_CACHE, View.DYNAMIC_RESOURCE_NAMES);
			if (resourceNames == null)
			{
				resourceNames = new HashSet();
				data.addSpcialData(View.VIEW_CACHE, View.DYNAMIC_RESOURCE_NAMES, resourceNames);
			}
		}
	}

	/**
	 * 添加一组动态方法.
	 */
	public static void addDynamicFunction(Map fnMap)
	{
		if (fnMap == null)
		{
			return;
		}
		if (fnMap.size() > 0)
		{
			AppData data = AppData.getCurrentData();
			Map functions = (Map) data.getSpcialData(View.VIEW_CACHE, View.DYNAMIC_FUNCTIONS);
			if (functions == null)
			{
				functions = new HashMap();
				data.addSpcialData(View.VIEW_CACHE, View.DYNAMIC_FUNCTIONS, functions);
			}
			putAllFunction(functions, fnMap);
		}
	}

	/**
	 * 向方法的map中添加一组方法.
	 */
	public static void putAllFunction(Map functionMap, Map putMap)
	{
		if (putMap != null)
		{
			Iterator entrys = putMap.entrySet().iterator();
			while (entrys.hasNext())
			{
				Map.Entry entry = (Map.Entry) entrys.next();
				Function oldFn = (Function) functionMap.get(entry.getKey());
				if (oldFn != null && oldFn != entry.getValue())
				{
					log.error("Duplicate function name:[" + entry.getKey() + "] when add it.");
				}
				if (oldFn == null)
				{
					functionMap.put(entry.getKey(), entry.getValue());
				}
			}
		}
	}

	/**
	 * 解析资源文本.
	 */
	public static List parseResourceText(String text)
			throws EternaException
	{
		GrammerElement ge = grammerManager.getGrammerElement("resource_parser");
		ParserData pd = new ParserData(text);
		try
		{
			if (!ge.verify(pd))
			{
				throw new EternaException("Parse resource error:" + text
						+ "\n[maxBuf:" + pd.getMaxErrorBuffer() + "].");
			}
			return pd.getGrammerCellLst();
		}
		catch (Exception ex)
		{
			log.error("Error in parse resource.", ex);
			throw new EternaException("Parse resource error:" + text + "\n[msg:"
					+ ex.getMessage() + "].");
		}
	}

	/**
	 * 处理代码中的注释, 替换代码中的扩展标签等.
	 */
	public static String dealScriptPart(ModifiableViewRes viewRes, String script,
			int grammerType, EternaFactory factory)
			throws EternaException
	{
		if (script == null)
		{
			return null;
		}
		return StringTool.intern(checkGrammmer(viewRes, script, grammerType, factory), true);
	}

	private static String checkGrammmer(ModifiableViewRes viewRes, String script,
			int grammerType, EternaFactory factory)
			throws EternaException
	{
		GrammerElement ge;
		if (!checkGrammer || grammerType == GRAMMER_TYPE_NONE)
		{
			ge = grammerManager.getGrammerElement("expression_checker_onlyPlus");
		}
		else
		{
			ge = grammerManager.getGrammerElement(
					grammerType == GRAMMER_TYPE_JSON ? "json_part" : "expression_checker");
		}
		ParserData pd = new ParserData(script);
		try
		{
			if (!ge.verify(pd))
			{
				throw new EternaException("Grammer error:" + script
						+ "\n[maxBuf:" + pd.getMaxErrorBuffer() + "].");
			}
			StringAppender buf = StringTool.createStringAppender(script.length());
			parseGrammerCell(viewRes, pd.getGrammerCellLst(), buf, factory);
			if (log.isDebugEnabled())
			{
				if (buf.length() < script.length())
				{
					log.debug("buf:\n" + buf + "\n-----------------------\nscript:\n" + script);
				}
			}
			return buf.toString();
		}
		catch (Exception ex)
		{
			log.error("Error in check grammer.", ex);
			throw new EternaException("Grammer error:" + script + "\n[msg:" + ex.getMessage() + "].");
		}
	}

	private static void parseGrammerCell(ModifiableViewRes viewRes, List gclist,
			StringAppender buf, EternaFactory factory)
			throws EternaException
	{
		if (gclist == null)
		{
			return;
		}
		StringCoder stringCoder = factory.getStringCoder(StringCoder.DEFAULT_NAME);
		Iterator itr = gclist.iterator();
		while (itr.hasNext())
		{
			ParserData.GrammerCell cell = (ParserData.GrammerCell) itr.next();
			int type = cell.grammerElement.getType();
			if ("plus".equals(cell.grammerElement.getName()))
			{
				boolean validPlusName = false;
				ParserData.GrammerCell[] plusCells = new ParserData.GrammerCell[PLUS_GRAMMER_CELL_COUNT];
				cell.subCells.toArray(plusCells);
				for (int i = 0; i < PLUS_NAMES.length; i += 2)
				{
					String plusName = PLUS_NAMES[i];
					if (plusName.equals(plusCells[1].textBuf))
					{
						String tmpName = plusCells[3].textBuf;
						if (tmpName.length() > 0)
						{
							if (i < 4)
							{
								// 前两个表示使用的是静态方法调用, 需要注册此方法
								tmpName = viewRes.addFunction(factory.getFunction(tmpName));
							}
							else if (i < 6)
							{
								// 第三个表示typical控件, 要添加此名称
								viewRes.addTypicalComponentNames(tmpName);
							}
							else if (i < 8)
							{
								// 第四个表示resource, 要添加此名称
								viewRes.addResourceNames(tmpName);
							}
						}
						if ("$".equals(PLUS_NAMES[i + 1]))
						{
							if ("caption".equals(plusName))
							{
								buf.append('"');
								String caption = Tool.translateCaption(factory, tmpName);
								if (caption == null)
								{
									buf.append(stringCoder.toJsonString(tmpName));
								}
								else
								{
									buf.append(stringCoder.toJsonString(caption));
								}
								buf.append('"');
							}
						}
						else
						{
							buf.append(PLUS_NAMES[i + 1]);
							if (tmpName.length() > 0)
							{
								if (plusName.charAt(plusName.length() - 1) == 'V')
								{
									buf.append('[').append(tmpName).append(']');
								}
								else
								{
									buf.append(stringCoder.parseJsonRefName(tmpName));
								}
							}
						}
						validPlusName = true;
						break;
					}
				}
				if (validPlusName)
				{
					continue;
				}
				else
				{
					// 这段现在执行不到, 因为非法的名称不会解析为plus
					log.error("Invalid plus name:" + cell.textBuf);
				}
			}
			if (type == GrammerElement.TYPE_NOTE)
			{
				if (buf.length() > 0 && buf.charAt(buf.length() - 1) > ' ')
				{
					buf.append(' ');
				}
				continue;
			}
			if (cell.subCells != null)
			{
				parseGrammerCell(viewRes, cell.subCells, buf, factory);
			}
			else
			{
				buf.append(cell.textBuf);
			}
		}
	}


}