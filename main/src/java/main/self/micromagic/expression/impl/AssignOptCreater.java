
package self.micromagic.expression.impl;

import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.DataHandler;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.expression.AbstractExpression;
import self.micromagic.expression.ExprCreater;
import self.micromagic.expression.ExprTool;
import self.micromagic.expression.Expression;
import self.micromagic.expression.antlr.ExprTokenTypes;
import self.micromagic.util.ref.BooleanRef;
import antlr.collections.AST;

/**
 * 构造赋值操作的表达式.
 */
public class AssignOptCreater
		implements ExprCreater
{
	public Object create(AST node)
	{
		int assignType = node.getType();
		AST tmp = node.getFirstChild();
		if (tmp.getType() != ExprTokenTypes.VAR)
		{
			throw new EternaException("Only assign to var, the node is [" + tmp.getType() + "].");
		}
		DataHandler var = VarCreater.create("assign", tmp, false);
		tmp = tmp.getNextSibling();
		Object arg = ExprTool.parseExpNode(tmp);
		if (assignType == ExprTokenTypes.PLUS_ASSIGN)
		{
			arg = new PlusExpression(var, arg);
		}
		return new AssignOptExpression(var, arg);
	}

}

class AssignOptExpression extends AbstractExpression
		implements Expression
{
	private final DataHandler var;
	private final Object arg;

	public AssignOptExpression(DataHandler var, Object arg)
	{
		this.var = var;
		this.arg = tryGetValue(arg, null);
	}

	public Object getResult(AppData data)
	{
		Object value = getValue(this.arg, data);
		this.var.setData(data, value);
		return value;
	}

	public Object tryGetResult(BooleanRef getted)
	{
		return this;
	}

}

