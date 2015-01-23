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

package self.micromagic.eterna.view.impl;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import self.micromagic.cg.BeanMethodInfo;
import self.micromagic.cg.BeanTool;
import self.micromagic.cg.ClassGenerator;
import self.micromagic.cg.ClassKeyCache;
import self.micromagic.eterna.base.ResultIterator;
import self.micromagic.eterna.base.ResultMetaData;
import self.micromagic.eterna.base.ResultRow;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.Tool;
import self.micromagic.eterna.view.DataPrinter;
import self.micromagic.eterna.view.StringCoder;
import self.micromagic.util.FormatTool;

/**
 * @author micromagic@sina.com
 */
public class DataPrinterImpl extends AbstractGenerator
		implements DataPrinter
{
	protected StringCoder stringCoder;
	protected DateFormat dateFormat = FormatTool.dateFullFormat;

	public DataPrinterImpl()
	{
	}

	public DataPrinterImpl(StringCoder stringCoder)
	{
		this.stringCoder = stringCoder;
	}

	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		if (this.stringCoder != null)
		{
			return true;
		}
		this.stringCoder = factory.getStringCoder();
		return false;
	}

	public void printData(Writer out, Map data, boolean hasPreData)
			throws IOException, EternaException
	{
		boolean first = true;
		Set entrySet = data.entrySet();
		if (entrySet == null)
		{
			// 对于某些实现不完善的map, 如果entry set为null则作为空的处理
			return;
		}
		Iterator entrys = entrySet.iterator();
		while (entrys.hasNext())
		{
			Map.Entry entry = (Map.Entry) entrys.next();
			Object value = entry.getValue();
			if (value != null)
			{
				Object key = entry.getKey();
				String keyStr = key == null ? null : key.toString();
				if (hasPreData || !first)
				{
					// 输出data数据集前面会有其它数据或不是第一个数据时，需要先输出","
					out.write(",\"");
				}
				else
				{
					first = false;
					out.write('"');
				}
				this.stringCoder.toJsonString(out, keyStr);
				out.write("\":");
				this.print(out, value);
			}
		}
	}

	public void print(Writer out, Object value)
			throws IOException, EternaException
	{
		if (value == null)
		{
			out.write("null");
		}
		else if (value instanceof String)
		{
			out.write('"');
			this.stringCoder.toJsonStringWithoutCheck(out, (String) value);
			out.write('"');
		}
		else if (value instanceof Number || value instanceof Boolean)
		{
			out.write(value.toString());
		}
		else if (value instanceof Map)
		{
			this.printMap(out, (Map) value);
		}
		else if (value instanceof Collection)
		{
			this.printCollection(out, (Collection) value);
		}
		else if (value instanceof ResultRow)
		{
			try
			{
				this.printResultRow(out, (ResultRow) value);
			}
			catch (SQLException ex)
			{
				throw new EternaException(ex);
			}
		}
		else if (value instanceof ResultIterator)
		{
			out.write('{');
			try
			{
				this.printResultIterator(out, (ResultIterator) value);
			}
			catch (SQLException ex)
			{
				throw new EternaException(ex);
			}
			out.write('}');
		}
		else if (value instanceof Iterator)
		{
			this.printIterator(out, (Iterator) value);
		}
		else if (value instanceof BeanPrinter)
		{
			((BeanPrinter) value).print(this, out, value);
		}
		else if (value instanceof Enumeration)
		{
			this.printEnumeration(out, (Enumeration) value);
		}
		else if (ClassGenerator.isArray(value.getClass()))
		{
			char flag = value.getClass().getName().charAt(1);
			if (flag == 'L' || flag == '[')
			{
				// Object array
				this.print(out, (Object[]) value);
			}
			else if (flag < 'I')
			{
				// B byte C char F float D dougle
				if (flag == 'D')
				{
					this.print(out, (double[]) value);
				}
				else if (flag == 'B')
				{
					this.print(out, (byte[]) value);
				}
				else if (flag == 'C')
				{
					this.print(out, (char[]) value);
				}
				else if (flag == 'F')
				{
					this.print(out, (float[]) value);
				}
				else
				{
					throw new Error("Error class:" + value.getClass().getName() + ".");
				}
			}
			else
			{
				// I int J long S short Z boolean
				if (flag == 'I')
				{
					this.print(out, (int[]) value);
				}
				else if (flag == 'Z')
				{
					this.print(out, (boolean[]) value);
				}
				else if (flag == 'J')
				{
					this.print(out, (long[]) value);
				}
				else if (flag == 'S')
				{
					this.print(out, (short[]) value);
				}
				else
				{
					throw new Error("Error class:" + value.getClass().getName() + ".");
				}
			}
		}
		else if (value instanceof Date)
		{
			this.print(out, this.dateFormat.format((Date) value));
		}
		else if (Tool.isBean(value.getClass()))
		{
			BeanPrinter bp = this.getBeanPrinter(value.getClass());
			out.write('{');
			bp.print(this, out, value);
			out.write('}');
		}
		else if (value instanceof Calendar)
		{
			Date d = ((Calendar) value).getTime();
			this.print(out, this.dateFormat.format(d));
		}
		else if (value instanceof Map.Entry)
		{
			Map.Entry entry = (Map.Entry) value;
			Object tKey = entry.getKey();
			Object tValue = entry.getValue();
			out.write("{\"key\":");
			this.print(out, tKey);
			out.write(",\"value\":");
			this.print(out, tValue);
			out.write('}');
		}
		else
		{
			out.write('"');
			this.stringCoder.toJsonStringWithoutCheck(out, String.valueOf(value));
			out.write('"');
		}
	}

	public void print(Writer out, Object[] values)
			throws IOException, EternaException
	{
		out.write('[');
		if (values.length > 0)
		{
			this.print(out, values[0]);
		}
		for (int i = 1; i < values.length; i++)
		{
			out.write(',');
			this.print(out, values[i]);
		}
		out.write(']');
	}

	public void printMap(Writer out, Map map)
			throws IOException, EternaException
	{
		out.write('{');
		this.printData(out, map, false);
		out.write('}');
	}

	protected void printCollection(Writer out, Collection collection)
			throws IOException, EternaException
	{
		if (collection.size() > 0)
		{
			this.printIterator(out, collection.iterator());
		}
		else
		{
			out.write("[]");
		}
	}

	public void printResultRow(Writer out, ResultRow row)
			throws IOException, EternaException, SQLException
	{
		out.write('{');
		ResultMetaData rmd = row.getResultIterator().getMetaData();
		int count = rmd.getColumnCount();
		boolean firstSetted = false;
		for (int i = 1; i <= count; i++)
		{
			if (rmd.getColumnReader(i).isValid())
			{
				if (firstSetted)
				{
					out.write(",\"");
				}
				else
				{
					firstSetted = true;
					out.write('"');
				}
				this.stringCoder.toJsonString(out, rmd.getColumnName(i));
				out.write("\":");
				this.print(out, row.getFormated(i));
			}
		}
		out.write('}');
	}

	public void printResultIterator(Writer out, ResultIterator ritr)
			throws IOException, EternaException, SQLException
	{
		ResultMetaData rmd = ritr.getMetaData();
		int count = rmd.getColumnCount();
		out.write("names:{");
		boolean firstSetted = false;
		for (int i = 1; i <= count; i++)
		{
			if (rmd.getColumnReader(i).isValid())
			{
				if (firstSetted)
				{
					out.write(",\"");
				}
				else
				{
					firstSetted = true;
					out.write('"');
				}
				this.stringCoder.toJsonString(out, rmd.getColumnName(i));
				out.write("\":");
				out.write(Integer.toString(i));
			}
		}
		out.write("},rowCount:");
		out.write(Integer.toString(ritr.getRecordCount()));
		out.write(",rows:[");
		boolean nextRow = false;
		while (ritr.hasNext())
		{
			if (nextRow)
			{
				out.write(",[");
			}
			else
			{
				nextRow = true;
				out.write('[');
			}
			ResultRow row = (ResultRow) ritr.next();
			for (int i = 1; i <= count; i++)
			{
				if (i > 1)
				{
					out.write(',');
				}
				this.print(out, row.getFormated(i));
			}
			out.write(']');
		}
		out.write(']');
	}

	public void printEnumeration(Writer out, Enumeration e)
			throws IOException, EternaException
	{
		out.write('[');
		if (e.hasMoreElements())
		{
			this.print(out, e.nextElement());
		}
		while (e.hasMoreElements())
		{
			out.write(',');
			this.print(out, e.nextElement());
		}
		out.write(']');
	}

	public void printIterator(Writer out, Iterator itr)
			throws IOException, EternaException
	{
		out.write('[');
		if (itr.hasNext())
		{
			this.print(out, itr.next());
		}
		while (itr.hasNext())
		{
			out.write(',');
			this.print(out, itr.next());
		}
		out.write(']');
	}

	public void print(Writer out, boolean b)
			throws IOException, EternaException
	{
		out.write(b ? "true" : "false");
	}

	public void print(Writer out, boolean[] values)
			throws IOException, EternaException
	{
		out.write('[');
		if (values.length > 0)
		{
			out.write(values[0] ? "true" : "false");
		}
		for (int i = 1; i < values.length; i++)
		{
			out.write(values[i] ? ",true" : ",false");
		}
		out.write(']');
	}

	public void print(Writer out, char c)
			throws IOException, EternaException
	{
		out.write('"');
		this.stringCoder.toJsonString(out, c);
		out.write('"');
	}

	public void print(Writer out, char[] values)
			throws IOException, EternaException
	{
		out.write('[');
		if (values.length > 0)
		{
			out.write('"');
			this.stringCoder.toJsonString(out, values[0]);
			out.write('"');
		}
		for (int i = 1; i < values.length; i++)
		{
			out.write(",\"");
			this.stringCoder.toJsonString(out, values[i]);
			out.write('"');
		}
		out.write(']');
	}

	public void print(Writer out, int i)
			throws IOException, EternaException
	{
		out.write(Integer.toString(i, 10));
	}

	public void print(Writer out, int[] values)
			throws IOException, EternaException
	{
		out.write('[');
		if (values.length > 0)
		{
			out.write(Integer.toString(values[0], 10));
		}
		for (int i = 1; i < values.length; i++)
		{
			out.write(',');
			out.write(Integer.toString(values[i], 10));
		}
		out.write(']');
	}

	public void print(Writer out, byte[] values)
			throws IOException, EternaException
	{
		out.write('[');
		if (values.length > 0)
		{
			out.write(Integer.toString(values[0], 10));
		}
		for (int i = 1; i < values.length; i++)
		{
			out.write(',');
			out.write(Integer.toString(values[i], 10));
		}
		out.write(']');
	}

	public void print(Writer out, short[] values)
			throws IOException, EternaException
	{
		out.write('[');
		if (values.length > 0)
		{
			out.write(Integer.toString(values[0], 10));
		}
		for (int i = 1; i < values.length; i++)
		{
			out.write(',');
			out.write(Integer.toString(values[i], 10));
		}
		out.write(']');
	}

	public void print(Writer out, long l)
			throws IOException, EternaException
	{
		out.write(Long.toString(l, 10));
	}

	public void print(Writer out, long[] values)
			throws IOException, EternaException
	{
		out.write('[');
		if (values.length > 0)
		{
			out.write(Long.toString(values[0], 10));
		}
		for (int i = 1; i < values.length; i++)
		{
			out.write(',');
			out.write(Long.toString(values[i], 10));
		}
		out.write(']');
	}

	public void print(Writer out, float f)
			throws IOException, EternaException
	{
		out.write(Float.toString(f));
	}

	public void print(Writer out, float[] values)
			throws IOException, EternaException
	{
		out.write('[');
		if (values.length > 0)
		{
			out.write(Float.toString(values[0]));
		}
		for (int i = 1; i < values.length; i++)
		{
			out.write(',');
			out.write(Float.toString(values[i]));
		}
		out.write(']');
	}

	public void print(Writer out, double d)
			throws IOException, EternaException
	{
		out.write(Double.toString(d));
	}

	public void print(Writer out, double[] values)
			throws IOException, EternaException
	{
		out.write('[');
		if (values.length > 0)
		{
			out.write(Double.toString(values[0]));
		}
		for (int i = 1; i < values.length; i++)
		{
			out.write(',');
			out.write(Double.toString(values[i]));
		}
		out.write(']');
	}

	public void print(Writer out, String s)
			throws IOException, EternaException
	{
		if (s == null)
		{
			out.write("null");
		}
		else
		{
			out.write('"');
			this.stringCoder.toJsonStringWithoutCheck(out, s);
			out.write('"');
		}
	}

	public void printObjectBegin(Writer out)
			throws IOException
	{
		out.write('{');
	}

	public void printObjectEnd(Writer out)
			throws IOException
	{
		out.write('}');
	}

	public void printPair(Writer out, String key, boolean value, boolean first)
			throws IOException, EternaException
	{
		if (!first)
		{
			out.write(',');
		}
		this.print(out, key);
		out.write(':');
		this.print(out, value);
	}

	public void printPair(Writer out, String key, char value, boolean first)
			throws IOException, EternaException
	{
		if (!first)
		{
			out.write(',');
		}
		this.print(out, key);
		out.write(':');
		this.print(out, value);
	}

	public void printPair(Writer out, String key, int value, boolean first)
			throws IOException, EternaException
	{
		if (!first)
		{
			out.write(',');
		}
		this.print(out, key);
		out.write(':');
		this.print(out, value);
	}

	public void printPairWithoutCheck(Writer out, String key, int value, boolean first)
			throws IOException, EternaException
	{
		if (first)
		{
			out.write('"');
		}
		else
		{
			out.write(",\"");
		}
		out.write(key);
		out.write("\":");
		this.print(out, value);
	}

	public void printPair(Writer out, String key, long value, boolean first)
			throws IOException, EternaException
	{
		if (!first)
		{
			out.write(',');
		}
		this.print(out, key);
		out.write(':');
		this.print(out, value);
	}

	public void printPairWithoutCheck(Writer out, String key, long value, boolean first)
			throws IOException, EternaException
	{
		if (first)
		{
			out.write('"');
		}
		else
		{
			out.write(",\"");
		}
		out.write(key);
		out.write("\":");
		this.print(out, value);
	}

	public void printPair(Writer out, String key, float value, boolean first)
			throws IOException, EternaException
	{
		if (!first)
		{
			out.write(',');
		}
		this.print(out, key);
		out.write(':');
		this.print(out, value);
	}

	public void printPair(Writer out, String key, double value, boolean first)
			throws IOException, EternaException
	{
		if (!first)
		{
			out.write(',');
		}
		this.print(out, key);
		out.write(':');
		this.print(out, value);
	}

	public void printPairWithoutCheck(Writer out, String key, double value, boolean first)
			throws IOException, EternaException
	{
		if (first)
		{
			out.write('"');
		}
		else
		{
			out.write(",\"");
		}
		out.write(key);
		out.write("\":");
		this.print(out, value);
	}

	public void printPair(Writer out, String key, String value, boolean first)
			throws IOException, EternaException
	{
		if (!first)
		{
			out.write(',');
		}
		this.print(out, key);
		out.write(':');
		this.print(out, value);
	}

	public void printPairWithoutCheck(Writer out, String key, String value, boolean first)
			throws IOException, EternaException
	{
		if (first)
		{
			out.write('"');
		}
		else
		{
			out.write(",\"");
		}
		out.write(key);
		out.write("\":");
		this.print(out, value);
	}

	public void printPair(Writer out, String key, Object value, boolean first)
			throws IOException, EternaException
	{
		if (!first)
		{
			out.write(',');
		}
		this.print(out, key);
		out.write(':');
		this.print(out, value);
	}

	public void printPairWithoutCheck(Writer out, String key, Object value, boolean first)
			throws IOException, EternaException
	{
		if (first)
		{
			out.write('"');
		}
		else
		{
			out.write(",\"");
		}
		out.write(key);
		out.write("\":");
		this.print(out, value);
	}

	public void setDateFormat(DateFormat format)
	{
		if (format == null)
		{
			throw new NullPointerException("The param format is null.");
		}
		this.dateFormat = format;
	}

	public DataPrinter createDataPrinter()
	{
		return this;
	}

	public Object create()
	{
		return this.createDataPrinter();
	}

	/**
	 * 存放BeanPrinter的缓存
	 */
	private static ClassKeyCache beanPrinterCache = ClassKeyCache.getInstance();

	/**
	 * 根据需要打印对象的类型获取一个BeanPrinter对象.
	 */
	public BeanPrinter getBeanPrinter(Class beanClass)
	{
		BeanPrinter bp = (BeanPrinter) beanPrinterCache.getProperty(beanClass);
		if (bp == null)
		{
			bp = getBeanPrinter0(beanClass);
		}
		return bp;
	}

	/**
	 * 注册一个BeanPrinter对象.
	 */
	public static synchronized void registerBeanPrinter(Class beanClass, BeanPrinter p)
	{
      if (beanClass != null && p != null)
		{
			beanPrinterCache.setProperty(beanClass, p);
		}
	}

	private static synchronized BeanPrinter getBeanPrinter0(Class beanClass)
	{
		BeanPrinter bp = (BeanPrinter) beanPrinterCache.getProperty(beanClass);
		if (bp == null)
		{
			try
			{
				/*
				String mh = "public void print(DataPrinter p, Writer out, Object bean)"
						+ " throws IOException, EternaException";
				String ut = "p.printPair(out, \"${name}\", ${value}, ${first});";
				String pt = "p.printPair(out, \"${name}\", ${o_value}, ${first});";
				String lt = "";
				使用上面这段代码，对bean的处理效率明显下降
				*/
				String mh = "public void print(DataPrinter p, Writer out, Object bean)"
						+ " throws IOException, EternaException";
				String ut = "out.write(\"\\\"${name}\\\":\");"
						+ "p.print(out, ${value});";
				String pt = "out.write(\"\\\"${name}\\\":\");"
						+ "p.print(out, ${o_value});";
				String lt = "out.write(\",\");";
				String[] imports = new String[]{
					ClassGenerator.getPackageString(DataPrinter.class),
					ClassGenerator.getPackageString(Writer.class),
					ClassGenerator.getPackageString(EternaException.class),
					ClassGenerator.getPackageString(beanClass)
				};
				bp = (BeanPrinter) Tool.createBeanPrinter(beanClass, BeanPrinter.class, mh,
						"bean", ut, pt, lt, imports);
				if (bp == null)
				{
					bp = new BeanPrinterImpl(beanClass);
				}
			}
			catch (Throwable ex)
			{
				bp = new BeanPrinterImpl(beanClass);
			}
			beanPrinterCache.setProperty(beanClass, bp);
		}
		return bp;
	}

	private static class BeanPrinterImpl
			implements BeanPrinter
	{
		private final Field[] fields;
		private final BeanMethodInfo[] methods;

		public BeanPrinterImpl(Class c)
		{
			this.fields = BeanTool.getBeanFields(c);
			this.methods = BeanTool.getBeanReadMethods(c);
		}

		public void print(DataPrinter p, Writer out, Object bean)
				throws IOException, EternaException
		{
			try
			{
				boolean first = true;
				for (int i = 0; i < this.fields.length; i++)
				{
					if (first)
					{
						first = false;
						out.write('"');
					}
					else
					{
						out.write(",\"");
					}
					Field f = this.fields[i];
					out.write(f.getName());
					out.write("\":");
					p.print(out, f.get(bean));
				}
				for (int i = 0; i < this.methods.length; i++)
				{
					BeanMethodInfo m = this.methods[i];
					if (m.method != null)
					{
						if (first)
						{
							first = false;
							out.write('"');
						}
						else
						{
							out.write(",\"");
						}
						out.write(m.name);
						out.write("\":");
						p.print(out, m.method.invoke(bean, new Object[0]));
					}
				}
			}
			catch (Exception ex)
			{
				if (ex instanceof IOException)
				{
					throw (IOException) ex;
				}
				if (ex instanceof EternaException)
				{
					throw (EternaException) ex;
				}
				throw new EternaException(ex);
			}
		}

	}

}