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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.WeakHashMap;

import junit.framework.TestCase;
import self.micromagic.util.container.ThreadCache;

public class FormatTest extends TestCase
{
	public void testTemplateFormat()
	{
		TemplateFormat t = new TemplateFormat();
		t.setPattern("s[v]dfdfds[v]df[v]");
		t.parseTemplate();
		assertEquals(4, t.patterns.length);
		assertEquals("s", t.patterns[0]);
		assertEquals("dfdfds", t.patterns[1]);
		assertEquals("df", t.patterns[2]);
		assertEquals("", t.patterns[3]);
	}

	public void testFullDate()
			throws Exception
	{
		System.out.println(FormatTool.formatFullDate(new Date()));
		Thread.sleep(173L);
		System.out.println(FormatTool.formatFullDate(new Date()));
		Thread.sleep(173L);
		System.out.println(FormatTool.formatFullDate(new Date()));
		Thread.sleep(173L);
		System.out.println(FormatTool.formatFullDate(new Date()));
		Thread.sleep(173L);
		System.out.println(FormatTool.formatFullDate(new Date()));
	}

	static final DateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static String dStr = "2015-01-17 11:12:11";
	static java.util.Date tmpDate;
	static
	{
		try
		{
			tmpDate = datetimeFormat.parse(dStr);
		}
		catch (Exception ex) {}
	}
	static DecimalFormat f1 = new DecimalFormat("#,###.#");
	static DecimalFormat f2 = new DecimalFormat("#");

	public void test01()
			throws Exception
	{
		int count = 3;
		Thread[] tArr = new Thread[count];

		// DateFormat 格式化时是非线程安全的
		for (int i = 0; i < count; i++)
		{
			tArr[i] = new Thread(new T_DataFormat("d" + i));
			tArr[i].start();
		}
		for (int i = 0; i < count; i++)
		{
			tArr[i].join();
		}

		// DecimalFormat 格式化时是线程安全的
		for (int i = 0; i < count; i++)
		{
			tArr[i] = new Thread(new T_NumberFormat("n" + i));
			tArr[i].start();
		}
		for (int i = 0; i < count; i++)
		{
			tArr[i].join();
		}
	}

	public void test02()
			throws Exception
	{
		int count = 3;
		Thread[] tArr = new Thread[count];

		// 使用synchronized需要的时间
		for (int i = 0; i < count; i++)
		{
			tArr[i] = new Thread(new T_SynTime("  syn." + i));
			tArr[i].start();
		}
		for (int i = 0; i < count; i++)
		{
			tArr[i].join();
		}

		// 使用ThreadCache需要的时间
		for (int i = 0; i < count; i++)
		{
			tArr[i] = new Thread(new T_ThreadCacheTime("cache." + i));
			tArr[i].start();
		}
		for (int i = 0; i < count; i++)
		{
			tArr[i].join();
		}

		// 使用ThreadCache2需要的时间
		for (int i = 0; i < count; i++)
		{
			tArr[i] = new Thread(new T_ThreadCacheTime("cache2." + i));
			tArr[i].start();
		}
		for (int i = 0; i < count; i++)
		{
			tArr[i].join();
		}

		// 使用Clone需要的时间
		for (int i = 0; i < count; i++)
		{
			//tArr[i] = new Thread(new T_CloneTime("clone." + i));
			//tArr[i].start();
		}
		for (int i = 0; i < count; i++)
		{
			//tArr[i].join();
		}

		// 仅仅使用本地变量需要的时间
		for (int i = 0; i < count; i++)
		{
			tArr[i] = new Thread(new T_LocaleTime("  loc." + i));
			tArr[i].start();
		}
		for (int i = 0; i < count; i++)
		{
			tArr[i].join();
		}

		// 每次创建一个新的需要的时间
		for (int i = 0; i < count; i++)
		{
			tArr[i] = new Thread(new T_CreateTime("  new." + i));
			tArr[i].start();
		}
		for (int i = 0; i < count; i++)
		{
			tArr[i].join();
		}
	}

