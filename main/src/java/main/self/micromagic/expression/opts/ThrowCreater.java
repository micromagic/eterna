
package self.micromagic.expression.opts;

import java.lang.reflect.Constructor;

import org.apache.commons.logging.Log;

import self.micromagic.cg.ClassGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.expression.SpecialCreater;
import self.micromagic.expression.SpecialOpt;
import self.micromagic.util.Utility;

/**
 * 抛出异常操作的创建者.
 */
public class ThrowCreater
		implements SpecialCreater, SpecialOpt
{
	private static final Log log = Utility.createLog("eterna.expression");

	protected Constructor constructorEx;
	protected Constructor constructorStr;

	public Object exec(Object[] args)
	{
		if (args == null || args.length == 0)
		{
			return this.exec(null, args);
		}
		return this.exec(args[0], args);
	}

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
			if (this.constructorEx != null)
			{
				Object tmp = null;
				try
				{
					tmp = this.constructorEx.newInstance(new Object[]{obj});
				}
				catch (Exception ex)
				{
					log.error("Error in constructor exception [" + this.constructorEx + "].", ex);
				}
				if (tmp != null)
				{
					throw (RuntimeException) tmp;
				}
			}
			throw new EternaException((Exception) obj);
		}
		String msg = obj == null ? "" : obj.toString();
		if (this.constructorStr != null)
		{
			Object tmp = null;
			try
			{
				tmp = this.constructorStr.newInstance(new Object[]{msg});
			}
			catch (Exception ex)
			{
				log.error("Error in constructor exception [" + this.constructorEx + "].", ex);
			}
			if (tmp != null)
			{
				throw (RuntimeException) tmp;
			}
		}
		throw new EternaException(msg);
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
				ThrowCreater result = null;
				for (int i = 0; i < arr.length; i++)
				{
					Class[] types = arr[i].getParameterTypes();
					if (types.length == 1)
					{
						if (types[0].isAssignableFrom(Exception.class))
						{
							if (result == null)
							{
								result = new ThrowCreater();
							}
							result.constructorEx = arr[i];
						}
						else if (types[0] == String.class)
						{
							if (result == null)
							{
								result = new ThrowCreater();
							}
							result.constructorStr = arr[i];
						}
					}
				}
				if (result == null)
				{
					log.warn("Not found suitable constructor for ["
							+ ClassGenerator.getClassName(c) + "].");
				}
				else
				{
					return result;
				}
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
