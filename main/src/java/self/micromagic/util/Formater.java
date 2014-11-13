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
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.NumberFormat;
import java.util.Date;

/**
 * @deprecated
 * @see FormatTool
 */
public class Formater
{
	private static DateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private static DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	private static NumberFormat currencyFormat = new DecimalFormat("#0.00");
	private static NumberFormat currency2Format = new DecimalFormat("#,##0.00");

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
		return datetimeFormat.format(datetime);
	}

	/**
	 * 格式化输出某个日期
	 */
	public static String formatDate(Object datetime)
	{
		return dateFormat.format(datetime);
	}

	/**
	 * 格式化输出某个时间
	 */
	public static String formatTime(Object time)
	{
		return timeFormat.format(time);
	}

	/**
	 * 将某个字符串按日期-时间的格式解析成Date
	 */
	public static Date parseDatetime(String str)
			throws ParseException
	{
		return datetimeFormat.parse(str);
	}

	/**
	 * 将某个字符串按日期的格式解析成Date
	 */
	public static Date parseDate(String str)
			throws ParseException
	{
		return dateFormat.parse(str);
	}

	/**
	 * 将某个字符串按时间的格式解析成Date
	 */
	public static Date parseTime(String str)
			throws ParseException
	{
		return timeFormat.parse(str);
	}

	/**
	 * @deprecated As of eterna 1.0.0,
	 * replaced by <code>parseDatetime</code>.
	 */
	public static Date parserDatetime(String str)
			throws ParseException
	{
		return datetimeFormat.parse(str);
	}

	/**
	 * @deprecated As of eterna 1.0.0,
	 * replaced by <code>parseDate</code>.
	 */
	public static Date parserDate(String str)
			throws ParseException
	{
		return dateFormat.parse(str);
	}

	/**
	 * @deprecated As of eterna 1.0.0,
	 * replaced by <code>parseTime</code>.
	 */
	public static Date parserTime(String str)
			throws ParseException
	{
		return timeFormat.parse(str);
	}

}