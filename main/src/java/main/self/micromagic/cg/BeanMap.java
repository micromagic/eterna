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
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import self.micromagic.eterna.sql.ResultRow;
import self.micromagic.util.StringRef;
import self.micromagic.util.StringTool;
import self.micromagic.util.converter.ValueConverter;

/**
 * bean和map的转换工具. <p>
 * 可以快速地将bean转换为map.
 * 也可以快速地将map中的值设置到bean中.
 *
 * @author micromagic@sina.com
 */
public class BeanMap extends AbstractMap
		implements Map
{
	private Object beanObj;
	private final Class beanType;
	private final String namePrefix;
	private final BeanDescriptor beanDescriptor;
	private ConverterManager converterManager;
	private boolean converterManagerCopied;
	private List entryList = null;
	private boolean bean2Map;
	private boolean readBeforeModify = true;

	BeanMap(Object beanObj, String namePrefix, BeanDescriptor beanDescriptor)
	{
		this.beanObj = beanObj;
		this.beanType = beanObj.getClass();
		this.namePrefix = StringTool.isEmpty(namePrefix) ? "" : namePrefix;
		this.beanDescriptor = beanDescriptor;
		this.converterManager = beanDescriptor.getConverterManager();
	}

	BeanMap(Class beanType, String namePrefix, BeanDescriptor beanDescriptor)
	{
		this.beanType = beanType;
		this.namePrefix = StringTool.isEmpty(namePrefix) ? "" : namePrefix;
		this.beanDescriptor = beanDescriptor;
		this.converterManager = beanDescriptor.getConverterManager();
	}

	/**
	 * 注册一个类型转换器.
	 */
	public void registerConverter(Class type, ValueConverter converter)
	{
		if (!this.converterManagerCopied)
		{
			this.converterManager = this.converterManager.copy();
			this.converterManagerCopied = true;
		}
		this.converterManager.registerConverter(type, converter);
	}

	/**
	 * 注册一个类型转换时使用的<code>PropertyEditor</code>.
	 */
	public void registerPropertyEditor(Class type, PropertyEditor pe)
	{
		if (!this.converterManagerCopied)
		{
			this.converterManager = this.converterManager.copy();
			this.converterManagerCopied = true;
		}
		this.converterManager.registerPropertyEditor(type, pe);
	}

	/**
	 * 根据转换器的索引值获取对应的转换器.
	 *
	 * @param index  转换器的索引值
	 */
	public ValueConverter getConverter(int index)
	{
		return this.converterManager.getConverter(index);
	}

	/**
	 * 创建一个新的bean对象, 此对象会覆盖原来绑定的bean对象.
	 */
	public Object createBean()
	{
		try
		{
			this.beanObj = this.beanDescriptor.getInitCell().readProcesser.getBeanValue(
					null, null, null, this.getPrefix(), this);
		}
		catch (Exception ex) {}
		return this.beanObj;
	}

	/**
	 * 获取bean对象对应的类型.
	 */
	public Class getBeanType()
	{
		return this.beanType;
	}

	/**
	 * 获取对应的bean对象.
	 */
	public Object getBean()
	{
		return this.beanObj;
	}

	/**
	 * 在修改数据时是否要先读取原始值.
	 */
	public boolean isReadBeforeModify()
	{
		return this.readBeforeModify;
	}

	/**
	 * 设置在修改数据时是否要先读取原始值.
	 */
	public void setReadBeforeModify(boolean rbm)
	{
		this.readBeforeModify = rbm;
	}

	/**
	 * 获取对象时, 是否要将bean对象转换成map返回.
	 */
	public boolean isBean2Map()
	{
		return this.bean2Map;
	}

	/**
	 * 设置获取对象时, 是否要将bean对象转换成map返回.
	 */
	public void setBean2Map(boolean bean2Map)
	{
		this.bean2Map = bean2Map;
	}

	/**
	 * 获取属性名称的前缀.
	 */
	public String getPrefix()
	{
		return this.namePrefix;
	}

	/**
	 * 通过一个Map设置bean中所有对应的属性值.
	 * 此方法是以bean的结构为基础, 从map中获取对应的值并进行设置.
	 */
	public int setValues(Map map)
	{
		if (this.getBean() == null)
		{
			this.createBean();
		}
		int settedCount = 0;
		Iterator cdItr = this.beanDescriptor.getCellIterator();
		Object bean = this.getBean();
		String prefix = this.getPrefix();
		Object value;
		while (cdItr.hasNext())
		{
			CellDescriptor cd = (CellDescriptor) cdItr.next();
			if (!cd.isValid())
			{
				continue;
			}
			String pName = cd.getName();
			value = map.get(prefix.length() == 0 ? pName : prefix + pName);
			if (cd.writeProcesser != null)
			{
				if (cd.isBeanType() || value != null)
				{
					try
					{
						Object oldValue = null;
						if (cd.readProcesser != null && cd.isReadOldValue())
						{
							oldValue = cd.readProcesser.getBeanValue(cd, null, beanObj, prefix, this);
						}
						settedCount += cd.writeProcesser.setBeanValue(
								cd, null, bean, value, prefix, this, map, oldValue);
					}
					catch (Exception ex)
					{
						if (ClassGenerator.COMPILE_LOG_TYPE > CG.COMPILE_LOG_TYPE_ERROR)
						{
							CG.log.info("Write bean value error.", ex);
						}
					}
				}
			}
		}
		return settedCount;
	}

	/**
	 * 通过一个ResultRow设置bean中所有对应的属性值.
	 * 此方法是以bean的结构为基础, 从ResultRow中获取对应的值并进行设置.
	 */
	public int setValues(ResultRow row)
	{
		if (this.getBean() == null)
		{
			this.createBean();
		}
		int settedCount = 0;
		Iterator cdItr = this.beanDescriptor.getCellIterator();
		Object bean = this.getBean();
		String prefix = this.getPrefix();
		Object value;
		while (cdItr.hasNext())
		{
			CellDescriptor cd = (CellDescriptor) cdItr.next();
			if (!cd.isValid())
			{
				continue;
			}
			String pName = cd.getName();
			String name = prefix.length() == 0 ? pName : prefix + pName;
			try
			{
				value = row.getObject(name, true);
				if (cd.writeProcesser != null)
				{
					if (cd.isBeanType() || value != null)
					{
						Object oldValue = null;
						if (cd.readProcesser != null && cd.isReadOldValue())
						{
							oldValue = cd.readProcesser.getBeanValue(cd, null, beanObj, prefix, this);
						}
						settedCount += cd.writeProcesser.setBeanValue(
								cd, null, bean, value, prefix, this, row, oldValue);
					}
				}
			}
			catch (Exception ex)
			{
				if (ClassGenerator.COMPILE_LOG_TYPE > CG.COMPILE_LOG_TYPE_ERROR)
				{
					CG.log.info("Write bean value error.", ex);
				}
			}
		}
		return settedCount;
	}

	/**
	 * 根据键值获取属性单元的访问信息.
	 *
	 * @param key  用于获取属性单元访问信息的键值
	 */
	public CellAccessInfo getCellAccessInfo(String key)
	{
		return this.getCellAccessInfo(key, false);
	}

	/**
	 * 根据键值获取属性单元的访问信息.
	 *
	 * @param key         用于获取属性单元访问信息的键值
	 * @param needCreate  如果对应的bean不存在时是否要自动创建
	 */
	public CellAccessInfo getCellAccessInfo(String key, boolean needCreate)
	{
		int index = key.indexOf('.');
		if (index != -1)
		{
			String tmpName;
			StringRef refName = new StringRef();
			int[] indexs = parseArrayName(key.substring(0, index), refName);
			tmpName = refName.getString();
			CellDescriptor cd =  this.beanDescriptor.getCell(tmpName);
			if (cd != null && cd.isValid())
			{
				if (cd.isBeanType())
				{
					if (indexs != null)
					{
						// bean类型的无法以数组方式获取
						return null;
					}
					BeanMap sub = null;
					Object thisObj = this.getBean();
					if (thisObj == null && needCreate)
					{
						thisObj = this.createBean();
					}
					String prefix = this.getPrefix();
					if (thisObj != null && cd.readProcesser != null)
					{
						try
						{
							Object tmpObj = cd.readProcesser.getBeanValue(cd, null, thisObj, prefix, this);
							if (tmpObj != null)
							{
								sub = BeanTool.getBeanMap(tmpObj, prefix + tmpName + ".");
							}
						}
						catch (Exception ex) {}
					}
					if (sub == null)
					{
						sub = BeanTool.getBeanMap(cd.getCellType(), prefix + tmpName + ".");
						if (needCreate && cd.writeProcesser != null)
						{
							try
							{
								cd.writeProcesser.setBeanValue(cd, null, thisObj, sub.createBean(),
										prefix, this, null, null);
							}
							catch (Exception ex)
							{
								if (ClassGenerator.COMPILE_LOG_TYPE > CG.COMPILE_LOG_TYPE_ERROR)
								{
									CG.log.info("Write bean value error.", ex);
								}
							}
						}
					}
					if (sub != null)
					{
						return sub.getCellAccessInfo(key.substring(index + 1), needCreate);
					}
				}
				else if (cd.isArrayType())
				{
					if (!cd.isArrayBeanType() || indexs == null)
					{
						// 数组的元素类型不是bean或没有数组索引, 无法访问子属性
						return null;
					}
					Object thisObj = this.getBean();
					if (thisObj == null)
					{
						// 当前对象不存在, 无法访问数组子属性
						return null;
					}
					String prefix = this.getPrefix();
					if (cd.readProcesser != null)
					{
						try
						{
							Object tmpObj = cd.readProcesser.getBeanValue(cd, indexs, thisObj, prefix, this);
							if (tmpObj == null || tmpObj.getClass().isArray())
							{
								// 如果没获得到对象, 或对象还是一个数组, 无法访问子属性
								return null;
							}
							BeanMap sub = BeanTool.getBeanMap(tmpObj, prefix + tmpName + ".");
							return sub.getCellAccessInfo(key.substring(index + 1), needCreate);
						}
						catch (Exception ex) {}
					}
				}
				else if (Collection.class.isAssignableFrom(cd.getCellType()))
				{
					if (indexs == null || indexs.length != 1)
					{
						// 集合容器类型且元素索引个数不为1, 无法访问子属性
						return null;
					}
					Object thisObj = this.getBean();
					if (thisObj == null)
					{
						// 当前对象不存在, 无法访问数组子属性
						return null;
					}
					String prefix = this.getPrefix();
					if (cd.readProcesser != null)
					{
						try
						{
							Object tmpObj = cd.readProcesser.getBeanValue(cd, indexs, thisObj, prefix, this);
							if (tmpObj == null || !BeanTool.checkBean(tmpObj.getClass()))
							{
								// 如果没获得到对象, 或对象不是一个bean
								return null;
							}
							BeanMap sub = BeanTool.getBeanMap(tmpObj, prefix + tmpName + ".");
							return sub.getCellAccessInfo(key.substring(index + 1), needCreate);
						}
						catch (Exception ex) {}
					}
				}
			}
			return null;
		}
		StringRef refName = new StringRef();
		int[] indexs = parseArrayName(key, refName);
		CellDescriptor cd = this.beanDescriptor.getCell(refName.toString());
		if (cd == null || !cd.isValid())
		{
			return null;
		}
		return new CellAccessInfo(this, cd, indexs);
	}

	/**
	 * 清空单元描述的缓存.
	 */
	public void clearCellCache()
	{
		this.entryList = null;
	}

	/**
	 * 解析名称中的数组信息. <p>
	 * 如:
	 * 定义的名称  出参     返回值
	 * tmpName     tmpName  null
	 * tArr[1]     tArr     [1]
	 * arrs[2][3]  arrs     [2, 3]
	 *
	 * @param name      定义的名称
	 * @param pureName  出参, 不包含数组信息的名称定义
	 * @return  数组访问的索引值列表
	 */
	public static int[] parseArrayName(String name, StringRef pureName)
	{
		if (name.charAt(name.length() - 1) == ']')
		{
			int index = name.indexOf('[');
			if (index == -1 || index == 0)
			{
				throw new IllegalArgumentException("Error array visit name:\"" + name + "\".");
			}
			pureName.setString(name.substring(0, index));
			int endIndex = name.indexOf(']', index + 1);
			if (endIndex == name.length() - 1)
			{
				return new int[]{Integer.parseInt(name.substring(index + 1, endIndex))};
			}
			else
			{
				List indexList = new LinkedList();
				indexList.add(new Integer(name.substring(index + 1, endIndex)));
				while (endIndex < name.length() - 1)
				{
					index = name.indexOf('[', endIndex + 1);
					if (index == -1)
					{
						throw new IllegalArgumentException("Error array visit name:\"" + name + "\".");
					}
					endIndex = name.indexOf(']', index + 1);
					indexList.add(new Integer(name.substring(index + 1, endIndex)));
				}
				int tmpI = 0;
				int[] indexs = new int[indexList.size()];
				Iterator itr = indexList.iterator();
				while (itr.hasNext())
				{
					indexs[tmpI++] = ((Integer) itr.next()).intValue();
				}
				return indexs;
			}
		}
		pureName.setString(name);
		return null;
	}

	/**
	 * 获取属性单元描述类的列表.
	 */
	private synchronized List getEntryList(Class[] beanTypeStack)
	{
		List result = this.entryList;
		if (result != null)
		{
			return result;
		}
		if (beanTypeStack == null)
		{
			beanTypeStack = new Class[]{this.getBeanType()};
		}
		else
		{
			Class[] types = new Class[beanTypeStack.length + 1];
			System.arraycopy(beanTypeStack, 0, types, 0, beanTypeStack.length);
			types[beanTypeStack.length] = this.getBeanType();
		}
		result = new LinkedList();
		Iterator cdItr = this.beanDescriptor.getCellIterator();
		while (cdItr.hasNext())
		{
			CellDescriptor cd = (CellDescriptor) cdItr.next();
			if (!cd.isValid())
			{
				continue;
			}
			String pName = cd.getName();
			BeanMap sub = null;
			if (cd.isBeanType() && !this.bean2Map)
			{
				Object thisObj = this.getBean();
				String prefix = this.getPrefix();
				if (thisObj != null && cd.readProcesser != null)
				{
					try
					{
						Object tmpObj = cd.readProcesser.getBeanValue(cd, null, thisObj, prefix, this);
						if (tmpObj != null)
						{
							sub = BeanTool.getBeanMap(tmpObj, prefix + pName + ".");
						}
					}
					catch (Exception ex) {}
				}
				if (sub == null)
				{
					sub = BeanTool.getBeanMap(cd.getCellType(), prefix + pName + ".");
				}
			}
			if (sub != null)
			{
				if (!this.isTypeInStack(beanTypeStack, sub.getBeanType()))
				{
					result.addAll(sub.getEntryList(beanTypeStack));
				}
			}
			else
			{
				result.add(new BeanMapEntry(this, pName, cd));
			}
		}
		this.entryList = result;
		return result;
	}

	/**
	 * 判断给出的类型是否在类型堆栈中, 防止递归的解析
	 */
	private boolean isTypeInStack(Class[] beanTypeStack, Class type)
	{
		if (beanTypeStack == null)
		{
			return false;
		}
		for (int i = 0; i < beanTypeStack.length; i++)
		{
			if (type == beanTypeStack[i])
			{
				return true;
			}
		}
		return false;
	}

	public void putAll(Map t)
	{
		this.setValues(t);
	}

	public boolean containsKey(Object key)
	{
		if (key == null)
		{
			return false;
		}
		return this.getCellAccessInfo(String.valueOf(key), false) != null;
	}

	public Object get(Object key)
	{
		if (key == null)
		{
			return null;
		}
		CellAccessInfo cai = this.getCellAccessInfo(String.valueOf(key), false);
		if (cai == null)
		{
			return null;
		}
		return cai.getValue();
	}

	public Object put(Object key, Object value)
	{
		if (key == null)
		{
			return null;
		}
		CellAccessInfo cai = this.getCellAccessInfo(String.valueOf(key), true);
		if (cai == null)
		{
			return null;
		}
		return cai.setValue(value);
	}

	public Object remove(Object key)
	{
		return this.put(key, null);
	}

	public void clear()
	{
		this.entrySet().clear();
	}

	public Set keySet()
	{
		return new BeanMapEntrySet(this, BEANMAP_SET_TYPE_KEY);
	}

	public Collection values()
	{
		return new BeanMapEntrySet(this, BEANMAP_SET_TYPE_VALUE);
	}

	public Set entrySet()
	{
		return new BeanMapEntrySet(this, BEANMAP_SET_TYPE_ENTRY);
	}

	private static final int BEANMAP_SET_TYPE_ENTRY = 1;
	private static final int BEANMAP_SET_TYPE_VALUE = 2;
	private static final int BEANMAP_SET_TYPE_KEY = 3;

	private static class BeanMapEntrySet extends AbstractSet
			implements Set
	{
		private final int beanMapSetType;
		private final List entryList;

		public BeanMapEntrySet(BeanMap beanMap, int beanMapSetType)
		{
			this.beanMapSetType = beanMapSetType;
			this.entryList = beanMap.getEntryList(null);
		}

		public int size()
		{
			return this.entryList.size();
		}

		public Iterator iterator()
		{
			return new BeanMapIterator(this.beanMapSetType, this.entryList.iterator());
		}

		public boolean add(Object o)
		{
			return false;
		}

		public boolean addAll(Collection c)
		{
			return false;
		}

		public boolean retainAll(Collection c)
		{
			return false;
		}

		public boolean remove(Object o)
		{
			return false;
		}

		public boolean removeAll(Collection c)
		{
			return false;
		}

	}

	private static class BeanMapIterator
			implements Iterator
	{
		private final int beanMapSetType;
		private final Iterator entrySetIterator;
		BeanMapEntry nowEntry = null;

		public BeanMapIterator(int beanMapSetType, Iterator entrySetIterator)
		{
			this.beanMapSetType = beanMapSetType;
			this.entrySetIterator = entrySetIterator;
		}

		public boolean hasNext()
		{
			return this.entrySetIterator.hasNext();
		}

		public Object next()
		{
			this.nowEntry = (BeanMapEntry) this.entrySetIterator.next();
			if (this.beanMapSetType == BEANMAP_SET_TYPE_VALUE)
			{
				return this.nowEntry.getValue();
			}
			else if (this.beanMapSetType == BEANMAP_SET_TYPE_KEY)
			{
				return this.nowEntry.getKey();
			}
			return this.nowEntry;
		}

		public void remove()
		{
			if (this.nowEntry != null)
			{
				this.nowEntry.setValue(null);
			}
		}

	}

	private static class BeanMapEntry
			implements Map.Entry
	{
		private final BeanMap beanMap;
		private final Object key;
		private final CellAccessInfo cellAccessInfo;

		public BeanMapEntry(BeanMap beanMap, Object key, CellDescriptor cellDescriptor)
		{
			this.beanMap = beanMap;
			this.key = key;
			this.cellAccessInfo = new CellAccessInfo(beanMap, cellDescriptor, null);
		}

		public Object getKey()
		{
			String prefix = this.beanMap.getPrefix();
			return prefix.length() == 0 ? this.key : prefix + this.key;
		}

		public Object getValue()
		{
			return this.cellAccessInfo.getValue();
		}

		public Object setValue(Object value)
		{
			return this.cellAccessInfo.setValue(value);
		}

		public int hashCode()
		{
			Object key = this.getKey();
			Object value = this.getValue();
			return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
		}

		public boolean equals(Object obj)
		{
			if (obj instanceof Map.Entry)
			{
				Map.Entry e = (Map.Entry) obj;
				Object k1 = this.getKey();
				Object k2 = e.getKey();
				if (k1 == k2 || (k1 != null && k1.equals(k2)))
				{
					Object v1 = this.getValue();
					Object v2 = e.getValue();
					if (v1 == v2 || (v1 != null && v1.equals(v2)))
					{
						return true;
					}
				}
			}
			return false;
		}

		public String toString()
		{
			return this.getKey() + "=" + this.getValue();
		}

	}

}