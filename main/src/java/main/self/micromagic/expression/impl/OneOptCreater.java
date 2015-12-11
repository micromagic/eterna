
package self.micromagic.expression.impl;

import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.expression.AbstractExpression;
import self.micromagic.expression.ExpCreater;
import self.micromagic.expression.ExpTool;
import self.micromagic.expression.Expression;
import self.micromagic.expression.Operation;
import self.micromagic.util.converter.BooleanConverter;
import self.micromagic.util.ref.BooleanRef;
import self.micromagic.util.ref.ObjectRef;
import antlr.collections.AST;

/**
 * 构造单目操作的表达式.
 */
public class OneOptCreater
		implements ExpCreater
{
	final int typeLevel;
	final String optName;
	final String optFlag;

	public OneOptCreater(int typeLevel, String optName, String optFlag)
	{
		this.typeLevel = typeLevel;
		this.optName = optName;
		this.optFlag = optFlag;
	}

	public Object create(AST node)
	{
		AST tmp = node.getFirstChild();
		Object arg = ExpTool.parseExpNode(tmp);
		return new OneOptExpression(arg, this);
	}

}

class OneOptExpression extends AbstractExpression
		implements Expression
{
	private final OneOptCreater creater;
	private final Object arg;

	public OneOptExpression(Object arg, OneOptCreater creater)
	{
		this.creater = creater;
		BooleanRef getted = new BooleanRef();
		this.arg = tryGetValue(arg, getted);
		this.allArgConst = getted.value;
	}

	public Object getResult(AppData data)
	{
		return exec(getValue(this.arg, data), this.creater);
	}

	public Object tryGetResult(BooleanRef getted)
	{
		if (this.allArgConst)
		{
			if (getted != null)
			{
				getted.value = true;
			}
			return exec(this.arg, this.creater);
		}
		return this;
	}

	/**
	 * 执行表达式.
	 */
	private static Object exec(Object arg, OneOptCreater creater)
	{
		if (creater.typeLevel < TYPE_LEVLE_NUM)
		{
			return !BooleanConverter.toBoolean(arg) ? Boolean.TRUE : Boolean.FALSE;
		}
		ObjectRef ref = new ObjectRef(arg);
		int level = ExpTool.getNumberLevel(ref, true);
		if (level == -1)
		{
			throw new EternaException("The [" + arg.getClass() + "] isn't number.");
		}
		if (level > ExpTool.LONG_LEVEL && (creater.typeLevel & TYPE_LEVLE_INT) != 0)
		{
			level = ExpTool.LONG_LEVEL;
		}
		Operation opt = ExpTool.getNumberOpt(level, creater.optName, creater.optFlag);
		return opt.exec(ref.getObject(), null);
	}

}

