
package self.micromagic.expression;

import self.micromagic.eterna.model.AppData;

/**
 * 动态对象.
 */
public interface DynamicObject
{
	/**
	 * 获取动态结果.
	 */
	Object getResult(AppData data);

}
