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

import self.micromagic.dbvm.IndexDefiner;
import self.micromagic.dbvm.IndexDesc;
import self.micromagic.dbvm.core.AbstractObject;
import self.micromagic.eterna.share.EternaException;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;

/**
 * mysql的数据库索引定义.
 */
public class MySqlIndex extends AbstractObject
		implements IndexDefiner
{
	public String getIndexDefine(IndexDesc indexDesc, List paramList)
	{
		StringAppender buf = StringTool.createStringAppender(128);
		if (indexDesc.optType == OPT_TYPE_CREATE)
		{
			String[] arr = new String[indexDesc.columns.size()];
			indexDesc.columns.toArray(arr);
			if (indexDesc.key)
			{
				buf.append("alter table ").append(indexDesc.tableName)
						.append(" add primary key ")
						.append(indexDesc.indexName).append(" (")
						.append(StringTool.linkStringArr(arr, ", ")).append(')');
			}
			else if (indexDesc.foreign)
			{
				String[] refArr = new String[indexDesc.refColumns.size()];
				indexDesc.refColumns.toArray(refArr);
				buf.append("alter table ").append(indexDesc.tableName)
						.append(" add foreign key ")
						.append(indexDesc.indexName).append(" (")
						.append(StringTool.linkStringArr(arr, ", ")).append(") references ")
						.append(indexDesc.refName).append(" (")
						.append(StringTool.linkStringArr(refArr, ", ")).append(')');
			}
			else
			{
				buf.append("alter table ").append(indexDesc.tableName).append(" add ")
						.append(indexDesc.unique ? "unique " : "index ")
						.append(indexDesc.indexName).append(" (")
						.append(StringTool.linkStringArr(arr, ", ")).append(')');
			}
		}
		else if (indexDesc.optType == OPT_TYPE_DROP)
		{
			if (indexDesc.key)
			{
				buf.append("alter table ").append(indexDesc.tableName)
						.append(" drop primary key");
			}
			else if (indexDesc.foreign)
			{
				buf.append("alter table ").append(indexDesc.tableName)
					.append(" drop foreign key ").append(indexDesc.indexName);
			}
			else
			{
				buf.append("alter table ").append(indexDesc.tableName)
						.append(" drop index ").append(indexDesc.indexName);
			}
		}
		else
		{
			throw new EternaException("Error opt type [" + indexDesc.optType + "].");
		}
		return buf.toString();
	}

}
