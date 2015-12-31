
package self.micromagic.expression.opts;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import self.micromagic.cg.BeanTool;
import self.micromagic.cg.ClassGenerator;
import self.micromagic.expression.AbstractOneSpecial;

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

/**
 * 判断对象是否为object.
 */
class IsObject extends AbstractCheck
{
	protected Object exec(Object obj, Object[] args)
	{
		if (obj instanceof Map || BeanTool.checkBean(obj.getClass()))
		{
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

}