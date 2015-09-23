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

package self.micromagic.eterna.dao.preparer;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import self.micromagic.cg.ClassGenerator;
import self.micromagic.eterna.dao.reader.ObjectReader;
import self.micromagic.eterna.digester2.ParseException;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.Factory;
import self.micromagic.eterna.share.Tool;
import self.micromagic.eterna.share.TypeManager;

/**
 * prepare对象创建者的构造器和管理类.
 */
public class CreaterManager extends AbstractGenerator
{
	public void setType(String type)
	{
		this.type = type;
	}
	private String type;

	public void setPattern(String pattern)
	{
		this.pattern = pattern;
	}
	private String pattern;

	public Object create()
			throws EternaException
	{
		PreparerCreater creater = createPreparerCreater(this.type, this.name);
		if (this.pattern != null)
		{
			creater.setPattern(this.pattern);
		}
		((AbstractPreparerCreater) creater).setAttributes(this.attributes);
		return creater;
	}


	/**
	 * 根据给出的类型id或值准备器创建者的名称创建一个PreparerCreater.
	 *
	 * @param type         类型的id
	 * @param prepareName  值准备器创建者的名称
	 * @see TypeManager
	 */
	public static PreparerCreater createPreparerCreater(int type, String prepareName,
			EternaFactory factory)
			throws EternaException
	{
		if (prepareName == null)
		{
			return CreaterManager.createPreparerCreater(
					TypeManager.getPureTypeName(type), null, factory);
		}
		else
		{
			String checkStr = Tool.PATTERN_PREFIX;
			if (prepareName.startsWith(checkStr))
			{
				return CreaterManager.createPreparerCreater(
						TypeManager.getPureTypeName(type),
						prepareName.substring(checkStr.length()), factory);
			}
			else
			{
				return factory.getPrepare(prepareName);
			}
		}
	}

	/**
	 * 创建一个空值的ValuePreparer.
	 *
	 * @param type  类型id
	 * @see TypeManager
	 */
	public static ValuePreparer createNullPreparer(EternaFactory factory, int type)
			throws EternaException
	{
		NullCreater nc = createNullCreater(factory);
		return nc.createPreparer(TypeManager.getSQLType(type));
	}

	/**
	 * 创建一个NullCreater.
	 *
	 * @see TypeManager
	 */
	static NullCreater createNullCreater(EternaFactory factory)
			throws EternaException
	{
		Map createrCache = getCreaterCache(factory);
		String name = "null";
		NullCreater creater = (NullCreater) createrCache.get(name);
		if (creater != null)
		{
			return creater;
		}
		creater = new NullCreater(name);
		creater.initialize(factory);
		createrCache.put(name, creater);
		return creater;
	}

	/**
	 * 根据给出的类型及模式创建一个PreparerCreater.
	 *
	 * @param type  类型的名称
	 * @see TypeManager
	 */
	public static PreparerCreater createPreparerCreater(String type, String pattern,
			EternaFactory factory)
			throws EternaException
	{
		Map createrCache = getCreaterCache(factory);
		String name = type;
		if (pattern != null)
		{
			name = name.concat(".").concat(pattern);
		}
		PreparerCreater creater = (PreparerCreater) createrCache.get(name);
		if (creater != null)
		{
			return creater;
		}
		CreaterManager cm = new CreaterManager();
		cm.setType(type);
		cm.setName("_auto");
		if (pattern != null)
		{
			cm.setPattern(pattern);
		}
		creater = (PreparerCreater) cm.create();
		creater.initialize(factory);
		createrCache.put(name, creater);
		return creater;
	}

	/**
	 * 从工厂属性中获取PreparerCreater的缓存.
	 */
	private static Map getCreaterCache(Factory factory)
	{
		Map createrCache = (Map) factory.getAttribute(ATTR_CREATER);
		if (createrCache == null)
		{
			createrCache = new HashMap();
			factory.setAttribute(ATTR_CREATER, createrCache);
		}
		return createrCache;
	}
	/**
	 * 在factory的属性中存放自动创建的PreparerCreater的缓存的键值.
	 */
	public static final String ATTR_CREATER = "__f_attr.PreparerCreater";

	private static Map typeClassMap = new HashMap();
	static
	{
		typeClassMap.put("String", StringCreater.class);
		typeClassMap.put("boolean", BooleanCreater.class);
		typeClassMap.put("byte", ByteCreater.class);
		typeClassMap.put("short", ShortCreater.class);
		typeClassMap.put("int", IntegerCreater.class);
		typeClassMap.put("long", LongCreater.class);
		typeClassMap.put("float", FloatCreater.class);
		typeClassMap.put("double", DoubleCreater.class);
		typeClassMap.put("Bytes", BytesCreater.class);
		typeClassMap.put("Date", DateCreater.class);
		typeClassMap.put("Time", TimeCreater.class);
		typeClassMap.put("Datetime", TimestampCreater.class);
		typeClassMap.put("Timestamp", TimestampCreater.class);
		typeClassMap.put("BigString", StringCreater.class);
		typeClassMap.put("Stream", StreamCreater.class);
		typeClassMap.put("Chars", CharsCreater.class);
		typeClassMap.put("Blob", BlobCreater.class);
		typeClassMap.put("Clob", ClobCreater.class);
	}

	static PreparerCreater createPreparerCreater(String type, String name)
			throws EternaException
	{
		Class c = (Class) CreaterManager.typeClassMap.get(type);
		if (c == null)
		{
			throw new ParseException("Can't create [PreparerCreater] type:" + type + ".");
		}
		return CreaterManager.createPreparerCreater(c, name);
	}

	static PreparerCreater createPreparerCreater(Class type, String name)
			throws EternaException
	{
		if (type == null)
		{
			throw new NullPointerException();
		}
		if (!PreparerCreater.class.isAssignableFrom(type))
		{
			throw new ParseException(ClassGenerator.getClassName(type)
					+ " is not instance of " + ClassGenerator.getClassName(ObjectReader.class));
		}
		try
		{
			Constructor ct = type.getConstructor(new Class[]{String.class});
			return (PreparerCreater) ct.newInstance(new Object[]{name});
		}
		catch (Exception ex)
		{
			Tool.log.warn("create PreparerCreater:" + name, ex);
			throw new ParseException("Can't create [PreparerCreater] class:"
					+ ClassGenerator.getClassName(type) + ".");
		}
	}

}