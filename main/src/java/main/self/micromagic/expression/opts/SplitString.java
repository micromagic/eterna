
package self.micromagic.expression.opts;

import self.micromagic.expression.AbstractOneSpecial;
import self.micromagic.expression.SpecialOpt;
import self.micromagic.util.StringTool;
import self.micromagic.util.converter.BooleanConverter;
import self.micromagic.util.converter.IntegerConverter;
import self.micromagic.util.converter.StringConverter;

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

class CharAt
		implements SpecialOpt
{
	public Object exec(Object[] args)
	{
		if (args == null || args.length < 2)
		{
			return null;
		}
		Object obj = args[0];
		if (obj instanceof CharSequence)
		{
			int index = IntegerConverter.toInt(args[1]);
			return new Character(((CharSequence) obj).charAt(index));
		}
		return null;
	}

	public boolean isStabile()
	{
		return true;
	}

}

class ToString extends AbstractOneSpecial
		implements SpecialOpt
{
	private final StringConverter converter = new StringConverter();

	protected Object exec(Object obj, Object[] args)
	{
		boolean null2Empty = true;
		if (args.length > 1)
		{
			null2Empty = BooleanConverter.toBoolean(args[1]);
		}
		if (obj == null)
		{
			return null2Empty ? "" : null;
		}
		return this.converter.convert(obj);
	}

}
