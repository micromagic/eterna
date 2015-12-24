
package self.micromagic.expression;

import antlr.collections.AST;

/**
 * 表达式对象的创建者.
 */
public interface ExprCreater
{
	/**
	 * 创建一个语法节点解析后的对象.
	 */
	Object create(AST node);

}
