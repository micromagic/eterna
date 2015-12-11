
package self.micromagic.expression.impl;

import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.expression.AbstractExpression;
import self.micromagic.expression.ExpCreater;
import self.micromagic.expression.ExpTool;
import self.micromagic.expression.Expression;
import self.micromagic.expression.Operation;
import self.micromagic.util.Utility;
import self.micromagic.util.converter.BooleanConverter;
import self.micromagic.util.ref.BooleanRef;
import self.micromagic.util.ref.ObjectRef;
import antlr.collections.AST;

/**
 * 构造双目目操作的表达式.
 */
public class TwoOptCreater
		implements ExpCreater
{
	final int typeLevel;
	final String optName;
	final String optFlag;

	public TwoOptCreater(int typeLevel, String optName, String optFlag)
	{
		this.typeLevel = typeLevel;
		this.optName = optName;
		this.optFlag = optFlag;
	}

	public Object create(AST node)
	{
		AST tmp = node.getFirstChild();
		Object arg1 = ExpTool.parseExpNode(tmp);
		tmp = tmp.getNextSibling();
		Object arg2 = ExpTool.parseExpNode(tmp);
		return new TwoOptExpression(arg1, arg2, this);
	}

}

class TwoOptExpression extends AbstractExpression
		implements Expression
{
	private final TwoOptCreater creater;
	private final Object arg1;
	private final Object arg2;

	public TwoOptExpression(Object arg1, Object arg2, TwoOptCreater creater)
	{
		this.creater = creater;
		BooleanRef getted1 = new BooleanRef();
		this.arg1 = tryGetValue(arg1, getted1);
		BooleanRef getted2 = new BooleanRef();
		this.arg2 = tryGetValue(arg2, getted2);
		this.allArgConst = getted1.value && getted2.value;
	}

	public Object getResult(AppData data)
	{
		int level = this.creater.typeLevel;
		if (level < TYPE_LEVLE_NUM)
		{
			boolean r = BooleanConverter.toBoolean(getValue(this.arg1, data));
			if (r)
			{
				if ((level & TYPE_LEVLE_OR) != 0)
				{
					return Boolean.TRUE;
				}
				r = BooleanConverter.toBoolean(getValue(this.arg2, data));
				return r ? Boolean.TRUE : Boolean.FALSE;
			}
			else
			{
				if ((level & TYPE_LEVLE_AND) != 0)
				{
					return Boolean.FALSE;
				}
				r = BooleanConverter.toBoolean(getValue(this.arg2, data));
				return r ? Boolean.TRUE : Boolean.FALSE;
			}
		}
		return exec(getValue(this.arg1, data), getValue(this.arg2, data), this.creater);
	}

	public Object tryGetResult(BooleanRef getted)
	{
		if (this.allArgConst)
		{
			if (getted != null)
			{
				getted.value = true;
			}
			return exec(this.arg1, this.arg2, this.creater);
		}
		return this;
	}

	/**
	 * 执行表达式.
	 */
	private static Object exec(Object arg1, Object arg2, TwoOptCreater creater)
	{
		ObjectRef ref1 = new ObjectRef(arg1);
		ObjectRef ref2 = new ObjectRef(arg2);
		int level1 = ExpTool.getNumberLevel(ref1, true);
		int level2 = ExpTool.getNumberLevel(ref2, true);
		Operation opt;
		if (level1 == -1 || level2 == -1)
		{
			if (creater.typeLevel <= TYPE_LEVLE_BOOL_NUM)
			{
				if ((creater.typeLevel & TYPE_LEVLE_BOOL) == 0)
				{
					throw new EternaException(
							"The [" + (level1 == -1 ? arg1 : arg2).getClass() + "] isn't number.");
				}
				boolean b1 = BooleanConverter.toBoolean(arg1);
				boolean b2 = BooleanConverter.toBoolean(arg2);
				if ((creater.typeLevel & TYPE_LEVLE_AND) != 0)
				{
					return b1 && b2 ? Boolean.TRUE : Boolean.FALSE;
				}
				else if ((creater.typeLevel & TYPE_LEVLE_OR) != 0)
				{
					return b1 || b2 ? Boolean.TRUE : Boolean.FALSE;
				}
				else
				{
					return b1 ^ b2 ? Boolean.TRUE : Boolean.FALSE;
				}
			}
			else
			{
				return getCompareResult(arg1, arg2, creater.typeLevel);
			}
		}
		else
		{
			int level = Math.max(level1, level2);
			if (level > ExpTool.LONG_LEVEL && (creater.typeLevel & TYPE_LEVLE_INT) != 0)
			{
				level = ExpTool.LONG_LEVEL;
			}
			opt = ExpTool.getNumberOpt(level, creater.optName, creater.optFlag);
		}
		return opt.exec(ref1.getObject(), ref2.getObject());
	}

	private static Boolean getCompareResult(Object arg1, Object arg2, int level)
	{
		boolean r;
		if (level <= TYPE_LEVLE_OBJ_EQUAL)
		{
			r = Utility.objectEquals(arg1, arg2);
			if (level != TYPE_LEVLE_OBJ_EQUAL)
			{
				r = !r;
			}
		}
		else
		{
			int cNum;
			if (arg1 == null || arg2 == null)
			{
				cNum = arg1 != null ? 1 : arg2 != null ? -1 : 0;
			}
			else
			{
				if (!(arg1 instanceof Comparable))
				{
					throw new EternaException("The [" + arg1.getClass() + "] can't compare.");
				}
				cNum = ((Comparable) arg1).compareTo(arg2);
			}
			r = level == TYPE_LEVLE_OBJ_MORE ? cNum > 0 : level == TYPE_LEVLE_OBJ_LESSL ? cNum < 0
					: level == TYPE_LEVLE_OBJ_MORE_EQUAL ? cNum >= 0 : cNum <= 0;
		}
		return r ? Boolean.TRUE : Boolean.FALSE;
	}

}

