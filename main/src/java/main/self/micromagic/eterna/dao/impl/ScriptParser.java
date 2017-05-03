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

package self.micromagic.eterna.dao.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import self.micromagic.dbvm.DataBaseLocker;
import self.micromagic.eterna.dao.Dao;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.ref.IntegerRef;
import self.micromagic.util.ref.ObjectRef;

/**
 * 数据操作脚本的解析器.
 */
public class ScriptParser
{
	/**
	 * 节点类型为一个操作符.
	 */
	public static final int ELEMENT_GROUP = 1000;

	/**
	 * 节点类型为一个操作符.
	 */
	public static final int BASE_TYPE_OPERATOR = 1;
	/**
	 * 节点类型为一个控制标识符.
	 */
	public static final int BASE_TYPE_FLAG = 2;
	/**
	 * 节点类型为一个转义操作符.
	 */
	public static final int BASE_TYPE_ESCAPE = 3;
	/**
	 * 节点类型为一个参数符.
	 */
	public static final int BASE_TYPE_PARAM = 5;
	/**
	 * 节点类型为一个关键字.
	 */
	public static final int BASE_TYPE_KEY = 31;
	/**
	 * 节点类型为一个次要关键字.
	 */
	public static final int BASE_TYPE_KEY_MINOR = 32;
	/**
	 * 节点类型为一个字符串.
	 */
	public static final int BASE_TYPE_STRING = 21;
	/**
	 * 节点类型为一个数字.
	 */
	public static final int BASE_TYPE_NUMBER = 22;
	/**
	 * 节点类型为一个名称.
	 */
	public static final int BASE_TYPE_NAME = 10;
	/**
	 * 节点类型为一个带有引号的名称.
	 */
	public static final int BASE_TYPE_NAME_QUOT = 11;


	/**
	 * 引号.
	 */
	public static final String QUOTE = "\"";

	/**
	 * 引号, 字符型.
	 */
	public static final char QUOTE_CHAR = '\"';

	/**
	 * 字符串的标识符.
	 */
	public static final char STRING_FLAG = '\'';

	/**
	 * 对一个脚本进行解析并返回词法节点列表.
	 *
	 * @param script  需要解析的脚本
	 * @param level   需要解析的等级
	 */
	public Element[] parseScript(String script, int level)
	{
		List r = this.parseScript0(script);
		if (level > 0)
		{
			r = this.parseElement1(r);
		}
		if (level > 1)
		{
			r = this.parseElement2(r);
		}
		int count = r.size();
		return (Element[]) r.toArray(new Element[count]);
	}

	/**
	 * 解析一个脚本并返回词法节点列表.
	 * 即前置解析.
	 */
	List parseScript0(String script)
	{
		List r = new ArrayList();
		int count = script.length();
		boolean notMatch = false;
		int begin = 0;
		int i = 0;
		while (i < count)
		{
			char c = script.charAt(i);
			if (c <= ' ' || operators.get(c))
			{
				if (notMatch)
				{
					r.add(new ParserBaseElement(begin, script.substring(begin, i)));
					notMatch = false;
				}
				r.add(new ParserBaseElement(i, script.substring(i, i + 1)));
				begin = ++i;
				continue;
			}
			notMatch = true;
			i++;
		}
		if (notMatch)
		{
			r.add(new ParserBaseElement(begin, script.substring(begin)));
		}
		return r;
	}

	/**
	 * 对每个词法节点进行第二轮解析.
	 * 处理操作符, 分组等.
	 */
	List parseElement2(List elements)
	{
		return elements;
	}

	private static final int P1_NORMAL_MODE = 0;
	private static final int P1_STRING_MODE = 1;
	private static final int P1_NAME_MODE = 2;

