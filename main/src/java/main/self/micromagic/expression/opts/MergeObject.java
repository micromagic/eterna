
package self.micromagic.expression.opts;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import self.micromagic.cg.ArrayTool;
import self.micromagic.cg.ClassGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.expression.SpecialOpt;
import self.micromagic.util.converter.ConverterFinder;
import self.micromagic.util.converter.ValueConverter;

/**
 * 合并两个对象.
 */
public class MergeObject
		implements SpecialOpt
{
	private static ValueConverter mapConverter;

	static
	{
		ValueConverter converter = ConverterFinder.findConverter(Map.class, true);
		converter.setNeedThrow(false);
		mapConverter = converter;
	}

	public Object exec(Object[] args)
	{
		if (args == null || args.length == 0)
		{
			return null;
		}
		Object obj = args[0];
		if (obj == null || args.length <= 1)
		{
			return obj;
		}
		if (obj instanceof Collection)
		{
			Collection collection = (Collection) obj;
			if (args.length <= 2)
			{
				appendValue(collection, args[1]);
			}
			else
			{
				// 如果有多个参数, 以非集合的方式添加
				for (int i = 1; i < args.length; i++)
				{
					collection.add(args[i]);
				}
			}
		}
		else if (obj instanceof Map)
		{
			Map map = (Map) obj;
			if (args.length <= 2)
			{
				if (args[1] instanceof Map)
				{
					map.putAll((Map) args[1]);
				}
				else if (args[1] != null)
				{
					Object srcMap = mapConverter.convert(args[1]);
					if (srcMap != null)
					{
						map.putAll((Map) srcMap);
					}
				}
			}
			else if ((args.length & 0x1) != 1)
			{
				throw new EternaException("The map merge value count must be even.");
			}
			else
			{
				// 如果有多个参数, 以key-value的方式添加
				for (int i = 1; i < args.length; i += 2)
				{
					Object key = args[i];
					map.put(key == null ? null : key.toString(), args[i + 1]);
				}
			}
		}
		return obj;
	}

	public boolean isStabile()
	{
		return true;
	}

	/**
	 * 向集合中添加对象.
	 *
	 * @return  被添加的对象是否为集合.
	 */
	private static boolean appendValue(Collection collection, Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		if (obj instanceof Collection)
		{
			collection.addAll((Collection) obj);
			return true;
		}
		Class type = obj.getClass();
		if (ClassGenerator.isArray(type))
		{
			if (type.getComponentType().isPrimitive())
			{
				obj = ArrayTool.wrapPrimitiveArray(1, obj);
			}
			collection.add(Arrays.asList((Object[]) obj));
			return true;
		}
		collection.add(obj);
		return false;
	}

}
