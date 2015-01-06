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

package self.micromagic.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Date;

public class FormatTool
{
	/**
	 * 完整的日期类型的格式化.
	 */
	public static final DateFormat dateFullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S Z");

	private static final DateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private static final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	private static final NumberFormat currencyFormat = new DecimalFormat("#0.00");
	private static final NumberFormat currency2Format = new DecimalFormat("#,##0.00");

	/**
	 * 格式化输出当前的日期-时间
	 */
	public static String getCurrentDatetimeString()
	{
		return datetimeFormat.format(new Date());
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
	 * 格式化输出某个日期-时间
	 */
	public static String formatDatetime(Object datetime)
	{
		return datetime == null ? "" : datetimeFormat.format(datetime);
	}

	/**
	 * 格式化输出某个日期
	 */
	public static String formatDate(Object date)
	{
		return date == null ? "" : dateFormat.format(date);
	}

	/**
	 * 格式化输出某个时间
	 */
	public static String formatTime(Object time)
	{
		return time == null ? "" : timeFormat.format(time);
	}

	/**
	 * 将某个字符串按日期-时间的格式解析成Date
	 */
	public static Date parseDatetime(String str)
			throws ParseException
	{
		return str == null ? null : datetimeFormat.parse(str);
	}

	/**
	 * 将某个字符串按日期的格式解析成Date
	 */
	public static Date parseDate(String str)
			throws ParseException
	{
		return str == null ? null : dateFormat.parse(str);
	}

	/**
	 * 将某个字符串按时间的格式解析成Date
	 */
	public static Date parseTime(String str)
			throws ParseException
	{
		return str == null ? null : timeFormat.parse(str);
	}

}