	/**
	 * 对每个词法节点进行第一轮解析.
	 * 去除空白字符, 合并字符串, 添加类型等.
	 */
	List parseElement1(List elements)
	{
		List r = new ArrayList();
		IntegerRef mode = new IntegerRef(P1_NORMAL_MODE);
		IntegerRef begin = new IntegerRef();
		Iterator itr = elements.iterator();
		ObjectRef buf = new ObjectRef();
		while (itr.hasNext())
		{
			ParserBaseElement e = (ParserBaseElement) itr.next();
			this.parseElement1_0(e, itr, mode, begin, buf, r);
		}
		return r;
	}

	/**
	 * 解析单个词法节点.
	 * 第一轮.
	 */
	private void parseElement1_0(ParserBaseElement element, Iterator elements,
			IntegerRef mode, IntegerRef begin, ObjectRef buf, List result)
	{
		ParserBaseElement next = null;
		if (mode.value == P1_STRING_MODE)
		{
			StringAppender tmpBuf = (StringAppender) buf.getObject();
			tmpBuf.append(element.text);
			if (isStringFlag(element))
			{
				if (elements.hasNext())
				{
					next = (ParserBaseElement) elements.next();
					if (isStringFlag(next))
					{
						// 连续两个字符串标识, 作为转义符
						tmpBuf.append(next.text);
						return;
					}
				}
				ParserBaseElement e = new ParserBaseElement(begin.value, tmpBuf.toString());
				e.type = BASE_TYPE_STRING;
				result.add(e);
				buf.setObject(null);
				mode.value = P1_NORMAL_MODE;
			}
		}
		else if (mode.value == P1_NAME_MODE)
		{
			StringAppender tmpBuf = (StringAppender) buf.getObject();
			tmpBuf.append(element.text);
			if (isNameFlag(element))
			{
				ParserBaseElement e = new ParserBaseElement(begin.value, tmpBuf.toString());
				e.type = BASE_TYPE_NAME_QUOT;
				result.add(e);
				buf.setObject(null);
				mode.value = P1_NORMAL_MODE;
			}
		}
		else if (!emptyElement(element))
		{
			int c = checkOperatorFlag(element);
			if (c != -1)
			{
				begin.value = element.begin;
				if (c == STRING_FLAG)
				{
					mode.value = P1_STRING_MODE;
					StringAppender tmpBuf = StringTool.createStringAppender(32);
					tmpBuf.append(element.text);
					buf.setObject(tmpBuf);
				}
				else if (c == QUOTE_CHAR)
				{
					mode.value = P1_NAME_MODE;
					StringAppender tmpBuf = StringTool.createStringAppender(32);
					tmpBuf.append(element.text);
					buf.setObject(tmpBuf);
				}
				else
				{
					if (c == '?')
					{
						element.type = BASE_TYPE_PARAM;
					}
					else if (c == '#')
					{
						element.type = BASE_TYPE_OPERATOR;
						if (elements.hasNext())
						{
							next = (ParserBaseElement) elements.next();
							int tmpChar = checkOperatorFlag(next);
							if (tmpChar != -1)
							{
								if (tmpChar != STRING_FLAG && tmpChar != QUOTE_CHAR)
								{
									// 可作为转义符
									element.type = BASE_TYPE_ESCAPE;
									element.text = element.text.concat(next.text);
									next = null;
								}
							}
							else if (!emptyElement(next))
							{
								String tmpTxt = next.text.toUpperCase();
								if (!isNumber(tmpTxt))
								{
									element.type = BASE_TYPE_FLAG;
									// 控制标识不转大写
									element.text = element.text.concat(next.text);
									next = null;
								}
							}
						}
					}
					else if (c == '.')
					{
						element.type = BASE_TYPE_OPERATOR;
						if (elements.hasNext())
						{
							next = (ParserBaseElement) elements.next();
							String tmpTxt = next.text.toUpperCase();
							if (isNumber(tmpTxt))
							{
								element.type = BASE_TYPE_NUMBER;
								element.text = element.text.concat(tmpTxt);
								next = null;
							}
						}
					}
					else
					{
						element.type = BASE_TYPE_OPERATOR;
					}
					result.add(element);
				}
			}
			else
			{
				String tmp = element.text.toUpperCase();
				Boolean flag = (Boolean) keys.get(tmp);
				if (flag == null)
				{
					if (isNumber(tmp))
					{
						if (elements.hasNext())
						{
							next = (ParserBaseElement) elements.next();
							if (checkOperatorFlag(next) == '.')
							{
								tmp = tmp.concat(next.text);
								if (elements.hasNext())
								{
									next = (ParserBaseElement) elements.next();
									String tmpTxt = next.text.toUpperCase();
									if (isNumber(tmpTxt))
									{
										tmp = tmp.concat(tmpTxt);
										next = null;
									}
								}
								else
								{
									next = null;
								}
							}
						}
						element.type = BASE_TYPE_NUMBER;
					}
					else
					{
						element.type = BASE_TYPE_NAME;
					}
				}
				else
				{
					element.type = flag.booleanValue() ?
							BASE_TYPE_KEY : BASE_TYPE_KEY_MINOR;
				}
				element.text = tmp;
				result.add(element);
			}
		}
		if (next != null)
		{
			// 有预读的节点, 需要对其进行处理
			this.parseElement1_0(next, elements, mode, begin, buf, result);
		}
	}

