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

import self.micromagic.eterna.dao.Parameter;
import self.micromagic.eterna.dao.ParameterGenerator;
import self.micromagic.eterna.dao.preparer.CreaterManager;
import self.micromagic.eterna.dao.preparer.PreparerCreater;
import self.micromagic.eterna.dao.preparer.ValuePreparer;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.AttributeManager;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.TypeManager;

public class ParameterGeneratorImpl extends AbstractGenerator
		implements ParameterGenerator
{
	private String colName;
	private String type;
	private String prepareName;

	public void setColumnName(String name)
	{
		this.colName = name;
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
				paramIndex, this.prepareName, this.attributes);
	}

}

class ParameterImpl
		implements Parameter
{
	protected String name;
	private final String colName;
	protected String prepareName;
	protected PreparerCreater prepare;
	protected int type;
	protected int index;
	protected AttributeManager attrs;

	public ParameterImpl(String name, String colName, String typeName,
			int index, String prepareName, AttributeManager attrs)
	{
		this.name = name;
		this.colName = colName == null ? name : colName;
		this.type = TypeManager.getTypeId(typeName);
		this.index = index;
		this.prepareName = prepareName;
		this.attrs = attrs;
	}

	public void initialize(EternaFactory factory)
			throws EternaException
	{
		this.prepare = CreaterManager.createPrepareCreater(this.type, this.prepareName, factory);
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

	public int getType()
	{
		return this.type;
	}

	public int getPureType()
	{
		return TypeManager.getPureType(this.type);
	}

	public int getIndex()
	{
		return this.index;
	}

	public String getTypeName()
	{
		return TypeManager.getTypeName(this.type);
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
		vp.setRelativeIndex(this.index);
		return vp;
	}

	public ValuePreparer createValuePreparer(Object value)
			throws EternaException
	{
		ValuePreparer vp = this.prepare.createPreparer(value);
		vp.setRelativeIndex(this.index);
		return vp;
	}

}