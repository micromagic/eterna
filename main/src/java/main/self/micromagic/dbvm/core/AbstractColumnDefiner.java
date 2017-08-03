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

package self.micromagic.dbvm.core;

import java.util.List;

import self.micromagic.dbvm.ColumnDefiner;
import self.micromagic.dbvm.ColumnDesc;
import self.micromagic.dbvm.TypeDefiner;
import self.micromagic.eterna.dao.Update;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.eterna.share.EternaFactory;
import self.micromagic.eterna.share.TypeManager;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.ref.BooleanRef;

/**
 * 抽象的数据库列定义.
 */
public abstract class AbstractColumnDefiner extends AbstractObject
		implements ColumnDefiner
{
	protected TypeDefiner typeDefiner;

	public boolean initialize(EternaFactory factory)
			throws EternaException
	{
		if (super.initialize(factory))
		{
			return true;
		}
		this.typeDefiner = (TypeDefiner) factory.createObject(TYPE_DEF_NAME);
		return false;
	}

	/**
	 * 生成默认值的表达式.
	 */
	protected static String makeDefaultExpression(ColumnDesc colDesc, BooleanRef drop)
	{
		if (colDesc.defaultValue == null)
		{
			return null;
		}
		if (colDesc.defaultValue.length() == 0)
		{
			if (drop != null)
			{
				drop.value = true;
			}
			return "default null";
		}
		if (TypeManager.isString(colDesc.typeId))
		{
			String dValue = StringTool.replaceAll(colDesc.defaultValue, "'", "''");
			return "default '".concat(dValue.concat("'"));
		}
		return "default ".concat(colDesc.defaultValue);
	}

	/**
	 * 构造修改列注释的表达式.
	 */
	protected String makeColumnCommon(ColumnDesc colDesc, String tableName, List paramList)
	{
		if (colDesc.desc != null && colDesc.optType != OPT_TYPE_DROP
				&& (!StringTool.isEmpty(colDesc.desc) || colDesc.optType == OPT_TYPE_MODIFY))
		{
			String colName = colDesc.colName;
			if (!StringTool.isEmpty(colDesc.newName))
			{
				colName = colDesc.newName;
			}
			StringAppender buf = StringTool.createStringAppender(72);
			buf.append("comment on column ").append(tableName).append('.')
					.append(colName).append(" is '")
					.append(StringTool.replaceAll(colDesc.desc, "'", "''")).append('\'');
			String script = buf.toString();
			if (paramList != null)
			{
				Update u = this.factory.createUpdate(COMMON_EXEC);
				u.setSubScript(1, script);
				paramList.add(u);
			}
			return script;
		}
		return null;
	}

}