	/**
	 * 检查给出的文本是否可作为一个数字.
	 * 文本必须都转为大写.
	 */
	private static boolean isNumber(String text)
	{
		int count = text.length();
		boolean hasX = false;
		for (int i = 0; i < count; i++)
		{
			char c = text.charAt(i);
			if (c < '0' || c > '9')
			{
				if (c == 'E')
				{
					continue;
				}
				if ((c == 'X' && i == 1))
				{
					hasX = true;
					continue;
				}
				if (hasX && c >= 'A' && c <= 'F')
				{
					continue;
				}
				return false;
			}
		}
		return true;
	}

	/**
	 * 检测是否为操作标识节点.
	 * 如果是则返回操作字符, 否则返回-1.
	 */
	private static int checkOperatorFlag(ParserBaseElement e)
	{
		if (e.text.length() == 1)
		{
			char c = e.text.charAt(0);
			if (operators.get(c))
			{
				return c;
			}
		}
		return -1;
	}

	/**
	 * 检测是否为字符串标识节点.
	 */
	private static boolean isStringFlag(ParserBaseElement e)
	{
		return e.text.length() == 1 && e.text.charAt(0) == STRING_FLAG;
	}

	/**
	 * 检测是否为名称标识节点.
	 */
	private static boolean isNameFlag(ParserBaseElement e)
	{
		return e.text.length() == 1 && e.text.charAt(0) == QUOTE_CHAR;
	}

	/**
	 * 检测是否为空白字符节点.
	 */
	private static boolean emptyElement(ParserBaseElement e)
	{
		return e.text.length() == 1 && e.text.charAt(0) <= ' ';
	}

	/**
	 * 检查名称, 如果需要添加引号则添上.
	 */
	public static String checkNameForQuote(String name)
	{
		return checkNeedQuote(name) ? QUOTE.concat(name).concat(QUOTE) : name;
	}

	/**
	 * 检查名称是否需要添加引号.
	 */
	public static boolean checkNeedQuote(String name)
	{
		if (StringTool.isEmpty(name))
		{
			return false;
		}
		int len = name.length();
		for (int i = 0; i < len; i++)
		{
			char c = name.charAt(i);
			if (c == QUOTE_CHAR)
			{
				// 包含引号的名称不能再添加引号
				return false;
			}
			if (c != '_' && (c < 'A' || c > 'Z') && (c < 'a' || c > 'z'))
			{
				if (i == 0 || (c < '0' || c > '9'))
				{
					return true;
				}
			}
		}
		if (len <= MAX_MAIN_KEY_LENGTH && keys.get(name.toUpperCase()) == Boolean.TRUE)
		{
			// 如果是主要关键字, 需要添加引号
			return true;
		}
		return false;
	}

