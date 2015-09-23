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

package self.micromagic.util.logging;

/**
 * 时间记录器.
 *
 * @author micromagic@sina.com
 */
public class TimeLogger
{
	/**
	 * 将一个时间格式化成毫秒显示时的小数精度.
	 */
	private int precision = 3;

	/**
	 * 起始时间, 格式化时将减去此时间再输出.
	 */
	private long beginTime;

	/**
	 * 构造一个时间记录器.
	 */
	public TimeLogger()
	{
		this.beginTime = instance.getTime();
	}

	/**
	 * 构造一个时间记录器.
	 *
	 * @param precision  格式化时小数部分的精度, 即保留几位小数
	 */
	public TimeLogger(int precision)
	{
		this();
		this.precision = precision;
	}

	/**
	 * 格式化输出经过的时间, 单位为毫秒.
	 *
	 * @param reset  输出完后是否需要重置起始时间
	 */
	public String formatPassTime(boolean reset)
	{
		String r = instance.formatMillionSecond(getTime() - this.beginTime, this.precision);
		if (reset)
		{
			this.reset();
		}
		return r;
	}

	/**
	 * 获取经过的时间, 单位为毫秒.
	 *
	 * @param reset  获取完后是否需要重置起始时间
	 */
	public long getPassTime(boolean reset)
	{
		long t = instance.getMillionSecond(getTime() - this.beginTime);
		if (reset)
		{
			this.reset();
		}
		return t;
	}

	/**
	 * 重置起始时间.
	 */
	public void reset()
	{
		this.beginTime = instance.getTime();
	}

	/**
	 * 获取当前时间, 将根据jdk版本获取毫秒或纳秒.
	 */
	public static long getTime()
	{
		return instance.getTime();
	}

	/**
	 * 将一个(经过的)时间格式化成毫秒显示, 如果是纳秒将显示小数部分(保留3位).
	 *
	 * @param time  要格式化的时间
	 *              此参数必须是通过getTime方法获取的两个时间之差
	 * @see #getTime
	 */
	public static String formatPassTime(long time)
	{
		return instance.formatMillionSecond(time, 3);
	}

	/**
	 * 将一个(经过的)时间格式化成毫秒显示, 如果是纳秒将显示小数部分.
	 *
	 * @param time       要格式化的时间
	 *                   此参数必须是通过getTime方法获取的两个时间之差
	 * @param precision  小数部分的精度, 即保留几位小数, 最多6位.
	 */
	public static String formatPassTime(long time, int precision)
	{
		return instance.formatMillionSecond(time, precision);
	}

	/**
	 * 获取经过的时间, 单位为毫秒.
	 *
	 * @param time  要格式化的时间
	 *              此参数必须是通过getTime方法获取的两个时间之差
	 */
	public static long getPassTime(long time)
	{
		return instance.getMillionSecond(time);
	}

	private static TimeGetter instance;

	static
	{
		try
		{
			System.class.getMethod("nanoTime", new Class[0]);
			String nanoClassName = "self.micromagic.util.logging.TimeLogger$NanoTime";
			instance = (TimeGetter) Class.forName(nanoClassName).newInstance();
		}
		catch (Throwable ex)
		{
			instance = new MillionSecond();
		}
	}

	/**
	 * 用于获取当前时间及毫秒的格式化显示.
	 */
	interface TimeGetter
	{
		/**
		 * 获取当前时间, 将根据jdk版本获取毫秒或纳秒.
		 */
		public long getTime();

		/**
		 * 将一个(经过的)时间格式化成毫秒显示, 如果是纳秒将显示小数部分.
		 *
		 * @param time       要格式化的时间
		 * @param precision  小数部分的精度, 及保留几位小数, 最多6位.
		 */
		public String formatMillionSecond(long time, int precision);

		/**
		 * 将一个时间保留到毫秒返回.
		 *
		 * @param time  经过的时间
		 */
		public long getMillionSecond(long time);

	}

	static class MillionSecond
			implements TimeGetter
	{
		public long getTime()
		{
			return System.currentTimeMillis();
		}

		public String formatMillionSecond(long time, int precision)
		{
			return Long.toString(time);
		}

		public long getMillionSecond(long time)
		{
			return time;
		}

	}

}