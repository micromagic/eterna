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
import java.util.Calendar;
import java.util.List;

import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;

public class TestMainBean
{
	private String id;
	private String name;
	private java.util.Date birth;
	private Calendar comeDate;
	private int gradeYear;
	private TestSubBean subInfo;

	public String getId()
	{
		return this.id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public java.util.Date getBirth()
	{
		return this.birth;
	}

	public void setBirth(java.util.Date birth)
	{
		this.birth = birth;
	}

	public Calendar getComeDate()
	{
		return this.comeDate;
	}

	public void setComeDate(Calendar comeDate)
	{
		this.comeDate = comeDate;
	}

	public int getGradeYear()
	{
		return this.gradeYear;
	}

	public void setGradeYear(int gradeYear)
	{
		this.gradeYear = gradeYear;
	}

	public TestSubBean getSubInfo()
	{
		return this.subInfo;
	}

	public void setSubInfo(TestSubBean subInfo)
	{
		this.subInfo = subInfo;
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