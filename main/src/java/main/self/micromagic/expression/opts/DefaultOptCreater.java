
package self.micromagic.expression.opts;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import self.micromagic.cg.ClassGenerator;
import self.micromagic.expression.AbstractOneSpecial;
import self.micromagic.expression.SpecialCreater;
import self.micromagic.expression.SpecialOpt;
import self.micromagic.util.converter.DoubleConverter;
import self.micromagic.util.converter.IntegerConverter;
import self.micromagic.util.converter.LongConverter;
import self.micromagic.util.converter.UtilDateConverter;

/**
 * 默认的特殊操作对象创建者.
 */
public class DefaultOptCreater
		implements SpecialCreater
{
	public SpecialOpt create(String name, Object[] args)
	{
		Object r = optCache.get(name);
		if (r instanceof SpecialCreater)
		{
			return ((SpecialCreater) r).create(name, args);
		}
		return (SpecialOpt) r;
	}

	private static Map optCache = new HashMap();
	static
	{
		optCache.put("link", new LinkString());
		optCache.put("split", new SplitString());
		optCache.put("charAt", new CharAt());
		optCache.put("length", new GetLength());

		optCache.put("hasNext", new HasNext());
		optCache.put("next", new NextValue());
		optCache.put("first", new FirstValue());
		optCache.put("entryKey", new EntryValue(true));
		optCache.put("entryValue", new EntryValue(false));
		optCache.put("merge", new MergeObject());
		optCache.put("format", new FormatValueCreater());

		optCache.put("toIterator", new ToIterator());
		optCache.put("toInt", new ToWantedType(new IntegerConverter()));
		optCache.put("toLong", new ToWantedType(new LongConverter()));
		optCache.put("toDouble", new ToWantedType(new DoubleConverter()));
		optCache.put("toString", new ToString());
		optCache.put("toList", new ToCollection(false));
		optCache.put("toSet", new ToCollection(true));
		optCache.put("toDate", new ToDataCreater(null));
		optCache.put("toDatetime", new ToWantedType(new UtilDateConverter()));

		optCache.put("isBoolean", new IsBoolean());
		optCache.put("isNumber", new IsNumber());
		optCache.put("isString", new IsString());
		optCache.put("isDate", new IsDate());
		optCache.put("isMap", new IsMap());
		optCache.put("isList", new IsList());
		optCache.put("isSet", new IsSet());
		optCache.put("isCollection", new IsCollection());
		optCache.put("isArray", new IsArray());
	}

}

/**
 * 抽象的检测操作.
 */
abstract class AbstractCheck extends AbstractOneSpecial
{
	public AbstractCheck()
	{
		this.defaultValue = Boolean.FALSE;
	}

}

/**
 * 判断对象是否为boolean.
 */
class IsBoolean extends AbstractCheck
{
	protected Object exec(Object obj, Object[] args)
	{
		return obj instanceof Boolean ? Boolean.TRUE : Boolean.FALSE;
	}

}

/**
 * 判断对象是否为number.
 */
class IsNumber extends AbstractCheck
{
	protected Object exec(Object obj, Object[] args)
	{
		return obj instanceof Number ? Boolean.TRUE : Boolean.FALSE;
	}

}

/**
 * 判断对象是否为string.
 */
class IsString extends AbstractCheck
{
	protected Object exec(Object obj, Object[] args)
	{
		return obj instanceof CharSequence ? Boolean.TRUE : Boolean.FALSE;
	}

}

/**
 * 判断对象是否为date.
 */
class IsDate extends AbstractCheck
{
	protected Object exec(Object obj, Object[] args)
	{
		return obj instanceof Date ? Boolean.TRUE : Boolean.FALSE;
	}

}

/**
 * 判断对象是否为map.
 */
class IsMap extends AbstractCheck
{
	protected Object exec(Object obj, Object[] args)
	{
		return obj instanceof Map ? Boolean.TRUE : Boolean.FALSE;
	}

}

/**
 * 判断对象是否为list.
 */
class IsList extends AbstractCheck
{
	protected Object exec(Object obj, Object[] args)
	{
		return obj instanceof List ? Boolean.TRUE : Boolean.FALSE;
	}

}

/**
 * 判断对象是否为set.
 */
class IsSet extends AbstractCheck
{
	protected Object exec(Object obj, Object[] args)
	{
		return obj instanceof Set ? Boolean.TRUE : Boolean.FALSE;
	}

}

/**
 * 判断对象是否为collection.
 */
class IsCollection extends AbstractCheck
{
	protected Object exec(Object obj, Object[] args)
	{
		return obj instanceof Collection ? Boolean.TRUE : Boolean.FALSE;
	}

}

/**
 * 判断对象是否为array.
 */
class IsArray extends AbstractCheck
{
	protected Object exec(Object obj, Object[] args)
	{
		return ClassGenerator.isArray(obj.getClass()) ? Boolean.TRUE : Boolean.FALSE;
	}

}