	/**
	 * 检查名称, 如果是主要关键字, 则添加引号.
	 */
	public static String checkNameWithKey(String name)
	{
		return isKey(name) == Boolean.TRUE ? QUOTE.concat(name).concat(QUOTE) : name;
	}

	/**
	 * 判断所给出的名称是否为关键字.
	 *
	 * @return TRUE 为主要关键字, FALSE 为次要关键字, null 不是关键字
	 */
	public static Boolean isKey(String name)
	{
		if (StringTool.isEmpty(name) || name.startsWith(QUOTE))
		{
			// 为空或以引号开始的不会是关键字
			return null;
		}
		return (Boolean) keys.get(name.toUpperCase());
	}

	/**
	 * 检查语句中的名称标识符, 替换成数据库相关的名称标识符.
	 */
	public static String checkScriptNameQuote(Connection conn, String script)
			throws SQLException
	{
		if (conn == null || StringTool.isEmpty(script))
		{
			return script;
		}
		String dbName = DataBaseLocker.getDataBaseProductName(conn);
		char[] nameQuote = (char[]) nameQuoteIndex.get(dbName);
		if (nameQuote == null || nameQuote[0] == QUOTE_CHAR)
		{
			return script;
		}
		return checkScriptNameQuote0(nameQuote, script);
	}
	static String checkScriptNameQuote0(char[] nameQuote, String script)
	{
		int nameIndex = script.indexOf(QUOTE_CHAR);
		if (nameIndex == -1)
		{
			return script;
		}
		// 处理数据库相关的名称标识符
		int strIndex = script.indexOf(STRING_FLAG);
		if (strIndex == -1 && nameQuote[0] == nameQuote[1])
		{
			return script.replace(QUOTE_CHAR, nameQuote[0]);
		}
		int count = script.length();
		StringAppender buf = null;
		if (strIndex == -1)
		{
			strIndex = count;
		}
		int baseIndex = 0;
		while (strIndex < count || nameIndex < count)
		{
			if (strIndex < nameIndex)
			{
				// 字符串标识在引号前, 需要判断名称标识符是否在字符串内
				int next = getStringFlagEnd(script, strIndex + 1, count);
				if (next == -1)
				{
					// 字符串标识有问题, 不做处理
					return script;
				}
				// 查找下一个字符串标识符
				strIndex = script.indexOf(STRING_FLAG, next + 1);
				if (strIndex == -1)
				{
					strIndex = count;
				}
				if (next > nameIndex)
				{
					// 如果字符串标识结束位置大于名称标识符, 说明这个名称标识符在字符串内, 重新查找下一个名称标识符
					nameIndex = script.indexOf(QUOTE_CHAR, next + 1);
					if (nameIndex == -1)
					{
						nameIndex = count;
					}
				}
			}
			else
			{
				if (buf == null)
				{
					buf = StringTool.createStringAppender(script.length());
				}
				buf.append(script.substring(baseIndex, nameIndex)).append(nameQuote[0]);
				int next = script.indexOf(QUOTE_CHAR, nameIndex + 1);
				if (next == -1)
				{
					// 名称标识有问题, 不做处理
					return script;
				}
				buf.append(script.substring(nameIndex + 1, next)).append(nameQuote[1]);
				baseIndex = next + 1;
				// 查找下一个名称标识符
				nameIndex = script.indexOf(QUOTE_CHAR, next + 1);
				if (nameIndex == -1)
				{
					nameIndex = count;
				}
				if (next > strIndex)
				{
					// 如果名称标识结束位置大于字符串标识符, 说明这个字符串标识符在名称内, 重新查找下一个字符串标识符
					strIndex = script.indexOf(STRING_FLAG, next + 1);
					if (strIndex == -1)
					{
						strIndex = count;
					}
				}
			}
		}
		return buf == null ? script
				: buf.append(script.substring(baseIndex, count)).toString();
	}

