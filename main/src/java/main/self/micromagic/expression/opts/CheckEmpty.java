
package self.micromagic.expression.opts;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import self.micromagic.cg.ClassGenerator;
import self.micromagic.eterna.dao.Entity;
import self.micromagic.expression.SpecialOpt;

/**
 * 获取对象的长度.
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
