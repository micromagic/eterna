
package self.micromagic.expression.opts;

import java.lang.reflect.Array;
import java.util.List;

import self.micromagic.cg.ClassGenerator;
import self.micromagic.expression.SpecialOpt;
import self.micromagic.util.converter.IntegerConverter;

/**
 * 取子对象.
 */
public class SubObject
		implements SpecialOpt
{
	public Object exec(Object[] args)
	{
		if (args == null || args.length == 0)
		{
			return null;
		}
		Object obj = args[0];
		int begin = 0;
		int end = -1;
		if (args.length > 2)
		{
			end = IntegerConverter.toInt(args[2]);
		}
		if (args.length > 1)
		{
			begin = IntegerConverter.toInt(args[1]);
		}
		if (begin == 0 && end == -1)
		{
			return obj;
		}
		if (obj instanceof CharSequence)
		{
			CharSequence str = (CharSequence) obj;
			if (end == -1)
			{
				end = str.length();
			}
			return str.subSequence(begin, end);
		}
		if (obj instanceof List)
		{
			List list = (List) obj;
			if (end == -1)
			{
				end = list.size();
			}
			return list.subList(begin, end);
		}
		Class type = obj.getClass();
		if (ClassGenerator.isArray(type))
		{
			if (end == -1)
			{
				end = Array.getLength(obj);
			}
			Object newArr = Array.newInstance(type.getComponentType(), end - begin);
			System.arraycopy(obj, begin, newArr, 0, end - begin);
			return newArr;
		}
		return null;
	}

	public boolean isStabile()
	{
		return true;
	}

}
