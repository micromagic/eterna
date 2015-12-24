
package self.micromagic.expression;

/**
 * 只需要单个参数的抽象特殊操作.
 */
public abstract class AbstractOneSpecial
		implements SpecialOpt
{
	/**
	 * 当参数为空或没有参数时的返回值.
	 */
	protected Object defaultValue;

	public Object exec(Object[] args)
	{
		if (args == null || args.length == 0)
		{
			return this.defaultValue;
		}
		Object obj = args[0];
		return obj == null ? this.defaultValue : this.exec(args[0], args);
	}

	/**
	 * 执行操作.
	 *
	 * @param obj   第一个参数对象
	 * @param args  所有传入的参数
	 */
	protected abstract Object exec(Object obj, Object[] args);

	public boolean isStabile()
	{
		return true;
	}

}
