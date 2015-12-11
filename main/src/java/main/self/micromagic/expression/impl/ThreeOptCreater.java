
package self.micromagic.expression.impl;

import self.micromagic.eterna.model.AppData;
import self.micromagic.expression.AbstractExpression;
import self.micromagic.expression.ExpCreater;
import self.micromagic.expression.ExpTool;
import self.micromagic.expression.Expression;
import self.micromagic.util.converter.BooleanConverter;
import self.micromagic.util.ref.BooleanRef;
import antlr.collections.AST;

/**
 * 构造三目操作的表达式.
 */
public class ThreeOptCreater
		implements ExpCreater
{
	public Object create(AST node)
	{
		AST tmp = node.getFirstChild();
		Object arg1 = ExpTool.parseExpNode(tmp);
		BooleanRef getted = new BooleanRef();
		arg1 = AbstractExpression.tryGetValue(arg1, getted);
		if (getted.value)
		{
			if (BooleanConverter.toBoolean(arg1))
			{
				// get arg2
				tmp = tmp.getNextSibling();
				return ExpTool.parseExpNode(tmp);
			}
			else
			{
				// get arg3
				tmp = tmp.getNextSibling();
				tmp = tmp.getNextSibling();
				return ExpTool.parseExpNode(tmp);
			}
		}
		tmp = tmp.getNextSibling();
		Object arg2 = ExpTool.parseExpNode(tmp);
		tmp = tmp.getNextSibling();
		Object arg3 = ExpTool.parseExpNode(tmp);
		return new ThreeOptExpression(arg1, arg2, arg3);
	}

}

class ThreeOptExpression extends AbstractExpression
		implements Expression
{
	private final Object arg1;
	private final Object arg2;
	private final Object arg3;

	public ThreeOptExpression(Object arg1, Object arg2, Object arg3)
	{
		this.arg1 = arg1;
		this.arg2 = tryGetValue(arg2, null);
		this.arg3 = tryGetValue(arg3, null);
	}

	public Object getResult(AppData data)
	{
		boolean b = BooleanConverter.toBoolean(getValue(this.arg1, data));
		return b ? getValue(this.arg2, data) : getValue(this.arg3, data);
	}

	public Object tryGetResult(BooleanRef getted)
	{
		return this;
	}

}

