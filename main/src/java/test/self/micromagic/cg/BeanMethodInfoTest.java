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

package self.micromagic.cg;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.IndexedPropertyDescriptor;

import junit.framework.TestCase;

public class BeanMethodInfoTest extends TestCase
{
	protected void setUp() throws Exception
	{
		System.out.println("begin ===============================");
	}

	protected void tearDown() throws Exception
	{
		System.out.println("end   ===============================");
	}

	public void testParse2()
			throws Exception
	{
		BeanMethodInfo[] infos = BeanMethodInfo.getBeanMethods(MyBean.class);
		for (int i = 0; i < infos.length; i++)
		{
			System.out.println(infos[i].name);
			System.out.println(infos[i].doGet);
			System.out.println(infos[i].type);
			System.out.println(infos[i].method);
			System.out.println(infos[i].indexedType);
			System.out.println(infos[i].indexedMethod);
			System.out.println("----------------------------------------------");
		}
	}

	public void testParse1()
			throws Exception
	{
		BeanInfo info = Introspector.getBeanInfo(MyBean.class, Object.class);
		PropertyDescriptor[] pds = info.getPropertyDescriptors();
		for (int i = 0; i < pds.length; i++)
		{

			System.out.println(pds[i].getName());
			System.out.println(pds[i].getPropertyType());
			System.out.println(pds[i].getReadMethod());
			System.out.println(pds[i].getWriteMethod());
			if (pds[i] instanceof IndexedPropertyDescriptor)
			{
				IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor) pds[i];
				System.out.println(ipd.getIndexedPropertyType());
				System.out.println(ipd.getIndexedReadMethod());
				System.out.println(ipd.getIndexedWriteMethod());
			}
			System.out.println("----------------------------------------------");
		}
	}


	public static class MyBean
	{

		// info5 5种类型

		public void setInfo5(int index, int info)
		{
		}

		public String getInfo5(int index)
		{
			return null;
		}

		public byte[] getInfo5()
		{
			return null;
		}

		public void setInfo5(double info)
		{
		}

		public boolean isInfo5()
		{
			return false;
		}

		// info 同一种类型  public boolean getInfo() 和 public boolean isInfo() 应该合并

		public void setInfo(int index, boolean info)
		{
		}

		public boolean getInfo(int index)
		{
			return false;
		}

		public boolean getInfo()
		{
			return false;
		}

		public void setInfo(boolean info)
		{
		}

		public boolean isInfo()
		{
			return false;
		}

	}

}