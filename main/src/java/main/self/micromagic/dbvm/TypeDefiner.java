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

/**
 * 数据库列类型的定义者.
 */
public class TypeDefiner
		implements EternaObject
{
	public TypeDefiner()
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
		this.types[TypeManager.TYPE_STRING] = new CommonString("String");
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

	public void modifyTypeDefineDesc(TypeDefineDesc desc)
	{
		int id = TypeManager.getTypeId(desc.getConstName());
		this.types[id] = desc;
	}

}

/**
 * 通用的字符串类型定义.
 */
class CommonString extends TypeDefineDesc
{
	public CommonString(String constName)
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