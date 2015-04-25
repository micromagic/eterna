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

import self.micromagic.eterna.share.EternaObject;
import self.micromagic.util.ref.ObjectRef;

/**
 * 数据库列的定义者.
 */
public interface ColumnDefiner extends EternaObject
{
	/**
	 * 获取列的定义.
	 *
	 * @param colName  列名
	 * @param typeId   类型id
	 * @param desc     列注释
	 * @param param    出参
	 * @return  列定义字符串
	 */
	String getColumnDefine(String colName, int typeId, String desc, ObjectRef param);

}
