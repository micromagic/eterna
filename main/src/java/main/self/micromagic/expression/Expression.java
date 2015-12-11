
package self.micromagic.expression;

import self.micromagic.eterna.model.AppData;
import self.micromagic.util.ref.BooleanRef;

/**
 * 表达式对象.
 */
public interface Expression
{
	/**
	 * 获取表达式的结果.
	 */
	Object getResult(AppData data);

	/**
	 * 尝试获取表达式的结果, 如果表达式中都是常量, 就能获取到结果.
	 *
	 * @param getted  出参, 是否获取到了结果
	 */
	Object tryGetResult(BooleanRef getted);

}
