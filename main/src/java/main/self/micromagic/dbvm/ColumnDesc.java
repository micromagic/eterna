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
import self.micromagic.eterna.share.TypeManager;

/**
 * 数据库列定义的描述.
 */
public class ColumnDesc
		implements ConstantDef
{
	/**
	 * 列名.
	 */
	public String colName;

	/**
	 * 修改列时, 新的列名.
	 */
	public String newName;

	/**
	 * 列注释.
	 */
	public String desc;

	/**
	 * 是否可空.
	 */
	public boolean nullable = true;

	/**
	 * 类型id.
	 */
	public int typeId;

	/**
	 * 设置类型的名称.
	 */
	public void setTypeName(String typeName)
	{
		this.typeId = TypeManager.getTypeId(typeName);
		if (this.typeId == TypeManager.TYPE_NULL)
		{
			throw new EternaException("Error type [" + typeName + "].");
		}
	}

	/**
	 * 操作方式.
	 */
	public int optType = OPT_TYPE_CREATE;

	/**
	 * 设置操作的名称.
	 */
	public void setOptName(String optName)
	{
		if ("drop".equalsIgnoreCase(optName))
		{
			this.optType = OPT_TYPE_DROP;
		}
		else if("modify".equalsIgnoreCase(optName))
		{
			this.optType = OPT_TYPE_MODIFY;
		}
	}

}
