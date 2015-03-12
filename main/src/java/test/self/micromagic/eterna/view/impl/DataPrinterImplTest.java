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

import java.io.Writer;

import junit.framework.TestCase;
import self.micromagic.cg.BeanMapTest;
import self.micromagic.cg.TestArrayBean;
import self.micromagic.cg.TestBean;
import self.micromagic.cg.TestMainBean;
import self.micromagic.cg.TestSubBean;
import self.micromagic.eterna.view.DataPrinter;
import self.micromagic.util.StringTool;
import tool.PrivateAccessor;

public class DataPrinterImplTest extends TestCase
{
	DataPrinterImpl dataPrinter;

	protected void setUp()
			throws Exception
	{
		this.dataPrinter = new DataPrinterImpl(new StringCoderImpl());
		//Tool.registerBean(TestBean.class.getName());
	}

	protected void tearDown()
			throws Exception
	{
		this.dataPrinter = null;
	}

	public void testBeanArray()
			throws Exception
	{
		//Utility.setProperty(CG.COMPILE_TYPE_PROPERTY, CG.COMPILE_TYPE_ANT);
		TestArrayBean ab = BeanMapTest.getArrayBean(null);
		//ab.setArrInt(null);
		Writer sw = StringTool.createWriter();
		this.dataPrinter.print(sw, ab);
		System.out.println(sw);
		sw = StringTool.createWriter();
		this.writeArrayBean(sw, ab);
		System.out.println(sw);


		long l1, l2;
		int count = 10000;
		EmptyWriter ew = new EmptyWriter();

		l1 = System.currentTimeMillis();
		for (int i = 0; i < count; i++)
		{
			this.dataPrinter.print(ew, ab);
		}
		l2 = System.currentTimeMillis();
		System.out.println("t1:" + (l2 - l1));

		l1 = System.currentTimeMillis();
		for (int i = 0; i < count; i++)
		{
			this.writeArrayBean(ew, ab);
		}
		l2 = System.currentTimeMillis();
		System.out.println("t2:" + (l2 - l1));
	}

	private void writeArrayBean(Writer out, TestArrayBean ab)
			throws Exception
	{
		out.write("{\"arrInt\":[");
		int[] iArr = ab.getArrInt();
		for (int i = 0; i < iArr.length; i++)
		{
			if (i > 0)
			{
				out.write(',');
			}
			out.write(String.valueOf(iArr[i]));
		}
		out.write("],\"arrStr\":[");
		String[][] sArr = ab.getArrStr();
		for (int i = 0; i < sArr.length; i++)
		{
			if (i > 0)
			{
				out.write(',');
			}
			out.write('[');
			for (int j = 0; j < sArr[i].length; j++)
			{
				if (j > 0)
				{
					out.write(',');
				}
				out.write('"');
				out.write(sArr[i][j]);
				out.write('"');
			}
			out.write(']');
		}
		out.write("],\"mainBean1\":[");
		TestMainBean[] mbArr1 = ab.getMainBean1();
		for (int i = 0; i < mbArr1.length; i++)
		{
			if (i > 0)
			{
				out.write(',');
			}
			this.writeMainBean(out, mbArr1[i]);
		}
		out.write("],\"mainBean2\":[");
		TestMainBean[][] mbArr2 = ab.getMainBean2();
		for (int i = 0; i < mbArr2.length; i++)
		{
			if (i > 0)
			{
				out.write(',');
			}
			out.write('[');
			for (int j = 0; j < mbArr2[i].length; j++)
			{
				if (j > 0)
				{
					out.write(',');
				}
				this.writeMainBean(out, mbArr2[i][j]);
			}
			out.write(']');
		}
		out.write("],\"subBean1\":[");
		TestSubBean[] sbArr1 = ab.getSubBean1();
		for (int i = 0; i < sbArr1.length; i++)
		{
			if (i > 0)
			{
				out.write(',');
			}
			this.writeSubBean(out, sbArr1[i]);
		}
		out.write("],\"subBean2\":[");
		TestSubBean[][][] sbArr2 = ab.getSubBean2();
		for (int i = 0; i < sbArr2.length; i++)
		{
			if (i > 0)
			{
				out.write(',');
			}
			out.write('[');
			for (int j = 0; j < sbArr2[i].length; j++)
			{
				if (j > 0)
				{
					out.write(',');
				}
				out.write('[');
				for (int k = 0; k < sbArr2[i][j].length; k++)
				{
					if (k > 0)
					{
						out.write(',');
					}
					this.writeSubBean(out, sbArr2[i][j][k]);
				}
				out.write(']');
			}
			out.write(']');
		}
		out.write("]}");
	}

