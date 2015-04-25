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

package self.micromagic.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import self.micromagic.cg.BeanDescriptor;
import self.micromagic.cg.BeanPropertyReader;
import self.micromagic.cg.BeanPropertyWriter;
import self.micromagic.cg.BeanTool;
import self.micromagic.cg.CellDescriptor;
import self.micromagic.eterna.dao.Entity;
import self.micromagic.eterna.dao.EntityItem;
import self.micromagic.eterna.dao.impl.EntityImpl;
import self.micromagic.eterna.dao.impl.EntityItemGenerator;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.FactoryContainer;

/**
 * 将一个bo对象解析为Entity对象.
 */
public class BeanEntity extends AbstractGenerator
		implements Entity
{
	private boolean initialized;
	private final EntityImpl base = new EntityImpl();

	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		if (this.initialized)
		{
			return true;
		}
		if (this.beanClass == null)
		{
			throw new EternaException("Attribute beanClass hasn't setted.");
		}
		this.initialized = true;
		ClassLoader loader = (ClassLoader) factory.getFactoryContainer()
				.getAttribute(FactoryContainer.CLASSLOADER_FLAG);
		if (loader == null)
		{
			loader = this.getClass().getClassLoader();
		}
		try
		{
			Class<?> c = Class.forName(this.beanClass, true, loader);
			// 解析bo的属性
			BeanDescriptor bd = BeanTool.getBeanDescriptor(c);
			@SuppressWarnings("rawtypes")
			Iterator itr = bd.getCellIterator();
			while (itr.hasNext())
			{
				CellDescriptor cd = (CellDescriptor) itr.next();
				BeanPropertyReader r = cd.getReadProcesser();
				BeanPropertyWriter w = cd.getWriteProcesser();
				if (r != null && w != null)
				{
					Annotation a = null;
					Class<?> mType = null;
					// 解析field中的标注
					Field f = BeanTool.getField(c, cd.getName());
					if (f != null && f.getType() == cd.getCellType())
					{
						a = this.getAnnotation(f);
						mType = f.getType();
					}
					else
					{
						mType = cd.getCellType();
					}
					if (a == null)
					{
						// 解析get成员中的标注
						Member m = r.getMember();
						a = this.getAnnotation(m);
					}
					if (a == null)
					{
						// 解析set成员中的标注
						Member m = w.getMember();
						a = this.getAnnotation(m);
					}
					if (a == null || COLUMN_ANNOTATION.isInstance(a))
					{
						String rName = cd.getName();
						String colName = rName;
						if (a != null)
						{
							colName = getColumnName(a);
						}
						String tName = typeDictionary.get(mType);
						if (tName == null)
						{
							tName = "Object";
						}
						EntityItemGenerator itemG = new EntityItemGenerator();
						itemG.setFactory(factory);
						itemG.setColumnName(colName);
						itemG.setName(tName);
						itemG.setType(tName);
						this.base.addItem((EntityItem) itemG.create());
					}
				}
			}
		}
		catch (Exception ex)
		{
			throw new EternaException(ex);
		}
		return this.base.initialize(factory);
	}

	/**
	 * 类型到类型名称的字典表.
	 */
	@SuppressWarnings("rawtypes")
	private static Map<Class, String> typeDictionary
			= new HashMap<Class, String>();
	static
	{
		typeDictionary.put(String.class, "String");
		typeDictionary.put(boolean.class, "boolean");
		typeDictionary.put(Boolean.class, "boolean");
		typeDictionary.put(byte.class, "byte");
		typeDictionary.put(Byte.class, "byte");
		typeDictionary.put(short.class, "short");
		typeDictionary.put(Short.class, "short");
		typeDictionary.put(int.class, "int");
		typeDictionary.put(Integer.class, "int");
		typeDictionary.put(long.class, "long");
		typeDictionary.put(Long.class, "long");
		typeDictionary.put(float.class, "float");
		typeDictionary.put(Float.class, "float");
		typeDictionary.put(double.class, "double");
		typeDictionary.put(Double.class, "double");
		typeDictionary.put(byte[].class, "Bytes");
		typeDictionary.put(java.sql.Date.class, "Date");
		typeDictionary.put(java.sql.Time.class, "Time");
		typeDictionary.put(java.sql.Timestamp.class, "Datetime");
		typeDictionary.put(java.util.Date.class, "Datetime");
		typeDictionary.put(java.io.InputStream.class, "Stream");
		typeDictionary.put(java.io.Reader.class, "Reader");
	}

	/**
	 * 获取成员中的持久化标注.
	 */
	private Annotation getAnnotation(Member m)
	{
		Annotation r = null;
		if (m instanceof Method)
		{
			r = ((Method) m).getAnnotation(TRANSIENT_ANNOTATION);
			if (r == null)
			{
				r = ((Method) m).getAnnotation(COLUMN_ANNOTATION);
			}
		}
		else if (m instanceof Field)
		{
			r = ((Field) m).getAnnotation(TRANSIENT_ANNOTATION);
			if (r == null)
			{
				r = ((Field) m).getAnnotation(COLUMN_ANNOTATION);
			}
		}
		return r;
	}

	@Override
	public void setName(String name)
	{
		this.base.setName(name);
	}

	@Override
	public String getName()
			throws EternaException
	{
		return this.base.getName();
	}

	public EternaFactory getFactory()
			throws EternaException
	{
		return this.base.getFactory();
	}

	public Object create()
			throws EternaException
	{
		return this;
	}

	public String getOrder()
			throws EternaException
	{
		return this.base.getOrder();
	}
	public void setOrder(String order)
	{
		this.base.setOrder(order);
	}

	public int getItemCount()
			throws EternaException
	{
		return this.getItemCount();
	}

	public EntityItem getItem(String name)
			throws EternaException
	{
		return this.getItem(name);
	}

	public EntityItem getItem(int index)
			throws EternaException
	{
		return this.getItem(index);
	}

	@SuppressWarnings("rawtypes")
	public Iterator getItemIterator()
			throws EternaException
	{
		return this.getItemIterator();
	}

	public void setBeanClass(String beanClass)
	{
		this.beanClass = beanClass;
	}
	private String beanClass;

	/**
	 * 从列标注中获取列名.
	 */
	private static String getColumnName(Annotation column)
	{
		return "";
	}

	/**
	 * 不需要持久化的标注.
	 */
	private static Class<? extends Annotation> TRANSIENT_ANNOTATION;
	/**
	 * 列配置的标注.
	 */
	private static Class<? extends Annotation> COLUMN_ANNOTATION;

}