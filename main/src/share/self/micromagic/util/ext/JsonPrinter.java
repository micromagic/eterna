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

package self.micromagic.util.ext;

import java.io.Writer;
import java.io.IOException;

import self.micromagic.eterna.view.impl.DataPrinterImpl;
import self.micromagic.eterna.view.impl.StringCoderImpl;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.util.Utility;

/**
 * 将一个对象或数据类型以json的格式输出.
 *
 * @see DataPrinterImpl
 */
public class JsonPrinter
{
	/**
	 * 使用<code>DataPrinterImpl</code>进行对象->Json字符串的转换.
	 */
	private static DataPrinterImpl printer = new DataPrinterImpl(new StringCoderImpl());

	/**
	 * json字符串的输出流.
	 */
	private Writer out;

	/**
	 * 构造一个<code>JsonPrinter</code>对象.
	 *
	 * @param out   转换后的json字符串的输出流.
	 */
	public JsonPrinter(Writer out)
	{
		this.out = out;
	}

	/**
	 * 输出布尔类型值.
	 *
	 * @param b           布尔值
	 */
	public JsonPrinter print(boolean b)
			throws IOException
	{
		this.printer.print(this.out, b);
		return this;
	}

	/**
	 * 输出字符类型值.
	 *
	 * @param c           字符值
	 */
	public JsonPrinter print(char c)
			throws IOException
	{
		this.printer.print(this.out, c);
		return this;
	}

	/**
	 * 输出整型值.
	 *
	 * @param i           整型值
	 */
	public JsonPrinter print(int i)
			throws IOException
	{
		this.printer.print(this.out, i);
		return this;
	}

	/**
	 * 输出长整型值.
	 *
	 * @param l           长整型值
	 */
	public JsonPrinter print(long l)
			throws IOException
	{
		this.printer.print(this.out, l);
		return this;
	}

	/**
	 * 输出浮点型值.
	 *
	 * @param f           浮点型值
	 */
	public JsonPrinter print(float f)
			throws IOException
	{
		this.printer.print(this.out, f);
		return this;
	}

	/**
	 * 输出双精度浮点型值.
	 *
	 * @param d           双精度浮点型值
	 */
	public JsonPrinter print(double d)
			throws IOException
	{
		this.printer.print(this.out, d);
		return this;
	}


	/**
	 * 输出字符串类型值.
	 *
	 * @param s           字符串类型值
	 */
	public JsonPrinter print(String s)
			throws IOException
	{
		this.printer.print(this.out, s);
		return this;
	}

	/**
	 * 输出一个Object对象.
	 *
	 * @param value        要输出的Object对象
	 */
	public JsonPrinter print(Object value)
			throws IOException
	{
		this.printer.print(this.out, value);
		return this;
	}

	/**
	 * 输出一个Object对象数组.
	 *
	 * @param values       要输出的Object对象数组
	 */
	public JsonPrinter print(Object[] values)
			throws IOException
	{
		this.printer.print(this.out, values);
		return this;
	}

	/**
	 * 输出一个换行符.
	 */
	public JsonPrinter println()
			throws IOException
	{
		this.out.write(Utility.LINE_SEPARATOR);
		return this;
	}

}