	private void writeMainBean(Writer out, TestMainBean mb)
			throws Exception
	{
		out.write("{\"birth\":");
		out.write(String.valueOf(mb.getBirth()));
		out.write(",\"comeDate\":");
		out.write(String.valueOf(mb.getComeDate()));
		out.write(",\"gradeYear\":");
		out.write(String.valueOf(mb.getGradeYear()));
		out.write(",\"id\":\"");
		out.write(mb.getId());
		out.write("\",\"name\":");
		out.write(String.valueOf(mb.getName()));
		out.write(",\"subInfo\":");
		this.writeSubBean(out, mb.getSubInfo());
		out.write('}');
	}

	private void writeSubBean(Writer out, TestSubBean sb)
			throws Exception
	{
		out.write("{\"address\":");
		out.write(String.valueOf(sb.getAddress()));
		out.write(",\"amount\":");
		out.write(String.valueOf(sb.getAmount()));
		out.write(",\"id\":\"");
		out.write(sb.getId());
		out.write("\",\"otherInfo\":");
		out.write(String.valueOf(sb.getOtherInfo()));
		out.write(",\"phone\":");
		out.write(String.valueOf(sb.getPhone()));
		out.write('}');
	}

	public void testReflectBean()
	{
		try
		{
			DataPrinter.BeanPrinter bp = (DataPrinter.BeanPrinter) PrivateAccessor.create(
					Class.forName("self.micromagic.eterna.view.impl.DataPrinterImpl$BeanPrinterImpl"),
					new Class[]{TestBean.class});
			Writer sw = StringTool.createWriter();
			bp.print(this.dataPrinter, sw, new TestBean());
			String r = sw.toString();
			assertEquals(r.indexOf("\"publicDoubleValue\":55.7926") >= 0, true);
			assertEquals(r.indexOf("\"stringValue\":\"test 测试\"") >= 0, true);
			assertEquals(r.indexOf("\"intValue2\":13") >= 0, true);
			//System.out.println(sw);
		}
		catch (Exception ex)
		{
			fail(ex.getMessage());
		}
	}

	public void testJavassistBean()
	{
		try
		{
			DataPrinter.BeanPrinter bp = this.dataPrinter.getBeanPrinter(TestBean.class);
			String startStr = "cg.self.micromagic.cg.TestBean$Printer$$ECG_";
			assertTrue(bp.getClass().getName().startsWith(startStr));
			Writer sw = StringTool.createWriter();
			bp.print(this.dataPrinter, sw, new TestBean());
			String r = sw.toString();
			//System.out.println(sw);
			assertEquals(r.indexOf("\"publicDoubleValue\":55.7926") >= 0, true);
			assertEquals(r.indexOf("\"stringValue\":\"test 测试\"") >= 0, true);
			assertEquals(r.indexOf("\"intValue2\":13") >= 0, true);
		}
		catch (Exception ex)
		{
			fail(ex.getMessage());
		}
	}

	public static class EmptyWriter extends Writer
	{
		public EmptyWriter()
		{
		}

		public void write(int c)
		{
		}

		public void write(String str, int off, int len)
		{
		}

		public void write(String str)
		{
		}

		public void write(char[] cbuf, int off, int len)
		{
		}

		public void write(char[] cbuf)
		{
		}

		public void flush()
		{
		}

		public void close()
		{
		}

	}
}