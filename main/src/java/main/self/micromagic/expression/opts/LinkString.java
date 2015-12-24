
package self.micromagic.expression.opts;

import java.lang.reflect.Array;

import self.micromagic.cg.ClassGenerator;
import self.micromagic.expression.SpecialOpt;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;

/**
 * 对象转字符串并连接.
 */
public class LinkString
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
		this.appendArray(args, buf);
		return buf.toString();
	}

	private void appendArray(Object[] args, StringAppender buf)
	{
		for (int i = 0; i < args.length; i++)
		{
			Object obj = args[i];
			if (obj != null)
			{
				Class type = obj.getClass();
				if (ClassGenerator.isArray(type))
				{
					if (type.getComponentType().isPrimitive())
					{
						int len = Array.getLength(obj);
						for (int j = 0; j < len; j++)
						{
							buf.append(Array.get(obj, j));
						}
					}
					else
					{
						this.appendArray((Object[]) obj, buf);
					}
					continue;
				}
			}
			buf.append(obj);
		}
	}

	public boolean isStabile()
	{
		return true;
	}

}
