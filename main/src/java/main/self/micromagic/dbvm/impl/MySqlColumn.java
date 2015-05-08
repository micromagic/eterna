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

package self.micromagic.dbvm.impl;

import java.util.List;

import self.micromagic.dbvm.AbstractObject;
import self.micromagic.dbvm.ColumnDefiner;
import self.micromagic.dbvm.ColumnDesc;
import self.micromagic.dbvm.TableDesc;
import self.micromagic.eterna.dao.Update;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.ref.BooleanRef;

/**
 * mysql的数据库列定义.
 */
public class MySqlColumn extends AbstractObject
		implements ColumnDefiner
{
	public String getColumnDefine(TableDesc tableDesc, ColumnDesc colDesc, List paramList)
	{
		String tableName = tableDesc.tableName;
		if (!StringTool.isEmpty(tableDesc.newName))
		{
			tableName = tableDesc.newName;
		}
		StringAppender buf = StringTool.createStringAppender(16);
		BooleanRef dropDefault = new BooleanRef();
		String defExp = makeDefaultExpression(colDesc, dropDefault);
		if (colDesc.optType == OPT_TYPE_CREATE)
		{
			if (tableDesc.optType == OPT_TYPE_CREATE)
			{
				buf.append(colDesc.colName).append(' ')
						.append(this.typeDefiner.getTypeDefine(colDesc.typeId));
				if (!colDesc.nullable)
				{
					buf.append(" not null");
				}
				if (defExp != null)
				{
					buf.append(' ').append(defExp);
				}
			}
			else
			{
				buf.append("alter table ").append(tableName).append(" add column ")
						.append(colDesc.colName).append(' ')
						.append(this.typeDefiner.getTypeDefine(colDesc.typeId));
				if (!colDesc.nullable)
				{
					buf.append(" not null");
				}
				if (defExp != null)
				{
					StringAppender s = StringTool.createStringAppender(16);
					s.append("alter table ").append(tableName).append(" alter column ")
							.append(colDesc.colName).append(" set ").append(defExp);
					Update u = this.factory.createUpdate(COMMON_EXEC);
					u.setSubScript(1, s.toString());
					if (paramList != null)
					{
						paramList.add(u);
					}
				}
			}
		}
		else if (colDesc.optType == OPT_TYPE_MODIFY)
		{
			buf.append("alter table ").append(tableName).append(" change column ")
					.append(colDesc.colName).append(' ');
			if (!StringTool.isEmpty(colDesc.newName))
			{
				buf.append(colDesc.newName);
			}
			else
			{
				buf.append(colDesc.colName);
			}
			buf.append(this.typeDefiner.getTypeDefine(colDesc.typeId));
			if (!colDesc.nullable)
			{
				buf.append(" not null");
			}
			if (defExp != null)
			{
				StringAppender s = StringTool.createStringAppender(16);
				s.append("alter table ").append(tableName).append(" alter column ")
						.append(colDesc.colName);
				if (dropDefault.value)
				{
					buf.append(" drop default");
				}
				else
				{
					buf.append(" set ").append(defExp);
				}
				Update u = this.factory.createUpdate(COMMON_EXEC);
				u.setSubScript(1, s.toString());
				if (paramList != null)
				{
					paramList.add(u);
				}
			}
		}
		else if (colDesc.optType == OPT_TYPE_DROP)
		{
			buf.append("alter table ").append(tableName).append(" drop column ")
					.append(colDesc.colName);
		}
		else
		{
			throw new EternaException("Error opt type [" + colDesc.optType + "].");
		}
		if (colDesc.desc != null && colDesc.optType != OPT_TYPE_DROP)
		{
			if (!StringTool.isEmpty(colDesc.desc) || colDesc.optType == OPT_TYPE_MODIFY)
			{
				buf.append(" comment '")
						.append(StringTool.replaceAll(colDesc.desc, "'", "''")).append("'");
			}
		}
		return buf.toString();
	}

}