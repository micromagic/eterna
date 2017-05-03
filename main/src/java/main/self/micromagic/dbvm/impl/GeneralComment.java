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

import self.micromagic.dbvm.TableComment;
import self.micromagic.dbvm.TableDesc;
import self.micromagic.util.StringAppender;
import self.micromagic.util.StringTool;

/**
 * 通用的数据库表注释定义.
 * 使用的库为: Oracle, H2, PostgreSQL
 */
public class GeneralComment
		implements TableComment
{
	public String getComment(TableDesc tableDesc)
	{
		String tableName = tableDesc.tableName;
		if (!StringTool.isEmpty(tableDesc.newName))
		{
			tableName = tableDesc.newName;
		}
		StringAppender buf = StringTool.createStringAppender(56);
		buf.append("comment on table ").append(tableName).append(" is '")
				.append(StringTool.replaceAll(tableDesc.desc, "'", "''")).append("'");
		return buf.toString();
	}

}
