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

import self.micromagic.dbvm.core.AbstractObject;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.EternaObject;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.ref.IntegerRef;

/**
 * 数据库列类型的定义者.
 */
public class TypeDefiner extends AbstractObject
		implements EternaObject
{
	private final TypeDefineDesc[] types
			= new TypeDefineDesc[TypeManager.TYPES_COUNT];

	public TypeDefiner()
	{
		this.init();
	}

	private void init()
	{
		this.types[TypeManager.TYPE_BOOLEAN] = new TypeDefineDesc("boolean");
		this.types[TypeManager.TYPE_INTEGER] = new TypeDefineDesc("int");
		this.types[TypeManager.TYPE_SHORT] = new TypeDefineDesc("short");
		this.types[TypeManager.TYPE_BYTE] = new TypeDefineDesc("byte");
		this.types[TypeManager.TYPE_LONG] = new TypeDefineDesc("long");
		this.types[TypeManager.TYPE_DOUBLE] = new ComminDouble("double", "numeric");
		this.types[TypeManager.TYPE_STRING] = new CommonString("String");
		TypeDefineDesc date = new TypeDefineDesc("Datetime");
		this.types[TypeManager.TYPE_TIMPSTAMP] = date;
		this.types[TypeManager.TYPE_DATE] = date;
		TypeDefineDesc blob = new TypeDefineDesc("Blob");
		this.types[TypeManager.TYPE_BLOB] = blob;
		this.types[TypeManager.TYPE_STREAM] = blob;
		TypeDefineDesc clob = new TypeDefineDesc("Clob");
		this.types[TypeManager.TYPE_CLOB] = clob;
		this.types[TypeManager.TYPE_BIGSTRING] = clob;
	}

	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		if (super.initialize(factory))
		{
			return true;
		}
		for (int i = 0; i < this.types.length; i++)
		{
			if (this.types[i] != null)
			{
				this.types[i].init(factory);
			}
		}
		return false;
	}

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

class ComminDouble extends TypeDefineDesc
{
	private final String extName;
	private String extDefine;

	public ComminDouble(String constName, String extName)
	{
		super(constName);
		this.extName = extName;
	}

	public void init(EternaFactory factory)
	{
		super.init(factory);
		this.extDefine = factory.getConstantValue(this.extName);
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
				return this.extDefine.concat("(" + ext + "," + sub + ")");
			}
			return this.extDefine.concat("(" + ext + ")");
		}
		return this.define;
	}

}
