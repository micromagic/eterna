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

package self.micromagic.eterna.search.impl;

import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.search.ConditionProperty;
import self.micromagic.eterna.search.ConditionPropertyGenerator;
import self.micromagic.eterna.share.AbstractGenerator;
import self.micromagic.eterna.share.TypeManager;

/**
 * @author micromagic@sina.com
 */
public class ConditionPropertyGeneratorImpl extends AbstractGenerator
		implements ConditionPropertyGenerator
{
	private ConditionPropertyImpl conditionProperty = new ConditionPropertyImpl();

	private String columnName;
	private String columnType;
	private String inputType;

	public void setColumnName(String name)
	{
		this.columnName = name;
	}

	public void setColumnCaption(String caption)
	{
		this.conditionProperty.columnCaption = caption;
	}

	public void setColumnType(String type)
	{
		this.columnType = type;
	}

	public void setColumnVPC(String vpcName)
	{
		this.conditionProperty.vpcName = vpcName;
	}

	public void setConditionInputType(String type)
	{
		this.inputType = type;
	}

	public void setDefaultValue(String value)
	{
		this.conditionProperty.defaultValue = value;
	}

	public void setVisible(boolean visible)
	{
		this.conditionProperty.visible = visible;
	}

	public void setPermissions(String permissions)
	{
		this.conditionProperty.permissions = permissions;
	}

	public void setUseDefaultConditionBuilder(boolean use)
	{
		this.conditionProperty.useDefaultConditionBuilder = use;
	}

	public void setDefaultConditionBuilderName(String name)
	{
		this.conditionProperty.defaultBuilderName = name;
	}

	public void setConditionBuilderListName(String name)
	{
		this.conditionProperty.listName = name;
	}

	public Object create()
			throws EternaException
	{
		return this.createConditionProperty();
	}

	public ConditionProperty createConditionProperty()
			throws EternaException
	{
		this.conditionProperty.name = this.name;
		this.conditionProperty.columnName = this.columnName == null ? this.name : this.columnName;
		this.conditionProperty.columnType = TypeManager.getTypeId(this.columnType);
		if (this.conditionProperty.columnType == TypeManager.TYPE_IGNORE)
		{
			this.conditionProperty.ignore = true;
		}
		this.conditionProperty.inputType = this.inputType == null ? "text" : this.inputType;
		if (this.conditionProperty.listName == null)
		{
			if (this.conditionProperty.inputType != null
					&& this.conditionProperty.inputType.toLowerCase().startsWith("select"))
			{
				this.conditionProperty.listName = "cbl_List";
			}
			else
			{
				if (TypeManager.isTypeString(this.conditionProperty.columnType))
				{
					this.conditionProperty.listName = "cbl_String";
				}
				else
				{
					this.conditionProperty.listName = "cbl_Other";
				}
			}
		}
		this.conditionProperty.attributes = this.attributes;
		return this.conditionProperty;
	}

}