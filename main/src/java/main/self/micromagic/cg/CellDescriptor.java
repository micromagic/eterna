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

import self.micromagic.cg.proxy.BeanPropertyReader;
import self.micromagic.cg.proxy.BeanPropertyWriter;

/**
 * bean属性单元的描述类.
 *
 * @author micromagic@sina.com
 */
public class CellDescriptor
{
	private String name;
	private boolean readOldValue;
	private boolean beanType;
	private boolean arrayType;
	private boolean arrayBeanType;
	private boolean valid = true;

	BeanPropertyReader readProcesser;  // BeanPropertyReader
	BeanPropertyWriter writeProcesser; // BeanPropertyWriter

	private Class cellType;
	private Class arrayElementType;

	/**
	 * 获取属性的名称.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * 设置属性的名称.
	 */
	void setName(String name)
	{
		this.name = name;
	}

	/**
	 * 获取写属性时是否要读取原来的值.
	 */
	public boolean isReadOldValue()
	{
		return readOldValue;
	}

	/**
	 * 设置写属性时是否要读取原来的值.
	 */
	public void setReadOldValue(boolean readOldValue)
	{
		this.readOldValue = readOldValue;
	}

	/**
	 * 获取属性单元的类型.
	 */
	public Class getCellType()
	{
		return this.cellType;
	}

	/**
	 * 设置属性单元的类型.
	 */
	public void setCellType(Class cellType)
	{
		this.cellType = cellType;
	}

	/**
	 * 获取属性单元的类型是否是一个数组.
	 */
	public boolean isArrayType()
	{
		return arrayType;
	}

	/**
	 * 设置属性单元的类型是否是一个数组.
	 */
	public void setArrayType(boolean arrayType)
	{
		this.arrayType = arrayType;
		if (arrayType)
		{
			this.setReadOldValue(true);
			Class tmpClass = ClassGenerator.getArrayElementType(this.getCellType(), null);
			this.setArrayElementType(tmpClass);
			if (BeanTool.checkBean(tmpClass))
			{
				this.setArrayBeanType(true);
			}
		}
		else
		{
			this.arrayBeanType = false;
			this.arrayElementType = null;
		}
	}

	/**
	 * 如果属性单元是一个数组, 获取该数组的元素类型是否是一个bean.
	 */
	public boolean isArrayBeanType()
	{
		return this.arrayBeanType;
	}

	/**
	 * 如果属性单元是一个数组, 设置该数组的元素类型是否是一个bean.
	 */
	public void setArrayBeanType(boolean arrayBeanType)
	{
		if (this.isArrayType())
		{
			this.arrayBeanType = arrayBeanType;
		}
	}

	/**
	 * 如果属性单元是一个数组, 获取该数组的元素类型.
	 */
	public Class getArrayElementType()
	{
		return this.arrayElementType;
	}

	/**
	 * 如果属性单元是一个数组, 设置该数组的元素类型.
	 */
	public void setArrayElementType(Class arrayElementType)
	{
		if (this.isArrayType())
		{
			this.arrayElementType = arrayElementType;
		}
	}

	/**
	 * 获取属性单元的类型是否是一个bean.
	 */
	public boolean isBeanType()
	{
		return this.beanType;
	}

	/**
	 * 设置属性单元的类型是否是一个bean.
	 */
	public void setBeanType(boolean beanType)
	{
		if (beanType)
		{
			this.setReadOldValue(true);
		}
		this.beanType = beanType;
	}

	/**
	 * 获取对bean属性的读处理者.
	 */
	public BeanPropertyReader getReadProcesser()
	{
		return this.readProcesser;
	}

	/**
	 * 设置对bean属性的读处理者.
	 */
	public void setReadProcesser(BeanPropertyReader readProcesser)
	{
		this.readProcesser = readProcesser;
	}

	/**
	 * 获取对bean属性的写处理者.
	 */
	public BeanPropertyWriter getWriteProcesser()
	{
		return this.writeProcesser;
	}

	/**
	 * 设置对bean属性的写处理者.
	 */
	public void setWriteProcesser(BeanPropertyWriter writeProcesser)
	{
		this.writeProcesser = writeProcesser;
	}

	/**
	 * 判断属性单元是否有效.
	 */
	public boolean isValid()
	{
		return this.valid;
	}

	/**
	 * 设置属性单元是否有效.
	 */
	public void setValid(boolean valid)
	{
		this.valid = valid;
	}

}