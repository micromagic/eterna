
package self.micromagic.expression.opts;

import java.util.HashMap;
import java.util.Map;

import self.micromagic.expression.SpecialCreater;
import self.micromagic.expression.SpecialOpt;

/**
 * 默认的特殊操作对象创建者.
 */
public class DefaultOptCreater
		implements SpecialCreater
{
	public SpecialOpt create(String name, Object[] args)
	{
		return (SpecialOpt) optCache.get(name);
	}

	private static Map optCache = new HashMap();
	static
	{
		optCache.put("link", new LinkOpt());
	}

}
