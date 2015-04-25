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

package self.micromagic.dbvm;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.EternaObject;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.ref.IntegerRef;

/**
 * 数据库列类型的定义者.
 */
public class ColumnTypeDefiner
		implements EternaObject
{
	public ColumnTypeDefiner()
	{
		this.init();
	}
	private void init()
	{
		this.types[TypeManager.TYPE_INTEGER] = new TypeDefineDesc("int");
		this.types[TypeManager.TYPE_SHORT] = new TypeDefineDesc("short");
		this.types[TypeManager.TYPE_BYTE] = new TypeDefineDesc("byte");
		this.types[TypeManager.TYPE_LONG] = new TypeDefineDesc("long");
		this.types[TypeManager.TYPE_DOUBLE] = new TypeDefineDesc("double");
		this.types[TypeManager.TYPE_STRING] = new StringDefineDesc("String");
		this.types[TypeManager.TYPE_TIMPSTAMP] = new TypeDefineDesc("Datetime");
		this.types[TypeManager.TYPE_BLOB] = new TypeDefineDesc("Blob");
		this.types[TypeManager.TYPE_CLOB] = new TypeDefineDesc("Clob");
	}
	private final TypeDefineDesc[] types
			= new TypeDefineDesc[TypeManager.TYPES_COUNT];

	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		if (this.initialized)
		{
			return true;
		}
		this.initialized = true;
		for (int i = 0; i < this.types.length; i++)
		{
			if (this.types[i] != null)
			{
				this.types[i].init(factory);
			}
		}
		return false;
	}
	private boolean initialized;

	/**
	 * 根据类型定义的id获取类型定义.
	 */
	public String getTypeDefine(int typeId)
	{
		int pId = TypeManager.getPureType(typeId);
		if (this.types[pId] != null)
		{
			return this.types[pId].getDefine(typeId);
		}
		throw new EternaException("Unknow type [" + TypeManager.getTypeName(typeId) + "].");
	}

	public String getName()
			throws EternaException
	{
		return this.name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	private String name;

	public void modifyTypeDesc(String type, String name)
	{
		int id = TypeManager.getTypeId(type);
		this.types[id] = createSpecialType(name, type);
	}

	private static TypeDefineDesc createSpecialType(String name, String constName)
	{
		if ("mysql.string".equals(name))
		{
			return new MySqlString(constName);
		}
		else if ("oracle.double".equals(name))
		{
			return new OracleDouble(constName);
		}
		throw new EternaException("Unknow type [" + name + "].");
	}

}

/**
 * 类型定义的描述信息.
 */
class TypeDefineDesc
{
	public TypeDefineDesc(String constName)
	{
		this.constName = constName;
	}
	protected final String constName;

	public void init(EternaFactory factory)
	{
		this.define = factory.getConstantValue(this.constName);
	}
	protected String define;

	/**
	 * 获取类型的定义.
	 */
	public String getDefine(int type)
	{
		return this.define;
	}

}

class StringDefineDesc extends TypeDefineDesc
{
	public StringDefineDesc(String constName)
	{
		super(constName);
	}

	/**
	 * 获取类型的定义.
	 */
	public String getDefine(int type)
	{
		int ext = TypeManager.getTypeExtend(type, null);
		return this.define.concat("(" + ext + ")");
	}

}

class MySqlString extends TypeDefineDesc
{
	public MySqlString(String constName)
	{
		super(constName);
	}

	/**
	 * 获取类型的定义.
	 */
	public String getDefine(int type)
	{
		int ext = TypeManager.getTypeExtend(type, null);
		if (ext < 200)
		{
			return this.define.concat("(" + ext + ")");
		}
		else
		{
			return "text(" .concat(ext + ")");
		}
	}

}

class OracleDouble extends TypeDefineDesc
{
	public OracleDouble(String constName)
	{
		super(constName);
	}

	/**
	 * 获取类型的定义.
	 */
	public String getDefine(int type)
	{
		IntegerRef sub = new IntegerRef();
		int ext = TypeManager.getTypeExtend(type, sub);
		if (ext > 0)
		{
			if (sub.value > 0)
			{
				return this.define.concat("(" + ext + "," + sub + ")");
			}
			return this.define.concat("(" + ext + ")");
		}
		return this.define;
	}

}