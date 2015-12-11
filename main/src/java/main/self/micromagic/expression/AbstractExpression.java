
package self.micromagic.expression;

import self.micromagic.eterna.model.AppData;
import self.micromagic.eterna.model.DataHandler;
import self.micromagic.util.ref.BooleanRef;

/**
 * 抽象的表达式对象.
 */
public abstract class AbstractExpression
		implements Expression
{
	public static final int TYPE_LEVLE_BOOL = 0x10;
	public static final int TYPE_LEVLE_NUM = 0x20;

	public static final int TYPE_LEVLE_INT = 0x1;
	public static final int TYPE_LEVLE_AND = 0x2;
	public static final int TYPE_LEVLE_OR = 0x4;
	public static final int TYPE_LEVLE_NOT = 0x8;

	public static final int TYPE_LEVLE_ONLY_INT = TYPE_LEVLE_INT | TYPE_LEVLE_NUM;
	public static final int TYPE_LEVLE_BOOL_NOT_INT
			= TYPE_LEVLE_NOT | TYPE_LEVLE_INT | TYPE_LEVLE_NUM | TYPE_LEVLE_BOOL;
	public static final int TYPE_LEVLE_BOOL_OR_INT
			= TYPE_LEVLE_OR | TYPE_LEVLE_INT | TYPE_LEVLE_NUM | TYPE_LEVLE_BOOL;
	public static final int TYPE_LEVLE_BOOL_AND_INT
			= TYPE_LEVLE_AND | TYPE_LEVLE_INT | TYPE_LEVLE_NUM | TYPE_LEVLE_BOOL;

	public static final int TYPE_LEVLE_BOOL_NUM = 0x50;

	public static final int TYPE_LEVLE_OBJ_NOT_EQUAL = 0x100;
	public static final int TYPE_LEVLE_OBJ_EQUAL = 0x120;

	public static final int TYPE_LEVLE_OBJ_MORE_EQUAL = 0x210;
	public static final int TYPE_LEVLE_OBJ_LESS_EQUAL = 0x220;
	public static final int TYPE_LEVLE_OBJ_MORE = 0x230;
	public static final int TYPE_LEVLE_OBJ_LESSL = 0x240;

	/**
	 * 是否所有的参数都为常量.
	 */
	protected boolean allArgConst;

	/**
	 * 获取一个对象的值. 如: 表达式的结果, 变量的值等.
	 */
	public static Object getValue(Object arg, AppData data)
	{
		if (arg != null)
		{
			if (arg instanceof DataHandler)
			{
				return ((DataHandler) arg).getData(data, false);
			}
			else if (arg instanceof Expression)
			{
				return ((Expression) arg).getResult(data);
			}
		}
		return arg;
	}

	/**
	 * 尝试获取一个对象的值. 如: 常量表达式的结果等.
	 */
	public static Object tryGetValue(Object arg, BooleanRef getted)
	{
		if (arg != null)
		{
			if (arg instanceof DataHandler)
			{
				if (getted != null)
				{
					getted.value = false;
				}
				return arg;
			}
			else if (arg instanceof Expression)
			{
				return ((Expression) arg).tryGetResult(getted);
			}
		}
		if (getted != null)
		{
			getted.value = true;
		}
		return arg;
	}

}
