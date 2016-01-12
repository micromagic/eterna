
package self.micromagic.expression.opts;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import self.micromagic.cg.ClassGenerator;
import self.micromagic.eterna.dao.Entity;
import self.micromagic.expression.AbstractOneSpecial;
import self.micromagic.expression.SpecialOpt;
import self.micromagic.util.Utility;

/**
 * 获取对象的长度.
 */
public class GetLength extends AbstractOneSpecial
		implements SpecialOpt
{
	protected Object exec(Object obj, Object[] args)
	{
		if (obj instanceof Collection)
		{
			return Utility.createInteger(((Collection) obj).size());
		}
		if (obj instanceof CharSequence)
		{
			return Utility.createInteger(((CharSequence) obj).length());
		}
		if (obj instanceof Map)
		{
			return Utility.createInteger(((Map) obj).size());
		}
		if (obj instanceof Entity)
		{
			return Utility.createInteger(((Entity) obj).getItemCount());
		}
		Class type = obj.getClass();
		if (ClassGenerator.isArray(type))
		{
			if (type.getComponentType().isPrimitive())
			{
				return Utility.createInteger(Array.getLength(obj));
			}
			else
			{
				return Utility.createInteger(((Object[]) obj).length);
			}
		}
		return null;
	}

}
