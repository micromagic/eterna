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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * 多行的文本对象.
 *
 * @author micromagic@sina.com
 */
public class MultiLineText
{
	/**
	 * 清除文本起始部分和结束部分的空行.
	 */
	public static String skipEmptyEndsLine(String text)
	{
		if (StringTool.isEmpty(text))
		{
			return text;
		}
		int begin = 0;
		int end = text.length();
		// 去除起始部分的空行
		if (text.charAt(0) == '\n')
		{
			begin = 1;
		}
		else if (end > 1 && text.charAt(0) == '\r' && text.charAt(1) == '\n')
		{
			begin = 2;
		}
		// 去除结束部分的空行
		if (end > begin && text.charAt(end - 1) == '\n')
		{
			if (end > begin + 1 && text.charAt(end - 2) == '\r')
			{
				end -= 2;
			}
			else
			{
				end -= 1;
			}
		}
		return text.substring(begin, end);
	}

	private static final char NEW_LINE = '\n';

	private final List elements = new ArrayList();
	private int count = 0;
	private CharElement preElement = null;
	private String cacheStr = null;
	private String cacheTrim = null;

	/**
	 * 添加一段字符块.
	 *
	 * @param ch       字符块
	 * @param start    起始位置
	 * @param length   取的字符个数
	 * @return         返回本对象
	 */
	public MultiLineText append(char[] ch, int start, int length)
	{
		this.append(ch, start, length, true);
		return this;
	}

	/**
	 * 添加一段字符块.
	 *
	 * @param ch            字符块
	 * @param start         起始位置
	 * @param length        取的字符个数
	 * @param unbindLine    是否需要将字符块中的每一行拆分开来
	 * @return              返回本对象
	 */
	MultiLineText append(char[] ch, int start, int length, boolean unbindLine)
	{
		this.cacheStr = null;
		this.cacheTrim = null;
		CharElement temp;
		if (unbindLine)
		{
			int preStart = start;
			int end = start + length;
			for (int i = start; i < end; i++)
			{
				if (ch[i] == MultiLineText.NEW_LINE)
				{
					temp = new CharElement(ch, preStart, i - preStart + 1, this.preElement);
					this.elements.add(temp);
					this.preElement = temp;
					preStart = i + 1;
				}
			}
			if (preStart < end)
			{
				temp = new CharElement(ch, preStart, end - preStart, this.preElement);
				this.elements.add(temp);
				this.preElement = temp;
			}
		}
		else
		{
			this.elements.add(new CharElement(ch, start, length, null));
			this.preElement = null;
		}
		this.count += length;
		return this;
	}

	/**
	 * 将文本块转为字符串, 此方法不会去除每一行两边的空格.
	 */
	public String toString()
	{
		if (this.cacheStr == null)
		{
			StringAppender sb = StringTool.createStringAppender(this.count);
			int size = this.elements.size();
			Iterator itr = this.elements.iterator();
			for (int i = 0; i < size; i++)
			{
				((CharElement) itr.next()).appendTo(sb);
			}
			this.cacheStr = sb.toString();
		}
		return this.cacheStr;
	}

	/**
	 * 将文本块转为字符串, 此方法会去除每一行两边的空格.
	 * 但是最后一行右边的空格不会去除.
	 *
	 * @param noLine     是否将换行符替换为空格
	 */
	public String trimEveryLineSpace(boolean noLine)
	{
		if (this.cacheTrim == null || noLine)
		{
			int size = this.elements.size();
			Iterator itr = this.elements.iterator();
			int tempCount = 0;
			for (int i = 0; i < size; i++)
			{
				tempCount += ((CharElement) itr.next()).trimSpacelength();
			}
			StringAppender sb = StringTool.createStringAppender(tempCount);
			itr = this.elements.iterator();
			for (int i = 0; i < size; i++)
			{
				((CharElement) itr.next()).trimSpaceAppendTo(sb, noLine);
			}
			if (noLine)
			{
				return sb.toString();
			}
			else
			{
				this.cacheTrim = sb.toString();
			}
		}
		return this.cacheTrim;
	}

