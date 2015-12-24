
package self.micromagic.expression.opts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import self.micromagic.cg.ArrayTool;
import self.micromagic.cg.ClassGenerator;
import self.micromagic.eterna.dao.Entity;
import self.micromagic.expression.AbstractOneSpecial;
import self.micromagic.expression.SpecialOpt;
import self.micromagic.util.converter.BooleanConverter;

/**
 * 转换成迭代器.
 */
public class ToIterator extends AbstractOneSpecial
		implements SpecialOpt
{
	protected Object exec(Object obj, Object[] args)
	{
		if (obj instanceof Iterator || obj instanceof Enumeration)
		{
			return obj;
		}
		if (obj instanceof Map)
		{
			if (args.length > 1 && BooleanConverter.toBoolean(args[1]))
			{
				// 仅仅获取map的名称集合转为迭代
				return ((Map) obj).keySet().iterator();
			}
			return ((Map) obj).entrySet().iterator();
		}
		if (obj instanceof Collection)
		{
			return ((Collection) obj).iterator();
		}
		if (obj instanceof Entity)
		{
			return ((Entity) obj).getItemIterator();
		}
		Class type = obj.getClass();
		if (ClassGenerator.isArray(type))
		{
			if (type.getComponentType().isPrimitive())
			{
				obj = ArrayTool.wrapPrimitiveArray(1, obj);
			}
			return Arrays.asList((Object[]) obj).iterator();
		}
		return null;
	}

}

/**
 * 转换成集合.
 */
class ToCollection extends AbstractOneSpecial
		implements SpecialOpt
{
	private final boolean setType;

	public ToCollection(boolean setType)
	{
		this.setType = setType;
	}

	protected Object exec(Object obj, Object[] args)
	{
		if (this.setType)
		{
			if (obj instanceof Set)
			{
				return obj;
			}
		}
		else
		{
			if (obj instanceof List)
			{
				return obj;
			}
		}
		if (obj instanceof Map)
		{
			if (this.setType)
			{
				return ((Map) obj).keySet();
			}
			else
			{
				return new ArrayList(((Map) obj).values());
			}
		}
		if (obj instanceof Collection)
		{
			if (this.setType)
			{
				return new HashSet((Collection) obj);
			}
			else
			{
				return new ArrayList((Collection) obj);
			}
		}
		Class type = obj.getClass();
		if (ClassGenerator.isArray(type))
		{
			if (type.getComponentType().isPrimitive())
			{
				obj = ArrayTool.wrapPrimitiveArray(1, obj);
			}
			if (this.setType)
			{
				return new HashSet(Arrays.asList((Object[]) obj));
			}
			else
			{
				return new ArrayList(Arrays.asList((Object[]) obj));
			}
		}
		return null;
	}

}
