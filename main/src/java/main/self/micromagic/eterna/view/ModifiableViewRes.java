package self.micromagic.eterna.view;

import self.micromagic.eterna.share.EternaException;

public interface ModifiableViewRes extends View.ViewRes
{
	/**
	 * 添加一个方法.
	 *
	 * @return  添加的方法名称, 方法名称可能会根据当前环境有所变化.
	 */
	public String addFunction(Function fn) throws EternaException;

	public void addTypicalComponentNames(String name) throws EternaException;

	public void addResourceNames(String name) throws EternaException;

	public void addAll(View.ViewRes res) throws EternaException;

}