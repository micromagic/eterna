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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import self.micromagic.util.container.SynHashMap;
import self.micromagic.util.container.ThreadCache;
import self.micromagic.util.container.WeakIdentityMap;

/**
 * 处理格式化的工具. <p>
 * 如: 对日期或数字的格式化输出, 或将字符串解析成日期或数字.
 */
public class FormatTool
{
	/**
	 * 完整的日期类型的格式化.
	 */
	public static final DateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");

	private static final DateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private static final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	private static final NumberFormat currencyFormat = new DecimalFormat("#0.00");
	private static final NumberFormat currency2Format = new DecimalFormat("#,##0.00");

	/**
	 * 存放缓存的format对象.
	 */
	private static Map formatCache = new SynHashMap();

	private FormatTool()
	{
	}

	/**
	 * 获取缓存的format对象.
	 */
	public static Format getCachedFormat(String pattern)
	{
		Format format = (Format) formatCache.get(pattern);
		if (format == null)
		{
			format = createFormat(pattern);
		}
		return format;
	}
	private static Format createFormat(String pattern)
	{
		synchronized (formatCache)
		{
			Format format = (Format) formatCache.get(pattern);
			if (format != null)
			{
				return format;
			}
			if (pattern == null)
			{
				throw new NullPointerException("The pattern is null.");
			}
			boolean numFormat = false;
			int len = pattern.length();
			for (int i = 0; i < len; i++)
			{
				char c = pattern.charAt(i);
				if (c == '0' || c == '#')
				{
					// 有"0"或"#"可以判断为数字的格式化模式
					numFormat = true;
					break;
				}
			}
			if (numFormat)
			{
				format = new DecimalFormat(pattern);
			}
			else
			{
				format = new SimpleDateFormat(pattern);
			}
			formatCache.put(pattern, format);
			return format;
		}
	}

	/**
	 * 将一个format对象放入缓存.
	 */
	public static void putFormat(String key, Format format)
	{
		synchronized (formatCache)
		{
			formatCache.put(key, format);
		}
	}

	/**
	 * 在线程中存放Format实例的缓存.
	 */
	private static final String THREAD_FORMAT_KEY = "format.thread.cache";

	/**
	 * 根据给出的Format对象, 在当前线程中克隆一个实例.
	 */
	public static Format getThreadFormat(Format format)
	{
		return getThreadFormat0(format);
	}
	private static Format getThreadFormat0(Format format)
	{
		ThreadCache cache = ThreadCache.getInstance();
		Map fCache = (Map) cache.getProperty(THREAD_FORMAT_KEY);
		if (fCache == null)
		{
			fCache = new WeakIdentityMap();
			cache.setProperty(THREAD_FORMAT_KEY, fCache);
		}
		Format result = (Format) fCache.get(format);
		if (result == null)
		{
			result = (Format) format.clone();
			fCache.put(format, result);
		}
		return result;
	}

	/**
	 * 根据给出的DateFormat对象, 在当前线程中克隆一个实例.
	 */
	public static DateFormat getThreadFormat(DateFormat format)
	{
		return (DateFormat) getThreadFormat0(format);
	}

	/**
	 * 根据给出的NumberFormat对象, 在当前线程中克隆一个实例.
	 */
	public static NumberFormat getThreadFormat(NumberFormat format)
	{
		return (NumberFormat) getThreadFormat0(format);
	}

	/**
	 * 格式化输出当前的日期-时间
	 */
	public static String getCurrentDatetimeString()
	{
		return getThreadFormat(datetimeFormat).format(new Date());
	}

	/**
	 * 将一个double按货币的格式输出(保留2位小数)
	 */
	public static String formatCurrency(double number)
	{
		return currencyFormat.format(number);
	}

	/**
	 * 将一个double按货币的格式输出(保留2位小数, 并加上千分位)
	 */
	public static String formatCurrency2(double number)
	{
		return currency2Format.format(number);
	}

	/**
	 * 格式化输出完整的日期-时间, 包括毫秒和时区
	 */
	public static String formatFullDate(Object datetime)
	{
		return datetime == null ? "" : getThreadFormat(fullDateFormat).format(datetime);
	}

	/**
	 * 格式化输出某个日期-时间
	 */
	public static String formatDatetime(Object datetime)
	{
		return datetime == null ? "" : getThreadFormat(datetimeFormat).format(datetime);
	}

	/**
	 * 格式化输出某个日期
	 */
	public static String formatDate(Object date)
	{
		return date == null ? "" : getThreadFormat(dateFormat).format(date);
	}

	/**
	 * 格式化输出某个时间
	 */
	public static String formatTime(Object time)
	{
		return time == null ? "" : getThreadFormat(timeFormat).format(time);
	}

	/**
	 * 将某个字符串按日期-时间的格式解析成Date
	 */
	public static Date parseDatetime(String str)
			throws ParseException
	{
		return str == null ? null : getThreadFormat(datetimeFormat).parse(str);
	}

	/**
	 * 将某个字符串按日期的格式解析成Date
	 */
	public static Date parseDate(String str)
			throws ParseException
	{
		return str == null ? null : getThreadFormat(dateFormat).parse(str);
	}

	/**
	 * 将某个字符串按时间的格式解析成Date
	 */
	public static Date parseTime(String str)
			throws ParseException
	{
		return str == null ? null : getThreadFormat(timeFormat).parse(str);
	}

}
