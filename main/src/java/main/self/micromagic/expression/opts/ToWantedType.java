package self.micromagic.expression.opts;

import java.text.DateFormat;

import self.micromagic.expression.AbstractOneSpecial;
import self.micromagic.expression.ExprTool;
import self.micromagic.expression.SpecialCreater;
import self.micromagic.expression.SpecialOpt;
import self.micromagic.util.FormatTool;
import self.micromagic.util.converter.UtilDateConverter;
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
 * 转换成日期的操作创建者.
 */
class ToDataCreater extends AbstractOneSpecial
		implements SpecialCreater
{
	private static final String DEFAULT_PATTERN = "yyyy-MM-dd";

	protected UtilDateConverter converter;
	protected boolean constFormats = true;

	public ToDataCreater(Object[] args)
	{
		this.converter = new UtilDateConverter();
		this.converter.setNeedThrow(true);
		if (args == null || args.length < 2)
		{
			this.converter.setDateFormat((DateFormat) FormatTool.getCachedFormat(DEFAULT_PATTERN));
		}
		else
		{
			boolean allConst = true;
			DateFormat[] formats = new DateFormat[args.length - 1];
			for (int i = 1; i < args.length; i++)
			{
				if (ExprTool.isConstObject(args[i]))
				{
					args[i] = formats[i - 1] = trans2Format(args[i]);
				}
				else
				{
					allConst = false;
				}
			}
			if (allConst)
			{
				this.converter.setDateFormats(formats);
			}
			else
			{
				this.constFormats = false;
			}
		}
	}

	private static DateFormat trans2Format(Object obj)
	{
		if (obj instanceof DateFormat)
		{
			return (DateFormat) obj;
		}
		else
		{
			String pattern = obj == null ? DEFAULT_PATTERN : obj.toString();
			return (DateFormat) FormatTool.getCachedFormat(pattern);
		}
	}

	protected Object exec(Object obj, Object[] args)
	{
		if (this.constFormats)
		{
			return this.converter.convert(obj);
		}
		DateFormat[] formats = new DateFormat[args.length - 1];
		for (int i = 1; i < args.length; i++)
		{
			formats[i - 1] = trans2Format(args[i]);
		}
		return this.converter.convertToDate(obj, formats);
	}

	public SpecialOpt create(String name, Object[] args)
	{
		if (args == null || args.length < 2)
		{
			return this;
		}
		return new ToDataCreater(args);
	}

}