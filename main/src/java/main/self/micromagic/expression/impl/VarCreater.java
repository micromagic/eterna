
package self.micromagic.expression.impl;

import self.micromagic.eterna.model.DataHandler;
import self.micromagic.expression.ExprCreater;
import antlr.collections.AST;

/**
 * 构造变量的表达式.
 */
public class VarCreater
		implements ExprCreater
{
	public Object create(AST node)
	{
		return create("var", node, true);
	}

	public static DataHandler create(String caption, AST node, boolean readOnly)
	{
		DataHandler handler = new DataHandler(caption, !readOnly, readOnly);
		handler.setConfig(node);
		return handler;
	}

}
