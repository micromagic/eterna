
package self.micromagic.expression.impl;

import self.micromagic.eterna.model.AppData;
import self.micromagic.expression.AbstractExpression;
import self.micromagic.expression.Expression;
import self.micromagic.expression.SpecialOpt;
import self.micromagic.util.ref.BooleanRef;

/**
 * 特殊操作的表达式对象.
 */
public class SpecialExpression extends AbstractExpression
		implements Expression
{
	private final Object[] args;
	private final SpecialOpt opt;

	public SpecialExpression(Object[] args, SpecialOpt opt)
	{
		this.opt = opt;
		this.args = new Object[args.length];
		boolean allConst = true;
		for (int i = 0; i < args.length; i++)
		{
			BooleanRef ref = new BooleanRef();
			this.args[i] = tryGetValue(args[i], ref);
			if (!ref.value)
			{
				allConst = false;
			}
		}
		this.allArgConst = allConst && opt.isStabile();
	}

	public Object getResult(AppData data)
	{
		Object[] values = new Object[this.args.length];
		for (int i = 0; i < values.length; i++)
		{
			values[i] = getValue(this.args[i], data);
		}
		return this.opt.exec(values);
	}

	public Object tryGetResult(BooleanRef getted)
	{
		if (this.allArgConst)
		{
			if (getted != null)
			{
				getted.value = true;
			}
			return this.opt.exec(this.args);
		}
		return this;
	}

}
