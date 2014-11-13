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

/**
 * 属性的访问信息.
 *
 * @author micromagic@sina.com
 */
public class CellAccessInfo
{
	/**
	 * 该属性单元所在的BeanMap.
	 */
	public final BeanMap beanMap;

	/**
	 * 属性单元的描述类.
	 */
	public final CellDescriptor cellDescriptor;

	/**
	 * 对数组单元访问的索引值列表.
	 */
	public final int[] indexs;

	/**
	 * 构造一个属性的访问信息类.
	 */
	public CellAccessInfo(BeanMap beanMap, CellDescriptor cellDescriptor, int[] indexs)
	{
		this.beanMap = beanMap;
		this.cellDescriptor = cellDescriptor;
		this.indexs = indexs;
	}

	/**
	 * 获取对应访问信息的值.
	 * 如果该访问信息的属性单元没有读功能, 则不能获取.
	 */
	public Object getValue()
	{
		if (this.cellDescriptor.readProcesser != null)
		{
			try
			{
				Object beanObj = this.beanMap.getBean();
				String prefix = this.beanMap.getPrefix();
				if (beanObj != null)
				{
					return this.cellDescriptor.readProcesser.getBeanValue(
							this.cellDescriptor, this.indexs, beanObj, prefix, this.beanMap);
				}
			}
			catch (Exception ex)
			{
				if (ClassGenerator.COMPILE_LOG_TYPE > CG.COMPILE_LOG_TYPE_ERROR)
				{
					CG.log.info("Read bean value error.", ex);
				}
			}
		}
		return null;
	}

	/**
	 * 设置对应访问信息的值.
	 * 如果该访问信息的属性单元没有写功能, 则不能设置.
	 *
	 * @param value   要设置的值
	 * @return   该访问信息中原来的值, 如果该访问信息的属性单元没有读功能,
	 *           则永远返回<code>null</code>.
	 */
	public Object setValue(Object value)
	{
		if (this.cellDescriptor.writeProcesser != null)
		{
			try
			{
				Object oldValue = null;
				Object beanObj = this.beanMap.getBean();
				String prefix = this.beanMap.getPrefix();
				if (beanObj != null)
				{
					if (this.cellDescriptor.readProcesser != null)
					{
						oldValue = this.cellDescriptor.readProcesser.getBeanValue(
								this.cellDescriptor, this.indexs, beanObj, prefix, this.beanMap);
					}
				}
				else
				{
					beanObj = this.beanMap.createBean();
				}
				this.cellDescriptor.writeProcesser.setBeanValue(this.cellDescriptor, this.indexs,
						beanObj, value, prefix, this.beanMap, null, oldValue);
				return oldValue;
			}
			catch (Exception ex)
			{
				if (ClassGenerator.COMPILE_LOG_TYPE > CG.COMPILE_LOG_TYPE_ERROR)
				{
					CG.log.info("Write bean value error.", ex);
				}
			}
		}
		return null;
	}

}