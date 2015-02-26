
package self.micromagic.expression;

import self.micromagic.eterna.model.AppData;

/**
 * 运算操作.
 */
public interface Operator
{
	/**
	 * 获取运算的结果.
	 */
	Object getResult(AppData data);

	/**
	 * 创建当前的运算操作对象.
	 *
	 * @param arg1  第一个参数
	 * @param arg2  第二个参数
	 */
	Operator create(Object arg1, Object arg2);

	/**
	 * 当前运算操作对象是否有效, 即是否已被创建(设置了参数).
	 */
	boolean isValid();

	/**
	 * 操作符前是否需要参数.
	 */
	boolean needArgBefore();

	/**
	 * 操作符后是否需要参数.
	 */
	boolean needArgAfter();

}
