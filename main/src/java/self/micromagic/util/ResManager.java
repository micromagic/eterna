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

package self.micromagic.util;

import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import self.micromagic.util.container.UnmodifiableIterator;

/**
 * 资源数据管理者.
 */
public class ResManager
{
	private static final char SPECIAL_FLAG = '#';
	private static final int INDENT_SIZE = 3;
	private static final char[] INDENT_BUF = {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '};

	private String charset = "UTF-8";
	private boolean skipEmptyLine = true;
	private Map resCache = new HashMap();

	/**
	 * 对代码进行缩进处理.
	 */
	public static String indentCode(String code, int indent)
	{
		if (StringTool.isEmpty(code))
		{
			return "";
		}
		BufferedReader r = new BufferedReader(new StringReader(code));
		try
		{
			StringAppender buf = StringTool.createStringAppender(code.length() + 128);
			String line = r.readLine();
			int preIndent = indent, nowIndent = indent;
			int preBeginSpace = -1;
			boolean afterFirst = false;
			while (line != null)
			{
				if (afterFirst)
				{
					buf.appendln();
				}
				afterFirst = true;
				int[] arr = doIndentLine(nowIndent, line, buf, preIndent, preBeginSpace);
				preIndent = nowIndent;
				nowIndent = arr[0];
				if (arr[1] >= 0)
				{
					preBeginSpace = arr[1];
				}
				if (nowIndent < indent)
				{
					nowIndent = indent;
				}
				line = r.readLine();
		  }
			return buf.toString();
		}
		catch (IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	/**
	 * 处理1行的缩进.
	 * 返回两个数, 需要变更的缩进数 及 当前行的空格数
	 */
	private static int[] doIndentLine(int indent, String line, StringAppender buf,
			int preIndent, int preBeginSpace)
	{
		int index = -1;
		int count = line.length();
		int plusCount = 0;
		for (int i = 0; i < count; i++)
		{
			char c = line.charAt(i);
			if (c > ' ')
			{
				index = i;
				break;
			}
			else if (c == '\t')
			{
				// tab键算一个缩进距离-1, 因为本身已经有一个空格了
				plusCount += INDENT_SIZE - 1;
			}
		}
		// 没有找到非空格的其实字符, 作为空行处理
		if (index == -1)
		{
			return new int[]{indent, -1};
		}
		int beginSpace = index + plusCount;
		if (line.charAt(index) == '}' && indent > 0)
		{
			indent--;
		}
		if (preBeginSpace == -1 || preIndent != indent)
		{
			dealIndent(indent, buf);
			buf.append(line.substring(index));
		}
		else
		{
			int tmpI = (beginSpace - preBeginSpace) / INDENT_SIZE;
			tmpI = tmpI < 0 ? 0 : tmpI > 2 ? 2 : tmpI;
			dealIndent(indent + tmpI, buf);
			buf.append(line.substring(index));
		}
		if (getLastValidChar(line) == '{')
		{
			indent++;
		}
		return new int[]{indent, beginSpace};
	}

	/**
	 * 获得最后一个非空格字符, 如果都是空格则返回0
	 */
	private static char getLastValidChar(String line)
	{
		char c;
		for (int i = line.length() - 1; i >= 0; i--)
		{
			c = line.charAt(i);
			if (c > ' ')
			{
				return c;
			}
		}
		return (char) 0;
	}

	public synchronized void load(InputStream inStream)
			throws IOException
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(inStream, this.charset));
		Map initMap = new HashMap();
		String nowKey = null;
		int lineNum = 0;
		while (true)
		{
			lineNum++;
			// 获取下一行
			String line = in.readLine();
			if (line == null)
			{
				break;
			}

			if (line.length() > 0)
			{
				char firstChar = line.charAt(0);
				char secondChar = 0;
				if (line.length() > 1)
				{
					secondChar = line.charAt(1);
				}
				char thirdChar = 0;
				if (line.length() > 2)
				{
					thirdChar = line.charAt(2);
				}

				if (firstChar == SPECIAL_FLAG && secondChar != SPECIAL_FLAG)
				{
					// 一个“#”代表注释, 无需处理
				}
				else if (firstChar == SPECIAL_FLAG && secondChar == SPECIAL_FLAG
						&& thirdChar != SPECIAL_FLAG)
				{
					// 两个“#”开始代表资源的开始, 后面的文字去除控制字符后为资源的名称
					nowKey = line.substring(2).trim();
					List resList = new ArrayList();
					if (initMap.put(nowKey, resList) != null)
					{
						throw new IOException("Duplicate res name:" + nowKey + ".");
					}
				}
				else
				{
					// 其它情况为资源的文本
					if (nowKey == null)
					{
						throw new IOException("Hasn't res name at line:" + lineNum + ".");
					}
					if (firstChar == SPECIAL_FLAG && secondChar == SPECIAL_FLAG
							&& thirdChar == SPECIAL_FLAG)
					{
						// 连续3个“#”开始代表一个“#”
						line = line.substring(2);
					}
					List resList = (List) initMap.get(nowKey);
					resList.add(line);
				}
			}
			else if (!this.skipEmptyLine && nowKey != null)
			{
				// 如果不忽略空行, 且开始了资源文本, 则将空行作为资源文本的一部分
				List resList = (List) initMap.get(nowKey);
				resList.add(line);
			}
		}

		Iterator itr = initMap.entrySet().iterator();
		while (itr.hasNext())
		{
			Map.Entry entry = (Map.Entry) itr.next();
			this.resCache.put(entry.getKey(), transToArray((List) entry.getValue()));
		}
	}

	/**
	 * 将List类型的资源文本转换成字符串数组.
	 */
	private static String[] transToArray(List resList)
	{
		String[] arr = new String[resList.size()];
		return (String[]) resList.toArray(arr);
	}

	/**
	 * 输出资源的值.
	 *
	 * @param resName      需要输出的资源的名称
	 * @param paramBind    输出的资源需要绑定的参数
	 * @param indentCount  输出的资源每行需要缩进的值
	 * @param buf          用于输出资源的缓存
	 * @return  如果给出了buf参数, 则返回buf, 如果未给出则返回
	 *				新生成的<code>StringAppender</code>
	 */
	public StringAppender printRes(String resName, Map paramBind, int indentCount, StringAppender buf)
	{
		if (buf == null)
		{
			buf = StringTool.createStringAppender();
		}
		String[] resArr = (String[]) this.resCache.get(resName);
		for (int i = 0; i < resArr.length; i++)
		{
			if (i > 0)
			{
				buf.appendln();
			}
			String s = paramBind == null ?
					resArr[i] : Utility.resolveDynamicPropnames(resArr[i], paramBind, true);
			if (s.length() > 0)
			{
				dealIndent(indentCount, buf);
				buf.append(s);
			}
		}
		return buf;
	}

	/**
	 * 获取资源的值.
	 *
	 * @param resName      资源的名称
	 * @param paramBind    资源需要绑定的参数
	 * @param indentCount  资源每行需要缩进的值
	 */
	public String getRes(String resName, Map paramBind, int indentCount)
	{
		StringAppender buf = this.printRes(resName, paramBind, indentCount, null);
		return buf.toString();
	}

	/**
	 * 获取资源的值.
	 *
	 * @param resName  需要输出的资源的名称
	 */
	public String getRes(String resName)
	{
      StringAppender buf = this.printRes(resName, null, 0, null);
		return buf.toString();
	}

	/**
	 * 处理每行起始部分的缩进
	 */
	private static void dealIndent(int indentCount, StringAppender buf)
	{
		if (indentCount <= 0)
		{
			return;
		}
		int count = indentCount * INDENT_SIZE;
		while (count > INDENT_BUF.length)
		{
			buf.append(INDENT_BUF);
			count -= INDENT_BUF.length;
		}
		if (count > 0)
		{
			buf.append(INDENT_BUF, 0, count);
		}
	}

	/**
	 * 获取所有资源的名称.
	 */
	public Iterator getResNames()
	{
		return new UnmodifiableIterator(this.resCache.keySet().iterator());
	}

	/**
	 * 获取读取资源数据时使用的字符集.
	 */
	public String getCharset()
	{
		return this.charset;
	}

	/**
	 * 设置读取资源数据时使用的字符集.
	 */
	public void setCharset(String charset)
	{
		this.charset = charset;
	}

}