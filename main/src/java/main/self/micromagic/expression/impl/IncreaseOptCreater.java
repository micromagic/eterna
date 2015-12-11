
package self.micromagic.expression.impl;

import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.DataHandler;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.expression.AbstractExpression;
import self.micromagic.expression.ExpCreater;
import self.micromagic.expression.Expression;
import self.micromagic.expression.antlr.ExpTokenTypes;
import self.micromagic.util.Utility;
import self.micromagic.util.ref.BooleanRef;
import antlr.collections.AST;

/**
 * 构造自增减操作的表达式.
 */
public class IncreaseOptCreater
		implements ExpCreater
{
	private static final TwoOptCreater PLUS = new TwoOptCreater(
			AbstractExpression.TYPE_LEVLE_NUM, "PLUS2", "+");
	private static final TwoOptCreater MINUS = new TwoOptCreater(
			AbstractExpression.TYPE_LEVLE_NUM, "MINUS2", "-");

	public Object create(AST node)
	{
		int type = node.getType();
		AST tmp = node.getFirstChild();
		if (tmp.getType() != ExpTokenTypes.VAR)
		{
			throw new EternaException("Only var can inc or dec, the node is [" + tmp.getType() + "].");
		}
		DataHandler var;
		Object arg;
		boolean after = false;
		if (type == ExpTokenTypes.INC)
		{
			var = VarCreater.create("inc", tmp, false);
			arg = new TwoOptExpression(var, Utility.INTEGER_1, PLUS);
		}
		else if (type == ExpTokenTypes.DEC)
		{
			var = VarCreater.create("dec", tmp, false);
			arg = new TwoOptExpression(var, Utility.INTEGER_1, MINUS);
		}
		else if (type == ExpTokenTypes.POST_INC)
		{
			var = VarCreater.create("incAfter", tmp, false);
			arg = new TwoOptExpression(var, Utility.INTEGER_1, PLUS);
			after = true;
		}
		else if (type == ExpTokenTypes.POST_DEC)
		{
			var = VarCreater.create("decAfter", tmp, false);
			arg = new TwoOptExpression(var, Utility.INTEGER_1, MINUS);
			after = true;
		}
		else
		{
			throw new EternaException("Error inc or dec node type [" + type + "].");
		}
		Expression assign = new AssignOptExpression(var, arg);
		return after ? new AfterIncExpression(var, assign) : assign;
	}

}

class AfterIncExpression extends AbstractExpression
		implements Expression
{
	private final DataHandler var;
	private final Expression assign;

	public AfterIncExpression(DataHandler var, Expression assign)
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
