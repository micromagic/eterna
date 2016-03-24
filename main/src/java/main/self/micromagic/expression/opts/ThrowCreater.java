
package self.micromagic.expression.opts;

import java.lang.reflect.Constructor;

import org.apache.commons.logging.Log;

import self.micromagic.cg.ClassGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.expression.AbstractOneSpecial;
import self.micromagic.expression.SpecialCreater;
import self.micromagic.expression.SpecialOpt;
import self.micromagic.util.Utility;

/**
 * 抛出异常操作的创建者.
 */
public class ThrowCreater extends AbstractOneSpecial
		implements SpecialCreater
{
	private static final Log log = Utility.createLog("eterna.expression");

	protected Constructor constructor;

	protected Object exec(Object obj, Object[] args)
	{
		if (obj instanceof RuntimeException)
		{
			throw (RuntimeException) obj;
		}
		if (obj instanceof Error)
		{
			throw (Error) obj;
		}
		if (obj instanceof Exception)
		{
			if (this.constructor != null)
			{
				Object tmp = null;
				try
				{
					tmp = this.constructor.newInstance(new Object[]{obj});
				}
				catch (Exception ex)
				{
					log.error("Error in constructor exception [" + this.constructor + "].", ex);
				}
				if (tmp != null)
				{
					throw (RuntimeException) tmp;
				}
			}
			throw new EternaException((Exception) obj);
		}
		return null;
	}

	public SpecialOpt create(String name, Object[] args)
	{
		if (args == null || args.length < 2)
		{
			return this;
		}
		if (args[1] instanceof Class || args[1] instanceof String)
		{
			Class c;
			if (args[1] instanceof String)
			{
				try
				{
					c = Class.forName((String) args[1], true, Utility.getContextClassLoader());
				}
				catch (Exception ex)
				{
					log.warn("Can't get class [" + args[1] + "].", ex);
					return this;
				}
			}
			else
			{
				c = (Class) args[1];
			}
			if (RuntimeException.class.isAssignableFrom(c))
			{
				Constructor[] arr = c.getConstructors();
				for (int i = 0; i < arr.length; i++)
				{
					Class[] types = arr[i].getParameterTypes();
					if (types.length == 1 && types[0].isAssignableFrom(Exception.class))
					{
						ThrowCreater r = new ThrowCreater();
						r.constructor = arr[i];
						return r;
					}
				}
				log.warn("Not found suitable constructor for [" + ClassGenerator.getClassName(c) + "].");
			}
			else
			{
				log.warn("The class [" + ClassGenerator.getClassName(c)
						+ "] isn't instanceof RuntimeException.");
			}
		}
		return this;
	}

	public boolean isStabile()
	{
		return false;
	}

}
