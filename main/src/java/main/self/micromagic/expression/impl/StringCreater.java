
package self.micromagic.expression.impl;

import self.micromagic.expression.ExprCreater;
import self.micromagic.expression.antlr.ExprTokenTypes;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.ref.IntegerRef;
import antlr.collections.AST;

/**
 * 构造字符串的表达式.
 */
public class StringCreater
		implements ExprCreater, ExprTokenTypes
{
	public Object create(AST node)
	{
		String text = node.getText();
		int count = text.length() - 2;
		text = text.substring(1, count + 1);
		int index = text.indexOf('\\');
		if (index != -1)
		{
			StringAppender buf = StringTool.createStringAppender(count);
			buf.append(text.substring(0, index));
			for (int i = index; i < count; i++)
			{
				char c = text.charAt(i);
				if (c == '\\')
				{
					char next = text.charAt(++i);
					switch (next)
					{
						case 'u':
							int v = Integer.parseInt(text.substring(i + 1, i + 5), 16);
							// for循环中还会+1, 所以这里需要-1
							i += 5 - 1;
							buf.append((char) v);
							break;
						case 't':
							buf.append('\t');
							break;
						case 'f':
							buf.append('\f');
							break;
						case 'r':
							buf.append('\r');
							break;
						case 'n':
							buf.append('\n');
							break;
						case 'b':
							buf.append('\b');
							break;
						case '\\':
							buf.append('\\');
							break;
						case '\'':
							buf.append('\'');
							break;
						case '\"':
							buf.append('\"');
							break;
						default:
							IntegerRef begin = new IntegerRef(i);
							buf.append(numToChar(text, begin));
							// for循环中还会+1, 所以这里需要-1
							i = begin.value - 1;
					}
				}
				else
				{
					buf.append(c);
				}
			}
			text = buf.toString();
		}
		if (node.getType() == CHAR_LITERAL)
		{
			return new Character(text.charAt(0));
		}
		return text;
	}

	/**
	 * 将"\数字"转换为字符.
	 */
	private static char numToChar(String text, IntegerRef begin)
	{
		char first = text.charAt(begin.value);
		char[] cArr = new char[3];
		cArr[0] = first;
		int leftCount = 1;
		if (first <= '3' && first >= '0')
		{
			leftCount = 2;
		}
		int index = 1;
		while (leftCount > 0)
		{
			if (begin.value + index == text.length())
			{
				break;
			}
			char tmp = text.charAt(begin.value + index);
			if (tmp <= '7' && tmp >= 0)
			{
				cArr[index++] = tmp;
				leftCount--;
			}
			else
			{
				break;
			}
		}
		begin.value += index;
		int v = Integer.parseInt(new String(cArr, 0, index), 8);
		return (char) v;
	}

}
