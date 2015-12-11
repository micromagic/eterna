
package self.micromagic.expression.opts;

import self.micromagic.expression.SpecialOpt;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;

/**
 * 对象转字符串并连接.
 */
public class LinkOpt
		implements SpecialOpt
{
	public Object exec(Object[] args)
	{
		if (args == null || args.length == 0)
		{
			return "";
		}
		int count = args.length * 16;
		StringAppender buf = StringTool.createStringAppender(count);
		for (int i = 0; i < args.length; i++)
		{
			buf.append(args[i]);
		}
		return buf.toString();
	}

	public boolean isStabile()
	{
		return true;
	}

}
