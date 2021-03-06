
package self.micromagic.expression.impl;

import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.DataHandler;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.expression.AbstractExpression;
import self.micromagic.expression.ExprCreater;
import self.micromagic.expression.Expression;
import self.micromagic.expression.antlr.ExprTokenTypes;
import self.micromagic.util.Utility;
import self.micromagic.util.ref.BooleanRef;
import antlr.collections.AST;

/**
 * 构造对变量操作的表达式.
 */
public class VarOptCreater
		implements ExprCreater, ExprTokenTypes
{
	private static final TwoOptCreater PLUS = new TwoOptCreater(
			AbstractExpression.TYPE_LEVLE_NUM, "PLUS2", "+");
	private static final TwoOptCreater MINUS = new TwoOptCreater(
			AbstractExpression.TYPE_LEVLE_NUM, "MINUS2", "-");

	public Object create(AST node)
	{
		int type = node.getType();
		AST tmp = node.getFirstChild();
		if (tmp.getType() != VAR)
		{
			throw new EternaException("Only var can inc or dec, the node is [" + tmp.getType() + "].");
		}
		DataHandler var;
		Object arg;
		boolean after = false;
		if (type == INC)
		{
			var = VarCreater.create("inc", tmp, false);
			arg = new TwoOptExpression(var, Utility.INTEGER_1, PLUS);
		}
		else if (type == DEC)
		{
			var = VarCreater.create("dec", tmp, false);
			arg = new TwoOptExpression(var, Utility.INTEGER_1, MINUS);
		}
		else if (type == POST_INC)
		{
			var = VarCreater.create("incAfter", tmp, false);
			arg = new TwoOptExpression(var, Utility.INTEGER_1, PLUS);
			after = true;
		}
		else if (type == POST_DEC)
		{
			var = VarCreater.create("decAfter", tmp, false);
			arg = new TwoOptExpression(var, Utility.INTEGER_1, MINUS);
			after = true;
		}
		else if (type == DELETE)
		{
			return new DeleteExpression(VarCreater.create("delete", tmp, false));
		}
		else
		{
			throw new EternaException("Error inc or dec node type [" + type + "].");
		}
		Expression assign = new AssignOptExpression(var, arg);
		return after ? new AfterChangeExpression(var, assign) : assign;
	}

}

class AfterChangeExpression extends AbstractExpression
		implements Expression
{
	private final DataHandler var;
	private final Expression assign;

	public AfterChangeExpression(DataHandler var, Expression assign)
	{
		this.var = var;
		this.assign = assign;
	}

	public Object getResult(AppData data)
	{
		Object value = this.var.getData(data, false);
		this.assign.getResult(data);
		return value;
	}

	public Object tryGetResult(BooleanRef getted)
	{
		return this;
	}

}

class DeleteExpression extends AbstractExpression
		implements Expression
{
	private final DataHandler var;

	public DeleteExpression(DataHandler var)
	{
		this.var = var;
	}

	public Object getResult(AppData data)
	{
		return this.var.getData(data, true);
	}

	public Object tryGetResult(BooleanRef getted)
	{
		return this;
	}

}
