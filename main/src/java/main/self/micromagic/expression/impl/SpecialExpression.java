
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
	private final boolean canTry;
	private final Object[] args;
	private final SpecialOpt opt;

	public SpecialExpression(Object[] args, SpecialOpt opt, boolean allArgConst)
	{
		this.opt = opt;
		this.args = args;
		this.allArgConst = allArgConst;
		this.canTry = allArgConst && opt.isStabile();
	}

	/**
	 * 检测并处理所有可变为常量的参数.
	 *
	 * return 是否所有的参数都变为常量
	 */
	public static boolean checkArgs(Object[] args)
	{
		boolean allConst = true;
		BooleanRef ref = new BooleanRef();
		for (int i = 0; i < args.length; i++)
		{
			args[i] = tryGetValue(args[i], ref);
			if (allConst && !ref.value)
			{
				allConst = false;
			}
		}
		return allConst;
	}

	public Object getResult(AppData data)
	{
		Object[] values;
		if (this.allArgConst)
		{
			values = this.args;
		}
		else
		{
			values = new Object[this.args.length];
			for (int i = 0; i < values.length; i++)
			{
				values[i] = getValue(this.args[i], data);
			}
		}
		return this.opt.exec(values);
	}

	public Object tryGetResult(BooleanRef getted)
	{
		if (this.canTry)
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
