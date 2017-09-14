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

package self.micromagic.eterna.dao.impl;

import self.micromagic.eterna.dao.Parameter;
import self.micromagic.eterna.dao.ParameterGenerator;
import self.micromagic.eterna.dao.preparer.CreaterManager;
import self.micromagic.eterna.dao.preparer.PreparerCreater;
import self.micromagic.eterna.dao.preparer.ValuePreparer;
import self.micromagic.eterna.security.PermissionSet;
import self.micromagic.eterna.security.PermissionSetHolder;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.AttributeManager;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.StringTool;

/**
 * 参数对象构造器的实现.
 */
public class ParameterGeneratorImpl extends AbstractGenerator
		implements ParameterGenerator
{
	private String colName;
	private String permission;
	private String type;
	private String prepareName;

	public void setColumnName(String name)
	{
		this.colName = name;
	}

	public void setPermission(String permission)
	{
		this.permission = permission;
	}

	public void setParamType(String type)
	{
		this.type = type;
	}

	public void setPrepareName(String name)
	{
		this.prepareName = name;
	}

	public Object create()
			throws EternaException
	{
		throw new EternaException("You must use createParameter(int).");
	}

	public Parameter createParameter(int paramIndex)
			throws EternaException
	{
		return new ParameterImpl(this.getName(), this.colName, this.type,
				paramIndex, this.prepareName, this.permission, this.attributes);
	}

	public Parameter createParameter(int paramIndex, int[] preparers)
			throws EternaException
	{
		return new DifferentIndexParameter(this.getName(), this.colName, this.type,
				paramIndex, this.prepareName, this.permission, this.attributes, preparers);
	}

}

/**
 * 参数的索引与语句中的索引位置相同的Parameter.
 */
class ParameterImpl
		implements Parameter
{
	protected final String name;
	private final String colName;
	private PermissionSet permissionSet;
	protected final String prepareName;
	protected PreparerCreater prepare;
	protected final int type;
	protected final int index;
	protected final AttributeManager attrs;

	public ParameterImpl(String name, String colName, String typeName,
			int index, String prepareName, String permission, AttributeManager attrs)
	{
		this.name = name;
		this.colName = ScriptParser.checkNameWithKey(colName == null ? name : colName);
		this.type = TypeManager.getTypeId(typeName);
		this.index = index;
		this.prepareName = prepareName;
		this.permissionSet = StringTool.isEmpty(permission) ? null
				: new PermissionSetHolder(permission);
		this.attrs = attrs;
	}

	public void initialize(EternaFactory factory)
			throws EternaException
	{
		if (this.prepare == null)
		{
			this.prepare = CreaterManager.createPreparerCreater(
					this.type, this.prepareName, factory);
			this.attrs.convertType(factory, "parameter");
			this.permissionSet = PermissionSetHolder.getRealPermissionSet(
					factory, this.permissionSet);
		}
	}

	/**
	 * 获取值准备器的名称.
	 */
	String getPepareName()
	{
		return this.prepareName;
	}

	public String getName()
	{
		return this.name;
	}

	public String getColumnName()
	{
		return this.colName;
	}

	public PermissionSet getPermissionSet()
			throws EternaException
	{
		return this.permissionSet;
	}

	public String getTypeName()
	{
		return TypeManager.getTypeName(this.type);
	}

	public int getType()
	{
		return this.type;
	}

	public int getPureType()
	{
		return TypeManager.getPureType(this.type);
	}

	public boolean isMultiple()
	{
		return false;
	}

	public int getIndex()
	{
		return this.index;
	}

	public int getValuePreparerIndex()
	{
		return this.index;
	}

	public int[] listValuePreparerIndex()
	{
		throw this.makeMultipleError();
	}

	public boolean containsValuePreparerIndex(int index)
	{
		return this.index == index;
	}

	public Object getAttribute(String name)
	{
		return this.attrs.getAttribute(name);
	}

	public String[] getAttributeNames()
	{
		return this.attrs.getAttributeNames();
	}

	public ValuePreparer createValuePreparer(String value)
			throws EternaException
	{
		ValuePreparer vp = this.prepare.createPreparer(value);
		vp.setRelativeIndex(this.getValuePreparerIndex());
		vp.setName(this.getName());
		return vp;
	}

	public ValuePreparer[] listValuePreparer(String value)
			throws EternaException
	{
		throw this.makeMultipleError();
	}

	public ValuePreparer createValuePreparer(Object value)
			throws EternaException
	{
		ValuePreparer vp = this.prepare.createPreparer(value);
		vp.setRelativeIndex(this.getValuePreparerIndex());
		vp.setName(this.getName());
		return vp;
	}

	public ValuePreparer[] listValuePreparer(Object value)
			throws EternaException
	{
		throw this.makeMultipleError();
	}

	protected EternaException makeMultipleError()
	{
		return new EternaException("The parameter [" + this.getName() + "] "
				+ (this.isMultiple() ? "is" : "isn't") + " multiple.");
	}

}

/**
 * 参数的索引与语句中的索引位置不同的Parameter.
 */
class DifferentIndexParameter	extends ParameterImpl
{
	protected final int[] prepareIndexArray;

	public DifferentIndexParameter(String name, String colName, String typeName,
			int index, String prepareName, String permission, AttributeManager attrs,
			int[] prepareIndexArray)
	{
		super(name, colName, typeName, index, prepareName, permission, attrs);
		this.prepareIndexArray = prepareIndexArray;
	}

	public boolean isMultiple()
	{
		return this.prepareIndexArray.length != 1;
	}

	public int getValuePreparerIndex()
	{
		if (this.isMultiple())
		{
			throw this.makeMultipleError();
		}
		return this.prepareIndexArray[0];
	}

	public int[] listValuePreparerIndex()
	{
		if (!this.isMultiple())
		{
			throw this.makeMultipleError();
		}
		return this.prepareIndexArray;
	}

	public boolean containsValuePreparerIndex(int index)
	{
		for (int i = 0; i < this.prepareIndexArray.length; i++)
		{
			if (this.prepareIndexArray[i] == index)
			{
				return true;
			}
		}
		return false;
	}

	public ValuePreparer createValuePreparer(String value)
			throws EternaException
	{
		if (this.isMultiple())
		{
			throw this.makeMultipleError();
		}
		return super.createValuePreparer(value);
	}

	public ValuePreparer[] listValuePreparer(String value)
			throws EternaException
	{
		if (!this.isMultiple())
		{
			throw this.makeMultipleError();
		}
		ValuePreparer[] arr = new ValuePreparer[this.prepareIndexArray.length];
		for (int i = 0; i < arr.length; i++)
		{
			arr[i] = this.prepare.createPreparer(value);
			arr[i].setRelativeIndex(this.prepareIndexArray[i]);
			arr[i].setName(this.getName());
		}
		return arr;
	}

	public ValuePreparer createValuePreparer(Object value)
			throws EternaException
	{
		if (this.isMultiple())
		{
			throw this.makeMultipleError();
		}
		return super.createValuePreparer(value);
	}

	public ValuePreparer[] listValuePreparer(Object value)
			throws EternaException
	{
		if (!this.isMultiple())
		{
			throw this.makeMultipleError();
		}
		ValuePreparer[] arr = new ValuePreparer[this.prepareIndexArray.length];
		for (int i = 0; i < arr.length; i++)
		{
			arr[i] = this.prepare.createPreparer(value);
			arr[i].setRelativeIndex(this.prepareIndexArray[i]);
			arr[i].setName(this.getName());
		}
		return arr;
	}

}
