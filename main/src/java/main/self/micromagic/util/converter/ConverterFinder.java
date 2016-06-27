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
			// 如果为空但类型是boolean或int, 则说明在初始化过程中又调用了此方法, 所以直接构造一个
			if (c == int.class)
			{
				vc = new IntegerConverter();
				vc.setNeedThrow(true);
			}
			else if (c == boolean.class)
			{
				vc = new BooleanConverter();
				vc.setNeedThrow(true);
			}
			return vc;
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
	 * 判断finder是否已初始化完成.
	 */
	static boolean isInitialized()
	{
		return initialized;
	}
	private static boolean initialized;

	/**
	 * 重新初始化转换器的缓存.
	 */
	static void reInitCache()
	{
		Map converter = new HashMap();
		Map withoutThrow = new HashMap();
		initCache(converter, withoutThrow);
		converterCache = converter;
		withoutThrowCache = withoutThrow;
	}

	/**
	 * 默认的ValueConverter对象缓存.
	 */
	private static Map converterCache = new HashMap();

	/**
	 * isNeedThrow值为false的ValueConverter对象缓存.
	 */
	private static Map withoutThrowCache = new HashMap();

	/**
	 * 初始化转换器的缓存.
	 */
	private static void initCache(Map converter, Map withoutThrow)
	{
		ValueConverter tmp;

		tmp = new BooleanConverter();
		tmp.setNeedThrow(true);
		converter.put(boolean.class, tmp);
		converter.put(Boolean.class, tmp);
		tmp = new ByteConverter();
		tmp.setNeedThrow(true);
		converter.put(byte.class, tmp);
		converter.put(Byte.class, tmp);
		tmp = new CharacterConverter();
		tmp.setNeedThrow(true);
		converter.put(char.class, tmp);
		converter.put(Character.class, tmp);
		tmp = new ShortConverter();
		tmp.setNeedThrow(true);
		converter.put(short.class, tmp);
		converter.put(Short.class, tmp);
		tmp = new IntegerConverter();
		tmp.setNeedThrow(true);
		converter.put(int.class, tmp);
		converter.put(Integer.class, tmp);
		tmp = new LongConverter();
		tmp.setNeedThrow(true);
		converter.put(long.class, tmp);
		converter.put(Long.class, tmp);
		tmp = new FloatConverter();
		tmp.setNeedThrow(true);
		converter.put(float.class, tmp);
		converter.put(Float.class, tmp);
		tmp = new DoubleConverter();
		tmp.setNeedThrow(true);
		converter.put(double.class, tmp);
		converter.put(Double.class, tmp);

		tmp = new ObjectConverter();
		tmp.setNeedThrow(true);
		converter.put(Object.class, tmp);
		tmp = new StringConverter();
		tmp.setNeedThrow(true);
		converter.put(String.class, tmp);
		tmp = new BigIntegerConverter();
		tmp.setNeedThrow(true);
		converter.put(BigInteger.class, tmp);
		tmp = new DecimalConverter();
		tmp.setNeedThrow(true);
		converter.put(BigDecimal.class, tmp);
		tmp = new BytesConverter();
		tmp.setNeedThrow(true);
		converter.put(byte[].class, tmp);
		tmp = new TimeConverter();
		tmp.setNeedThrow(true);
		converter.put(java.sql.Time.class, tmp);
		tmp = new DateConverter();
		tmp.setNeedThrow(true);
		converter.put(java.sql.Date.class, tmp);
		tmp = new TimestampConverter();
		tmp.setNeedThrow(true);
		converter.put(java.sql.Timestamp.class, tmp);
		tmp = new UtilDateConverter();
		tmp.setNeedThrow(true);
		converter.put(java.util.Date.class, tmp);
		tmp = new CalendarConverter();
		tmp.setNeedThrow(true);
		converter.put(java.util.Calendar.class, tmp);
		tmp = new StreamConverter();
		tmp.setNeedThrow(true);
		converter.put(InputStream.class, tmp);
		tmp = new ReaderConverter();
		tmp.setNeedThrow(true);
		converter.put(Reader.class, tmp);
		tmp = new LocaleConverter();
		tmp.setNeedThrow(true);
		converter.put(Locale.class, tmp);
		tmp = new MapConverter();
		tmp.setNeedThrow(true);
		converter.put(Map.class, tmp);

		Iterator itr = converter.entrySet().iterator();
		while (itr.hasNext())
		{
			Map.Entry entry = (Map.Entry) itr.next();
			ValueConverter vc = (ValueConverter) entry.getValue();
			vc = vc.copy();
			vc.setNeedThrow(false);
			withoutThrow.put(entry.getKey(), vc);
		}
	}

	static
	{
		initCache(converterCache, withoutThrowCache);
		initialized = true;
	}

}