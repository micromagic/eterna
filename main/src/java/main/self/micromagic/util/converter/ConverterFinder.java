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

package self.micromagic.util.converter;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import self.micromagic.cg.BeanTool;

/**
 * ValueConverter对象的查找器, 可通过给出的Class找寻对应的
 * ValueConverter对象.
 * 通过findConverter方法取出的所有ValueConverter对象的
 * isNeedThrow值默认为true, 如果需要无法转换是不抛出异常,
 * 可使用findConverterWithoutThrow方法来获取.
 *
 * @see ValueConverter#isNeedThrow
 */
public class ConverterFinder
{
	/**
	 * 查找一个对应的ValueConverter对象.
	 *
	 * @param c  对应的Class
	 * @return  查到的ValueConverter对象, 如果未查到则返回null
	 */
	public static ValueConverter findConverter(Class c)
	{
		return findConverter(c, true);
	}

	/**
	 * 查找一个对应的ValueConverter对象.
	 * 此ValueConverter对象的isNeedThrow值默认为false
	 *
	 * @param c     对应的Class
	 * @param copy  是否需要复制ValueConverter对象
	 * @return  查到的ValueConverter对象, 如果未查到则返回null
	 */
	public static ValueConverter findConverterWithoutThrow(Class c, boolean copy)
	{
		ValueConverter vc = (ValueConverter) withoutThrowCache.get(c);
		if (vc == null)
		{
			return null;
		}
		return copy ? vc.copy() : vc;
	}

	/**
	 * 查找一个对应的ValueConverter对象.
	 *
	 * @param c     对应的Class
	 * @param copy  是否需要复制ValueConverter对象
	 * @return  查到的ValueConverter对象, 如果未查到则返回null
	 */
	public static ValueConverter findConverter(Class c, boolean copy)
	{
		ValueConverter vc = (ValueConverter) converterCache.get(c);
		if (vc == null)
		{
			return null;
		}
		return copy ? vc.copy() : vc;
	}

	/**
	 * 注册一个ValueConverter对象.
	 *
	 * @param c          对应的Class
	 * @param converter  ValueConverter对象, 如果给出的值为null, 表示
	 *                   删除已注册的ValueConverter对象
	 */
	public static void registerConverter(Class c, ValueConverter converter)
	{
		registerConverter(c, converter, false);
	}

	/**
	 * 注册一个ValueConverter对象.
	 *
	 * @param c           对应的Class
	 * @param converter   ValueConverter对象, 如果给出的值为null, 表示
	 *                    删除已注册的ValueConverter对象
	 * @param toBeanTool  是否需要同时注册到BeanTool中
	 * @see BeanTool#registerConverter(Class, ValueConverter)
	 */
	public static void registerConverter(Class c, ValueConverter converter, boolean toBeanTool)
	{
		if (converter == null)
		{
			withoutThrowCache.remove(c);
			converterCache.remove(c);
			return;
		}
		if (toBeanTool)
		{
			BeanTool.registerConverter(c, converter.copy());
		}
		// 将isNeedThrow值设为false注册
		ValueConverter vc1 = converter.copy();
		vc1.setNeedThrow(false);
		withoutThrowCache.put(c, vc1);
		// 将isNeedThrow值设为true注册
		ValueConverter vc2 = converter.copy();
		vc2.setNeedThrow(true);
		converterCache.put(c, vc2);
	}

	/**
	 * 默认的ValueConverter对象缓存.
	 */
	private static Map converterCache = new HashMap();

	/**
	 * isNeedThrow值为false的ValueConverter对象缓存.
	 */
	private static Map withoutThrowCache = new HashMap();

	static
	{
		ValueConverter converter;

		converter = new BooleanConverter();
		converter.setNeedThrow(true);
		converterCache.put(boolean.class, converter);
		converterCache.put(Boolean.class, converter);
		converter = new ByteConverter();
		converter.setNeedThrow(true);
		converterCache.put(byte.class, converter);
		converterCache.put(Byte.class, converter);
		converter = new CharacterConverter();
		converter.setNeedThrow(true);
		converterCache.put(char.class, converter);
		converterCache.put(Character.class, converter);
		converter = new ShortConverter();
		converter.setNeedThrow(true);
		converterCache.put(short.class, converter);
		converterCache.put(Short.class, converter);
		converter = new IntegerConverter();
		converter.setNeedThrow(true);
		converterCache.put(int.class, converter);
		converterCache.put(Integer.class, converter);
		converter = new LongConverter();
		converter.setNeedThrow(true);
		converterCache.put(long.class, converter);
		converterCache.put(Long.class, converter);
		converter = new FloatConverter();
		converter.setNeedThrow(true);
		converterCache.put(float.class, converter);
		converterCache.put(Float.class, converter);
		converter = new DoubleConverter();
		converter.setNeedThrow(true);
		converterCache.put(double.class, converter);
		converterCache.put(Double.class, converter);

		converter = new ObjectConverter();
		converter.setNeedThrow(true);
		converterCache.put(Object.class, converter);
		converter = new StringConverter();
		converter.setNeedThrow(true);
		converterCache.put(String.class, converter);
		converter = new BigIntegerConverter();
		converter.setNeedThrow(true);
		converterCache.put(BigInteger.class, converter);
		converter = new DecimalConverter();
		converter.setNeedThrow(true);
		converterCache.put(BigDecimal.class, converter);
		converter = new BytesConverter();
		converter.setNeedThrow(true);
		converterCache.put(byte[].class, converter);
		converter = new TimeConverter();
		converter.setNeedThrow(true);
		converterCache.put(java.sql.Time.class, converter);
		converter = new DateConverter();
		converter.setNeedThrow(true);
		converterCache.put(java.sql.Date.class, converter);
		converter = new TimestampConverter();
		converter.setNeedThrow(true);
		converterCache.put(java.sql.Timestamp.class, converter);
		converter = new UtilDateConverter();
		converter.setNeedThrow(true);
		converterCache.put(java.util.Date.class, converter);
		converter = new CalendarConverter();
		converter.setNeedThrow(true);
		converterCache.put(java.util.Calendar.class, converter);
		converter = new StreamConverter();
		converter.setNeedThrow(true);
		converterCache.put(InputStream.class, converter);
		converter = new ReaderConverter();
		converter.setNeedThrow(true);
		converterCache.put(Reader.class, converter);
		converter = new LocaleConverter();
		converter.setNeedThrow(true);
		converterCache.put(Locale.class, converter);
		converter = new MapConverter();
		converter.setNeedThrow(true);
		converterCache.put(Map.class, converter);

		Iterator itr = converterCache.entrySet().iterator();
		while (itr.hasNext())
		{
			Map.Entry entry = (Map.Entry) itr.next();
			ValueConverter vc = (ValueConverter) entry.getValue();
			vc = vc.copy();
			vc.setNeedThrow(false);
			withoutThrowCache.put(entry.getKey(), vc);
		}
	}

}