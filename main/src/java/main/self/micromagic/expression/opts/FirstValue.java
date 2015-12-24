
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
import self.micromagic.util.converter.IntegerConverter;

/**
 * 获取对象中的第一个.
 */
public class FirstValue extends AbstractOneSpecial
		implements SpecialOpt
{
	protected Object exec(Object obj, Object[] args)
	{
		if (obj instanceof Iterator)
		{
			return this.getNext((Iterator) obj);
		}
		if (obj instanceof Enumeration)
		{
			Enumeration e = (Enumeration) obj;
			return e.hasMoreElements() ? e.nextElement() : null;
		}
		if (obj instanceof Map)
		{
			int type = 0;
			if (args.length > 1)
			{
				type = IntegerConverter.toInt(args[1]);
			}
			if (type == 0)
			{
				return obj;
			}
			Map.Entry e = (Map.Entry) this.getNext(((Map) obj).entrySet().iterator());
			return type == 1 ? e.getKey() : type == 2 ? e.getValue() : e;
		}
		if (obj instanceof Collection)
		{
			return this.getNext(((Collection) obj).iterator());
		}
		if (obj instanceof Entity)
		{
			return this.getNext(((Entity) obj).getItemIterator());
		}
		if (obj instanceof CharSequence)
		{
			if (obj instanceof Character)
			{
				return obj;
			}
			if (args.length > 1 && IntegerConverter.toInt(args[1]) > 0)
			{
				CharSequence str = (CharSequence) obj;
				return str.length() > 0 ? new Character(str.charAt(0)) : null;
			}
		}
		Class type = obj.getClass();
		if (ClassGenerator.isArray(type))
		{
			if (type.getComponentType().isPrimitive())
			{
				if (Array.getLength(obj) > 0)
				{
					return Array.get(obj, 0);
				}
			}
			else
			{
				Object[] arr = (Object[]) obj;
				return arr.length > 0 ? arr[0] : null;
			}
		}
		// 不是array或list等, 返回原始值
		return obj;
	}

	/**
	 * 判断并获取迭代器中的下一个值.
	 */
	private Object getNext(Iterator itr)
	{
		return itr.hasNext() ? itr.next() : null;
	}

}
