
package self.micromagic.expression.impl;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.expression.ExpCreater;
import self.micromagic.expression.antlr.ExpTokenTypes;
import self.micromagic.util.Utility;
import antlr.collections.AST;

/**
 * 构造数值的表达式.
 */
public class NumValueCreater
		implements ExpCreater, ExpTokenTypes
{
	public Object create(AST node)
	{
		int type = node.getType();
		if (type == NUM_INT)
		{
			String text = node.getText();
			if (text.length() == 1)
			{
				char c = text.charAt(0);
				if (c >= '0' && c <= '9')
				{
					return Utility.createInteger(c - '0');
				}
			}
			return Integer.decode(text);
		}
		if (type == NUM_LONG)
		{
			String text = checkLast(node.getText(), 'L');
			return Long.decode(text);
		}
		if (type == NUM_DOUBLE)
		{
			String text = checkLast(node.getText(), 'D');
			return new Double(text);
		}
		if (type == NUM_FLOAT)
		{
			String text = checkLast(node.getText(), 'F');
			return new Float(text);
		}
		if (type == LITERAL_null)
		{
			return null;
		}
		if (type == LITERAL_true)
		{
			return Boolean.TRUE;
		}
		if (type == LITERAL_false)
		{
			return Boolean.FALSE;
		}
		throw new EternaException("Error numerical value node type [" + type + "].");
	}

	/**
	 * 检查并去除最后一个字符.
	 */
	private static String checkLast(String text, char lastChar)
	{
		char last = text.charAt(text.length() - 1);
		if (lastChar == Character.toUpperCase(last))
		{
			return text.substring(0, text.length() - 1);
		}
		return text;
	}

}
