
package self.micromagic.expression.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
 * 构造对象的表达式.
 */
public class ObjValueCreater
		implements ExprCreater, ExprTokenTypes
{
	public Object create(AST node)
	{
		int type = node.getType();
		List valueList = new ArrayList();
		AST tmp = node.getFirstChild();
		boolean constObj = false;
		if (tmp != null && tmp.getType() == CONST)
		{
			constObj = true;
			tmp = tmp.getNextSibling();
		}
		int index = 0;
		while (tmp != null)
		{
			boolean addedValue = false;
			if (type == OBJ_MAP && (index++ & 0x1) == 0 && tmp.getType() == VAR)
			{
				// map的key且类型为VAR
				AST first = tmp.getFirstChild();
				if (first.getNextSibling() == null)
				{
					// VAR的定义中没有扩展配置
					String name = first.getText();
					if (!DataHandler.isValidMainName(name))
					{
						// 不是一个有效的VAR名称, 那此名称可以作为map的key
						addedValue = true;
						valueList.add(name);
					}
				}
			}
			if (!addedValue)
			{
				valueList.add(ExprTool.parseExpNode(tmp));
			}
			tmp = tmp.getNextSibling();
		}
		Object[] values = valueList.toArray();
		return new ObjValueExpression(values, type, constObj);
	}

}

class ObjValueExpression extends AbstractExpression
		implements Expression, ExprTokenTypes
{
	private final int objType;
	private final Object[] values;
	private final boolean allValueConst;

	public ObjValueExpression(Object[] values, int objType, boolean constObj)
	{
		this.objType = objType;
		this.values = values;
		this.allValueConst = SpecialExpression.checkArgs(values);
		this.allArgConst = constObj && this.allValueConst;
	}

	public Object getResult(AppData data)
	{
		Object[] objs;
		if (this.allValueConst)
		{
			objs = this.values;
		}
		else
		{
			objs = new Object[this.values.length];
			for (int i = 0; i < objs.length; i++)
			{
				objs[i] = getValue(this.values[i], data);
			}
		}
		return this.create(objs, this.objType);
	}

	public Object tryGetResult(BooleanRef getted)
	{
		if (this.allArgConst)
		{
			if (getted != null)
			{
				getted.value = true;
			}
			return this.create(this.values, this.objType);
		}
		return this;
	}

	private Object create(Object[] values, int type)
	{
		int count = values == null ? 0 : values.length;
		if (type == OBJ_MAP && (count & 0x1) != 0)
		{
			throw new EternaException("The map value count must be even.");
		}
		if (type == OBJ_MAP)
		{
			Map result = new HashMap(count);
			for (int i = 0; i < count; i += 2)
			{
				Object key = values[i];
				result.put(key == null ? null : key.toString(), values[i + 1]);
			}
			return result;
		}
		else
		{
			Collection result;
			if (type == OBJ_SET)
			{
				result = new HashSet(count * 2);
			}
			else
			{
				result = new ArrayList(count);
			}
			for (int i = 0; i < count; i++)
			{
				result.add(values[i]);
			}
			return result;
		}
	}

}
