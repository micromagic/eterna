package self.micromagic.expression.opts;

import self.micromagic.expression.AbstractOneSpecial;
import self.micromagic.expression.SpecialOpt;
import self.micromagic.util.converter.BooleanConverter;
import self.micromagic.util.converter.StringConverter;
import self.micromagic.util.converter.ValueConverter;

/**
 * 转换成指定类型的操作.
 */
public class ToWantedType extends AbstractOneSpecial
		implements SpecialOpt
{
	protected ValueConverter converter;

	public ToWantedType(ValueConverter converter)
	{
		converter.setNeedThrow(true);
		this.converter = converter;
	}

	protected Object exec(Object obj, Object[] args)
	{
		return this.converter.convert(obj);
	}

}

/**
 * 将对象转换成字符串.
 */
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