	/**
	 * 文本对象的字符元素
	 */
	private class CharElement
	{
		public final int length;

		private final CharElement preElement;
		private char[] ch;
		private int leftTrimCount = 0;
		private int rightTrimCount = 0;
		private boolean endNewLine = true;

		public CharElement(char[] ch, int start, int length, CharElement preElement)
		{
			this.preElement = preElement;
			this.length = length;
			this.parse(ch, start, length);
			if (this.leftTrimCount + this.rightTrimCount == length)
			{
				this.ch = null;
				// 如果是以新行结束, 则需要检查前一个, 添加rightTrim数
				if (this.endNewLine && this.preElement != null)
				{
					this.rightTrimCount = length;
					this.leftTrimCount = 0;
					this.preElement.addRightTrimCount();
				}
			}
			else
			{
				this.ch = new char[length];
				System.arraycopy(ch, start, this.ch, 0, length);
			}
		}

		private void parse(char[] ch, int start, int length)
		{
			if (this.preElement == null || this.preElement.endNewLine || this.preElement.ch == null)
			{
				// 没有前一个元素, 前一个元素是以新行结束, 前一个元素全为空格
				// 才处理leftTrim数
				int leftTrimCount = 0;
				for (; leftTrimCount < length - 1 && ch[leftTrimCount + start] <= ' '; leftTrimCount++);
				if (leftTrimCount == length - 1)
				{
					char c = ch[start + length - 1];
					if (c != MultiLineText.NEW_LINE && c <= ' ')
					{
						leftTrimCount++;
					}
				}
				this.leftTrimCount = leftTrimCount;
			}
			if (ch[start + length - 1] == MultiLineText.NEW_LINE)
			{
				int rightTrimCount = 0;
				int count = length - this.leftTrimCount;
				for (; rightTrimCount < count && ch[start + length - rightTrimCount - 1] <= ' '; rightTrimCount++);
				this.rightTrimCount = rightTrimCount;
			}
			else
			{
				this.endNewLine = false;
			}
		}

		private void addRightTrimCount()
		{
			if (this.endNewLine)
			{
				// 如果是以新行结束, 就不需要处理rightTrim数了
				return;
			}
			if (this.ch == null || this.allBlank())
			{
				this.ch = null;
				this.rightTrimCount = this.leftTrimCount;
				this.leftTrimCount = 0;
				if (this.preElement != null)
				{
					this.preElement.addRightTrimCount();
				}
				return;
			}
			int rightTrimCount = 0;
			int count = this.length - this.leftTrimCount;
			for (; rightTrimCount < count && this.ch[this.length - rightTrimCount - 1] <= ' '; rightTrimCount++);
			this.rightTrimCount = rightTrimCount;
		}

		private boolean allBlank()
		{
			for (int i = 0; i < this.ch.length; i++)
			{
				if (this.ch[i] > ' ') return false;
			}
			return true;
		}

		public void appendTo(StringAppender sb)
		{
			if (this.ch != null)
			{
				sb.append(this.ch);
				return;
			}
			for (int i = 0; i < this.length - 1; i++)
			{
				sb.append(' ');
			}
			sb.append(this.endNewLine ? MultiLineText.NEW_LINE : ' ');
		}

		public void trimSpaceAppendTo(StringAppender sb, boolean noLine)
		{
			if (this.ch == null)
			{
				if (this.endNewLine)
				{
					sb.append(noLine ? ' ' : MultiLineText.NEW_LINE);
				}
				return;
			}
			sb.append(this.ch, this.leftTrimCount,
						 this.ch.length - this.leftTrimCount - this.rightTrimCount);
			if (this.endNewLine)
			{
				sb.append(noLine ? ' ' : MultiLineText.NEW_LINE);
			}
		}

		public int trimSpacelength()
		{
			return this.length - this.leftTrimCount - this.rightTrimCount
					+ (this.endNewLine ? 1 : 0);
		}

	}

}