
package self.micromagic.expression.opts;

import java.util.Enumeration;
import java.util.Iterator;

import self.micromagic.expression.AbstractOneSpecial;
import self.micromagic.expression.SpecialOpt;

/**
 * 获取迭代器的下一个值.
 */
public class NextValue extends AbstractOneSpecial
		implements SpecialOpt
{
	protected Object exec(Object obj, Object[] args)
	{
		if (obj instanceof Iterator)
		{
			return ((Iterator) obj).next();
		}
		if (obj instanceof Enumeration)
		{
			return ((Enumeration) obj).nextElement();
		}
		return null;
	}

}
