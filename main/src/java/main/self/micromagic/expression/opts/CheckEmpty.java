
package self.micromagic.expression.opts;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import self.micromagic.cg.ClassGenerator;
import self.micromagic.eterna.dao.Entity;
import self.micromagic.expression.AbstractOneSpecial;
import self.micromagic.expression.SpecialOpt;

/**
 * 检查对象是否为null, 或空字符串/空数组等.
 */
public class CheckEmpty extends AbstractCheck
		implements SpecialOpt
{
	public CheckEmpty()
	{
		this.defaultValue = Boolean.TRUE;
	}

	protected Object exec(Object obj, Object[] args)
	{
		if (obj instanceof Collection)
		{
			return ((Collection) obj).isEmpty() ? Boolean.TRUE : Boolean.FALSE;
		}
		if (obj instanceof CharSequence)
		{
			return ((CharSequence) obj).length() == 0 ? Boolean.TRUE : Boolean.FALSE;
		}
		if (obj instanceof Map)
		{
			return ((Map) obj).isEmpty() ? Boolean.TRUE : Boolean.FALSE;
		}
		if (obj instanceof Iterator)
		{
			return ((Iterator) obj).hasNext() ? Boolean.FALSE : Boolean.TRUE;
		}
		if (obj instanceof Enumeration)
		{
			return ((Enumeration) obj).hasMoreElements() ? Boolean.FALSE : Boolean.TRUE;
		}
		if (obj instanceof Entity)
		{
			return ((Entity) obj).getItemCount() == 0 ? Boolean.TRUE : Boolean.FALSE;
		}
		Class type = obj.getClass();
		if (ClassGenerator.isArray(type))
		{
			if (type.getComponentType().isPrimitive())
			{
				return Array.getLength(obj) == 0 ? Boolean.TRUE : Boolean.FALSE;
			}
			else
			{
				return ((Object[]) obj).length == 0 ? Boolean.TRUE : Boolean.FALSE;
			}
		}
		return Boolean.FALSE;
	}

}

/**
 * 检查是否包含某个对象或集合.
 */
class CheckContains
		implements SpecialOpt
{
	private static IndexOf indexOf = new IndexOf();

	public Object exec(Object[] args)
	{
		if (args == null || args.length < 2 || args[0] == null)
		{
			return Boolean.FALSE;
		}
		if (args[0] instanceof CharSequence)
		{
			Integer i = (Integer) indexOf.exec(args);
			return i != null && i.intValue() != -1 ? Boolean.TRUE : Boolean.FALSE;
		}
		boolean result = false;
		if (args[0] instanceof Collection)
		{
			if (args[1] instanceof Collection)
			{
				result = ((Collection) args[0]).containsAll((Collection) args[1]);
			}
			else
			{
				result = ((Collection) args[0]).contains(args[1]);
			}
		}
		else if (args[0] instanceof Map)
		{
			if (args[1] instanceof Collection)
			{
				result = ((Map) args[0]).keySet().containsAll((Collection) args[1]);
			}
			else
			{
				result = ((Map) args[0]).containsKey(args[1]);
			}
		}
		return result ? Boolean.TRUE : Boolean.FALSE;
	}

	public boolean isStabile()
	{
		return true;
	}

}

/**
 * 判断迭代器是否有下一个.
 */
class HasNext extends AbstractOneSpecial
		implements SpecialOpt
{
	public HasNext()
	{
		this.defaultValue = Boolean.FALSE;
	}

	protected Object exec(Object obj, Object[] args)
	{
		if (obj instanceof Iterator)
		{
			return ((Iterator) obj).hasNext() ? Boolean.TRUE : Boolean.FALSE;
		}
		if (obj instanceof Enumeration)
		{
			return ((Enumeration) obj).hasMoreElements() ? Boolean.TRUE : Boolean.FALSE;
		}
		return Boolean.FALSE;
	}

}
