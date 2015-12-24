
package self.micromagic.expression.impl;

import self.micromagic.eterna.model.AppData;
import self.micromagic.expression.AbstractExpression;
import self.micromagic.expression.ExprCreater;
import self.micromagic.expression.ExprTool;
import self.micromagic.expression.Expression;
import self.micromagic.expression.Operation;
import self.micromagic.util.ref.BooleanRef;
import antlr.collections.AST;

/**
 * 构造加法操作的表达式.
 */
public class PlusCreater
		implements ExprCreater
{
	public Object create(AST node)
	{
		AST tmp = node.getFirstChild();
		Object arg1 = ExprTool.parseExpNode(tmp);
		tmp = tmp.getNextSibling();
		Object arg2 = ExprTool.parseExpNode(tmp);
		return new PlusExpression(arg1, arg2);
	}

}

class PlusExpression extends AbstractExpression
		implements Expression
{
	private final Object arg1;
	private final Object arg2;

	public PlusExpression(Object arg1, Object arg2)
	{
		BooleanRef getted1 = new BooleanRef();
		this.arg1 = tryGetValue(arg1, getted1);
		BooleanRef getted2 = new BooleanRef();
		this.arg2 = tryGetValue(arg2, getted2);
		this.allArgConst = getted1.value && getted2.value;
	}

	public Object getResult(AppData data)
	{
		return exec(getValue(this.arg1, data), getValue(this.arg2, data));
	}

	public Object tryGetResult(BooleanRef getted)
	{
		if (this.allArgConst)
		{
			if (getted != null)
			{
				getted.value = true;
			}
			return exec(this.arg1, this.arg2);
		}
		return this;
	}

	/**
	 * 执行表达式.
	 */
	private static Object exec(Object arg1, Object arg2)
	{
		int level1 = ExprTool.getNumberLevel(arg1, true);
		int level2 = ExprTool.getNumberLevel(arg2, true);
		if (level1 == -1 || level2 == -1)
		{
			// 不是数字, 按字符串的方式+
			return String.valueOf(arg1).concat(String.valueOf(arg2));
		}
		if (level1 >= ExprTool.NEED_CAST_LEVEL)
		{
			arg1 = ExprTool.cast2Number(level1, arg1);
			level1 ^= ExprTool.NEED_CAST_LEVEL;
		}
		if (level2 >= ExprTool.NEED_CAST_LEVEL)
		{
			arg2 = ExprTool.cast2Number(level2, arg2);
			level2 ^= ExprTool.NEED_CAST_LEVEL;
		}
		Operation opt = ExprTool.getNumberOpt(Math.max(level1, level2), "plus2", "+");
		return opt.exec(arg1, arg2);
	}

}
