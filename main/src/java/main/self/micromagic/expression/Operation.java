
package self.micromagic.expression;

/**
 * 运算操作的接口.
 */
public interface Operation
{
	/**
	 * 执行运算操作.
	 */
	Object exec(Object obj1, Object obj2);

}