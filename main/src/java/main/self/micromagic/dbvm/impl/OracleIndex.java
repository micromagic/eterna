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

import self.micromagic.dbvm.AbstractObject;
import self.micromagic.dbvm.IndexDefiner;
import self.micromagic.dbvm.IndexDesc;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;
import self.micromagic.util.ref.ObjectRef;

/**
 * mysql的数据库列定义.
 */
public class OracleIndex extends AbstractObject
		implements IndexDefiner
{
	public String getIndexDefine(IndexDesc indexDesc, ObjectRef param)
	{
		StringAppender buf = StringTool.createStringAppender(128);
		if (indexDesc.optType == OPT_TYPE_CREATE)
		{
			String[] arr = new String[indexDesc.columns.size()];
			indexDesc.columns.toArray(arr);
			if (indexDesc.key)
			{
				buf.append("alter table ").append(indexDesc.tableName)
						.append(" add constraint ")
						.append(indexDesc.indexName).append(" primary key (")
						.append(StringTool.linkStringArr(arr, ", ")).append(") using index");
			}
			else
			{
				buf.append("create ");
				if (indexDesc.unique)
				{
					buf.append("unique ");
				}
				buf.append("index ");
				buf.append(indexDesc.indexName).append(" on ").append(indexDesc.tableName)
						.append(" (").append(StringTool.linkStringArr(arr, ", ")).append(')');
			}
		}
		else if (indexDesc.optType == OPT_TYPE_DROP)
		{
			if (indexDesc.key)
			{
				buf.append("alter table ").append(indexDesc.tableName)
						.append(" drop constraint ").append(indexDesc.indexName)
						.append(" cascade drop index");
			}
			else
			{
				buf.append("drop index ").append(indexDesc.indexName);
			}
		}
		else
		{
			throw new EternaException("Error opt type [" + indexDesc.optType + "].");
		}
		return buf.toString();
	}

}