	/**
	 * 获取字符串标识的结束位置.
	 */
	private static int getStringFlagEnd(String script, int fromInde, int count)
	{
		int next = script.indexOf(STRING_FLAG, fromInde);
		while (next != -1)
		{
			if (next < count - 1 && script.charAt(next + 1) == STRING_FLAG)
			{
				// 连续两个字符串标识符是转义, 继续查找后面的
				next = script.indexOf(STRING_FLAG, next + 2);
			}
			else
			{
				return next;
			}
		}
		return next;
	}

	/**
	 * 名称引用符号的索引表.
	 */
	private static Map nameQuoteIndex = new HashMap();

	/**
	 * 操作字符集合.
	 */
	private static BitSet operators = new BitSet();

	/**
	 * 关键字集合.
	 */
	private static Map keys = new HashMap();

	/**
	 * 主要关键字的最大长度.
	 */
	private static int MAX_MAIN_KEY_LENGTH = 8;

	static
	{
		operators.set('?');
		operators.set(',');
		operators.set('(');
		operators.set(')');
		operators.set('[');
		operators.set(']');
		operators.set(STRING_FLAG);
		operators.set(QUOTE_CHAR);
		operators.set('.');
		operators.set('=');
		operators.set('<');
		operators.set('>');
		operators.set('!');
		operators.set('*');
		operators.set('/');
		operators.set('|');
		operators.set('+');
		operators.set('-');
		operators.set('#');

		char[] defaultNameQuote = new char[] {QUOTE_CHAR, QUOTE_CHAR};
		nameQuoteIndex.put(DataBaseLocker.DB_NAME_ORACLE, defaultNameQuote);
		nameQuoteIndex.put(DataBaseLocker.DB_NAME_H2, defaultNameQuote);
		nameQuoteIndex.put(DataBaseLocker.DB_NAME_MYSQL, new char[] {'`', '`'});
		nameQuoteIndex.put(DataBaseLocker.DB_NAME_PGSQL, defaultNameQuote);

		try
		{
			String tmpStr = StringTool.toString(
					Dao.class.getResourceAsStream("script_key.res"), "UTF-8");
			String[] arr = StringTool.separateString(tmpStr, "\n", true);
			for (int i = 0; i < arr.length; i++)
			{
				if (!StringTool.isEmpty(arr[i]))
				{
					if (arr[i].charAt(0) == '*')
					{
						keys.put(arr[i].substring(1), Boolean.TRUE);
						int len = arr[i].length() - 1;
						if (len > MAX_MAIN_KEY_LENGTH)
						{
							MAX_MAIN_KEY_LENGTH = len;
						}
					}
					else
					{
						keys.put(arr[i], Boolean.FALSE);
					}
				}
			}
		}
		catch (Exception ex)
		{
			DaoManager.log.error("Error in init script key.", ex);
		}
	}

	/**
	 * 解析后的节点.
	 */
	public interface Element
	{
		/**
		 * 获取节点的类型.
		 */
		public int getType();

		/**
		 * 获取节点的文本.
		 */
		public String getText();

	}

}

/**
 * 解析后的基本词法节点.
 */
class ParserBaseElement
		implements ScriptParser.Element
{
	ParserBaseElement(int begin, String text)
	{
		this.begin = begin;
		this.text = text;
	}
	/**
	 * 词法节点的起始位置.
	 */
	int begin;
	/**
	 * 词法节点的类型.
	 */
	int type;
	/**
	 * 词法节点的文本.
	 */
	String text;
	/**
	 * 是否为解析完成的词法节点.
	 */
	boolean finish;

	public int getType()
	{
		return this.type;
	}

	public String getText()
	{
		return this.text;
	}

	public String toString()
	{
		return this.finish ? this.text : this.begin + ":" + this.type + ":" + this.text;
	}

}

/**
 * 解析后的聚合节点.
 */
class ParserGroupElement
{

}
