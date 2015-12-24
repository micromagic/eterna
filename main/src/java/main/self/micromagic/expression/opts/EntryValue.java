
package self.micromagic.expression.opts;

import java.util.Map;

import self.micromagic.expression.AbstractOneSpecial;
import self.micromagic.expression.SpecialOpt;

/**
 * 获取Entry的key或value.
 */
public class EntryValue extends AbstractOneSpecial
		implements SpecialOpt
{
	/**
	 * 标识获取key还是value.
	 */
	private final boolean getKey;

	public EntryValue(boolean getKey)
	{
		this.getKey = getKey;
	}

	protected Object exec(Object obj, Object[] args)
	{
		if (obj instanceof Map.Entry)
		{
			Map.Entry e = (Map.Entry) obj;
			return this.getKey ? e.getKey() : e.getValue();
		}
		return null;
	}

}
