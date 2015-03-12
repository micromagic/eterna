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

package self.micromagic.cg;

import java.lang.reflect.Field;
import java.util.List;

import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;

public class TestSubBean
{
	private String id;
	private String address;
	private String phone;
	private double amount;
	private List otherInfo;

	public String getId()
	{
		return this.id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getAddress()
	{
		return this.address;
	}

	public void setAddress(String address)
	{
		this.address = address;
	}

	public String getPhone()
	{
		return this.phone;
	}

	public void setPhone(String phone)
	{
		this.phone = phone;
	}

	public double getAmount()
	{
		return this.amount;
	}

	public void setAmount(double amount)
	{
		this.amount = amount;
	}

	public List getOtherInfo()
	{
		return this.otherInfo;
	}

	public void setOtherInfo(List otherInfo)
	{
		this.otherInfo = otherInfo;
	}

	public String toString()
	{
		Field[] fields = this.getClass().getDeclaredFields();
		StringAppender sa = StringTool.createStringAppender(128);
		sa.append("class:").append(ClassGenerator.getClassName(this.getClass())).append(" [");
		try
		{
			for (int i = 0; i < fields.length; i++)
			{
				Field field = fields[i];
				sa.append(field.getName()).append(':')
						.append(field.get(this)).append(", ");
			}
		}
		catch (Exception ex)
		{
			sa.append("error:").append(ex);
		}
		sa.append(']');
		return sa.toString();
	}

}