
package self.micromagic.eterna.model;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.grammer.GrammerElement;
import self.micromagic.grammer.ParserData.GrammerCell;
import self.micromagic.util.Utility;

/**
 * 一个表达式对象.
 */
public class Expression
{
	/**
	 * 从词法节点中获取其定义的值.
	 */
	public static Object getValue(GrammerCell cell)
	{
		int type = cell.grammerElement.getType();
		if (type == GrammerElement.TYPE_FLOAT)
		{
			return new Double(cell.textBuf);
		}
		else if (type == GrammerElement.TYPE_INT)
		{
			return tryConvertInt(new Long(cell.textBuf));
		}
		else if (type == GrammerElement.TYPE_INT8)
		{
			return tryConvertInt(new Long(Long.parseLong(cell.textBuf.substring(1), 8)));
		}
		else if (type == GrammerElement.TYPE_INT16)
		{
			return tryConvertInt(new Long(Long.parseLong(cell.textBuf.substring(2), 16)));
		}
		else if (type == GrammerElement.TYPE_ESCAPE)
		{
			char c = cell.textBuf.charAt(1);
			switch (c)
			{
				case 'u':
					int v = Integer.parseInt(cell.textBuf.substring(2), 16);
					return String.valueOf((char) v);
				case 't':
					return "\t";
				case 'f':
					return "\f";
				case 'r':
					return "\r";
				case 'n':
					return "\n";
				case 'b':
					return "\b";
				case '\\':
					return "\\";
				case '\'':
					return "\'";
				case '\"':
					return "\"";
			}
			throw new EternaException("Error escape [" + cell.textBuf + "].");
		}
		else if (type == GrammerElement.TYPE_TEXT)
		{
			return cell.textBuf;
		}
		throw new EternaException("Error grammer type [" + type
				+ "], value [" + cell.textBuf + "].");
	}
	/**
	 * 尝试转换成整型.
	 */
	private static Object tryConvertInt(Long value)
	{
		long v = value.longValue();
		if (v <= Integer.MAX_VALUE && v >= Integer.MIN_VALUE)
		{
			return Utility.createInteger(value.intValue());
		}
		return value;
	}

}