	class T_CreateTime
			implements Runnable
	{
		public T_CreateTime(String name)
		{
			this.name = name;
		}
		private final String name;

		private int errCount;
		public void run()
		{
			try
			{
				DateFormat tf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Calendar c = Calendar.getInstance();
				c.setTime(tf.parse("2015-01-20 11:12:11"));
				DateFormat f;
				long begin = System.currentTimeMillis();
				for (int i = 0; i < 100000; i++)
				{
					f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String s = "2015-01-2" + (i % 10) + " 11:12:11";
					c.set(Calendar.DATE, 20 + (i % 10));
					if (!s.equals(f.format(c.getTime())))
					{
						this.errCount++;
					}
				}
				System.out.println("name:" + this.name + ", "
						+ (System.currentTimeMillis() - begin) + ", " + this.errCount);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	class T_LocaleTime
			implements Runnable
	{
		public T_LocaleTime(String name)
		{
			this.name = name;
		}
		private final String name;

		private int errCount;
		public void run()
		{
			try
			{
				DateFormat tf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Calendar c = Calendar.getInstance();
				c.setTime(tf.parse("2015-01-20 11:12:11"));
				DateFormat f = (DateFormat) datetimeFormat.clone();
				long begin = System.currentTimeMillis();
				for (int i = 0; i < 100000; i++)
				{
					String s = "2015-01-2" + (i % 10) + " 11:12:11";
					c.set(Calendar.DATE, 20 + (i % 10));
					if (!s.equals(f.format(c.getTime())))
					{
						this.errCount++;
					}
				}
				System.out.println("name:" + this.name + ", "
						+ (System.currentTimeMillis() - begin) + ", " + this.errCount);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	class T_CloneTime
			implements Runnable
	{
		public T_CloneTime(String name)
		{
			this.name = name;
		}
		private final String name;

		private int errCount;
		public void run()
		{
			try
			{
				DateFormat tf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Calendar c = Calendar.getInstance();
				c.setTime(tf.parse("2015-01-20 11:12:11"));
				long begin = System.currentTimeMillis();
				for (int i = 0; i < 100000; i++)
				{
					String s = "2015-01-2" + (i % 10) + " 11:12:11";
					c.set(Calendar.DATE, 20 + (i % 10));
					DateFormat f = (DateFormat) datetimeFormat.clone();
					if (!s.equals(f.format(c.getTime())))
					{
						this.errCount++;
					}
				}
				System.out.println("name:" + this.name + ", "
						+ (System.currentTimeMillis() - begin) + ", " + this.errCount);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	class T_ThreadCacheTime
			implements Runnable
	{
		public T_ThreadCacheTime(String name)
		{
			this.name = name;
		}
		private final String name;

		private int errCount;
		public void run()
		{
			try
			{
				DateFormat tf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Calendar c = Calendar.getInstance();
				c.setTime(tf.parse("2015-01-20 11:12:11"));
				long begin = System.currentTimeMillis();
				for (int i = 0; i < 100000; i++)
				{
					String s = "2015-01-2" + (i % 10) + " 11:12:11";
					c.set(Calendar.DATE, 20 + (i % 10));
					ThreadCache cache = ThreadCache.getInstance();
					Map fCache = (Map) cache.getProperty("ttt");
					if (fCache == null)
					{
						fCache = new WeakHashMap();
						cache.setProperty("ttt", fCache);
					}
					DateFormat f = (DateFormat) fCache.get(datetimeFormat);
					if (f == null)
					{
						f = (DateFormat) datetimeFormat.clone();
						fCache.put(datetimeFormat, f);
					}
					if (!s.equals(f.format(c.getTime())))
					{
						this.errCount++;
					}
				}
				System.out.println("name:" + this.name + ", "
						+ (System.currentTimeMillis() - begin) + ", " + this.errCount);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	class T_ThreadCacheTime2
			implements Runnable
	{
		public T_ThreadCacheTime2(String name)
		{
			this.name = name;
		}
		private final String name;

		private int errCount;
		public void run()
		{
			try
			{
				DateFormat tf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Calendar c = Calendar.getInstance();
				c.setTime(tf.parse("2015-01-20 11:12:11"));
				long begin = System.currentTimeMillis();
				for (int i = 0; i < 100000; i++)
				{
					String s = "2015-01-2" + (i % 10) + " 11:12:11";
					c.set(Calendar.DATE, 20 + (i % 10));
					DateFormat f = FormatTool.getThreadFormat(datetimeFormat);
					if (!s.equals(f.format(c.getTime())))
					{
						this.errCount++;
					}
				}
				System.out.println("name:" + this.name + ", "
						+ (System.currentTimeMillis() - begin) + ", " + this.errCount);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	class T_SynTime
			implements Runnable
	{
		public T_SynTime(String name)
		{
			this.name = name;
		}
		private final String name;

		private int errCount;
		public void run()
		{
			try
			{
				DateFormat tf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Calendar c = Calendar.getInstance();
				c.setTime(tf.parse("2015-01-20 11:12:11"));
				long begin = System.currentTimeMillis();
				for (int i = 0; i < 100000; i++)
				{
					String s = "2015-01-2" + (i % 10) + " 11:12:11";
					c.set(Calendar.DATE, 20 + (i % 10));
					synchronized (datetimeFormat)
					{
						if (!s.equals(datetimeFormat.format(c.getTime())))
						{
							this.errCount++;
						}
					}
				}
				System.out.println("name:" + this.name + ", "
						+ (System.currentTimeMillis() - begin) + ", " + this.errCount);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	class T_NumberFormat
			implements Runnable
	{
		public T_NumberFormat(String name)
		{
			this.name = name;
		}
		private final String name;

		private int errCount;
		public void run()
		{
			long begin = System.currentTimeMillis();
			for (int i = 0; i < 1000000; i++)
			{
				try
				{
					if (!Integer.toString(i).equals(f2.format(i)))
					{
						this.errCount++;
					}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
			System.out.println("name:" + this.name + ", "
					+ (System.currentTimeMillis() - begin) + ", " + this.errCount);
		}
	}

	class T_DataFormat
			implements Runnable
	{
		public T_DataFormat(String name)
		{
			this.name = name;
		}
		private final String name;

		private int errCount;
		public void run()
		{
			long begin = System.currentTimeMillis();
			for (int i = 0; i < 100000; i++)
			{
				try
				{
					String s = "2015-01-2" + (i % 10) + " 11:12:11";
					java.util.Date d = FormatTool.parseDatetime(s);
					if (!s.equals(datetimeFormat.format(d)))
					{
						this.errCount++;
					}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
			System.out.println("name:" + this.name + ", "
					+ (System.currentTimeMillis() - begin) + ", " + this.errCount);
		}
	}

}
