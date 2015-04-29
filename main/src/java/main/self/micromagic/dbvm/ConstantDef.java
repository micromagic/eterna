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

/**
 * 操作类型等常量的定义.
 */
public interface ConstantDef
{
	/**
	 * 操作方式, 创建.
	 */
	int OPT_TYPE_CREATE = 1;

	/**
	 * 操作方式, 修改.
	 */
	int OPT_TYPE_MODIFY = 0;

	/**
	 * 操作方式, 删除.
	 */
	int OPT_TYPE_DROP = -1;

	/**
	 * TableComment对象在工厂中的名称.
	 */
	String TABLE_COMM_NAME = "tableComment";

	/**
	 * TypeDefiner对象在工厂中的名称.
	 */
	String TYPE_DEF_NAME = "typeDefiner";

	/**
	 * ColumnDefiner对象在工厂中的名称.
	 */
	String COLUMN_DEF_NAME = "columnDefiner";

	/**
	 * IndexDefiner对象在工厂中的名称.
	 */
	String INDEX_DEF_NAME = "indexDefiner";

	/**
	 * 通用的更新执行对象.
	 */
	String COMMON_EXEC = "commonExec";

	/**
	 * 工厂的属性中存放是否可以多行的标识.
	 */
	String MUTIPLE_LINE_FLAG = "db.script.mutipleLine";

}
