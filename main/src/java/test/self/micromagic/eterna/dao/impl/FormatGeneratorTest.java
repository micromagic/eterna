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

package self.micromagic.eterna.dao.impl;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.TestCase;
import self.micromagic.eterna.EternaFactoryCreater;
import self.micromagic.eterna.dao.ResultFormat;
import self.micromagic.eterna.dao.impl.FormatGenerator;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;

public class FormatGeneratorTest extends TestCase
{
	FormatGenerator rfg;
	EternaFactory factory;

	protected void setUp() throws Exception
	{
		this.rfg = new FormatGenerator();
		this.factory = EternaFactoryCreater.getEternaFactory(this.getClass());
	}

	protected void tearDown() throws Exception
	{
		this.rfg = null;
		this.factory = null;
	}

	public void testBoolean_config()
	{
		try
		{
			ResultFormat f = this.factory.getFormat("testBoolean");
			String r = (String) f.format(Boolean.TRUE, null, null, null);
			assertEquals("是", r);
			r = (String) f.format(Boolean.FALSE, null, null, null);
			assertEquals("否", r);
			r = (String) f.format(null, null, null, null);
			assertEquals("", r);
		}
		catch (Exception ex)
		{
			fail("调用时不应抛出异常");
		}
	}

	public void testBoolean()
	{
		try
		{
			this.rfg.setName("testBoolean");
			this.rfg.setType("boolean");
		}
		catch (EternaException ex)
		{
			fail("设置时不应抛出异常");
		}
		try
		{
			this.rfg.setPattern("是:否");
			ResultFormat f = (ResultFormat) this.rfg.create();
			String r = (String) f.format(Boolean.TRUE, null, null, null);
			assertEquals("是", r);
			r = (String) f.format(Boolean.FALSE, null, null, null);
			assertEquals("否", r);
		}
		catch (Exception ex)
		{
			fail("调用时不应抛出异常");
		}
	}

	public void testDate_config()
	{
		try
		{
			Date now = new Date();
			ResultFormat f = this.factory.getFormat("testDate1");
			String r = (String) f.format(now, null, null, null);
			assertEquals(DateFormat.getInstance().format(now), r);

			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			this.rfg.setPattern("yyyy-MM-dd");
			f = this.factory.getFormat("testDate2");
			r = (String) f.format(df.parse("2012-01-01"), null, null, null);
			assertEquals("2012-01-01", r);

			df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			this.rfg.setPattern("locale:en,dd/MMM/yyyy HH:mm:ss");
			f = this.factory.getFormat("testDate3");
			r = (String) f.format(df.parse("2012-01-01 23:59:59"), null, null, null);
			assertEquals("01/Jan/2012 23:59:59", r);
		}
		catch (Exception ex)
		{
			fail("调用时不应抛出异常");
		}
	}

	public void testDate()
	{
		try
		{
			this.rfg.setName("testDate");
			this.rfg.setType("Date");
		}
		catch (EternaException ex)
		{
			fail("设置时不应抛出异常");
		}
		try
		{
			Date now = new Date();
			ResultFormat f = (ResultFormat) this.rfg.create();
			String r = (String) f.format(now, null, null, null);
			assertEquals(DateFormat.getInstance().format(now), r);

			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			this.rfg.setPattern("yyyy-MM-dd");
			f = (ResultFormat) this.rfg.create();
			r = (String) f.format(df.parse("2012-01-01"), null, null, null);
			assertEquals("2012-01-01", r);

			df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			this.rfg.setPattern("yyyy-MM-dd HH:mm:ss");
			f = (ResultFormat) this.rfg.create();
			r = (String) f.format(df.parse("2012-01-01 23:59:59"), null, null, null);
			assertEquals("2012-01-01 23:59:59", r);

			df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			this.rfg.setPattern("locale:en,dd/MMM/yyyy HH:mm:ss");
			f = (ResultFormat) this.rfg.create();
			r = (String) f.format(df.parse("2012-01-01 23:59:59"), null, null, null);
			assertEquals("01/Jan/2012 23:59:59", r);

			r = (String) f.format(null, null, null, null);
			assertEquals("", r);
		}
		catch (Exception ex)
		{
			fail("调用时不应抛出异常");
		}
	}

	public void testNumber_config()
	{
		try
		{
			ResultFormat f = this.factory.getFormat("testNumber1");
			String r = (String) f.format(new Double("1000"), null, null, null);
			assertEquals(NumberFormat.getInstance().format(1000.0), r);

			this.rfg.setPattern("#0.00");
			f = this.factory.getFormat("testNumber2");
			r = (String) f.format(new Double("1000"), null, null, null);
			assertEquals("1000.00", r);

			this.rfg.setPattern("#,##0.00");
			f = this.factory.getFormat("testNumber3");
			r = (String) f.format(new Double("1000"), null, null, null);
			assertEquals("1,000.00", r);

			r = (String) f.format(null, null, null, null);
			assertEquals("", r);
		}
		catch (EternaException ex)
		{
			fail("调用时不应抛出异常");
		}
	}

	public void testNumber()
	{
		try
		{
			this.rfg.setName("testNumber");
			this.rfg.setType("Number");
		}
		catch (EternaException ex)
		{
			fail("设置时不应抛出异常");
		}
		try
		{
			ResultFormat f = (ResultFormat) this.rfg.create();
			String r = (String) f.format(new Double("1000"), null, null, null);
			assertEquals(NumberFormat.getInstance().format(1000.0), r);

			this.rfg.setPattern("#0.00");
			f = (ResultFormat) this.rfg.create();
			r = (String) f.format(new Double("1000"), null, null, null);
			assertEquals("1000.00", r);

			this.rfg.setPattern("#,##0.00");
			f = (ResultFormat) this.rfg.create();
			r = (String) f.format(new Double("1000"), null, null, null);
			assertEquals("1,000.00", r);
		}
		catch (EternaException ex)
		{
			fail("调用时不应抛出异常");
		}
	}

	public void testErrorType()
	{
		try
		{
			this.rfg.setName("testErrorType");
		}
		catch (EternaException ex)
		{
			fail("设置时不应抛出异常");
		}
		try
		{
			this.rfg.create();
			fail("在没有设置类型时应抛出异常");
		}
		catch (EternaException ex)
		{
		}
		try
		{
			this.rfg.setType("other");
			this.rfg.create();
			fail("在设置了错误的类型时应抛出异常");
		}
		catch (EternaException ex)
		{
		}
	}

}