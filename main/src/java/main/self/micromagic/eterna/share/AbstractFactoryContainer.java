
package self.micromagic.eterna.share;

import java.util.Iterator;
import java.util.Map;

public abstract class AbstractFactoryContainer
		implements FactoryContainer
{
	public void reInit()
	{
		this.reInit(null);
	}

	/**
	 * 批量设置属性.
	 */
	public boolean setAttrs(Map attrs)
	{
		if (attrs != null)
		{
			boolean hasClassLoader = false;
			int count = attrs.size();
			Iterator itr = attrs.entrySet().iterator();
			for (int i = 0; i < count; i++)
			{
				Map.Entry e = (Map.Entry) itr.next();
				String name = (String) e.getKey();
				if (!hasClassLoader && CLASSLOADER_FLAG.equals(name))
				{
					hasClassLoader = true;
				}
				this.setAttribute(name, e.getValue());
			}
			return hasClassLoader;
		}
		return false;
	}

	public void setAttribute(String name, Object attr)
	{
		this.attrs.setAttribute(name, attr);
	}
	public void removeAttribute(String name)
	{
		this.attrs.removeAttribute(name);
	}
	public Object getAttribute(String name)
	{
		return this.attrs.getAttribute(name);
	}
	protected AttributeManager attrs = new AttributeManager();

	protected void finalize()
			throws Throwable
	{
		this.destroy();
		super.finalize();
	}

}
