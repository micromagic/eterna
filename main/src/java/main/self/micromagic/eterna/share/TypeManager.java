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

package self.micromagic.eterna.share;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import self.micromagic.util.Utility;
import self.micromagic.util.converter.ConverterFinder;
import self.micromagic.util.converter.ValueConverter;
import self.micromagic.util.ref.IntegerRef;

public class TypeManager
{
	/**
	 * 没有类型的标识.
	 */
	public static final int TYPE_NONE = -1;

	/**
	 * 空类型.
	 */
	public static final int TYPE_NULL = 0;

	/**
	 * 字符串类型.
	 */
	public static final int TYPE_STRING = 1;

	/**
	 * 整型.
	 */
	public static final int TYPE_INTEGER = 2;

	/**
	 * 双精度浮点型.
	 */
	public static final int TYPE_DOUBLE = 3;

	/**
	 * 字节数组.
	 */
	public static final int TYPE_BYTES = 4;

	/**
	 * 布尔型.
	 */
	public static final int TYPE_BOOLEAN = 5;

	/**
	 * 日期型.
	 */
	public static final int TYPE_DATE = 6;

	/**
	 * 日期时间型.
	 */
	public static final int TYPE_TIMPSTAMP = 7;

	/**
	 * 长整型.
	 */
	public static final int TYPE_LONG = 8;

	/**
	 * 时间型.
	 */
	public static final int TYPE_TIME = 9;

	/**
	 * 短整型.
	 */
	public static final int TYPE_SHORT = 10;

	/**
	 * 字节型.
	 */
	public static final int TYPE_BYTE = 11;

	/**
	 * 浮点型.
	 */
	public static final int TYPE_FLOAT = 12;

	/**
	 * 复杂对象型.
	 */
	public static final int TYPE_OBJECT = 13;

	/**
	 * 大字符串类型.
	 */
	public static final int TYPE_BIGSTRING = 14;

	/**
	 * 字节流类型.
	 */
	public static final int TYPE_STREAM = 15;

	/**
	 * 字符流类型.
	 */
	public static final int TYPE_CHARS = 16;

	/**
	 * 十进制数类型.
	 */
	public static final int TYPE_DECIMAL = 17;

	/**
	 * 大数据块类型.
	 */
	public static final int TYPE_BLOB = 18;

	/**
	 * 大字符快类型.
	 */
	public static final int TYPE_CLOB = 19;

	/**
	 * 可使用的类型个数.
	 */
	public static final int TYPES_COUNT = 20;

	// 类型名称->索引值的对应表
	private static Map typeMap = new HashMap();

	// 对于SQL类型的数组
	private static int[] SQL_TYPES = new int[]{
		Types.NULL,           //TYPE_NULL
		Types.VARCHAR,        //TYPE_STRING
		Types.INTEGER,        //TYPE_INTEGER
		Types.DOUBLE,         //TYPE_DOUBLE
		Types.BINARY,         //TYPE_BYTES
		Types.BIT,            //TYPE_BOOLEAN
		Types.DATE,           //TYPE_DATE
		Types.TIMESTAMP,      //TYPE_TIMPSTAMP
		Types.BIGINT,         //TYPE_LONG
		Types.TIME,           //TYPE_TIME
		Types.SMALLINT,       //TYPE_SHORT
		Types.TINYINT,        //TYPE_BYTE
		Types.FLOAT,          //TYPE_FLOAT
		Types.OTHER,          //TYPE_OBJECT
		Types.LONGVARCHAR,    //TYPE_BIGSTRING
		Types.LONGVARBINARY,  //TYPE_STREAM
		Types.LONGVARCHAR,    //TYPE_READER
		Types.DECIMAL,        //TYPE_DECIMAL
		Types.BLOB,           //TYPE_BLOB
		Types.CLOB            //TYPE_CLOB
	};

	// 对应类型名称的数组
	private static String[] typeNames = {
		"null",
		"String",
		"int",
		"double",
		"Bytes",
		"boolean",
		"Date",
		"Timestamp",
		"long",
		"Time",
		"short",
		"byte",
		"float",
		"Object",
		"BigString",
		"Stream",
		"Chars",
		"Decimal",
		"Blob",
		"Clob"
	};

	// 对应类型转换器的数组
	private static ValueConverter[] converters;

	// 对应java类型的数组
	private static Class[] javaTypes = {
		null,
		String.class,
		int.class,
		double.class,
		byte[].class,
		boolean.class,
		java.sql.Date.class,
		java.sql.Timestamp.class,
		long.class,
		java.sql.Time.class,
		short.class,
		byte.class,
		float.class,
		Object.class,
		String.class,
		InputStream.class,
		Reader.class,
		BigDecimal.class,
		Blob.class,
		Clob.class
	};

