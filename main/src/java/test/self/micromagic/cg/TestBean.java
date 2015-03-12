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

import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;

public class TestBean
{
	private boolean booleanValue = true;
	private byte byteValue = 1;
	private char charValue = 'e';
	private short shortValue = 2;
	private int intValue = 3;
	private long longValue = 4L;
	private float floatValue = 5.1f;
	private double doubleValue = 6.2;
	private Boolean booleanValue2 = Boolean.FALSE;
	private Byte byteValue2 = new Byte((byte) 11);
	private Character charValue2 = new Character('好');
	private Short shortValue2 = new Short((short) 12);
	private Integer intValue2 = new Integer(13);
	private Long longValue2 = new Long(14L);
	private Float floatValue2 = new Float(15.1f);
	private Double doubleValue2 = new Double(16.2);
	private String stringValue = "test 测试";

	public int publicIntValue = 100;
	public char publicCharValue = '大';
	public double publicDoubleValue = 55.7926;
	public Boolean publicBooleanValue = Boolean.TRUE;
	public String publicStringValue = "ok?";

	public String getStringValue()
	{
		return this.stringValue;
	}

	public void setStringValue(String stringValue)
	{
		this.stringValue = stringValue;
	}

	public boolean isBooleanValue()
	{
		return this.booleanValue;
	}

	public void setBooleanValue(boolean booleanValue)
	{
		this.booleanValue = booleanValue;
	}

	public byte getByteValue()
	{
		return this.byteValue;
	}

	public void setByteValue(byte byteValue)
	{
		this.byteValue = byteValue;
	}

	public char getCharValue()
	{
		return this.charValue;
	}

	public void setCharValue(char charValue)
	{
		this.charValue = charValue;
	}

	public short getShortValue()
	{
		return this.shortValue;
	}

	public void setShortValue(short shortValue)
	{
		this.shortValue = shortValue;
	}

	public int getIntValue()
	{
		return this.intValue;
	}

	public void setIntValue(int intValue)
	{
		this.intValue = intValue;
	}

	public long getLongValue()
	{
		return this.longValue;
	}

	public void setLongValue(long longValue)
	{
		this.longValue = longValue;
	}

	public float getFloatValue()
	{
		return this.floatValue;
	}

	public void setFloatValue(float floatValue)
	{
		this.floatValue = floatValue;
	}

	public double getDoubleValue()
	{
		return this.doubleValue;
	}

	public void setDoubleValue(double doubleValue)
	{
		this.doubleValue = doubleValue;
	}

	public Boolean getBooleanValue2()
	{
		return this.booleanValue2;
	}

	public void setBooleanValue2(Boolean booleanValue2)
	{
		this.booleanValue2 = booleanValue2;
	}

	public Byte getByteValue2()
	{
		return this.byteValue2;
	}

	public void setByteValue2(Byte byteValue2)
	{
		this.byteValue2 = byteValue2;
	}

	public Character getCharValue2()
	{
		return this.charValue2;
	}

	public void setCharValue2(Character charValue2)
	{
		this.charValue2 = charValue2;
	}

	public Short getShortValue2()
	{
		return this.shortValue2;
	}

	public void setShortValue2(Short shortValue2)
	{
		this.shortValue2 = shortValue2;
	}

	public Integer getIntValue2()
	{
		return this.intValue2;
	}

	public void setIntValue2(Integer intValue2)
	{
		this.intValue2 = intValue2;
	}

	public Long getLongValue2()
	{
		return this.longValue2;
	}

	public void setLongValue2(Long longValue2)
	{
		this.longValue2 = longValue2;
	}

	public Float getFloatValue2()
	{
		return this.floatValue2;
	}

	public void setFloatValue2(Float floatValue2)
	{
		this.floatValue2 = floatValue2;
	}

	public Double getDoubleValue2()
	{
		return this.doubleValue2;
	}

	public void setDoubleValue2(Double doubleValue2)
	{
		this.doubleValue2 = doubleValue2;
	}

	public String toString()
	{
		Field[] fields = this.getClass().getDeclaredFields();
		StringAppender sa = StringTool.createStringAppender(128);
		sa.append("class:").append(ClassGenerator.getClassName(this.getClass()))
				.append(" {").appendln();
		try
		{
			for (int i = 0; i < fields.length; i++)
			{
				Field field = fields[i];
				sa.append("   ").append(field.getName()).append(':')
						.append(field.get(this)).appendln();
			}
		}
		catch (Exception ex)
		{
			sa.append("error:").append(ex);
		}
		sa.append('}');
		return sa.toString();
	}

}