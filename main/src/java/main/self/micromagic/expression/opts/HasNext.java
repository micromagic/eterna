
package self.micromagic.expression.opts;

import java.util.Enumeration;
import java.util.Iterator;

import self.micromagic.expression.AbstractOneSpecial;
import self.micromagic.expression.SpecialOpt;

/**
 * 判断迭代器是否有下一个.
 */
public class HasNext extends AbstractOneSpecial
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
