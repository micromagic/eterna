/*
 * Copyright 2015 xinjunli (micromagic@sina.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package self.micromagic.cg;

import java.util.Iterator;
import java.util.Map;

/**
 * bean的描述类.
 *
 * @author micromagic@sina.com
 */
public class BeanDescriptor
{
	private Map cells;
	private CellDescriptor initCell;
	private Class beanType;
	private ConverterManager converterManager;

	BeanDescriptor(Class beanType, Map cells, CellDescriptor initCell)
	{
		this.beanType = beanType;
		this.cells = cells;
		this.initCell = initCell;
		this.converterManager = BeanTool.converterManager;
	}

	/**
	 * 获取与此bean相关的类型转换器管理者.
	 */
	ConverterManager getConverterManager()
	{
		return this.converterManager;
	}

	/**
	 * 设置与此bean相关的类型转换器管理者.
	 */
	void setConverterManager(ConverterManager converterManager)
	{
		this.converterManager = converterManager;
	}

	/**
	 * 获得BeanMap处理的bean的类型.
	 */
	public Class getBeanType()
	{
		return this.beanType;
	}

	/**
	 * 获得BeanMap中对bean的构造单元.
	 */
	public CellDescriptor getInitCell()
	{
		return this.initCell;
	}

	/**
	 * 获得BeanMap中对bean属性的操作单元.
	 */
	public CellDescriptor getCell(String name)
	{
		return (CellDescriptor) this.cells.get(name);
	}

	/**
	 * 以迭代器的方式获得BeanMap中对bean属性的所有操作单元.
	 */
	public Iterator getCellIterator()
	{
		return this.cells.values().iterator();
	}

	/**
	 * 在BeanMap中添加一个对bean属性的操作单元.
	 *
	 * @param cd  bean属性的操作单元
	 * @return    <code>true</code>添加成功, <code>false</code>添加失败
	 *            如已经存在同名的操作单元, cd参数为null或cd的name为null
	 */
	public synchronized boolean addCell(CellDescriptor cd)
	{
		if (cd == null)
		{
			return false;
		}
		String name = cd.getName();
		if (name == null)
		{
			return false;
		}
		if (this.cells.containsKey(name))
		{
			return false;
		}
		this.cells.put(name, cd);
		return true;
	}

}