	static
	{
		typeMap.put("none", Utility.INTEGER_MINUS1);
		typeMap.put("null", Utility.INTEGER_0);
		typeMap.put("String", Utility.INTEGER_1);
		typeMap.put("string", Utility.INTEGER_1);
		typeMap.put("int", Utility.INTEGER_2);
		typeMap.put("double", Utility.INTEGER_3);
		typeMap.put("Bytes", Utility.INTEGER_4);
		typeMap.put("bytes", Utility.INTEGER_4);
		typeMap.put("boolean", Utility.INTEGER_5);
		typeMap.put("Date", Utility.INTEGER_6);
		typeMap.put("date", Utility.INTEGER_6);
		typeMap.put("Timestamp", Utility.INTEGER_7);
		typeMap.put("timestamp", Utility.INTEGER_7);
		typeMap.put("Datetime", Utility.INTEGER_7); // 兼容Datetime的设置
		typeMap.put("datetime", Utility.INTEGER_7); // 兼容Datetime的设置
		typeMap.put("long", Utility.INTEGER_8);
		typeMap.put("Time", Utility.INTEGER_9);
		typeMap.put("time", Utility.INTEGER_9);
		typeMap.put("short", Utility.INTEGER_10);
		typeMap.put("byte", Utility.INTEGER_11);
		typeMap.put("float", Utility.INTEGER_12);
		typeMap.put("Object", Utility.INTEGER_13);
		typeMap.put("object", Utility.INTEGER_13);
		typeMap.put("BigString", Utility.INTEGER_14);
		typeMap.put("bigstring", Utility.INTEGER_14);
		typeMap.put("Stream", Utility.INTEGER_15);
		typeMap.put("stream", Utility.INTEGER_15);
		Integer tmp;

		tmp = new Integer(16);
		typeMap.put("Reader", tmp);
		typeMap.put("reader", tmp);
		tmp = new Integer(17);
		typeMap.put("Decimal", tmp);
		typeMap.put("decimal", tmp);
		tmp = new Integer(18);
		typeMap.put("Blob", tmp);
		typeMap.put("blob", tmp);
		tmp = new Integer(19);
		typeMap.put("Clob", tmp);
		typeMap.put("clob", tmp);

		converters = new ValueConverter[javaTypes.length];
		for (int i = 0; i < javaTypes.length; i++)
		{
			Class type = javaTypes[i];
			if (type != null)
			{
				converters[i] = ConverterFinder.findConverter(type, true);
				if (converters[i] != null)
				{
					converters[i].setNeedThrow(false);
				}
			}
		}
	}

	/**
	 * 根据类型的名称获得类型的id.
	 *
	 * @param name   类型的名称
	 * @return    与类型名称对应的id, 如果该类型名称没有对应的类型id则
	 *            返回TYPE_NONE
	 */
	public static int getTypeId(String name)
	{
		if (name == null)
		{
			return TYPE_NONE;
		}
		int param = 0;
		name = name.trim();
		if (name.endsWith(")"))
		{
			int index = name.lastIndexOf('(');
			String temp = name.substring(index + 1, name.length() - 1);
			name = name.substring(0, index);
			index = temp.indexOf(',');
			try
			{
				if (index == -1)
				{
					param = Integer.parseInt(temp.trim()) << 8;
				}
				else
				{
					param = (Integer.parseInt(temp.substring(0, index).trim()) << 8)
							| (Integer.parseInt(temp.substring(index + 1).trim()) << 24);
				}
			}
			catch (Exception ex){}
		}
		Integer i = (Integer) typeMap.get(name);
		if (i == null)
		{
			Tool.log.warn("Error type [" + name + "].");
			return TYPE_NONE;
		}
		return i.intValue() | param;
	}

	/**
	 * 获取纯类型id.
	 * 纯类型id保存在类型id的低8位, 有效类型id还包括其它参数,
	 * 如: 长度 精度等.
	 *
	 * @param id  类型的id
	 * @return   纯类型id, 如果给出的类型id无效则返回TYPE_NONE
	 */
	public static int getPureType(int id)
	{
		int realId = id & 0xff;
		if (realId < 0 || realId >= typeNames.length)
		{
			return TYPE_NONE;
		}
		return realId;
	}

	/**
	 * 根据类型id获取纯类型的名称.
	 *
	 * @param id  类型的id
	 * @return  与此类型id对应的纯类型名称, 如果给出的类型id无效
	 *          则返回null
	 */
	public static String getPureTypeName(int id)
	{
		return getTypeName(getPureType(id));
	}

