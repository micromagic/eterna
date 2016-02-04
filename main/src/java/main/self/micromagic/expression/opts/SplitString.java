
package self.micromagic.expression.opts;

import java.util.List;

import self.micromagic.expression.AbstractOneSpecial;
import self.micromagic.expression.SpecialOpt;
import self.micromagic.util.StringTool;
import self.micromagic.util.Utility;
import self.micromagic.util.converter.BooleanConverter;
import self.micromagic.util.converter.IntegerConverter;

/**
 * 分隔字符串.
 */
public class SplitString extends AbstractOneSpecial
		implements SpecialOpt
{
	public SplitString()
	{
		this.defaultValue = StringTool.EMPTY_STRING_ARRAY;
	}

	protected Object exec(Object obj, Object[] args)
	{
		String str = obj.toString();
		if (args.length > 1)
		{
			String delimiter = args[1] == null ? "," : args[1].toString();
			boolean trim = true;
			if (args.length > 2)
			{
				trim = BooleanConverter.toBoolean(args[2]);
			}
			return StringTool.separateString(str, delimiter, trim);
		}
		return StringTool.separateString(str, ",", true);
	}

}

/**
 * 在字符串或列表中查找指定字符或对象的索引值.
 */
class IndexOf
		implements SpecialOpt
{
	public Object exec(Object[] args)
	{
		if (args == null || args.length < 2 || args[0] == null)
		{
			return null;
		}
		if (args[0] instanceof List)
		{
			List list = (List) args[0];
			boolean last = false;
			if (args.length > 2)
			{
				last = BooleanConverter.toBoolean(args[2]);
			}
			return Utility.createInteger(last ? list.lastIndexOf(args[1]) : list.indexOf(args[1]));
		}
		if (StringTool.isEmpty(args[1]))
		{
			return Utility.INTEGER_0;
		}
		String str = args[0].toString();
		String check = args[1].toString();
		int begin = 0;
		boolean last = false;
		if (args.length > 2)
		{
			if (args[2] instanceof Boolean)
			{
				last = ((Boolean) args[2]).booleanValue();
				begin = last ? str.length() : 0;
			}
			else
			{
				begin = IntegerConverter.toInt(args[2]);
				if (args.length > 3)
				{
					last = BooleanConverter.toBoolean(args[3]);
				}
			}
		}
		int result = check.length() != 1 ? (last ? str.lastIndexOf(check, begin) : str.indexOf(check, begin))
				: (last ? str.lastIndexOf(check.charAt(0), begin) : str.indexOf(check.charAt(0), begin));
		return Utility.createInteger(result);
	}

	public boolean isStabile()
	{
		return true;
	}

}

class CharAt
		implements SpecialOpt
{
	public Object exec(Object[] args)
	{
		if (args == null || args.length < 2 || args[0] == null)
		{
			return null;
		}
		CharSequence str = args[0] instanceof CharSequence ? (CharSequence) args[0] : args[0].toString();
		int index = IntegerConverter.toInt(args[1]);
		return new Character(str.charAt(index));
	}

	public boolean isStabile()
	{
		return true;
	}

}
