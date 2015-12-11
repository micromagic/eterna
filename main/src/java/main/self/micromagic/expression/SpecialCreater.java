
package self.micromagic.expression;

/**
 * 特殊操作对象的创建者.
 */
public interface SpecialCreater
{
	/**
	 * 创建一个特殊的操作对象.
	 *
	 * @param args  参数列表, 常量或表达式对象
	 */
	SpecialOpt create(String name, Object[] args);

}
