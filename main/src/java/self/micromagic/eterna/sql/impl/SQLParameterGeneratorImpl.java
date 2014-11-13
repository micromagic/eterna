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

package self.micromagic.eterna.sql.impl;

import self.micromagic.eterna.digester.ConfigurationException;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.eterna.sql.SQLParameter;
import self.micromagic.eterna.sql.SQLParameterGenerator;
import self.micromagic.eterna.sql.preparer.ValuePreparer;
import self.micromagic.eterna.sql.preparer.ValuePreparerCreater;

public class SQLParameterGeneratorImpl extends AbstractGenerator
		implements SQLParameterGenerator
{
	private String colName;
	private String type;
	private String vpcName;

	public void setColumnName(String name)
	{
		this.colName = name;
	}

	public void setParamType(String type)
	{
		this.type = type;
	}

	public void setParamVPC(String vpcName)
	{
		this.vpcName = vpcName;
	}

	public Object create()
			throws ConfigurationException
	{
		throw new ConfigurationException("You must use createParameter(int).");
	}

	public SQLParameter createParameter(int paramIndex)
			throws ConfigurationException
	{
		if (this.type == null)
		{
			this.type = TypeManager.getTypeName(TypeManager.TYPE_OBJECT);
		}
		return new SQLParameterImpl(this.getName(), this.colName, this.type, paramIndex, this.vpcName);
	}

	static class SQLParameterImpl
			implements SQLParameter
	{
		protected String name;
		private String colName;
		protected String vpcName = null;
		protected ValuePreparerCreater vpCreater;
		protected int type;
		protected int index;

		public SQLParameterImpl(String name, String colName, String typeName, int index, String vpaName)
		{
			this.name = name;
			this.colName = colName == null ? name : colName;
			this.type = TypeManager.getTypeId(typeName);
			this.index = index;
			this.vpcName = vpaName;
		}

		public void initialize(EternaFactory factory)
				throws ConfigurationException
		{
			this.vpCreater = factory.createValuePreparerCreater(this.vpcName, this.getPureType());
			if (this.vpCreater == null)
			{
				log.warn("The value preparer generator [" + this.vpcName + "] not found.");
				this.vpCreater = factory.createValuePreparerCreater(this.getPureType());
			}
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

		public ValuePreparer createValuePreparer(String value)
				throws ConfigurationException
		{
			ValuePreparer vp = this.vpCreater.createPreparer(value);
			vp.setRelativeIndex(this.index);
			return vp;
		}

		public ValuePreparer createValuePreparer(Object value)
				throws ConfigurationException
		{
			ValuePreparer vp = this.vpCreater.createPreparer(value);
			vp.setRelativeIndex(this.index);
			return vp;
		}

	}

}