	/**
	 * 根据类型id获取类型的名称.
	 *
	 * @param id  类型的id
	 * @return  与此类型id对应的类型名称, 如果给出的类型id无效
	 *          则返回null
	 */
	public static String getTypeName(int id)
	{
		int realId = id & 0xff;
		if (realId < 0 || realId >= typeNames.length)
		{
			return null;
		}
		String temp = typeNames[realId];
		if (id > 0xffffff || id < 0)
		{
			temp += "(" + ((id & 0xffff00) >> 8) + "," + ((id & 0xff000000) >>> 24) + ")";
		}
		else if (id > 0xff)
		{
			temp += "(" + ((id & 0xffff00) >> 8) + ")";
		}
		return temp;
	}

	/**
	 * 根据类型id获取类型的扩展定义.
	 *
	 * @param id   类型的id
	 * @param sub  出参, 子扩展部分
	 * @return  主扩展部分, -1 没有扩展
	 */
	public static int getTypeExtend(int id, IntegerRef sub)
	{
		int realId = id & 0xff;
		if (realId < 0 || realId >= typeNames.length)
		{
			return -1;
		}
		int r = (id & 0xffff00) >> 8;
		if (sub != null)
		{
			sub.value = (id & 0xff000000) >>> 24;
		}
		return r;
	}

	/**
	 * 将一个java.sql.Types里的类型id翻译成这里的类型.
	 */
	public static int transSQLType(int sqlType)
	{
		for (int i = 0; i < SQL_TYPES.length; i++)
		{
			if (sqlType == SQL_TYPES[i])
			{
				return i;
			}
		}
		// 如果没有匹配的, 默认为object类型
		return TYPE_OBJECT;
	}

	/**
	 * 获取类型id对应的SQL的类型.
	 *
	 * @param id  类型的id
	 * @return  SQL的类型, 如果给出的类型id无效则返回Types.NULL
	 */
	public static int getSQLType(int id)
	{
		int realId = id & 0xff;
		if (realId < 0 || realId >= typeNames.length)
		{
			return Types.NULL;
		}
		return SQL_TYPES[realId];
	}

	/**
	 * 获取类型id对应的Java的类型.
	 *
	 * @param id  类型的id
	 * @return  Java的类型, 如果给出的类型id无效则返回null
	 */
	public static Class getJavaType(int id)
	{
		int realId = id & 0xff;
		if (realId < 0 || realId >= javaTypes.length)
		{
			return null;
		}
		return javaTypes[realId];
	}

	/**
	 * 获取类型id对应的类型转换器.
	 *
	 * @param id  类型的id
	 * @return  类型转换器, 如果给出的类型id无效则返回null
	 */
	public static ValueConverter getConverter(int id)
	{
		int realId = id & 0xff;
		if (realId < 0 || realId >= converters.length)
		{
			return null;
		}
		return converters[realId];
	}

	/**
	 * 判断给出的类型id是否是一个布尔类型.
	 *
	 * @param id  类型的id
	 * @return  true表示此类型id为一个数字类型
	 */
	public static boolean isBoolean(int id)
	{
		int realId = id & 0xff;
		if (realId < 0 || realId >= typeNames.length)
		{
			return false;
		}
		return realId == TYPE_BOOLEAN;
	}

	/**
	 * 判断给出的类型id是否是一个数字类型.
	 *
	 * @param id  类型的id
	 * @return  true表示此类型id为一个数字类型
	 */
	public static boolean isNumber(int id)
	{
		int realId = id & 0xff;
		if (realId < 0 || realId >= typeNames.length)
		{
			return false;
		}
		return realId == TYPE_INTEGER || realId == TYPE_DOUBLE || realId == TYPE_LONG
				|| realId == TYPE_SHORT || realId == TYPE_BYTE || realId == TYPE_FLOAT
				|| realId == TYPE_DECIMAL;
	}

	/**
	 * 判断给出的类型id是否是一个日期类型.
	 *
	 * @param id  类型的id
	 * @return  true表示此类型id为一个日期类型
	 */
	public static boolean isDate(int id)
	{
		int realId = id & 0xff;
		if (realId < 0 || realId >= typeNames.length)
		{
			return false;
		}
		return realId == TYPE_DATE || realId == TYPE_TIME || realId == TYPE_TIMPSTAMP;
	}

	/**
	 * 判断给出的类型id是否是一个字符串类型.
	 *
	 * @param id  类型的id
	 * @return  true表示此类型id为一个字符串类型
	 */
	public static boolean isString(int id)
	{
		int realId = id & 0xff;
		if (realId < 0 || realId >= typeNames.length)
		{
			return false;
		}
		return realId == TYPE_STRING || realId == TYPE_BIGSTRING;
	}

}