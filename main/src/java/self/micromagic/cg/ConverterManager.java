/*
 * Copyright 2009-2015 xinjunli (micromagic@sina.com).
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

import java.beans.PropertyEditor;
import java.util.HashMap;

import self.micromagic.util.converter.ObjectConverter;
import self.micromagic.util.converter.ValueConverter;
import self.micromagic.util.Utility;

/**
 * 设置属性时的转换器管理者.
 *
 * @author micromagic@sina.com
 */
class ConverterManager
{
	private ValueConverter[] converters = new ValueConverter[16];
	private int usedCount;
	private HashMap converterIndexMap = new HashMap();

	/**
	 * 根据转换器的索引值获取对应的转换器.
	 *
	 * @param index  转换器的索引值
	 */
	ValueConverter getConverter(int index)
	{
		return this.converters[index];
	}

	/**
	 * 根据值的类型获得转换器的索引值.
	 *
	 * @param type    值的类型
	 * @return   -1未找到对应的转换器, 如果大于等于0则为转换器对应的索引值
	 * @see #getConverter(int)
	 */
	int getConverterIndex(Class type)
	{
		Integer i = (Integer) this.converterIndexMap.get(type);
		if (i == null)
		{
			return -1;
		}
		return i.intValue();
	}

	/**
	 * 给一个类型注册一个转换器.
	 */
	synchronized void registerConverter(Class type, ValueConverter converter)
	{
		if (converter == null)
		{
			return;
		}
		Integer i = (Integer) this.converterIndexMap.get(type);
		if (i == null)
		{
			if (this.converters.length <= this.usedCount + 1)
			{
				int newCapacity = this.usedCount + 16;
				ValueConverter[] newConverters = new ValueConverter[newCapacity];
				System.arraycopy(this.converters, 0, newConverters, 0, this.converters.length);
				this.converters = newConverters;
			}
			i = Utility.createInteger(++this.usedCount);
			this.converterIndexMap.put(type, i);
		}
		if (this.converters[i.intValue()] != null && type.isPrimitive())
		{
			if (this.converters[i.intValue()].getClass() != converter.getClass())
			{
				throw new IllegalArgumentException("For the primitive [" + type
						+ "], the ValueConverter class must same as the old.");
			}
		}
		this.converters[i.intValue()] = converter;
	}

	/**
	 * 给一个类型注册一个<code>PropertyEditor</code>, 转换器会使用它来进行转换.
	 */
	synchronized void registerPropertyEditor(Class type, PropertyEditor pe)
	{
		if (pe == null)
		{
			return;
		}
		if (type.isPrimitive())
		{
			int tmpI = this.getConverterIndex(type);
			ValueConverter vc = this.converters[tmpI].copy();
			vc.setPropertyEditor(pe);
			this.converters[tmpI] = vc;
		}
		else
		{
			ValueConverter vc = new ObjectConverter();
			vc.setPropertyEditor(pe);
			this.registerConverter(type, vc);
		}
	}

	/**
	 * 复制转换器管理者.
	 */
	public ConverterManager copy()
	{
		ConverterManager result = new ConverterManager();
		result.usedCount = this.usedCount;
		result.converters = new ValueConverter[this.converters.length];
		System.arraycopy(this.converters, 0, result.converters, 0, result.converters.length);
		result.converterIndexMap = new HashMap(this.converterIndexMap);
		return result;
	}

}