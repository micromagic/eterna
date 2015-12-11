
package self.micromagic.expression;


/**
 * 特殊的操作接口.
 */
public interface SpecialOpt
{
	/**
	 * 执行操作.
	 */
	Object exec(Object[] args);

	/**
	 * 是否为稳定操作. 即给出相同的参数永远返回相同的值.
	 */
	boolean isStabile();

}
