
package self.micromagic.expression.opts;

import java.text.Format;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.expression.ExprTool;
import self.micromagic.expression.SpecialCreater;
import self.micromagic.expression.SpecialOpt;
import self.micromagic.util.FormatTool;

/**
 * 格式化数据的操作创建者.
 */
public class FormatValueCreater
		implements SpecialCreater, SpecialOpt
{
	protected Format format;

	FormatValueCreater()
	{
	}

	public FormatValueCreater(Object[] args)
	{
		if (args == null || args.length < 2)
		{
			throw new EternaException("Format opt need 2 param.");
		}
		else
		{
			if (ExprTool.isConstObject(args[1]))
			{
				this.format = trans2Format(args[1]);
			}
		}
	}

	public Object exec(Object[] args)
	{
		if (args == null || args.length < 2 || args[0] == null)
		{
			return null;
		}
		Format f = this.format;
		if (f == null)
		{
			f = trans2Format(args[1]);
		}
		return f.format(args[0]);
	}

	public SpecialOpt create(String name, Object[] args)
	{
		return new FormatValueCreater(args);
	}

	public boolean isStabile()
	{
		return true;
	}

	private static Format trans2Format(Object obj)
	{
		if (obj instanceof Format)
		{
			return (Format) obj;
		}
		else if (obj == null)
		{
			throw new NullPointerException("The format or pattern is null.");
		}
		return FormatTool.getCachedFormat(obj.toString());
	}

}
